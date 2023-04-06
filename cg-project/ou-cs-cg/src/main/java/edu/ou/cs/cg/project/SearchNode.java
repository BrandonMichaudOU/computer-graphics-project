package edu.ou.cs.cg.project;

import java.util.ArrayList;

public class SearchNode {
    public SearchNode parent;
    public Node node;
    public int depth;

    public SearchNode(Node node) {
        this.parent = null;
        this.node = node;
    }
}
