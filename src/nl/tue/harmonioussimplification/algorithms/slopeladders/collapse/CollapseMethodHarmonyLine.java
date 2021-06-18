/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithms.slopeladders.collapse;

import nl.tue.harmonioussimplification.algorithms.util.SlopeLadderUtil;
import nl.tue.harmonioussimplification.data.output.OutputCoordinate;
import nl.tue.harmonioussimplification.data.output.SlopeLadder;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.geometrycore.util.Pair;

public abstract class CollapseMethodHarmonyLine extends CollapseMethod {

    private final boolean LIMIT = true;
    private final int NO_SAMPLES = 500;

    protected abstract double computeSingleScore(OutputCoordinate coord, Vector sol);

    @Override
    public boolean compute(SlopeLadder ladder) {
        //TODO: determine which part of the ladder take part
        int startCollapse = 0;
        int endCollapse = ladder.size() - 1;

        while (startCollapse < ladder.size() && !ladder.get(startCollapse).isCollapsable()) {
            startCollapse++;
        }
        while (endCollapse >= 0 && !ladder.get(endCollapse).isCollapsable()) {
            endCollapse--;
        }
        if (startCollapse > endCollapse) {
            return false;
        }

//        	1. compute perpendicular by taking outsides
        Line sampleLine = computeSampleLine(ladder, startCollapse, endCollapse);
        if (sampleLine == null) {
            return false;
        }

        Line harmonyLine = sampleLine.clone();
        harmonyLine.getDirection().rotate90DegreesCounterclockwise();

//        	2. find first and last point along sampleLine
        Pair<Vector, Vector> firstLast = findFirstLast(ladder, sampleLine);
        if (firstLast == null) {
            return false;
        }
        Vector first = firstLast.getFirst();
        Vector last = firstLast.getSecond();

//        	3. Sample along perpendicular of perpendicular along area-preserving lines.
//        	4. keep track of the cheapest (min)
        Vector dir = Vector.subtract(last, first);
        double bestScore = Double.POSITIVE_INFINITY;
        Vector bestSample = null;
        for (int i = 0; i < NO_SAMPLES; i++) {
            Vector samplePoint = Vector.add(first, new Vector(dir.getX() * i / NO_SAMPLES, dir.getY() * i / NO_SAMPLES));
            harmonyLine.setThrough(samplePoint);

            double totalScore = 0;
            for (int j = startCollapse; j <= endCollapse; j++) {
                OutputCoordinate coord = ladder.get(j);
                if (coord.isCollapsable()) {
                    Line areaPres = SlopeLadderUtil.getAreaPreservationLine(coord);
                    Vector sol = computeSolPlacement(areaPres, harmonyLine, coord);
                    double scoreSingle = computeSingleScore(coord, sol);
                    totalScore = Math.max(totalScore, scoreSingle);
                }
            }

            if (totalScore < bestScore) {
                bestScore = totalScore;
                bestSample = samplePoint;
            }
        }
        if (bestSample == null) {
            return false;
        }
        harmonyLine.setThrough(bestSample);
        for (int i = startCollapse; i <= endCollapse; i++) {
            OutputCoordinate coord = ladder.get(i);
            if (coord.isCollapsable()) {
                Line areaPres = SlopeLadderUtil.getAreaPreservationLine(coord);

                Vector sol = computeSolPlacement(areaPres, harmonyLine, coord);
                coord.setCollapseLocation(sol);
            }
        }
        return true;
    }

    private Vector computeSolPlacement(Line areaPres, Line harmonyLine, OutputCoordinate coord) {
        Vector sol = (Vector) harmonyLine.intersect(areaPres).get(0);
        if (LIMIT) {
            sol = limitToSlab(coord, areaPres, sol);
        }
        return sol;
    }

    private Vector limitToSlab(OutputCoordinate coord, Line areaPresLine, Vector v) {
        Pair<Vector, Vector> firstLast = findFirstLast(coord, areaPresLine);
        Vector first = firstLast.getFirst();
        Vector last = firstLast.getSecond();
        if (pointOrderedOnLine(areaPresLine, v, first)) {
            return first;
        } else if (pointOrderedOnLine(areaPresLine, last, v)) {
            return last;
        } else {
            return v;
        }
    }

    private Line computeSampleLine(SlopeLadder ladder, int startCollapse, int endCollapse) {
        Line sampleLine;
        if (startCollapse == endCollapse) {
            sampleLine = SlopeLadderUtil.getAreaPreservationLine(ladder.get(startCollapse));
        } else {
            sampleLine = computeSampleLineMultiple(ladder, startCollapse, endCollapse);
        }
        return sampleLine;
    }

    private Line computeSampleLineMultiple(SlopeLadder ladder, int startCollapse, int endCollapse) {
        Line sampleLine = null;

        OutputCoordinate start_coord = ladder.get(startCollapse);
        OutputCoordinate end_coord = ladder.get(endCollapse);

        Vector start = halfwayPoint(start_coord);
        Vector end = halfwayPoint(end_coord);

        Line perp = Line.byThroughpoints(new Vector(start), new Vector(end));
        Vector delta = Vector.subtract(end, start);
        delta.scale(0.5);
        sampleLine = perp.clone();
        sampleLine.getDirection().rotate90DegreesCounterclockwise();
        sampleLine.translate(delta);

        return sampleLine;
    }

    private Vector halfwayPoint(OutputCoordinate coord) {
        Vector halfway = Vector.add(coord.getLocation(), coord.getCyclicNext().getLocation());
        halfway.scale(0.5);
        return halfway;
    }

    private Pair<Vector, Vector> findFirstLast(SlopeLadder ladder, Line sampleLine) {
        Vector first = null, last = null;
        for (int i = 0; i < ladder.size(); i++) {

            if (!ladder.get(i).isCollapsable()) {
                continue;
            }
            Pair<Vector, Vector> firstLastLayer = findFirstLast(ladder.get(i), sampleLine);

            Vector layerFirst = firstLastLayer.getFirst();
            Vector layerLast = firstLastLayer.getSecond();
            if (first == null || pointOrderedOnLine(sampleLine, layerFirst, first)) {
                first = layerFirst;
            }
            if (last == null || pointOrderedOnLine(sampleLine, last, layerLast)) {
                last = layerLast;
            }
        }
        return new Pair<Vector, Vector>(first, last);
    }

    private Pair<Vector, Vector> findFirstLast(OutputCoordinate coord, Line sampleLine) {
        if (!coord.isCollapsable()) {
            return null;
        }

        Vector first = null, last = null;

        Line areaPres = SlopeLadderUtil.getAreaPreservationLine(coord);
        for (OutputCoordinate cc : coord.getExtendedCoordinates()) {
            Vector areaProj = areaPres.closestPoint(cc.getLocation());
            Vector nodeProj = sampleLine.closestPoint(areaProj);
            if (first == null || pointOrderedOnLine(sampleLine, nodeProj, first)) {
                first = nodeProj;
            }
            if (last == null || pointOrderedOnLine(sampleLine, last, nodeProj)) {
                last = nodeProj;
            }
        }
        return new Pair<>(first, last);
    }

    private boolean pointOrderedOnLine(Line l, Vector a, Vector b) {
        if (l.distanceTo(a) > DoubleUtil.EPS || l.distanceTo(b) > DoubleUtil.EPS) {
            throw new Error("points are not on line. Point a: " + a + " point b: " + b + " Line: " + l);
        }
        Vector dirLine = l.getDirection();
        Vector dirPoints = Vector.subtract(b, a);
        return Vector.dotProduct(dirLine, dirPoints) > 0;
    }
}
