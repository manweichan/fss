/*
 * Copyright 2015 Paul T. Grogan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mit.fss.tutorial.part4;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.mit.fss.tutorial.part2.MobileElement;
import edu.mit.fss.tutorial.part3.OnlineTutorialFederate;
import hla.rti1516e.exceptions.RTIexception;

/**
 * An abstract class which contains the main method to run an 
 * OnlineTutorialFederate with a MobileElement object instance.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public abstract class ElementGUI {
	// Define a logger to manage log messages.
	private static Logger logger = Logger.getLogger("edu.mit.fss");
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws RTIexception the RTI exception
	 */
	public static void main(String[] args) throws RTIexception {
		// Configure the logger and set it to display info messages.
		BasicConfigurator.configure();
		logger.setLevel(Level.INFO);
		
		// Use an input dialog to request the element's name.
		String name = null;
		while(name == null || name.isEmpty()) {
			name = JOptionPane.showInputDialog("Enter element name:");
		}

		// Create a MobileElement object instance. The "final" keyword allows 
		// it to be referenced in the GUI thread below.
		final MobileElement element = new MobileElement(
				name, new Vector3D(0, 0, 0));

		// Create an OnlineTutorialFederate object instance. The "final" 
		// keyword allows it to be referenced in the GUI thread below.
		final OnlineTutorialFederate fed = new OnlineTutorialFederate(element);

		// Create the graphical user interface using the Event Dispatch Thread.
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// Create a new ControlPanel object instance.
					ControlPanel controlPanel = new ControlPanel();
					
					// Bind it to the element above.
					controlPanel.setBoundElement(element);
					
					// Add the control panel as an object change listener.
					fed.addObjectChangeListener(controlPanel);
					
					// Create a new frame to display the panel. Add the panel
					// as the content, pack it, and make it visible.
					JFrame frame = new JFrame();
					frame.setContentPane(controlPanel);
					frame.pack();
					frame.setVisible(true);
					
					// Add a new WindowAdapter object instance to exit the
					// federate when the window is closing.
					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							fed.exit();
						}
					});
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
		
		// Execute the federate.
		fed.execute(0, Long.MAX_VALUE, 1000);
	}
}
