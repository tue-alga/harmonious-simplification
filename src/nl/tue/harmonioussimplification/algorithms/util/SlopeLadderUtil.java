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

import nl.tue.harmonioussimplification.data.output.SlopeLadder;
import nl.tue.harmonioussimplification.data.input.InputCoordinate;
import nl.tue.harmonioussimplification.data.output.OutputCoordinate;
import nl.tue.harmonioussimplification.data.output.OutputIsoline;
import nl.tue.harmonioussimplification.data.output.OutputMap;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;

public class SlopeLadderUtil {

    public static PolyLine oldGeometry(OutputCoordinate coord) {
        PolyLine pl = new PolyLine();
        for (OutputCoordinate cc : coord.getExtendedCoordinates()) {
            if (cc != null) {
                pl.addVertex(cc.getLocation());
            }
        }
        return pl;
    }

    public static PolyLine newGeometry(OutputCoordinate coord) {
        return newGeometry(coord, coord.getCollapseLocation());
    }

    public static PolyLine newGeometry(OutputCoordinate coord, Vector location) {
        return new PolyLine(coord.getExtendedStart().getLocation(), location, coord.getExtendedEnd().getLocation());
    }

    public static PolyLine representsGeometry(OutputCoordinate coord, boolean extend) {

        PolyLine pl = new PolyLine();

        InputCoordinate fwd_walk_sentinel;
        if (extend) {
            InputCoordinate back_walk_sentinel = coord.getExtendedEnd().getRepresentsFrom();
            InputCoordinate back_walk = coord.getExtendedStart().getRepresentsTo();
            InputCoordinate back_walk_prev = back_walk.getCyclicPrevious();
            Vector refloc = coord.getExtendedStart().getLocation();
            while (back_walk_prev != null && back_walk_prev != back_walk_sentinel
                    && back_walk.getLocation().distanceTo(refloc)
                    > back_walk_prev.getLocation().distanceTo(refloc)) {
                pl.addVertex(back_walk_prev.getLocation());
                back_walk = back_walk_prev;
                back_walk_prev = back_walk.getCyclicPrevious();
            }
            fwd_walk_sentinel = back_walk;
            pl.reverse();
        } else {
            fwd_walk_sentinel = coord.getExtendedStart().getRepresentsTo();
        }
        InputCoordinate orig_walk = coord.getExtendedStart().getRepresentsTo();
        pl.addVertex(orig_walk.getLocation());
        while (orig_walk != coord.getExtendedEnd().getRepresentsFrom()) {
            orig_walk = orig_walk.getCyclicNext();
            pl.addVertex(orig_walk.getLocation());
        }
        if (extend) {
            InputCoordinate fwd_walk = coord.getExtendedEnd().getRepresentsTo();
            InputCoordinate fwd_walk_next = fwd_walk.getCyclicNext();
            Vector refloc = coord.getExtendedEnd().getLocation();
            while (fwd_walk_next != null && fwd_walk_next != fwd_walk_sentinel
                    && fwd_walk.getLocation().distanceTo(refloc)
                    > fwd_walk_next.getLocation().distanceTo(refloc)) {
                pl.addVertex(fwd_walk_next.getLocation());
                fwd_walk = fwd_walk_next;
                fwd_walk_next = fwd_walk.getCyclicNext();
            }
        }
        return pl;
    }

    public static Line getAreaPreservationLine(OutputCoordinate coord) {
        Vector a = coord.getExtendedStart().getLocation();
        Vector b = coord.getLocation();
        Vector c = coord.getCyclicNext().getLocation();
        Vector d = coord.getExtendedEnd().getLocation();

        Polygon pl = new Polygon(a, d, c, b);
        double area = pl.areaSigned();

        double base = a.distanceTo(d);
        double height = 2 * area / base; // area = 0.5 * base * height;
        Line L = Line.byThroughpoints(a.clone(), d.clone());
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

    public static boolean doesNotCauseIntersections(OutputMap map, SlopeLadder ladder) {
        // just brute force it for the time being... there is room to be more clever about which isolines to test and such, using counters to keep track

        // check intersections between replacements
        for (OutputCoordinate coord : ladder) {
            if (!coord.isCollapsable()) {
                //in other words if we tried to collapse this section in the first place.
                //otherwise ignore as it was there already anyway.
                continue;
            }

            PolyLine pl_n = newGeometry(coord);

            // check with existing segments
            for (OutputIsoline iso : map) {
                for (OutputCoordinate nn : iso) {
                    if (nn.getCyclicNext() == null) {
                        // end of the line
                        continue;
                    }
                    if (ladder.contains(nn.getCyclicNext()) || ladder.contains(nn) || ladder.contains(nn.getCyclicPrevious())) {
                        // its being replaced with this ladder
                        continue;
                    }

                    LineSegment ls = new LineSegment(nn.getLocation(), nn.getCyclicNext().getLocation());

                    if (nn.getCyclicNext() == coord.getCyclicPrevious()) {
                        // dont check the common endpoint
                        if (!ls.intersect(pl_n.edge(1)).isEmpty()) {
                            return false;
                        }
                    } else if (nn == coord.getCyclicNext().getCyclicNext()) {
                        // dont check the common endpoint
                        if (!ls.intersect(pl_n.edge(0)).isEmpty()) {
                            return false;
                        }
                    } else {
                        if (!ls.intersect(pl_n).isEmpty()) {
                            return false;
                        }
                    }
                }
            }

            // check with other ladders
            for (OutputCoordinate other_coord : ladder) {
                if (other_coord == coord) {
                    continue;
                }
                if (other_coord.isCollapsable()) {
                    //in other words if we tried to collapse this section in the first place.
                    //otherwise ignore as it was there already anyway.

                    PolyLine pl_nn = newGeometry(other_coord);
                    if (!pl_n.intersect(pl_nn).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
