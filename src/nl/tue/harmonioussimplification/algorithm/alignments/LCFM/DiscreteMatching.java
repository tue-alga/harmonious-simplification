/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.alignments.LCFM;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.util.DoubleUtil;

public class DiscreteMatching {

    private Instance I;
    private List<InstanceNode> M = new ArrayList<InstanceNode>();

    public DiscreteMatching(Instance I, SolutionTree T) {
        this.I = I;
        SolutionNode n = T.getNodes()[I.N - 1][I.M - 1];
        while (n != null) {
            M.add(0, n.getNode());
            n = n.getPred();
        }
    }

    public List<InstanceNode> getMatching() {
        return M;
    }

    public boolean verify() {
        boolean correct = true;
        for (int t1 = 0; t1 < M.size(); t1++) {
            for (int t2 = t1 + 1; t2 < M.size(); t2++) {
                correct = correct && verify(t1, t2);
            }
        }
        return correct;
    }

    private boolean verify(int t1, int t2) {
        double match = 0;
        for (int t = t1; t <= t2; t++) {
            match = Math.max(match, M.get(t).value);
        }

        int n0 = M.get(t1).i;
        int m0 = M.get(t1).j;
        int n = M.get(t2).i - n0 + 1;
        int m = M.get(t2).j - m0 + 1;

        double[][] dfd = new double[n][m];

        dfd[0][0] = I.values[n0][m0].value;
        for (int i = 1; i < n; i++) {
            dfd[i][0] = Math.max(dfd[i - 1][0], I.values[n0 + i][m0].value);
        }
        for (int j = 1; j < m; j++) {
            dfd[0][j] = Math.max(dfd[0][j - 1], I.values[n0][m0 + j].value);
        }

        for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
                dfd[i][j] = Math.max(
                        Math.min(dfd[i - 1][j - 1], Math.min(dfd[i][j - 1], dfd[i - 1][j])), I.values[n0 + i][m0 + j].value);
            }
        }

        if (dfd[n - 1][m - 1] != match) {
            System.err.println("Error " + t1 + " - " + t2);
            System.err.println("  Match " + match);
            System.err.println("  DFD   " + dfd[n - 1][m - 1]);
        }

        return DoubleUtil.close(dfd[n - 1][m - 1], match);
    }
}
