package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public final class Application
	implements GLEventListener, Runnable
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final String		DEFAULT_NAME = "project";
	private static final Dimension		DEFAULT_SIZE = new Dimension(1280, 720);

	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final GLUT			MYGLUT = new GLUT();
	public static final Random			RANDOM = new Random();

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private int				w;				// Canvas width
	private int				h;				// Canvas height
	private int				k = 0;			// Animation counter
	private TextRenderer		renderer;

    private Graph graph = new Graph();

    private boolean init = false;

	private float				defaultLine = 1.0f;		// normal thickness
	private float				edgeLine = 2.5f;		// edge thickness

	//**********************************************************************
	// Main
	//**********************************************************************

	public static void	main(String[] args)
	{
		SwingUtilities.invokeLater(new Application(args));
	}

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Application(String[] args)
	{
	}

	//**********************************************************************
	// Override Methods (Runnable)
	//**********************************************************************

	public void	run()
	{
		GLProfile		profile = GLProfile.getDefault();

		System.out.println("Running on Java version " + 
			System.getProperty("java.version"));
		System.out.println("Running with OpenGL version " +
			profile.getName());

		GLCapabilities	capabilities = new GLCapabilities(profile);
		GLCanvas		canvas = new GLCanvas(capabilities);	// Single-buffer
		//GLJPanel		canvas = new GLJPanel(capabilities);	// Double-buffer
		JFrame			frame = new JFrame(DEFAULT_NAME);

		// Rectify display scaling issues when in Hi-DPI mode on macOS.
		edu.ou.cs.cg.utilities.Utilities.setIdentityPixelScale(canvas);

		// Specify the starting width and height of the canvas itself
		canvas.setPreferredSize(DEFAULT_SIZE);

		// Populate and show the frame
		frame.setBounds(50, 50, 200, 200);
		frame.getContentPane().add(canvas);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Exit when the user clicks the frame's close button
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

		// Register this class to update whenever OpenGL needs it
		canvas.addGLEventListener(this);

		// Have OpenGL call display() to update the canvas 60 times per second
		FPSAnimator	animator = new FPSAnimator(canvas, 60);

		animator.start();
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	// Called immediately after the GLContext of the GLCanvas is initialized.
	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Serif", Font.PLAIN, 18),
									true, true);

		initPipeline(drawable);
	}

	// Notification to release resources for the GLContext.
	public void	dispose(GLAutoDrawable drawable)
	{
		renderer = null;
	}

	// Called to initiate rendering of each frame into the GLCanvas.
	public void	display(GLAutoDrawable drawable)
	{
		update(drawable);
		render(drawable);
	}

	// Called during the first repaint after a resize of the GLCanvas.
	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************

	// Update the scene model for the current animation frame.
	private void	update(GLAutoDrawable drawable)
	{
		k++;							// Advance animation counter
        if (!init) {
            init = true;
            ArrayList<Node> nodes = new ArrayList<>();
            nodes.add(new Node(340, 680));
            nodes.add(new Node(640, 680));
            nodes.add(new Node(940, 680));
            nodes.add(new Node(40, 360));
            nodes.add(new Node(490, 360));
            nodes.add(new Node(1240, 360));
            nodes.add(new Node(340, 40));
            nodes.add(new Node(640, 40));
            nodes.add(new Node(940, 40));

            ArrayList<Edge> edges = new ArrayList<>();
            edges.add(new Edge(nodes.get(0), nodes.get(1)));
            edges.add(new Edge(nodes.get(0), nodes.get(3)));
            edges.add(new Edge(nodes.get(0), nodes.get(6)));
            edges.add(new Edge(nodes.get(1), nodes.get(2)));
            edges.add(new Edge(nodes.get(1), nodes.get(4)));
            edges.add(new Edge(nodes.get(1), nodes.get(8)));
            edges.add(new Edge(nodes.get(2), nodes.get(5)));
            edges.add(new Edge(nodes.get(2), nodes.get(8)));
            edges.add(new Edge(nodes.get(3), nodes.get(6)));
            edges.add(new Edge(nodes.get(4), nodes.get(6)));
            edges.add(new Edge(nodes.get(4), nodes.get(7)));
            edges.add(new Edge(nodes.get(5), nodes.get(8)));
            edges.add(new Edge(nodes.get(6), nodes.get(7)));
            edges.add(new Edge(nodes.get(7), nodes.get(8)));

            graph = new Graph(nodes, edges);
        }
	}

	// Render the scene model and display the current animation frame.
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

	// www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBlendFunc.xml
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
		for (int i = 0; i < 30; i++) {
			double angle = i * 2.0 * Math.PI / 30;
			gl.glVertex2d(cx + (a * Math.cos(angle)), cy + (b * Math.sin(angle)));
		}
		gl.glEnd();
	}

	// edges an ellipse defined by the center, x radius, and y radius
	private void edgeEllipse(GL2 gl, double cx, double cy, double a, double b) {
		gl.glBegin(GL.GL_LINE_LOOP);
		for (int i = 0; i < 30; i++) {
			double angle = i * 2.0 * Math.PI / 30;
			gl.glVertex2d(cx + (a * Math.cos(angle)), cy + (b * Math.sin(angle)));
		}
		gl.glEnd();
	}
}