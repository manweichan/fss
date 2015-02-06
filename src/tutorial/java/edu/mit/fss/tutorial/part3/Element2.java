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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.mit.fss.tutorial.part2.MobileElement;

/**
 * An abstract class which contains the main method to run an 
 * OnlineTutorialFederate with the "MegaDrill 2" object instance.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public abstract class Element2 {
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

		// Create a MobileElement object instance.
		MobileElement element = new MobileElement(
				"Element 2", new Vector3D(0, 5, 0));

		// Create an OnlineTutorialFederate object instance.
		OnlineTutorialFederate fed = new OnlineTutorialFederate(element);

		// Execute the federate and exit when complete.
		fed.execute(0, 50000, 1000);
		fed.exit();
	}
}
