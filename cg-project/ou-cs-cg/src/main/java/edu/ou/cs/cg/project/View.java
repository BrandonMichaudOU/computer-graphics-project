package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
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

	private final FPSAnimator			animator;
	private int						k;	// Frame counter

    private Graph graph = new Graph();

    private boolean init = false;

	private float				defaultLine = 1.0f;		// normal thickness
	private float				edgeLine = 2.5f;		// edge thickness

	private double 				radius = 25;

	private final Model				model;

	private final KeyHandler			keyHandler;
	private final MouseHandler			mouseHandler;

	//**********************************************************************
	// Private Scene Members
	//**********************************************************************

	private float				defaultline = 1.0f;		// normal thickness
	private float				thickline = 2.5f;		// bold thickness

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public View(GLJPanel canvas)
	{
		this.canvas = canvas;

		// Initialize rendering
		k = 0;
		canvas.addGLEventListener(this);

		// Initialize model (scene data and parameter manager)
		model = new Model(this);

		// Initialize controller (interaction handlers)
		keyHandler = new KeyHandler(this, model);
		mouseHandler = new MouseHandler(this, model);

		// Initialize animation
		animator = new FPSAnimator(canvas, DEFAULT_FRAMES_PER_SECOND);
		animator.start();
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
        if (!init) {
            init = true;
            buildGraphOne();
        }
	}

    private void buildGraphOne() {
		//adding default nodes
		graph.addNode(340, 680, radius);
		graph.addNode(640, 680, radius);
		graph.addNode(940, 680, radius);
		graph.addNode(40, 360, radius);
		graph.addNode(490, 360, radius);
		graph.addNode(1240, 360, radius);
		graph.addNode(340, 40, radius);
		graph.addNode(640,40, radius);
		graph.addNode(940,40, radius);
		//adding default edges
		graph.addEdge(0, 1);
		graph.addEdge(0, 3);
		graph.addEdge(0, 6);
		graph.addEdge(1, 2);
		graph.addEdge(1,4);
		graph.addEdge(1, 8);
		graph.addEdge(2, 5);
		graph.addEdge(2, 8);
		graph.addEdge(3,6);
		graph.addEdge(4, 6);
		graph.addEdge(4, 7);
		graph.addEdge(5, 8);
		graph.addEdge(6, 7);
		graph.addEdge(7, 8);
	}

	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);	// White background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);			// Clear the buffer

		setProjection(gl);							// Use screen coordinates
        
        drawEdges(gl);
        drawNodes(gl);
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
		glu.gluOrtho2D(0.0f, 1280.0f, 0.0f, 720.0f);// 2D translate and scale
	}

    //**********************************************************************
	// Private Methods (Graph Functions)
	//**********************************************************************
    private void drawEdges(GL2 gl) {
        setColor(gl, 0, 0, 0);
        gl.glLineWidth(edgeLine);				// set the line width to the default
        gl.glBegin(GL.GL_LINES);
        for (Edge e: graph.edges) {
            gl.glVertex2d(e.node1.x, e.node1.y);
            gl.glVertex2d(e.node2.x, e.node2.y);
        }
        gl.glEnd();
        gl.glLineWidth(defaultLine);
    }

    private void drawNodes(GL2 gl) {
        setColor(gl, 255, 0, 0);
        for (Node n: graph.nodes) {
            fillCircle(gl, n.x, n.y, 25);
            edgeCircle(gl, n.x, n.y, 25);
        }
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

	// Fills a rectangle having lower left corner at (x,y) and dimensions (w,h).
	private void	fillRect(GL2 gl, int x, int y, int w, int h)
	{
		gl.glBegin(GL2.GL_POLYGON);

		gl.glVertex2i(x+0, y+0);
		gl.glVertex2i(x+0, y+h);
		gl.glVertex2i(x+w, y+h);
		gl.glVertex2i(x+w, y+0);

		gl.glEnd();
	}

	// Edges a rectangle having lower left corner at (x,y) and dimensions (w,h).
	private void	edgeRect(GL2 gl, int x, int y, int w, int h)
	{
		gl.glBegin(GL.GL_LINE_LOOP);

		gl.glVertex2i(x+0, y+0);
		gl.glVertex2i(x+0, y+h);
		gl.glVertex2i(x+w, y+h);
		gl.glVertex2i(x+w, y+0);

		gl.glEnd();
	}

	// Fills a polygon defined by a starting point and a sequence of offsets.
	private void	fillPoly(GL2 gl, int startx, int starty, Point[] offsets)
	{
		gl.glBegin(GL2.GL_POLYGON);

		for (int i=0; i<offsets.length; i++)
			gl.glVertex2i(startx + offsets[i].x, starty + offsets[i].y);

		gl.glEnd();
	}

	// Edges a polygon defined by a starting point and a sequence of offsets.
	private void	edgePoly(GL2 gl, int startx, int starty, Point[] offsets)
	{
		gl.glBegin(GL2.GL_LINE_LOOP);

		for (int i=0; i<offsets.length; i++)
			gl.glVertex2i(startx + offsets[i].x, starty + offsets[i].y);

		gl.glEnd();
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

	// fills a stellation given the center, radius, number of sides, and skip number
	private void fillStar(GL2 gl, double cx, double cy, double r, int sides, int skip) {
		// Use the given (outer) radius to find inner radius
		double r1 = r;
		double r2 = r * (Math.cos(Math.PI * skip / sides) / Math.cos(Math.PI * (skip - 1) / sides));
		if (sides == 4 || sides == 3) {
			r2 = r1 / 3.0;
		}

		// Draw the star, alternating between radiuses
		gl.glBegin(GL2.GL_TRIANGLE_FAN);
		gl.glVertex2d(cx, cy);
		for (int i = 0; i <= sides * 2; ++i) {
			double angle = i * 2.0 * Math.PI / sides * 0.5 + Math.PI / 2;
			if (i % 2 == 0) {
				gl.glVertex2d(cx + (r1 * Math.cos(angle)), cy + (r1 * Math.sin(angle)));
			}
			else {
				gl.glVertex2d(cx + (r2 * Math.cos(angle)), cy + (r2 * Math.sin(angle)));
			}
		}
		gl.glEnd();
	}

	// edges a stellation given the center, radius, number of sides, and skip number
	private void edgeStar(GL2 gl, double cx, double cy, double r, int sides, int skip) {
		// Use the given (outer) radius to find inner radius
		double r1 = r;
		double r2 = r * (Math.cos(Math.PI * skip / sides) / Math.cos(Math.PI * (skip - 1) / sides));
		if (sides == 4 || sides == 3) {
			r2 = r1 / 3.0;
		}

		// Edge the star, alternating between radiuses
		gl.glBegin(GL2.GL_LINE_LOOP);
		for (int i = 0; i <= sides * 2; ++i) {
			double angle = i * 2.0 * Math.PI / sides * 0.5 + Math.PI / 2;
			if (i % 2 == 0) {
				gl.glVertex2d(cx + (r1 * Math.cos(angle)), cy + (r1 * Math.sin(angle)));
			}
			else {
				gl.glVertex2d(cx + (r2 * Math.cos(angle)), cy + (r2 * Math.sin(angle)));
			}
		}
		gl.glEnd();
	}
}

//******************************************************************************