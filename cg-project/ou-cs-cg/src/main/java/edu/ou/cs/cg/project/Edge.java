package edu.ou.cs.cg.project;

// Represent an edge
public class Edge {
    // Declare variables to hold connected nodes
    private Node node1;
    private Node node2;

    // Weight of edge
    private int weight;

    // Create an edge
    public Edge(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    // Create an edge
    public Edge(Node node1, Node node2, int weight) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;
    }

    // Get the first connected edge
    public Node getNode1() {
        return new Node(node1.getX(), node1.getY(), node1.getRadius());
    }

    // Get the second connected edge
    public Node getNode2() {
        return new Node(node2.getX(), node2.getY(), node1.getRadius());
    }

    // Get the weight
    public int getWeight() {
        return weight;
    }
}
