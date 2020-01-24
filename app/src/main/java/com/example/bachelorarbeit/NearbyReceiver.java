package com.example.bachelorarbeit;

public interface NearbyReceiver {
    void receive(byte[] data);

    void onDeviceConnected(String userID);
}
