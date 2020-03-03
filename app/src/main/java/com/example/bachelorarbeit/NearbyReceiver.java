package com.example.bachelorarbeit;

import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public interface NearbyReceiver {
    void onReceive(byte[] data);
    void onPayloadTransferUpdate(String nearbyID, PayloadTransferUpdate payloadTransferUpdate);
    void onDeviceConnected(String userID);

}
