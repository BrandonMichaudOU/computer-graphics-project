package edu.ou.cs.cg.project;

import java.util.ArrayList;
import java.util.Random;

public class SearchNode {
    public SearchNode parent;
    public Node node;
    public int depth;
    public int weight;

    public SearchNode(Node node) {
        this.parent = null;
        this.node = node;
        this.weight=1;
    }

    public void randomNum(){
        Random r = new Random();
        weight= r.nextInt(5)+1;
    }
}
