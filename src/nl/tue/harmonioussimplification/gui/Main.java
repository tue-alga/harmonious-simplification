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

import nl.tue.geometrycore.gui.GUIUtil;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Data data = new Data();
        GUIUtil.makeMainFrame("Harmonious simplification", data.draw, data.side);
    }

}
