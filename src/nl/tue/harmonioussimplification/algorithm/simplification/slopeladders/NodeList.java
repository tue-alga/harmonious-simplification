/*
 * Harmonious Simplification
 * Copyright (C) 2021   
 * Developed by 
 *   Arthur van Goethem (a.i.v.goethem@tue.nl) 
 *   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided LICENSE document for more information.
 */
package nl.tue.harmonioussimplification.algorithm.simplification.slopeladders;

import nl.tue.geometrycore.datastructures.doublylinkedlist.DoublyLinkedList;

public class NodeList extends DoublyLinkedList<Node> {

    public Node get(int index) {
        Node node = getFirst();
        while (index > 0) {
            node = node.getNext();
            index--;
        }
        return node;
    }
}
