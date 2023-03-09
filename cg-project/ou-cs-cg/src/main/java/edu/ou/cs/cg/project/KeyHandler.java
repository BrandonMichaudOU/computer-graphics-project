package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.Component;
import java.awt.event.*;

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
			case KeyEvent.VK_C:
				model.clearGraph();
				break;
		}
	}
}