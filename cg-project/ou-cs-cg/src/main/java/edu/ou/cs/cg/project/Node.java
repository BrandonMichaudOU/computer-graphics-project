package edu.ou.cs.cg.project;

public class Node {
    public double x;
    public double y;
    public boolean start;
    public boolean end;

    public Node(double x, double y) {
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
}
