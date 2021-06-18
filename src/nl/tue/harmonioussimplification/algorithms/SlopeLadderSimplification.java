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

import java.util.Collections;
import java.util.HashMap;
import nl.tue.harmonioussimplification.algorithms.slopeladders.collapse.CollapseMethod;
import nl.tue.harmonioussimplification.algorithms.slopeladders.collapse.CollapseMethodHarmonyLineDirectedHausdorff;
import nl.tue.harmonioussimplification.algorithms.slopeladders.maintain.MapMaintainer;
import nl.tue.harmonioussimplification.algorithms.slopeladders.maintain.MapMaintainerLCFM;
import nl.tue.harmonioussimplification.algorithms.slopeladders.score.ScoreFunction;
import nl.tue.harmonioussimplification.algorithms.slopeladders.score.ScoreFunctionSymmetricDifference;
import nl.tue.harmonioussimplification.data.output.SlopeLadder;
import nl.tue.harmonioussimplification.data.input.InputCoordinate;
import nl.tue.harmonioussimplification.data.input.InputIsoline;
import nl.tue.harmonioussimplification.data.input.InputMap;
import nl.tue.harmonioussimplification.data.input.MatchInterval;
import nl.tue.harmonioussimplification.data.output.OutputCoordinate;
import nl.tue.harmonioussimplification.data.output.OutputIsoline;
import nl.tue.harmonioussimplification.data.output.OutputMap;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

public class SlopeLadderSimplification {

    //private boolean checkIntersections = true;
    private int target = 50;
    // fixed settings for now...
    private final boolean normalize = true;
    private final ScoreFunction score = new ScoreFunctionSymmetricDifference();
    private final CollapseMethod collapse = new CollapseMethodHarmonyLineDirectedHausdorff();
    private final MapMaintainer mapper = new MapMaintainerLCFM();

    public void addConfigurationControls(SideTab tab) {
        //tab.addCheckbox("Check intersections", checkIntersections, (e, v) -> checkIntersections = v);

        tab.makeSplit(2, 2);
        tab.addLabel("Target");
        tab.addIntegerSpinner(target, 0, Integer.MAX_VALUE, 1, (e, v) -> target = v);
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    // cache
    private OutputMap map;
    private int vertexCount;
    private SlopeLadder best;

    public OutputMap initialize(InputMap input) {
        clear();

        // clone the output map
        map = new OutputMap();

        HashMap<InputCoordinate, OutputCoordinate> fwdmap = new HashMap();

        for (InputIsoline iso : input) {

            OutputIsoline iso_out = new OutputIsoline(iso.isCyclic(), iso);
            map.add(iso_out);

            for (InputCoordinate coord : iso) {
                OutputCoordinate coord_out = new OutputCoordinate(coord.getLocation().clone(), iso_out, coord);
                iso_out.addLast(coord_out);
                fwdmap.put(coord, coord_out);
            }
        }

        for (OutputIsoline iso : map) {

            for (OutputCoordinate coord : iso) {
                if (coord.getLadder() != null || coord.getCyclicNext() == null) {
                    continue;
                }

                SlopeLadder ladder = new SlopeLadder();
                map.getLadders().add(ladder);

                ladder.add(coord);
                coord.setLadder(ladder);

                OutputCoordinate next = coord.getCyclicNext();

                for (MatchInterval mi : coord.getRepresentsFrom().getMatching()) {
                    for (MatchInterval next_mi : next.getRepresentsFrom().getMatching()) {
                        if (mi.getFirst().getCyclicPrevious() == next_mi.getLast()) {
                            // opposite direction between isolines
                            extendLadder(coord.getIsoline(), ladder, fwdmap.get(next_mi.getLast()), fwdmap.get(mi.getFirst()), fwdmap);
                            Collections.reverse(ladder);
                        } else if (mi.getLast().getCyclicNext() == next_mi.getFirst()) {
                            // same direction between isolines
                            extendLadder(coord.getIsoline(), ladder, fwdmap.get(mi.getLast()), fwdmap.get(next_mi.getFirst()), fwdmap);
                            Collections.reverse(ladder);
                        }
                    }
                }
            }
        }

        vertexCount = map.coordinateCount();

        computeBestSlopeLadder();

        return map;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    private void extendLadder(OutputIsoline prev_iso, SlopeLadder ladder, OutputCoordinate coord, OutputCoordinate next, HashMap<InputCoordinate, OutputCoordinate> fwdmap) {
        ladder.add(coord);
        coord.setLadder(ladder);

        for (MatchInterval mi : coord.getRepresentsFrom().getMatching()) {
            if (mi.getIsoline() == prev_iso.getRepresents()) {
                continue;
            }
            for (MatchInterval next_mi : next.getRepresentsFrom().getMatching()) {
                if (mi.getFirst().getCyclicPrevious() == next_mi.getLast()) {
                    // opposite direction between isolines
                    extendLadder(coord.getIsoline(), ladder, fwdmap.get(next_mi.getLast()), fwdmap.get(mi.getFirst()), fwdmap);
                    Collections.reverse(ladder);
                } else if (mi.getLast().getCyclicNext() == next_mi.getFirst()) {
                    // same direction between isolines
                    extendLadder(coord.getIsoline(), ladder, fwdmap.get(mi.getLast()), fwdmap.get(next_mi.getFirst()), fwdmap);
                    Collections.reverse(ladder);
                }
            }
        }
    }

    private void computeBestSlopeLadder() {
        best = null;
        for (SlopeLadder ladder : map.getLadders()) {
            if (ladder.isDirty()) {
                ladder.setContractible(collapse.compute(ladder));
                if (ladder.isContractible()) {
                    computeIntersections(ladder);

                    if (ladder.doesNotCauseInteractions()) {
                        ladder.setCost(score.compute(ladder, normalize));
                    }
                }
                ladder.setClean();
            }

            // test code for intersection checking
//            if (ladder.isContractible() && ladder.doesNotCauseInteractions() != SlopeLadderUtil.doesNotCauseIntersections(map, ladder)) {
//                System.err.println("DIFF?");
//            }
            if (ladder.isContractible() && ladder.doesNotCauseInteractions() && (best == null || ladder.getCost() < best.getCost())) {
                best = ladder;
            }
        }
    }

    private void computeIntersections(SlopeLadder ladder) {
        ladder.checkSelfIntersects();
        ladder.setIntersectionCount(0);
        for (OutputIsoline iso : map) {
            for (OutputCoordinate coord : iso) {
                ladder.checkIn(coord);
            }
        }
    }

    public boolean isInitialized() {
        return map != null;
    }

    public void clear() {
        map = null;
    }

    public void run() {
        if (map == null) {
            return;
        }

        while (vertexCount > target && best != null) {
            step();
        }
    }

    public void step() {

        if (best == null) {
            return;
        }

        map.getLadders().remove(best);
        int numCollapse = 0;
        SlopeLadder newLadder = null;
        for (OutputCoordinate coord : best) {
            if (coord.isCollapsable()) {
                numCollapse++;

                OutputCoordinate a = coord.getExtendedStart();
                OutputCoordinate b = coord;
                OutputCoordinate c = coord.getCyclicNext();
                OutputCoordinate d = coord.getExtendedEnd();

                // check out old three segments: a-b-c[-d]
                for (SlopeLadder oldladder : map.getLadders()) {
                    oldladder.checkOut(a);
                    oldladder.checkOut(b);
                    oldladder.checkOut(c);
                }

                // we keep c and remove b
                // recompute the represented parts
                mapper.compute(coord);

                if (a.getCyclicPrevious() != null && a.getCyclicPrevious().getLadder() != null) {
                    a.getCyclicPrevious().getLadder().setDirty();
                }
                a.getLadder().setDirty();
                c.getLadder().setDirty();
                if (d.getLadder() != null) {
                    d.getLadder().setDirty();
                }
                c.setLocation(b.getCollapseLocation());
                b.getIsoline().remove(b);

                // check in new three segments: a-c[-d]
                for (SlopeLadder oldladder : map.getLadders()) {
                    oldladder.checkIn(a);
                    oldladder.checkIn(c);
                }

            } else {
                if (newLadder == null) {
                    newLadder = new SlopeLadder();
                    map.getLadders().add(newLadder);
                    newLadder.setDirty();
                }
                newLadder.add(coord);
            }
        }

        vertexCount -= numCollapse;

        computeBestSlopeLadder();
    }

    public boolean canContinue() {
        return map != null && best != null;
    }
}
