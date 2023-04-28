package edu.ou.cs.cg.project;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import edu.ou.cs.cg.utilities.Utilities;

public final class View
	implements GLEventListener
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final int			DEFAULT_FRAMES_PER_SECOND = 60;
	private static final DecimalFormat	FORMAT = new DecimalFormat("0.000");

	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final GLUT			MYGLUT = new GLUT();
	public static final Random			RANDOM = new Random();

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final GLJPanel				canvas;
	private int						w;			// Canvas width
	private int						h;			// Canvas height

	private TextRenderer				renderer;
	private final FPSAnimator			animator;

	private final Model				model;

	private final KeyHandler			keyHandler;
	private final MouseHandler			mouseHandler;

	//**********************************************************************
	// Private Scene Members
	//**********************************************************************

	// Edge thicknesses
	private float				edgeLine = 2.5f;

	// Animation counters
    public double               pathCounter = 0;
	public int					numEdgesToDraw = 0;
	public double				proportionOfFinalEdge = 0;
	public int					numDrawn = 0;

	// Model animation variables
	private Point2D.Double 		pan;
	private double				zoom;
	private double				speed;
	private int					pause;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public View(GLJPanel canvas, JComboBox<String> cb, JButton start, JButton random)
	{
		this.canvas = canvas;

		// Initialize rendering
		canvas.addGLEventListener(this);

		// Initialize model (scene data and parameter manager)
		model = new Model(this);
		cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				model.changeMode(cb.getSelectedItem().toString());
			}
		}); 

		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				model.start();
			}
		});

		random.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				model.randomGraph();
			}
		});

		// Initialize controller (interaction handlers)
		keyHandler = new KeyHandler(this, model);
		mouseHandler = new MouseHandler(this, model);

		// Initialize animation
		animator = new FPSAnimator(canvas, DEFAULT_FRAMES_PER_SECOND);
		animator.start();

		// Initialize model variables controlling animation
		pan = model.getPan();
		zoom = model.getZoom();
		speed = model.getSpeed();
		pause = model.getPause();

		// Initialize the graph
		model.defaultGraph();
	}

	//**********************************************************************
	// Getters and Setters
	//**********************************************************************

	public GLJPanel	getCanvas()
	{
		return canvas;
	}

	public int	getWidth()
	{
		return w;
	}

	public int	getHeight()
	{
		return h;
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Monospaced", Font.PLAIN, 12),
									true, true);

		initPipeline(drawable);
	}

	public void	dispose(GLAutoDrawable drawable)
	{
		
	}

	public void	display(GLAutoDrawable drawable)
	{
		update(drawable);
		render(drawable);
	}

	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************

	private void	update(GLAutoDrawable drawable)
	{
		// Update the model animation variables
		pan = model.getPan();
		zoom = model.getZoom();
		speed = model.getSpeed();
		pause = model.getPause();
	}

	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);	// White background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);			// Clear the buffer

		// Set projection based on model
		setProjection(gl);
        
		// Draw the edges
        drawEdges(gl);

		if (model.getWeights()) {
			drawWeights(gl);
		}

		// Draw the nodes
        drawNodes(gl);

		// Get the path type from the model
		boolean[] pathType = model.getPathType();

		// If the path is BFS, draw it
		if (pathType[0]) {
			drawBFS(gl);
		}
		// If the path is DFS, draw it
		else if (pathType[1]) {
			drawDFS(gl);
		}
		// If the path is shortest path, draw it
		else if (pathType[2]) {
			List<SearchNode> path = model.getPath();
			drawShortestPath(gl, path);
		}

		gl.glFlush();								// Finish and display
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	private void	initPipeline(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		// Make points easier to see on Hi-DPI displays
		gl.glEnable(GL2.GL_POINT_SMOOTH);	// Turn on point anti-aliasing
	}

    // Position and orient the default camera to view in 2-D, in pixel coords.
	private void	setProjection(GL2 gl)
	{
		GLU	glu = GLU.createGLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);		// Prepare for matrix xform
		gl.glLoadIdentity();						// Set to identity matrix

		// Get the projection from the model and apply it
		double[] projection = model.getProjection();
		glu.gluOrtho2D(projection[0], projection[1], projection[2], projection[3]);
	}

    //**********************************************************************
	// Private Methods (Graph Functions)
	//**********************************************************************
	// Draw the edges
    private void drawEdges(GL2 gl) {
		// Set the color and thickness of edges, based on model animation variables
        setColor(gl, 0, 0, 0);
        gl.glLineWidth(edgeLine / (float) zoom);

		// Draw a line for each edge in the model
        gl.glBegin(GL.GL_LINES);
        for (Edge e: model.getEdges()) {
            gl.glVertex2d(e.getNode1().getX() + pan.x, e.getNode1().getY() + pan.y);
            gl.glVertex2d(e.getNode2().getX() + pan.x, e.getNode2().getY() + pan.y);
        }
        gl.glEnd();
    }

	// Draw the weights for each edge
	private void drawWeights(GL2 gl) {
		// Loop over each edge
		for (Edge e: model.getEdges()) {
			// Get the vector of the edge
			double vx = e.getNode2().getX() - e.getNode1().getX();
			double vy = e.getNode2().getY() - e.getNode1().getY();
			double vs = Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));

			// Get the midpoint of the edge
			double mx = (e.getNode2().getX() + e.getNode1().getX()) / 2.0;
			double my = (e.getNode2().getY() + e.getNode1().getY()) / 2.0;

			// Find the normal vector offset
			double offset = 15;
			double px = (-vy / vs) * offset;
			double py = (vx / vs) * offset;

			// Calculate position of weight text in scene coordinates
			double x = mx + px;
			double y = my + py;

			// Adjust the coordinates for the pan
			x += pan.x;
			y += pan.y;

			// Get the position of weight text in screen coordinates
			double[] newPoints = Utilities.mapSceneToView(gl, x, y, 0.0);

			// Draw the weight in black
			renderer.beginRendering(w, h);
			renderer.setColor(0.f, 0.f, 0.f, 1.0f);
			renderer.draw("" + e.getWeight(), (int)newPoints[0], (int)newPoints[1]);
			renderer.endRendering();
		}
	}

	// Draw the nodes
    private void drawNodes(GL2 gl) {
		// Draw each node in the model
        for (Node n: model.getNodes()) {
			// If the node is the start, draw it blue
            if (n.isStart()) {
                setColor(gl, 0, 0, 255);
            }
			// If the node is the end, draw it purple
			else if (n.isEnd()) {
				setColor(gl, 255, 0, 255);
			}
			// Otherwise, draw the node red
            else {
                setColor(gl, 255, 0, 0);
            }

			// Draw a circle for the node
            fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());
        }
    }

	// Define method that determines if a collection contains a node
	public boolean contains(Collection<Node> list, Node node) {
		// Loop over collection
		for (Node n: list) {
			// If the given node is the same as the current node, return true
			if (n.equals(node)) {
				return true;
			}
		}

		// Return false if the given node was not found
		return false;
	}

	// Draw BFS
	public void drawBFS(GL2 gl) {
		// Get the graph
		Graph graph = model.getGraph();

		// If the start is not specified or there are no nodes, return null
        if (graph.start == -1 || graph.nodes.size() == 0) {
            return;
        }

        // Get the start node
        Node root = graph.getStart();

        // Create lists to hold seen nodes and the queue of nodes. Add root to both
        ArrayList<Node> seen = new ArrayList<>();
        Queue<Node> q = new LinkedList<>();
        q.add(root);
        seen.add(root);

		// Get number of edges to draw
		numEdgesToDraw = ((int) pathCounter / 121) + 1;
		proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;

		// Keep track of number of edges drawn
		numDrawn = 0;

		// Set the width of edges
		gl.glLineWidth(edgeLine / (float) zoom);

		// Keep track of the BFS state
		Node previous = null;
		boolean broke = false;

        // Loop over the queue until it is empty
        while (numDrawn < numEdgesToDraw && !q.isEmpty()) {
            // Get the front of the queue
            Node n = q.poll();

			// Draw the current node in purple
			setColor(gl, 0, 255, 255);
			fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());

            // Loop over each edge to find if it is adjacent to current node
            for (Edge e: graph.edges){
                // If the first side of the edge is the current node, procede
                if (n.equals(e.getNode1())) {
                    // Get the connected node
                    Node n2 = e.getNode2();

					// If this edge is the last one to be drawn, draw a portion of it
					if (numDrawn == numEdgesToDraw - 1) {
						// Draw the edge portion in yellow
						setColor(gl, 255, 255, 0);
						gl.glBegin(GL.GL_LINES);
						gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
						double xVector = n2.getX() - n.getX();
						double yVector = n2.getY() - n.getY();
						gl.glVertex2d(n.getX() + xVector * proportionOfFinalEdge + pan.x, 
							n.getY() + yVector * proportionOfFinalEdge + pan.y);
						gl.glEnd();

						// If the connected node is in the queue, set the color to purple
						if (contains(q, n2)) {
							setColor(gl, 255, 0, 255);
						}
						// If the connected node has not been seen, set the color to red
						else if (!contains(seen, n2)) {
							setColor(gl, 255, 0, 0);
						}
						// If the connected node has been seen, set the color to cyan
						else {
							setColor(gl, 0, 255, 255);
						}

						// Draw the connected node
						fillCircle(gl, n2.getX() + pan.x, n2.getY() + pan.y, n2.getRadius());
					}
					// If this edge is not the last one to be drawn, draw it if the connected node has not been seen
					else {
						if (!contains(seen, n2)) {
							// Draw the whole edge in green
							setColor(gl, 0, 255, 0);
							gl.glBegin(GL.GL_LINES);
							gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
							gl.glVertex2d(n2.getX() + pan.x, n2.getY() + pan.y);
							gl.glEnd();

							// Draw the current node in cyan
							setColor(gl, 0, 255, 255);
							fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());

							// Draw the connected node in purple
							setColor(gl, 255, 0, 255);
							fillCircle(gl, n2.getX() + pan.x, n2.getY() + pan.y, n2.getRadius());

							// Add the connected node to the q and seen nodes
							q.add(n2);
							seen.add(n2);
						}
					}

					// Indicate another edge has been drawn
					++numDrawn;

					// If all edges have been drawn, break out of loop
					if (numDrawn >= numEdgesToDraw) {
						broke = true;
						break;
					}
                }
                // If the second side of the edge is the current node, procede
                else if (n.equals(e.getNode2())) {
                    // Get the connected node
                    Node n1 = e.getNode1();

					// If this edge is the last one to be drawn, draw a portion of it
                    if (numDrawn == numEdgesToDraw - 1) {
						// Draw the edge portion in yellow
						setColor(gl, 255, 255, 0);
						gl.glBegin(GL.GL_LINES);
						gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
						double xVector = n1.getX() - n.getX();
						double yVector = n1.getY() - n.getY();
						gl.glVertex2d(n.getX() + xVector * proportionOfFinalEdge + pan.x, 
							n.getY() + yVector * proportionOfFinalEdge + pan.y);
						gl.glEnd();

						// If the connected node is in the queue, set the color to purple
						if (contains(q, n1)) {
							setColor(gl, 255, 0, 255);
						}
						// If the connected node has not been seen, set the color to red
						else if (!contains(seen, n1)) {
							setColor(gl, 255, 0, 0);
						}
						// If the connected node has been seen, set the color to cyan
						else {
							setColor(gl, 0, 255, 255);
						}

						// Draw the connected node
						fillCircle(gl, n1.getX() + pan.x, n1.getY() + pan.y, n1.getRadius());
					}
					// If this edge is not the last one to be drawn, draw it if the connected node has not been seen
					else {
						if (!contains(seen, n1)) {
							// Draw the whole edge in green
							setColor(gl, 0, 255, 0);
							gl.glBegin(GL.GL_LINES);
							gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
							gl.glVertex2d(n1.getX() + pan.x, n1.getY() + pan.y);
							gl.glEnd();

							// Draw the current node in cyan
							setColor(gl, 0, 255, 255);
							fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());

							// Draw the connected node in purple
							setColor(gl, 255, 0, 255);
							fillCircle(gl, n1.getX() + pan.x, n1.getY() + pan.y, n1.getRadius());

							// Add the connected node to the q and seen nodes
							q.add(n1);
							seen.add(n1);
						}
					}

					// Indicate another edge has been drawn
					++numDrawn;

					// If all edges have been drawn, break out of loop
					if (numDrawn >= numEdgesToDraw) {
						broke = true;
						break;
					}
                }
            }

			// Update the previous node
			previous = n;
        }

		// If the algorithm was not allowed to complete, draw the previous node in dark purple
		if (previous != null && broke) {
			setColor(gl, 75, 0, 130);
			fillCircle(gl, previous.getX() + pan.x, previous.getY() + pan.y, previous.getRadius());
		}

		// Update the path counter
		pathCounter += pause * speed;
	} 

	// Recursive function for DFS
    public void recursiveDFS(GL2 gl, Node n, List<Node> seen, List<Node> finished, Graph graph) {
		// If the edge limit is not reached, draw the current node in purple
		if (numDrawn < numEdgesToDraw) {
			setColor(gl, 255, 0, 255);
			fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());
		}

        // Loop over edges to find connected nodes
        for (Edge e: graph.edges) {
			// If all edge limit has been reached, return
			if (numDrawn >= numEdgesToDraw) {
				return;
			}

            // If the current node is the first node of the edge procede
            if (n.equals(e.getNode1())) {
                // Get the connected node
				Node n2 = e.getNode2();
                
				// If this edge is the last one to be drawn, draw a portion of it
				if (numDrawn == numEdgesToDraw - 1) {
					// Draw the edge portion in yellow
					setColor(gl, 255, 255, 0);
					gl.glBegin(GL.GL_LINES);
					gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
					double xVector = n2.getX() - n.getX();
					double yVector = n2.getY() - n.getY();
					gl.glVertex2d(n.getX() + xVector * proportionOfFinalEdge + pan.x, 
						n.getY() + yVector * proportionOfFinalEdge + pan.y);
					gl.glEnd();

					// Draw the current node in purple
					setColor(gl, 255, 0, 255);
					fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());

					// If the connected node has finished, set the color to cyan
					if (contains(finished, n2)) {
						setColor(gl, 0, 255, 255);
					}
					// If the connected node has not been seen, set the color to red
					else if (!contains(seen, n2)) {
						setColor(gl, 255, 0, 0);
					}
					// If the connected node had been seen, set the color to purple
					else {
						setColor(gl, 255, 0, 255);
					}

					// Draw the connected node
					fillCircle(gl, n2.getX() + pan.x, n2.getY() + pan.y, n2.getRadius());
				}
				// If this edge is not the last one to be drawn, draw it if the connected node has not been seen
				else {
					if (!contains(seen, n2)) {
						// Draw the whole edge in green
						setColor(gl, 0, 255, 0);
						gl.glBegin(GL.GL_LINES);
						gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
						gl.glVertex2d(n2.getX() + pan.x, n2.getY() + pan.y);
						gl.glEnd();

						// Draw both ends of the edge in purple
						setColor(gl, 255, 0, 255);
						fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());
						fillCircle(gl, n2.getX() + pan.x, n2.getY() + pan.y, n2.getRadius());
					}
				}

				// Indicate another edge has been drawn
				++numDrawn;

				// If the connected node has not been seen, recursively call function on it
				if (!contains(seen, n2)) {
					seen.add(n2);
					recursiveDFS(gl, n2, seen, finished, graph);
				}
            }
            // If the current node is the second node of the edge procede
            else if (n.equals(e.getNode2())) {
                // Get the connected node
				Node n1 = e.getNode1();
                
				// If this edge is the last one to be drawn, draw a portion of it
				if (numDrawn == numEdgesToDraw - 1) {
					// Draw the edge portion in yellow
					setColor(gl, 255, 255, 0);
					gl.glBegin(GL.GL_LINES);
					gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
					double xVector = n1.getX() - n.getX();
					double yVector = n1.getY() - n.getY();
					gl.glVertex2d(n.getX() + xVector * proportionOfFinalEdge + pan.x, 
						n.getY() + yVector * proportionOfFinalEdge + pan.y);
					gl.glEnd();

					// Draw the current node in purple
					setColor(gl, 255, 0, 255);
					fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());

					// If the connected node has finished, set the color to cyan
					if (contains(finished, n1)) {
						setColor(gl, 0, 255, 255);
					}
					// If the connected node has not been seen, set the color to red
					else if (!contains(seen, n1)) {
						setColor(gl, 255, 0, 0);
					}
					// If the connected node had been seen, set the color to purple
					else {
						setColor(gl, 255, 0, 255);
					}

					// Draw the connected node
					fillCircle(gl, n1.getX() + pan.x, n1.getY() + pan.y, n1.getRadius());
				}
				// If this edge is not the last one to be drawn, draw it if the connected node has not been seen
				else {
					if (!contains(seen, n1)) {
						// Draw the whole edge in green
						setColor(gl, 0, 255, 0);
						gl.glBegin(GL.GL_LINES);
						gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
						gl.glVertex2d(n1.getX() + pan.x, n1.getY() + pan.y);
						gl.glEnd();

						// Draw the endpoints of the edge in purple
						setColor(gl, 255, 0, 255);
						fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());
						fillCircle(gl, n1.getX() + pan.x, n1.getY() + pan.y, n1.getRadius());
					}
				}

				// Indicate another edge has been drawn
				++numDrawn;

				// If the connected node has not been seen, recursively call function on it
				if (!contains(seen, n1)) {
					seen.add(n1);
					recursiveDFS(gl, n1, seen, finished, graph);
				}
            }
        }

		// If the current node finished, draw it in cyan
		if (numDrawn < numEdgesToDraw) {
			finished.add(n);
			setColor(gl, 0, 255, 255);
			fillCircle(gl, n.getX() + pan.x, n.getY() + pan.y, n.getRadius());
		}
    }

	// Draw DFS
	public void drawDFS(GL2 gl) {
		// Get the graph
		Graph graph = model.getGraph();

		// If the start is not specified or there are no nodes, return null
        if (graph.start == -1 || graph.nodes.size() == 0) {
            return;
        }

        // Get the start node
        Node root = graph.getStart();

        // Create a list to hold seen nodes and add root
        ArrayList<Node> seen = new ArrayList<>();
        seen.add(root);

		// Create a list to hold finished nodes
		ArrayList<Node> finished = new ArrayList<>();

		// Get number of edges to draw
		numEdgesToDraw = ((int) pathCounter / 121) + 1;
		proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;

		// Keep track of number of edges drawn
		numDrawn = 0;

		// Set the width of edges
		gl.glLineWidth(edgeLine / (float) zoom);

        // Perform recursive DFS
        recursiveDFS(gl, root, seen, finished, graph);

		// Update the path counter
		pathCounter += pause * speed;
	}

	// Draw shortest path
	public void drawShortestPath(GL2 gl, List<SearchNode> path) {
		// If the path is null or is empty, return
		if (path == null || path.size() == 0) {
			return;
		}

		// Find number of nodes to draw
		numEdgesToDraw = ((int) pathCounter / 121) + 2;
		proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;

		// Declare variables for cyan to purple gradient
		int maxDepth = path.get(path.size() - 1).depth - 1;
		int rgbIncrement = 255 / maxDepth;
		int[] nodeColor = {0, 255, 255};
		
		// Set the edge width
		gl.glLineWidth(edgeLine / (float) zoom);

		// Incrementally draw the path
		for (int i = 1; i < numEdgesToDraw; ++i) {
			// If the index is larger than the size, break
			if (i >= path.size()) {
				break;
			}
			// If this edge is the last one, draw a portion of it
			else if (i == numEdgesToDraw - 1) {
				// Get the depth of current Search Node and find the color based off of it
				int depth = path.get(i).depth;
				setColor(gl, nodeColor[0] + (depth - 1) * rgbIncrement, nodeColor[1] - (depth - 1) * rgbIncrement, nodeColor[2]);

				// Draw the portion of the edge
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
				double xVector = path.get(i).node.getX() - path.get(i).parent.node.getX();
				double yVector = path.get(i).node.getY() - path.get(i).parent.node.getY();
				gl.glVertex2d(path.get(i).parent.node.getX() + xVector * proportionOfFinalEdge + pan.x, 
					path.get(i).parent.node.getY() + yVector * proportionOfFinalEdge + pan.y);
				gl.glEnd();

				// Draw the parent node in green
				setColor(gl, 0, 255, 0);
				fillCircle(gl, path.get(i).parent.node.getX(), path.get(i).parent.node.getY(), path.get(i).parent.node.getRadius());

				// If the node is the end, set the color to purple
				if (path.get(i).node.isEnd()) {
					setColor(gl, 255, 0, 255);
				}
				// If the node is not the end, set the color to red
				else {
					setColor(gl, 255, 0, 0);
				}
				fillCircle(gl, path.get(i).node.getX(), path.get(i).node.getY(), path.get(i).node.getRadius());
			}
			// If this edge is not the last one, draw the whole thing
			else {
				// Get the depth of current Search Node and find the color based off of it
				int depth = path.get(i).depth;
				setColor(gl, nodeColor[0] + (depth - 1) * rgbIncrement, nodeColor[1] - (depth - 1) * rgbIncrement, nodeColor[2]);

				// Draw the whole edge
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
				gl.glVertex2d(path.get(i).node.getX() + pan.x, path.get(i).node.getY() + pan.y);
				gl.glEnd();

				// Draw both nodes in green
				setColor(gl, 0, 255, 0);
				fillCircle(gl, path.get(i).parent.node.getX(), path.get(i).parent.node.getY(), path.get(i).parent.node.getRadius());
				fillCircle(gl, path.get(i).node.getX(), path.get(i).node.getY(), path.get(i).node.getRadius());
			}
		}
		
		// Update the animation counter
		pathCounter += pause * speed;
	}

	//**********************************************************************
	// Private Methods (Convenience Functions)
	//**********************************************************************

	// Sets color, normalizing r, g, b, a values from max 255 to 1.0.
	private void	setColor(GL2 gl, int r, int g, int b, int a)
	{
		gl.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
	}

	// Sets fully opaque color, normalizing r, g, b values from max 255 to 1.0.
	private void	setColor(GL2 gl, int r, int g, int b)
	{
		setColor(gl, r, g, b, 255);
	}

	// fills a circle defined by the center and radius
	private void fillCircle(GL2 gl, double cx, double cy, double r) {
		fillEllipse(gl, cx, cy, r, r);
	}

	// fills an ellipse defined by the center, x radius, and y radius
	private void fillEllipse(GL2 gl, double cx, double cy, double a, double b) {
		gl.glBegin(GL2.GL_POLYGON);
		for (int i = 0; i < 32; i++) {
			double angle = i * 2.0 * Math.PI / 32;
			gl.glVertex2d(cx + (a * Math.cos(angle)), cy + (b * Math.sin(angle)));
		}
		gl.glEnd();
	}
}

//******************************************************************************