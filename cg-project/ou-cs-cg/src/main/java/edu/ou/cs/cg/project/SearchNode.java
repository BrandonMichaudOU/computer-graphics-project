package edu.ou.cs.cg.project;

import java.util.Random;

// Represents an abstract object used for algorithms
public class SearchNode {
    // Declare variables to use for BFS, DFS, and shortest path
    public SearchNode parent;
    public Node node;
    public int depth;

    // Create a search node
    public SearchNode(Node node) {
        this.parent = null;
        this.node = node;
    }
}
