package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

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

	public double 				radius = 25;

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

		pan = model.getPan();
		zoom = model.getZoom();
		speed = model.getSpeed();
		pause = model.getPause();
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
        if (!init) {
            init = true;
            buildGraphOne();
        }

		pan = model.getPan();
		zoom = model.getZoom();
		speed = model.getSpeed();
		pause = model.getPause();
	}

    private void buildGraphOne() {
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

        model.addNodes(nodes);

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

        model.addEdges(edges);
	}

	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);	// White background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);			// Clear the buffer

		setProjection(gl);							// Use screen coordinates
        
        drawEdges(gl);
		List<SearchNode> reached = drawPath(gl);
        drawNodes(gl);
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
		double[] projection = model.getProjection();
		glu.gluOrtho2D(projection[0], projection[1], projection[2], projection[3]);

		//glu.gluOrtho2D(0.0f, 1280.0f * zoom, 0.0f, 720.0f * zoom);// 2D translate and scale
	}

    //************************************ ***********************************
	// Private Methods (Graph Functions)
	//**********************************************************************
    private void drawEdges(GL2 gl) {
        setColor(gl, 0, 0, 0);
        gl.glLineWidth(edgeLine / (float) zoom);				// set the line width to the default
        gl.glBegin(GL.GL_LINES);
        for (Edge e: model.getEdges()) {
            gl.glVertex2d(e.getNode1().getX() * 1 + pan.x, e.getNode1().getY() * 1 + pan.y);
            gl.glVertex2d(e.getNode2().getX() * 1 + pan.x, e.getNode2().getY() * 1 + pan.y);
        }
        gl.glEnd();
        gl.glLineWidth(defaultLine);
    }

    private void drawNodes(GL2 gl) {
        for (Node n: model.getNodes()) {
            if (n.isStart()) {
                setColor(gl, 0, 0, 255);
            }
			else if (n.isEnd()) {
				setColor(gl, 255, 0, 255);
			}
            else {
                setColor(gl, 255, 0, 0);
            }
            Point2D.Double p = n.getPoint();
            fillCircle(gl, p.x * 1 + pan.x, p.y * 1 + pan.y, radius);
            edgeCircle(gl, p.x * 1 + pan.x, p.y * 1 + pan.y, radius);
        }
    }

	private List<SearchNode> drawPath(GL2 gl) {
		List<SearchNode> path = model.getPath();
		ArrayList<SearchNode> reached = new ArrayList<>();
        if (path != null) {
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
        }
		return reached;
	}

    private void drawReached(GL2 gl, List<SearchNode> reached) {
		if (reached.size() > 0) {
			int rgbIncrement = 255 / maxDepth;
			int[] nodeColor = {0, 255, 255};
        	for (SearchNode n: reached) {
				int depth = n.depth;
				setColor(gl, nodeColor[0] + depth * rgbIncrement, nodeColor[1] - depth * rgbIncrement, nodeColor[2]);
				fillCircle(gl, n.node.getX() + pan.x, n.node.getY() + pan.y, radius);
				edgeCircle(gl, n.node.getX() + pan.x, n.node.getY() + pan.y, radius);
			}
		}
    }

	private void	drawMode(GLAutoDrawable drawable)
	{
		GL2		gl = drawable.getGL().getGL2();

		renderer.beginRendering(w, h);

		// Draw all text in black
		renderer.setColor(0.f, 0.f, 0.f, 1.0f);

		Point2D.Double	cursor = model.getCursor();

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