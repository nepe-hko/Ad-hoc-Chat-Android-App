package com.example.bachelorarbeit.types;

import com.example.bachelorarbeit.Route;

import java.util.UUID;

public class RERR extends PayloadType  {

    private String sourceID;
    private String uID;
    private String destinationID;
    private Route route;

    public RERR (DATA data,  String myID) {
        //TODO: include failed hop in RREQ
        super.type = "RERR";
        this.uID = data.getUID();
        this.sourceID = myID;
        this.destinationID = data.getSourceID();

        Route r = data.getRoute();
        r.removeHopsAfterIncl(myID);
        r.reverse();
        r.addHop(data.getSourceID());
        this.route = r;
    }

    public String getSourceID() { return sourceID; }
    public String getUID() { return uID; }
    public String getDestinationID() { return destinationID; }
    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

}
