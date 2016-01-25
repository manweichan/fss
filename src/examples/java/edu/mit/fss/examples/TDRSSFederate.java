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

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.AbsoluteDate;
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
 * The sample TDRSS federate includes 8 {@link SpaceSystem} objects
 * using the orbital parameters of the TDRSS 3, 5-11 spacecraft.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public class TDRSSFederate extends DefaultFederate {
	private static Logger logger = Logger.getLogger("edu.mit.fss");
	
	/**
	 * The main method. This configures the Orekit data path, creates the
	 * SaudiComsat federate objects and launches the associated graphical user
	 * interface.
	 *
	 * @param args the arguments
	 * @throws RTIexception the RTI exception
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws RTIexception, URISyntaxException {
		BasicConfigurator.configure();
		
		logger.debug("Setting Orekit data path.");
		System.setProperty(DataProvidersManager.OREKIT_DATA_PATH,
		        new File(TDRSSFederate.class.getResource(
		        		"/orekit-data.zip").toURI()).getAbsolutePath());

		logger.trace("Creating federate instance.");
		final TDRSSFederate federate = new TDRSSFederate();

		logger.trace("Setting minimum step duration and time step.");
		long timeStep = 60*1000, minimumStepDuration = 100;
		federate.setMinimumStepDuration(minimumStepDuration);
		federate.setTimeStep(timeStep);

		logger.debug("Loading TLE data from file.");
		final List<Component> panels = new ArrayList<Component>();
		for(String satName : Arrays.asList("TDRS 3", "TDRS 5", "TDRS 6", 
				"TDRS 7", "TDRS 8", "TDRS 9", "TDRS 10", "TDRS 11")) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						federate.getClass().getClassLoader().getResourceAsStream(
								"edu/mit/fss/examples/data.tle")));
				
				while(br.ready()) {
					if(br.readLine().matches(".*"+satName+".*")) {
						logger.debug("Found " + satName + " data.");
						
						logger.trace("Adding " + satName + " supplier space system.");
						SpaceSystem system = new SpaceSystem(satName, 
								new TLE(br.readLine(), br.readLine()), 5123e3);
						federate.addObject(system);

						panels.add(new SpaceSystemPanel(federate, system));

						try {
							logger.trace("Setting inital time.");
							federate.setInitialTime(
									system.getInitialState().getDate().toDate(
											TimeScalesFactory.getUTC()).getTime());
						} catch (IllegalArgumentException | OrekitException e) {
							logger.error(e.getMessage());
							e.printStackTrace();
						}
						break;
					}
				}
				br.close();
			} catch (IllegalArgumentException | OrekitException | IOException e) {
				e.printStackTrace();
				logger.fatal(e);
			}
		}
		
		try {
			logger.trace("Adding WSGT ground station.");
			SurfaceSystem wsgt = new SurfaceSystem("WSGT", new GeodeticPoint(
					FastMath.toRadians(32.5007), 
					FastMath.toRadians(-106.6086), 
					1474), new AbsoluteDate(), 5123e3, 5);
			federate.addObject(wsgt);
			panels.add(new SurfaceSystemPanel(federate, wsgt));

			logger.trace("Adding STGT ground station.");
			SurfaceSystem stgt = new SurfaceSystem("STGT", new GeodeticPoint(
					FastMath.toRadians(32.5430), 
					FastMath.toRadians(-106.6120), 
					1468), new AbsoluteDate(), 5123e3, 5);
			federate.addObject(stgt);
			panels.add(new SurfaceSystemPanel(federate, stgt));

			logger.trace("Adding GRGT ground station.");
			SurfaceSystem grgt = new SurfaceSystem("GRGT", new GeodeticPoint(
					FastMath.toRadians(13.6148), 
					FastMath.toRadians(144.8565), 
					142), new AbsoluteDate(), 5123e3, 5);
			federate.addObject(grgt);
			panels.add(new SurfaceSystemPanel(federate, grgt));
		} catch (OrekitException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		logger.debug("Launching the graphical user interface.");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					MemberFrame frame = new MemberFrame(federate, 
							new MultiComponentPanel(panels));
					frame.pack();
					frame.setVisible(true);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		logger.trace("Setting federate name, type, and FOM path.");
		federate.getConnection().setFederateName("TDRSS");
		federate.getConnection().setFederateType("FSS Supplier");
		federate.getConnection().setFederationName("FSS");
		federate.getConnection().setFomPath(
				new File(federate.getClass().getClassLoader().getResource(
						"edu/mit/fss/hla/fss.xml").toURI()).getAbsolutePath());
		federate.getConnection().setOfflineMode(false);
		federate.connect();
	}

	/**
	 * Instantiates a new TDRSS federate using a 
	 * {@link DefaultAmbassador} ambassador.
	 *
	 * @throws RTIexception the RTI exception
	 */
	public TDRSSFederate() throws RTIexception {
		super(new DefaultAmbassador(DefaultAmbassador.PORTICO_RTI));
	}
}
