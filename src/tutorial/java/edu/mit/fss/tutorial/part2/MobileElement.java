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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.mit.fss.ReferenceFrame;
import edu.mit.fss.SimObject;
import edu.mit.fss.SurfaceElement;

/**
 * A simple surface element which updates position using (inaccurate) 
 * Euler integration of velocity and reports geodetic coordinates 
 * using an (inaccurate) spherical Earth model.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class MobileElement implements SurfaceElement {
	private String name;
	private Vector3D position, nextPosition, initialPosition;
	private Vector3D velocity, nextVelocity, initialVelocity;
	
	/**
	 * Instantiates a new mobile element.
	 *
	 * @param name the name
	 */
	public MobileElement(String name, Vector3D initialVelocity) {
		this.name = name;
		position = new Vector3D(0, 0, 0);
		initialPosition = new Vector3D(0, 0, 0);
		velocity = new Vector3D(0, 0, 0);
		this.initialVelocity = initialVelocity;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getAltitude()
	 */
	@Override
	public double getAltitude() {
		return position.getNorm() - 6371e3;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getFrame()
	 */
	@Override
	public ReferenceFrame getFrame() {
		return ReferenceFrame.ITRF2008;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getLatitude()
	 */
	@Override
	public double getLatitude() {
		return Math.toDegrees(
				Math.atan2(position.getZ(),position.getX()));
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getLongitude()
	 */
	@Override
	public double getLongitude() {
		return Math.toDegrees(
				Math.atan2(position.getY(),position.getX()));
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getName()
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
		return new ArrayList<SimObject>();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getPosition()
	 */
	@Override
	public Vector3D getPosition() {
		return position;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getVelocity()
	 */
	@Override
	public Vector3D getVelocity() {
		return velocity;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		position = initialPosition;
		nextPosition = initialPosition;
		velocity = initialVelocity;
		nextVelocity = initialVelocity;
	}
	
	/**
	 * Sets the velocity.
	 *
	 * @param velocity the new velocity
	 */
	public void setVelocity(Vector3D velocity) {
		nextVelocity = velocity;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tick(long)
	 */
	@Override
	public void tick(long duration) {
		nextPosition = position.add(
				velocity.scalarMultiply(duration/1000d));
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tock()
	 */
	@Override
	public void tock() {
		position = nextPosition;
		velocity = nextVelocity;
	}
}
