/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithms.util;

import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.geometrycore.util.Pair;

public class HausdorffDistance {

    public static double computeDirectedHausdorffDistance(PolyLine polyA, PolyLine polyB) {
        Pair<Vector, Vector> pair = computeDirectedHausdorffPair(polyA, polyB);
        return pair.getFirst().distanceTo(pair.getSecond());
    }

    private static void updatePair(Pair<Vector, Vector> best, Vector pA, BaseGeometry defB, PolyLine polyB) {

        Vector b = defB.closestPoint(pA);
        double dist = b.distanceTo(pA);
        if (polyB != null) {
            for (LineSegment ls : polyB.edges()) {
                if (ls.distanceTo(pA) < dist - DoubleUtil.EPS) {
                    // nothing to do here
                    return;
                }
            }
        }
        if (best.getFirst() == null || best.getFirst().distanceTo(best.getSecond()) < dist) {
            best.setFirst(pA);
            best.setSecond(b);
        }
    }

    public static Pair<Vector, Vector> computeDirectedHausdorffPair(PolyLine polyA, PolyLine polyB) {
        // potential furthest sites are:
        //  - any of the vertices of polyA to their closest point on B
        //  - intersection of (bisector of two vertices of polyB) with polyA to their closest point on polyB
        //  - intersection of (bisector of two edges of polyB) with polyA to their closest point on polyB

        Pair<Vector, Vector> result = new Pair(null, null);
        for (Vector v : polyA.vertices()) {
            updatePair(result, v, polyB.closestPoint(v), null);
        }

        for (int i = 0; i < polyB.vertexCount(); i++) {
            Vector vI = polyB.vertex(i);
            for (int j = i + 1; j < polyB.vertexCount(); j++) {
                Vector vJ = polyB.vertex(j);

                // bisector
                Line bisec = Line.bisector(vI, vJ);
                for (BaseGeometry bg : polyA.intersect(bisec)) {
                    switch (bg.getGeometryType()) {
                        case VECTOR:
                            updatePair(result, (Vector) bg, vI, polyB);
                            break;
                        case LINESEGMENT:
                            LineSegment ls = (LineSegment) bg;
                            updatePair(result, ls.getStart(), vI, polyB);
                            updatePair(result, ls.getEnd(), vI, polyB);
                            break;
                    }
                }
            }
        }

        for (int i = 0; i < polyB.edgeCount(); i++) {
            LineSegment lsI = polyB.edge(i);
            Line lI = new Line(lsI.getStart(), lsI.getDirection());
            for (int j = i + 1; j < polyB.edgeCount(); j++) {
                LineSegment lsJ = polyB.edge(j);
                Line lJ = new Line(lsJ.getStart(), lsJ.getDirection());

                // bisector
                Vector dir = Vector.add(lI.getDirection(), lJ.getDirection());
                Vector through;
                List<BaseGeometry> intersect = lI.intersect(lJ);
                if (intersect.isEmpty() || intersect.get(0).getGeometryType() == GeometryType.LINE) {
                    through = Vector.multiply(0.5, Vector.add(
                            lI.getThrough(), lJ.getThrough()
                    ));
                } else {
                    through = (Vector) intersect.get(0);
                }
                Line bisec = new Line(through, dir);
                for (BaseGeometry bg : polyA.intersect(bisec)) {
                    switch (bg.getGeometryType()) {
                        case VECTOR:
                            updatePair(result, (Vector) bg, lI, polyB);
                            break;
                        case LINESEGMENT:
                            LineSegment ls = (LineSegment) bg;
                            updatePair(result, ls.getStart(), lI, polyB);
                            updatePair(result, ls.getEnd(), lI, polyB);
                            break;
                    }
                }

                dir.rotate90DegreesClockwise();
                for (BaseGeometry bg : polyA.intersect(bisec)) {
                    switch (bg.getGeometryType()) {
                        case VECTOR:
                            updatePair(result, (Vector) bg, lI, polyB);
                            break;
                        case LINESEGMENT:
                            LineSegment ls = (LineSegment) bg;
                            updatePair(result, ls.getStart(), lI, polyB);
                            updatePair(result, ls.getEnd(), lI, polyB);
                            break;
                    }
                }
            }
        }
        return result;
    }
}
