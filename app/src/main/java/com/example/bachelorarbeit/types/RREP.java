package com.example.bachelorarbeit.types;

import com.example.bachelorarbeit.Route;

import java.util.UUID;

public class RREP extends PayloadType  {


    //private String sourceID; wird denke ich nicht benötigt
    private String uID;
    private String destinationID;
    private Route route;

    public RREP (RREQ rreq, Route route) {
        super.type = "RREP";
        this.uID = rreq.getUID();
        this.destinationID = rreq.getSourceID();
        //this.route = rreq.getRouteReverse();
        //this.route.
    }

    //public String getSourceID() { return sourceID; }
    //public void setSourceID(String sourceID) { this.sourceID = sourceID; }
    public String getUID() { return uID; }
    public void setUID(String uID) { this.uID = uID; }
    public String getDestinationID() { return destinationID; }
    public void setDestinationID(String destinationID) { this.destinationID = destinationID; }
    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

}
