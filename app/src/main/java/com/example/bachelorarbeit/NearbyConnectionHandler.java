package com.example.bachelorarbeit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import java9.util.concurrent.CompletableFuture;

public class NearbyConnectionHandler implements Discoverer {

    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String SERVICE_ID  = "hko_app";
    private final Context context;
    private final String myID;

    private final Map<String,String> connectedDevices;
    private final Map<String,String> pendingDevices;
    private final NearbyReceiver receiver;
    private final TimeoutManager discoveryTimeoutManager;
    private final ConnectionsClient connectionsClient;
    private final TestServer testServer;

    NearbyConnectionHandler(Context context, String myID, TestServer testServer, NearbyReceiver receiver) {
        this.testServer = testServer;
        this.context = context;
        this.myID = myID;
        this.receiver = receiver;
        this.connectedDevices = new HashMap<>();
        this.pendingDevices = new HashMap<>();
        this.discoveryTimeoutManager = new TimeoutManager(this, testServer);
        this.connectionsClient = Nearby.getConnectionsClient(context);

        startAdvertise();
    }

    private ConnectionLifecycleCallback clc = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointID, @NonNull ConnectionInfo connectionInfo) {
            pendingDevices.put(endpointID, connectionInfo.getEndpointName());

            acceptConnection(endpointID);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointID, @NonNull ConnectionResolution connectionResolution) {
            if(connectionResolution.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
                String username = pendingDevices.get(endpointID);
                connectedDevices.put(username, endpointID);
                pendingDevices.remove(username);
                testServer.echo("connected to " + username);
            }
            else {
                testServer.echo("connection rejected from other side, or other connection issue");
            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            connectedDevices.remove(s);
            testServer.echo("lost connection to " + s);
        }
    };



    private void startAdvertise() {

        AdvertisingOptions options = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(myID, SERVICE_ID, clc, options)
                .addOnSuccessListener( (Void unused) -> testServer.echo("start Advertise..."))
                .addOnFailureListener( (Exception e) -> Log.e("Advertise", "not able to Advertise"));
    }

    /**
     * starts Discovering
     * discovering stops automatically, after timer expired
     */
    private void startDiscover() {
        Log.d("nearby", "startDiscover()");

        testServer.echo("start Discover");

        if (discoveryTimeoutManager.isDiscovery()) {
            Log.d("nearby (startDiscover)", "is already discovering");
            return;
        }

        discoveryTimeoutManager.startTimer();

        EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String nearbyID, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                testServer.echo(discoveredEndpointInfo.getEndpointName() + " found");
                if (!connectedDevices.containsValue(nearbyID) || !pendingDevices.containsValue(nearbyID)) {
                    requestConnection( nearbyID, discoveredEndpointInfo);
                } else {
                    testServer.echo("already connected to" + discoveredEndpointInfo.getEndpointName());
                }

            }

            @Override
            public void onEndpointLost(@NonNull String endpointID) {
                Log.d("Discover", "The previously discovered endpoint (" + endpointID + ") has gone away");
                connectedDevices.remove(connectedDevices.get(endpointID));
                testServer.echo("lost connection to " + endpointID);
            }
        };

        DiscoveryOptions options = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
                .addOnSuccessListener( (Void unused ) -> Log.e("Discover", "start Discover..."))
                .addOnFailureListener( (Exception e) -> Log.e("Discover", "Unable to Discover"));
    }

    /**
     * Accepts Connection for given endpointID
     * @param endpointID ID of endpoint whose connection should be accepted
     */
    private void acceptConnection( String endpointID) {
        connectionsClient.acceptConnection(endpointID, new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                testServer.echo("Payload received");
                receiver.receive(payload.asBytes());
            }


            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // not used
                testServer.echo("Payload transferupdate");
            }
        })
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener( aVoid -> testServer.echo("could not accept connection to " + pendingDevices.get(endpointID))
                );
    }

    /**
     * Request connection for given endpointID
     * @param endpointID ID of the endpoint to which a connection should be requested
     * @param info Info to get username of endpoint
     */
    private void requestConnection(String endpointID, DiscoveredEndpointInfo info) {
        connectionsClient.requestConnection(myID, endpointID, clc)
                .addOnSuccessListener((Void unused) -> {})
                .addOnFailureListener((Exception e) -> testServer.echo("request Connection to " + info.getEndpointName() + " failed"));
    }

    @Override
    public void onDiscoveryTimerExpired() {
        connectionsClient.stopDiscovery();
        testServer.echo("stop Discover");
    }


    /**
     * connects to given user
     * @param userID user to connect to
     * @return nearbyID from connected user
     */
    CompletableFuture<String> connect(String userID) {

        // if already connected return nearbyID
        if(connectedDevices.containsKey(userID)) {
            return CompletableFuture.completedFuture(connectedDevices.get(userID));
        }

        // if not connected start Discover and return nearbyID when connected
        Log.wtf("test", "connect() to given device");
        startDiscover();
        return CompletableFuture.supplyAsync( () -> {
            while(true) {
                if (connectedDevices.containsKey(userID)) break;
            }
            return connectedDevices.get(userID);
        });

    }

    /**
     * connects to all devices in range and returns them after 20 seconds
     * @return Map of all connected devices
     */
    CompletableFuture<Map<String,String>> connectAll() {
        Log.d("nearby", "connectAll()");
        startDiscover();
        return CompletableFuture.supplyAsync( () -> {
            Log.d("test", "before thread");

            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("test", "connected devices: " + connectedDevices.toString());
            return connectedDevices;
        });
    }

}
