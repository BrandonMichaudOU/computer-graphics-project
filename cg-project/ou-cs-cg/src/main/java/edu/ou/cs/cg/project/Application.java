package edu.ou.cs.cg.project;

//import java.lang.*;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import java.awt.Component;

//******************************************************************************

/**
 * The <CODE>Application</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Application
	implements Runnable
{
	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final String		DEFAULT_NAME = "Homework03";
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
		
		String[] choices = {"", "Breadth-First-Search", "Depth-First-Search", "Shortest-Path"};
		final JComboBox<String> cb = new JComboBox<String>(choices);
		cb.setMaximumSize(cb.getPreferredSize());
		cb.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(cb);
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
		view = new View(canvas, cb);
	}
}

//******************************************************************************
