package edu.ou.cs.cg.project;

public class Node {
    public double x;
    public double y;
    public double r;
    public boolean start;
    public boolean end;

    public Node(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.start = false;
        this.end = false;
    }

    public Node(double x, double y, boolean start, boolean end) {
        this.x = x;
        this.y = y;
        this.start = start;
        this.end = end;
    }

    public boolean isInNode(double x, double y) {
        double dist = Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
        if (dist <= r) {
            return true;
        }
        return false;
    }
}
