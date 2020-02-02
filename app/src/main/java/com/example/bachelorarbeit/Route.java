package com.example.bachelorarbeit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route implements Serializable {
    private final List<String> hops;

    public Route(String hop) {
        this.hops = new ArrayList<>();
        addHop(hop);
    }


    public void addHop(String hop) {
        this.hops.add(hop);
    }

    public void addHops(List<String> hops) {
        this.hops.addAll(hops);
    }

    public String getNextHop(String myID) {

        // wenn meine eigene ID nicht in der Route auftaucht, dann an ersten Hop in Route senden
        int myIndex = this.hops.indexOf(myID);
        if (myIndex == -1) {
            return this.hops.get(0);
        }
        // wenn meine eigene ID in der Route auftaucht, dann an den Hop nach mir senden
        //TODO hier fehler
        return this.hops.get(myIndex + 1);
    }

    // is only needed for testing
    public String getHopBefore(String myID) {
        int myIndex = this.hops.indexOf(myID);
        if (myIndex == 0)
            return null;
        else
            return this.hops.get(myIndex - 1);
    }

    public List<String> getHops() {
        return this.hops;
    }

    public void reverse() {
        Collections.reverse(this.hops);
    }

    public void remove(String userID) {
        this.hops.remove(userID);
    }
}
