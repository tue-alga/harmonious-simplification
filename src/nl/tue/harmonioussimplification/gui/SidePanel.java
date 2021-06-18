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

import nl.tue.harmonioussimplification.algorithm.alignments.Alignment;
import nl.tue.harmonioussimplification.algorithm.simplification.Simplification;
import nl.tue.geometrycore.gui.sidepanel.ComboTab;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;

public class SidePanel extends TabbedSidePanel {

    public SidePanel() {
        makeBaseTab();

        makeAlignTab();

        makeSimplifyTab();
    }

    private void makeBaseTab() {
        SideTab t = addTab("BASE");

        t.addButton("Paste from IPE (V)", (e) -> Data.loadClipboard());
        t.addButton("Copy input map to IPE (C)", (e) -> Data.copyToClipboard());
        t.addButton("Save to file (S)", (e) -> Data.save());

        t.addButton("Copy rendering to IPE", (e) -> Data.renderToClipboard());
        
        t.addSpace(4);

        t.addButton("Run (R)", (e) -> Data.run(true, true));
        t.addCheckbox("Auto run", Data.autorun, (e, b) -> {
            Data.autorun = b;
            if (b) {
                Data.autorun(true, true);
            }
        });

        t.addSpace(4);

        t.addCheckbox("Show input", Data.showinput, (e, v) -> {
            Data.showinput = v;
            Data.draw.repaint();
        });
        t.addCheckbox("Show input vertices", Data.showinputvertices, (e, v) -> {
            Data.showinputvertices = v;
            Data.draw.repaint();
        });
        t.addCheckbox("Show algorithm", Data.showalgorithm, (e, v) -> {
            Data.showalgorithm = v;
            Data.draw.repaint();
        });
        t.addCheckbox("Show output", Data.showoutput, (e, v) -> {
            Data.showoutput = v;
            Data.draw.repaint();
        });
        t.addCheckbox("Show output vertices", Data.showoutputvertices, (e, v) -> {
            Data.showoutputvertices = v;
            Data.draw.repaint();
        });
        t.addCheckbox("Show alignment", Data.showalign, (e, v) -> {
            Data.showalign = v;
            Data.draw.repaint();
        });
    }

    private void makeAlignTab() {

        ComboTab<Alignment> tab = addComboTab("Align", (e, v) -> {
            Data.selectedAligner = v;
            Data.autorun(true, true);
        }, Data.selectedAligner, Data.aligners);

        tab.startCommonMode();
        tab.addButton("Run", (e) -> Data.run(true, false));
        tab.endCommonMode();
    }

    private void makeSimplifyTab() {

        ComboTab<Simplification> tab = addComboTab("Simplify", (e, v) -> {
            Data.selectedSimplifier = v;
            Data.autorun(false, true);
        }, Data.selectedSimplifier, Data.simplifiers);

        tab.startCommonMode();
        tab.addButton("Run", (e) -> Data.run(false, true));
        tab.endCommonMode();
    }

}
