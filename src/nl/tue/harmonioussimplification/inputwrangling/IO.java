/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.inputwrangling;

import nl.tue.harmonioussimplification.map.Isoline;
import nl.tue.harmonioussimplification.map.Map;
import nl.tue.harmonioussimplification.algorithm.simplification.Simplification;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;
import nl.tue.geometrycore.io.ipe.IPEWriter;
import nl.tue.harmonioussimplification.gui.DrawPanel;

public class IO {

    public static Map readIPEClipboard() {
        Map map = null;
        try (IPEReader read = IPEReader.clipboardReader()) {
            read.setBezierSampling(1);
            List<PolyLine> lines = new ArrayList();
            List<ReadItem> items = read.read();
            for (ReadItem ri : items) {
                switch (ri.toGeometry().getGeometryType()) {
                    case POLYLINE:
                        lines.add((PolyLine) ri.toGeometry());
                        break;
                    case CIRCLE:
                        Simplification.eps = ((Circle) ri.toGeometry()).getRadius();
                        break;
                    default:
                        System.err.println("Unexpected type: " + ri.toGeometry().getGeometryType());
                        break;
                }
            }
            map = new Map();
            map.constructList(lines);
        } catch (IOException ex) {
            Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    public static Map readIPEFile(String file) {
        Map map = null;
        try (IPEReader read = IPEReader.fileReader(new File(file))) {
            read.setBezierSampling(1);
            List<PolyLine> lines = new ArrayList();
            List<ReadItem> items = read.read();
            for (ReadItem ri : items) {
                switch (ri.toGeometry().getGeometryType()) {
                    case POLYLINE:
                        lines.add((PolyLine) ri.toGeometry());
                        break;
                    case CIRCLE:
                        Simplification.eps = ((Circle) ri.toGeometry()).getRadius();
                        break;
                    default:
                        System.err.println("Unexpected type: " + ri.toGeometry().getGeometryType());
                        break;
                }
            }
            map = new Map();
            map.constructList(lines);
        } catch (IOException ex) {
            Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    public static Map[] readIPEFileMultiple(String file, String layerStart) {
        Map[] result = null;
        try (IPEReader read = IPEReader.fileReader(new File(file))) {
            List<ReadItem> items = read.read();

            int pages = 0;
            for (ReadItem ri : items) {
                int pagenum = ri.getPageNumber();
                if (pagenum > pages) {
                    pages = pagenum;
                }
            }

            List<PolyLine>[] lines = new List[pages];
            for (int i = 0; i < pages; i++) {
                lines[i] = new ArrayList();
            }

            for (ReadItem ri : items) {
                if (ri.getLayer().startsWith(layerStart)) {
                    switch (ri.toGeometry().getGeometryType()) {
                        case POLYLINE:
                            lines[ri.getPageNumber() - 1].add((PolyLine) ri.toGeometry());
                            break;
                        case CIRCLE:
                            Simplification.eps = ((Circle) ri.toGeometry()).getRadius();
                            break;
                        default:
                            System.err.println("Unexpected type: " + ri.toGeometry().getGeometryType());
                            break;
                    }
                }
            }

            result = new Map[pages];
            for (int i = 0; i < pages; i++) {
                result[i] = new Map();
                result[i].constructList(lines[i]);
            }
        } catch (IOException ex) {
            Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public static void writeIPEClipboard(Map map) {
        if (map == null) {
            return;
        }
        try (IPEWriter write = IPEWriter.clipboardWriter()) {
            write.setView(IPEWriter.getA4Size());
            write.setWorldview(map.getBoundingBox());
            write.initialize();
            write.setSizeMode(SizeMode.VIEW);
            write.setStroke(Color.black, 0.4, Dashing.SOLID);
            for (Isoline iso : map.isolines) {
                write.draw(iso.input);
            }
        } catch (IOException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
