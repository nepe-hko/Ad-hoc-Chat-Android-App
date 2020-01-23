package com.example.bachelorarbeit.types;

import com.example.bachelorarbeit.Route;

import java.util.UUID;

public class DATA extends PayloadType  {
    private String sourceID;
    private String uID;
    private String destinationID;
    private Route route;
    private String message;

    public DATA (String sourceID, String destinationID, String message) {
        super.type = "DATA";
        this.uID = UUID.randomUUID().toString();
        this.sourceID = sourceID;
        this.destinationID = destinationID;
        this.message = message;
    }

    public void addtoRoute(String endpointID) {
        this.route.add(endpointID);
    }

    public String getSourceID() { return sourceID; }
    public String getUID() { return uID; }
    public String getDestinationID() { return destinationID; }
    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }
    public String getMessage() { return message; }

}
