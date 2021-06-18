/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.score;

import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.Node;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlopeLadder;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class ScoreFunction {

    public abstract double computeSingle(Node[] nodes);

    public double compute(SlopeLadder ladder, boolean normalize) {
        double score = 0;
        int size = 0;
        for (int i = 0; i < ladder.size(); i++) {
            Node[] nodes = ladder.getNeighbors(i);
            if (nodes != null) { //iow: if this part was contract
            	score += computeSingle(nodes);
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
