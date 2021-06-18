/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.data.output;

import nl.tue.harmonioussimplification.data.AbstractIsoline;
import nl.tue.harmonioussimplification.data.input.InputIsoline;

public class OutputIsoline extends AbstractIsoline<OutputIsoline, OutputCoordinate> {

    public OutputIsoline(boolean cyclic, InputIsoline represents) {
        super(cyclic);
        this.represents = represents;
    }

    private InputIsoline represents;

    public InputIsoline getRepresents() {
        return represents;
    }

    public void setRepresents(InputIsoline represents) {
        this.represents = represents;
    }

}
