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

import org.orekit.bodies.GeodeticPoint;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;

import edu.mit.fss.SimObject;

/**
 * A surface system composing a {@link SurfaceCommSubsystem} 
 * subsystem component.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SurfaceSystem extends OrekitSurfaceElement {
	private SurfaceCommSubsystem commSubsystem;
	
	/**
	 * Instantiates a new surface system.
	 *
	 * @param name the name
	 * @param initialPosition the initial position
	 * @param initialDate the initial date
	 * @throws OrekitException 
	 */
	public SurfaceSystem(String name, GeodeticPoint initialPosition, 
			AbsoluteDate initialDate, double maxSlantRange, 
			double minElevation) throws OrekitException {
		super(name, initialPosition, initialDate);
		commSubsystem = new SurfaceCommSubsystem(this, 
				maxSlantRange, minElevation);
	}

	/**
	 * Gets the communications subsystem.
	 *
	 * @return the communications subsystem
	 */
	public SurfaceCommSubsystem getCommSubsystem() {
		return commSubsystem;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getNestedObjects()
	 */
	@Override
	public Collection<? extends SimObject> getNestedObjects() {
		// only communications subsystem has nested objects
		return commSubsystem.getNestedObjects();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultSatellite#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		super.initialize(time);
		commSubsystem.initialize(time);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultSatellite#tick(long)
	 */
	@Override
	public void tick(long duration) {
		super.tick(duration);
		commSubsystem.tick(duration);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultSatellite#tock()
	 */
	@Override
	public void tock() {
		super.tock();
		commSubsystem.tock();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder("Surface System {").
				append("Comm: ").append(commSubsystem).
				append("}").toString();
	}
}
