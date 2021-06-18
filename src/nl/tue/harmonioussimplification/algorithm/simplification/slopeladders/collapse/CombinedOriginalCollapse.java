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

import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlodeLadderUtil;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.Node;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.PolyLine;

public class CombinedOriginalCollapse extends CombinedCollapse {

    @Override
    protected double computeSingleScore(Node[] nodes, Vector sol) {
        PolyLine newGeometry = SlodeLadderUtil.newGeometry(nodes, sol);
        PolyLine oldGeometry = SlodeLadderUtil.representsGeometry(nodes, true);
        oldGeometry.addVertex(0, newGeometry.getStart());
        oldGeometry.addVertex(newGeometry.getEnd());
        return SlodeLadderUtil.computeDirectedHausdorffDistance(newGeometry, oldGeometry);
    }
    
}
