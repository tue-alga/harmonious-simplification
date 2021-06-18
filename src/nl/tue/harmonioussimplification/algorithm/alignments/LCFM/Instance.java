/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.alignments.LCFM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.tue.geometrycore.algorithms.dsp.DijkstrasShortestPath;
import nl.tue.geometrycore.algorithms.hulls.ConvexHull;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;

public class Instance {

    protected int N, M;
    protected InstanceNode[][] values;

    public Instance(PolyLine C1, PolyLine C2) {
        N = C1.vertexCount();
        M = C2.vertexCount();
        values = new InstanceNode[N][M];
    }

    public void computeEuclideanDistances(PolyLine C1, PolyLine C2) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                values[i][j] = new InstanceNode(i, j, C1.vertex(i).distanceTo(C2.vertex(j)));
            }
        }
    }

    public void computeCrossingPenaltyDistances(PolyLine C1, PolyLine C2, double crossingPenalty) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                values[i][j] = new InstanceNode(i, j, computeCrossingDistance(C1, i, C2, j, crossingPenalty));
            }
        }
    }

    private static Graph computeVisibilityGraph(PolyLine C1, PolyLine C2) {
        Graph G = new Graph();

        int N = C1.vertexCount();
        int M = C2.vertexCount();

        for (int i = 0; i < N; i++) {
            G.addVertex(C1.vertex(i));
        }
        for (int j = 0; j < M; j++) {
            G.addVertex(C2.vertex(j));
        }

        boolean cyclemode = C1.firstVertex().distanceTo(C1.lastVertex()) < 0.1 * C1.perimeter();
        List<Polygon> Ps;
        if (cyclemode) {
            Ps = computeCycles(C1, C2);
        } else {
            Ps = computePockets(C1, C2);
        }

        for (int i = 0; i < N; i++) {
            Vertex u = G.getVertices().get(i);

            for (int ii = i + 1; ii < N; ii++) {
                Vertex v = G.getVertices().get(ii);

                LineSegment ls = new LineSegment(u, v);

                //System.out.println("C1 " + i + " " + ii);
                if (ii > i + 1 && !isInteriorEdge(ls, Ps, cyclemode)) {
                    continue;
                }
                G.addEdge(u, v, ls.clone());
            }
        }

        for (int j = 0; j < M; j++) {
            Vertex u = G.getVertices().get(N + j);

            for (int jj = j + 1; jj < M; jj++) {
                Vertex v = G.getVertices().get(N + jj);

                LineSegment ls = new LineSegment(u, v);
                //System.out.println("C2 " + j + " " + jj);
                if (jj > j + 1 && !isInteriorEdge(ls, Ps, cyclemode)) {
                    continue;
                }

                G.addEdge(u, v, ls.clone());
            }
        }

        for (int i = 0; i < N; i++) {
            Vertex u = G.getVertices().get(i);

            for (int j = 0; j < M; j++) {
                Vertex v = G.getVertices().get(N + j);

                LineSegment ls = new LineSegment(u, v);
                //System.out.println("C1 " + i + " C2 " + j);
                if (!isInteriorEdge(ls, Ps, cyclemode)) {
                    continue;
                }

                G.addEdge(u, v, ls.clone());
            }
        }
        return G;
    }

    public void computeGeodesicDistances(PolyLine C1, PolyLine C2) {

        Graph G = computeVisibilityGraph(C1, C2);

        DijkstrasShortestPath dsp = new DijkstrasShortestPath(G);

        for (int i = 0; i < N; i++) {
            Vertex u = G.getVertices().get(i);
            for (int j = 0; j < M; j++) {
                Vertex v = G.getVertices().get(N + j);
                values[i][j] = new InstanceNode(i, j, u.isNeighborOf(v) ? u.distanceTo(v) : dsp.computeShortestPathLength(u, v));
            }
        }
    }

    private static boolean isInteriorEdge(LineSegment ls, List<Polygon> Ps, boolean cyclemode) {
        int contained = 0;
        for (Polygon P : Ps) {
            List<BaseGeometry> ints = P.intersect(ls);
            for (BaseGeometry bg : ints) {
                if (bg.getGeometryType() == GeometryType.VECTOR) {
                    Vector v = (Vector) bg;
                    if (!v.isApproximately(ls.getStart()) && !v.isApproximately(ls.getEnd())) {
                        //System.out.println("     failed intersect 1");
                        return false;
                    }
                } else {
                    LineSegment ls2 = (LineSegment) bg;
                    if (ls2.getStart().isApproximately(ls.getStart()) && ls2.getEnd().isApproximately(ls.getEnd())) {
                        // OK
                    } else if (ls2.getEnd().isApproximately(ls.getStart()) && ls2.getStart().isApproximately(ls.getEnd())) {
                        // also OK
                    } else {
                        //System.out.println("     failed intersect 2");
                        return false;
                    }
                }
            }
            if (P.contains(ls.getPointAt(0.5))) {
                contained++;
            }
        }
        if (cyclemode) {
            return contained == 1;
        } else {
            return contained > 0;
        }
    }

    public int getM() {
        return M;
    }

    public int getN() {
        return N;
    }

    public double getValue(int i, int j) {
        return values[i][j].value;
    }

    public InstanceNode getNode(int i, int j) {
        return values[i][j];
    }

    private double computeCrossingDistance(PolyLine C1, int i, PolyLine C2, int j, double crossingPenalty) {
        LineSegment ls = new LineSegment(C1.vertex(i), C2.vertex(j));
        double E = ls.length();
        int num = 0;
        if (crossingPenalty > 0) {
            for (int ii = 0; ii < C1.edgeCount(); ii++) {
                if (ii == i || ii + 1 == i) {
                    continue;
                }
                num += C1.edge(ii).intersect(ls).size();
            }
            for (int jj = 0; jj < C2.edgeCount(); jj++) {
                if (jj == j || jj + 1 == j) {
                    continue;
                }
                num += C2.edge(jj).intersect(ls).size();
            }
        }
        return E * (1 + num * crossingPenalty);
    }

    private static List<Polygon> computeCycles(PolyLine C1, PolyLine C2) {
        List<Polygon> Ps = new ArrayList();
        Polygon P1 = new Polygon(C1.vertices());
        Polygon P2 = new Polygon(C2.vertices());
        Ps.add(P1);
        Ps.add(P2);

        LineSegment e1 = P1.edge(-1); // new edge
        for (int i = 1; i < P1.edgeCount() - 2; i++) {
            if (!e1.intersect(P1.edge(i)).isEmpty()) {
                System.out.println("   <------------- intersection in cyclemode");
            }
        }
        for (int i = 0; i < P2.edgeCount(); i++) {
            if (!e1.intersect(P2.edge(i)).isEmpty()) {
                System.out.println("   <------------- intersection in cyclemode");
            }
        }

        LineSegment e2 = P2.edge(-1); // new edge
        for (int i = 1; i < P2.edgeCount() - 2; i++) {
            if (!e2.intersect(P2.edge(i)).isEmpty()) {
                System.out.println("   <------------- intersection in cyclemode");
            }
        }
        for (int i = 0; i < P1.edgeCount(); i++) {
            if (!e2.intersect(P1.edge(i)).isEmpty()) {
                System.out.println("   <------------- intersection in cyclemode");
            }
        }

        return Ps;
    }

    private static List<Polygon> computePockets(PolyLine C1, PolyLine C2) {
        List<Polygon> pockets = new ArrayList();

        //System.out.println("Computing");
        //System.out.println("  " + C1);
        //System.out.println("  " + C2);
        int[] starts = findPockets(C1, C2, pockets);

        PolyLine C1rev = C1.clone();
        C1rev.reverse();
        PolyLine C2rev = C2.clone();
        C2rev.reverse();
        int[] ends = findPockets(C1rev, C2rev, pockets);
        ends[0] = C1.vertexCount() - 1 - ends[0];
        ends[1] = C2.vertexCount() - 1 - ends[1];

        //System.out.println("starts " + starts[0]);
        //System.out.println("       " + starts[1]);
        //System.out.println("ends " + ends[0]);
        //System.out.println("     " + ends[1]);
        Polygon P = new Polygon();
        pockets.add(P);

        for (int i = starts[0]; i <= ends[0]; i++) {
            P.addVertex(C1.vertex(i));
        }
        for (int i = ends[1]; i >= starts[1]; i--) {
            P.addVertex(C2.vertex(i));
        }

        //System.out.println("Pocket count: " + pockets.size());
        return pockets;
    }

    private static int[] findPockets(PolyLine C1, PolyLine C2, List<Polygon> pockets) {
        LineSegment cap = new LineSegment(C1.vertex(0), C2.vertex(0));

        //System.out.println("Finding...");
        //System.out.println("  " + C1);
        //System.out.println("  " + C2);
        int last_C1 = 0;
        for (int i = 1; i < C1.edgeCount(); i++) {
            LineSegment edge = C1.edge(i);
            if (!edge.intersect(cap).isEmpty()) {
                last_C1 = i;
            }
        }
        int last_C2 = 0;
        for (int i = 1; i < C2.edgeCount(); i++) {
            LineSegment edge = C2.edge(i);
            if (!edge.intersect(cap).isEmpty()) {
                last_C2 = i;
            }
        }

        //System.out.println("last_C1 " + last_C1);
        //System.out.println("last_C2 " + last_C2);
        Line line = Line.spannedBy(cap);
        boolean considerLeft;
        if (last_C1 == 0 && last_C2 == 0) {
            // this is just visible...
            return new int[]{0, 0};
        } else if (last_C1 == 0) {
            considerLeft = !line.isLeftOf(C2.vertex(last_C2 + 1));
        } else if (last_C2 == 0) {
            considerLeft = !line.isLeftOf(C1.vertex(last_C1 + 1));
        } else {
            considerLeft = !line.isLeftOf(C1.vertex(last_C1 + 1));
            if (line.isLeftOf(C1.vertex(last_C1 + 1)) != line.isLeftOf(C2.vertex(last_C2 + 1))) {
                System.out.println("   <--------- disagreement");
            }
        }

        List<Vec> vecs = new ArrayList();
        for (int i = 0; i <= last_C1; i++) {
            //System.out.println("  " + i);
            if (i == 0) {

            } else if (considerLeft) {
                if (!line.isLeftOf(C1.vertex(i))) {
                    continue;
                }
            } else {
                if (!line.isRightOf(C1.vertex(i))) {
                    continue;
                }
            }
            //System.out.println("  adding C1 " + i);
            vecs.add(new Vec(C1, i));
        }
        for (int i = 0; i <= last_C2; i++) {
            //System.out.println("  " + i);
            if (i == 0) {

            } else if (considerLeft) {
                if (!line.isLeftOf(C2.vertex(i))) {
                    continue;
                }
            } else {
                if (!line.isRightOf(C2.vertex(i))) {
                    continue;
                }
            }
            //System.out.println("  adding C2 " + i);
            vecs.add(new Vec(C2, i));
        }

        //System.out.println("vecs");
        for (Vec v : vecs) {
            //System.out.println("  " + v.poly + " " + v.index);
        }

        ConvexHull<Vec> ch_comp = new ConvexHull();
        List<Vec> ch = ch_comp.computeHull(vecs, ConvexHull.ListMaintenance.MAINTAIN_ORDER);

        int ch_from1 = 0;
        int ch_from2 = 0;
        for (int k = 0; k < ch.size(); k++) {
            Vec v = ch.get(k);
            if (v.poly == C1 && v.index == 0) {
                ch_from1 = k;
            }
            if (v.poly == C2 && v.index == 0) {
                ch_from2 = k;
            }
        }

        if (ch_from1 == ch.size() - 1 && ch_from2 == 0) {
            Collections.reverse(ch);
        } else if (ch_from1 == ch_from2 - 1) {
            Collections.reverse(ch);
        }
        while (ch.get(0).poly != C1 || ch.get(0).index != 0) {
            ch.add(ch.remove(0));
        }
        // start1 ...... start2

        //System.out.println("ch");
        for (Vec v : ch) {
            //System.out.println("  " + v.poly + " " + v.index);
        }

        int walk1 = 0;
        int ch1 = 0;
        while (ch.get(ch1 + 1).poly == C1) {
            ch1++;
            Polygon P = new Polygon();
            //System.out.println("C1 pocket");
            while (walk1 != ch.get(ch1).index) {
                //System.out.println("   " + walk1);
                P.addVertex(C1.vertex(walk1));
                walk1++;
            }
            //System.out.println("   " + walk1);
            P.addVertex(C1.vertex(walk1));

            if (P.vertexCount() > 2) {
                pockets.add(P);
            }
        }
        int walk2 = 0;
        int ch2 = ch.size() - 1;
        while (ch.get(ch2 - 1).poly == C2) {
            ch2--;
            Polygon P = new Polygon();
            //System.out.println("C2 pocket");
            while (walk2 != ch.get(ch2).index) {
                //System.out.println("   " + walk2);
                P.addVertex(C2.vertex(walk2));
                walk2++;
            }
            //System.out.println("   " + walk2);
            P.addVertex(C2.vertex(walk2));

            if (P.vertexCount() > 2) {
                pockets.add(P);
            }
        }

        return new int[]{walk1, walk2};
    }

    private static class Vec extends Vector {

        PolyLine poly;
        int index;

        public Vec(PolyLine poly, int index) {
            super(poly.vertex(index));
            this.poly = poly;
            this.index = index;
        }

    }

    private static class Graph extends SimpleGraph<LineSegment, Vertex, Edge> {

        @Override
        public Vertex createVertex(double x, double y) {
            return new Vertex(x, y);
        }

        @Override
        public Edge createEdge() {
            return new Edge();
        }

    }

    private static class Vertex extends SimpleVertex<LineSegment, Vertex, Edge> {

        public Vertex(double x, double y) {
            super(x, y);
        }

    }

    private static class Edge extends SimpleEdge<LineSegment, Vertex, Edge> {

    }
}
