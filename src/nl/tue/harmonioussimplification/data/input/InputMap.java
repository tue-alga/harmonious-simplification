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

import nl.tue.harmonioussimplification.data.AbstractMap;

public class InputMap extends AbstractMap<InputIsoline, InputCoordinate> {

    public double distanceNorm() {
        return getBoundingBox().diagonal();
    }
}
