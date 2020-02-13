package com.example.bachelorarbeit;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public interface NearbyReceiver {
    void receive(byte[] data);
    void onPayloadTransferUpdate(String nearbyID, PayloadTransferUpdate payloadTransferUpdate);

    void onDeviceConnected(String userID);
}
