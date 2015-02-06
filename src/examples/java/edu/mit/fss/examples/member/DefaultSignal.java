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
import edu.mit.fss.SimInteraction;

/**
 * A default implementation of the {@link Signal} interface. Uses an
 * immutable data structure such that no data members can be modified
 * after the object is created.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class DefaultSignal implements Signal, SimInteraction {
	private final Element element;
	private final Transmitter transmitter;
	private final String type = "Default";
	private final String content;

	/**
	 * Instantiates a new default signal.
	 *
	 * @param element the element
	 * @param transmitter the transmitter
	 * @param content the content
	 */
	public DefaultSignal(Element element, Transmitter transmitter, 
			String content) {
		this.element = element;
		this.transmitter = transmitter;
		this.content = content;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getContent()
	 */
	@Override
	public String getContent() {
		return content;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getElement()
	 */
	public Element getElement() {
		return element;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getElementName()
	 */
	@Override
	public String getElementName() {
		return element.getName();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getTransmitter()
	 */
	public Transmitter getTransmitter() {
		return transmitter;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getTransmitterName()
	 */
	@Override
	public String getTransmitterName() {
		return transmitter.getName();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
}
