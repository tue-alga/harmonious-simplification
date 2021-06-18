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

import nl.tue.harmonioussimplification.data.AbstractCoordinate;
import nl.tue.harmonioussimplification.data.AbstractIsoline;
import nl.tue.geometrycore.geometry.linear.PolyLine;

public class IsolineCarving<TIso extends AbstractIsoline<TIso, TCoord>, TCoord extends AbstractCoordinate<TIso, TCoord>> {

    public TIso isoA, isoB;
    public PolyLine pA, pB;
    public TCoord startA, startB;
    public TCoord endA, endB;

    public IsolineCarving(TIso isoA, TIso isoB) {
        this.isoA = isoA;
        this.isoB = isoB;
        carve();
    }

    private void carve() {

        if (isoA.isCyclic() && isoB.isCyclic()) {
            // find closest point for a common cut

            startA = null;
            startB = null;
            double dist = Double.POSITIVE_INFINITY;
            for (TCoord lc : isoA) {
                for (TCoord uc : isoB) {
                    double d = lc.getLocation().distanceTo(uc.getLocation());
                    if (d < dist) {
                        dist = d;
                        startA = lc;
                        startB = uc;
                    }
                }
            }

            pA = carveCycle(startA);
            pB = carveCycle(startB);

            endA = startA;
            endB = startB;

        } else if (isoA.isCyclic()) {
            // find furthest point to cut
            pB = (PolyLine) isoB.toGeometry();
            startB = isoB.getFirst();
            endB = isoB.getLast();

            startA = endA = findCarve(isoA, pB);
            pA = carveCycle(startA);

        } else if (isoB.isCyclic()) {
            // find furthest point to cut
            pA = (PolyLine) isoA.toGeometry();
            startA = isoA.getFirst();
            endA = isoA.getLast();

            startB = endB = findCarve(isoB, pA);
            pB = carveCycle(findCarve(isoB, pA));
        } else {
            // no need to cut
            pA = (PolyLine) isoA.toGeometry();
            startA = isoA.getFirst();
            endA = isoA.getLast();

            pB = (PolyLine) isoB.toGeometry();
            startB = isoB.getFirst();
            endB = isoB.getLast();
        }
    }

    private TCoord findCarve(TIso iso, PolyLine line) {
        TCoord furthest_coord = null;
        double dist = -1;
        for (TCoord coord : iso) {
            double d = line.distanceTo(coord.getLocation());
            if (d > dist) {
                furthest_coord = coord;
                dist = d;
            }
        }
        return furthest_coord;
    }

    private PolyLine carveCycle(TCoord coord) {
        PolyLine poly = new PolyLine();
        TCoord walk = coord;
        poly.addVertex(coord.getLocation());
        // NB: adds first vertex also at the end, this is intentional!
        do {
            walk = walk.getCyclicNext();
            poly.addVertex(walk.getLocation());
        } while (walk != coord);
        return poly;
    }
}
