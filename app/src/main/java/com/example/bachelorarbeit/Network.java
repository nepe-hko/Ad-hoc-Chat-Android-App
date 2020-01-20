package com.example.bachelorarbeit;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.bachelorarbeit.test.TestServer;
import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.PayloadType;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;

public class Network implements NearbyReceiver {

    private final Context context;
    private final String myID;
    private final DSRRouter router;
    private final ConnectionsClient connectionsClient;
    private final TestServer testServer;

    private NearbyConnectionHandler nearby;

    public Network(Context context, String username, TestServer testServer) {
        this.context = context;
        this.myID = username;
        this.testServer = testServer;
        this.nearby = new NearbyConnectionHandler(context, myID, testServer, this);
        this.router = new DSRRouter(context, this.nearby, myID, testServer);
        this.connectionsClient = Nearby.getConnectionsClient(context);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendText(String destinationID, String message) {

        testServer.echo(myID + ": sendText("+ message + ") to " + destinationID);

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
