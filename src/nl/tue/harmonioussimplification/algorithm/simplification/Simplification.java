/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification;

import nl.tue.harmonioussimplification.map.Map;
import nl.tue.geometrycore.gui.sidepanel.ComboTabItem;
import nl.tue.harmonioussimplification.gui.DrawPanel;

public abstract class Simplification implements ComboTabItem {

    public static double eps = 10;

    public void run(Map map) {
        process(map);
        map.processing += "_" + whatHaveYouDone();
    }
    
    protected abstract void process(Map map);

    public abstract String whatHaveYouDone();
        
    @Override
    public abstract String toString();   
    
    public void draw(DrawPanel draw) {
        
    }
    
}
