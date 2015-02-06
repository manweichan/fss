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
package edu.mit.fss.examples.member.gui;

import java.awt.BorderLayout;
import java.awt.Component;
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
 * {@link ComponentPanel} component.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class MemberFrame extends FederateFrame 
		implements SimulationTimeListener {
	private static Logger logger = Logger.getLogger(MemberFrame.class);
	private static final long serialVersionUID = 620103555577061986L;
	
	/**
	 * Instantiates a new default federate frame for a federate and a
	 * component. Adds this frame to the federate as a 
	 * {@link SimulationTimeListener}. Adds {@link component} to the 
	 * federate as a {@link SimulationTimeListener} and a 
	 * {@link ObjectChangeListener} if possible.
	 *
	 * @param federate the federate
	 * @param component the component
	 */
	public MemberFrame(Federate federate, Component component) {
		super(federate);

		logger.trace("Adding this panel as a time listener.");
		federate.addSimulationTimeListener(this);

		logger.trace("Initializing the component panel and " +
				"adding it as a time or object listener (if necessary).");
		add(component, BorderLayout.CENTER);
		if(component instanceof SimulationTimeListener) {
			this.federate.addSimulationTimeListener(
					(SimulationTimeListener) component);
		}
		if(component instanceof ObjectChangeListener) {
			this.federate.addObjectChangeListener(
					(ObjectChangeListener) component);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.SimulationTimeListener#timeAdvanced(edu.mit.fss.event.SimulationTimeEvent)
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
