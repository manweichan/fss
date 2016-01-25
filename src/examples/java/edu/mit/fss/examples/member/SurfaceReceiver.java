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

import edu.mit.fss.Element;
import edu.mit.fss.Signal;
import edu.mit.fss.Transmitter;

/**
 * Extends the {@link DefaultReceiver} class for an 
 * {@link OrekitSurfaceElement} controlling element. Includes a maximum slant
 * range governing signal reception from transmitters and minimum elevation 
 * governing signal reception from elements.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public class SurfaceReceiver extends DefaultReceiver {
	private final double maxSlantRange, minElevation;
	private final OrekitSurfaceElement station;

	/**
	 * Instantiates a new surface receiver.
	 *
	 * @param name the name
	 * @param initiallyActive the initially active
	 * @param maxSlantRange the max slant range
	 * @param minElevation the min elevation
	 * @param station the station
	 * @param transmitter the transmitter
	 */
	public SurfaceReceiver(String name, boolean initiallyActive,
			double maxSlantRange, double minElevation,
			OrekitSurfaceElement station, Transmitter transmitter) {
		super(name, initiallyActive, station, transmitter);
		this.maxSlantRange = maxSlantRange;
		this.minElevation = minElevation;
		this.station = station;
	}
	
	/**
	 * Instantiates a new surface receiver, assigning default 
	 * values to attributes.
	 */
	protected SurfaceReceiver() {
		super();
		this.maxSlantRange = 0;
		this.minElevation = 0;
		this.station = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("SurfaceReceiver { ")
				.append("name: ").append(getName())
				.append(", element name: ").append(getElementName())
				.append(", transmitter name: ").append(getTransmitterName())
				.append(", type: ").append(getReceiverType())
				.append(", state: ").append(getReceiverState())
				.append("}").toString();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultReceiver#isSignalReceived(edu.mit.fss.Signal)
	 */
	@Override
	public boolean isSignalReceived(Signal signal) {
		// A signal is received if:
		// 1) receiver is active
		// 2) can receive from associated transmitter
		return isActive() && canReceiveFrom(signal.getTransmitter());
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultReceiver#canReceiveFrom(edu.mit.fss.Element)
	 */
	@Override
	public boolean canReceiveFrom(Element element) {
		// A signal can be received if:
		// 1) element elevation exceeds minimum.
		return station.getElevation(element) > minElevation;
	}


	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultReceiver#canReceiveFrom(edu.mit.fss.RadioTransmitter)
	 */
	@Override
	public boolean canReceiveFrom(Transmitter transmitter) {
		// A signal can be received if:
		// 1) can receive from transmitter's associated element and
		// 2) slant range does not exceed maximum.
		return transmitter.getElement() != null 
				&& canReceiveFrom(transmitter.getElement()) 
				&& station.getSlantRange(transmitter.getElement()) 
				< maxSlantRange;
	}
}
