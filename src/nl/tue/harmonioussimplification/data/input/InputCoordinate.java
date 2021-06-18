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

import java.util.ArrayList;
import java.util.List;
import nl.tue.harmonioussimplification.data.AbstractCoordinate;
import nl.tue.geometrycore.geometry.Vector;

public class InputCoordinate extends AbstractCoordinate<InputIsoline, InputCoordinate> {

    // alignment data
    private final List<MatchInterval> matching = new ArrayList();

    public InputCoordinate(Vector location, InputIsoline isoline) {
        super(location, isoline);
    }

    public List<MatchInterval> getMatching() {
        return matching;
    }

}
