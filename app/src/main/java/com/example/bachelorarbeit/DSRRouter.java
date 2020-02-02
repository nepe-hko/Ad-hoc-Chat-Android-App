package com.example.bachelorarbeit;

import android.content.Context;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import java9.util.concurrent.CompletableFuture;


class DSRRouter {

    private final Cache cache;
    private final List<String> seenRREQs = new ArrayList<>(); //TODO: liste irgendwann wieder leeren
    private final ConnectionsClient connectionsClient;
    private final NearbyConnectionHandler nearby;
    private final String myID;

    DSRRouter(Context context, NearbyConnectionHandler nearby, String myID) {
        this.cache = new Cache();
        this.connectionsClient = Nearby.getConnectionsClient(context);
        this.nearby = nearby;
        this.myID = myID;
    }

    public CompletableFuture<Route> getRoute(String userID) {


        return CompletableFuture.supplyAsync( () -> {

            if (cache.hasRoute(userID)){
                return cache.getRoute(userID);
            }

            RREQ rreq = new RREQ(this.myID, userID);
            seenRREQs.add(rreq.getUID());
            sendRREQ(rreq);
            TestServer.echo("after sendRREQ()");
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (cache.hasRoute(userID)) break;
            }
            return cache.getRoute(userID);
        });
    }

    private void sendRREQ(RREQ rreq) {

        new Thread( () -> {
            nearby.connectAll();
            Map<String, String> alreadySend = new HashMap<>();
            Map<String, String> connectedDev;
            List<String> sendThisLoop = new ArrayList<>();

            do {

                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // if connected to searched device -> send RREP back and return
                if (cache.hasRoute(rreq.getDestinationID()) && !rreq.getSourceID().equals(myID)) {
                    rreq.addRouteToRoute(cache.getRoute(rreq.getDestinationID()));
                    RREP rrep = new RREP(rreq);
                    // TODO: fehler: wenn ich selbst abender einer nachricht bin, darf ich kein RREP schicken (drei zeilen oben schon verbessert)
                    nearby.connect(rrep.getRoute().getNextHop(myID))
                            .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, rrep.serialize()));
                    return;
                }

                // not connected to searched device -> forward rreq

                connectedDev = nearby.getConnectedDevices();
                sendThisLoop.clear();

                // remove devices to which RREQ has already been sent
                connectedDev = connectedDev.entrySet()
                        .stream()
                        .filter( entry -> !alreadySend.containsKey(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                sendThisLoop.addAll(connectedDev.values());
                sendThisLoop.remove(connectedDev.get(rreq.getSourceID()));

                connectionsClient.sendPayload(sendThisLoop , rreq.serialize());
                alreadySend.putAll(connectedDev);
                TestServer.sendRREQ(new ArrayList<>(connectedDev.keySet()), rreq.getDestinationID());

            } while ( !cache.hasRoute(rreq.getDestinationID()));
        }).start();

    }

    void deviceConnected(String userID) {
        cache.setRoute(userID, new Route(userID));
    }

    void receive(Object payload) {

        new Thread( () -> {
        String payloadType = payload.getClass().getSimpleName();
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
        }).start();
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

        TestServer.receivedRREP(rrep);

        if (rrep.getDestinationID().equals(myID)) {
            String userID = rrep.getFirstHop();
            rrep.reverseRoute();
            rrep.removeFromRoute(myID);
            cache.setRoute(userID, rrep.getRoute());
            return;
        }

        // forward message to next hop
        nearby.connect(rrep.getRoute().getNextHop(myID))
                .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, rrep.serialize()));

    }

    private void handleRREQ(RREQ rreq) {

        TestServer.receivedRREQ(rreq);

        if(seenRREQs.contains(rreq.getUID())) return;
        seenRREQs.add(rreq.getUID());
        rreq.addEndpointToRoute(myID);

        // route known -> send RREP
        String destinationID = rreq.getDestinationID();
        if (cache.hasRoute(destinationID)) {
            rreq.addRouteToRoute(cache.getRoute(destinationID));
            RREP rrep = new RREP(rreq);
            nearby.connect(rrep.getRoute().getNextHop(myID))
                    .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, rrep.serialize()));
            return;
        }

        // route not known -> broadcast RREQ
        sendRREQ(rreq);

    }
}
