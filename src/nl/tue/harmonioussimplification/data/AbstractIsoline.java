/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.data;

import nl.tue.geometrycore.datastructures.doublylinkedlist.DoublyLinkedList;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;

public abstract class AbstractIsoline<TIso extends AbstractIsoline<TIso, TCoord>, TCoord extends AbstractCoordinate<TIso, TCoord>>
        extends DoublyLinkedList<TCoord> implements GeometryConvertable {

    private boolean cyclic;

    public AbstractIsoline(boolean cyclic) {
        this.cyclic = cyclic;
    }

    @Override
    public BaseGeometry toGeometry() {
        if (cyclic) {
            Polygon p = new Polygon();
            for (TCoord c : this) {
                p.addVertex(c.toGeometry());
            }
            return p;
        } else {
            PolyLine p = new PolyLine();
            for (TCoord c : this) {
                p.addVertex(c.toGeometry());
            }
            return p;
        }
    }

    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

}
