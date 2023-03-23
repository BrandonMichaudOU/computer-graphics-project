package edu.ou.cs.cg.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

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

    public Node getEnd() {
        Node endNode = nodes.get(end);
        return new Node(endNode.getX(), endNode.getY());
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
        if (end == idx) {
            nodes.get(end).toggleEnd();
            end = -1;
        }
        if (start == idx) {
            nodes.get(start).toggleStart();
            start = -1;
            return;
        }
        if (start != -1) {
            nodes.get(start).toggleStart();
        }
        nodes.get(idx).toggleStart();
        start = idx;
    }

    public void setEnd(int idx) {
        if (start == idx) {
            nodes.get(start).toggleStart();
            start = -1;
        }
        if (end == idx) {
            nodes.get(end).toggleEnd();
            end = -1;
            return;
        }
        if (end != -1) {
            nodes.get(end).toggleEnd();
        }
        nodes.get(idx).toggleEnd();
        end = idx;
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

    public List<Edge> DFS() {
        if (start == -1 || nodes.size() == 0) {
            return null;
        }
        ArrayList<Edge> path = new ArrayList<>();
        ArrayList<Node> seen = new ArrayList<>();
        seen.add(nodes.get(start));
        recursiveDFS(nodes.get(start), seen, path);
        
        return Collections.unmodifiableList(path);
    }

    public void recursiveDFS(Node n, List<Node> seen, List<Edge> path) {
        List<Edge> unseenConnected = getUnseenConnected(n, seen);
        for (Edge e: unseenConnected) {
            if (n.equals(e.getNode1()) && !containsNode(seen, e.getNode2())) {
                path.add(e);
                seen.add(e.getNode2());
                recursiveDFS(e.getNode2(), seen, path);
            }
            else if (!containsNode(seen, e.getNode1())) {
                path.add(e);
                seen.add(e.getNode1());
                recursiveDFS(e.getNode1(), seen, path);
            }
        }
    }

    public List<Edge> getUnseenConnected(Node n, List<Node> seen) {
        ArrayList<Edge> connected = new ArrayList<>();
        for (Edge e: edges) {
            if (e.getNode1().equals(n) && !containsNode(seen, e.getNode2())) {
                connected.add(e);
            }
            else if (e.getNode2().equals(n) && !containsNode(seen, e.getNode1())) {
                connected.add(e);
            }
        }
        return connected;
    }

    public static boolean containsNode(List<Node> arr, Node n) {
        for (Node nod: arr) {
            if (n.equals(nod)) {
                return true;
            }
        }
        return false;
    }
}
