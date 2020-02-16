package com.example.bachelorarbeit.types;

import com.example.bachelorarbeit.Route;

public class ACK extends PayloadType  {
    private String originalSourceID;
    private String originalUID;
    private Route route;

    public ACK(DATA data) {
        super.type = "ACK";
        this.originalSourceID = data.getSourceID();
        this.originalUID = data.getUID();
        Route r = data.getRoute();
        r.reverse();
        r.addHop(data.getSourceID());
        this.route = r;
    }

    public Route getRoute() {
        return this.route;
    }

    public String getOriginalSourceID() {
        return this.originalSourceID;
    }

    public String getOriginalUID() {
        return this.originalUID;
    }

}
