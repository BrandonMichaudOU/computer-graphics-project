package edu.ou.cs.cg.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

// Represents a graph
public class Graph {
    // Declare variables to hold state of graph
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private int start = -1;
    private int end = -1;

    // Initialzize a graph
    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    // Get the list of nodes
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    // Get the list of edges
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    // Get the start node
    public Node getStart() {
        Node startNode = nodes.get(start);
        return new Node(startNode.getX(), startNode.getY());
    }

    // Get the end node
    public Node getEnd() {
        Node endNode = nodes.get(end);
        return new Node(endNode.getX(), endNode.getY());
    }

    // Add a node
    public void addNode(Node n){
        nodes.add(n);
    }

    // Add a list of nodes
    public void addNodes(List<Node> nodes) {
        for (Node n: nodes) {
            addNode(n);
        }
    }

    // Add an edge
    public void addEdge(Edge e){
        edges.add(e);
    }

    // Add a list of edges
    public void addEdges(List<Edge> edges) {
        for (Edge e: edges) {
            addEdge(e);
        }
    }

    // Set the start node
    public void setStart(int idx) {
        // If the new index is currently the end, remove end and set start
        if (end == idx) {
            nodes.get(end).toggleEnd();
            end = -1;
        }
        // If the new index is already the start, remove it
        if (start == idx) {
            nodes.get(start).toggleStart();
            start = -1;
            return;
        }
        // If the start index is currently populated, toggle it off
        if (start != -1) {
            nodes.get(start).toggleStart();
        }
        // Update the start node
        nodes.get(idx).toggleStart();
        start = idx;
    }

    // Set the end node
    public void setEnd(int idx) {
        // If the new index is currently the start, remove start and set end
        if (start == idx) {
            nodes.get(start).toggleStart();
            start = -1;
        }
        // If the new index is already the end, remove it
        if (end == idx) {
            nodes.get(end).toggleEnd();
            end = -1;
            return;
        }
        // If the end index is currently populated, toggle it off
        if (end != -1) {
            nodes.get(end).toggleEnd();
        }// Update the end node
        nodes.get(idx).toggleEnd();
        end = idx;
    }

    // Perform breadth-first search on graph
    public List<SearchNode> BFS() {
        // If the start is not specified or there are no nodes, return null
        if (start == -1 || nodes.size() == 0) {
            return null;
        }

        // Declare a variable holding the order of visited search nodes
        ArrayList<SearchNode> path = new ArrayList<>();

        // Create a search node for the start and add it to the path
        SearchNode root = new SearchNode(getStart());
        root.depth = 0;
        path.add(root);

        // Create lists to hold seen nodes and the queue of search nodes. Add root to both
        ArrayList<Node> seen = new ArrayList<>();
        Queue<SearchNode> q = new LinkedList<>();
        q.add(root);
        seen.add(root.node);

        // Loop over the queue until it is empty
        while (!q.isEmpty()) {
            // Get the front of the queue
            SearchNode n = q.poll();

            // Loop over each edge to find if it is adjacent to current node
            for (Edge e: edges){
                // If the first side of the edge is the current node, procede
                if (n.node.equals(e.getNode1())) {
                    // Create a search node for the connected node and add to queue, seen, and path if it is unseen
                    Node n2 = e.getNode2();
                    if (!containsNode(seen, n2)) {
                        SearchNode child = new SearchNode(n2);
                        child.parent = n;
                        child.depth = child.parent.depth + 1;
                        q.add(child);
                        seen.add(n2);
                        path.add(child);
                    }
                }
                // If the second side of the edge is the current node, procede
                else if (n.node.equals(e.getNode2())) {
                    // Create a search node for the connected node and add to queue, seen, and path if it is unseen
                    Node n1 = e.getNode1();
                    if (!containsNode(seen, n1)) {
                        SearchNode child = new SearchNode(n1);
                        child.parent = n;
                        child.depth = child.parent.depth + 1;
                        q.add(child);
                        seen.add(n1);
                        path.add(child);
                    }
                }
            }
        }

        // Return the path
        return Collections.unmodifiableList(path);
    }

    // Perform depth first search on the graph
    public List<SearchNode> DFS() {
        // If the start is not specified or there are no nodes, return null
        if (start == -1 || nodes.size() == 0) {
            return null;
        }

        // Declare a variable holding the order of visited search nodes
        ArrayList<SearchNode> path = new ArrayList<>();

        // Create a search node for the start and add it to the path
        SearchNode root = new SearchNode(getStart());
        path.add(root);

        // Create a list to hold seen and add root
        ArrayList<Node> seen = new ArrayList<>();
        seen.add(root.node);

        // Perform recursive DFS
        recursiveDFS(root, seen, path);
        
        // Return the path
        return Collections.unmodifiableList(path);
    }

    // Recursive function for DFS
    public void recursiveDFS(SearchNode n, List<Node> seen, List<SearchNode> path) {
        // Get all the edges connected to the current node and an unseen node
        List<Edge> unseenConnected = getUnseenConnected(n, seen);

        // Recursively call method from unseen connected nodes
        for (Edge e: unseenConnected) {
            // If the current nodes is the first node of the edge procede
            if (n.node.equals(e.getNode1()) && !containsNode(seen, e.getNode2())) {
                // Create a search node for the unseen node and add it to the path and seen
                SearchNode child = new SearchNode(e.getNode2());
                child.parent = n;
                child.depth = child.parent.depth + 1;
                path.add(child);
                seen.add(e.getNode2());

                // Recurse from new search node
                recursiveDFS(child, seen, path);
            }
            // If the current node is the second node of the edge procede
            else if (!containsNode(seen, e.getNode1())) {
                // Create a search node for the unseen node and add it to the path and seen
                SearchNode child = new SearchNode(e.getNode1());
                child.parent = n;
                child.depth = child.parent.depth + 1;
                path.add(child);
                seen.add(e.getNode1());

                // Recurse from new search node
                recursiveDFS(child, seen, path);
            }
        }
    }

    // Get all the edges adjacent to a search node that connect to an unseen node
    public List<Edge> getUnseenConnected(SearchNode n, List<Node> seen) {
        // Create a variable to store the edges
        ArrayList<Edge> connected = new ArrayList<>();

        // Loop over each edge to see connects the given search node to an unseen node. If it does, add it
        for (Edge e: edges) {
            if (e.getNode1().equals(n.node) && !containsNode(seen, e.getNode2())) {
                connected.add(e);
            }
            else if (e.getNode2().equals(n.node) && !containsNode(seen, e.getNode1())) {
                connected.add(e);
            }
        }

        // Return the connected edges
        return connected;
    }

    // Test if a node is in a given list
    public static boolean containsNode(List<Node> arr, Node n) {
        for (Node nod: arr) {
            if (n.equals(nod)) {
                return true;
            }
        }
        return false;
    }
}
