/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.collapse;

import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlopeLadder;

public abstract class CollapseMethod {

    public abstract boolean compute(SlopeLadder ladder);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
