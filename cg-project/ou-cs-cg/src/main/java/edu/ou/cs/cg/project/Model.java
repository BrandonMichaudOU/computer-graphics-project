package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;

import com.jogamp.opengl.*;

//******************************************************************************

/**
 * The <CODE>Model</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Model
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View					view;

	// Model variables
	private Graph graph;
	private Point2D.Double cursor;	// Current cursor coords
	private List<SearchNode> path;
	private boolean[] pathType;

	// Animation variables
	private Point2D.Double pan;
	private double zoom;
	private double speed;
	private int pause;
	private String currentMode;

	// Projection variables
	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize model variables
		graph = new Graph();
        path = null;
		pathType = new boolean[] {false, false, false};
		pan = new Point2D.Double();
		zoom = 1;
		speed = 1;
		pause = 1;
		currentMode = "";
		xmin = 0;
		xmax = 1280;
		ymin = 0;
		ymax = 720;
	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	// Get the cursor
	public Point2D.Double	getCursor()
	{
		if (cursor == null)
			return null;
		else
			return new Point2D.Double(cursor.x, cursor.y);
	}

	// Get the list of nodes
	public List<Node>	getNodes()
	{
		return graph.getNodes();
	}

	// Get the list of edges
    public List<Edge>	getEdges()
	{
		return graph.getEdges();
	}

	public Graph		getGraph()
	{
		return graph;
	}

	// Get the path
	public List<SearchNode>   getPath()
    {
        return path;
    }

	// Get the path type
	public boolean[] 	getPathType()
	{
		return pathType;
	}

	// Get the start node
    public Node         getStart() 
    {
        return graph.getStart();
    }

	// Get the end node
	public Node         getEnd() 
    {
        return graph.getEnd();
    }

	// Get the screen pan
	public Point2D.Double	getPan()
	{
		return (Point2D.Double) pan.clone();
	}

	// Get the zoom level
	public double	getZoom()
	{
		return zoom;
	}

	// Get the animation speed
	public double getSpeed()
	{
		return speed;
	}

	// Get the pause input
	public int getPause()
	{
		return pause;
	}

	// Get the screen projection
	public double[] getProjection()
	{
		double[] projection = {xmin, xmax, ymin, ymax};
		return projection;
	}

	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************

	// Load default graph
	public void defaultGraph(){
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl){
				if (path == null) {
					graph.defaultGraph();
				}
			}
		});
	}

	// Load random graph
	public void randomGraph(){
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl){
				if (path == null) {
					graph.randomGraph();
				}
			}
		});
	}

	// Change the algorithm
	public void changeMode(String mode){
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl){
				if (!mode.equals("Shortest-Path")) {
					setEnd(-1);
				}
				currentMode = mode;
			}
		});
	}

	// Add a node to the graph
	public void addNode(Node n) {
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				if (path == null) {
					graph.addNode(n);
				}
			}
		});
	}

	// Add an edge to the graph
	public void addEdge(Edge e) {
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				if (path == null) {
					graph.addEdge(e);
				}
			}
		});
	}

	// Add a list of nodes to the graph
    public void addNodes(List<Node> nodes) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				if (path == null) {
					graph.addNodes(nodes);
				}
			}
		});
    }

	// Add a list of edges to the graph
	public void addEdges(List<Edge> edges) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				if (path == null) {
					graph.addEdges(edges);
				}
			}
		});
    }

	// Set the start node of the graph
    public void setStart(int idx) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
                if (path != null) {
                    return;
                }
				graph.setStart(idx);
			}
		});
    }

	// Find the distance between two points
	private double distance(Point2D.Double a, Point2D.Double b) 
	{
		double dist = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
		return dist;
	}

	// Handle a mouse click
	public void handleClick(Point mouse, boolean shift) {
		view.getCanvas().invoke(false, new ViewPointUpdater(mouse) {
			public void	update(double[] p) {
				// Loop over nodes, keeping track of index
                int i = 0;
				for (Node n: graph.getNodes()) {
					// Find the distance between mouse click and current node
					Point2D.Double newP = new Point2D.Double(p[0], p[1]);
					Point2D.Double node_new = new Point2D.Double(n.getX() + pan.x, n.getY() + pan.y);
					double dist = distance(node_new, newP);

					// If the click occurred inside the node, handle it
					if (dist <= n.getRadius()) {
						// If shift is down, set the current node to the end
						if (shift && currentMode.equals("Shortest-Path")) {
							setEnd(i);
						}
						// Otherwise set the current node to the start
						else {
							setStart(i);
						}
						// Exit method
						return;
					}
					++i;
				}
			}
		});
	}

	// Set the screen pan
	public void setPan(double x, double y) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				pan = new Point2D.Double(x, y);
			}
		});
    }

	// Set the zoom and update projection variables
	public void setZoom(double x) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				zoom = zoom * x;
				xmin = (xmin - cursor.x) * x + cursor.x;
				xmax = (xmax - cursor.x) * x + cursor.x;
				ymin = (ymin - cursor.y) * x + cursor.y;
				ymax = (ymax - cursor.y) * x + cursor.y;
			}
		});
    }

	// Set the animation speed
	public void setSpeed(double x) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				speed = x;
			}
		});
    }

	// Toggle the pause functionality
	public void togglePause() {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				if (pause == 1) {
					pause = 0;
				}
				else {
					pause = 1;
				}
			}
		});
    }

	// Set the end node
	public void setEnd(int idx) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
                if (path != null) {
                    return;
                }
				graph.setEnd(idx);
			}
		});
    }

	// Start the currently selected algorithm
	public void start(){
		switch(currentMode){
			case "Breadth-First-Search": BFS();break;
			case "Depth-First-Search": DFS();break;
			case "Shortest-Path": shortestPath();break;
			default: System.out.println("Please input a valid mode before starting.");break;
		}
	}

	// Perform breadth first search
    public void BFS() {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				pathType[0] = true;
			}
		});
    }

	// Perform depth first search
	public void DFS() {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				pathType[1] = true;
				path = graph.DFS();
			}
		});
    }

	// Perform shortest path
	public void shortestPath() {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				pathType[2] = true;
				path = graph.shortestPathWrapper();
			}
		});
    }

	// Clear the path
    public void clearPath() {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				for (int i = 0; i < pathType.length; ++i) {
					pathType[i] = false;
				}
				path = null;
				view.pathCounter = 0;
			}
		});
    }

	// Reset the transformations
	public void resetTransform() {
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				xmin = 0;
				xmax = 1280;
				ymin = 0;
				ymax = 720;
				pan.x = 0;
				pan.y = 0;
				zoom = 1;
			}
		});
	}

	// Set the cursor point
	public void	setCursorInViewCoordinates(Point q)
	{
		if (q == null)
		{
			view.getCanvas().invoke(false, new BasicUpdater() {
					public void	update(GL2 gl) {
						cursor = null;
					}
				});;
		}
		else
		{
			view.getCanvas().invoke(false, new ViewPointUpdater(q) {
					public void	update(double[] p) {
						cursor = new Point2D.Double(p[0], p[1]);
					}
				});;
		}
	}

	//**********************************************************************
	// Inner Classes
	//**********************************************************************

	// Convenience class to simplify the implementation of most updaters.
	private abstract class BasicUpdater implements GLRunnable
	{
		public final boolean	run(GLAutoDrawable drawable)
		{
			GL2	gl = drawable.getGL().getGL2();

			update(gl);

			return true;	// Let animator take care of updating the display
		}

		public abstract void	update(GL2 gl);
	}

	// Convenience class to simplify updates in cases in which the input is a
	// single point in view coordinates (integers/pixels).
	private abstract class ViewPointUpdater extends BasicUpdater
	{
		private final Point	q;

		public ViewPointUpdater(Point q)
		{
			this.q = q;
		}

		public final void	update(GL2 gl)
		{
			int		h = view.getHeight();
			double[]	p = edu.ou.cs.cg.utilities.Utilities.mapViewToScene(gl, q.x, h - q.y, 0.0);

			update(p);
		}

		public abstract void	update(double[] p);
	}

	//**********************************************************************
	// Coordinate Translators
	//**********************************************************************

	public Point2D.Double translateCoordsToScene(Point2D.Double p) {
		return new Point2D.Double((p.x + 1) * 640, (p.y + 1) * 360);
	}

	public Point2D.Double translateSceneToCoords(Point2D.Double p) {
		return new Point2D.Double((p.x / 640) - 1, (p.y / 360) - 1);
	}

	public Point2D.Double translateScreenToCoords(Point p) {
		double w = view.getWidth();
		double h = view.getHeight();
		return new Point2D.Double((p.getX() / w * 2) - 1, ((h - p.getY()) / h * 2) - 1);
	}

    public Point2D.Double translateScreenToScene(Point p) {
        double w = view.getWidth();
		double h = view.getHeight();
		return new Point2D.Double((p.getX() / w) * 1280, ((h - p.getY()) / h ) * 720);
    }
}

//******************************************************************************
