package com.example.bachelorarbeit;

import android.content.Context;

import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.PayloadType;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;

public class Network implements NearbyReceiver {

    private final Context context;
    private final String myID = "k45skf04u5s";
    private final DSRRouter router;
    private final ConnectionsClient connectionsClient;


    private NearbyConnectionHandler nearby;

    public Network(Context context) {
        this.context = context;
        this.nearby = new NearbyConnectionHandler(context, myID, this);
        this.router = new DSRRouter(context, this.nearby, myID);
        this.connectionsClient = Nearby.getConnectionsClient(context);
    }


    public void sendText(String destinationID, String message) {

        DATA dataPackage = new DATA(myID, destinationID, message);

        // Nachricht im Chatfenster als abgesendet darstellen

        // Route holen
        router.getRoute(destinationID)
                // Mit nächstem Hop verbinden
                .thenApplyAsync( route -> nearby.connect(route.getNextHop(myID))
                // Nachricht senden
                .thenAccept( id -> connectionsClient.sendPayload(id, dataPackage.serialize())));

        // ACK erhalten -> Nachricht im Chatfenster als zugestellt darstellen

        // Ack bis zu Timeout nicht erhalten -> Nachricht als nicht zustellbar markieren


    }
    public void receive(byte[] data) {

        Object payload = PayloadType.deserialize(data);
        assert payload != null;
        String payloadType = payload.getClass().toString();

        // Für mich bestimmte Nachrichten werden gespeichert
        if (payloadType.equals("DATA")) {
            DATA receivedData = (DATA)payload;
            if (receivedData.getDestinationID().equals(myID)) {
                Message message = new Message(receivedData);
            }
        }

        // Alle Nachrichten an Router weiterleiten
        router.receive(payload);
    }

}
