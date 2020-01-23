package com.example.bachelorarbeit.test;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class TestServer {

    private static Socket socket = null;
    private static PrintWriter out;
    private static String myID;

    private TestServer() {}

    public static void connect(String ip, int port) {

        new Thread(() -> {
            try {
                socket = new Socket(ip,port);
                out = new PrintWriter(TestServer.socket.getOutputStream(), true);
                Log.d("test", "connected to Socket");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("test", "Socket connection failed");
            }
        });
    }

    public static void setMyID(String myID) {
        TestServer.myID = myID;
    }

    public static void disconnect() {

        if (socket == null) return;

        try {
            TestServer.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void echo(String message) {
        if (socket == null) return;
        new Thread( () -> TestServer.out.println( myID + ": " + message)).start();
    }


    public static void rreq( List<String> devices) {
        if (socket == null) return;
        String devicesString = android.text.TextUtils.join(",", devices);
        new Thread( () -> TestServer.out.println(myID + ": send RREQ to " + devicesString)).start();
    }
}
