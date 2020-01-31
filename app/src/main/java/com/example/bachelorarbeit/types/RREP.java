package com.example.bachelorarbeit.types;

import com.example.bachelorarbeit.Route;

import java.util.UUID;

public class RREP extends PayloadType  {


    //private String sourceID; wird denke ich nicht benötigt
    private String uID;
    private String destinationID;
    private Route route;

    public RREP (RREQ rreq) {
        super.type = "RREP";
        this.uID = rreq.getUID();
        this.destinationID = rreq.getSourceID();
        this.route = rreq.getRoute();
        reverseRoute();
    }

    //public String getSourceID() { return sourceID; }
    //public void setSourceID(String sourceID) { this.sourceID = sourceID; }
    public String getUID() {
        return this.uID;
    }
    public String getDestinationID() {
        return this.destinationID;
    }
    public Route getRoute() {
        return this.route;
    }
    public void setRoute(Route route) {
        this.route = route;
    }

    public void reverseRoute() {
        this.route.reverse();
    }

    public String getFirstHop() {
        return this.route.getHops().get(0);
    }

    public void removeFromRoute(String myID) {
        this.route.remove(myID);
    }
}
