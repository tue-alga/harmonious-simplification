/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithms.slopeladders.maintain;

import nl.tue.harmonioussimplification.data.output.OutputCoordinate;

public abstract class MapMaintainer {

    // should update the representsFrom/To for a/c/d (b is removed!)
    public abstract void compute(OutputCoordinate coord);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
