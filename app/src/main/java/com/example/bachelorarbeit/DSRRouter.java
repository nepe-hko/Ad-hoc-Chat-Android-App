package com.example.bachelorarbeit;

import android.content.Context;
import android.util.Log;
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
import java.util.List;
import java9.util.concurrent.CompletableFuture;


class DSRRouter {

    private final Cache cache;
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

    CompletableFuture<Route> getRoute(String userID) {
        Log.d("DSRRouter", "getRoute(" + userID + ")");
        TestServer.echo("getRoute() to " + userID);
        return CompletableFuture.supplyAsync( () -> {
            Log.d("test", "supplyAsync");
            if (cache.hasRoute(userID)){
                TestServer.echo("Route to " + userID + " in Cache");
                return cache.getRoute(userID);
            }
            TestServer.echo("Route to " + userID + " not in Cache");
            routeDiscovery(userID).thenApply( route -> cache.getRoute(userID));

            while (true) {
                if (cache.hasRoute(userID)) break;
            }
            return cache.getRoute(userID);
        });
    }



    private CompletableFuture<Route> routeDiscovery(String userID) {
        Log.d("DSRRouter", "RouteDiscovery(" + userID + ")");
        TestServer.echo("routeDiscovery to find Route to " + userID);
        // Send RREQ to all Devices in Range
        nearby.connectAll()
                .thenAccept(  connectedDevices -> {

                    // if already connected to the searched device insert Route
                    if(connectedDevices.containsKey(userID)) {
                        TestServer.echo("insert Route to " + userID);
                        Log.d("test", "insert Route");
                        cache.setRoute(userID, new Route(userID));
                    }
                    // if not connected to the searched device make a RREQ to all connected devices
                    else {
                        TestServer.sendRREQ(new ArrayList<>(connectedDevices.keySet()), userID);

                        RREQ rreq = new RREQ(this.myID, userID);
                        List<String> receiverKeys = new ArrayList<>(connectedDevices.values());
                        connectionsClient.sendPayload(receiverKeys , rreq.serialize());
                    }


                });
        // Warten bis Route im Cache ist
        while(true){
            if (cache.hasRoute(userID)) break;

        }
        Log.d("test", "return route from route Discovery");
        return CompletableFuture.completedFuture(cache.getRoute(userID));


    }

    public void deviceConnected(String userID) {
        cache.setRoute(userID, new Route(userID));
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
        TestServer.echo("received MACK from " + mack.getOriginalSourceID() + " for Data Package with UID " + mack.getOriginalUID());
    }

    private void handleUACK(UACK uack) {
        TestServer.echo("received UACK from " + uack.getSourceID() + " for Data Package with UID " + uack.getOriginalUID() + " (Original Sender: " + uack.getOriginalSourceID());
    }

    private void handleDATA(DATA data) {

        TestServer.receivedDATA(data);
        if (data.getDestinationID().equals(myID)) return;

        // forward message to next hop
        nearby.connect(data.getRoute().getNextHop(myID))
                .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, data.serialize()));

    }

    private void handleRERR(RERR rerr) {
        String sender = rerr.getRoute().getHopBefore(myID);
        if (sender == null)
            sender = rerr.getSourceID();
        TestServer.echo("received RERR with UID " + rerr.getUID() + " from" + sender + " (Original Sender: " + rerr.getSourceID() + ", Destination: " + rerr.getDestinationID() + ")");
    }

    private void handleRREP(RREP rrep) {
        String sender = rrep.getRoute().getHopBefore(myID);
        TestServer.echo("received RREP with UID " + rrep.getUID() + " from" + sender + " (Destination: " + rrep.getDestinationID() + ")");

    }

    private void handleRREQ(RREQ rreq) {

        TestServer.receivedRREQ(rreq);

        if(seenPackages.contains(rreq.getUID())) return;

        // route known -> send RREP back
        if (cache.hasRoute(rreq.getDestinationID())) {
            RREP rrep = new RREP(rreq, cache.getRoute(rreq.getDestinationID()));
            nearby.connect(rrep.getRoute().getNextHop(myID))
                    .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, rrep.serialize()));
        }

        // route not known -> broadcast RREQ
        else {
            rreq.addEndpointToRoute(myID);
            nearby.connectAll()
                    .thenAccept(  connectedDevices -> connectionsClient.sendPayload(new ArrayList<>(connectedDevices.keySet()), rreq.serialize()));
        }

        seenPackages.add(rreq.getUID());
    }
}
