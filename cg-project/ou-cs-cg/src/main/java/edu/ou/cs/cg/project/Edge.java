package edu.ou.cs.cg.project;

public class Edge {
    private Node node1;
    private Node node2;

    public Edge(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public Node getNode1() {
        return new Node(node1.getX(), node1.getY());
    }

    public Node getNode2() {
        return new Node(node2.getX(), node2.getY());
    }
}
