package com.example.bachelorarbeit;

import android.content.Context;

import com.example.bachelorarbeit.test.TestServer;
import com.example.bachelorarbeit.types.ACK;
import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.RERR;
import com.example.bachelorarbeit.types.RREP;
import com.example.bachelorarbeit.types.RREQ;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

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
    private final Network network;
    private final Map<Long, DATA> outgoingPayloads;
    private final Map<String, DATA>  awaitingACKs;

    DSRRouter(Context context, NearbyConnectionHandler nearby, String myID, Network network) {
        this.cache = new Cache();
        this.connectionsClient = Nearby.getConnectionsClient(context);
        this.nearby = nearby;
        this.myID = myID;
        this.network = network;
        this.outgoingPayloads = new HashMap<>();
        this.awaitingACKs = new HashMap<>();
    }

    public CompletableFuture<Route> getRoute(String userID) {

        return CompletableFuture.supplyAsync( () -> {

            if (cache.hasRoute(userID)){
                return cache.getRoute(userID);
            }

            RREQ rreq = new RREQ(this.myID, userID);
            seenRREQs.add(rreq.getUID());
            broadcastRREQ(rreq);

            do {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!cache.hasRoute(userID));
            return cache.getRoute(userID);
        }).orTimeout(40,TimeUnit.SECONDS);
    }

    public void onDeviceConnected(String userID) {
        cache.setRoute(userID, new Route(userID));
    }

    public void receive(Object payload) {

        new Thread( () -> {
            String payloadType = payload.getClass().getSimpleName();
            switch (payloadType) {
                case "RREQ" : handleRREQ((RREQ)payload);
                    break;
                case "RREP" : handleRREP((RREP)payload);
                    break;
                case "RERR" : handleRERR((RERR)payload);
                    break;
                case "DATA" : handleDATA((DATA)payload);
                    break;
                case "ACK" : handleACK((ACK)payload);
                    break;
            }
        }).start();
    }

    private void handleACK(ACK ack) {

        if(ack.getOriginalSourceID().equals(myID)) {
            awaitingACKs.remove(ack.getOriginalUID());
            return;
        }

        // forward ack to next hop
        sendACK(ack);
        TestServer.echo("received ACK from " + ack.getOriginalSourceID() + " for Data Package with UID " + ack.getOriginalUID());
    }

    private void handleDATA(DATA data) {
        //TODO: Save Route/Routes
        TestServer.receivedDATA(data);

        // If this node ist receiver of DATA package -> send ACK to Source
        if (data.getDestinationID().equals(myID)) {
            if (data.getDestinationID().equals(myID)) {
                TestServer.echo("send ACK");
                ACK ack = new ACK(data);
                sendACK(ack);
            }
            return;
        }

        // forward message to next hop
        sendDATA(data);

    }

    private void handleRERR(RERR rerr) {
        String sender = rerr.getRoute().getHopBefore(myID);
        if (sender == null)
            sender = rerr.getSourceID();
        TestServer.echo("received RERR with UID " + rerr.getUID() + " from" + sender + " (Original Sender: " + rerr.getSourceID() + ", Destination: " + rerr.getDestinationID() + ")");



        if(rerr.getDestinationID().equals(myID)) {
            TestServer.echo("RERR für mich erhalten");
            return;
        }

        // forward rerr to next hop
        sendRERR(rerr);

    }

    private void handleRREP(RREP rrep) {

        //TODO: Save other Routes
        TestServer.receivedRREP(rrep);

        if (rrep.getDestinationID().equals(myID)) {
            //TODO: dont save route, if another rrep with same uid was faster
            //      or better: save this route as alternative route
            String userID = rrep.getFirstHop();
            rrep.reverseRoute();
            rrep.removeFromRoute(myID);
            cache.setRoute(userID, rrep.getRoute());
            return;
        }

        // forward message to next hop
        sendRREP(rrep);

    }

    private void handleRREQ(RREQ rreq) {

        if(rreq.getDestinationID().equals(myID)) {
            TestServer.echo("SOMETHING WRONG: GOT RREQ FOR MYSELF");
            return;
        }

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
        broadcastRREQ(rreq);

    }

    /**
     * Is called on PayloadTransferUpdate
     * @param nearbyID NearbyID of other Node
     * @param payloadTransferUpdate Info about the TransferUpdate
     */
    public void onPayloadTransferupdate(String nearbyID, PayloadTransferUpdate payloadTransferUpdate) {
        Long payloadID = payloadTransferUpdate.getPayloadId();
        int status = payloadTransferUpdate.getStatus();
        if (status == PayloadTransferUpdate.Status.IN_PROGRESS) return;
        DATA data = outgoingPayloads.get(payloadID);
        if (data == null) return;

        switch (status)
        {

            case PayloadTransferUpdate.Status.SUCCESS :
                awaitingACKs.put(data.getUID(),data);
                outgoingPayloads.remove(payloadID);
                break;


            case PayloadTransferUpdate.Status.FAILURE :

                String failedHopUserID = data.getRoute().getNextHop(myID);
                cache.deleteRoutesContainingHop(failedHopUserID);

                try {
                    // If this node was sender of DATA package -> Delete Routes containting failed hop and send again
                    if (data.getSourceID().equals(myID)) {
                        TestServer.echo("try again to send Message");
                        network.sendText(data.getDestinationID(), data.getMessage());
                    }

                } catch (Exception e) {
                    TestServer.echo("Failed do serialize DATA in onPayloadTransferUpdate");
                }

                break;

        }


    }

    /**
     * Notifys about the waiting ACK
     * @param ackUID ACK to wait for
     * @return SUCCESS or FAILURE, depending on whether ACK is available or not
     */
    public CompletableFuture<String> notifyStatus(String ackUID) {

        return CompletableFuture.supplyAsync( () -> {
            do {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!isACKAvailable(ackUID));
            return "SUCCESS";
        })
                .completeOnTimeout("FAILURE", 30,TimeUnit.SECONDS);

    }

    /**
     * checks if ACK for given AckUID is available
     * @param ackUID ACK to be checked
     * @return true or false, depending on whether ACK is available or not
     */
    private boolean isACKAvailable(String ackUID) {
        return !awaitingACKs.containsKey(ackUID);
    }

    /**
     * sends DATA along the Route
     * @param data DATA which should be send
     * @return UID of DATA Package
     */
    public CompletableFuture<String> sendDATA(DATA data) {

        Payload payload = data.serialize();
        outgoingPayloads.put(payload.getId(), data);

        nearby.connect(data.getRoute().getNextHop(myID))
                .handle( (nearbyID, exception) -> {
                    if (exception != null) {
                        TestServer.echo( "Could not connect to next hop");
                        return null;
                    }
                    return nearbyID;
                })
                .thenAccept( nearbyID -> {
                    // verbindung zum nächsten hop fehlgeschlagen
                    if (nearbyID == null) {
                        cache.deleteRoutesContainingHop(data.getRoute().getNextHop(myID));
                        TestServer.echo("send RERR");
                        RERR rerr = new RERR(data, myID);
                        sendRERR(rerr);

                    // erfolgreich zum nächsten hop verbunden
                    } else {
                        TestServer.sendDATA(data);
                        connectionsClient.sendPayload( nearbyID, payload);
                    }
                });
        return CompletableFuture.completedFuture(data.getUID());
    }

    /**
     * sends RERR along the Route
     * @param rerr RERR which should be send
     */
    private void sendRERR(RERR rerr) {
        nearby.connect(rerr.getRoute().getNextHop(myID))
                .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, rerr.serialize()));
    }

    /**
     * sends ACK along the Route
     * @param ack ACK which should be send
     */
    private void sendACK(ACK ack) {
        nearby.connect(ack.getRoute().getNextHop(myID))
                .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, ack.serialize()));
    }

    /**
     *  sends RREP along the Route
     * @param rrep RREP which sould be send
     */
    private void sendRREP(RREP rrep) {
        nearby.connect(rrep.getRoute().getNextHop(myID))
                .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, rrep.serialize()));
    }

    /**
     * broadcasts given RREQ to all devices in Range
     * @param rreq RREQ which should be broadcasted
     */
    private void broadcastRREQ(RREQ rreq) {

        if(rreq.getDestinationID().equals(myID)) {
            TestServer.echo("SOMETHING WRONG: GOT RREQ FOR MYSELF");
            return;
        }

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
                if (cache.hasRoute(rreq.getDestinationID()) && !rreq.getDestinationID().equals(myID)) {

                    try {
                        rreq.addRouteToRoute(cache.getRoute(rreq.getDestinationID()));
                        RREP rrep = new RREP(rreq);

                        nearby.connect(rrep.getRoute().getNextHop(myID))
                                .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, rrep.serialize()));
                        return;
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        return;
                    }

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

            } while ( !cache.hasRoute(rreq.getDestinationID()) );
        }).start();

    }

}
