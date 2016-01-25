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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.TimeScalesFactory;

import edu.mit.fss.DefaultFederate;
import edu.mit.fss.examples.member.SpaceSystem;
import edu.mit.fss.examples.member.SurfaceSystem;
import edu.mit.fss.examples.member.gui.MemberFrame;
import edu.mit.fss.examples.member.gui.MultiComponentPanel;
import edu.mit.fss.examples.member.gui.SpaceSystemPanel;
import edu.mit.fss.examples.member.gui.SurfaceSystemPanel;
import edu.mit.fss.hla.DefaultAmbassador;
import hla.rti1516e.exceptions.RTIexception;

/**
 * The sample ISS federate includes a {@link SpaceSystem} object
 * using the orbital parameters of the the International Space Station 
 * and three {@link SurfaceSystem} objects using geodetic positions of
 * Keio University, SkolTech, and MIT.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public class ISSFederate extends DefaultFederate {
	private static Logger logger = Logger.getLogger("edu.mit.fss");
	
	/**
	 * The main method. This configures the Orekit data path, creates the 
	 * ISS federate objects and launches the associated graphical user 
	 * interface.
	 *
	 * @param args the arguments
	 * @throws RTIexception the RTI exception
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws RTIexception, URISyntaxException {
		BasicConfigurator.configure();

		boolean headless = false;
		
		logger.debug("Setting Orekit data path.");
		System.setProperty(DataProvidersManager.OREKIT_DATA_PATH,
		        new File(ISSFederate.class.getResource(
		        		"/orekit-data.zip").toURI()).getAbsolutePath());

		logger.trace("Creating federate instance.");
		final ISSFederate federate = new ISSFederate();

		logger.trace("Setting minimum step duration and time step.");
		long timeStep = 60*1000, minimumStepDuration = 100;
		federate.setMinimumStepDuration(minimumStepDuration);
		federate.setTimeStep(timeStep);
		
		try {
			logger.debug("Loading TLE data from file.");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					federate.getClass().getClassLoader().getResourceAsStream(
							"edu/mit/fss/examples/data.tle")));
			
			final SpaceSystem satellite;
			final SurfaceSystem station1, station2, station3;

			while(br.ready()) {
				if(br.readLine().matches(".*ISS.*")) {
					logger.debug("Found ISS data.");

					logger.trace("Adding FSS supplier space system.");
					satellite = new SpaceSystem("FSS Supplier", 
							new TLE(br.readLine(), br.readLine()), 5123e3);
					federate.addObject(satellite);

					logger.trace("Adding Keio ground station.");
					station1 = new SurfaceSystem("Keio", new GeodeticPoint(
							FastMath.toRadians(35.551929), 
							FastMath.toRadians(139.647119), 300),
							satellite.getState().getDate(), 5123e3, 5);
					federate.addObject(station1);

					logger.trace("Adding SkolTech ground station.");
					station2 = new SurfaceSystem("SkolTech", new GeodeticPoint(
							FastMath.toRadians(55.698679), 
							FastMath.toRadians(37.571994), 200),
							satellite.getState().getDate(), 5123e3, 5);
					federate.addObject(station2);

					logger.trace("Adding MIT ground station.");
					station3 = new SurfaceSystem("MIT", new GeodeticPoint(
							FastMath.toRadians(42.360184), 
							FastMath.toRadians(-71.093742), 100),
							satellite.getState().getDate(), 5123e3, 5);
					federate.addObject(station3);

					try {
						logger.trace("Setting inital time.");
				        federate.setInitialTime(
				        		satellite.getInitialState().getDate().toDate(
				        				TimeScalesFactory.getUTC()).getTime());
					} catch (IllegalArgumentException | OrekitException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}
					
					if(!headless) {
						logger.debug("Launching the graphical user interface.");
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								MemberFrame frame = new MemberFrame(federate, 
										new MultiComponentPanel(
												Arrays.asList(
														new SpaceSystemPanel(federate, satellite),
														new SurfaceSystemPanel(federate, station1),
														new SurfaceSystemPanel(federate, station2),
														new SurfaceSystemPanel(federate, station3)
												)));
								frame.pack();
								frame.setVisible(true);
							}
						});
					}
					
					break;
				}
			}
			br.close();
		} catch (InvocationTargetException | InterruptedException 
				| OrekitException | IOException e) {
			e.printStackTrace();
			logger.fatal(e);
		}

		logger.trace("Setting federate name, type, and FOM path.");
		federate.getConnection().setFederateName("ISS");
		federate.getConnection().setFederateType("FSS Supplier");
		federate.getConnection().setFederationName("FSS");
		federate.getConnection().setFomPath(
				new File(federate.getClass().getClassLoader().getResource(
						"edu/mit/fss/hla/fss.xml").toURI()).getAbsolutePath());
		federate.getConnection().setOfflineMode(false);
		federate.connect();

		if(headless) {
			federate.setMinimumStepDuration(10);
			federate.initialize();
			federate.run();
		}
	}

	/**
	 * Instantiates a new ISS federate using a 
	 * {@link DefaultAmbassador} ambassador.
	 *
	 * @throws RTIexception the RTI exception
	 */
	public ISSFederate() throws RTIexception {
		super(new DefaultAmbassador(DefaultAmbassador.PORTICO_RTI));
	}
}
