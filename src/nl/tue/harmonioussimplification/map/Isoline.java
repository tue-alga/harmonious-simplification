/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.map;

import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.Node;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.NodeList;
import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.linear.PolyLine;

public class Isoline {

    // input
    public int index;
    public PolyLine input;
    public List<Neighbor> neighbors;
    // cached output for ladders
    public NodeList nodes;

    public Isoline(int index, PolyLine input) {
        this.index = index;
        this.input = input;
        this.neighbors = new ArrayList();
    }

    public PolyLine outputPolyline() {
        PolyLine poly = null;
        if (nodes != null) {
            poly = new PolyLine();
            for (Node n : nodes) {
                poly.addVertex(n.loc);
            }
        }
        return poly;
    }

    public void clearCache(boolean alignCache, boolean simplifyCache) {
        if (simplifyCache) {
            nodes = null;
        }
        for (Neighbor nbr : neighbors) {
            nbr.clearCache(alignCache, simplifyCache);
        }
    }

    public String getName() {
        int i = index;
        String name = "";
        do {
            int m = i % 26;
            name = (char) ('A' + m) + name;
            i = i / 26;
        } while (i > 0);
        return name;
    }

}
