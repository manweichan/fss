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
import java.util.HashSet;

import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;

import edu.mit.fss.SimObject;

/**
 * A power subsystem composing a {@link SpaceSystem} element. Includes power 
 * generation and storage values for various operational states. Stored power
 * is used to handle insufficient generation and surplus power is stored up to
 * the storage capacity. This implementation does not handle 
 * insufficient/excess power storage conditions.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SpacePowerSubsystem implements SimObject {
	private static Logger logger = Logger.getLogger(SpacePowerSubsystem.class);
	
	private final SpaceSystem satellite;
	private final double nominalPowerGeneration = 1; // W
	private final double powerGenerationInPenumbra = 0; // W
	private final double powerGenerationInUmbra = 0; // W
	private final double nominalPowerConsumption = 0; // W
	private final double initialStored = 0; // W-hr
	private final double storageCapacity = 10; // W-hr
	private final double storageEfficiency = 1; // --
	private final double retrievalEfficiency = 1; // --
	
	private double powerStored; // W-hr
	private transient double nextPowerStored; // W-hr
	
	/**
	 * Instantiates a new space power subsystem.
	 *
	 * @param satellite the space system
	 */
	public SpacePowerSubsystem(SpaceSystem satellite) {
		this.satellite = satellite;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getName()
	 */
	@Override
	public String getName() {
		return satellite.getName() + " Power";
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getNestedObjects()
	 */
	@Override
	public Collection<? extends SimObject> getNestedObjects() {
		return new HashSet<SimObject>();
	}

	/**
	 * Gets this subsystem's power consumption (Watts).
	 *
	 * @return the power consumption
	 */
	public double getPowerConsumption() {
		return nominalPowerConsumption;
	}

	/**
	 * Gets this subsystem's power generation (Watts).
	 *
	 * @return the total power generation
	 */
	public double getPowerGeneration() {
		return satellite.isInUmbra()?powerGenerationInUmbra :
			(satellite.isInPenumbra()?powerGenerationInPenumbra 
					: nominalPowerGeneration);
	}

	/**
	 * Gets this subsystem's power stored (Watt-hours).
	 *
	 * @return the power stored
	 */
	public double getPowerStored() {
		return powerStored;
	}
	
	/**
	 * Gets the space system's total power consumption (Watts).
	 *
	 * @return the total power consumption
	 */
	public double getTotalPowerConsumption() {
		return satellite.getPowerConsumption();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		powerStored = initialStored;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tick(long)
	 */
	@Override
	public void tick(long duration) {
		double totalGeneration = getPowerGeneration();
		double totalConsumption = getTotalPowerConsumption();
		if(totalConsumption-totalGeneration > 0) {
			// consumption exceeds generation; compute deficit
			double deficit = duration/(1000*60*60.) 
					* (totalConsumption-totalGeneration);
			if(deficit/retrievalEfficiency <= powerStored) {
				// retrieve power and compute next stored value
				nextPowerStored = FastMath.max(0, 
						powerStored - deficit/retrievalEfficiency);
			} else {
				logger.error("Insufficient energy to meet consumption demands: " 
						+ (deficit/retrievalEfficiency - powerStored) + "W-hr");
				nextPowerStored = 0;
			}
		} else {
			// generation exceeds consumption; compute surplus
			double surplus = duration/(1000*60*60.) 
					* (totalGeneration-totalConsumption);
			if(surplus*storageEfficiency <= storageCapacity - powerStored) {
				// store surplus power and compute next stored value
				nextPowerStored = FastMath.min(storageCapacity, 
						powerStored + surplus*storageEfficiency);
			} else {
				logger.error("Excess power for storage capacity: " 
						+ (surplus*storageEfficiency 
								- (storageCapacity - powerStored)) + "W-hr");
				nextPowerStored = storageCapacity;
			}
		}
		logger.debug(getName() + " next power stored is " 
				+ nextPowerStored + "W-hr");
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tock()
	 */
	@Override
	public void tock() {
		powerStored = nextPowerStored;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder(getName()).append(" {").
				append("stored (W-hr): ").append(powerStored).
				append("}").toString();
	}
}
