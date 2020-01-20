package com.example.bachelorarbeit.test;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;

public class NodeServer {

    private PrintWriter out;
    private BufferedReader in;

    public void connect(){
        String ip = "80.139.94.83";
        int port = 3333;
        try {

            Socket socket = new Socket(ip, port);
            socket.connect(new SocketAddress() {
                @Override
                public int hashCode() {
                    return super.hashCode();
                }
            });
            Log.d("test", "Verbunden zu Socket");
            //this.out = new PrintWriter(socket.getOutputStream(), true);
            //this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("test", "Verbindungsaufbau zu Socket fehlgeschlagen");
        }
    }

    public void send(String msg) {
        out.println(msg);
    }
}
