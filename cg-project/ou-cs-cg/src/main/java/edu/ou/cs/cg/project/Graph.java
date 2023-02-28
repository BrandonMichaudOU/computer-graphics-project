package edu.ou.cs.cg.project;

import java.util.ArrayList;

public class Graph {
    public ArrayList<Node> nodes;
    public ArrayList<Edge> edges;

    public Graph(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }
}
