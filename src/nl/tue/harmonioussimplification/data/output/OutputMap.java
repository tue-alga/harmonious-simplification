/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.data.output;

import java.util.ArrayList;
import java.util.List;
import nl.tue.harmonioussimplification.data.AbstractMap;

public class OutputMap extends AbstractMap<OutputIsoline, OutputCoordinate> {

    private List<SlopeLadder> ladders = new ArrayList();

    public List<SlopeLadder> getLadders() {
        return ladders;
    }

}
