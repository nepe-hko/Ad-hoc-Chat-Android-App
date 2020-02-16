package com.example.bachelorarbeit;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Cache {


    private  Map<String,Route> routes;

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
        //TODO: test Method
        this.routes = this.routes.entrySet()
                .stream()
                .filter(entry -> !entry.getValue().getHops().contains(userID))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
