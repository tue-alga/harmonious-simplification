/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithms.slopeladders.score;

import nl.tue.harmonioussimplification.data.output.SlopeLadder;
import nl.tue.harmonioussimplification.data.output.OutputCoordinate;

public abstract class ScoreFunction {

    public abstract double computeSingle(OutputCoordinate coord);

    public double compute(SlopeLadder ladder, boolean normalize) {
        double score = 0;
        int size = 0;
        for (OutputCoordinate coord : ladder) {
            if (coord.isCollapsable()) {
                score += computeSingle(coord);
                size++;
            }
        }
        if (normalize) {
            score = score / size;
        }
        return score;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
