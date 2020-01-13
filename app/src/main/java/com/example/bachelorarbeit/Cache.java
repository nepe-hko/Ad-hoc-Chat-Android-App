package com.example.bachelorarbeit;

import java.util.List;
import java.util.Map;

public class Cache {

    private Map<String,List<Route>> routes;

    public void deleteRoute(String endpointID, Route route) {
        routes.get(endpointID).remove(route);
    }
    public void addRoute (String endpointID,Route route) {
        routes.get(endpointID).add(route);
    }
    public Route getRoute (String endpointID) { return new Route();}
    public boolean hasRoute(String endpointID) { return false;}

}
