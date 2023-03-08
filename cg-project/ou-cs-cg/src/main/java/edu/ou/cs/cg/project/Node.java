package edu.ou.cs.cg.project;

import java.awt.geom.Point2D;
import java.util.Comparator;

public class Node {
    private double x;
    private double y;
    private double r = 25;
    private boolean start = false;
    private boolean end = false;

    public Node(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point2D.Double getPoint() {
        return new Point2D.Double(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isInNode(double x, double y) {
        double dist = Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
        if (dist <= r) {
            return true;
        }
        return false;
    }

    public void toggleStart() {
        start = !start;
    }

    public void toggleEnd() {
        end = !end;
    }

    public boolean isStart() {
        return start;
    }

    public boolean isEnd() {
        return end;
    }

    public boolean equals(Node n) {
        double alpha = 0.05;
        if (Math.abs(n.x - this.x) <= alpha && Math.abs(n.y - this.y) <= alpha) {
            return true;
        }
        return false;
    }
}
