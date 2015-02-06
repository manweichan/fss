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
package edu.mit.fss.examples;

import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;

import edu.mit.fss.DefaultFederate;
import edu.mit.fss.examples.visual.gui.VisualizationFrame;
import edu.mit.fss.hla.DefaultAmbassador;

/**
 * The sample visualization federate which uses the NASA World Wind 
 * application to display simulation objects.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class VisualizationFederate extends DefaultFederate {
	private static Logger logger = Logger.getLogger("edu.mit.fss");
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws URISyntaxException 
	 * @throws RTIinternalError 
	 */
	public static void main(String[] args) throws RTIexception, URISyntaxException {
		BasicConfigurator.configure();

		logger.debug("Setting Orekit data path.");
		System.setProperty(DataProvidersManager.OREKIT_DATA_PATH,
		        new File(VisualizationFederate.class.getResource(
		        		"/orekit-data.zip").toURI()).getAbsolutePath());

		
		logger.trace("Creating federate instance.");
		final VisualizationFederate federate = new VisualizationFederate();
		
		// federate.setInitialTime(1382270400000l); // Oct 20 2013, 12:00 UTC

		logger.debug("Launching the graphical user interface.");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					VisualizationFrame frame;
					try {
						frame = new VisualizationFrame(federate);
						frame.pack();
						frame.setVisible(true);
					} catch (OrekitException e) {
						e.printStackTrace();
						logger.fatal(e);
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			logger.fatal(e);
		}

		logger.trace("Setting federate name, type, and FOM path.");
		federate.getConnection().setFederateName("World Wind");
		federate.getConnection().setFederateType("Visualization");
		federate.getConnection().setFederationName("FSS");
		federate.getConnection().setFomPath(
				new File(federate.getClass().getClassLoader().getResource(
						"edu/mit/fss/hla/fss.xml").toURI()).getAbsolutePath());
		federate.getConnection().setOfflineMode(false);
		federate.connect();
	}

	/**
	 * Instantiates a new visualization federate using a 
	 * {@link DefaultAmbassador} ambassador.
	 *
	 * @throws RTIinternalError the RTI exception
	 */
	public VisualizationFederate() throws RTIexception {
		super(new DefaultAmbassador(DefaultAmbassador.PORTICO_RTI));
	}
}
