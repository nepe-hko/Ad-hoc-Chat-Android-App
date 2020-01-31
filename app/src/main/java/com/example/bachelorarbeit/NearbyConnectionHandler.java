package com.example.bachelorarbeit;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.bachelorarbeit.test.TestServer;
import com.google.android.gms.common.util.MapUtils;
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
import java.util.List;
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
    private final HkoTimer discoveryHkoTimer;
    private final ConnectionsClient connectionsClient;


    NearbyConnectionHandler(Context context, String myID, NearbyReceiver receiver) {

        this.context = context;
        this.myID = myID;
        this.receiver = receiver;
        this.connectedDevices = new HashMap<>();
        this.pendingDevices = new HashMap<>();
        this.discoveryHkoTimer = new HkoTimer(this);
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

            String username = pendingDevices.get(endpointID);

            if(connectionResolution.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
                receiver.onDeviceConnected(username);
                connectedDevices.put(username, endpointID);
                TestServer.echo("connected to " + username);
            }
            else {
                TestServer.echo("connection rejected from other side, or other connection issue");
            }

            pendingDevices.remove(username);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onDisconnected(@NonNull String nearbyID) {
            String userID = getUserIDbyNearbyID(nearbyID);
            TestServer.echo("disconnected from " + userID);
            connectedDevices.remove(userID);
        }
    };



    private void startAdvertise() {

        AdvertisingOptions options = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(myID, SERVICE_ID, clc, options)
                .addOnSuccessListener( (Void unused) -> TestServer.echo("start Advertise..."))
                .addOnFailureListener( (Exception e) -> TestServer.echo("Advertise failed"));
    }

    /**
     * starts Discovering
     * discovering stops automatically, after timer expired
     */
    private void startDiscover() {

        TestServer.echo("start Discover");

        if (discoveryHkoTimer.isDiscovery()) {
            return;
        }

        discoveryHkoTimer.start();

        EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {

            @Override
            public void onEndpointFound(@NonNull String nearbyID, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {

                // only for testing
                if (myID.equals("MARCO") && discoveredEndpointInfo.getEndpointName().equals("DANIEL")) {
                    return;
                }
                if (myID.equals("MARCO") && discoveredEndpointInfo.getEndpointName().equals("ASTI")) {
                    return;
                }
                if (myID.equals("PATRICK") && discoveredEndpointInfo.getEndpointName().equals("ASTI")) {
                    return;
                }
                if (myID.equals("DANIEL") && discoveredEndpointInfo.getEndpointName().equals("MARCO")) {
                    return;
                }
                if (myID.equals("ASTI") && discoveredEndpointInfo.getEndpointName().equals("PATRICK")) {
                    return;
                }
                if (myID.equals("ASTI") && discoveredEndpointInfo.getEndpointName().equals("MARCO")) {
                    return;
                }
                // end testing code

                if (connectedDevices.containsValue(nearbyID)){
                    return;
                }
                if (pendingDevices.containsValue(nearbyID)) {
                    return;
                }

                requestConnection(nearbyID, discoveredEndpointInfo);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEndpointLost(@NonNull String nearbyID) {

                String userID = getUserIDbyNearbyID(nearbyID);
                TestServer.echo("lost connection to " + userID);
                connectedDevices.remove(userID);
            }
        };

        DiscoveryOptions options = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options);
    }

    /**
     * Accepts Connection for given endpointID
     * @param endpointID ID of endpoint whose connection should be accepted
     */
    private void acceptConnection( String endpointID) {
        connectionsClient.acceptConnection(endpointID, new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                receiver.receive(payload.asBytes());
            }


            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // not used
            }
        })
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener( aVoid -> TestServer.echo("could not accept connection to " + pendingDevices.get(endpointID))
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
                .addOnFailureListener((Exception e) -> TestServer.echo("request Connection to " + info.getEndpointName() + " failed"));
    }

    @Override
    public void onDiscoveryTimerExpired() {
        connectionsClient.stopDiscovery();
        TestServer.echo("stop Discover");
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
        startDiscover();
        return CompletableFuture.supplyAsync( () -> {
            while(true) {
                if (connectedDevices.containsKey(userID)) break;
            }
            return connectedDevices.get(userID);
        });

    }

    /**
     * connects to all devices in range and returns them after 15 seconds
     * @return Map of all connected devices
     */
    CompletableFuture<Map<String,String>> connectAll() {

        startDiscover();
        return CompletableFuture.supplyAsync( () -> {

            try {
                TimeUnit.SECONDS.sleep(12);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return connectedDevices;
        });
    }

    Map<String,String> getConnectedDevices() {
        return this.connectedDevices;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getUserIDbyNearbyID(String nearbyID) {
        return connectedDevices.entrySet()
                .stream()
                .filter(entry -> nearbyID.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().get();
    }
}
