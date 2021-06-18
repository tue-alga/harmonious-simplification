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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Neighbor extends ArrayList<Set<Integer>> {

    public Neighbor(Isoline from, Isoline to) {
        this.from = from;
        this.to = to;
    }

    // input
    public Neighbor twin;
    public Isoline from;
    public Isoline to;

    public void clearCache(boolean alignCache, boolean simplifyCache) {
        if (alignCache) {
            clear();
        }
    }

    public void matchAll() {
        clear();
        int numVertices = from.input.vertexCount();
        while (numVertices > 0) {
            add(null);
            numVertices--;
        }
    }

    public void matchNone() {
        clear();
        int numVertices = from.input.vertexCount();
        while (numVertices > 0) {
            add(new HashSet());
            numVertices--;
        }
    }

}
