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
 * {@link SurfaceSystem} element and {@link SurfaceReceiver} receiver.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SurfaceCommSubsystem implements CommSubsystem, SimObject {
	private final SurfaceSystem station;
	private final SurfaceReceiver receiver;
	private final DefaultTransmitter transmitter;
	
	/**
	 * Instantiates a new surface communications subsystem.
	 *
	 * @param station the surface system
	 * @param maxSlantRange the maximum slant range
	 * @param minElevation the minimum elevation
	 */
	public SurfaceCommSubsystem(SurfaceSystem station, 
			double maxSlantRange, double minElevation) {
		this.station = station;
		transmitter = new DefaultTransmitter(station.getName() + " TX",
				false, station);
		receiver = new SurfaceReceiver(station.getName() + " RX", 
				false, maxSlantRange, minElevation, station, transmitter);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getName()
	 */
	@Override
	public String getName() {
		return station.getName() + " Comm";
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getNestedObjects()
	 */
	@Override
	public Collection<? extends SimObject> getNestedObjects() {
		// transmitter and receiver are nested objects
		return Arrays.asList(transmitter, receiver);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.app.CommSubsystem#getReceiver()
	 */
	@Override
	public SurfaceReceiver getReceiver() {
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
