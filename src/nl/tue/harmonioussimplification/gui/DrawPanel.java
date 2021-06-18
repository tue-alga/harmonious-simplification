/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.gui;

import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import nl.tue.harmonioussimplification.algorithms.util.SlopeLadderUtil;
import nl.tue.harmonioussimplification.data.output.SlopeLadder;
import nl.tue.harmonioussimplification.data.AbstractCoordinate;
import nl.tue.harmonioussimplification.data.AbstractIsoline;
import nl.tue.harmonioussimplification.data.AbstractMap;
import nl.tue.harmonioussimplification.data.input.InputCoordinate;
import nl.tue.harmonioussimplification.data.input.InputIsoline;
import nl.tue.harmonioussimplification.data.input.InputMap;
import nl.tue.harmonioussimplification.data.input.MatchInterval;
import nl.tue.harmonioussimplification.data.output.OutputCoordinate;
import nl.tue.harmonioussimplification.data.output.OutputMap;
import nl.tue.harmonioussimplification.gui.Data.MapRendering;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;

public class DrawPanel extends GeometryPanel {

    private final Data data;

    public DrawPanel(Data data) {
        this.data = data;
    }

    @Override
    protected void drawScene() {
        setSizeMode(SizeMode.VIEW);

        if (data.input == null) {
            return;
        }

        drawMap(data.input, data.draw_input);

        setStroke(ExtendedColors.black, 1, Dashing.SOLID);
        setTextStyle(TextAnchor.TOP_LEFT, 20);
        draw(getBoundingRectangle().leftTop(), "IN: " + data.input.coordinateCount());

        if (data.output != null) {
            if (data.split) {
                Rectangle rect = data.input.getBoundingBox();
                double w = rect.width() * 1.05;
                pushMatrix(AffineTransform.getTranslateInstance(w, 0));
            }

            drawMap(data.output, data.draw_output);

            if (data.split) {
                popMatrix();
            }

            setStroke(ExtendedColors.black, 1, Dashing.SOLID);
            setTextStyle(TextAnchor.TOP_RIGHT, 20);
            draw(getBoundingRectangle().rightTop(), "OUT: " + data.output.coordinateCount());
        }
    }

    private void drawMap(AbstractMap<? extends AbstractIsoline, ? extends AbstractCoordinate> map, MapRendering style) {

        if (style.isolines) {
            setStroke(ExtendedColors.black, 1, Dashing.SOLID);
            draw(map);
        }

        if (style.alignment) {
            if (map instanceof OutputMap) {
                drawLadders((OutputMap) map);
            } else {
                drawMatching((InputMap) map);
            }
        }

        if (style.vertices) {
            setStroke(ExtendedColors.black, 1, Dashing.SOLID);
            setPointStyle(PointStyle.CIRCLE_WHITE, 3);
            for (AbstractIsoline iso : map) {
                for (Object coord : iso) {
                    draw((AbstractCoordinate) coord);
                }
            }
        }
    }

    private void drawMatching(InputMap map) {
        setStroke(ExtendedColors.darkOrange, 1, Dashing.SOLID);
        for (InputIsoline iso : map) {
            for (InputCoordinate coord : iso) {
                for (MatchInterval match : coord.getMatching()) {
                    for (InputCoordinate mcoord : match) {
                        draw(new LineSegment(coord.getLocation(), mcoord.getLocation()));
                    }
                }
            }
        }
    }

    private void drawLadders(OutputMap map) {
        setStroke(ExtendedColors.darkOrange, 3, Dashing.SOLID);
        SlopeLadder best = null;
        for (SlopeLadder ladder : map.getLadders()) {
            if (ladder.isContractible() && ladder.doesNotCauseInteractions() && (best == null || ladder.getCost() < best.getCost())) {
                best = ladder;
            }
            if (ladder.doesNotCauseInteractions()) {
                setStroke(ExtendedColors.darkOrange, 3, Dashing.SOLID);
            } else {
                setStroke(ExtendedColors.darkRed, 3, Dashing.SOLID);
            }
            PolyLine seqA = new PolyLine();
            PolyLine seqB = new PolyLine();
            for (OutputCoordinate coord : ladder) {
                LineSegment LS = new LineSegment(coord.getLocation(), coord.getCyclicNext().getLocation()).clone();
                Vector mid = LS.getPointAt(0.5);
                LS.scale(0.9, mid);
                seqA.addVertex(LS.getStart());
                seqB.addVertex(LS.getEnd());
            }
            if (seqA.vertexCount() > 1) {

                Vector dA0 = seqA.getStartTangent();
                dA0.scale(seqA.edge(0).length() * 0.1);
                seqA.vertex(0).translate(dA0);

                Vector daN = seqA.getEndTangent();
                daN.scale(-seqA.edge(seqA.edgeCount() - 1).length() * 0.1);
                seqA.vertex(seqA.edgeCount()).translate(daN);

                Vector dB0 = seqB.getStartTangent();
                dB0.scale(seqB.edge(0).length() * 0.1);
                seqB.vertex(0).translate(dB0);

                Vector dBN = seqB.getEndTangent();
                dBN.scale(-seqB.edge(seqB.edgeCount() - 1).length() * 0.1);
                seqB.vertex(seqB.edgeCount()).translate(dBN);

                draw(seqA, seqB);
                for (int i = 0; i < seqA.vertexCount(); i++) {
                    draw(new LineSegment(seqA.vertex(i), seqB.vertex(i)));
                }
            } else {
                draw(new LineSegment(seqA.vertex(0), seqB.vertex(0)));
            }
        }

        if (best != null) {
            setStroke(ExtendedColors.darkBlue, 3, Dashing.SOLID);
            setPointStyle(PointStyle.SQUARE_SOLID, 3);
            for (OutputCoordinate coord : best) {
                if (coord.isCollapsable()) {
                    draw(coord.getCollapseLocation());
                    draw(SlopeLadderUtil.newGeometry(coord));
                }

            }
        }
    }

    @Override
    public Rectangle getBoundingRectangle() {
        if (data.input == null) {
            return null;
        }
        Rectangle rect = data.input.getBoundingBox();
        if (data.split) {
            rect.scale(2.05, 1, rect.leftBottom());
        }
        return rect;
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {

    }

    @Override
    protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_V:
                data.pasteMap();
                break;
            case KeyEvent.VK_A:
                data.align();
                break;
            case KeyEvent.VK_I:
                data.initialize();
                break;
            case KeyEvent.VK_S:
                data.stepSimplify();
                break;
            case KeyEvent.VK_R:
                data.simplify();
                break;
        }
    }

}
