/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.alignments;

import nl.tue.harmonioussimplification.algorithm.alignments.LCFM.DiscreteMatching;
import nl.tue.harmonioussimplification.algorithm.alignments.LCFM.Instance;
import nl.tue.harmonioussimplification.algorithm.alignments.LCFM.InstanceNode;
import nl.tue.harmonioussimplification.algorithm.alignments.LCFM.SolutionTree;
import nl.tue.harmonioussimplification.map.Neighbor;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

public class LCFMAlign extends Alignment {

    public boolean useCrossingPenalty = false;
    public boolean useGeodesic = true;
    public double crossingPenalty = 1;
// "distance" between two vertices is going to be E*(1+#*cp) where 
    // E is euclidean distance, 
    // # is number of crossings the straightline causes and 
    // cp the the crossing penalty parameter

    @Override
    protected void process(Neighbor nbrA, Neighbor nbrB) {

        Instance I = new Instance(nbrA.from.input, nbrA.to.input);
        long start = System.currentTimeMillis();
        //System.out.println("start matching");
        if (useGeodesic) {
            I.computeGeodesicDistances(nbrA.from.input, nbrA.to.input);
        } else if (useCrossingPenalty) {
            I.computeCrossingPenaltyDistances(nbrA.from.input, nbrA.to.input, crossingPenalty);
        } else {
            I.computeEuclideanDistances(nbrA.from.input, nbrA.to.input);
        }
        long init = System.currentTimeMillis();
        //System.out.println("init " + (init - start));
        SolutionTree T = new SolutionTree(I);
        T.finish();
        DiscreteMatching DM = new DiscreteMatching(I, T);
        long done = System.currentTimeMillis();
        //System.out.println("done " + (done - init));

        nbrA.matchNone();
        nbrB.matchNone();

        for (InstanceNode n : DM.getMatching()) {
            nbrA.get(n.getI()).add(n.getJ());
            nbrB.get(n.getJ()).add(n.getI());
        }
    }

    @Override
    public void createGUI(SideTab tab) {
        tab.addCheckbox("Crossing penalty", useCrossingPenalty, (e, v) -> useCrossingPenalty = v);
        tab.addDoubleSpinner(crossingPenalty, 0, Integer.MAX_VALUE, 1, (e, v) -> crossingPenalty = v);
        tab.addCheckbox("Geodesic distance", useGeodesic, (e, v) -> useGeodesic = v);
    }

    @Override
    public String whatHaveYouDone() {
        return "LCFMAlign[useGeodesic=" + useGeodesic + ";useCrossingPenalty=" + useCrossingPenalty + ";crossingPenalty=" + crossingPenalty + "]";
    }

    @Override
    public String toString() {
        return "LCFMAlign";
    }
}
