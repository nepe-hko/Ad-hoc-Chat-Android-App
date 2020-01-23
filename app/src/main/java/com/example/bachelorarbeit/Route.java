package com.example.bachelorarbeit;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private List<String> hops = new ArrayList<>();

    public Route(String hop) {
        add(hop);
    }


    public void add(String hop) {
        hops.add(hop);
    }

    public String getNextHop(String myID) {

        // wenn meine eigene ID nicht in der Route auftaucht, dann an ersten Hop in Route senden
        int myIndex = hops.indexOf(myID);
        if (myIndex == -1) {
            return hops.get(0);
        }
        // wenn meine eigene ID in der Route auftaucht, dann an den Hop nach mir senden
        return hops.get(myIndex + 1);
    }

    public String getHopBefore(String myID) {
        int myIndex = hops.indexOf(myID);
        if (myIndex == 0)
            return null;
        else
            return hops.get(myIndex - 1);
    }

}
