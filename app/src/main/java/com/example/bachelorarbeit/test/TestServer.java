package com.example.bachelorarbeit.test;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TestServer {

    private Socket socket = null;
    private PrintWriter out;

    public void connect(String ip, int port) {
        try {
            this.socket = new Socket(ip,port);
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            Log.d("test", "connected to Socket");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("test", "connection to Socket failed");
        }
    }

    public void disconnect() {

        if (socket == null) return;

        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void echo(String message) {
        if (socket == null) return;
        new Thread( () -> this.out.println(message)).start();
    }


    public void rreq(String initiator, List<String> devices) {
        if (socket == null) return;
        String devicesString = android.text.TextUtils.join(",", devices);
        new Thread( () -> this.out.println(initiator + ": send RREQ to " + devicesString)).start();
    }
}
