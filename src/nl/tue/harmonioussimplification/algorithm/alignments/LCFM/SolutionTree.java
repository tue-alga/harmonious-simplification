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

import java.util.Iterator;
import java.util.List;
import nl.tue.geometrycore.util.DoubleUtil;

public class SolutionTree {

    private Instance I;
    private SolutionNode[][] nodes;
    private int next_i, next_j;

    public SolutionTree(Instance I) {
        this.I = I;
        init();
    }

    private void init() {
        nodes = new SolutionNode[I.N][I.M];
        for (int i = 0; i < I.N; i++) {
            for (int j = 0; j < I.M; j++) {
                nodes[i][j] = new SolutionNode();
                nodes[i][j].node = I.values[i][j];
            }
        }

        for (int i = 1; i < I.N; i++) {
            nodes[i][0].pred = nodes[i - 1][0];
            nodes[i - 1][0].right = nodes[i][0];
        }

        for (int j = 1; j < I.M; j++) {
            nodes[0][j].pred = nodes[0][j - 1];
            nodes[0][j - 1].up = nodes[0][j];
        }

        next_i = 1;
        next_j = 1;
    }

    private void expandTree(int i, int j) {
        SolutionNode node = nodes[i][j];
        SolutionNode down = nodes[i][j - 1];
        SolutionNode diagonal = nodes[i - 1][j - 1];
        SolutionNode left = nodes[i - 1][j];

        SolutionNode best = down;

        if (better(diagonal, best)) {
            best = diagonal;
        }

        if (better(left, best)) {
            best = left;
        }

        node.pred = best;
        if (best == down) {
            best.up = node;
        } else if (best == diagonal) {
            best.diagonal = node;
        } else {
            best.right = node;
        }

        // make up shortcut for nodes[i][j - 1] if necessary
        if (down.sc_up == null) {
            Shortcut sc = new Shortcut();
            down.sc_up = sc;

            sc.from = down;
            if (down.pred.isRoot()) {
                sc.to = down.pred;
                sc.inc = Incoming.RIGHT;
                sc.to.getIncs(sc.inc).add(sc);
                sc.max = down.node.value;
            } else if (down.pred.outdegree() == 1) {
                sc.to = down.pred.sc_up.to;
                sc.inc = down.pred.sc_up.inc;
                sc.to.getIncs(sc.inc).add(sc);
                sc.max = Math.max(down.node.value, down.pred.sc_up.max);
            } else {
                sc.to = down.pred;
                sc.inc = Incoming.RIGHT;
                sc.to.getIncs(sc.inc).add(sc);
                sc.max = down.node.value;
            }
        }

        // make right shortcut for nodes[i - 1][j] if necessary
        if (left.sc_right == null) {
            Shortcut sc = new Shortcut();
            left.sc_right = sc;

            sc.from = left;
            if (left.pred.isRoot()) {
                sc.to = left.pred;
                sc.inc = Incoming.UP;
                sc.to.getIncs(sc.inc).add(sc);
                sc.max = left.node.value;
            } else if (left.pred.outdegree() == 1) {
                sc.to = left.pred.sc_right.to;
                sc.inc = left.pred.sc_right.inc;
                sc.to.getIncs(sc.inc).add(sc);
                sc.max = Math.max(left.node.value, left.pred.sc_right.max);
            } else {
                sc.to = left.pred;
                sc.inc = Incoming.UP;
                sc.to.getIncs(sc.inc).add(sc);
                sc.max = left.node.value;
            }
        }

        // make shortcuts for nodes[i][j] where necessary
        if (best == down) {
            // went up, make up shortcut
            Shortcut sc = new Shortcut();
            node.sc_up = sc;

            sc.from = node;
            sc.to = down.sc_up.to;
            sc.inc = down.sc_up.inc;
            sc.to.getIncs(sc.inc).add(sc);
            sc.max = Math.max(node.node.value, down.sc_up.max);
        } else if (best == diagonal) {
            // diagonal, make both shortcuts
            Shortcut sc_right = new Shortcut();
            node.sc_right = sc_right;

            sc_right.from = node;
            if (diagonal.right == null) {
                sc_right.to = diagonal.sc_right.to;
                sc_right.inc = diagonal.sc_right.inc;
                sc_right.to.getIncs(sc_right.inc).add(sc_right);
                sc_right.max = Math.max(node.node.value, diagonal.sc_right.max);
            } else {
                sc_right.to = diagonal;
                sc_right.inc = Incoming.DIAGRIGHT;
                sc_right.to.getIncs(sc_right.inc).add(sc_right);
                sc_right.max = node.node.value;
            }

            Shortcut sc_up = new Shortcut();
            node.sc_up = sc_up;

            sc_up.from = node;
            if (diagonal.up == null) {
                sc_up.to = diagonal.sc_up.to;
                sc_up.inc = diagonal.sc_up.inc;
                sc_up.to.getIncs(sc_up.inc).add(sc_up);
                sc_up.max = Math.max(node.node.value, diagonal.sc_up.max);
            } else {
                sc_up.to = diagonal;
                sc_up.inc = Incoming.DIAGUP;
                sc_up.to.getIncs(sc_up.inc).add(sc_up);
                sc_up.max = node.node.value;
            }
        } else {
            // went right, make right shortcut
            Shortcut sc = new Shortcut();
            node.sc_right = sc;

            sc.from = node;
            sc.to = left.sc_right.to;
            sc.inc = left.sc_right.inc;
            sc.to.getIncs(sc.inc).add(sc);
            sc.max = Math.max(node.node.value, left.sc_right.max);
        }

        if (diagonal.outdegree() == 0) {
            // kill diagonal branch
            SolutionNode dead = diagonal;
            SolutionNode alive = diagonal.pred;
            while (alive.outdegree() == 1) {
                // kill alive
                alive.up = alive.diagonal = alive.right = null;
                dead = alive;
                alive = alive.pred;
            }

            // alive is the branching node, dead is its now dead child
            if (alive.up == dead) {
                alive.up = null;
                List<Shortcut> extend;
                Shortcut with = alive.sc_up;
                if (alive.diagonal != null) {
                    extend = alive.getIncs(Incoming.DIAGUP);
                } else { // alive.right != null
                    extend = alive.getIncs(Incoming.RIGHT);
                }
                Iterator<Shortcut> it = extend.iterator();
                while (it.hasNext()) {
                    Shortcut sc = it.next();
                    if (sc.from.outdegree() > 1 || sc.from.onWorkingBoundary(next_i, next_j)) {
                        // extend
                        sc.to = with.to;
                        sc.inc = with.inc;
                        sc.max = Math.max(sc.max, with.max);
                        sc.to.getIncs(sc.inc).add(sc);
                    } else {
                        // remove
                        sc.from.sc_up = null;
                    }
                    it.remove();
                }
            } else if (alive.diagonal == dead) {
                alive.diagonal = null;
                if (alive.up != null && alive.right != null) {
                    // no extensions needed
                } else if (alive.up != null) {
                    // extend those from UP
                    List<Shortcut> extend = alive.getIncs(Incoming.UP);
                    Shortcut with = alive.sc_right;
                    Iterator<Shortcut> it = extend.iterator();
                    while (it.hasNext()) {
                        Shortcut sc = it.next();
                        if (sc.from.outdegree() > 1 || sc.from.onWorkingBoundary(next_i, next_j)) {
                            // extend
                            sc.to = with.to;
                            sc.inc = with.inc;
                            sc.max = Math.max(sc.max, with.max);
                            sc.to.getIncs(sc.inc).add(sc);
                        } else {
                            // remove
                            sc.from.sc_right = null;
                        }
                        it.remove();
                    }

                } else { // alive.right != null
                    // extend those from RIGHT
                    List<Shortcut> extend = alive.getIncs(Incoming.RIGHT);
                    Shortcut with = alive.sc_up;
                    Iterator<Shortcut> it = extend.iterator();
                    while (it.hasNext()) {
                        Shortcut sc = it.next();
                        if (sc.from.outdegree() > 1 || sc.from.onWorkingBoundary(next_i, next_j)) {
                            // extend
                            sc.to = with.to;
                            sc.inc = with.inc;
                            sc.max = Math.max(sc.max, with.max);
                            sc.to.getIncs(sc.inc).add(sc);
                        } else {
                            // remove
                            sc.from.sc_up = null;
                        }
                        it.remove();
                    }
                }
            } else if (alive.right == dead) {
                alive.right = null;

                List<Shortcut> extend;
                Shortcut with = alive.sc_right;
                if (alive.diagonal != null) {
                    extend = alive.getIncs(Incoming.DIAGRIGHT);
                } else { // alive.up != null
                    extend = alive.getIncs(Incoming.UP);
                }
                Iterator<Shortcut> it = extend.iterator();
                while (it.hasNext()) {
                    Shortcut sc = it.next();
                    if (sc.from.outdegree() > 1 || sc.from.onWorkingBoundary(next_i, next_j)) {
                        // extend
                        sc.to = with.to;
                        sc.inc = with.inc;
                        sc.max = Math.max(sc.max, with.max);
                        sc.to.getIncs(sc.inc).add(sc);
                    } else {
                        // remove
                        sc.from.sc_right = null;
                    }
                    it.remove();
                }
            }
        }

        if (next_j == I.M - 1) {
            next_i++;
            next_j = 1;
        } else {
            next_j++;
        }
    }

    private boolean better(SolutionNode A, SolutionNode B) {
        // check if A is strictly better than B (lower max to ECA)
        double maxA = 0;
        double maxB = 0;
        SolutionNode wA = A;
        SolutionNode wB = B;

        if (A.pred == B) {
            return false;
        }

        if (B.pred == A) {
            return B.node.value > 0;
        }

        if (B.pred == A.pred) {
            return A.node.value < B.node.value;
        }

        int steps = 0;
        while (wA != wB) {
            steps++;
            if (wA.node.i > wB.node.i || (wA.node.i == wB.node.i && wA.node.j > wB.node.j)) {
                // walk A down one shortcut (right shortcut)
                if (wA.sc_right == null) {
                    maxA = Math.max(maxA, Math.max(wA.node.value, wA.pred.isRoot() ? 0 : wA.pred.sc_right.max));
                    wA = wA.pred.isRoot() ? wA.pred : wA.pred.sc_right.to;
                } else {
                    maxA = Math.max(maxA, wA.sc_right.max);
                    wA = wA.sc_right.to;
                }
            } else {
                // walk B down one shortcut (up shortcut)
                if (wB.sc_up == null) {
                    maxB = Math.max(maxB, Math.max(wB.node.value, wB.pred.isRoot() ? 0 : wB.pred.sc_up.max));
                    wB = wB.pred.isRoot() ? wB.pred : wB.pred.sc_up.to;
                } else {
                    maxB = Math.max(maxB, wB.sc_up.max);
                    wB = wB.sc_up.to;
                }
            }
        }
        if (steps > 3) {
            System.err.println("Steps: " + steps);
        }

        return maxA < maxB - DoubleUtil.EPS;
    }

    public boolean done() {
        return next_i >= I.N;
    }

    public void step() {
        if (!done()) {
            expandTree(next_i, next_j);
        }
    }

    public void finish() {
        while (!done()) {
            expandTree(next_i, next_j);
        }
    }

    public SolutionNode[][] getNodes() {
        return nodes;
    }
}
