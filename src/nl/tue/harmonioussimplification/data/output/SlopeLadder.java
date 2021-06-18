/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.data.output;

import java.util.ArrayList;
import nl.tue.harmonioussimplification.algorithms.util.SlopeLadderUtil;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;

public class SlopeLadder extends ArrayList<OutputCoordinate> {

    // convention: first of the nodes is in, in the direction of the isoline
    private boolean dirty = true;
    private boolean contractible = false;
    private double cost = Double.NaN;
    private int intersectionCount = 0;
    private boolean selfIntersects = false;

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        dirty = true;
        cost = Double.NaN;
        contractible = false;
        intersectionCount = 0;
        selfIntersects = false;
    }

    public void setClean() {
        dirty = false;
    }

    public boolean isContractible() {
        return contractible;
    }

    public void setContractible(boolean contractible) {
        this.contractible = contractible;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean doesNotCauseInteractions() {
        return intersectionCount == 0 && !selfIntersects;
    }

    public void setIntersectionCount(int intersectionCount) {
        this.intersectionCount = intersectionCount;
    }

    private void check(OutputCoordinate other, int inc) {

        if (other.getCyclicNext() == null) {
            // does not define a line segment
            return;
        }

        // changes interaection counts based LS = [other,next]
        // make sure its not involved in this ladder (this is checked separately)
        for (OutputCoordinate coord : this) {
            if (other == coord || other == coord.getExtendedStart() || other == coord.getCyclicNext()) {
                // involved, we shouldnt count these
                return;
            }
        }

        // remove interaection counts for LS = [other,other.next]
        LineSegment LS = new LineSegment(other.getLocation(), other.getCyclicNext().getLocation());
        for (OutputCoordinate coord : this) {
            if (!coord.isCollapsable()) {
                // irrelevant
                continue;
            }
            // does LS share an endpoint with what we would be replacing here?
            // if so, ignore, otherwise check interactions
            OutputCoordinate start = coord.getExtendedStart();
            if (other != start && other.getCyclicNext() != start) {
                LineSegment LS2 = new LineSegment(start.getLocation(), coord.getCollapseLocation());
                if (!LS2.intersect(LS).isEmpty()) {
                    intersectionCount += inc;
                }
            }

            OutputCoordinate end = coord.getExtendedEnd();

            if (other != end && other.getCyclicNext() != end) {
                LineSegment LS2 = new LineSegment(end.getLocation(), coord.getCollapseLocation());
                if (!LS2.intersect(LS).isEmpty()) {
                    intersectionCount += inc;
                }
            }
        }
    }

    public void checkOut(OutputCoordinate old) {
        check(old, -1);
    }

    public void checkIn(OutputCoordinate newc) {
        check(newc, -1);
    }

    public void checkSelfIntersects() {
        selfIntersects = false;
        PolyLine prev = null;
        for (OutputCoordinate coord : this) {
            PolyLine next;
            if (coord.isCollapsable()) {
                next = SlopeLadderUtil.newGeometry(coord);
            } else {
                next = SlopeLadderUtil.oldGeometry(coord);
            }
            if (prev != null && !prev.intersect(next).isEmpty()) {
                selfIntersects = true;
                break;
            }
            prev = next;
        }
    }

}
