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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.mit.fss.SurfaceElement;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.tutorial.part2.MobileElement;

/**
 * A panel to display element locations on a 2D (X-Y) plane. Includes key
 * bindings to change the velocity of the bound element with arrow keys: 
 * up (Y +1 m/s), down (Y -1 m/s), left (X -1 m/s), and right (X +1 m/s).
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class ControlPanel extends JPanel implements ObjectChangeListener {
	private static final long serialVersionUID = -1729012305362962099L;

	private static float DISPLAY_SCALE = 5.0f; 	// meters per pixel
	private static int ELEMENT_SIZE = 4; 		// pixels
	private MobileElement boundElement;

	// Define a list of elements. This must be synchronized to allow it to be
	// edited across different threads.
	private List<SurfaceElement> elements = Collections.synchronizedList(
			new ArrayList<SurfaceElement>());

	/**
	 * Instantiates a new control panel.
	 */
	public ControlPanel() {
		setFocusable(true); // required for key listener
		setPreferredSize(new Dimension(300,300));
		setBackground(Color.white);

		// Add a new KeyAdapter object instance to bind actions to key presses.
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(boundElement==null) {
					// Do nothing if no element is bound.
					return;
				}
				if(e.getKeyCode()==KeyEvent.VK_UP) {
					// Increment Y-velocity.
					boundElement.setVelocity(
							boundElement.getVelocity().add(
									new Vector3D(0,1,0)));
				} else if(e.getKeyCode()==KeyEvent.VK_DOWN) {
					// Decrement Y-velocity.
					boundElement.setVelocity(
							boundElement.getVelocity().add(
									new Vector3D(0,-1,0)));
				} else if(e.getKeyCode()==KeyEvent.VK_LEFT) {
					// Decrement X-velocity.
					boundElement.setVelocity(
							boundElement.getVelocity().add(
									new Vector3D(-1,0,0)));
				} else if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
					// Increment X-velocity.
					boundElement.setVelocity(
							boundElement.getVelocity().add(
									new Vector3D(1,0,0)));
				}
			}
		});
	}

	/**
	 * Helper function to get the screen location for a given element position.
	 *
	 * @param position the position
	 * @return the screen location
	 */
	private int[] getScreenLocation(Vector3D position) {
		// Convert X-position to pixels using display scale, and offset by half
		// the screen width (i.e. X=0 is at the center of screen).
		int x = (int) Math.round(getWidth()/2 + position.getX()/DISPLAY_SCALE);

		// Convert Y-position to pixels using display scale, and offset by half
		// the screen height (i.e. Y=0 is at the middle of screen).
		int y = (int) Math.round(getHeight()/2 - position.getY()/DISPLAY_SCALE);
		
		// Return X- and Y-locations on screen.
		return new int[]{x, y};
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#interactionOccurred(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void interactionOccurred(ObjectChangeEvent event) { 
		// Nothing to do.
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectChanged(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectChanged(ObjectChangeEvent event) {
		// Repaint the panel.
		repaint();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectDiscovered(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectDiscovered(ObjectChangeEvent event) {
		// If discovered object is a SurfaceElement...
		if(event.getObject() instanceof SurfaceElement) {
			// Add it to the list of elements. Note: this action is 
			// performed by a different thread (i.e. from the RTI) so the 
			// elements list must be thread-safe.
			elements.add((SurfaceElement) event.getObject());
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectRemoved(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectRemoved(ObjectChangeEvent event) {
		// Try to remove the object from the list. Note: this action is 
		// performed by a different thread (i.e. from the RTI) so the 
		// elements list must be thread-safe.
		elements.remove(event.getObject());
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		// Calls the super-class paint method for the default background.
		super.paint(g);
		
		// Set color to gray and draw the x-axis (Y=0) and y=axis (X=0).
		g.setColor(Color.gray);
		g.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
		g.drawLine(getWidth()/2, 0, getWidth()/2, getHeight());

		// Make sure no changes to elements can take place while painting.
		synchronized(elements) {
			// For each surface element...
			for(SurfaceElement e : elements) {
				if(e instanceof MobileElement) {
					// If it is a MobileElement, use blue color.
					g.setColor(Color.blue);
				} else {
					// Otherwise use black color.
					g.setColor(Color.black);
				}
				// Determine the screen location for the element.
				int[] location = getScreenLocation(e.getPosition());
				
				// Fill an oval at the location (offset by half the oval size).
				g.fillOval(location[0] - ELEMENT_SIZE/2, 
						location[1] - ELEMENT_SIZE/2, 
						ELEMENT_SIZE, ELEMENT_SIZE);
				
				// Draw the element name to the right of the oval, offset by
				// 2 pixels to the right and half the oval size vertically.
				g.drawString(e.getName(), location[0] + ELEMENT_SIZE/2 + 2, 
						location[1] + ELEMENT_SIZE/2 + ELEMENT_SIZE/2);
			}
		}
	}

	/**
	 * Sets the element bound to this panel.
	 *
	 * @param element the new bound element
	 */
	public void setBoundElement(MobileElement element) {
		this.boundElement = element;
	}
}
