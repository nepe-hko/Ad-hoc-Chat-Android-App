package com.example.bachelorarbeit;

import android.util.Log;

import com.example.bachelorarbeit.test.TestServer;
import com.google.android.gms.common.data.DataBufferObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class Cache {


    private final Map<String,Route> routes;

    public Cache() {
        this.routes = new HashMap<>();
    }


    public void deleteRoute(String userID) {
        routes.remove(userID);
    }
    public void setRoute (String userID,Route route) {
        routes.put(userID, route);
    }
    public Route getRoute (String userID) {
        return routes.get(userID);
    }
    public boolean hasRoute(String userID) {
        return routes.containsKey(userID);
    }

    public void deleteRoutesContainingHop(String userID) {
        //TODO: implement Method
    }
}
