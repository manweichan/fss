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

import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIexception;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.mit.fss.Element;
import edu.mit.fss.Transmitter;
import edu.mit.fss.Signal;
import edu.mit.fss.SimInteraction;

/**
 * FSSsignal is the HLA interaction class implementing the {@link Signal} 
 * interface for communication with the RTI.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class FSSsignal extends HLAinteraction implements Signal {
	private static Logger logger = Logger.getLogger(FSSsignal.class);
	public static final String CLASS_NAME = "HLAinteractionRoot.Signal";
	
	public static final String TRANSMITTER_PARAMETER = "Transmitter",
			ELEMENT_PARAMETER = "Element",
			TYPE_PARAMETER = "Type",
			CONTENT_PARAMETER = "Content";
	
	public static final String[] PARAMETERS = new String[]{
		TRANSMITTER_PARAMETER,
		ELEMENT_PARAMETER,
		TYPE_PARAMETER,
		CONTENT_PARAMETER
	};

	/**
	 * Publishes this interaction class.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @throws RTIexception the RTI exception
	 */
	public static void publish(RTIambassador rtiAmbassador) 
			throws RTIexception {
		rtiAmbassador.publishInteractionClass(
				rtiAmbassador.getInteractionClassHandle(CLASS_NAME));
	}

	/**
	 * Subscribes to this interaction class.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @throws RTIexception the RTI exception
	 */
	public static void subscribe(RTIambassador rtiAmbassador) 
			throws RTIexception {
		rtiAmbassador.subscribeInteractionClass(
				rtiAmbassador.getInteractionClassHandle(CLASS_NAME));
	}

	private Element element;
	private Transmitter transmitter;
	private final HLAunicodeString transmitterName, elementName, type, content;
	protected final Map<ParameterHandle,DataElement> parameterValues = 
			new HashMap<ParameterHandle,DataElement>();
	
	/**
	 * Instantiates a new FSS signal.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @param encoderFactory the encoder factory
	 * @throws RTIexception the RTI exception
	 */
	public FSSsignal(RTIambassador rtiAmbassador, 
			EncoderFactory encoderFactory) throws RTIexception {
		super(rtiAmbassador);

		// create the transmitter name data element and add it as a parameter
		transmitterName = encoderFactory.createHLAunicodeString();
		parameterValues.put(
				getParameterHandle(TRANSMITTER_PARAMETER), transmitterName);

		// create the element name data element and add it as a parameter
		elementName = encoderFactory.createHLAunicodeString();
		parameterValues.put(
				getParameterHandle(ELEMENT_PARAMETER), elementName);

		// create the type data element and add it as a parameter
		type = encoderFactory.createHLAunicodeString();
		parameterValues.put(
				getParameterHandle(TYPE_PARAMETER), type);

		// create the content data element and add it as a parameter
		content = encoderFactory.createHLAunicodeString();
		parameterValues.put(
				getParameterHandle(CONTENT_PARAMETER), content);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getContent()
	 */
	@Override
	public String getContent() {
		return content.getValue();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getElement()
	 */
	@Override
	public Element getElement() {
		return element;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getElementName()
	 */
	@Override
	public String getElementName() {
		return elementName.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAinteraction#getInteractionClassName()
	 */
	@Override
	public String getInteractionClassName() {
		return CLASS_NAME;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAinteraction#getParameterNames()
	 */
	@Override
	public String[] getParameterNames() {
		return PARAMETERS;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAinteraction#getParameterValue(hla.rti1516e.ParameterHandle)
	 */
	@Override
	public DataElement getParameterValue(ParameterHandle parameterHandle) {
		return parameterValues.get(parameterHandle);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAinteraction#getSendOrder()
	 */
	@Override
	public OrderType getSendOrder() {
		return OrderType.TIMESTAMP;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getTransmitter()
	 */
	@Override
	public Transmitter getTransmitter() {
		return transmitter;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getTransmitterName()
	 */
	@Override
	public String getTransmitterName() {
		return transmitterName.getValue();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Signal#getType()
	 */
	@Override
	public String getType() {
		return type.getValue();
	}
	
	/**
	 * Sets this signal's element object reference by comparing the stored
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
	 * @see edu.mit.fss.hla.HLAinteraction#setParameters(java.lang.Object)
	 */
	@Override
	public void setParameters(SimInteraction object) {
		if(object instanceof Signal) {
			Signal signal = (Signal) object;
			transmitterName.setValue(signal.getTransmitterName());
			elementName.setValue(signal.getElementName());
			type.setValue(signal.getType());
			content.setValue(signal.getContent());
			element = signal.getElement();
			transmitter = signal.getTransmitter();
		} else {
			logger.warn("Incompatible object passed: expected " 
					+ Signal.class + " but received "
					+ object.getClass() + ".");
		}
	}

	/**
	 * Sets this signal's transmitter object reference by comparing
	 * the stored transmitter name with names of known HLA objects
	 * passed as a parameter.
	 *
	 * @param knownObjects the collection of known objects
	 */
	public void setTransmitter(Collection<? extends HLAobject> knownObjects) {
		if(transmitter == null 
				|| !transmitterName.getValue().equals(transmitter.getName())) {
			logger.trace("Searching for the associated transmitter with name " 
					+ transmitterName.getValue() + ".");
			for(HLAobject object : knownObjects) {
				if(object instanceof Transmitter 
						&& ((Transmitter)object).getName().equals(
								transmitterName.getValue())) {
					logger.trace("Found the associated transmitter " 
							+ object + ".");
					this.transmitter = ((Transmitter) object);
				}
			}
			if(transmitter == null) {
				logger.warn("Transmitter with name " + elementName.getValue() 
						+ " is not a known object instance.");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAobject#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("FSSsignal {")
				.append(" element: ").append(getElement())
				.append(", element name: ").append(getElementName())
				.append(", transmitter: ").append(getTransmitter())
				.append(", transmitter name: ").append(getTransmitterName())
				.append(", type: ").append(getType())
				.append(", content: ").append(getContent())
				.append("}").toString();
	}
}
