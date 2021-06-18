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

import java.util.LinkedList;
import java.util.List;

public class SolutionNode {

    protected InstanceNode node;
    protected SolutionNode pred, up, diagonal, right;
    protected Shortcut sc_up, sc_right;
    protected List<Shortcut> sc_incs_up = new LinkedList<Shortcut>();
    protected List<Shortcut> sc_incs_right = new LinkedList<Shortcut>();
    protected List<Shortcut> sc_incs_diagup = new LinkedList<Shortcut>();
    protected List<Shortcut> sc_incs_diagright = new LinkedList<Shortcut>();

    protected List<Shortcut> getIncs(Incoming inc) {
        switch (inc) {
            case UP:
                return sc_incs_up;
            case RIGHT:
                return sc_incs_right;
            case DIAGUP:
                return sc_incs_diagup;
            case DIAGRIGHT:
                return sc_incs_diagright;
            default:
                return null;
        }
    }

    protected int outdegree() {
        int c = 0;
        if (up != null) {
            c++;
        }
        if (diagonal != null) {
            c++;
        }
        if (right != null) {
            c++;
        }
        return c;
    }

    protected boolean isRoot() {
        return node.i == 0 && node.j == 0;
    }

    protected boolean onWorkingBoundary(int next_i, int next_j) {
        // NB: next_i and next_j indicate the latest filled position
        if (node.j >= next_j) {
            return node.i >= next_i - 1;
        } else {
            return node.i >= next_i;
        }
    }

    public boolean isDead() {
        return !isRoot() && pred.up != this && pred.diagonal != this && pred.right != this;
    }

    public InstanceNode getNode() {
        return node;
    }

    public SolutionNode getPred() {
        return pred;
    }
}
