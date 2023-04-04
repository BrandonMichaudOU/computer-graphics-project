package edu.ou.cs.cg.project;

import java.util.ArrayList;

public class SearchNode {
    public ArrayList<SearchNode> children;
    public SearchNode parent;
    public Node node;

    public SearchNode(Node node) {
        this.children = new ArrayList<>();
        this.parent = null;
        this.node = node;
    }
}
