/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.map;

import nl.tue.harmonioussimplification.algorithm.simplification.slopeladders.SlopeLadder;
import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Rectangle;

public class Map {

    public List<Isoline> isolines = new ArrayList();
    public String processing = "input";
    
    // cached output for ladders
    public List<SlopeLadder> ladders;

    public void constructList(List<PolyLine> lines) {
        isolines.clear();
        Isoline prev = null;
        for (int i = 0; i < lines.size(); i++) {
            Isoline iso = new Isoline(i, lines.get(i));
            isolines.add(iso);

            if (prev != null) {
                Neighbor nbr = new Neighbor(iso, prev);
                iso.neighbors.add(nbr);

                Neighbor nbr2 = new Neighbor(prev, iso);
                prev.neighbors.add(nbr2);

                nbr.twin = nbr2;
                nbr2.twin = nbr;
            }

            prev = iso;
        }
    }

    public void clearCache(boolean alignCache, boolean simplifyCache) {
        if (alignCache) {
            processing = "input";
        } else if (simplifyCache) {
            processing = processing.substring(0, processing.lastIndexOf("_"));
        }
        if (simplifyCache) {
            ladders = null;
        }
        for (Isoline iso : isolines) {
            iso.clearCache(alignCache, simplifyCache);
        }
    }

    public Rectangle getBoundingBox() {
        Rectangle R = new Rectangle();
        for (Isoline iso : isolines) {
            R.includeGeometry(iso.input);
        }
        return R;
    }
}
