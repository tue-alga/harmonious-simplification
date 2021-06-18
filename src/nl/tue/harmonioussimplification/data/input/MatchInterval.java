/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.data.input;

import java.util.HashMap;
import java.util.Iterator;

public class MatchInterval implements Iterable<InputCoordinate> {

    // NB: should be from same isoline!
    private InputCoordinate first;
    private InputCoordinate last;

    @Override
    public Iterator<InputCoordinate> iterator() {
        return new Iterator<InputCoordinate>() {
            InputCoordinate next = first;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public InputCoordinate next() {
                InputCoordinate result = next;
                if (result == last) {
                    next = null;
                } else {
                    next = next.getCyclicNext();
                }
                return result;
            }
        };
    }

    public InputIsoline getIsoline() {
        return first.getIsoline();
    }

    public InputCoordinate getFirst() {
        return first;
    }

    public void setFirst(InputCoordinate first) {
        this.first = first;
    }

    public InputCoordinate getLast() {
        return last;
    }

    public void setLast(InputCoordinate last) {
        this.last = last;
    }

    MatchInterval shallowClone(HashMap<InputIsoline, InputIsoline> iso_hash, HashMap<InputCoordinate, InputCoordinate> coord_hash) {
        MatchInterval match = new MatchInterval();
        match.first = coord_hash.get(first);
        match.last = coord_hash.get(last);
        return match;
    }

}
