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
package edu.mit.fss.tutorial.part1;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.mit.fss.DefaultFederate;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import hla.rti1516e.exceptions.RTIexception;

/**
 * A federate class used to execute a single trivial clock object.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public class ClockFederate extends DefaultFederate {
	// Define a logger to manage log messages.
	private static Logger logger = Logger.getLogger("edu.mit.fss");

	/**
	 * Instantiates a new clock federate.
	 *
	 * @throws RTIexception the RTI exception
	 */
	public ClockFederate() throws RTIexception {
		super();
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

		// Create a TrivialClock object instance. It uses the "final"
		// keyword to allow it to be referenced in the SimulationTimeListener.
		final TrivialClock clock = new TrivialClock("My clock");
		
		// Create a ClockFederate object instance.
		ClockFederate fed = new ClockFederate();
		
		// Add the clock object to the federate.
		fed.addObject(clock);
		
		// Add a new SimulationTimeListener object instance to the federate.
		// This will execute a snippit of code each time the simulation time
		// is advanced.
		fed.addSimulationTimeListener(new SimulationTimeListener() {
			@Override
			public void timeAdvanced(SimulationTimeEvent event) {
				// Log an info message when the simulation time is advanced.
				logger.info("The clock reads " + clock.getDate() + ".");
			}
		});
		
		// Configure the federate: set initial/final times and time step.
		fed.setInitialTime(0);
		fed.setFinalTime(20000);
		fed.setTimeStep(1000);
		
		// Set to offline mode.
		fed.getConnection().setOfflineMode(true);
		
		// Initialize the federate, run, and finally, exit.
		fed.initialize();
		fed.run();
		fed.exit();
	}
}
