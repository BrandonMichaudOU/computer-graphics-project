package edu.ou.cs.cg.project;

import java.awt.*;
import java.awt.event.*;

public final class MouseHandler extends MouseAdapter
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View		view;
	private final Model	model;
	private final Component component;
	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public MouseHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		component = view.getCanvas();

		component.addMouseListener(this);
		component.addMouseMotionListener(this);
		component.addMouseWheelListener(this);
	}

	//**********************************************************************
	// Override Methods (MouseListener)
	//**********************************************************************

	public void		mouseClicked(MouseEvent e)
	{
		// Handle the click
		model.handleClick(e.getPoint(), e.isShiftDown());
	}

	public void		mouseEntered(MouseEvent e)
	{
		// Set the cursor point
		model.setCursorInViewCoordinates(e.getPoint());
		component.requestFocusInWindow();
	}

	public void		mouseExited(MouseEvent e)
	{
		// Remove cursor point
		model.setCursorInViewCoordinates(null);
	}

	//**********************************************************************
	// Override Methods (MouseMotionListener)
	//**********************************************************************

	public void		mouseDragged(MouseEvent e)
	{
		// Update cursor point
		model.setCursorInViewCoordinates(e.getPoint());
	}

	public void		mouseMoved(MouseEvent e)
	{
		// Update cursor point
		model.setCursorInViewCoordinates(e.getPoint());
	}

	//**********************************************************************
	// Override Methods (MouseWheelListener)
	//**********************************************************************

	public void		mouseWheelMoved(MouseWheelEvent e)
	{
		// Update the zoom
		model.setZoom(Math.pow(1.1, -e.getWheelRotation()));
	}

	//**********************************************************************
	// Convenience Functions
	//**********************************************************************
}

//******************************************************************************
