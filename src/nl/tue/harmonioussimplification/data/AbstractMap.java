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

import java.util.ArrayList;
import nl.tue.geometrycore.geometry.linear.Rectangle;

public abstract class AbstractMap<TIso extends AbstractIsoline<TIso, TCoord>, TCoord extends AbstractCoordinate<TIso, TCoord>>
        extends ArrayList<TIso> {

    public Rectangle getBoundingBox() {
        return Rectangle.byBoundingBox(this);
    }

    public int coordinateCount() {
        int cnt = 0;
        for (TIso iso : this) {
            cnt += iso.size();
        }
        return cnt;
    }
}
