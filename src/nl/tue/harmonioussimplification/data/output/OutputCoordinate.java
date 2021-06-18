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

import nl.tue.harmonioussimplification.data.AbstractCoordinate;
import nl.tue.harmonioussimplification.data.input.InputCoordinate;
import nl.tue.geometrycore.geometry.Vector;

public class OutputCoordinate extends AbstractCoordinate<OutputIsoline, OutputCoordinate> {

    // algorithm data    
    private InputCoordinate representsFrom;
    private InputCoordinate representsTo;
    private SlopeLadder ladder = null;
    private Vector collapseLocation = null;

    public OutputCoordinate(Vector location, OutputIsoline isoline, InputCoordinate represents) {
        super(location, isoline);
        this.representsFrom = represents;
        this.representsTo = represents;
    }

    public OutputCoordinate(Vector location, OutputIsoline isoline, InputCoordinate representsFrom, InputCoordinate representsTo) {
        super(location, isoline);
        this.representsFrom = representsFrom;
        this.representsTo = representsTo;
    }

    public InputCoordinate getRepresentsFrom() {
        return representsFrom;
    }

    public void setRepresentsFrom(InputCoordinate representsFrom) {
        this.representsFrom = representsFrom;
    }

    public InputCoordinate getRepresentsTo() {
        return representsTo;
    }

    public void setRepresentsTo(InputCoordinate representsTo) {
        this.representsTo = representsTo;
    }

    public SlopeLadder getLadder() {
        return ladder;
    }

    public void setLadder(SlopeLadder ladder) {
        this.ladder = ladder;
    }

    public Vector getCollapseLocation() {
        return collapseLocation;
    }

    public void setCollapseLocation(Vector collapseLocation) {
        this.collapseLocation = collapseLocation;
    }

    public boolean isCollapsable() {
        OutputIsoline isoline = getIsoline();
        return // at least 4 vertices in the isoline still
                isoline.size() > 3
                // and its either cyclic
                && (isoline.isCyclic()
                // or its far enough away from the last points
                || !(isoline.getFirst() == this || isoline.getLast() == this || isoline.getLast().getPrevious() == this));
    }

    public OutputCoordinate getExtendedStart() {
        return getCyclicPrevious();
    }

    public OutputCoordinate getExtendedEnd() {
        return getCyclicNext().getCyclicNext();
    }

    public OutputCoordinate[] getExtendedCoordinates() {
        return new OutputCoordinate[]{
            this.getCyclicPrevious(),
            this,
            this.getCyclicNext(),
            this.getCyclicNext().getCyclicNext()
        };
    }

}
