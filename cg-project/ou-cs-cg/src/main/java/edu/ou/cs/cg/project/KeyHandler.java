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
		// Get the current scene pan
		Point2D.Double currPan = model.getPan();

		// Define pan increment
		double a = (Utilities.isShiftDown(e) ? 10 : 100);

		// Handle input
		switch (e.getKeyCode())
		{
			// Load default graph
			case KeyEvent.VK_D:
				model.defaultGraph();
				break;

			// Load random graph
			case KeyEvent.VK_R:
				model.randomGraph();
				break;
				
			// Clear the path animation
			case KeyEvent.VK_Q:
				model.clearPath();
				view.pathCounter = 120;
				break;

			// Reset Transformation
			case KeyEvent.VK_C:
				model.resetTransform();
				break;
			
			// Update the animation speed
			case KeyEvent.VK_A:
				model.setSpeed(model.getSpeed() / 1.1);
				break;
			case KeyEvent.VK_S:
				model.setSpeed(model.getSpeed() * 1.1);
				break;

			// Pause the animation
			case KeyEvent.VK_P:
				model.togglePause();;
				break;

			// Pan the scene
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
			
			// Start the animation
			case KeyEvent.VK_SPACE:
				model.start();
				break;
		}
	}
}