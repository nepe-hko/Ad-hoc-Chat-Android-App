package com.example.bachelorarbeit;

import com.example.bachelorarbeit.test.TestServer;
import java.util.Timer;
import java.util.TimerTask;

class DiscoveryTimer {

    private final Discoverer discoverer;
    private java.util.Timer timer;
    private final static int seconds = 30;
    private boolean isRunning = false;


    DiscoveryTimer(Discoverer discoverer) {
        this.discoverer = discoverer;
        this.timer = new java.util.Timer();
    }

    void start() {

        // cancel if timer is running
        if(isRunning){
            TestServer.echo("restart Discovery DiscoveryTimer");
            timer = new java.util.Timer();
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
    }

    public boolean isDiscovery() {
        return this.isRunning;
    }
}