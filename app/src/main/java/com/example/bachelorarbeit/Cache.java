package com.example.bachelorarbeit;

import android.util.Log;

import com.example.bachelorarbeit.test.TestServer;

import java.util.HashMap;
import java.util.Map;

public class Cache {

    private final Map<String,Route> routes;

    public Cache() {
        this.routes = new HashMap<>();
    }


    public void deleteRoute(String userID, Route route) {
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

}
