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
package edu.mit.fss.tutorial.part2;

import hla.rti1516e.exceptions.RTIexception;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.mit.fss.DefaultFederate;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;

/**
 * A federate class used to execute a single mobile element object in an 
 * offline federation.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class OfflineTutorialFederate extends DefaultFederate {
	// Define a logger to manage log messages.
	private static Logger logger = Logger.getLogger("edu.mit.fss");

	/**
	 * Instantiates a new offline tutorial federate.
	 *
	 * @param element the element
	 * @throws RTIexception the RTI exception
	 */
	public OfflineTutorialFederate(final MobileElement element) throws RTIexception {
		super();
		
		// Add the mobile element to the federate.
		addObject(element);
		
		// Add a new SimulationTimeListener object instance to the federate.
		// This will execute a snippit of code each time the simulation time
		// is advanced.
		addSimulationTimeListener(new SimulationTimeListener() {
			@Override
			public void timeAdvanced(SimulationTimeEvent event) {
				// Log an info message when the simulation time is advanced.
				logger.info("At time " + event.getTime() + ", " 
						+ element.getName() + " is at " 
						+ element.getPosition() + ".");
			}
		});
	}
	
	/**
	 * Executes this federate.
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
		
		// Set to offline mode.
		getConnection().setOfflineMode(true);
		
		// Initialize and run the federate.
		initialize();
		run();
	}

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

		// Create a MobileElement object instance.
		MobileElement element = new MobileElement(
				"Element", new Vector3D(10, 0, 0));

		// Create a OfflineTutorialFederate object instance.
		OfflineTutorialFederate fed = new OfflineTutorialFederate(element);
		
		// Execute the federate and exit when complete.
		fed.execute(0, 20000, 1000);
		fed.exit();
	}
}
