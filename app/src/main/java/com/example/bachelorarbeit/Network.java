package com.example.bachelorarbeit;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.example.bachelorarbeit.test.TestServer;
import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.PayloadType;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class Network implements NearbyReceiver {

    private final TextView receivedView;
    private final String myID;
    private final DSRRouter router;
    private final ConnectionsClient connectionsClient;
    private final TestServer testServer;
    private final NearbyConnectionHandler nearby;

    public Network(Context context, String username, TestServer testServer, TextView receivedView) {
        this.receivedView = receivedView;
        this.myID = username;
        this.testServer = testServer;
        this.nearby = new NearbyConnectionHandler(context, myID, testServer, this);
        this.router = new DSRRouter(context, this.nearby, myID, testServer);
        this.connectionsClient = Nearby.getConnectionsClient(context);

    }

    public void sendText(String userID, String message) {
        Log.d("Network", "sendText()");

        testServer.echo("sendText("+ message + ") to " + userID);

        DATA dataPackage = new DATA(myID, userID, message);

        // Nachricht im Chatfenster als abgesendet darstellen

        // Route holen
        router.getRoute(userID)
                // Mit nächstem Hop verbinden
                .thenApplyAsync( route -> nearby.connect(route.getNextHop(myID))

                // Nachricht senden
                .thenAccept( nearbyID -> connectionsClient.sendPayload(nearbyID, dataPackage.serialize())));

        // ACK erhalten -> Nachricht im Chatfenster als zugestellt darstellen

        // Ack bis zu Timeout nicht erhalten -> Nachricht als nicht zustellbar markieren


    }
    public void receive(byte[] data) {

        Object payload = PayloadType.deserialize(data);
        assert payload != null;
        String payloadType = payload.getClass().getSimpleName();

        Log.d("test", "payload type: " + payloadType);
        // Für mich bestimmte Nachrichten werden gespeichert
        if (payloadType.equals("DATA")) {
            DATA receivedData = (DATA)payload;
            if (receivedData.getDestinationID().equals(myID)) {
                String text = receivedData.getMessage();
                this.receivedView.setText(receivedData.getSourceID() + " -> " + myID + ": " + text + "\n" + receivedView.getText());
                testServer.echo("received a Message " + text);
            }
        }

        // Alle Nachrichten an Router weiterleiten
        router.receive(payload);
    }

}
