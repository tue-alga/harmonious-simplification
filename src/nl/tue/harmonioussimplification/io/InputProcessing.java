/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.io;

import nl.tue.harmonioussimplification.data.input.InputCoordinate;
import nl.tue.harmonioussimplification.data.input.InputIsoline;
import nl.tue.harmonioussimplification.data.input.InputMap;

public class InputProcessing {

    public static void cleanMap(InputMap map) {
        closeAcyclicCycles(map);
        eliminateDuplicateCoordinates(map);
    }

    public static void closeAcyclicCycles(InputMap map) {
        for (InputIsoline iso : map) {
            if (!iso.isCyclic() && iso.getFirst().getLocation().isApproximately(iso.getLast().getLocation())) {
                iso.removeLast();
                iso.setCyclic(true);
            }
        }
    }

    public static void eliminateDuplicateCoordinates(InputMap map) {
        for (InputIsoline iso : map) {
            InputCoordinate coord = iso.getFirst();
            do {
                while (coord.getCyclicPrevious() != null && coord.getCyclicPrevious().getLocation().isApproximately(coord.getLocation())) {
                    iso.remove(coord.getCyclicPrevious());
                }
                coord = coord.getCyclicNext();
            } while (coord != iso.getFirst() && coord != null);
        }
    }
}
