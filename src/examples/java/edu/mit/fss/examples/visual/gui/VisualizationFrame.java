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
package edu.mit.fss.examples.visual.gui;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import edu.mit.fss.Federate;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import edu.mit.fss.examples.gui.FederateFrame;

/**
 * A top-level graphical user interface component containing a 
 * {@link WorldWindVisualization} component for Earth visualization.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class VisualizationFrame extends FederateFrame implements SimulationTimeListener {
	private static final long serialVersionUID = 9148183974955674498L;
	private static Logger logger = Logger.getLogger(
			VisualizationFrame.class);
	
	private final WorldWindVisualization visualization;
	
	/**
	 * Instantiates a new visualization frame for a federate. 
	 * Adds this frame to the federate as a {@link SimulationTimeListener}.
	 * Adds the {@link WorldWindVisualization} object to the federate as a 
	 * {@link SimulationTimeListener} and a {@link ObjectChangeListener}.
	 *
	 * @param federate the federate
	 */
	public VisualizationFrame(Federate federate) throws OrekitException {
		super(federate);
		
		logger.trace("Adding this panel as a time listener.");
		federate.addSimulationTimeListener(this);

		logger.trace("Initializing the visualization and adding " +
				"it as a time and object listener.");
		visualization = new WorldWindVisualization();
		federate.addSimulationTimeListener(visualization);
		federate.addObjectChangeListener(visualization);
		getContentPane().add(visualization, BorderLayout.CENTER);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.SimulationTimeListener#timeAdvanced(edu.mit.fss.gui.SimulationTimeEvent)
	 */
	@Override
	public void timeAdvanced(final SimulationTimeEvent event) {
		// run in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					try {
						// update title to display current date
						setTitle(new AbsoluteDate(new Date(event.getTime()), 
								TimeScalesFactory.getUTC()).toString());
					} catch(OrekitException e) {
						logger.error(e);
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
	}
}