package com.example.bachelorarbeit.types;

import com.example.bachelorarbeit.Route;

import java.util.List;
import java.util.UUID;

public class RREQ extends PayloadType  {

    private String sourceID;
    private String uID;
    private String destinationID;
    private Route route;

    public RREQ (String sourceID, String destinationID) {
        super.type = "RREQ";
        this.route = new Route(sourceID);
        this.uID = UUID.randomUUID().toString();
        this.sourceID = sourceID;
        this.destinationID = destinationID;

    }

    public void addEndpointToRoute(String userID) {
        this.route.addHop(userID);
    }

    public String getSourceID() {
        return this.sourceID;
    }

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


    public void addRouteToRoute(Route route) {
        List<String> hops = route.getHops();
        this.route.addHops(hops);
    }


}
