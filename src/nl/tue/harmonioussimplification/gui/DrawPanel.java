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

import nl.tue.harmonioussimplification.map.Isoline;
import nl.tue.harmonioussimplification.map.Neighbor;
import nl.tue.harmonioussimplification.algorithm.simplification.Simplification;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
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

    public DrawPanel() {
    }

    @Override
    protected void drawScene() {
        if (Data.map == null) {
            return;
        }
        setSizeMode(SizeMode.VIEW);

        setLayer("align");
        if (Data.showalign) {
            setStroke(ExtendedColors.darkOrange, 1, Dashing.SOLID);
            for (Isoline iso : Data.map.isolines) {
                for (Neighbor nbr : iso.neighbors) {
                    for (int i = 0; i < nbr.size(); i++) {
                        if (nbr.get(i) == null) {
                            break;
                        }

                        for (Integer j : nbr.get(i)) {
                            draw(new LineSegment(iso.input.vertex(i), nbr.to.input.vertex(j)));
                        }
                    }
                }
            }
        }

        setLayer("input");
        if (Data.showinput) {
            setStroke(ExtendedColors.black, 2, Dashing.SOLID);
            setPointStyle(PointStyle.CIRCLE_WHITE, 4);
            setTextStyle(TextAnchor.RIGHT, 20);
            for (Isoline iso : Data.map.isolines) {
                draw(iso.input);
                if (Data.showinputvertices) {
                    draw(iso.input.vertices());
                }
                draw(iso.input.vertex(0), iso.getName() + ":" + iso.input.edgeCount() + " ");
            }
        }

        setLayer("output");
        if (Data.showoutput) {
            setStroke(ExtendedColors.darkBlue, 2, Dashing.SOLID);
            setTextStyle(TextAnchor.LEFT, 20);
            setPointStyle(PointStyle.CIRCLE_SOLID, 4);
            for (Isoline iso : Data.map.isolines) {
                PolyLine poly = iso.outputPolyline();
                if (poly == null) {
                    continue;
                }
                draw(poly);
                if (Data.showoutputvertices) {
                    draw(poly.vertices());
                }
                draw(poly.vertex(poly.edgeCount()), " " + poly.edgeCount());
            }
        }
        setLayer("algorithm");
        if (Data.showalgorithm) {
            Data.selectedSimplifier.draw(this);
        }
        setLayer("aux");
        setStroke(ExtendedColors.darkGray, 1, Dashing.SOLID);
        setTextStyle(TextAnchor.TOP_LEFT, 20);
        Vector c = getBoundingRectangle().leftTop();
        if (c != null) {
            draw(new Circle(c, Simplification.eps), LineSegment.byStartAndOffset(c, Vector.right(Simplification.eps)));
            draw(c, Data.map.processing);
        }
    }

    @Override
    public Rectangle getBoundingRectangle() {
        if (Data.map == null) {
            return null;
        }
        return Data.map.getBoundingBox();
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (button == MouseEvent.BUTTON1) {
            if (shift) {
                Simplification.eps = getBoundingRectangle().leftTop().distanceTo(loc);
                Data.autorun(false, true);
            }
        }
    }

    @Override
    protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_R:
                Data.run(true, true);
                break;
            case KeyEvent.VK_V:
                Data.loadClipboard();
                break;
            case KeyEvent.VK_C:
                Data.copyToClipboard();
                break;
            case KeyEvent.VK_S:
                Data.save();
                break;
        }
    }

}
