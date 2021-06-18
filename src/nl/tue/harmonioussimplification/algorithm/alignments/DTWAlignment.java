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

import nl.tue.harmonioussimplification.map.Neighbor;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.harmonioussimplification.gui.Data;

public class DTWAlignment extends Alignment {

    int splash = 0;

    @Override
    protected void process(Neighbor nbrA, Neighbor nbrB) {

        int nA = nbrA.from.input.vertexCount();
        int nB = nbrA.to.input.vertexCount();
        double[][] dtw = new double[nA][nB];
        int[][] choices = new int[nA][nB];

        dtw[0][0] = nbrA.from.input.vertex(0).distanceTo(nbrA.to.input.vertex(0));
        choices[0][0] = 0;
        for (int i = 1; i < nA; i++) {
            dtw[i][0] = dtw[i - 1][0] + nbrA.from.input.vertex(i).distanceTo(nbrA.to.input.vertex(0));
            choices[i][0] = -1;
        }
        for (int j = 1; j < nB; j++) {
            dtw[0][j] = dtw[0][j - 1] + nbrA.from.input.vertex(0).distanceTo(nbrA.to.input.vertex(j));
            choices[0][j] = 1;
        }
        for (int i = 1; i < nA; i++) {
            for (int j = 1; j < nB; j++) {
                dtw[i][j] = nbrA.from.input.vertex(i).distanceTo(nbrA.to.input.vertex(j));
                if (dtw[i - 1][j - 1] <= Math.min(dtw[i - 1][j], dtw[i][j - 1])) {
                    dtw[i][j] += dtw[i - 1][j - 1];
                    choices[i][j] = 0;
                } else if (dtw[i - 1][j] <= dtw[i][j - 1]) {
                    dtw[i][j] += dtw[i - 1][j];
                    choices[i][j] = -1;
                } else {
                    dtw[i][j] += dtw[i][j - 1];
                    choices[i][j] = 1;
                }
            }
        }

        nbrA.matchNone();
        nbrB.matchNone();

        int wi = nA - 1;
        int wj = nB - 1;
        while (wi + wj >= 0) {
            nbrA.get(wi).add(wj);
            nbrB.get(wj).add(wi);
            for (int s = 1; s <= splash; s++) {
                if (wj + s < nB) {
                    nbrA.get(wi).add(wj + s);
                    nbrB.get(wj + s).add(wi);
                }

                if (wj - s >= 0) {
                    nbrA.get(wi).add(wj - s);
                    nbrB.get(wj - s).add(wi);
                }

                if (wi + s < nA) {
                    nbrA.get(wi + s).add(wj);
                    nbrB.get(wj).add(wi + s);
                }

                if (wi - s >= 0) {
                    nbrA.get(wi - s).add(wj);
                    nbrB.get(wj).add(wi - s);
                }
            }
            switch (choices[wi][wj]) {
                case 0: {
                    // diagonal
                    wi--;
                    wj--;
                    break;
                }
                case 1: {
                    // reduce wj
                    wj--;
                    break;
                }
                case -1: {
                    // reduce wi
                    wi--;
                    break;
                }
            }
        }
    }

    @Override
    public void createGUI(SideTab tab) {
        tab.makeSplit(4, 2);
        tab.addLabel("Splash");
        tab.addIntegerSpinner(splash, 0, Integer.MAX_VALUE, 1, (e, v) -> {
            splash = v;
            Data.autorun(true, true);
        });
    }

    @Override
    public String whatHaveYouDone() {
        return "DTWAlign[splash=" + splash + "]";
    }

    @Override
    public String toString() {
        return "DTWAlign";
    }
}
