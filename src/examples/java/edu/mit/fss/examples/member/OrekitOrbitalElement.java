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
package edu.mit.fss.examples.member;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.EclipseDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import edu.mit.fss.Element;
import edu.mit.fss.OrbitalElement;
import edu.mit.fss.ReferenceFrame;
import edu.mit.fss.SimObject;
import edu.mit.fss.SurfaceElement;

/**
 * An implementation of the {@link OrbitalElement} interface using
 * the Orekit library for state propagation and geometric calculations.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class OrekitOrbitalElement implements OrbitalElement, SimObject {
	private static Logger logger = Logger.getLogger(OrekitOrbitalElement.class);
	
	private final String name;
	private final SpacecraftState initialState;
	private final long initialTime;
	
	private TLEPropagator tlePropagator;
	private SpacecraftState state;
	private OneAxisEllipsoid earth;
	private long time;
	private EclipseDetector totalEclipseDetector;
	private EclipseDetector partialEclipseDetector;

	private transient SpacecraftState nextState;
	private transient long nextTime;
	
	/**
	 * Instantiates a new Orekit orbital element.
	 *
	 * @param name the name
	 * @param initialState the initial state
	 * @throws OrekitException the orekit exception
	 */
	public OrekitOrbitalElement(String name, SpacecraftState initialState) 
			throws OrekitException {
		this.name = name;
		this.initialState = initialState;
		this.state = initialState;
		
		// set the initial time using the UTC time scale
		initialTime = initialState.getDate().toDate(
				TimeScalesFactory.getUTC()).getTime();
		
		// create an Earth ellipsoid for geometric calculations
		earth = new OneAxisEllipsoid(
				Constants.WGS84_EARTH_EQUATORIAL_RADIUS, 
				Constants.WGS84_EARTH_FLATTENING, 
				FramesFactory.getITRF2008());
		
		// create a total eclipse detector (Sun occulted by Earth, umbra)
		totalEclipseDetector = new EclipseDetector(
				CelestialBodyFactory.getSun(), 696000000.,
				CelestialBodyFactory.getEarth(), 
				Constants.WGS84_EARTH_EQUATORIAL_RADIUS, true);
		
		// create a partial eclipse detector (Sun occulted by Earth, penumbra)
		partialEclipseDetector = new EclipseDetector(
				CelestialBodyFactory.getSun(), 696000000.,
				CelestialBodyFactory.getEarth(), 
				Constants.WGS84_EARTH_EQUATORIAL_RADIUS, false);
	}

	/**
	 * Instantiates a new Orekit orbital element.
	 *
	 * @param name the name
	 * @param tle the two line elements
	 * @throws OrekitException the orekit exception
	 */
	public OrekitOrbitalElement(String name, TLE tle) throws OrekitException {
		this(name, TLEPropagator.selectExtrapolator(tle).getInitialState());
		tlePropagator = TLEPropagator.selectExtrapolator(tle);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getArgumentOfPeriapsis()
	 */
	@Override
	public double getArgumentOfPeriapsis() {
		// convert spacecraft state to Keplerian orbit
		return FastMath.toDegrees(new KeplerianOrbit(
				state.getOrbit()).getPerigeeArgument());
	}

	/**
	 * Gets this element's absolute date.
	 *
	 * @return the date
	 */
	public AbsoluteDate getDate() {
		return state.getDate();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getEccentricity()
	 */
	@Override
	public double getEccentricity() {
		return state.getE();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.demo.Element#getFrame()
	 */
	@Override
	public ReferenceFrame getFrame() {
		try {
			return ReferenceFrame.getReferenceFrame(state.getFrame());
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return ReferenceFrame.UNKNOWN;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getInclination()
	 */
	@Override
	public double getInclination() {
		return FastMath.toDegrees(state.getI());
	}
	
	/**
	 * Gets this element's initial state.
	 *
	 * @return the initial state
	 */
	public SpacecraftState getInitialState() {
		return initialState;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getLongitudeOfAscendingNode()
	 */
	@Override
	public double getLongitudeOfAscendingNode() {
		// convert spacecraft state to Keplerian orbit
		return FastMath.toDegrees(new KeplerianOrbit(
				state.getOrbit()).getRightAscensionOfAscendingNode());
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getMeanAnomaly()
	 */
	@Override
	public double getMeanAnomaly() {
		// convert spacecraft state to Keplerian orbit
		return FastMath.toDegrees(new KeplerianOrbit(
				state.getOrbit()).getMeanAnomaly());
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.demo.Element#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getNestedObjects()
	 */
	@Override
	public Collection<? extends SimObject> getNestedObjects() {
		return new HashSet<SimObject>();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.demo.Element#getPosition()
	 */
	@Override
	public Vector3D getPosition() {
		return state.getPVCoordinates().getPosition();
	}
	
	/**
	 * Gets the relative speed (in m/s) from this element 
	 * to the specified element.
	 *
	 * @param element the element
	 * @return the relative speed
	 */
	public double getRelativeSpeed(Element element) {
		if(element.getFrame() == ReferenceFrame.UNKNOWN) {
			logger.warn("Unknown reference frame for element " 
					+ element + ",  cannot compute elevation.");
			return 0;
		}
		try {
			Transform t = element.getFrame().getOrekitFrame().getTransformTo(
					getFrame().getOrekitFrame(), getDate());
			
			Vector3D relPosition = t.transformVector(element.getPosition())
					.subtract(getPosition());
			Vector3D relVelocity = t.transformVector(element.getVelocity())
					.subtract(getVelocity());
			
			// compute relative speed
			return relVelocity.dotProduct(relPosition.normalize());
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getSemimajorAxis()
	 */
	@Override
	public double getSemimajorAxis() {
		return state.getA();
	}

	/**
	 * Gets the slant range (in meters) from this element 
	 * to the specified element.
	 *
	 * @param element the element
	 * @return the slant range
	 */
	public double getSlantRange(Element element) {
		if(element.getFrame() == ReferenceFrame.UNKNOWN) {
			logger.warn("Unknown reference frame for element " 
					+ element + ",  cannot compute slant range.");
			return 0;
		}
		try {
			// use Orekit library to convert between reference frames
			Vector3D thisPosition = getPosition();
			Vector3D thatPosition = element.getFrame().getOrekitFrame()
					.getTransformTo(state.getFrame(), state.getDate())
					.transformPosition(element.getPosition());
			
			// compute vector distance
			return thisPosition.distance(thatPosition);
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return 0;
	}

	/**
	 * Gets this element's spacecraft state.
	 *
	 * @return the spacecraft state
	 */
	public SpacecraftState getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.demo.Element#getVelocity()
	 */
	@Override
	public Vector3D getVelocity() {
		return state.getPVCoordinates().getVelocity();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		state = initialState;
		this.time = initialTime;
		tick(time - initialTime);
		tock();
	}
	
	/**
	 * Checks if this element is in penumbra.
	 *
	 * @return true, if is in penumbra
	 */
	public boolean isInPenumbra() {
		try {
			return partialEclipseDetector.g(state) < 0;
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	/**
	 * Checks if this element is in umbra.
	 *
	 * @return true, if is in umbra
	 */
	public boolean isInUmbra() {
		try {
			return totalEclipseDetector.g(state) < 0;
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	/**
	 * Checks if there is a line of sight between this element 
	 * and the specified element.
	 *
	 * @param element the element
	 * @return true, if there is a line of sight
	 */
	public boolean isLineOfSightVisible(Element element) {
		if(element.getFrame() == ReferenceFrame.UNKNOWN) {
			logger.warn("Unknown reference frame for element " 
					+ element + ",  cannot compute slant range.");
			return false;
		}
		if(element instanceof OrbitalElement) {
			// for orbital elements, check for Earth occlusion
			try{
				Vector3D thisPosition = getPosition();
				Vector3D thatPosition = element.getFrame().getOrekitFrame()
						.getTransformTo(state.getFrame(), state.getDate())
						.transformPosition(element.getPosition());
				// calculate intersection point on Earth surface between elements
				// if null, there is no intersection and a line of sight exists
				return null == earth.getIntersectionPoint(new Line(thisPosition,thatPosition,0.1), 
						new Vector3D(0,0,0), state.getFrame(), state.getDate());
			} catch (OrekitException e) {
				logger.error(e.getMessage());
				return false;
			}
		} else if(element instanceof SurfaceElement) {
			// for surface elements, check the elevation angle
			SurfaceElement surf = (SurfaceElement) element;
			try {
				// create a topocentric frame at surface element
				TopocentricFrame topo = new TopocentricFrame(
						earth, new GeodeticPoint(
								FastMath.toRadians(surf.getLatitude()),
								FastMath.toRadians(surf.getLongitude()),
								surf.getAltitude()), element.getName());
				return topo.getElevation(getPosition(), 
						state.getFrame(), state.getDate()) > 0;
			} catch (OrekitException e) {
				logger.error(e.getMessage());
				return false;
			}
		} else {
			logger.warn("Unknown type of element " + element 
					+ ",  cannot compute life of sight.");
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tick(long)
	 */
	@Override
	public void tick(long duration) {
		nextTime = time + duration;
		try {
			Propagator propagator;
			if(tlePropagator != null) {
				// use TLE propagator if defined
				propagator = tlePropagator;
			} else {
				// otherwise use Keplerian propagator
				propagator = new KeplerianPropagator(state.getOrbit());
			}
			
			// propagate to next time step
			nextState = propagator.propagate(new AbsoluteDate(
					new Date(time+duration), TimeScalesFactory.getUTC()));
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tock()
	 */
	@Override
	public void tock() {
		time = nextTime;
		state = nextState;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAobject#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("DefaultSatellite { ")
				.append("name: ").append(getName())
				.append(", frame: ").append(getFrame())
				.append(", position: ").append(getPosition())
				.append(", velocity: ").append(getVelocity())
				.append(", eccentricity: ").append(getEccentricity())
				.append(", semimajor axis: ").append(getSemimajorAxis())
				.append(", inclination: ").append(getInclination())
				.append(", longitude of ascending node: ").append(getLongitudeOfAscendingNode())
				.append(", argument of periapsis: ").append(getArgumentOfPeriapsis())
				.append(", mean anomaly: ").append(getMeanAnomaly())
				.append("}").toString();
	}
}
