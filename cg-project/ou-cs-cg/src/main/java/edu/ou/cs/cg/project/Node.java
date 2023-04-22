package edu.ou.cs.cg.project;

import java.awt.geom.Point2D;

// Represents a node
public class Node {
    // Declare variables to hold position and state of node
    private double x;
    private double y;
    private double r;
    private boolean start = false;
    private boolean end = false;

    // Create a node
    public Node(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    // Get the center of node
    public Point2D.Double getPoint() {
        return new Point2D.Double(x, y);
    }

    // Get the x-coordinate
    public double getX() {
        return x;
    }

    // Get the y-coordinate
    public double getY() {
        return y;
    }

    // Get the radius
    public double getRadius() {
        return r;
    }

    // Flip the start bit
    public void toggleStart() {
        start = !start;
    }

    // Flip the end bit
    public void toggleEnd() {
        end = !end;
    }

    // Get the start bit
    public boolean isStart() {
        return start;
    }

    // Get the end bit
    public boolean isEnd() {
        return end;
    }

    // Determine if this node is equal to a given node
    public boolean equals(Node n) {
        // If the x and y coordinates are the same, the nodes are equal
        double alpha = 0.05;
        if (Math.abs(n.x - this.x) <= alpha && Math.abs(n.y - this.y) <= alpha) {
            return true;
        }
        return false;
    }
}
