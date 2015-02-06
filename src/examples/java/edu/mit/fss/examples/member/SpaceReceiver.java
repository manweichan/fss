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
import edu.mit.fss.Transmitter;
import edu.mit.fss.Signal;

/**
 * Extends the {@link DefaultReceiver} class for an 
 * {@link OrekitOrbitalElement} controlling element. Includes a maximum slant
 * range governing signal reception from transmitters.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SpaceReceiver extends DefaultReceiver {
	private final double maxSlantRange;
	private OrekitOrbitalElement satellite;

	/**
	 * Instantiates a new space receiver.
	 *
	 * @param name the name
	 * @param initiallyActive the initially active
	 * @param maxSlantRange the max slant range
	 * @param satellite the satellite
	 * @param transmitter the transmitter
	 */
	public SpaceReceiver(String name, boolean initiallyActive,
			double maxSlantRange, OrekitOrbitalElement satellite, 
			Transmitter transmitter) {
		super(name, initiallyActive, satellite, transmitter);
		this.maxSlantRange = maxSlantRange;
		this.satellite = satellite;
	}
	
	/**
	 * Instantiates a new space receiver, assigning default 
	 * values to attributes.
	 */
	protected SpaceReceiver() {
		super();
		this.maxSlantRange = 0;
		this.satellite = null;
	}
	
	@Override
	public boolean isSignalReceived(Signal signal) {
		// A signal is received if:
		// 1) receiver is active
		// 2) can receive from associated transmitter
		return isActive() && canReceiveFrom(signal.getTransmitter());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("SpaceReceiver { ")
				.append("name: ").append(getName())
				.append(", element name: ").append(getElementName())
				.append(", transmitter name: ").append(getTransmitterName())
				.append(", type: ").append(getReceiverType())
				.append(", state: ").append(getReceiverState())
				.append("}").toString();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.app.DefaultReceiver#canReceiveFrom(edu.mit.fss.Element)
	 */
	@Override
	public boolean canReceiveFrom(Element element) {
		// A signal can be received if:
		// 1) element is line-of-sight visible.
		return satellite.isLineOfSightVisible(element);
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
				&& satellite.getSlantRange(transmitter.getElement()) 
				< maxSlantRange;
	}
}
