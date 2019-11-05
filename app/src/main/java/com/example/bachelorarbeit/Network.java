package com.example.bachelorarbeit;

import android.content.Context;

import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.MACK;
import com.example.bachelorarbeit.types.PayloadType;
import com.example.bachelorarbeit.types.RERR;
import com.example.bachelorarbeit.types.RREP;
import com.example.bachelorarbeit.types.RREQ;
import com.example.bachelorarbeit.types.UACK;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import java.util.ArrayList;
import java.util.List;

public class Network implements NearbyReceiver {

    private final Context context;
    private final Cache cache = new Cache();
    private final String myID = "k45skf04u5s";

    private List<DATA> sendBuffer = new ArrayList<>();
    private List<String> seenPackages = new ArrayList<>(); //TODO: liste irgendwann wieder leeren
    private NearbyConnectionHandler nearby;

    public Network(Context context) {
        this.context = context;
        this.nearby = new NearbyConnectionHandler(context, myID, this);
    }


    public void sendText(String destinationID, String message) {

        DATA dataPackage = new DATA(myID, destinationID, message);

        // Route finden
        Route route = new Route();



        // Nachricht an nächsten in der Route senden
        nearby.get(route.getNextHop(myID))
                .thenAccept( id -> Nearby.getConnectionsClient(context).sendPayload(id, Payload.fromBytes(dataPackage.serialize())));

                //timeout starten und auf reply warten

        /*
        DATA dataPackage = new DATA(myID, destinationID, message);

        if (cache.hasRoute(destinationID)) {
            dataPackage.setRoute(cache.getRoute(destinationID));
            unicast(dataPackage);
        }
        else {
            RREQ rreq = new RREQ(myID, destinationID);
        }
        */

    }
    public void receive(byte[] data) {

        // Empfangene Daten werden deserialisiert und an den jeweiligen Handler weitergereicht
        Object payload = PayloadType.deserialize(data);
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
            unicast(rrep);
        }
        // Route nicht bekannt und RREQ noch nie behandelt
        else if (!seenPackages.contains(rreq.getUID())) {
            rreq.addEndpointToRoute(myID);
            broadcast(rreq);
        }

        seenPackages.add(rreq.getUID());
    }

    private void unicast(PayloadType payload) {

    }

    private void broadcast(PayloadType message) {

    }
}
