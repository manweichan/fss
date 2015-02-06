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
package edu.mit.fss.hla;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.OrderType;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIexception;

import java.util.Collection;

import org.apache.log4j.Logger;

import edu.mit.fss.Element;
import edu.mit.fss.SimObject;
import edu.mit.fss.Transmitter;

/**
 * {@link FSStransmitter} is the HLA object class implementing the 
 * {@link Transmitter} interface for communication with the RTI.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class FSStransmitter extends HLAobject implements Transmitter {
	private static Logger logger = Logger.getLogger(FSStransmitter.class);
	public static final String CLASS_NAME = "HLAobjectRoot.RadioComponent.Transmitter";
	
	public static final String NAME_ATTRIBUTE = "Name",
			ELEMENT_ATTRIBUTE = "Element",
			TYPE_ATTRIBUTE = "Type",
			STATE_ATTRIBUTE = "State";
	
	public static final String[] ATTRIBUTES = new String[]{
		NAME_ATTRIBUTE,
		ELEMENT_ATTRIBUTE,
		TYPE_ATTRIBUTE,
		STATE_ATTRIBUTE
	};

	/**
	 * Publishes all of this object class's attributes.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @throws RTIexception the RTI exception
	 */
	public static void publishAll(RTIambassador rtiAmbassador) 
			throws RTIexception {
		AttributeHandleSet attributeHandleSet = 
				rtiAmbassador.getAttributeHandleSetFactory().create();
		for(String attributeName : ATTRIBUTES) {
			attributeHandleSet.add(rtiAmbassador.getAttributeHandle(
					rtiAmbassador.getObjectClassHandle(CLASS_NAME), 
					attributeName));
		}
		rtiAmbassador.publishObjectClassAttributes(
				rtiAmbassador.getObjectClassHandle(CLASS_NAME), 
				attributeHandleSet);
	}
	
	/**
	 * Subscribes to all of this object class's attributes.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @throws RTIexception the RTI exception
	 */
	public static void subscribeAll(RTIambassador rtiAmbassador) 
			throws RTIexception {
		AttributeHandleSet attributeHandleSet = 
				rtiAmbassador.getAttributeHandleSetFactory().create();
		for(String attributeName : ATTRIBUTES) {
			attributeHandleSet.add(rtiAmbassador.getAttributeHandle(
					rtiAmbassador.getObjectClassHandle(CLASS_NAME), 
					attributeName));
		}
		rtiAmbassador.subscribeObjectClassAttributes(
				rtiAmbassador.getObjectClassHandle(CLASS_NAME), 
				attributeHandleSet);
	}

	private Element element;
	private final HLAunicodeString name;
	private final HLAunicodeString elementName;
	private final HLAunicodeString type;
	private final HLAunicodeString state;
	
	/**
	 * Instantiates a new {@link FSStransmitter} object interpreted as 
	 * local if {@link instanceName} is null and remote if 
	 * {@link instanceName} is not null.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @param encoderFactory the encoder factory
	 * @param instanceName the instance name
	 * @throws RTIexception the RTI exception
	 */
	public FSStransmitter(RTIambassador rtiAmbassador, 
			EncoderFactory encoderFactory, String instanceName) 
					throws RTIexception {
		super(rtiAmbassador, instanceName);

		// create the name data element, add it as an attribute, 
		// and set the send order
		name = encoderFactory.createHLAunicodeString();
		attributeValues.put(getAttributeHandle(NAME_ATTRIBUTE),  name);
		sendOrderMap.put(getAttributeHandle(NAME_ATTRIBUTE), 
				OrderType.RECEIVE);


		// create the element name data element, add it as an attribute, 
		// and set the send order
		elementName = encoderFactory.createHLAunicodeString();
		attributeValues.put(getAttributeHandle(ELEMENT_ATTRIBUTE), 
				elementName);
		sendOrderMap.put(getAttributeHandle(ELEMENT_ATTRIBUTE), 
				OrderType.TIMESTAMP);

		// create the state data element, add it as an attribute, 
		// and set the send order
		state = encoderFactory.createHLAunicodeString();
		attributeValues.put(getAttributeHandle(STATE_ATTRIBUTE),  state);
		sendOrderMap.put(getAttributeHandle(STATE_ATTRIBUTE), 
				OrderType.TIMESTAMP);

		// create the type data element, add it as an attribute, 
		// and set the send order
		type = encoderFactory.createHLAunicodeString();
		attributeValues.put(getAttributeHandle(TYPE_ATTRIBUTE), type);
		sendOrderMap.put(getAttributeHandle(TYPE_ATTRIBUTE), 
				OrderType.TIMESTAMP);
	}

	/* (non-Javadoc)
	 * @see edu.mit.sips.hla.HLAobject#getAttributeNames()
	 */
	@Override
	public String[] getAttributeNames() {
		return ATTRIBUTES;
	}

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
		return elementName.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getName()
	 */
	@Override
	public String getName() {
		return name.getValue();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.sips.hla.HLAobject#getObjectClassName()
	 */
	@Override
	public String getObjectClassName() {
		return CLASS_NAME;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.RadioTransmitter#getState()
	 */
	@Override
	public String getTransmitterState() {
		return state.getValue();
	}
	

	/* (non-Javadoc)
	 * @see edu.mit.fss.RadioTransmitter#getType()
	 */
	@Override
	public String getTransmitterType() {
		return type.getValue();
	}
	
	public void setAttributes(SimObject object) {
		if(object instanceof Transmitter) {
			Transmitter transmitter = (Transmitter)object;
			name.setValue(transmitter.getName());
			elementName.setValue(transmitter.getElementName());
			state.setValue(transmitter.getTransmitterState());
			type.setValue(transmitter.getTransmitterType());
		} else {
			logger.warn("Incompatible object passed: expected " 
					+ Transmitter.class + " but received "
					+ object.getClass() + ".");
		}
	}

	/**
	 * Sets this transmitter's element object reference by comparing the stored
	 * element name with names of known HLA objects passed as a parameter.
	 *
	 * @param knownElements the collection of known objects
	 */
	public void setElement(Collection<? extends HLAobject> knownObjects) {
		if(element == null 
				|| !elementName.getValue().equals(element.getName())) {
			logger.trace("Searching for the associated element with name " 
					+ elementName.getValue() + ".");
			for(HLAobject object : knownObjects) {
				if(object instanceof Element 
						&& ((Element)object).getName().equals(
								elementName.getValue())) {
					logger.trace("Found the associated element " 
								+ object + ".");
					this.element = (Element) object;
				}
			}
			if(element == null) {
				logger.warn("Element with name " + elementName.getValue() 
						+ " is not a known object instance.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAobject#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("FSSradioTransmitter {")
				.append(" name: ").append(getName())
				.append(", element: ").append(getElementName())
				.append(", type: ").append(getTransmitterType())
				.append(", state: ").append(getTransmitterState())
				.append("}").toString();
	}
}
