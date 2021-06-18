/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification;

import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlodeLadderUtil;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.Node;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.NodeList;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlopeLadder;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.collapse.CollapseMethod;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.collapse.CombinedOriginalCollapse;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.maintain.LCFMMaintainer;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.maintain.MapMaintainer;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.score.ScoreFunction;
import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.score.SymmetricDifference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.harmonioussimplification.map.Isoline;
import nl.tue.harmonioussimplification.map.Map;
import nl.tue.harmonioussimplification.map.Neighbor;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.harmonioussimplification.gui.Data;
import nl.tue.harmonioussimplification.gui.DrawPanel;

public class SlopeLadderSimplification extends Simplification {

    // gui options    
    public CollapseMethod[] methods = {new CombinedOriginalCollapse()};
    public ScoreFunction[] functions = {new SymmetricDifference()};
    public MapMaintainer[] maintainers = {new LCFMMaintainer()};
    // actual settings    
    public int until = 1000;
    public boolean checkIntersections = true;
    public boolean normalize = true;
    public CollapseMethod collapse = methods[0];
    public ScoreFunction score = functions[0];
    public MapMaintainer mapper = maintainers[0];
    public boolean singletonLadders = false;
    // cache
    Map map;
    public int k;
    public SlopeLadder best = null;
    // rendering settings
    int delay = 0;
    boolean showLadders = true;
    boolean showPinch = true;
    boolean showLadderResults = false;
    boolean showBestLadder = true;
    boolean showMapper = false;
    boolean showOther = false;
    boolean debug = false;

    @Override
    protected void process(Map map) {
        this.map = map;

        //System.out.println("Starting");
        long start = System.currentTimeMillis();
        init();

        long init = (System.currentTimeMillis() - start);
        //System.out.println("  initTime " + init + " ms");

        steps();

        long greedy = (System.currentTimeMillis() - start);
        //System.out.println("  greedTime " + greedy + " ms");
        // System.out.println("  totalTime " + (greedy + init) + " ms");
    }

    public void init() {
        map.ladders = new ArrayList();

        k = 0;
        for (Isoline iso : map.isolines) {
            iso.nodes = new NodeList();
            for (int i = 0; i < iso.input.vertexCount(); i++) {
                Node node = new Node();
                node.ladder = null;
                node.ladderloc = null;
                node.loc = iso.input.vertex(i);
                node.list = iso.nodes;
                node.representsFrom = i;
                node.representsTo = i;
                node.isoline = iso;
                iso.nodes.addLast(node);
            }
            k += iso.input.vertexCount();
        }

        for (Isoline iso : map.isolines) {
            for (Node node : iso.nodes) {
                if (node.ladder != null || node.getNext() == null) {
                    continue;
                }

                SlopeLadder ladder = node.ladder = new SlopeLadder();
                ladder.add(node);
                map.ladders.add(ladder);

                for (Neighbor nbr : iso.neighbors) {
                    // walk
                    addToLadder(ladder, node, nbr);
                    Collections.reverse(ladder);
                    Node t = ladder.startPinch;
                    ladder.startPinch = ladder.endPinch;
                    ladder.endPinch = t;
                }
            }
        }

        computeBest();
    }

    public void reinitialize() {
        if (map != null) {
            for (SlopeLadder ladder : map.ladders) {
                ladder.dirty = true;
            }
            computeBest();
        }
    }

    private void computeBest() {
        best = null;
        for (SlopeLadder ladder : map.ladders) {
            if (ladder.dirty) {
                ladder.contractible = collapse.compute(ladder);
                if (ladder.contractible && checkIntersections) {
                    ladder.contractible = doesNotCauseIntersections(ladder);
                }
                if (ladder.contractible && checkIntersections) {
                    ladder.cost = score.compute(ladder, normalize);
                }
                ladder.dirty = false;
            } else if (ladder.contractible && checkIntersections) {
                ladder.contractible = doesNotCauseIntersections(ladder);
            }

            if (ladder.contractible && (best == null || ladder.cost < best.cost)) {
                best = ladder;
            }
        }
    }

    private void addToLadder(SlopeLadder ladder, Node node, Neighbor nbr) {

        int i = nbr.from.input.vertices().indexOf(node.loc);

        int low = Collections.max(nbr.get(i));
        int high = Collections.min(nbr.get(i + 1));
        if (low == high) {
            // end of ladder
            ladder.endPinch = nbr.to.nodes.get(low);
            return;
        } else if (low != high - 1) {
            // error?
            System.out.println("  ??" + low + " " + high);
            return;
        } else if (singletonLadders) {
            ladder.endPinch = nbr.to.nodes.get(map.isolines.indexOf(nbr.from) < map.isolines.indexOf(nbr.to) ? low : high);
            return;
        }

        Node next = nbr.to.nodes.get(low); // TODO: this is slow
        next.ladder = ladder;
        ladder.add(next);

        for (Neighbor nextnbr : nbr.to.neighbors) {
            if (nextnbr.to != nbr.from) {
                addToLadder(ladder, next, nextnbr);
            }
        }

    }

    public void steps() {

        while (k > until && best != null) {
            step();
            if (delay > 0) {
                Data.draw.repaintNow();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SlopeLadderSimplification.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void step() {

        if (best == null) {
            return;
        }

        map.ladders.remove(best);
        int numbCollapses = 0;
        SlopeLadder bewladder = null;
        Node pinch = best.startPinch;
        for (int i = 0; i < best.size(); i++) {
            Node[] nodes = best.getNeighbors(i);
            if (nodes != null) { //iow we collapse this rung
                numbCollapses++;

                Node a = nodes[0];
                Node b = nodes[1];
                Node c = nodes[2];
                Node d = nodes[3];

                // we keep c and remove b
                // recompute the represented parts
                mapper.compute(nodes);

                if (a.getPrevious() != null && a.getPrevious().ladder != null) {
                    a.getPrevious().ladder.dirty = true;
                }
                a.ladder.dirty = true;
                c.ladder.dirty = true;
                if (d.ladder != null) {
                    d.ladder.dirty = true;
                }
                c.loc = b.ladderloc;
                b.list.remove(b);
                b.replacedWith = c; // for updating pinches

                if (bewladder != null) {
                    bewladder.endPinch = c;
                    bewladder = null;
                }
                pinch = c;
            } else {
                if (bewladder == null) {
                    bewladder = new SlopeLadder();
                    map.ladders.add(bewladder);
                    bewladder.startPinch = pinch;
                    bewladder.dirty = false;
                    bewladder.contractible = false;
                }
                bewladder.add(best.get(i));
            }
        }
        if (bewladder != null) {
            bewladder.endPinch = best.endPinch;
            bewladder = null;
        }

        // update pinches!
        for (SlopeLadder ladder : map.ladders) {
            if (ladder.startPinch != null && ladder.startPinch.replacedWith != null) {
                ladder.startPinch = ladder.startPinch.replacedWith;
            }
            if (ladder.endPinch != null && ladder.endPinch.replacedWith != null) {
                ladder.endPinch = ladder.endPinch.replacedWith;
            }
        }
        k -= numbCollapses;

        computeBest();
    }

    @Override
    public String whatHaveYouDone() {
        return "SlopeLadderSimplification[until=" + until + ";collapse=" + collapse + ";score=" + score + ";normalize=" + normalize + ";mapper=" + mapper + ";simple=" + checkIntersections + "]";
    }

    @Override
    public String toString() {
        return "SlopeLadderSimplification";
    }

    @Override
    public void createGUI(SideTab tab) {

        tab.addComboBox(methods, collapse, (e, v) -> {
            collapse = v;
            reinitialize();
            Data.draw.repaint();
        });

        tab.addComboBox(functions, score, (e, v) -> {
            score = v;
            reinitialize();
            Data.draw.repaint();
        });

        tab.addCheckbox("Normalize score", normalize, (e, v) -> {
            normalize = v;
            reinitialize();
            Data.draw.repaint();
        });

        tab.addComboBox(maintainers, mapper, (e, v) -> {
            mapper = v;
            //reinitialize();
            Data.draw.repaint();
        });

        tab.addCheckbox("Check intersections", checkIntersections, (e, v) -> {
            checkIntersections = v;
            init();
            Data.draw.repaint();
        });

        tab.addCheckbox("Singleton ladders", singletonLadders, (e, v) -> {
            singletonLadders = v;
            reinitialize();
            Data.draw.repaint();
        });

        tab.addCheckbox("DEBUG", debug, (e, v) -> {
            debug = v;
        });

        tab.addLabel("Until");
        tab.addIntegerSpinner(until, 0, Integer.MAX_VALUE, 5, (e, v) -> until = v);

        tab.addButton("Continue until", (e) -> {
            steps();
            Data.draw.repaint();
        });

        tab.addButton("Step", (e) -> {
            step();
            Data.draw.repaint();
        });

        tab.addLabel("Draw delay");
        tab.addIntegerSpinner(delay, 0, Integer.MAX_VALUE, 500, (e, v) -> delay = v);

        tab.addCheckbox("Draw ladders", showLadders, (e, v) -> {
            showLadders = v;
            Data.draw.repaint();
        });
        tab.addCheckbox("Draw ladder pinch", showPinch, (e, v) -> {
            showPinch = v;
            Data.draw.repaint();
        });
        tab.addCheckbox("Draw ladder results", showLadderResults, (e, v) -> {
            showLadderResults = v;
            Data.draw.repaint();
        });
        tab.addCheckbox("Draw best ladder", showBestLadder, (e, v) -> {
            showBestLadder = v;
            Data.draw.repaint();
        });
        tab.addCheckbox("Draw mapper", showMapper, (e, v) -> {
            showMapper = v;
            Data.draw.repaint();
        });
        tab.addCheckbox("Draw other collapse info", showOther, (e, v) -> {
            showOther = v;
            Data.draw.repaint();
        });

    }

    private boolean doesNotCauseIntersections(SlopeLadder ladder) {
        // just brute force it 

        // check intersections between replacements
        for (Node n : ladder) {
            if (n.getPrevious() == null || n.getNext() == null || n.getNext().getNext() == null) {
                //in other words if we tried to collapse this section in the first place.
                //otherwise ignore as it was there already anyway.

                continue;
            }

            PolyLine pl_n = new PolyLine(n.getPrevious().loc, n.ladderloc, n.getNext().getNext().loc);

            // check with existing segments
            for (Isoline iso : map.isolines) {
                for (Node nn : iso.nodes) {
                    if (nn.getNext() == null) {
                        // end of the line
                        continue;
                    }
                    if (ladder.contains(nn.getNext()) || ladder.contains(nn) || ladder.contains(nn.getPrevious())) {
                        // its being replaced with this ladder
                        continue;
                    }

                    LineSegment ls = new LineSegment(nn.loc, nn.getNext().loc);

                    if (nn.getNext() == n.getPrevious()) {
                        // dont check the common endpoint
                        if (!ls.intersect(pl_n.edge(1)).isEmpty()) {
                            return false;
                        }
                    } else if (nn == n.getNext().getNext()) {
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

            // check with other ladder
            for (Node nn : ladder) {
                if (nn == n) {
                    continue;
                }
                if (nn.getPrevious() != null && nn.getNext() != null && nn.getNext().getNext() != null) {
                    //in other words if we tried to collapse this section in the first place.
                    //otherwise ignore as it was there already anyway.

                    PolyLine pl_nn = new PolyLine(nn.getPrevious().loc, nn.ladderloc, nn.getNext().getNext().loc);
                    if (!pl_n.intersect(pl_nn).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void draw(DrawPanel draw) {
        super.draw(draw);

        if (map != null && map.ladders != null) {
            if (showMapper) {
                draw.setStroke(ExtendedColors.darkGreen, 1, Dashing.SOLID);
                for (Isoline iso : map.isolines) {
                    for (Node n : iso.nodes) {
                        for (int i = n.representsFrom; i <= n.representsTo; i++) {
                            draw.draw(new LineSegment(n.loc, iso.input.vertex(i)));
                        }
                    }
                }
            }

            if (showLadders) {
                draw.setPointStyle(PointStyle.SQUARE_SOLID, 4);
                for (SlopeLadder ladder : map.ladders) {
                    PolyLine plA = new PolyLine();
                    PolyLine plB = new PolyLine();
                    PolyLine plN = new PolyLine();
                    PolyLine plNN = new PolyLine();
                    if (showPinch && ladder.startPinch != null) {
                        plA.addVertex(ladder.startPinch.loc);
                        plB.addVertex(ladder.startPinch.loc);
                    }
                    for (Node n : ladder) {
                        plA.addVertex(n.loc);
                        plB.addVertex(n.getNext().loc);
                        if (n.ladderloc != null) {
                            plN.addVertex(n.ladderloc);
                            plNN.addVertex(Vector.multiply(0.5, Vector.add(n.loc, n.getNext().loc)));
                        }
                    }
                    if (showPinch && ladder.endPinch != null) {
                        plA.addVertex(ladder.endPinch.loc);
                        plB.addVertex(ladder.endPinch.loc);
                    }
                    draw.setStroke(ExtendedColors.darkOrange, 1, Dashing.SOLID);
                    draw.draw(plA, plB);

                    if (showLadderResults && plN.vertexCount() > 0) {
                        if (ladder.contractible) {
                            draw.setStroke(ExtendedColors.darkGreen, 1, Dashing.SOLID);
                        } else {
                            draw.setStroke(ExtendedColors.darkRed, 1, Dashing.SOLID);
                        }
                        draw.draw(plN);
                        draw.draw(plN.vertices());
                        draw.setForwardArrowStyle(ArrowStyle.LINEAR, 8);
                        for (int i = 0; i < plN.vertexCount(); i++) {
                            draw.draw(new LineSegment(plNN.vertex(i), plN.vertex(i)));
                        }
                        draw.setForwardArrowStyle(null, 4);
                    }
                }
            }

            if (best != null && showBestLadder) {
                draw.setStroke(ExtendedColors.darkPurple, 4, Dashing.SOLID);
                draw.setTextStyle(TextAnchor.LEFT, 20);
                draw.setPointStyle(PointStyle.SQUARE_SOLID, 6);
                for (Node n : best) {
                    draw.draw(new LineSegment(n.loc, n.getNext().loc));
                    draw.draw(n.ladderloc);
                }
            }
            if (best != null && showOther) {
                draw.setStroke(ExtendedColors.darkPurple, 4, Dashing.SOLID);
                if (best.perpendicular != null) {
                    draw.draw(best.perpendicular);
                }
                for (Node n : best) {
                    if (n.ladderlocTrimmed != null) {
                        draw.draw(n.ladderlocTrimmed);
                    }
                    draw.draw(SlodeLadderUtil.representsGeometry(best.getNeighbors(n), true));
                }
            }
        }
    }

}
