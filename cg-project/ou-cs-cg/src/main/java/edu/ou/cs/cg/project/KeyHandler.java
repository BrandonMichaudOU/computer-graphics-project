package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.Component;
import java.awt.event.*;
import java.awt.geom.Point2D;

import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>KeyHandler</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class KeyHandler extends KeyAdapter
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

	public KeyHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		Component	component = view.getCanvas();

		component.addKeyListener(this);
	}

	//**********************************************************************
	// Override Methods (KeyListener)
	//**********************************************************************

	public void		keyPressed(KeyEvent e)
	{
		Point2D.Double currPan = model.getPan();
		double			a = (Utilities.isShiftDown(e) ? 10 : 100);
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_B:
				model.BFS();
				break;
			case KeyEvent.VK_D:
				model.DFS();
				break;
			case KeyEvent.VK_Q:
				model.clearPath();
				view.pathCounter = 120;
				break;
			case KeyEvent.VK_A:
				model.setSpeed(model.getSpeed() / 1.1);
				break;
			case KeyEvent.VK_S:
				model.setSpeed(model.getSpeed() * 1.1);
				break;
			case KeyEvent.VK_P:
				model.togglePause();;
				break;
			case KeyEvent.VK_LEFT:
				model.setPan(currPan.x - a, currPan.y);
				break;
			case KeyEvent.VK_RIGHT:
				model.setPan(currPan.x + a, currPan.y);
				break;
			case KeyEvent.VK_DOWN:
				model.setPan(currPan.x, currPan.y - a);
				break;
			case KeyEvent.VK_UP:
				model.setPan(currPan.x, currPan.y + a);
				break;
			// case KeyEvent.VK_C:
			// 	model.clearGraph();
			// 	break;
		}
	}
}