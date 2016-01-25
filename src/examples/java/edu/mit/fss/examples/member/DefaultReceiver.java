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

import edu.mit.fss.Element;
import edu.mit.fss.Receiver;
import edu.mit.fss.Signal;
import edu.mit.fss.SimObject;
import edu.mit.fss.Transmitter;

/**
 * A default implementation of the {@link Receiver} interface which 
 * assigns "Default" as the type attribute. Only the state attribute is
 * mutable via the {@link #setActive(boolean)} method which assigns static 
 * values {@link #STATE_INACTIVE} ("inactive") or {@link #STATE_ACTIVE} 
 * ("active").
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public abstract class DefaultReceiver implements Receiver, SimObject {
	public static final String STATE_INACTIVE = "inactive", STATE_ACTIVE = "active";
	
	private final String name;
	private final Element element;
	private final Transmitter transmitter;
	private final String type = "Default";
	private final String initialState;
	
	private String state = "";

	/**
	 * Instantiates a new default receiver.
	 *
	 * @param name the name
	 * @param initiallyActive if the receiver is initially active
	 * @param element the controlling element
	 * @param transmitter the associated transmitter
	 */
	public DefaultReceiver(String name, boolean initiallyActive, 
			Element element, Transmitter transmitter) {
		this.name = name;
		this.initialState = initiallyActive?STATE_ACTIVE:STATE_INACTIVE;
		this.state = initialState;
		this.element = element;
		this.transmitter = transmitter;
	}
	
	/**
	 * Instantiates a new default receiver, assigning default 
	 * values to attributes.
	 */
	protected DefaultReceiver() {
		this.name = "";
		this.initialState = STATE_INACTIVE;
		this.state = initialState;
		this.element = null;
		this.transmitter = null;
	}
	
	/**
	 * Checks if this receiver can receive a signal from an element.
	 *
	 * @param element the element
	 * @return true, if a signal can be received
	 */
	public abstract boolean canReceiveFrom(Element element);
	
	/**
	 * Checks if this receiver can receive a signal from a transmitter.
	 *
	 * @param transmitter the transmitter
	 * @return true, if a signal can be received
	 */
	public abstract boolean canReceiveFrom(Transmitter transmitter);
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.RadioComponent#getElement()
	 */
	@Override
	public Element getElement() {
		return element;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.RadioComponent#getElementName()
	 */
	@Override
	public String getElementName() {
		if(element == null) {
			return "";
		} else {
			return element.getName();
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.RadioComponent#getName()
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
	 * @see edu.mit.fss.RadioReceiver#getState()
	 */
	@Override
	public String getReceiverState() {
		return state;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.RadioReceiver#getTransmitter()
	 */
	@Override
	public Transmitter getTransmitter() {
		return transmitter;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.RadioReceiver#getTransmitterName()
	 */
	@Override
	public String getTransmitterName() {
		if(transmitter == null) {
			return "";
		} else {
			return transmitter.getName();
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.RadioReceiver#getType()
	 */
	@Override
	public String getReceiverType() {
		return type;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		this.state = initialState;
	}
	
	/**
	 * Checks if this receiver is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return state == STATE_ACTIVE;
	}
	
	/**
	 * Checks if this receiver receives a signal.
	 *
	 * @param signal the signal
	 * @return true, if the signal is received
	 */
	public abstract boolean isSignalReceived(Signal signal);
	
	/**
	 * Activates or deactivates this receiver.
	 *
	 * @param active true to activate this receiver, false to deactivate
	 */
	public void setActive(boolean active) {
		if(active) {
			state = STATE_ACTIVE;
		} else {
			state = STATE_INACTIVE;
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tick(long)
	 */
	@Override
	public void tick(long duration) { }

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tock()
	 */
	@Override
	public void tock() { }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("DefaultReceiver { ")
				.append("name: ").append(getName())
				.append(", element name: ").append(getElementName())
				.append(", transmitter name: ").append(getTransmitterName())
				.append(", type: ").append(getReceiverType())
				.append(", state: ").append(getReceiverState())
				.append("}").toString();
	}
}
