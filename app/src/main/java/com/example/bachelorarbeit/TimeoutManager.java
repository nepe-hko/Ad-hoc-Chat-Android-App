package com.example.bachelorarbeit;

import com.example.bachelorarbeit.test.TestServer;

import java.util.Timer;
import java.util.TimerTask;

class TimeoutManager {

    private final Discoverer discoverer;
    private Timer timer;
    private final static int seconds = 20;
    private boolean isRunning = false;
    private final TestServer testServer;

    TimeoutManager(Discoverer discoverer, TestServer testServer) {
        this.discoverer = discoverer;
        this.timer = new Timer();
        this.testServer = testServer;
    }

    void startTimer() {

        testServer.echo("start Discovery Timer");

        // cancel if timer is running
        if(isRunning){
            testServer.echo("restart Discovery Timer");
            timer = new Timer();
        }

        // start new timer
        isRunning = true;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerExpired();
            }
        },seconds * 1000);

    }

    private void timerExpired() {
        isRunning = false;
        discoverer.onDiscoveryTimerExpired();
        testServer.echo("Discovery Timer expired");
    }

    public boolean isDiscovery() {
        return this.isRunning;
    }
}