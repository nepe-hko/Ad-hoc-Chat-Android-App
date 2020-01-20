package com.example.bachelorarbeit;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.bachelorarbeit.test.TestServer;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

public class NearbyConnectionHandler implements Discoverer {

    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String SERVICE_ID  = "hko_app";
    private final Context context;
    private final String myID;
    //private final List<String> connectedDevices;
    private final Hashtable<String,String> connectedDevices;
    private final NearbyReceiver receiver;
    private final TimeoutManager discoveryTimeoutManager;
    private final ConnectionsClient connectionsClient;
    private final TestServer testServer;

    NearbyConnectionHandler(Context context, String myID, TestServer testServer, NearbyReceiver receiver) {
        this.testServer = testServer;
        this.context = context;
        this.myID = myID;
        this.receiver = receiver;
        this.connectedDevices = new Hashtable<>();
        this.discoveryTimeoutManager = new TimeoutManager(this, myID, testServer);
        this.connectionsClient = Nearby.getConnectionsClient(context);

        startAdvertise();
    }

    private ConnectionLifecycleCallback clc = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointID, @NonNull ConnectionInfo connectionInfo) {

            connectedDevices.put(connectionInfo.getEndpointName(), endpointID);

            testServer.echo(myID + " is connected to " + connectionInfo.getEndpointName());

            connectionsClient.acceptConnection(myID, new PayloadCallback() {
                    @Override
                    public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                        receiver.receive(payload.asBytes());
                        testServer.echo(myID + ": Payload received");
                    }

                    @Override
                    public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                        // not used
                        testServer.echo(myID + ": Payload transferupdate");
                    }
                })
                .addOnSuccessListener(aVoid -> {
                   // not used
                });
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            if(connectionResolution.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {

            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            connectedDevices.remove(s);
            testServer.echo(myID + " lost connection to " + s);
        }
    };

    private void startAdvertise() {

        AdvertisingOptions options = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(myID, SERVICE_ID, clc, options)
                .addOnSuccessListener( (Void unused) -> {
                    Log.d("Advertise", "start Advertise...");
                    testServer.echo(myID + ": start Advertise");
                })
                .addOnFailureListener( (Exception e) -> Log.e("Advertise", "not able to Advertise"));
    }


    private void startDiscover() {

        testServer.echo(myID + ": start Discover");
/*
        if (discoveryTimeoutManager.isDiscovery()) {
            return;
        }
*/
        discoveryTimeoutManager.startTimer();

        EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String endpointID, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                //testServer.echo(myID + ": " + endpointID + " found");
                connectionsClient.requestConnection(myID, endpointID, clc)
                        .addOnSuccessListener((Void unused) -> {
                            connectedDevices.put(discoveredEndpointInfo.getEndpointName(), endpointID);
                            //testServer.echo(myID + " is connected to " + discoveredEndpointInfo.getEndpointName());
                        })
                        .addOnFailureListener((Exception e) -> Log.e("Discover", "Could not connect to Endpoint " + discoveredEndpointInfo.getEndpointName()));
            }

            @Override
            public void onEndpointLost(@NonNull String endpointID) {
                Log.d("Discover", "The previously discovered endpoint (" + endpointID + ") has gone away");
                connectedDevices.remove(endpointID);
                testServer.echo(myID + " lost connection to " + endpointID);
            }
        };

        DiscoveryOptions options = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
                .addOnSuccessListener( (Void unused ) -> Log.e("Discover", "start Discover..."))
                .addOnFailureListener( (Exception e) -> Log.e("Discover", "Unable to Discover"));
    }

    @Override
    public void onDiscoveryTimerExpired() {
        connectionsClient.stopDiscovery();
        testServer.echo(myID + ": stop Discover");
        Log.e("Discover", "...stop Discover");
    }


    // liefert die gewünschte EndpointID zurück, wenn verbunden
    @RequiresApi(api = Build.VERSION_CODES.N)
    CompletableFuture<String> connect(String endpointID) {


        if(connectedDevices.containsKey(endpointID)) {
            return CompletableFuture.completedFuture(connectedDevices.get(endpointID));
        }

        startDiscover();

        return CompletableFuture.supplyAsync( () -> {
            while(true) {
                if (connectedDevices.containsKey(endpointID)) break;
            }
            return connectedDevices.get(endpointID);
        });

    }

    // liefert nach 10 Sekunden alle verbundenen Endpoints zurück
    @RequiresApi(api = Build.VERSION_CODES.N)
    CompletableFuture<Hashtable<String,String>> connectAll() {

        testServer.echo(myID + ": connectAll()");

        startDiscover();
        Log.d("test", "before return");
        return CompletableFuture.supplyAsync( () -> {
            /*
            try {
                Log.d("test", "wait 10s...");

                wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(15));
            Log.d("test", "return connected Devices");

            return connectedDevices;
        });
    }

    public Hashtable<String, String> getAllConnected() {
        return this.connectedDevices;
    }

}
