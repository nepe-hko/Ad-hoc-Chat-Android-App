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

    public String getNextHop(String currentHop) {

        // if route does not contain currentHop, return first hop in Route
        int myIndex = this.hops.indexOf(currentHop);
        if (myIndex == -1) {
            return this.hops.get(0);
        }
        // ir route does contain current, return hop after current hop
        return this.hops.get(myIndex + 1);
    }

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

    public void removeHopsAfter(String userID) {
        int myIndex = this.hops.indexOf(userID);
        hops.subList(myIndex + 1, hops.size()).clear();
    }

    public boolean containsSeries(String userID1, String userID2) {
        for(int i = 0; i < hops.size() - 1; i++) {
            if ((hops.get(i).equals(userID1) && hops.get(i+1).equals(userID2)) ||
                    (hops.get(i).equals(userID2) && hops.get(i+1).equals(userID1))) {
                return true;
            }
        }
        return false;
    }
}
