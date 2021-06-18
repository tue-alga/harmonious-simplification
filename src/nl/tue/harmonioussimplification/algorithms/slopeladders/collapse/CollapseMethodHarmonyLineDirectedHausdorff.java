/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithms.slopeladders.collapse;

import nl.tue.harmonioussimplification.algorithms.util.HausdorffDistance;
import nl.tue.harmonioussimplification.algorithms.util.SlopeLadderUtil;
import nl.tue.harmonioussimplification.data.output.OutputCoordinate;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.PolyLine;

public class CollapseMethodHarmonyLineDirectedHausdorff extends CollapseMethodHarmonyLine {

    @Override
    protected double computeSingleScore(OutputCoordinate coord, Vector sol) {
        PolyLine newGeometry = SlopeLadderUtil.newGeometry(coord, sol);
        PolyLine oldGeometry = SlopeLadderUtil.representsGeometry(coord, true);
        oldGeometry.addVertex(0, newGeometry.getStart());
        oldGeometry.addVertex(newGeometry.getEnd());
        return HausdorffDistance.computeDirectedHausdorffDistance(newGeometry, oldGeometry);
    }

}
