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

import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;

import edu.mit.fss.SimObject;

/**
 * A space system composing a {@link SpacePowerSubsystem} 
 * and {@link SpaceCommSubsystem} subsystem components.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SpaceSystem extends OrekitOrbitalElement {
	private final SpacePowerSubsystem powerSubsystem;
	private final SpaceCommSubsystem commSubsystem;
	
	/**
	 * Instantiates a new space system.
	 *
	 * @param name the name
	 * @param initialState the initial state
	 * @param maxSlantRange the maximum slant range
	 * @throws OrekitException the orekit exception
	 */
	public SpaceSystem(String name, SpacecraftState initialState, 
			double maxSlantRange) throws OrekitException {
		super(name, initialState);
		powerSubsystem = new SpacePowerSubsystem(this);
		commSubsystem = new SpaceCommSubsystem(this, maxSlantRange);
	}
	
	/**
	 * Instantiates a new space system.
	 *
	 * @param name the name
	 * @param tle the two line elements
	 * @param maxSlantRange the maximum slant range
	 * @throws OrekitException the orekit exception
	 */
	public SpaceSystem(String name, TLE tle, double maxSlantRange) 
			throws OrekitException {
		super(name, tle);
		powerSubsystem = new SpacePowerSubsystem(this);
		commSubsystem = new SpaceCommSubsystem(this, maxSlantRange);
	}

	/**
	 * Gets the communications subsystem.
	 *
	 * @return the communications subsystem
	 */
	public SpaceCommSubsystem getCommSubsystem() {
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

	/**
	 * Gets the power consumption (Watts).
	 *
	 * @return the power consumption
	 */
	public double getPowerConsumption() {
		return commSubsystem.getPowerConsumption()
				+ powerSubsystem.getPowerConsumption();
	}

	/**
	 * Gets the power subsystem.
	 *
	 * @return the power subsystem
	 */
	public SpacePowerSubsystem getPowerSubsystem() {
		return powerSubsystem;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultSatellite#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		super.initialize(time);
		powerSubsystem.initialize(time);
		commSubsystem.initialize(time);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultSatellite#tick(long)
	 */
	@Override
	public void tick(long duration) {
		super.tick(duration);
		powerSubsystem.tick(duration);
		commSubsystem.tick(duration);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultSatellite#tock()
	 */
	@Override
	public void tock() {
		super.tock();
		powerSubsystem.tock();
		commSubsystem.tock();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder("Space System {").
				append("Power: ").append(powerSubsystem).
				append(", Comm: ").append(commSubsystem).
				append("}").toString();
	}
}
