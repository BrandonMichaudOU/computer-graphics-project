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
    private List<Edge> bfs;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		graph = new Graph();
        bfs = null;
	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	public List<Node>	getNodes()
	{
		return graph.getNodes();
	}

    public List<Edge>	getEdges()
	{
		return graph.getEdges();
	}

    public List<Edge>   getBFS()
    {
        return bfs;
    }

	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************

    public void addNodes(List<Node> nodes) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				graph.addNodes(nodes);
			}
		});
    }

	public void addEdges(List<Edge> edges) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				graph.addEdges(edges);
			}
		});
    }

    public void setStart(int idx) {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				graph.setStart(idx);
			}
		});
    }

    public void BFS() {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				bfs = graph.BFS();
			}
		});
    }

    public void clearBFS() {
        view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				bfs = null;
			}
		});
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
