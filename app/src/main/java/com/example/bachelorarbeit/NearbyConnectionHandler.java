package com.example.bachelorarbeit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NearbyConnectionHandler implements Discoverer {

    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String SERVICE_ID  = "hko_app";
    private final Context context;
    private final String myID;
    private final List<String> connectedDevices;
    private final NearbyReceiver receiver;
    private final TimeoutManager discoveryTimeoutManager;
    private final ConnectionsClient connectionsClient;

    NearbyConnectionHandler(Context context, String myID, NearbyReceiver receiver) {
        this.context = context;
        this.myID = myID;
        this.receiver = receiver;
        this.connectedDevices = Collections.synchronizedList(new ArrayList<>());
        this.discoveryTimeoutManager = new TimeoutManager(this);
        this.connectionsClient = Nearby.getConnectionsClient(context);
        startAdvertise();
    }

    private ConnectionLifecycleCallback clc = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
            connectionsClient.acceptConnection(myID, new PayloadCallback() {
                    @Override
                    public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                        receiver.receive(payload.asBytes());
                    }

                    @Override
                    public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                        // not used
                    }
                })
                .addOnSuccessListener(aVoid -> {
                   // not used
                });
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            if(connectionResolution.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
                connectedDevices.add(s);
            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            connectedDevices.remove(s);
        }
    };

    private void startAdvertise() {

        AdvertisingOptions options = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(myID, SERVICE_ID, clc, options)
                .addOnSuccessListener( (Void unused) -> Log.d("Advertise", "start Advertise..."))
                .addOnFailureListener( (Exception e) -> Log.e("Advertise", "not able to Advertise"));
    }


    private void startDiscover() {

        discoveryTimeoutManager.startTimer();

        if (discoveryTimeoutManager.isDiscovery()) {
            return;
        }

        EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String endpointID, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                connectionsClient.requestConnection(myID, endpointID, clc)
                        .addOnSuccessListener((Void unused) -> connectedDevices.add(endpointID))
                        .addOnFailureListener((Exception e) -> Log.e("Discover", "Could not connect to Endpoint " + endpointID));
            }

            @Override
            public void onEndpointLost(@NonNull String endpointID) {
                Log.d("Discover", "The previously discovered endpoint (" + endpointID + ") has gone away");
                connectedDevices.remove(endpointID);
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
        Log.e("Discover", "...stop Discover");
    }


    // liefert die gewünschte EndpointID zurück, wenn verbunden
    CompletableFuture<String> connect(String endpointID) {

        if(connectedDevices.contains(endpointID)) {
            return CompletableFuture.completedFuture(endpointID);
        }

        startDiscover();

        return CompletableFuture.supplyAsync( () -> {
            while(true) {
                if (connectedDevices.contains(endpointID)) break;
            }
            return endpointID;
        });

    }

    // liefert nach 10 Sekunden alle verbundenen Endpoints zurück
    CompletableFuture<List<String>> connectAll() {

        startDiscover();

        return CompletableFuture.supplyAsync( () -> {
            try {
                wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return connectedDevices;
        });
    }

    public List<String> getAllConnected() {
        return this.connectedDevices;
    }

}
