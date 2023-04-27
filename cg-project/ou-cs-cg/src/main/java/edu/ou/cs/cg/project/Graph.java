package edu.ou.cs.cg.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

// Represents a graph
public class Graph {
    // Declare variables to hold state of graph
    public ArrayList<Node> nodes;
    public ArrayList<Edge> edges;
    public int start = -1;
    public int end = -1;

    // Random number generator
    private Random rand = new Random();

    // Constants for randoms
    private final int MAX_WEIGHT = 10;
    private final double RADIUS = 25;
    private final int MAX_NODES = 10;
    private final int MIN_NODES = 5;

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
        return new Node(startNode.getX(), startNode.getY(), startNode.getRadius());
    }

    // Get the end node
    public Node getEnd() {
        Node endNode = nodes.get(end);
        return new Node(endNode.getX(), endNode.getY(), endNode.getRadius());
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
        if (idx >= 0 && idx < nodes.size()) {
            nodes.get(idx).toggleEnd();
        }
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

    // Find shortest path between start and end node
    public SearchNode shortestPath() {
        // Create a search node for each node in the graph
        ArrayList<SearchNode> searchNodes = new ArrayList<>();
        for (Node n: nodes) {
            SearchNode temp = new SearchNode(n);
            temp.depth = 0;
            searchNodes.add(temp);
        }

        // Keep track of the minimum distance to each search node from start
        HashMap<SearchNode, Integer> dist = new HashMap<>();

        // Priority queue for selecting smallest distance
        ArrayList<SearchNode> q = new ArrayList<>();

        // Set the distance to infinity for each node except start (0) and add to priority queue
        for (SearchNode sn: searchNodes) {
            if (sn.node.equals(getStart())) {
                dist.put(sn, 0);
            }
            else {
                dist.put(sn, Integer.MAX_VALUE);
            }
            q.add(sn);
        }

        // Loop over priority queue until end node is selected or all nodes are exhausted
        while (!q.isEmpty()) {
            // Find the minimum distance node left in the priority queue
            int minDist = Integer.MAX_VALUE;
            SearchNode minNode = null;
            for (SearchNode sn: q) {
                if (dist.get(sn) < minDist) {
                    minDist = dist.get(sn);
                    minNode = sn;
                }
            }

            // If the minimum distance node is the end, return it
            if (minNode.node.equals(getEnd())) {
                return minNode;
            }

            // Remove the minimum distance node from the priority queue
            q.remove(minNode);

            // Loop over every edge to see if it contains the source search node
            for (Edge e: edges) {
                // If the source search node is on front end, procede
                if (e.getNode1().equals(minNode.node)) {
                    // Check if the back end search node is in list. If so add it
                    for (SearchNode sn: q) {
                        if (sn.node.equals(e.getNode2())) {
                            // Find the distance to neighboring search node through the minimum distance search node
                            int temp = dist.get(minNode) + e.getWeight();

                            // If the new distance is smaller than the existing distance, update it, the parent, and the depth
                            if (temp < dist.get(sn)) {
                                dist.put(sn, temp);
                                sn.parent = minNode;
                                sn.depth = minNode.depth + 1;
                            }
                            break;
                        }
                    }
                }
                // If the source search node is on back end, procede
                else if (e.getNode2().equals(minNode.node)) {
                    // Check if the front end search node is in list. If so add it
                    for (SearchNode sn: q) {
                        if (sn.node.equals(e.getNode1())) {
                            // Find the distance to neighboring search node through the minimum distance search node
                            int temp = dist.get(minNode) + e.getWeight();

                            // If the new distance is smaller than the existing distance, update it, the parent, and the depth
                            if (temp < dist.get(sn)) {
                                dist.put(sn, temp);
                                sn.parent = minNode;
                                sn.depth = minNode.depth + 1;
                            }
                            break;
                        }
                    }
                }
            }
        }

        // Return null if the end node was not found
        return null;
    }

    // Find shortest path between start and end node
    public List<SearchNode> shortestPathWrapper() {
        // If the two nodes are not specified or there are no nodes, return null
        if (start == -1 || end == -1 || nodes.size() == 0) {
            return null;
        }

        // Get the end search node from shortest path
        SearchNode temp = shortestPath();

        // Build path by navigating to start from end
        ArrayList<SearchNode> path = new ArrayList<>();
        while (temp != null) {
            path.add(0, temp);
            temp = temp.parent;
        }

        // Return shortest path
        return path;
    }

    // Find the connected search nodes to a given source that are in a given list
    public List<SearchNode> getConnectedInList(SearchNode source, List<SearchNode> list) {
        // Keep track of the connected search nodes in the list
        ArrayList<SearchNode> connected = new ArrayList<>();

        // Loop over every edge to see if it contains the source search node
        for (Edge e: edges) {
            // If the source search node is on front end, procede
            if (e.getNode1().equals(source.node)) {
                // Check if the back end search node is in list. If so add it
                for (SearchNode sn: list) {
                    if (sn.node.equals(e.getNode2())) {
                        connected.add(sn);
                        break;
                    }
                }
            }
            // If the source search node is on back end, procede
            else if (e.getNode2().equals(source.node)) {
                // Check if the front end search node is in list. If so add it
                for (SearchNode sn: list) {
                    if (sn.node.equals(e.getNode1())) {
                        connected.add(sn);
                        break;
                    }
                }
            }
        }

        // Return list of connected search nodes in list
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

    //**********************************************************************
	// Private Methods (Auto-Generated Graphs)
	//**********************************************************************

    // Populate default graph
    public void defaultGraph() {
        // Clear start and end
        start = -1;
        end = -1;

        // Add nodes to graph
        nodes.clear();
		nodes.add(new Node(340, 680, RADIUS));
		nodes.add(new Node(640, 680, RADIUS));
		nodes.add(new Node(940, 680, RADIUS));
		nodes.add(new Node(40, 360, RADIUS));
		nodes.add(new Node(490, 360, RADIUS));
		nodes.add(new Node(1240, 360, RADIUS));
		nodes.add(new Node(340, 40, RADIUS));
		nodes.add(new Node(640, 40, RADIUS));
		nodes.add(new Node(940, 40, RADIUS));

		// Add edges to graph
        edges.clear();
		edges.add(new Edge(nodes.get(0), nodes.get(1), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(0), nodes.get(3), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(0), nodes.get(6), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(1), nodes.get(2), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(1), nodes.get(4), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(1), nodes.get(8), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(2), nodes.get(5), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(2), nodes.get(8), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(3), nodes.get(6), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(4), nodes.get(6), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(4), nodes.get(7), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(5), nodes.get(8), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(6), nodes.get(7), rand.nextInt(MAX_WEIGHT) + 1));
		edges.add(new Edge(nodes.get(7), nodes.get(8), rand.nextInt(MAX_WEIGHT) + 1));
    }

    // Find distance between two points
    private double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

    // Generate a random graph
    public void randomGraph() {
        // Clear start and end
        start = -1;
        end = -1;

        // Clear previous graph
        nodes.clear();
        edges.clear();

        // Generate a random number of nodes between bounds
        int numNodes = rand.nextInt(MAX_NODES - MIN_NODES + 1) + MIN_NODES;
		for (int i = 0; i < numNodes; ++i) {
            // Declare variables for node position
			int x;
			int y;

            // Continue randomly generating positions until one does not overlap other ndoes
			boolean trip;
			do {
                // Default to not overlapping
				trip = false;

                // Generate random position within screen
				x = rand.nextInt((int)(1280 - 2 * RADIUS)) + (int)RADIUS;
				y = rand.nextInt((int)(720 - 2 * RADIUS)) + (int)RADIUS;

                // Test if the new position conflicts with previous nodes
				for (Node n: nodes) {
                    // If the node is too close to another node, regenerate position
					if (distance(x, y, n.getX(), n.getY()) <= 7 * RADIUS) {
						trip = true;
						break;
					}
				}
			} while (trip);

            // Add the new node
			nodes.add(new Node(x, y, RADIUS));
		}

        // Define the maximum number of edges in a graph
        //int maxEdges = (((numNodes - 1) * (numNodes - 2)) / 2) + 1;
        int maxEdges = Math.min((((numNodes - 1) * (numNodes - 2)) / 2) + 1, numNodes * 2);

        // Define minimum number of edges to guarentee connected graph
        int minEdges = numNodes - 1;

        // Randomly generate minimum number of edges
		int numEdges = rand.nextInt(maxEdges - minEdges) + minEdges;

        // Define connected matrix for nodes and initialize every cell with false
        boolean[][] matrix = new boolean[numNodes][numNodes];
        for (int i = 0; i < numNodes; ++i) {
            for (int j = 0; j < numNodes; ++j) {
                matrix[i][j] = false;
            }
        }

        // Find list of all nodes connected to first node
        start = 0;
        List<SearchNode> testConnected = DFS();

        // Continue making edges until graph is connected or generated minimum number has not been met
        int k = 0;
		while (testConnected == null || testConnected.size() != numNodes || k < numEdges) {
            k++;
            // Declare variables for edge end points
			int node1;
			int node2;

            // Continue randomly generating node pairs until the edge:
            // 1. Is not a loop
            // 2. Is not a repeat
            // 3. Does not intersect a non-endpoint node
            int ctr = 0;
            boolean infinite = false;
			boolean trip;
			do {
                // Assume the edge is valid
				trip = false;

                // Generate random endpoints
				node1 = rand.nextInt(numNodes);
				node2 = rand.nextInt(numNodes);

                if (ctr > 1000) {
                    System.out.println("Infinite loop");
                    infinite = true;
                    break;
                }

                // If the endpoints are the same, regenerate new endpoints
				if (node1 == node2) {
					trip = true;
					continue;
				}
                // If the edge already exists, regenerate new endpoints
                else if (matrix[node1][node2]) {
                    trip = true;
                    continue;
                }

                // Find vector between nodes
				double vx = nodes.get(node2).getX() - nodes.get(node1).getX();
				double vy = nodes.get(node2).getY() - nodes.get(node1).getY();

                // Check to see if edge intersects each non-endpoint node
				for (int j = 0; j < numNodes; ++j) {
                    // If the current node is either endpoint, continue
					if (j == node1 || j == node2) {
						continue;
					}

                    // Declare variables for node projected onto edge
					double px, py;

                    // If the edge is vertical, specially define projection
					if (vx == 0) {
						px = nodes.get(node1).getX();
						py = nodes.get(j).getY();
					}
                    // If the edge is horizontal, specially define projection
					else if (vy == 0) {
						px = nodes.get(j).getX();
						py = nodes.get(node1).getY();
					}
                    // If the edge is not special, find projection using line intersection
					else {
                        // Find slope and y-intercept of edge
						double m = vy / vx;
						double b = nodes.get(node1).getY() - m * nodes.get(node1).getX();

                        // Find slope and y-intercept of perpendicular line to edge through node
						double mn = -(vx / vy);
						double bn = nodes.get(j).getY() - mn * nodes.get(j).getX();

                        // Find intersection of edge and perpendicular line to get projection of node onto edge
						px = (bn - b) / (m - mn);
						py = m * px + b;
					}

                    // If the projected x value is within edge, check the closest distance to edge
					if (nodes.get(node1).getX() < nodes.get(node2).getX() && nodes.get(node1).getX() <= px && px <= nodes.get(node2).getX()) {
                        // If the minimum distance from the node to the edge is less than the
                        // radius regenerate a new edge
						if (distance(nodes.get(j).getX(), nodes.get(j).getY(), px, py) <= RADIUS) {
							trip = true;
							break;
						}
					}
					else if (nodes.get(node2).getX() <= px && px <= nodes.get(node1).getX()) {
                        // If the minimum distance from the node to the edge is less than the
                        // radius regenerate a new edge
						if (distance(nodes.get(j).getX(), nodes.get(j).getY(), px, py) <= RADIUS) {
							trip = true;
							break;
						}
					}
				}
                ++ctr;
			} while (trip);

            // Add the edge
            if (!infinite) {
                matrix[node1][node2] = true;
                matrix[node2][node1] = true;
                edges.add(new Edge(nodes.get(node1), nodes.get(node2), rand.nextInt(MAX_WEIGHT) + 1));
            }

            // Test the graph to see if it is connected
            testConnected = DFS();
		}

        // Clear start node from connectivity tests
        start = -1;
    }
}
