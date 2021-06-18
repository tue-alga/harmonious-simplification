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

import nl.tue.harmonioussimplification.map.Isoline;
import nl.tue.geometrycore.datastructures.doublylinkedlist.DoublyLinkedListItem;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;

public class Node extends DoublyLinkedListItem<Node> implements GeometryConvertable<Vector> {

    public NodeList list = null;
    public Vector loc = null;
    public SlopeLadder ladder = null;
    public Vector ladderloc = null;
    public Vector ladderlocTrimmed = null;
    public Line perp = null;
    public Isoline isoline;
    public int representsFrom = -1;
    public int representsTo = -1;
    public Node replacedWith = null;

    @Override
    public Vector toGeometry() {
        return loc;
    }
}
