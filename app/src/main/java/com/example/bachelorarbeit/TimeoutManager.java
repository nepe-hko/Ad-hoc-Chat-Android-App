package com.example.bachelorarbeit;

import com.example.bachelorarbeit.test.TestServer;

import java.util.Timer;
import java.util.TimerTask;

class TimeoutManager {

    private final Discoverer discoverer;
    private final Timer timer;
    private final static int seconds = 20;
    private boolean isRunning = false;
    private final TestServer testServer;
    private final String myID;

    TimeoutManager(Discoverer discoverer, String myID, TestServer testServer) {
        this.discoverer = discoverer;
        this.timer = new Timer();
        this.testServer = testServer;
        this.myID = myID;
    }

    void startTimer() {

        testServer.echo(myID + ": start Discovery Timer");

        // cancel if timer is running
        if(isRunning){
            testServer.echo(myID + ": restart Discovery Timer");
            timer.cancel();
        }

        // start new timer
        isRunning = true;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerExpired();
                isRunning = false;
            }
        },seconds * 1000);

    }

    private void timerExpired() {

        discoverer.onDiscoveryTimerExpired();
        testServer.echo(myID + ": Discovery Timer expired");
    }

    public boolean isDiscovery() {
        return this.isRunning;
    }
}