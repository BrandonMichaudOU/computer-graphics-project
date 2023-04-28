package edu.ou.cs.cg.project;

import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import java.awt.Component;

public final class Application
	implements Runnable
{
	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final String		DEFAULT_NAME = "Team Project 7";
	public static final Dimension	DEFAULT_SIZE = new Dimension(500, 500);

	//**********************************************************************
	// Main
	//**********************************************************************

	public static void	main(String[] args)
	{
		SwingUtilities.invokeLater(new Application(args));
	}

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private View		view;

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
		//GLCanvas		canvas = new GLCanvas(capabilities);	// Single-buffer
		GLJPanel		canvas = new GLJPanel(capabilities);	// Double-buffer
		JFrame			frame = new JFrame(DEFAULT_NAME);
		frame.setVisible(true);

		// Rectify display scaling issues when in Hi-DPI mode on macOS.
		edu.ou.cs.cg.utilities.Utilities.setIdentityPixelScale(canvas);

		// Specify the starting width and height of the canvas itself
		canvas.setPreferredSize(DEFAULT_SIZE);

		// Populate and show the frame
		frame.setBounds(50, 50, 1000, 1000);
		//frame.getContentPane().add(canvas);
		JPanel 			panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		frame.getContentPane().add(panel);

		JLabel label = new JLabel("Select the Graph Algorithm to run.");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(label);
		JPanel panel2 = new JPanel();
		String[] choices = {"", "Breadth-First-Search", "Depth-First-Search", "Shortest-Path"};
		final JComboBox<String> cb = new JComboBox<String>(choices);
		cb.setMaximumSize(cb.getPreferredSize());
		cb.setAlignmentX(Component.CENTER_ALIGNMENT);
		JButton start= new JButton("Start the Visualizer");
		JButton random= new JButton("Randomize the Graph");
		start.setAlignmentX(Component.LEFT_ALIGNMENT);
		random.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel2.add(start);
		//panel2.add(label);
		panel2.add(cb);
		panel2.add(random);
		panel.add(panel2);
		panel.add(canvas);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		/* cb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
			}
		});	 */
		

		// Exit when the user clicks the frame's close button
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

		

		// Create a view to manage the canvas
		view = new View(canvas, cb, start, random);
	}
}

//******************************************************************************
