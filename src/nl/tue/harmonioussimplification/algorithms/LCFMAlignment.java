/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithms;

import nl.tue.harmonioussimplification.algorithms.lcfm.DiscreteMatching;
import nl.tue.harmonioussimplification.algorithms.lcfm.Instance;
import nl.tue.harmonioussimplification.algorithms.lcfm.InstanceNode;
import nl.tue.harmonioussimplification.algorithms.lcfm.SolutionTree;
import nl.tue.harmonioussimplification.algorithms.util.IsolineCarving;
import nl.tue.harmonioussimplification.data.input.InputCoordinate;
import nl.tue.harmonioussimplification.data.input.InputIsoline;
import nl.tue.harmonioussimplification.data.input.InputMap;
import nl.tue.harmonioussimplification.data.input.MatchInterval;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

public class LCFMAlignment {

    private boolean useGeodesicDistance = false;
    private boolean testReverse = false;
    private boolean enableFilters = false;
    private double maxFilter = 1;
    private double absFilter = 1;
    private double maxDetour = 1;

    public boolean isTestReverse() {
        return testReverse;
    }

    public void setTestReverse(boolean testReverse) {
        this.testReverse = testReverse;
    }

    public boolean isEnableFilters() {
        return enableFilters;
    }

    public void setEnableFilters(boolean enableFilters) {
        this.enableFilters = enableFilters;
    }

    public boolean isUseGeodesicDistance() {
        return useGeodesicDistance;
    }

    public void setUseGeodesicDistance(boolean useGeodesicDistance) {
        this.useGeodesicDistance = useGeodesicDistance;
    }

    public double getMaxFilter() {
        return maxFilter;
    }

    public void setMaxFilter(double maxFilter) {
        this.maxFilter = maxFilter;
    }

    public double getAbsFilter() {
        return absFilter;
    }

    public void setAbsFilter(double absFilter) {
        this.absFilter = absFilter;
    }

    public double getMaxDetour() {
        return maxDetour;
    }

    public void setMaxDetour(double maxDetour) {
        this.maxDetour = maxDetour;
    }

    public void addConfigurationControls(SideTab tab) {
        tab.addCheckbox("Use geodesic distance", useGeodesicDistance, (e, v) -> useGeodesicDistance = v);
        tab.addCheckbox("Test reverse", testReverse, (e, v) -> testReverse = v);

        tab.addCheckbox("Enable filters", enableFilters, (e, v) -> enableFilters = v);

        tab.makeSplit(2, 2);
        tab.addLabel("Max filter");
        tab.addDoubleSpinner(maxFilter, 0, 1, 0.05, (e, v) -> maxFilter = v);

        tab.makeSplit(2, 2);
        tab.addLabel("Absolute filter");
        tab.addDoubleSpinner(absFilter, 0, Double.MAX_VALUE, 0.05, (e, v) -> absFilter = v);

        tab.makeSplit(2, 2);
        tab.addLabel("Max detour filter");
        tab.addDoubleSpinner(maxDetour, 1, Double.MAX_VALUE, 0.05, (e, v) -> maxDetour = v);
    }

    public void run(InputMap map) {
        clearMatchings(map);

        double absThreshold = absFilter * map.distanceNorm();

        for (int i = 1; i < map.size(); i++) {
            InputIsoline lower_iso = map.get(i - 1);
            InputIsoline upper_iso = map.get(i);
            match(lower_iso, upper_iso, absThreshold);
        }
    }

    private void clearMatchings(InputMap map) {
        for (InputIsoline iso : map) {
            for (InputCoordinate coord : iso) {
                coord.getMatching().clear();
            }
        }
    }

    private void match(InputIsoline lower_iso, InputIsoline upper_iso, double absThreshold) {

        IsolineCarving<InputIsoline, InputCoordinate> carve = new IsolineCarving(lower_iso, upper_iso);

        DiscreteMatching DM = computeMatching(carve.pA, carve.pB);
        double max_dist = 0;
        for (InstanceNode n : DM.getMatching()) {
            if (n.getValue() > max_dist) {
                max_dist = n.getValue();
            }
        }

        double max_dist_rev;
        DiscreteMatching DM_rev;
        if (testReverse) {
            carve.pB.reverse();
            DM_rev = computeMatching(carve.pA, carve.pB);
            max_dist_rev = 0;
            for (InstanceNode n : DM_rev.getMatching()) {
                if (n.getValue() > max_dist_rev) {
                    max_dist_rev = n.getValue();
                }
            }
        } else {
            max_dist_rev = Double.POSITIVE_INFINITY;
            DM_rev = null;
        }

        if (max_dist < max_dist_rev) {
            assignMatching(DM, carve.startA, carve.startB, false, maxFilter * max_dist, absThreshold);
        } else {
            assignMatching(DM_rev, carve.startA, carve.endB, true, maxFilter * max_dist_rev, absThreshold);
        }
    }

    private class WalkData {

        boolean reversed;
        InputCoordinate start;
        InputCoordinate coord;
        MatchInterval init, current;
        int prev;

        private WalkData(InputCoordinate start, boolean reversed) {
            this.start = coord = start;
            coord = start;
            init = current = null;
            prev = -1;
            this.reversed = reversed;
        }

        private boolean shiftVertex(int i) {
            if (prev == i) {
                return false;
            } else if (prev < 0) {
                // we already have the first vertex, this is just for the first matching
                prev = i;
                return true;
            } else {
                coord = reversed ? coord.getCyclicPrevious() : coord.getCyclicNext();
                prev = i;
                return true;
            }
        }

        private void noMatch() {
            current = null;
        }

        private void updateCurrent(WalkData other, boolean this_changed) {

            if (this_changed || current == null) {
                // we should make a new interval!
                current = new MatchInterval();
                coord.getMatching().add(current);
                current.setFirst(other.coord);
                current.setLast(other.coord);
                if (prev == 0 && init == null) {
                    init = current;
                }
            } else {
                // just extend it!
                if (other.reversed) {
                    // the other is reversed, so first/last are backwards
                    current.setFirst(other.coord);
                } else {
                    // the other goes forward, so first/last are in walking direction
                    current.setLast(other.coord);
                }
            }
        }

        private void mergeEnds() {
            if (!start.getIsoline().isCyclic()) {
                return;
            }
            if (current == null || init == null) {
                return;
            }

            if (current.getLast() == init.getFirst()) {
                init.setFirst(current.getFirst());
                start.getMatching().remove(current);
            }
        }

    }

    private void assignMatching(DiscreteMatching DM, InputCoordinate lower_start, InputCoordinate upper_start, boolean reverse_upper, double relativeThreshold, double absThreshold) {

        WalkData lower = new WalkData(lower_start, false);
        WalkData upper = new WalkData(upper_start, reverse_upper);

        for (InstanceNode n : DM.getMatching()) {

            // update to next vertex, if necessary
            boolean lower_changed = lower.shiftVertex(n.getI());
            boolean upper_changed = upper.shiftVertex(n.getJ());

            if (enableFilters && (n.getValue() > relativeThreshold
                    || n.getValue() > absThreshold
                    || n.getValue() > maxDetour * lower.coord.getLocation().distanceTo(upper.coord.getLocation()))) {
                // dont match these!
                lower.noMatch();
                upper.noMatch();
            } else {
                // update the matchintervals
                lower.updateCurrent(upper, lower_changed);
                upper.updateCurrent(lower, upper_changed);
            }
        }

        // see if the first and last are matching
        lower.mergeEnds();
        upper.mergeEnds();
    }

    private DiscreteMatching computeMatching(PolyLine lower_line, PolyLine upper_line) {
        Instance I = new Instance(lower_line, upper_line);
        if (useGeodesicDistance) {
            I.computeGeodesicDistances(lower_line, upper_line);
        } else {
            I.computeEuclideanDistances(lower_line, upper_line);
        }
        SolutionTree T = new SolutionTree(I);
        T.finish();
        DiscreteMatching DM = new DiscreteMatching(I, T);
        return DM;
    }
}
