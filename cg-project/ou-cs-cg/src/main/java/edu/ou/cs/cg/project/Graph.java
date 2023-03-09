package edu.ou.cs.cg.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Graph {
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private int start = -1;
    private int end = -1;

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

    public Node getStart() {
        Node startNode = nodes.get(start);
        return new Node(startNode.getX(), startNode.getY());
    }

    public void clearGraph() {
        edges.clear();
        nodes.clear();
        start = -1;
        end = -1;
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

    // public void removeNode(int i) {
    //     if (start == i) {
    //         start = -1;
    //     }
    //     Node n = nodes.get(i);
    //     nodes.remove(i);
    //     for (Edge e: edges) {
    //         if (e.getNode1().equals(n) || e.getNode2().equals(n)) {
    //             edges.remove(e);
    //         }
    //     }
    // }

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

    public List<Edge> BFS() {
        if (start == -1 || nodes.size() == 0) {
            return null;
        }
        ArrayList<Edge> path = new ArrayList<>();

        ArrayList<Node> seen = new ArrayList<>();
        Queue<Node> q = new LinkedList<Node>();
        q.add(nodes.get(start));
        seen.add(nodes.get(start));
        while (!q.isEmpty()) {
            Node n = q.poll();
            for (Edge e: edges){
                if (n.equals(e.getNode1())) {
                    Node n2 = e.getNode2();
                    if (!containsNode(seen, n2)) {
                        q.add(n2);
                        seen.add(n2);
                        path.add(e);
                    }
                }
                else if (n.equals(e.getNode2())) {
                    Node n1 = e.getNode1();
                    if (!containsNode(seen, n1)) {
                        q.add(n1);
                        seen.add(n1);
                        path.add(e);
                    }
                }
            }
        }
        return Collections.unmodifiableList(path);
    }

    public static boolean containsNode(ArrayList<Node> arr, Node n) {
        for (Node nod: arr) {
            if (n.equals(nod)) {
                return true;
            }
        }
        return false;
    }
}
