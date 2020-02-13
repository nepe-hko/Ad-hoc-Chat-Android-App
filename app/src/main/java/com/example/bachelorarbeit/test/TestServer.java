package com.example.bachelorarbeit.test;

import android.util.Log;

import com.example.bachelorarbeit.types.DATA;
import com.example.bachelorarbeit.types.RREP;
import com.example.bachelorarbeit.types.RREQ;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class TestServer {

    private static String TEST_SERVER_IP = "80.139.87.232";
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
            } catch (IOException e) {
                e.printStackTrace();
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


    public static void sendRREQ( List<String> devices, String searchedUserID) {
        if (socket == null) return;
        if(devices.isEmpty()) return;

        String devicesString = android.text.TextUtils.join(",", devices);
        new Thread( () -> TestServer.out.println(myID + ": send RREQ to " + devicesString + " to get Route to " + searchedUserID)).start();
    }

    public static void sendRREQ( String device, String searchedUserID) {
        if (socket == null) return;

        new Thread( () -> TestServer.out.println(myID + ": send RREQ to " + device + " to get Route to " + searchedUserID)).start();
    }

    public static void receivedDATA(DATA data) {
        String sender = data.getRoute().getHopBefore(myID);
        if (sender == null)
            sender = data.getSourceID();
        TestServer.echo("received DATA with UID " + data.getUID() + " from " + sender + " (Original Sender: " + data.getSourceID() + ", Destination: " + data.getDestinationID() + ")");
    }

    public static void receivedRREQ(RREQ rreq) {
        TestServer.echo("received RREQ with UID " + rreq.getUID());
    }

    public static void receivedRREP(RREP rrep) {
        String sender = rrep.getRoute().getHopBefore(myID);
        TestServer.echo("received RREP with UID " + rrep.getUID() + " from" + sender + " (Destination: " + rrep.getDestinationID() + ")");
    }

    public static void sendDATA(DATA data) {
        TestServer.echo("send DATA to " + data.getDestinationID() + " to" + data.getRoute().getNextHop(myID));
    }
}
