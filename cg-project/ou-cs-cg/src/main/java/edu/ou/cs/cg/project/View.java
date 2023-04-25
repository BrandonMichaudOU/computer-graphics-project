package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

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

    public double                 pathCounter = 120;

	private Point2D.Double 		pan;
	private double				zoom;
	private double				speed;
	private int					pause;


	private int 				maxDepth;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public View(GLJPanel canvas, JComboBox<String> cb)
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

		// Draw the animation path
		List<SearchNode> reached = drawPath(gl);

		// Draw the nodes
        drawNodes(gl);

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
		renderer.beginRendering(w, h);

		// Draw all text in black
		renderer.setColor(0.f, 0.f, 0.f, 1.0f);

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

			renderer.draw("" + e.getWeight(), (int)((x / 1280) * w), (int)((y / 720) * h));
		}
		renderer.endRendering();
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

	// Draw the path
	private List<SearchNode> drawPath(GL2 gl) {
		// Get the path from the model
		List<SearchNode> path = model.getPath();

		// Create a variable for holding reached nodes
		List<SearchNode> reached = null;

		// If there is a path, draw it accordingly
        if (path != null) {
			// Get the path type from the model
			boolean[] pathType = model.getPathType();

			// If the path is BFS, draw it
			if (pathType[0]) {
				reached = drawBFS(gl, path);
			}
			// If the path is DFS, draw it
			else if (pathType[1]) {
				reached = drawDFS(gl, path);
			}
			// If the path is shortest path, draw it
			else if (pathType[2]) {
				reached = drawShortestPath(gl, path);
			}
        }

		// Return the list of reached nodes
		return reached;
	}

	// Draw BFS
	public List<SearchNode> drawBFS(GL2 gl, List<SearchNode> path) {
		ArrayList<SearchNode> reached = new ArrayList<>();
		int numNodesToDraw = ((int) pathCounter / 121) + 1;
		double proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;
		maxDepth = path.get(path.size() - 1).depth;
		gl.glLineWidth(edgeLine);				// set the line width to the default
		gl.glBegin(GL.GL_LINES);
		for (int i = 1, j = 1; i < numNodesToDraw; ++i, ++j) {
			setColor(gl, 0, 255, 0);
			if (j >= path.size()) {
				break;
			}
			else if (i == numNodesToDraw - 1) {
				gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
				double xVector = path.get(i).node.getX() - path.get(i).parent.node.getX();
				double yVector = path.get(i).node.getY() - path.get(i).parent.node.getY();
				gl.glVertex2d(path.get(i).parent.node.getX() + xVector * proportionOfFinalEdge + pan.x, 
					path.get(i).parent.node.getY() + yVector * proportionOfFinalEdge + pan.y);
			}
			else {
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

	// Draw DFS
	public List<SearchNode> drawDFS(GL2 gl, List<SearchNode> path) {
		ArrayList<SearchNode> reached = new ArrayList<>();
		int numNodesToDraw = ((int) pathCounter / 121) + 1;
		double proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;
		maxDepth = path.get(path.size() - 1).depth;
		gl.glLineWidth(edgeLine);				// set the line width to the default
		gl.glBegin(GL.GL_LINES);
		for (int i = 1, j =1; i < numNodesToDraw; ++i, ++j) {
			setColor(gl, 0, 255, 0);
			if (j >= path.size()) {
				break;
			}
			else if (i == numNodesToDraw - 1) {
				gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
				double xVector = path.get(i).node.getX() - path.get(i).parent.node.getX();
				double yVector = path.get(i).node.getY() - path.get(i).parent.node.getY();
				gl.glVertex2d(path.get(i).parent.node.getX() + xVector * proportionOfFinalEdge + pan.x, 
					path.get(i).parent.node.getY() + yVector * proportionOfFinalEdge + pan.y);
			}
			else {
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

	// Draw shortest path
	public List<SearchNode> drawShortestPath(GL2 gl, List<SearchNode> path) {
		// int i = 0;
		// for (SearchNode sn: path) {
		// 	System.out.println("Path[" + i + "] = (" + sn.node.getX() + ", " + sn.node.getY() + ")");
		// 	++i;
		// }
		drawWeights(gl);
		ArrayList<SearchNode> reached = new ArrayList<>();
		int numNodesToDraw = ((int) pathCounter / 121) + 1;
		double proportionOfFinalEdge = ((int) pathCounter % 121) / 120.0;
		maxDepth = path.get(path.size() - 1).depth;
		gl.glLineWidth(edgeLine);				// set the line width to the default
		gl.glBegin(GL.GL_LINES);
		for (int i = 1, j =1; i < numNodesToDraw; ++i, ++j) {
			setColor(gl, 0, 255, 0);
			if (j >= path.size()) {
				break;
			}
			else if (i == numNodesToDraw - 1) {
				gl.glVertex2d(path.get(i).parent.node.getX() + pan.x, path.get(i).parent.node.getY() + pan.y);
				double xVector = path.get(i).node.getX() - path.get(i).parent.node.getX();
				double yVector = path.get(i).node.getY() - path.get(i).parent.node.getY();
				gl.glVertex2d(path.get(i).parent.node.getX() + xVector * proportionOfFinalEdge + pan.x, 
					path.get(i).parent.node.getY() + yVector * proportionOfFinalEdge + pan.y);
			}
			else {
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
		// Draw the reached nodes, if any
		if (reached.size() > 0) {
			// Declare variables for cyan to purple gradient
			int rgbIncrement = 255 / maxDepth;
			int[] nodeColor = {0, 255, 255};

			// Draw each node
        	for (SearchNode n: reached) {
				// Set the color based on the depth
				int depth = n.depth;
				setColor(gl, nodeColor[0] + depth * rgbIncrement, nodeColor[1] - depth * rgbIncrement, nodeColor[2]);

				// Draw a circle for the reached node
				fillCircle(gl, n.node.getX() + pan.x, n.node.getY() + pan.y, n.node.getRadius());
				edgeCircle(gl, n.node.getX() + pan.x, n.node.getY() + pan.y, n.node.getRadius());
			}
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