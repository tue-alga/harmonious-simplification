/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.alignments;

import nl.tue.harmonioussimplification.map.Isoline;
import nl.tue.harmonioussimplification.map.Map;
import nl.tue.harmonioussimplification.map.Neighbor;
import nl.tue.geometrycore.gui.sidepanel.ComboTabItem;

public abstract class Alignment implements ComboTabItem {

    public void run(Map map) {
        for (Isoline iso : map.isolines) {
            for (Neighbor nbr : iso.neighbors) {
                if (nbr.from.index < nbr.to.index) {
                    process(nbr, nbr.twin);
                }
            }
        }
        map.processing += "_" + whatHaveYouDone();
    }

    protected abstract void process(Neighbor nbrA, Neighbor nbrB);

    public abstract String whatHaveYouDone();

    @Override
    public abstract String toString();
}
