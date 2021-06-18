/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.maintain;

import nl.tue.harmonioussimplification.algorithm.alignments.LCFM.DiscreteMatching;
import nl.tue.harmonioussimplification.algorithm.alignments.LCFM.Instance;
import nl.tue.harmonioussimplification.algorithm.alignments.LCFM.InstanceNode;
import nl.tue.harmonioussimplification.algorithm.alignments.LCFM.SolutionTree;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.Node;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlodeLadderUtil;
import nl.tue.geometrycore.geometry.linear.PolyLine;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class LCFMMaintainer extends MapMaintainer {

    @Override
    public void compute(Node[] nodes) {

        Node a = nodes[0];
        Node b = nodes[1];
        Node c = nodes[2];
        Node d = nodes[3];

        PolyLine newgeom = SlodeLadderUtil.newGeometry(nodes);
        PolyLine oldgeom = SlodeLadderUtil.representsGeometry(nodes, false);

        Instance I = new Instance(newgeom, oldgeom);
        I.computeEuclideanDistances(newgeom, oldgeom);
        SolutionTree T = new SolutionTree(I);
        T.finish();
        DiscreteMatching DM = new DiscreteMatching(I, T);

        int offset = a.representsTo;
        boolean firstc = true;

        loop:
        for (InstanceNode match : DM.getMatching()) {
            switch (match.getI()) {
                case 0: // a
                    a.representsTo = offset + match.getJ();
                    break;
                case 1: // c
                    if (firstc) {
                        c.representsFrom = offset + match.getJ();
                        firstc = false;
                    }
                    c.representsTo = offset + match.getJ();
                    break;
                case 2: // d
                    d.representsFrom = offset + match.getJ();
                    // done!
                    break loop;
                default:
                    System.err.println("??");
                    break;
            }
        }
    }

}
