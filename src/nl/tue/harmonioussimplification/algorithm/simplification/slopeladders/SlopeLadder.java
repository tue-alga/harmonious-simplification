/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification.slopeladders;

import java.util.ArrayList;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;

public class SlopeLadder extends ArrayList<Node> {

    public Line perpendicular;
    public LineSegment sampleRegion;

    // convention: first of the nodes is in
    public boolean dirty = true;
    public boolean contractible = false;
    public double cost = Double.NaN;
    public Node startPinch = null, endPinch = null;

    public Node[] getNeighbors(int i) {

        Node b = get(i);
        return getNeighbors(b);
    }

    public Node[] getNeighbors(Node b) {

        Node a = b.getPrevious();
        if (a == null) {
            return null;
        }
        Node c = b.getNext();
        if (c == null) {
            return null;
        }
        Node d = c.getNext();
        if (d == null) {
            return null;
        }
        return new Node[]{a, b, c, d};
    }
}
