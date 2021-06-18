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

import nl.tue.harmonioussimplification.map.Map;
import nl.tue.harmonioussimplification.algorithm.alignments.Alignment;
import nl.tue.harmonioussimplification.algorithm.alignments.DTWAlignment;
import nl.tue.harmonioussimplification.algorithm.alignments.LCFMAlign;
import nl.tue.harmonioussimplification.algorithm.simplification.SlopeLadderSimplification;
import nl.tue.harmonioussimplification.algorithm.simplification.Simplification;
import nl.tue.harmonioussimplification.inputwrangling.IO;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.io.ipe.IPEWriter;

public class Data {

    public static Map map = null;

    public static Alignment[] aligners = { new LCFMAlign(), new DTWAlignment()};
    public static Alignment selectedAligner = aligners[0];

    public static Simplification[] simplifiers = {
        new SlopeLadderSimplification()};
    public static Simplification selectedSimplifier = simplifiers[0];

    public static DrawPanel draw = null;
    public static SidePanel side = null;

    public static boolean autorun = false;
    // draw settings
    public static boolean showinput = true;
    public static boolean showinputvertices = false;
    public static boolean showoutput = true;
    public static boolean showoutputvertices = false;
    public static boolean showalgorithm = true;
    public static boolean showalign = true;

    public static void clearCache(boolean alignCache, boolean simplifyCache) {
        if (map != null) {
            map.clearCache(alignCache, simplifyCache);
        }
    }

    public static void run(boolean runAlign, boolean runSimplify) {
        if (map != null) {
            clearCache(runAlign, runSimplify);
            if (runAlign) {
                selectedAligner.run(map);
            }
            if (runSimplify) {
                selectedSimplifier.run(map);
            }
            draw.repaint();
        }
    }

    public static void autorun(boolean runAlign, boolean runSimplify) {
        if (autorun) {
            run(runAlign, runSimplify);
        } else {
            clearCache(runAlign, runSimplify);
            draw.repaint();
        }
    }

    public static void save() {
        if (map == null) {
            return;
        }
        File f = new File("../output/" + map.processing + ".ipe");
        f.getParentFile().mkdirs();
        try (IPEWriter write = IPEWriter.fileWriter(f)) {
            write.initialize();
            write.newPage("input", "output", "aux", "align", "algorithm");
            draw.render(write);
        } catch (IOException ex) {
            Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void loadClipboard() {
        map = IO.readIPEClipboard();
        autorun(true,true);
        draw.zoomToFit();
    }

    public static void copyToClipboard() {
        IO.writeIPEClipboard(map);
    }
    
    public static void renderToClipboard() {
        try {
            IPEWriter ipe = IPEWriter.clipboardWriter();
            ipe.initialize();
            draw.render(ipe);
            ipe.close();
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}