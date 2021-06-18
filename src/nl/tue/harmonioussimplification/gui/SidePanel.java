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

import nl.tue.harmonioussimplification.gui.Data.MapRendering;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;

public class SidePanel extends TabbedSidePanel {

    private final Data data;

    public SidePanel(Data data) {
        this.data = data;
        addIOTab();
        addAlignTab();
        addSimplifyTab();
        addRenderTab();

    }

    private void addIOTab() {
        SideTab tab = addTab("IO");

        tab.addButton("Paste map [v]", (e) -> data.pasteMap());
    }

    private void addAlignTab() {
        SideTab tab = addTab("Align");

        tab.addButton("Compute [a]", (e) -> data.align());

        data.align.addConfigurationControls(tab);
    }

    private void addSimplifyTab() {
        SideTab tab = addTab("Simplify");

        tab.addButton("Initialize [i]", (e) -> data.initialize());
        tab.addButton("Step [s]", (e) -> data.stepSimplify());
        tab.addButton("Run [r]", (e) -> data.simplify());

        data.simplify.addConfigurationControls(tab);
    }

    private void addRenderTab() {
        SideTab tab = addTab("Render");

        tab.addCheckbox("Split", data.split, (e, v) -> {
            data.split = v;
            data.draw.zoomToFit();
        });

        tab.addLabel("Input");
        makeSettings(tab, data.draw_input);

        tab.addLabel("Output");
        makeSettings(tab, data.draw_output);
    }

    private void makeSettings(SideTab tab, MapRendering style) {
        tab.addCheckbox("Isolines", style.isolines, (e, v) -> {
            style.isolines = v;
            data.draw.repaint();
        });
        tab.addCheckbox("Vertices", style.vertices, (e, v) -> {
            style.vertices = v;
            data.draw.repaint();
        });
        tab.addCheckbox("Alignment", style.alignment, (e, v) -> {
            style.alignment = v;
            data.draw.repaint();
        });
    }

}
