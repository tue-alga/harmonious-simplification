/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.harmonioussimplification.data.input.InputCoordinate;
import nl.tue.harmonioussimplification.data.input.InputIsoline;
import nl.tue.harmonioussimplification.data.input.InputMap;
import nl.tue.harmonioussimplification.gui.DrawPanel;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;

public class IPE {

    private static InputMap read(IPEReader read) throws IOException {

        read.setBezierSampling(1);
        List<ReadItem> items = read.read();

        return constructMap(items, null);
    }

    public static InputMap readClipboard() {
        try (IPEReader read = IPEReader.clipboardReader()) {
            return read(read);
        } catch (IOException ex) {
            Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static InputMap readFile(String file) {
        try (IPEReader read = IPEReader.fileReader(new File(file))) {
            return read(read);
        } catch (IOException ex) {
            Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static InputMap[] readManyFromFile(String file, String layerFilter) {
        try (IPEReader read = IPEReader.fileReader(new File(file))) {
            List<List<ReadItem>> pages = new ArrayList();
            read.readPages(pages);
            InputMap[] result = new InputMap[pages.size()];
            int i = 0;
            for (List<ReadItem> items : pages) {
                result[i] = constructMap(items, layerFilter);
                i++;
            }
            return result;
        } catch (IOException ex) {
            Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static InputMap constructMap(List<ReadItem> items, String layerFilter) {

        InputMap map = new InputMap();

        for (ReadItem ri : items) {
            if (layerFilter != null && !ri.getLayer().startsWith(layerFilter)) {
                continue;
            }
            switch (ri.toGeometry().getGeometryType()) {
                case POLYLINE: {
                    InputIsoline iso = new InputIsoline(false);
                    PolyLine pl = (PolyLine) ri.toGeometry();
                    for (Vector v : pl.vertices()) {
                        iso.addLast(new InputCoordinate(v, iso));
                    }
                    map.add(iso);
                    break;
                }
                case POLYGON: {
                    InputIsoline iso = new InputIsoline(true);
                    Polygon pl = (Polygon) ri.toGeometry();
                    for (Vector v : pl.vertices()) {
                        iso.addLast(new InputCoordinate(v, iso));
                    }
                    map.add(iso);
                    break;
                }
                default:
                    System.err.println("Unexpected type: " + ri.toGeometry().getGeometryType());
                    break;
            }
        }
        InputProcessing.cleanMap(map);
        return map;
    }
}
