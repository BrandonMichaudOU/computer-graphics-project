package edu.ou.cs.cg.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private int start = -1;
    private int end = -1;

    public Graph(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public void addNode(Node n){
        nodes.add(n);
    }

    public void addNodes(List<Node> nodes) {
        for (Node n: nodes) {
            addNode(n);
        }
    }

    public void addEdge(Edge e){
        edges.add(e);
    }

    public void addEdges(List<Edge> edges) {
        for (Edge e: edges) {
            addEdge(e);
        }
    }

    public void setStart(int idx) {
        if (start == idx) {
            return;
        }
        if (start != -1) {
            nodes.get(start).toggleStart();
        }
        nodes.get(idx).toggleStart();
        start = idx;
    }
}
