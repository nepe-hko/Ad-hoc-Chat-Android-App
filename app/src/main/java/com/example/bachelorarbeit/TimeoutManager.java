package com.example.bachelorarbeit;

import java.util.Timer;
import java.util.TimerTask;

class TimeoutManager {

    private final Discoverer discoverer;
    private final Timer timer;
    private final static int seconds = 5;
    private boolean isRunning = false;

    TimeoutManager(Discoverer discoverer) {
        this.discoverer = discoverer;
        this.timer = new Timer();
    }

    void startTimer() {

        // cancel if timer is running
        if(isRunning){
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
    }

    public boolean isDiscovery() {
        return this.isRunning;
    }
}