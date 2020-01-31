package com.example.bachelorarbeit;

import com.example.bachelorarbeit.test.TestServer;
import java.util.Timer;
import java.util.TimerTask;

class HkoTimer {

    private final Discoverer discoverer;
    private java.util.Timer timer;
    private final static int seconds = 12;
    private boolean isRunning = false;


    HkoTimer(Discoverer discoverer) {
        this.discoverer = discoverer;
        this.timer = new java.util.Timer();
    }

    void start() {

        // cancel if timer is running
        if(isRunning){
            TestServer.echo("restart Discovery HkoTimer");
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