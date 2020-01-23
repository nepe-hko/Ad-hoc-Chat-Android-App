package com.example.bachelorarbeit;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Cache {

    private Map<String,Route> routes;

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
        if (routes.containsKey(userID)) {
            Log.d("test", "route vorhanden");
            return true;
        }
        Log.d("test", "route nicht vorhanden");
        return false;
    }

}
