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

    public void addNode(double x, double y, double r){
        this.nodes.add(new Node(x, y, r));
    }

    public void addEdge(int node1, int node2){
        this.edges.add(new Edge(nodes.get(node1), nodes.get(node2)));
    }
}
