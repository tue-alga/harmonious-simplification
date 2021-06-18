/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithms.slopeladders.score;

import java.util.List;
import nl.tue.harmonioussimplification.data.output.OutputCoordinate;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;

public class ScoreFunctionSymmetricDifference extends ScoreFunction {

    @Override
    public double computeSingle(OutputCoordinate coord) {

        double score = 0;

        Vector a = coord.getCyclicPrevious().getLocation();
        Vector b = coord.getLocation();
        Vector c = coord.getCyclicNext().getLocation();
        Vector d = coord.getCyclicNext().getCyclicNext().getLocation();

        Vector n = coord.getCollapseLocation();
        // nb: new loc is on the area-preserving line

        // a--b--c--d
        // to 
        // a--n--d
        // normal case                        
        Vector ab_nd = intersect(a, b, n, d);
        Vector bc_nd = intersect(b, c, n, d);
        Vector bc_an = intersect(b, c, a, n);
        Vector cd_an = intersect(c, d, a, n);

        // NB: let's assume a normal case (b and c on same side of line ad
        // or the edge bc intersects the segment ad
        // under these assumptions, there are at most two intersections  
        // TODO
        // if this is not the case, the regular cases are still fine, but three intersections may occur                
        // in such a case, this overestimates / doesnt quite compute the sym diff
        // however, this case MUST cause intersections or it is at the end of a polyline so have little impact
        if (ab_nd != null) {
            // first old edge intersects last new edge
            // an cannot intersect anything
            score += area(a, n, ab_nd);
            if (bc_nd != null) {
                score += area(ab_nd, b, bc_nd);
                score += area(bc_nd, c, d);
            } else {
                score += area(ab_nd, b, c, d);
            }
        } else if (cd_an != null) {
            // symmetric to previous
            score += area(cd_an, n, d);
            if (bc_an != null) {
                score += area(bc_an, c, cd_an);
                score += area(a, b, bc_an);
            } else {
                score += area(a, b, c, cd_an);
            }
        } else if (bc_an == null && bc_nd == null) {
            score += area(a, b, c, d, n);
        } else if (bc_an == null) {
            score += area(n, a, b, bc_nd);
            score += area(bc_nd, c, d);
        } else if (bc_nd == null) {
            score += area(a, b, bc_an);
            score += area(bc_an, c, d, n);
        } else {
            score += area(a, b, bc_an);
            score += area(bc_an, n, bc_nd);
            score += area(bc_nd, c, d);
        }

        return score;
    }

    private Vector intersect(LineSegment ls1, LineSegment ls2) {
        List<BaseGeometry> is = ls1.intersect(ls2);
        if (is.isEmpty()) {
            return null;
        } else if (is.get(0).getGeometryType() == GeometryType.VECTOR) {
            return (Vector) is.get(0);
        } else {
            return null;
        }
    }

    private Vector intersect(Vector A_a, Vector A_b, Vector B_a, Vector B_b) {
        return intersect(new LineSegment(A_a, A_b), new LineSegment(B_a, B_b));
    }

    private double area(Vector... vecs) {
        return new Polygon(vecs).areaUnsigned();
    }
}
