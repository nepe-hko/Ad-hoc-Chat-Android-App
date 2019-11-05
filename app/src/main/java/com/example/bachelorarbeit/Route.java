package com.example.bachelorarbeit;

import com.example.bachelorarbeit.types.PayloadType;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private String endpointID;
    private List<String> endpointIDs = new ArrayList<>();


    public void add(String endpointID) {

        endpointIDs.add(endpointID);
    }

    public String getNextHop(String myID) {

        // wenn meine eigene ID nicht in der Route auftaucht, dann an ersten Hop in Route senden
        int myIndex = endpointIDs.indexOf(myID);
        if (myIndex == -1) {
            return endpointIDs.get(0);
        }
        // wenn meine eigene ID in der Route auftaucht, dann an den Hop nach mir senden
        return endpointIDs.get(myIndex + 1);
    }

}
