package com.example.bachelorarbeit;

import android.content.Context;
import android.widget.TextView;

import com.example.bachelorarbeit.test.TestServer;
import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.PayloadType;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public class Network implements NearbyReceiver {

    private final TextView receivedView;
    private final String myID;
    private final DSRRouter router;

    public Network(Context context, String username, TextView receivedView) {
        this.receivedView = receivedView;
        this.myID = username;
        NearbyConnectionHandler nearby = new NearbyConnectionHandler(context, myID, this);
        this.router = new DSRRouter(context, nearby, myID, this);

    }

    /**
     * sends a message to another device
     * @param userID username of receiver
     * @param message message to be transmitted
     */
    public void sendText(String userID, String message) {

        DATA dataPackage = new DATA(myID, userID, message);

        // get Route -> connect to next hop in route -> send Message to next hop
        router.getRoute(userID)
                .thenAccept(dataPackage::setRoute)
                .thenCompose(nothing -> router.sendDATA(dataPackage))
                .thenCompose(router::notifyStatus)
                .thenAccept( status -> {
                    TestServer.echo("ACK result: " + status);
                    receivedView.setText( message + " (" + status + ")" +  "\n" + receivedView.getText());
                });
    }


    /**
     * this function is called for incoming receivedDATA from other devices
     * @param data receivedDATA received from other device
     */
    @Override
    public void onReceive(byte[] data) {

        // unpack payload
        Object payload = PayloadType.deserialize(data);
        assert payload != null;

        // forward all messages to router
        router.receive(payload);

        // messages intended for this device are displayed
        String payloadType = payload.getClass().getSimpleName();
        if (payloadType.equals("DATA")) {
            DATA receivedData = (DATA)payload;
            if (receivedData.getDestinationID().equals(myID)) {
                String text = receivedData.getMessage();
                this.receivedView.setText(receivedData.getSourceID() + " -> " + myID + ": " + text + "\n" + receivedView.getText());
                TestServer.echo("received a Message " + text);
            }
        }
    }

    @Override
    public void onPayloadTransferUpdate(String nearbyID, PayloadTransferUpdate payloadTransferUpdate) {
        router.onPayloadTransferupdate(nearbyID, payloadTransferUpdate);
    }

    /**
     * this function is called if a connection to another device is established
     * @param userID userID from connected device
     */
    @Override
    public void onDeviceConnected(String userID) {
        router.onDeviceConnected(userID);
    }
}
