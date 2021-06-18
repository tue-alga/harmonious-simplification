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

import nl.tue.harmonioussimplification.algorithms.LCFMAlignment;
import nl.tue.harmonioussimplification.algorithms.SlopeLadderSimplification;
import nl.tue.harmonioussimplification.data.input.InputMap;
import nl.tue.harmonioussimplification.data.output.OutputMap;
import nl.tue.harmonioussimplification.io.IPE;

public class Data {

    // data
    InputMap input;
    OutputMap output;

    // algorithms
    LCFMAlignment align = new LCFMAlignment();
    SlopeLadderSimplification simplify = new SlopeLadderSimplification();

    // settings
    MapRendering draw_input = new MapRendering();
    MapRendering draw_output = new MapRendering();
    boolean split = true;

    // must be last in variable declarations
    final DrawPanel draw = new DrawPanel(this);
    final SidePanel side = new SidePanel(this);

    public void inputChanged() {
        output = null;
        draw.zoomToFit();
        simplify.clear();
    }

    public void alignmentChanged() {
        draw.repaint();
        simplify.clear();
        output = null;
    }

    public void pasteMap() {
        input = IPE.readClipboard();
        inputChanged();
    }

    public void align() {
        if (input != null) {
            align.run(input);
            alignmentChanged();
        }
    }

    public void simplificationChanged() {
        draw.repaint();
    }

    public void simplify() {
        if (!simplify.isInitialized()) {
            initialize();
        }
        simplify.run();
        simplificationChanged();
    }

    public void initialize() {
        output = simplify.initialize(input);
        simplificationChanged();
    }

    public void stepSimplify() {
        if (!simplify.isInitialized()) {
            initialize();
        }
        simplify.step();
        simplificationChanged();
    }

    public class MapRendering {

        boolean isolines = true;
        boolean vertices = false;
        boolean alignment = true;
    }
}
