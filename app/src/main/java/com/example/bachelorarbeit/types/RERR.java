package com.example.bachelorarbeit.types;

import com.example.bachelorarbeit.Route;

public class RERR extends NearbyPayload {

    private final String sourceID;
    private final String uID;
    private final String destinationID;
    private Route route;

    public RERR (DATA data,  String myID) {
        super.type = "RERR";
        this.uID = data.getUID();
        this.sourceID = myID;
        this.destinationID = data.getSourceID();

        Route r = data.getRoute();
        r.removeHopsAfter(r.getNextHop(myID));
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
