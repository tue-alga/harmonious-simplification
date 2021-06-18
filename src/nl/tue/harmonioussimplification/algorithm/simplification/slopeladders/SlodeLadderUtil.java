/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification.slopeladders;

import java.util.LinkedList;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.geometrycore.util.Pair;

public class SlodeLadderUtil {

    public static double computeDirectedHausdorffDistance(PolyLine polyA, PolyLine polyB) {
        Pair<Vector, Vector> pair = computeDirectedHausdorffPair(polyA, polyB);
        return pair.getFirst().distanceTo(pair.getSecond());
    }

    private final static List<Vector> tested = null; // new ArrayList();

    private static void updatePair(Pair<Vector, Vector> best, Vector pA, BaseGeometry defB, PolyLine polyB) {
        if (tested != null) {
            tested.add(pA);
        }
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
        if (tested != null) {
            tested.clear();
        }
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

    private double computeSingleScore(Node[] nodes, Vector sol) {
        //compute naively by comparing to all other line segments.

        Vector a = nodes[0].loc;
        Vector b = nodes[1].loc;
        Vector c = nodes[2].loc;
        Vector d = nodes[3].loc;

        double score = 0;
        /* pontential furthest sites are:
        	/* vertex sol
        	 * intersection of bisectors of abcd with the new line segments
         */
        LinkedList<Vector> potentialSites = new LinkedList<Vector>();
        potentialSites.addLast(sol);
        LineSegment ab = new LineSegment(a, b);
        LineSegment bc = new LineSegment(b, c);
        LineSegment cd = new LineSegment(c, d);
        LineSegment asol = new LineSegment(a, sol);
        LineSegment sold = new LineSegment(sol, d);

        if (ab.length() > 0 && bc.length() > 0) {
            List<Vector> intersectABCBisector = computeBisectorIntersection(ab, bc, asol, sold);
            potentialSites.addAll(intersectABCBisector);
        }
        if (bc.length() > 0 && cd.length() > 0) {
            List<Vector> intersectBCDBisector = computeBisectorIntersection(bc, cd, asol, sold);
            potentialSites.addAll(intersectBCDBisector);
        }
        if (bc.length() == 0 && ab.length() > 0 && cd.length() > 0) {
            List<Vector> intersectABCDBisector = computeBisectorIntersection(ab, cd, asol, sold);
            potentialSites.addAll(intersectABCDBisector);
        }

        for (Vector pos : potentialSites) {
            double bestToPos = Double.POSITIVE_INFINITY;
            if (ab.length() > 0) {
                double distL1 = ab.distanceTo(pos);
                bestToPos = Math.min(bestToPos, distL1);
            }
            if (bc.length() > 0) {
                double distL2 = bc.distanceTo(pos);
                bestToPos = Math.min(bestToPos, distL2);
            }
            if (cd.length() > 0) {
                double distL3 = cd.distanceTo(pos);
                bestToPos = Math.min(bestToPos, distL3);
            }
            score = Math.max(score, bestToPos);
        }
        return score;
    }

    private List<Vector> computeBisectorIntersection(LineSegment s1, LineSegment s2, LineSegment sol1, LineSegment sol2) {
        LinkedList<Vector> potentialSites = new LinkedList<Vector>();
        Vector dir = Vector.add(s1.getDirection(), s2.getDirection());
        dir.rotate90DegreesCounterclockwise();
        Line bisector = new Line(s2.getStart(), dir);
        List<BaseGeometry> intersects = sol1.intersect(bisector);
        if (intersects != null) {
            for (BaseGeometry v : intersects) {
                if (v instanceof Vector) {
                    potentialSites.addLast((Vector) v);
                }
            }
        }
        intersects = sol2.intersect(bisector);
        if (intersects != null) {
            for (BaseGeometry v : intersects) {
                if (v instanceof Vector) {
                    potentialSites.addLast((Vector) v);
                }
            }
        }
        return potentialSites;
    }

    public static PolyLine oldGeometry(Node[] nodes) {
        return new PolyLine(nodes[0].loc, nodes[1].loc, nodes[2].loc, nodes[3].loc);
    }

    public static PolyLine newGeometry(Node[] nodes) {
        return newGeometry(nodes, nodes[1].ladderloc);
    }

    public static PolyLine newGeometry(Node[] nodes, Vector location) {
        return new PolyLine(nodes[0].loc, location, nodes[3].loc);
    }

    public static PolyLine representsGeometry(Node[] nodes, boolean extend) {
        PolyLine input = nodes[1].isoline.input;
        PolyLine pl = new PolyLine();

        if (extend) {
            int k = nodes[0].representsTo - 1;
            while (k >= 0 && input.vertex(k + 1).distanceTo(nodes[0].loc) > input.vertex(k).distanceTo(nodes[0].loc)) {
                pl.addVertex(0, input.vertex(k));
                k--;
            }
        }
        for (int i = nodes[0].representsTo; i <= nodes[3].representsFrom; i++) {
            pl.addVertex(input.vertex(i));
        }
        if (extend) {
            int k = nodes[3].representsFrom + 1;
            while (k < input.vertexCount() && input.vertex(k - 1).distanceTo(nodes[3].loc) > input.vertex(k).distanceTo(nodes[3].loc)) {
                pl.addVertex(input.vertex(k));
                k++;
            }
        }
        return pl;
    }

    public static Line getAreaPreservationLine(Node[] nodes) {
        Node a = nodes[0];
        Node b = nodes[1];
        Node c = nodes[2];
        Node d = nodes[3];

        Polygon pl = new Polygon(a.loc, d.loc, c.loc, b.loc);
        double area = pl.areaSigned();

        double base = a.loc.distanceTo(d.loc);
        double height = 2 * area / base; // area = 0.5 * base * height;
        Line L = Line.byThroughpoints(a.loc.clone(), d.loc.clone());
        Vector perp = L.perpendicular();
        perp.scale(height);
        L.translate(perp);

        return L;
    }

    public static Line getAreaPreservationLine(Vector... vs) {

        Polygon pl = new Polygon(vs);
        double area = -pl.areaSigned();

        double base = vs[0].distanceTo(vs[vs.length - 1]);
        double height = 2 * area / base; // area = 0.5 * base * height;
        Line L = Line.byThroughpoints(vs[0].clone(), vs[vs.length - 1].clone());
        Vector perp = L.perpendicular();
        perp.scale(height);
        L.translate(perp);

        return L;
    }
}
