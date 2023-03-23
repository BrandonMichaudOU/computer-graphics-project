package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

//******************************************************************************

/**
 * The <CODE>MouseHandler</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class MouseHandler extends MouseAdapter
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View		view;
	private final Model	model;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public MouseHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		Component	component = view.getCanvas();

		component.addMouseListener(this);
		component.addMouseMotionListener(this);
		component.addMouseWheelListener(this);
	}

	//**********************************************************************
	// Override Methods (MouseListener)
	//**********************************************************************

	public void		mouseClicked(MouseEvent e)
	{
        Point2D.Double click = model.translateScreenToScene(e.getPoint());
        int i = 0;
		// boolean safe = true;
		for (Node n: model.getNodes()) {
			double dist = distance(n.getPoint(), click);
            if (dist <= view.radius) {
				if (e.isShiftDown()) {
					//model.addPossibleEdge(n);
					model.setEnd(i);
				}
				// else if (e.isControlDown()) {
				// 	model.removeNode(i);
				// }
				else {
                	model.setStart(i);
				}
				return;
            }
			// else if (dist <= 2 * view.radius) {
			// 	safe = false;
			// }
            ++i;
        }
		// if (safe) {
		// 	model.addNode(new Node(click.x, click.y));
		// }
	}

	public void		mousePressed(MouseEvent e)
	{
	}

	public void		mouseReleased(MouseEvent e)
	{
	}

	//**********************************************************************
	// Override Methods (MouseMotionListener)
	//**********************************************************************

	public void		mouseDragged(MouseEvent e)
	{
		
	}

	public void		mouseMoved(MouseEvent e)
	{
	}

	//**********************************************************************
	// Override Methods (MouseWheelListener)
	//**********************************************************************

	public void		mouseWheelMoved(MouseWheelEvent e)
	{
	}

	//**********************************************************************
	// Convenience Functions
	//**********************************************************************

	private double distance(Point2D.Double a, Point2D.Double b) 
	{
		double dist = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
		return dist;
	}
}

//******************************************************************************
