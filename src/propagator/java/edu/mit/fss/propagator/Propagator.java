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
package edu.mit.fss.propagator;

import hla.rti1516e.exceptions.RTIexception;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import edu.mit.fss.DefaultFederate;
import edu.mit.fss.Element;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import edu.mit.fss.examples.member.OrekitOrbitalElement;
import edu.mit.fss.examples.member.OrekitSurfaceElement;
import edu.mit.fss.hla.NullAmbassador;

/**
 * The Class Propagator.
 */
public class Propagator {
	private static Logger logger = Logger.getLogger("edu.mit.fss");
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws RTIexception Signals that an RTI exception has occurred.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws OrekitException Signals that an Orekit exception has occurred.
	 * @throws URISyntaxException  Signals that a URI syntax exception has occurred.
	 */
	public static void main(String[] args) throws RTIexception, IOException, OrekitException, URISyntaxException {
		Options options = new Options();
		options.addOption("t", "input-tle", true, "input file path to spacecraft two-line elements");
		options.addOption("k", "input-keplerian", true, "input file path to spacecraft Keplerian elements");
		options.addOption("r", "input-surface", true, "input file path to surface elements");
		options.addOption("o", "output", true, "output file path (default=output.csv)");
		options.addOption("d", "duration", true, "simulation duration (days, default=1.0)");
		options.addOption("s", "time-step", true, "simulation time step (minutes, default=1.0)");
		options.addOption("f", "frame", true, "reference frame (1: Earth inertial (EME2000), 2: Earth inertial (TEME), 3: Earth fixed (ITRF2008) (default=1)");
		options.addOption("g", "max-range", true, "maximum slant range for visibility (meters, default=none)");
		options.addOption("e", "min-elevation", true, "minimum elevation for visibility (degrees, default=0.0)");
		options.addOption("n", "output-network", true, "network output file path (default=none)");

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("fss-propagator", options);
		
		String tleInputFile = null, kepInputFile = null, surfInputFile = null;
		String outputFile = null;
		String networkOutputFile = null;
		int cmdFrame = 1;
		double cmdRange = -1, cmdElevation = -1;
		double simulationDurationDays = 0;
		double timeStepMinutes = 0;
		
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if(!cmd.hasOption('t') && !cmd.hasOption('k')){
				System.err.println("Input file argument (TLE and/or Keplerian) required");
				System.exit(1);
			}
			if(cmd.hasOption('t')) {
				tleInputFile = cmd.getOptionValue('t');
			}
			if(cmd.hasOption('k')) {
				kepInputFile = cmd.getOptionValue('k');
			} 
			if(cmd.hasOption('r')) {
				surfInputFile = cmd.getOptionValue('r');
			} 
			outputFile = cmd.getOptionValue('o', "output.csv");
			simulationDurationDays = Double.parseDouble(cmd.getOptionValue('d', "1.0"));
			timeStepMinutes = Double.parseDouble(cmd.getOptionValue('s', "1.0"));
			if(cmd.hasOption('n')) {
				networkOutputFile = cmd.getOptionValue('n');
			}
			cmdFrame = Integer.parseInt(cmd.getOptionValue('f', "1"));
			cmdRange = Double.parseDouble(cmd.getOptionValue('g', "-1.0"));
			cmdElevation = Double.parseDouble(cmd.getOptionValue('e', "0.0"));
		} catch(ParseException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		} catch(NumberFormatException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		
		final Frame frame;
		if(cmdFrame==1) {
			frame = FramesFactory.getEME2000();
		} else if(cmdFrame==2) {
			frame = FramesFactory.getTEME();
		} else if(cmdFrame==3) {
			frame = FramesFactory.getITRF2008();
		} else {
			frame = FramesFactory.getEME2000();
		}
		
		final double maxRange = cmdRange;
		final double minElevation = cmdElevation;
		
		// configure the logger to handle exceptions
		BasicConfigurator.configure();
		
		// configure the orekit data path
		System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, "orekit-data.zip");
		
        // create output file if it does not exist
        Path outputPath = Paths.get(outputFile);
        if(!outputPath.toFile().exists()) {
        	outputPath.toFile().createNewFile();
        }
        
		// create an output writer to handle file output
		final BufferedWriter outputWriter = Files.newBufferedWriter(
				outputPath, Charset.defaultCharset(), 
				StandardOpenOption.TRUNCATE_EXISTING);
        
        // create network output file if does not exist (if needed)
        Path networkOutputPath = null;
        if(networkOutputFile != null) {
        	networkOutputPath = Paths.get(networkOutputFile);
        	if(!networkOutputPath.toFile().exists()) {
        		networkOutputPath.toFile().createNewFile();
        	}
        }
        // create an output writer to handle network file output (if needed)
		final BufferedWriter networkOutputWriter;
		if(networkOutputPath != null) {
			networkOutputWriter = Files.newBufferedWriter(
					networkOutputPath, Charset.defaultCharset(), 
					StandardOpenOption.TRUNCATE_EXISTING);
		} else {
			networkOutputWriter = null;
		}
		
		// create a default federate to manage the simulation
		// use a null ambassador to avoid RTI overhead
		DefaultFederate fed = new DefaultFederate(new NullAmbassador());
		
		// define simulation objects
		final List<OrekitOrbitalElement> spacecraft = 
				new ArrayList<OrekitOrbitalElement>();
		// define simulation objects
		final List<OrekitSurfaceElement> stations = 
				new ArrayList<OrekitSurfaceElement>();
		final List<Element> elements = new ArrayList<Element>();
		
		if(tleInputFile != null) {
			List<OrekitOrbitalElement> tleSpacecraft = 
					new ArrayList<OrekitOrbitalElement>();
			
			// create a buffered reader to parse tle file
			BufferedReader tleReader = Files.newBufferedReader(
					Paths.get(tleInputFile), Charset.defaultCharset());

			// while there are more lines to read...
			while(tleReader.ready()) {
				String name = tleReader.readLine();
				OrekitOrbitalElement sat = new OrekitOrbitalElement(name, 
						new TLE(tleReader.readLine(), tleReader.readLine()));
				tleSpacecraft.add(sat);
				fed.addObject(sat);
			}
			
			// close tle reader
			tleReader.close();
			
			// add elements to master list
			spacecraft.addAll(tleSpacecraft);
			elements.addAll(tleSpacecraft);

			// write a header listing the tle spacecraft
			outputWriter.write("Spacecraft using SGP4 propagator:\n");
			for(OrekitOrbitalElement s : tleSpacecraft) {
				outputWriter.write((elements.indexOf(s)+1) + ": " + 
						s.getName() + "\n");
			}
			outputWriter.write("\n");

			// write a header listing the tle spacecraft (if needed)
			if(networkOutputWriter != null) {
				networkOutputWriter.write("Spacecraft using SGP4 propagator:\n");
				for(OrekitOrbitalElement s : tleSpacecraft) {
					networkOutputWriter.write((elements.indexOf(s)+1) + ": " + 
							s.getName() + "\n");
				}
				networkOutputWriter.write("\n");
			}
		}
		
		if(kepInputFile != null) {
			List<OrekitOrbitalElement> kepSpacecraft = 
					new ArrayList<OrekitOrbitalElement>();
			
			// create a buffered reader to parse keplerian elements file
			BufferedReader kepReader = Files.newBufferedReader(
					Paths.get(kepInputFile), Charset.defaultCharset());

			Pattern kepPattern = Pattern.compile("(.*)\\s*[,;]" + // name
					"\\s*([\\d]+)\\s*[,;]" + // date
					"\\s*([\\d]+\\.*[\\d]*)\\s*[,;]" + // a
					"\\s*([\\d]+\\.*[\\d]*)\\s*[,;]" + // e
					"\\s*([-]?[\\d]+\\.*[\\d]*)\\s*[,;]" + // i
					"\\s*([-]?[\\d]+\\.*[\\d]*)\\s*[,;]" + // pa
					"\\s*([-]?[\\d]+\\.*[\\d]*)\\s*[,;]" + // raan
					"\\s*([-]?[\\d]+\\.*[\\d]*)\\s*[,;]" + // anomaly
					"\\s*(ECCENTRIC|MEAN|TRUE)\\s*"); // type
			
			// while there are more lines to read...
			while(kepReader.ready()) {
				Matcher kepMatcher = kepPattern.matcher(kepReader.readLine());
				if(kepMatcher.matches()) {
					String name = kepMatcher.group(1);
					Date date = new Date(Long.parseLong(kepMatcher.group(2)));
					double a = Double.parseDouble(kepMatcher.group(3));
					double e = Double.parseDouble(kepMatcher.group(4));
					double i = Math.toRadians(Double.parseDouble(kepMatcher.group(5)));
					double pa = Math.toRadians(Double.parseDouble(kepMatcher.group(6)));
					double raan = Math.toRadians(Double.parseDouble(kepMatcher.group(7)));
					double anomaly = Math.toRadians(Double.parseDouble(kepMatcher.group(8)));
					PositionAngle type = PositionAngle.valueOf(kepMatcher.group(9));
					OrekitOrbitalElement sat = new OrekitOrbitalElement(name, 
							new SpacecraftState(new KeplerianOrbit(
									a, e, i, pa, raan, anomaly, type, 
									FramesFactory.getEME2000(), 
									new AbsoluteDate(date, TimeScalesFactory.getUTC()), 
									Constants.WGS84_EARTH_MU)));
					/*
					a - semi-major axis (m), negative for hyperbolic orbits
					e - eccentricity
					i - inclination (rad)
					pa - perigee argument (omega, rad)
					raan - right ascension of ascending node (Omega, rad)
					anomaly - mean, eccentric or true anomaly (rad)
					type - type of anomaly
					date - date of the orbital parameters
					*/
					kepSpacecraft.add(sat);
					fed.addObject(sat);
				}
			}
			
			// close keplerian reader
			kepReader.close();

			// add elements to master list
			spacecraft.addAll(kepSpacecraft);
			elements.addAll(kepSpacecraft);

			// write a header listing the tle spacecraft
			outputWriter.write("Spacecraft using Keplerian propagator:\n");
			for(OrekitOrbitalElement s : kepSpacecraft) {
				outputWriter.write((elements.indexOf(s)+1) + ": " + 
						s.getName() + "\n");
			}
			outputWriter.write("\n");

			// write a header listing the tle spacecraft (if needed)
			if(networkOutputWriter != null) {
				networkOutputWriter.write("Spacecraft using Keplerian propagator:\n");
				for(OrekitOrbitalElement s : kepSpacecraft) {
					networkOutputWriter.write((elements.indexOf(s)+1) + ": " + 
							s.getName() + "\n");
				}
				networkOutputWriter.write("\n");
			}
		}
		
		if(surfInputFile != null) {
			List<OrekitSurfaceElement> surfStations = 
					new ArrayList<OrekitSurfaceElement>();
			
			// create a buffered reader to parse surface elements file
			BufferedReader surfReader = Files.newBufferedReader(
					Paths.get(surfInputFile), Charset.defaultCharset());

			Pattern surfPattern = Pattern.compile("(.*)\\s*[,;]" + // name
					"\\s*([\\d]+)\\s*[,;]" + // date
					"\\s*([-]?[\\d]+\\.*[\\d]*)\\s*[,;]" + 	// latitude
					"\\s*([-]?[\\d]+\\.*[\\d]*)\\s*[,;]" + 	// longitude
					"\\s*([-]?[\\d]+\\.*[\\d]*)\\s*"); 		// altitude
			
			// while there are more lines to read...
			while(surfReader.ready()) {
				Matcher surfMatcher = surfPattern.matcher(surfReader.readLine());
				if(surfMatcher.matches()) {
					String name = surfMatcher.group(1);
					Date date = new Date(Long.parseLong(surfMatcher.group(2)));
					double latitude = Math.toRadians(Double.parseDouble(surfMatcher.group(3)));
					double longitude = Math.toRadians(Double.parseDouble(surfMatcher.group(4)));
					double altitude = Double.parseDouble(surfMatcher.group(5));
					OrekitSurfaceElement surf = new OrekitSurfaceElement(name, 
							new GeodeticPoint(latitude, longitude, altitude),
							new AbsoluteDate(date, TimeScalesFactory.getUTC()));
					surfStations.add(surf);
					fed.addObject(surf);
				}
			}
			
			// close surface reader
			surfReader.close();

			// add elements to master list
			stations.addAll(surfStations);
			elements.addAll(surfStations);

			// write a header listing the tle spacecraft
			outputWriter.write("Ground stations:\n");
			for(OrekitSurfaceElement s : surfStations) {
				outputWriter.write((elements.indexOf(s)+1) + ": " + 
						s.getName() + "\n");
			}
			outputWriter.write("\n");

			// write a header listing the tle spacecraft (if needed)
			if(networkOutputWriter != null) {
				networkOutputWriter.write("Ground stations:\n");
				for(OrekitSurfaceElement s : surfStations) {
					networkOutputWriter.write((elements.indexOf(s)+1) + ": " + 
							s.getName() + "\n");
				}
				networkOutputWriter.write("\n");
			}
		}
		
		// write a header (%15s is 15 characters (left-padded) for a string)
		outputWriter.write("Outputs in reference frame: " + frame.getName() + "\n\n");
		outputWriter.write(String.format(Locale.US,"%15s", "Time (ms)"));
		for(int i = 0; i < elements.size(); i++) {
			outputWriter.write(String.format(Locale.US,
					";%15s;%15s;%15s;%15s;%15s;%15s",
					(i+1)+"-Px (m)", (i+1)+"-Py (m)", (i+1)+"-Pz (m)", 
					(i+1)+"-Vx (m/s)", (i+1)+"-Vy (m/s)", (i+1)+"-Vz (m/s)"));
		}
		outputWriter.write("\n");
		
		// write a header (%15s is 15 characters (left-padded) for a string)
		if(networkOutputWriter != null) {
			networkOutputWriter.write("Maximum slant range: " 
					+ (maxRange<0?"unlimited":(maxRange+" m")) +"\n");
			networkOutputWriter.write("Minimum elevation: " 
					+ (minElevation<0?"unlimited":(minElevation+" deg")) +"\n\n");
			networkOutputWriter.write(String.format(Locale.US,"%15s", "Time (ms)"));
			for(int i = 0; i < elements.size(); i++) {
				for(int j = 0; j < elements.size(); j++) {
					networkOutputWriter.write(String.format(Locale.US,
							";%10s", "m_"+(i+1)+"_"+(j+1)));
				}
			}
			networkOutputWriter.write("\n");
		}
		
		// set the initial based on most recent tle data 
		long initialTime = 0;
		for(OrekitOrbitalElement s : spacecraft) {
			long t = s.getInitialState().getDate().toDate(
    				TimeScalesFactory.getUTC()).getTime();
			initialTime = Math.max(initialTime,  t);
		}
        fed.setInitialTime(initialTime);
        
        // set for desired simulation duration
        fed.setFinalTime(fed.getInitialTime() 
        		+ (long) Math.floor(simulationDurationDays*24*60*60*1000));
        
        // set time step and run as-fast-as-possible
        fed.setTimeStep((long) Math.floor(timeStepMinutes*60*1000));
        fed.setMinimumStepDuration(0);
		
		// add a simulation time listener to write output at each time step
		fed.addSimulationTimeListener(new SimulationTimeListener() {
			@Override
			public void timeAdvanced(SimulationTimeEvent event) {
				try {
					// write the outputs 
					// %15d is 15 characters (left-padded) for an integer
					// %15.2f is 15 characters (left-padded) for a floating-point with 2 decimals
					outputWriter.write(String.format(Locale.US,"%15d", event.getTime()));
					for(Element e : elements) {
						try {
							Vector3D position = e.getFrame().getOrekitFrame()
									.getTransformTo(frame, new AbsoluteDate(
											new Date(event.getTime()), 
											TimeScalesFactory.getUTC()))
									.transformPosition(e.getPosition());
							Vector3D velocity = e.getFrame().getOrekitFrame()
									.getTransformTo(frame, new AbsoluteDate(
											new Date(event.getTime()), 
											TimeScalesFactory.getUTC()))
									.transformPosition(e.getVelocity());
							
							outputWriter.write(String.format(Locale.US,
									";%15.2f;%15.2f;%15.2f;%15.2f;%15.2f;%15.2f",
									position.getX(), 
									position.getY(), 
									position.getZ(), 
									velocity.getX(),
									velocity.getY(),
									velocity.getZ()));
						} catch(OrekitException ex) {
							ex.printStackTrace();
						}
					}
					outputWriter.write("\n");

					if(networkOutputWriter != null) {
						networkOutputWriter.write(String.format(Locale.US,"%15d", event.getTime()));
						for(int i = 0; i < elements.size(); i++) {
							for(int j = 0; j < elements.size(); j++) {
								if(i==j) {
									networkOutputWriter.write(String.format(Locale.US,";%10s", "1"));
								} else if(elements.get(i) instanceof OrekitOrbitalElement) {
									OrekitOrbitalElement s = (OrekitOrbitalElement) elements.get(i);
									boolean visible = s.isLineOfSightVisible(elements.get(j));
									if(maxRange >=0) {
										visible = visible && (s.getSlantRange(elements.get(j)) <= maxRange);
									}
									if(elements.get(j) instanceof OrekitSurfaceElement) {
										OrekitSurfaceElement sj = (OrekitSurfaceElement) elements.get(j);
										visible = visible && (sj.getElevation(s) > minElevation);
									}
									networkOutputWriter.write(String.format(Locale.US,";%10s", 
											(visible?"1":"0")));
								} else if(elements.get(i) instanceof OrekitSurfaceElement) {
									OrekitSurfaceElement s = (OrekitSurfaceElement) elements.get(i);
									boolean visible = s.getElevation(elements.get(j)) > minElevation;
									if(maxRange >=0) {
										visible = visible && (s.getSlantRange(elements.get(j)) <= maxRange);
									}
									networkOutputWriter.write(String.format(Locale.US,";%10s", 
											(visible?"1":"0")));
								}
							}
						}
						networkOutputWriter.write("\n");
					}
				} catch (IOException e) {
					logger.error(e);
				}
			}
		});
		
		// initialize and run the federate
		fed.getConnection().setOfflineMode(true);
		fed.connect();
		
		fed.initialize();
		fed.run();
		
		// close the output writer after finishing
		outputWriter.close();
	}
}
