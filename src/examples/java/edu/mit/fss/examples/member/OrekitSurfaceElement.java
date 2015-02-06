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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import edu.mit.fss.Element;
import edu.mit.fss.ReferenceFrame;
import edu.mit.fss.SimObject;
import edu.mit.fss.SurfaceElement;

/**
 * An implementation of the {@link SurfaceElement} interface using the 
 * Orekit library for geometric calculations. This implementation assumes
 * a fixed (i.e. zero velocity) element.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class OrekitSurfaceElement implements SurfaceElement, SimObject {
	private static Logger logger = Logger.getLogger(OrekitSurfaceElement.class);
	
	private final ReferenceFrame frame = ReferenceFrame.ITRF2008;
	private final String name;
	private final GeodeticPoint position;
	private final long initialTime;

	private long time;
	private TopocentricFrame topoFrame;

	private transient long nextTime;
	
	/**
	 * Instantiates a new default station.
	 *
	 * @param name the name
	 * @param initialPosition the initial position
	 * @param initialDate the initial date
	 * @throws OrekitException the orekit exception
	 */
	public OrekitSurfaceElement(String name, GeodeticPoint initialPosition, 
			AbsoluteDate initialDate) throws OrekitException {
		this.name = name;
		this.position = initialPosition;
		
		// create the initial time using the UTC timescale
		this.initialTime = initialDate.toDate(
				TimeScalesFactory.getUTC()).getTime();

		// create a topocentric frame centered at the initial position
		topoFrame = new TopocentricFrame(new OneAxisEllipsoid(
						Constants.WGS84_EARTH_EQUATORIAL_RADIUS, 
						Constants.WGS84_EARTH_FLATTENING, 
						frame.getOrekitFrame()), 
						position, getName());
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getElevation()
	 */
	@Override
	public double getAltitude() {
		return position.getAltitude();
	}
	
	/**
	 * Gets the azimuth (in degrees) to the specified element.
	 *
	 * @param element the element
	 * @return the azimuth to
	 */
	public double getAzimuth(Element element) {
		if(element.getFrame() == ReferenceFrame.UNKNOWN) {
			logger.warn("Unknown reference frame for element " 
					+ element + ",  cannot compute azimuth.");
			return 0;
		}
		try {
			// use topocentric frame to compute azimuth
			return FastMath.toDegrees(topoFrame.getAzimuth(
					element.getPosition(), 
					element.getFrame().getOrekitFrame(), getDate()));
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return 0;
	}
	
	/**
	 * Gets this element's absolute date.
	 *
	 * @return the date
	 */
	public AbsoluteDate getDate() throws OrekitException {
		return new AbsoluteDate(new Date(time), TimeScalesFactory.getUTC());
	}
	
	/**
	 * Gets the elevation (in degrees) to the specified element.
	 *
	 * @param element the element
	 * @return the elevation to
	 */
	public double getElevation(Element element) {
		if(element.getFrame() == ReferenceFrame.UNKNOWN) {
			logger.warn("Unknown reference frame for element " 
					+ element + ",  cannot compute elevation.");
			return 0;
		}
		try {
			// use topocentric frame to compute elevation angle
			return FastMath.toDegrees(topoFrame.getElevation(
					element.getPosition(), 
					element.getFrame().getOrekitFrame(), getDate()));
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getFrame()
	 */
	@Override
	public ReferenceFrame getFrame() {
		return frame;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getLatitude()
	 */
	@Override
	public double getLatitude() {
		return FastMath.toDegrees(position.getLatitude());
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getLongitude()
	 */
	@Override
	public double getLongitude() {
		return FastMath.toDegrees(position.getLongitude());
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getNestedObjects()
	 */
	@Override
	public Collection<? extends SimObject> getNestedObjects() {
		return new HashSet<SimObject>();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getPosition()
	 */
	@Override
	public Vector3D getPosition() {
		Vector3D cartesianPosition = null;
		try {
			// transform (0,0,0) in topocentric frame to element frame
			cartesianPosition = topoFrame.getTransformTo(
					frame.getOrekitFrame(), getDate())
							.transformPosition(new Vector3D(0,0,0));
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return cartesianPosition;
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
					frame.getOrekitFrame(), getDate());
			
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

	/**
	 * Gets the slant range (in meters) from this element 
	 * to the specified element.
	 *
	 * @param element the element
	 * @return the slant range to
	 */
	public double getSlantRange(Element element) {
		if(element.getFrame() == ReferenceFrame.UNKNOWN) {
			logger.warn("Unknown reference frame for element " 
					+ element + ",  cannot compute elevation.");
			return 0;
		}
		try {
			// use Orekit library to convert between reference frames
			Vector3D thisPosition = new Vector3D(0,0,0);
			Vector3D thatPosition = element.getFrame().getOrekitFrame()
					.getTransformTo(topoFrame, getDate())
					.transformPosition(element.getPosition());

			// compute vector distance
			return thisPosition.distance(thatPosition);
		} catch (OrekitException e) {
			logger.error(e.getMessage());
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getVelocity()
	 */
	@Override
	public Vector3D getVelocity() {
		return new Vector3D(0,0,0);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		this.time = initialTime;
		tick(time - initialTime);
		tock();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tick(long)
	 */
	@Override
	public void tick(long duration) {
		nextTime = time + duration;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tock()
	 */
	@Override
	public void tock() {
		time = nextTime;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAobject#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("DefaultStation { ")
				.append("name: ").append(getName())
				.append(", frame: ").append(getFrame())
				.append(", position: ").append(getPosition())
				.append(", velocity: ").append(getVelocity())
				.append(", latitude: ").append(getLatitude())
				.append(", longitude: ").append(getLongitude())
				.append(", elevation: ").append(getAltitude())
				.append("}").toString();
	}
}
