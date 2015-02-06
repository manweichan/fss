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

import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import edu.mit.fss.Federate;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import edu.mit.fss.examples.member.SpaceSystem;

/**
 * A graphical user interface component for a {@link SpaceSystem} object.
 * Composes {@link OrbitalElementPanel}, {@link PowerSubsystemPanel}, and
 * {@link CommSubsystemPanel} components on separate tabs.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SpaceSystemPanel extends JTabbedPane implements
		SimulationTimeListener, ObjectChangeListener {
	private static Logger logger = Logger.getLogger(SpaceSystemPanel.class);
	private static final long serialVersionUID = 3019344218469184491L;
	
	/**
	 * Instantiates a new space system panel for a {@link system}. Signals are
	 * sent via the associated {@link federate}.
	 *
	 * @param federate the federate
	 * @param system the system
	 */
	public SpaceSystemPanel(Federate federate, SpaceSystem system) {
		setName(system.getName());
		
		logger.trace("Creating and adding the element panel.");
		OrbitalElementPanel elementPanel = new OrbitalElementPanel(system);
		logger.trace("Adding the element panel as an object listener.");
		listenerList.add(ObjectChangeListener.class, elementPanel);
		addTab(system.getName(), elementPanel);
		
		logger.trace("Creating and adding the power subsystem panel.");
		PowerSubsystemPanel powerPanel = new PowerSubsystemPanel(
				system.getPowerSubsystem());
		logger.trace("Adding the power subsystem panel as a time listener.");
		listenerList.add(SimulationTimeListener.class, powerPanel);
		addTab("Power", powerPanel);

		logger.trace("Creating and adding the comm subsystem panel.");
		CommSubsystemPanel commPanel = new CommSubsystemPanel(
				federate, system.getCommSubsystem());
		logger.trace("Adding the comm subsystem panel as an object " +
				"and time listener.");
		listenerList.add(ObjectChangeListener.class, commPanel);
		listenerList.add(SimulationTimeListener.class, commPanel);
		addTab("Communications", commPanel);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#interactionOccurred(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void interactionOccurred(ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.interactionOccurred(event);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectChanged(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectChanged(ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectChanged(event);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectDiscovered(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectDiscovered(ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectDiscovered(event);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectRemoved(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectRemoved(ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectRemoved(event);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.SimulationTimeListener#timeAdvanced(edu.mit.fss.event.SimulationTimeEvent)
	 */
	@Override
	public void timeAdvanced(SimulationTimeEvent event) {
		for(SimulationTimeListener l : listenerList.getListeners(
				SimulationTimeListener.class)) {
			l.timeAdvanced(event);
		}
	}
}
