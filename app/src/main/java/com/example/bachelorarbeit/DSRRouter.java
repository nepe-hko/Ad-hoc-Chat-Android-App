package com.example.bachelorarbeit;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.bachelorarbeit.test.TestServer;
import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.MACK;
import com.example.bachelorarbeit.types.RERR;
import com.example.bachelorarbeit.types.RREP;
import com.example.bachelorarbeit.types.RREQ;
import com.example.bachelorarbeit.types.UACK;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class DSRRouter {
    private final Cache cache;
    private final List<String> seenPackages = new ArrayList<>(); //TODO: liste irgendwann wieder leeren
    private final ConnectionsClient connectionsClient;
    private final NearbyConnectionHandler nearby;
    private final String myID;
    private final TestServer testServer;

    DSRRouter(Context context, NearbyConnectionHandler nearby, String myID, TestServer testServer) {
        this.testServer = testServer;
        this.cache = new Cache();
        this.connectionsClient = Nearby.getConnectionsClient(context);
        this.nearby = nearby;
        this.myID = myID;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    CompletableFuture<Route> getRoute(String endpointID) {
        testServer.echo(myID + ": getRoute() to " + endpointID);
        return CompletableFuture.supplyAsync( () -> {
            if (cache.hasRoute(endpointID)){
                testServer.echo(myID + ": Route to " + endpointID + " in Cache");
                return cache.getRoute(endpointID);
            }
            testServer.echo(myID + ": Route to " + endpointID + " not in Cache");
            routeDiscovery(endpointID).thenApply( route -> cache.getRoute(endpointID));
            return null; // falsch, nur zu testzweck eingefügt
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private CompletableFuture<Route> routeDiscovery(String endpointID) {
        testServer.echo(myID + ": routeDiscovery to find Route to " + endpointID);
        // Send RREQ to all Devices in Range
        nearby.connectAll()
                .thenAccept(  connectedDevices -> {
                    Log.d("test", "in thenAccept");
                    RREQ rreq = new RREQ(this.myID, endpointID);

                    List<String> receivers = new ArrayList<>(connectedDevices.values());
                    testServer.rreq(myID, receivers);
                    receivers.forEach(element -> Log.d("test", element));
                    connectionsClient.sendPayload(receivers , rreq.serialize());

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
        testServer.echo(myID + ": received MACK from " + mack.getOriginalSourceID() + " for Data Package with UID " + mack.getOriginalUID());
    }

    private void handleUACK(UACK uack) {
        testServer.echo(myID + ": received UACK from " + uack.getSourceID() + " for Data Package with UID " + uack.getOriginalUID() + " (Original Sender: " + uack.getOriginalSourceID());
    }

    private void handleDATA(DATA data) {
        String sender = data.getRoute().getHopBefore(myID);
        if (sender == null)
            sender = data.getSourceID();
        testServer.echo(myID + ": received DATA with UID " + data.getUID() + " from " + sender + " (Original Sender: " + data.getSourceID() + ", Destination: " + data.getDestinationID() + ")");
    }

    private void handleRERR(RERR rerr) {
        String sender = rerr.getRoute().getHopBefore(myID);
        if (sender == null)
            sender = rerr.getSourceID();
        testServer.echo(myID + ": received RERR with UID " + rerr.getUID() + " from" + sender + " (Original Sender: " + rerr.getSourceID() + ", Destination: " + rerr.getDestinationID() + ")");
    }

    private void handleRREP(RREP rrep) {
        String sender = rrep.getRoute().getHopBefore(myID);
        testServer.echo(myID + ": received RREP with UID " + rrep.getUID() + " from" + sender + " (Destination: " + rrep.getDestinationID() + ")");

    }

    private void handleRREQ(RREQ rreq) {

        testServer.echo(myID + ": received RREQ with UID " + rreq.getUID());

        // Route bekannt
        if (cache.hasRoute(rreq.getDestinationID())) {
            RREP rrep = new RREP(rreq, cache.getRoute(rreq.getDestinationID()));
            //todo: unicast rrep
        }
        // Route nicht bekannt und RREQ noch nie behandelt
        else if (!seenPackages.contains(rreq.getUID())) {
            rreq.addEndpointToRoute(myID);
            //todo: broadcast rreq
        }

        seenPackages.add(rreq.getUID());


    }

}
