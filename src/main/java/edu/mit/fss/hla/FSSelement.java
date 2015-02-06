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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.Logger;

import edu.mit.fss.Element;
import edu.mit.fss.ReferenceFrame;
import edu.mit.fss.SimObject;

/**
 * FSSelement is the HLA object class implementing the {@link Element} 
 * interface for communication with the RTI.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class FSSelement extends HLAobject implements Element {
	private static Logger logger = Logger.getLogger(FSSelement.class);
	public static final String CLASS_NAME = "HLAobjectRoot.Element";
	
	public static final String NAME_ATTRIBUTE = "Name",
			FRAME_ATTRIBUTE = "Frame",
			POSITION_ATTRIBUTE = "Position",
			VELOCITY_ATTRIBUTE = "Velocity";
	
	public static final String[] ATTRIBUTES = new String[]{
		NAME_ATTRIBUTE,
		FRAME_ATTRIBUTE,
		POSITION_ATTRIBUTE,
		VELOCITY_ATTRIBUTE
	};

	/**
	 * Publishes all of this object class's attributes.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @throws RTIexception the RTI exception
	 */
	public static void publishAll(RTIambassador rtiAmbassador) 
			throws RTIexception {
		AttributeHandleSet handles = 
				rtiAmbassador.getAttributeHandleSetFactory().create();
		for(String attributeName : ATTRIBUTES) {
			handles.add(rtiAmbassador.getAttributeHandle(
					rtiAmbassador.getObjectClassHandle(CLASS_NAME), 
					attributeName));
		}
		rtiAmbassador.publishObjectClassAttributes(
				rtiAmbassador.getObjectClassHandle(CLASS_NAME), handles);
	}
	

	/**
	 * Subscribes to all of this object class's attributes.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @throws RTIexception the RTI exception
	 */
	public static void subscribeAll(RTIambassador rtiAmbassador) 
			throws RTIexception {
		AttributeHandleSet handles = 
				rtiAmbassador.getAttributeHandleSetFactory().create();
		for(String attributeName : ATTRIBUTES) {
			handles.add(rtiAmbassador.getAttributeHandle(
					rtiAmbassador.getObjectClassHandle(CLASS_NAME), 
					attributeName));
		}
		rtiAmbassador.subscribeObjectClassAttributes(
				rtiAmbassador.getObjectClassHandle(CLASS_NAME), handles);
	}

	private final HLAunicodeString name;
	private final FSSreferenceFrame frame;
	private final FSScartesianVector position, velocity;
	
	/**
	 * Instantiates a new FSS element. The object is interpreted as local
	 * if {@link instanceName} is null and is interpreted as remote if 
	 * {@link instanceName} is not null.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @param encoderFactory the encoder factory
	 * @param instanceName the instance name
	 * @throws RTIexception the RTI exception
	 */
	public FSSelement(RTIambassador rtiAmbassador, 
			EncoderFactory encoderFactory, String instanceName) 
					throws RTIexception {
		super(rtiAmbassador, instanceName);
		
		logger.trace("Creating the name data element, " 
				+ "adding it as an attribute, "
				+ " and setting the send order.");
		name = encoderFactory.createHLAunicodeString();
		attributeValues.put(getAttributeHandle(NAME_ATTRIBUTE),  name);
		sendOrderMap.put(getAttributeHandle(NAME_ATTRIBUTE), 
				OrderType.RECEIVE);

		logger.trace("Creating the frame data element, " 
				+ "adding it as an attribute, "
				+ " and setting the send order.");
		frame = new FSSreferenceFrame(encoderFactory);
		attributeValues.put(getAttributeHandle(FRAME_ATTRIBUTE), frame);
		sendOrderMap.put(getAttributeHandle(FRAME_ATTRIBUTE), 
				OrderType.RECEIVE);

		logger.trace("Creating the position data element, " 
				+ "adding it as an attribute, "
				+ " and setting the send order.");
		position = new FSScartesianVector(encoderFactory);
		attributeValues.put(getAttributeHandle(POSITION_ATTRIBUTE), position);
		sendOrderMap.put(getAttributeHandle(POSITION_ATTRIBUTE), 
				OrderType.TIMESTAMP);

		logger.trace("Creating the velocity data element, " 
				+ "adding it as an attribute, "
				+ " and setting the send order.");
		velocity = new FSScartesianVector(encoderFactory);
		attributeValues.put(getAttributeHandle(VELOCITY_ATTRIBUTE), velocity);
		sendOrderMap.put(getAttributeHandle(VELOCITY_ATTRIBUTE), 
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
	 * @see edu.mit.fss.Element#getFrame()
	 */
	@Override
	public ReferenceFrame getFrame() {
		return frame.getValue();
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
	 * @see edu.mit.fss.Element#getPosition()
	 */
	@Override
	public Vector3D getPosition() {
		return position.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Element#getVelocity()
	 */
	@Override
	public Vector3D getVelocity() {
		return velocity.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAobject#setAttributes(java.lang.Object)
	 */
	@Override
	public void setAttributes(SimObject object) {
		if(object instanceof Element) {
			Element element = (Element) object;
			name.setValue(element.getName());
			frame.setValue(element.getFrame());
			position.setValue(element.getPosition());
			velocity.setValue(element.getVelocity());
		} else {
			logger.warn("Incompatible object passed: expected " 
					+ Element.class + " but received "
					+ object.getClass() + ".");
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAobject#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("FSSelement { name: ")
				.append(getName()).append(", frame: ").append(getFrame())
				.append(", position: ").append(getPosition().toString())
				.append(", velocity: ").append(getVelocity().toString())
				.append("}").toString();
	}
}
