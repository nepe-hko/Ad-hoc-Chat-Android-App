package com.example.bachelorarbeit.types;

import com.example.bachelorarbeit.Route;

import java.util.UUID;

public class RERR extends PayloadType  {

    private String sourceID;
    private String uID;
    private String destinationID;
    private Route route;

    public RERR () {
        super.type = "RERR";
        this.uID = UUID.randomUUID().toString();
    }

    public String getSourceID() { return sourceID; }
    public void setSourceID(String sourceID) { this.sourceID = sourceID; }
    public String getUID() { return uID; }
    public void setUID(String uID) { this.uID = uID; }
    public String getDestinationID() { return destinationID; }
    public void setDestinationID(String destinationID) { this.destinationID = destinationID; }
    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

}
