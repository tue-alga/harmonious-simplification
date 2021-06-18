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

import nl.tue.harmonioussimplification.algorithms.lcfm.DiscreteMatching;
import nl.tue.harmonioussimplification.algorithms.lcfm.Instance;
import nl.tue.harmonioussimplification.algorithms.lcfm.InstanceNode;
import nl.tue.harmonioussimplification.algorithms.lcfm.SolutionTree;
import nl.tue.harmonioussimplification.algorithms.util.SlopeLadderUtil;
import nl.tue.harmonioussimplification.data.input.InputCoordinate;
import nl.tue.harmonioussimplification.data.output.OutputCoordinate;
import nl.tue.geometrycore.geometry.linear.PolyLine;

public class MapMaintainerLCFM extends MapMaintainer {

    @Override
    public void compute(OutputCoordinate coord) {

        OutputCoordinate a = coord.getExtendedStart();
        OutputCoordinate b = coord;
        OutputCoordinate c = coord.getCyclicNext();
        OutputCoordinate d = coord.getExtendedEnd();

        PolyLine newgeom = SlopeLadderUtil.newGeometry(coord);
        PolyLine oldgeom = SlopeLadderUtil.representsGeometry(coord, false);

        Instance I = new Instance(newgeom, oldgeom);
        I.computeEuclideanDistances(newgeom, oldgeom);
        SolutionTree T = new SolutionTree(I);
        T.finish();
        DiscreteMatching DM = new DiscreteMatching(I, T);

        InputCoordinate original = a.getRepresentsTo();
        int track_original = 0;
        boolean firstc = true;

        loop:
        for (InstanceNode match : DM.getMatching()) {
            if (match.getJ() != track_original) {
                original = original.getCyclicNext();
                track_original = match.getJ();
            }
            switch (match.getI()) {
                case 0: // a
                    a.setRepresentsTo(original);
                    break;
                case 1: // c
                    if (firstc) {
                        c.setRepresentsFrom(original);
                        firstc = false;
                    }
                    c.setRepresentsTo(original);
                    break;
                case 2: // d
                    d.setRepresentsFrom(original);
                    // done!
                    break loop;
                default:
                    System.err.println("??");
                    break;
            }
        }
    }

}
