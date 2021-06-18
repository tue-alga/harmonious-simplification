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

import nl.tue.geometrycore.datastructures.doublylinkedlist.DoublyLinkedListItem;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;

public abstract class AbstractCoordinate<TIso extends AbstractIsoline<TIso, TCoord>, TCoord extends AbstractCoordinate<TIso, TCoord>>
        extends DoublyLinkedListItem<TCoord> implements GeometryConvertable<Vector> {

    // map data
    private Vector location;
    private TIso isoline;

    public AbstractCoordinate(Vector location, TIso isoline) {
        this.location = location;
        this.isoline = isoline;
    }

    public TIso getIsoline() {
        return isoline;
    }

    public void setIsoline(TIso isoline) {
        this.isoline = isoline;
    }

    public Vector getLocation() {
        return location;
    }

    public void setLocation(Vector location) {
        this.location = location;
    }

    @Override
    public Vector toGeometry() {
        return location;
    }

    public TCoord getCyclicNext() {
        TCoord next = getNext();
        if (next == null && isoline.isCyclic()) {
            return isoline.getFirst();
        } else {
            return next;
        }
    }

    public TCoord getCyclicPrevious() {
        TCoord prev = getPrevious();
        if (prev == null && isoline.isCyclic()) {
            return isoline.getLast();
        } else {
            return prev;
        }
    }
}
