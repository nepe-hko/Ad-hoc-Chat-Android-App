package com.example.bachelorarbeit.test;

import android.util.Log;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class TestServer {

    private static String TEST_SERVER_IP = "80.139.92.13";
    private static int TEST_SERVER_PORT = 16443;
    private static Socket socket = null;
    private static PrintWriter out;
    private static String myID;

    private TestServer() {}

    public static void connect() {

        new Thread(() -> {
            try {
                socket = new Socket(TEST_SERVER_IP,TEST_SERVER_PORT);
                out = new PrintWriter(TestServer.socket.getOutputStream(), true);
                Log.d("test", "connected to Socket");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("test", "Socket connection failed");
            }
        }).start();
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
