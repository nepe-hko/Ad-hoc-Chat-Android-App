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
    }

    public void addtoRoute(String endpointID) {
        this.route.add(endpointID);
    }

    public String getSourceID() { return sourceID; }
    public void setSourceID(String sourceID) { this.sourceID = sourceID; }
    public String getUID() { return uID; }
    public void setUID(String uID) { this.uID = uID; }
    public String getDestinationID() { return destinationID; }
    public void setDestinationID(String destinationID) { this.destinationID = destinationID; }
    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
