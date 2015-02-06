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

import java.util.Arrays;
import java.util.Collection;

import edu.mit.fss.SimObject;

/**
 * An implementation of the {@link CommSubsystem} interface composing a 
 * {@link SpaceSystem} element and {@link SpaceReceiver} receiver. 
 * Includes power consumption values for various operational states.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SpaceCommSubsystem implements CommSubsystem, SimObject {
	private final SpaceSystem satellite;
	private final SpaceReceiver receiver;
	private final DefaultTransmitter transmitter;
	
	private final double nominalPowerConsumption = 0.25; // W
	private final double activeRxPowerConsumption = 0.25; // W
	private final double activeTxPowerConsumption = 0.5; // W
	
	/**
	 * Instantiates a new space communications subsystem.
	 *
	 * @param satellite the space system
	 * @param maxSlantRange the maximum slant range
	 */
	public SpaceCommSubsystem(SpaceSystem satellite, 
			double maxSlantRange) {
		this.satellite = satellite;
		transmitter = new DefaultTransmitter(satellite.getName() + " TX", 
				false, satellite);
		receiver = new SpaceReceiver(satellite.getName() + " RX", 
				false, maxSlantRange, satellite, transmitter);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getName()
	 */
	@Override
	public String getName() {
		return satellite.getName() + " Comm";
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getNestedObjects()
	 */
	@Override
	public Collection<? extends SimObject> getNestedObjects() {
		// transmitter and receiver are nested objects
		return Arrays.asList(transmitter, receiver);
	} 
	
	/**
	 * Gets the power consumption.
	 *
	 * @return the power consumption
	 */
	public double getPowerConsumption() {
		return nominalPowerConsumption + 
				(transmitter.isActive()?activeTxPowerConsumption:0) +
				(receiver.isActive()?activeRxPowerConsumption:0);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.app.CommSubsystem#getReceiver()
	 */
	@Override
	public SpaceReceiver getReceiver() {
		return receiver;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.app.CommSubsystem#getTransmitter()
	 */
	@Override
	public DefaultTransmitter getTransmitter() {
		return transmitter;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		receiver.initialize(time);
		transmitter.initialize(time);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tick(long)
	 */
	@Override
	public void tick(long duration) {
		receiver.tick(duration);
		transmitter.tick(duration);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tock()
	 */
	@Override
	public void tock() {
		receiver.tock();
		transmitter.tock();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder(getName()).append(" {").
				append("transmitter: ").append(transmitter).
				append(", receiver: ").append(receiver).
				append("}").toString();
	}
}
