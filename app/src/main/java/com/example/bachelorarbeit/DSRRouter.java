package com.example.bachelorarbeit;

import android.content.Context;

import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.MACK;
import com.example.bachelorarbeit.types.RERR;
import com.example.bachelorarbeit.types.RREP;
import com.example.bachelorarbeit.types.RREQ;
import com.example.bachelorarbeit.types.UACK;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class DSRRouter {
    private final Cache cache;
    private final List<DATA> sendBuffer = new ArrayList<>();
    private final List<String> seenPackages = new ArrayList<>(); //TODO: liste irgendwann wieder leeren
    private final ConnectionsClient connectionsClient;
    private final NearbyConnectionHandler nearby;
    private final String myID;

    DSRRouter(Context context, NearbyConnectionHandler nearby, String myID) {
        this.cache = new Cache();
        this.connectionsClient = Nearby.getConnectionsClient(context);
        this.nearby = nearby;
        this.myID = myID;
    }

    CompletableFuture<Route> getRoute(String endpointID) {
        return CompletableFuture.supplyAsync( () -> {
            if (cache.hasRoute(endpointID)){
                return cache.getRoute(endpointID);
            }
            routeDiscovery(endpointID).thenApply( route -> cache.getRoute(endpointID));
            return null; // falsch, nur zu testzweck eingefügt
        });
    }

    private void routeMaintenance() {

    }

    private CompletableFuture<Route> routeDiscovery(String endpointID) {

        // Send RREQ to all Devices in Range
        nearby.connectAll()
                .thenAccept(  connectedDevices -> {
                    RREQ rreq = new RREQ(this.myID, endpointID);
                    connectionsClient.sendPayload(connectedDevices, rreq.serialize());
                });
        // Warten bis Route im Cache ist
        while(true){
            if (cache.hasRoute(endpointID)) break;

        }
        return CompletableFuture.completedFuture(cache.getRoute(endpointID));

        //TODO: wenn nach bestimmter zeit Route nicht verfügbar ist, return null;

    }

    void receive(Object payload) {

        String payloadType = payload.getClass().toString();
        switch (payloadType) {
            case "RREQ" :
                handleRREQ((RREQ)payload);
                break;

            case "RREP" :
                handleRREP((RREP)payload);
                break;

            case "RERR" :
                handleRERR((RERR)payload);
                break;

            case "DATA" :
                handleDATA((DATA)payload);
                break;

            case "UACK" :
                handleUACK((UACK)payload);
                break;

            case "MACK" :
                handleMACK((MACK)payload);
                break;
        }
    }

    private void handleMACK(MACK mack) {
    }

    private void handleUACK(UACK uack) {

    }

    private void handleDATA(DATA data) {

    }

    private void handleRERR(RERR rerr) {

    }

    private void handleRREP(RREP rrep) {


    }

    private void handleRREQ(RREQ rreq) {

        // Route bekannt
        if (cache.hasRoute(rreq.getDestinationID())) {
            RREP rrep = new RREP(rreq, cache.getRoute(rreq.getDestinationID()));
            //todo: unicast rrep
        }
        // Route nicht bekannt und RREQ noch nie behandelt
        else if (!seenPackages.contains(rreq.getUID())) {
            rreq.addEndpointToRoute(myID);
            //todo: unicast rreq
        }

        seenPackages.add(rreq.getUID());


    }

}
