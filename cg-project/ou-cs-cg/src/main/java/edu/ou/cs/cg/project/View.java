package edu.ou.cs.cg.project;

//import java.lang.*;
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

//******************************************************************************

/**
 * The <CODE>View</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
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
	private int						k;	// Frame counter

    private boolean init = false;

	private float				defaultLine = 1.0f;		// normal thickness
	private float				edgeLine = 2.5f;		// edge thickness

	private final Model				model;

	private final KeyHandler			keyHandler;
	private final MouseHandler			mouseHandler;

	//**********************************************************************
	// Private Scene Members
	//**********************************************************************

	private float				defaultline = 1.0f;		// normal thickness
	private float				thickline = 2.5f;		// bold thickness

    public double                 pathCounter = 0;

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
		k = 0;
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
		k++;							// Advance animation counter

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

		setProjection(gl);							// Based off model
        
		// Draw the graph without any animation
        drawEdges(gl);

		// Draw the nodes
        drawNodes(gl);

		// Get the path type from the model
		boolean[] pathType = model.getPathType();

		List<SearchNode> reached = null;

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
			reached = drawShortestPath(gl, path);
		}

		// Draw the reached nodes
        if (reached != null) {
            drawReached(gl, reached);
        }

		drawMode(drawable);						// Draw mode text

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

		// Draw each edge in the model
        gl.glBegin(GL.GL_LINES);
        for (Edge e: model.getEdges()) {
            gl.glVertex2d(e.getNode1().getX() + pan.x, e.getNode1().getY() + pan.y);
            gl.glVertex2d(e.getNode2().getX() + pan.x, e.getNode2().getY() + pan.y);
        }
        gl.glEnd();

		// Reset the line width
        gl.glLineWidth(defaultLine);
    }

	private void drawWeights(GL2 gl) {
		

		for (Edge e: model.getEdges()) {
			// Get the vector of the edge
			double vx = e.getNode2().getX() - e.getNode1().getX();
			double vy = e.getNode2().getY() - e.getNode1().getY();
			double vs = Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));

			// Get the midpoint of the edge
			double mx = (e.getNode2().getX() + e.getNode1().getX()) / 2.0;
			double my = (e.getNode2().getY() + e.getNode1().getY()) / 2.0;

			double offset = 15;
			double px = (-vy / vs) * offset;
			double py = (vx / vs) * offset;

			double x = mx + px;
			double y = my + py;

			Point2D.Double pan = model.getPan();
			double panx = pan.getX();
			double pany = pan.getY();
			x += panx;
			y += pany;

			double[] newPoints = Utilities.mapSceneToView(gl, x, y, 0.0);

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
            Point2D.Double p = n.getPoint();
            fillCircle(gl, p.x + pan.x, p.y + pan.y, n.getRadius());
            edgeCircle(gl, p.x + pan.x, p.y + pan.y, n.getRadius());
        }
    }

	public boolean contains(Collection<Node> list, Node node) {
		for (Node n: list) {
			if (n.equals(node)) {
				return true;
			}
		}
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
		int numEdgesToDraw = ((int) pathCounter / 121) + 1;
		double proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;

		// Keep track of number of edges drawn
		int numDrawn = 0;

		// Set the width of edges
		gl.glLineWidth(edgeLine);

		// Keep track of the BFS state
		Node previous = null;
		boolean broke = false;

        // Loop over the queue until it is empty
        while (numDrawn < numEdgesToDraw && !q.isEmpty()) {
            // Get the front of the queue
            Node n = q.poll();

			// Draw the node, indicating it has been visited
			setColor(gl, 0, 255, 255);
			fillCircle(gl, n.getX(), n.getY(), n.getRadius());

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
						// If the connected node had been seen, set the color to cyan
						else {
							setColor(gl, 0, 255, 255);
						}

						// Draw the connected node
						fillCircle(gl, n2.getX(), n2.getY(), n2.getRadius());
					}
					// If this edge is not the last one to be drawn, draw it if the connected node has not been seen
					else {
						// Draw the edge if the connected node has not been seen
						if (!contains(seen, n2)) {
							// Draw the whole edge in green
							setColor(gl, 0, 255, 0);
							gl.glBegin(GL.GL_LINES);
							gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
							gl.glVertex2d(n2.getX() + pan.x, n2.getY() + pan.y);
							gl.glEnd();

							// Draw the current node in cyan
							setColor(gl, 0, 255, 255);
							fillCircle(gl, n.getX(), n.getY(), n.getRadius());

							// Draw the connected node in purple
							setColor(gl, 255, 0, 255);
							fillCircle(gl, n2.getX(), n2.getY(), n2.getRadius());

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
						// If the connected node had been seen, set the color to cyan
						else {
							setColor(gl, 0, 255, 255);
						}

						// Draw the connected node
						fillCircle(gl, n1.getX(), n1.getY(), n1.getRadius());
					}
					// If this edge is not the last one to be drawn, draw it if the connected node has not been seen
					else {
						// Draw the edge if the connected node has not been seen
						if (!contains(seen, n1)) {
							// Draw the whole edge in green
							setColor(gl, 0, 255, 0);
							gl.glBegin(GL.GL_LINES);
							gl.glVertex2d(n.getX() + pan.x, n.getY() + pan.y);
							gl.glVertex2d(n1.getX() + pan.x, n1.getY() + pan.y);
							gl.glEnd();

							// Draw the current node in cyan
							setColor(gl, 0, 255, 255);
							fillCircle(gl, n.getX(), n.getY(), n.getRadius());

							// Draw the connected node in purple
							setColor(gl, 255, 0, 255);
							fillCircle(gl, n1.getX(), n1.getY(), n1.getRadius());

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

		// If the algorithm was not allowed to complete, draw the previous node in black
		if (previous != null && broke) {
			setColor(gl, 0, 0, 0);
			fillCircle(gl, previous.getX(), previous.getY(), previous.getRadius());
		}

		// Update the path counter
		pathCounter += pause * speed;
	}

	// Draw DFS
	public void drawDFS(GL2 gl) {











		
		// // Declare array of reached nodes
		// ArrayList<SearchNode> reached = new ArrayList<>();

		// // Find number of nodes to draw
		// int numNodesToDraw = ((int) pathCounter / 121) + 1;
		// double proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;

		// // Declare variables for cyan to purple gradient
		// int maxDepth = path.get(path.size() - 1).depth - 1;
		// int rgbIncrement = 255 / maxDepth;
		// int[] nodeColor = {0, 255, 255};
		
		// gl.glLineWidth(edgeLine);				// set the line width to the default
		// gl.glBegin(GL.GL_LINES);
		// for (int i = 1, j = 1; i < numNodesToDraw; ++i, ++j) {
		// 	if (j >= path.size()) {
		// 		break;
		// 	}
		// 	else if (i == numNodesToDraw - 1) {
		// 		int depth = path.get(i).depth;
		// 		setColor(gl, nodeColor[0] + (depth - 1) * rgbIncrement, nodeColor[1] - (depth - 1) * rgbIncrement, nodeColor[2]);
		// 		path.get(i).parent.isLeaf = false;
		// 		gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
		// 		double xVector = path.get(i).node.getX() - path.get(i).parent.node.getX();
		// 		double yVector = path.get(i).node.getY() - path.get(i).parent.node.getY();
		// 		gl.glVertex2d(path.get(i).parent.node.getX() + xVector * proportionOfFinalEdge + pan.x, 
		// 			path.get(i).parent.node.getY() + yVector * proportionOfFinalEdge + pan.y);
		// 	}
		// 	else {
		// 		int depth = path.get(i).depth;
		// 		setColor(gl, nodeColor[0] + (depth - 1) * rgbIncrement, nodeColor[1] - (depth - 1) * rgbIncrement, nodeColor[2]);
		// 		path.get(i).parent.isLeaf = false;
		// 		gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
		// 		gl.glVertex2d(path.get(i).node.getX() + pan.x, path.get(i).node.getY() + pan.y);
		// 		reached.add(path.get(i));
		// 	}
		// }
		// gl.glEnd();
		// gl.glLineWidth(defaultLine);
		// pathCounter += pause * speed;
		// return reached;
	}

	// Draw shortest path
	public List<SearchNode> drawShortestPath(GL2 gl, List<SearchNode> path) {
		// int i = 0;
		// for (SearchNode sn: path) {
		// 	System.out.println("Path[" + i + "] = (" + sn.node.getX() + ", " + sn.node.getY() + ")");
		// 	++i;
		// }
		drawWeights(gl);
		// Declare array of reached nodes
		ArrayList<SearchNode> reached = new ArrayList<>();

		// Find number of nodes to draw
		int numNodesToDraw = ((int) pathCounter / 121) + 1;
		double proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;

		// Declare variables for cyan to purple gradient
		int maxDepth = path.get(path.size() - 1).depth - 1;
		int rgbIncrement = 255 / maxDepth;
		int[] nodeColor = {0, 255, 255};
		
		gl.glLineWidth(edgeLine);				// set the line width to the default
		gl.glBegin(GL.GL_LINES);
		for (int i = 1, j = 1; i < numNodesToDraw; ++i, ++j) {
			if (j >= path.size()) {
				break;
			}
			else if (i == numNodesToDraw - 1) {
				int depth = path.get(i).depth;
				setColor(gl, nodeColor[0] + (depth - 1) * rgbIncrement, nodeColor[1] - (depth - 1) * rgbIncrement, nodeColor[2]);
				path.get(i).isLeaf = false;
				gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
				double xVector = path.get(i).node.getX() - path.get(i).parent.node.getX();
				double yVector = path.get(i).node.getY() - path.get(i).parent.node.getY();
				gl.glVertex2d(path.get(i).parent.node.getX() + xVector * proportionOfFinalEdge + pan.x, 
					path.get(i).parent.node.getY() + yVector * proportionOfFinalEdge + pan.y);
			}
			else {
				int depth = path.get(i).depth;
				setColor(gl, nodeColor[0] + (depth - 1) * rgbIncrement, nodeColor[1] - (depth - 1) * rgbIncrement, nodeColor[2]);
				path.get(i).isLeaf = false;
				gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
				gl.glVertex2d(path.get(i).node.getX() + pan.x, path.get(i).node.getY() + pan.y);
				reached.add(path.get(i));
			}
		}
		gl.glEnd();
		gl.glLineWidth(defaultLine);
		pathCounter += pause * speed;
		return reached;
	}

	// Draw a list of reached nodes
    private void drawReached(GL2 gl, List<SearchNode> reached) {
		// // Draw the reached nodes, if any
		// if (reached.size() > 0) {
		// 	// Declare variables for cyan to purple gradient
		// 	int rgbIncrement = 255 / maxDepth;
		// 	int[] nodeColor = {0, 255, 255};

		// 	// Draw each node
        // 	for (SearchNode n: reached) {
		// 		// Set the color based on the depth
		// 		int depth = n.depth;
		// 		setColor(gl, nodeColor[0] + depth * rgbIncrement, nodeColor[1] - depth * rgbIncrement, nodeColor[2]);

		// 		// Draw a circle for the reached node
		// 		fillCircle(gl, n.node.getX() + pan.x, n.node.getY() + pan.y, n.node.getRadius());
		// 		edgeCircle(gl, n.node.getX() + pan.x, n.node.getY() + pan.y, n.node.getRadius());
		// 	}
		// }

		// int maxDepth = 0;
		// if (reached.size() > 0) {
		// 	maxDepth = reached.get(reached.size() - 1).depth;
		// }
		// Draw each node

		for (SearchNode n: reached) {
			// if (n.depth == maxDepth) {
			// 	setColor(gl, 255, 255, 0);
			// }
			if (n.isLeaf) {
				setColor(gl, 255, 255, 0);
			}
			else {
				setColor(gl, 0, 255, 0);
			}

			// Draw a circle for the reached node
			fillCircle(gl, n.node.getX() + pan.x, n.node.getY() + pan.y, n.node.getRadius());
			edgeCircle(gl, n.node.getX() + pan.x, n.node.getY() + pan.y, n.node.getRadius());
		}
    }

	// Draw text
	private void	drawMode(GLAutoDrawable drawable)
	{
		GL2		gl = drawable.getGL().getGL2();

		renderer.beginRendering(w, h);

		// Draw all text in black
		renderer.setColor(0.f, 0.f, 0.f, 1.0f);

		Point2D.Double	cursor = model.getCursor();

		// Draw the cursor location
		if (cursor != null)
		{
			String		sx = FORMAT.format(new Double(cursor.x));
			String		sy = FORMAT.format(new Double(cursor.y));
			String		s = "Pointer at (" + sx + "," + sy + ")";

			renderer.draw(s, 2, 2);
		}
		else
		{
			renderer.draw("No Pointer", 2, 2);
		}

		renderer.endRendering();
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

	// edges a circle defined by the center and radius
	private void edgeCircle(GL2 gl, double cx, double cy, double r) {
		edgeEllipse(gl, cx, cy, r, r);
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

	// edges an ellipse defined by the center, x radius, and y radius
	private void edgeEllipse(GL2 gl, double cx, double cy, double a, double b) {
		gl.glBegin(GL.GL_LINE_LOOP);
		for (int i = 0; i < 32; i++) {
			double angle = i * 2.0 * Math.PI / 32;
			gl.glVertex2d(cx + (a * Math.cos(angle)), cy + (b * Math.sin(angle)));
		}
		gl.glEnd();
	}
}

//******************************************************************************