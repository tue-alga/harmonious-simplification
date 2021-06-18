/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.collapse;

import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlodeLadderUtil;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.Node;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlopeLadder;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.geometrycore.util.Pair;

/**
 * area preserving method, where the matching bisectors are used to create two
 * candidates and these are combined into a new location. This is the weighted
 * average based on the 1/distances towards the candidates. Cost is determined
 * based on the two areas spanned by the edge and its previous/next vertex.
 */
public abstract class CombinedCollapse extends CollapseMethod {

    protected abstract double computeSingleScore(Node[] nodes, Vector sol);

    private final boolean LIMIT = true;

    @Override
    public boolean compute(SlopeLadder ladder) {
        //TODO: determine which part of the ladder take part
        int startCollapse = 0;
        int endCollapse = ladder.size() - 1;

        while (startCollapse < ladder.size() && ladder.getNeighbors(startCollapse) == null) {
            startCollapse++;
        }
        while (endCollapse >= 0 && ladder.getNeighbors(endCollapse) == null) {
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

        Line perp = new Line(new Vector(sampleLine.getThrough()), new Vector(sampleLine.getDirection()));
        perp.getDirection().rotate90DegreesCounterclockwise();
        ladder.perpendicular = perp.clone();

//        	2. find first and last point along perp of perp
        Pair<Vector, Vector> firstLast = findFirstLast(ladder, sampleLine);
        if (firstLast == null) {
            return false;
        }
        Vector first = firstLast.getFirst();
        Vector last = firstLast.getSecond();
        ladder.sampleRegion = new LineSegment(first, last);
//        	3. Sample along perpendicular of perpendicular along area-preserving lines.
//        	4. keep track of the cheapest (min)
        int NO_SAMPLES = 500;
        Vector dir = Vector.subtract(last, first);
        double bestScore = Double.POSITIVE_INFINITY;
        Vector bestSample = null;
        for (int i = 0; i < NO_SAMPLES; i++) {
            Vector samplePoint = Vector.add(first, new Vector(dir.getX() * i / NO_SAMPLES, dir.getY() * i / NO_SAMPLES));
            perp.setThrough(samplePoint);

            double totalScore = 0;
            for (int j = startCollapse; j <= endCollapse; j++) {
                Node[] nodes = ladder.getNeighbors(j);
                if (nodes != null) {
                    Line areaPres = SlodeLadderUtil.getAreaPreservationLine(nodes);
                    Vector sol = computeSolPlacement(areaPres, perp, nodes);
                    double scoreSingle = computeSingleScore(nodes, sol);
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
        perp.setThrough(bestSample);
        for (int i = startCollapse; i <= endCollapse; i++) {
            Node[] nodes = ladder.getNeighbors(i);
            if (nodes != null) {
                Line areaPres = SlodeLadderUtil.getAreaPreservationLine(nodes);

                Vector sol = computeSolPlacement(areaPres, perp, nodes);
                nodes[1].ladderloc = sol;
            }
        }
        return true;
    }

    private Vector computeSolPlacement(Line areaPres, Line perp, Node[] nodes) {
        Vector sol = (Vector) perp.intersect(areaPres).get(0);
        nodes[1].ladderlocTrimmed = sol.clone();
        if (LIMIT) {
            sol = limitToSlab(nodes, areaPres, sol);
        }
        return sol;
    }

    private Vector limitToSlab(Node[] nodes, Line areaPresLine, Vector v) {
        Pair<Vector, Vector> firstLast = findFirstLast(nodes, areaPresLine);
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
            sampleLine = SlodeLadderUtil.getAreaPreservationLine(ladder.getNeighbors(startCollapse));
        } else {
            sampleLine = computeSampleLineMultiple(ladder, startCollapse, endCollapse);
        }
        return sampleLine;
    }

    private Line computeSampleLineMultiple(SlopeLadder ladder, int startCollapse, int endCollapse) {
        Line sampleLine = null;

        Node[] insideNodes = ladder.getNeighbors(startCollapse);
        Node[] outsideNodes = ladder.getNeighbors(endCollapse);

        Vector start = halfwayPoint(insideNodes);
        Vector end = halfwayPoint(outsideNodes);

        Line perp = Line.byThroughpoints(new Vector(start), new Vector(end));
        Vector delta = Vector.subtract(end, start);
        delta.scale(0.5);
        sampleLine = new Line(new Vector(perp.getThrough()), new Vector(perp.getDirection()));
        sampleLine.getDirection().rotate90DegreesCounterclockwise();
        sampleLine.translate(delta);

        return sampleLine;
    }

    private Vector halfwayPoint(Node[] nodes) {
        Vector halfway = Vector.add(nodes[1].loc, nodes[2].loc);
        halfway.scale(0.5);
        return halfway;
    }

    private Pair<Vector, Vector> findFirstLast(SlopeLadder ladder, Line sampleLine) {
        Vector first = null, last = null;
        for (int i = 0; i < ladder.size(); i++) {
            if (ladder.getNeighbors(i) == null) {
                continue;
            }
            Pair<Vector, Vector> firstLastLayer = findFirstLast(ladder.getNeighbors(i), sampleLine);

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

    private Pair<Vector, Vector> findFirstLast(Node[] nodes, Line sampleLine) {
        if (nodes == null) {
            return null;
        }

        Vector first = null, last = null;

        Line areaPres = SlodeLadderUtil.getAreaPreservationLine(nodes);
        for (int j = 0; j < nodes.length; j++) {
            Node node = nodes[j];
            Vector areaProj = areaPres.closestPoint(node.loc);
            Vector nodeProj = sampleLine.closestPoint(areaProj);
            if (first == null || pointOrderedOnLine(sampleLine, nodeProj, first)) {
                first = nodeProj;
            }
            if (last == null || pointOrderedOnLine(sampleLine, last, nodeProj)) {
                last = nodeProj;
            }
        }
        return new Pair<Vector, Vector>(first, last);
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
