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
package edu.mit.fss.tutorial.part3;

import hla.rti1516e.exceptions.RTIexception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.mit.fss.DefaultFederate;
import edu.mit.fss.SurfaceElement;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import edu.mit.fss.tutorial.part2.MobileElement;

/**
 * A federate class used to execute a single mobile element object 
 * in an online federation.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class OnlineTutorialFederate extends DefaultFederate {
	// Define a logger to manage log messages.
	private static Logger logger = Logger.getLogger(OnlineTutorialFederate.class);

	private MobileElement thisElement;
	
	// Define a list of surface elements which have been discovered.
	private List<SurfaceElement> otherElements = Collections.synchronizedList(
			new ArrayList<SurfaceElement>());

	/**
	 * Instantiates a new online tutorial federate.
	 *
	 * @throws RTIexception the RTI exception
	 */
	public OnlineTutorialFederate(MobileElement element) throws RTIexception {
		super();
		
		// Save the mobile element reference.
		this.thisElement = element;

		// Add the mobile element to the federate.
		addObject(element);
		
		// Add a new SimulationTimeListener object instance to the federate.
		// This will execute a snippit of code each time the simulation time
		// is advanced.
		addSimulationTimeListener(new SimulationTimeListener() {
			@Override
			public void timeAdvanced(SimulationTimeEvent event) {
				logger.info("At time " + event.getTime() + ", " 
						+ thisElement.getName() + " is at " 
						+ thisElement.getPosition() + ".");
				synchronized(otherElements) {
					for(SurfaceElement e : otherElements) {
						logger.info("... distance to " 
								+ e.getName() + " is " 
								+ thisElement.getPosition().distance(
										e.getPosition()) + ".");
					}
				}
			}
		});
		
		// Add a new ObjectChangeListener object instance to the federate.
		// This will execute a snippit of code each time objects change.
		addObjectChangeListener(new ObjectChangeListener() {
			@Override
			public void objectDiscovered(ObjectChangeEvent event) {
				// If the discovered object is another SurfaceElement...
				if(event.getObject() instanceof SurfaceElement
						&& event.getObject() != thisElement) {
					// Add it to the list of other elements.
					otherElements.add((SurfaceElement) event.getObject());
				}
			}

			@Override
			public void objectRemoved(ObjectChangeEvent event) { 
				// Try to remove the object from the list of other elements.
				otherElements.remove(event.getObject());
			}

			@Override
			public void objectChanged(ObjectChangeEvent event) {
				// Nothing to do.
			}

			@Override
			public void interactionOccurred(ObjectChangeEvent event) {
				// Nothing to do.
			}
		});
	}
	
	/**
	 * Execute.
	 *
	 * @param initialTime the initial time
	 * @param finalTime the final time
	 * @param timeStep the time step
	 */
	public void execute(long initialTime, long finalTime, long timeStep) {
		// Configure the federate: set initial/final times and time step.
		setInitialTime(initialTime);
		setFinalTime(finalTime);
		setTimeStep(timeStep);
		

		// Set to online mode and define federate name and type, 
		// federation name, and FOM path.
		getConnection().setOfflineMode(false);
		getConnection().setFederateName(thisElement.getName());
		getConnection().setFederateType("Demo");
		getConnection().setFederationName("Tutorial");
		getConnection().setFomPath("fss.xml");

		// Connect to the federation, initialize, and run the federate.
		connect();
		initialize();
		run();
	}
}
