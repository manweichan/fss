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
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.exceptions.RTIexception;

import org.apache.log4j.Logger;

import edu.mit.fss.SimObject;
import edu.mit.fss.SurfaceElement;

/**
 * FSSsurfaceElement is the HLA object class implementing the 
 * {@link SurfaceElement} interface for communication with the RTI.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class FSSsurfaceElement extends FSSelement implements SurfaceElement {
	private static Logger logger = Logger.getLogger(FSSsurfaceElement.class);
	public static final String CLASS_NAME = "HLAobjectRoot.Element.SurfaceElement";
	
	public static final String LATITUDE_ATTRIBUTE = "Latitude",
			LONGITUDE_ATTRIBUTE = "Longitude",
			ALTITUDE_ATTRIBUTE = "Altitude";
	
	public static final String[] ATTRIBUTES = new String[]{
		NAME_ATTRIBUTE,
		FRAME_ATTRIBUTE,
		POSITION_ATTRIBUTE,
		VELOCITY_ATTRIBUTE,
		LATITUDE_ATTRIBUTE,
		LONGITUDE_ATTRIBUTE,
		ALTITUDE_ATTRIBUTE
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
	
	private final HLAfloat64BE latitude;
	private final HLAfloat64BE longitude;
	private final HLAfloat64BE altitude;
	
	/**
	 * Instantiates a new FSS surface element. The object is interpreted as 
	 * local if {@link instanceName} is null and is interpreted as remote 
	 * if {@link instanceName} is not null.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @param encoderFactory the encoder factory
	 * @param instanceName the instance name
	 * @throws RTIexception the RTI exception
	 */
	public FSSsurfaceElement(RTIambassador rtiAmbassador, 
			EncoderFactory encoderFactory, String instanceName) 
					throws RTIexception {
		super(rtiAmbassador, encoderFactory, instanceName);
		
		// create the latitude data element, add it as an attribute, 
		// and set the send order
		latitude = encoderFactory.createHLAfloat64BE();
		attributeValues.put(getAttributeHandle(LATITUDE_ATTRIBUTE), 
				latitude);
		sendOrderMap.put(getAttributeHandle(LATITUDE_ATTRIBUTE), 
				OrderType.TIMESTAMP);

		// create the longitude data element, add it as an attribute, 
		// and set the send order
		longitude = encoderFactory.createHLAfloat64BE();
		attributeValues.put(getAttributeHandle(LONGITUDE_ATTRIBUTE), 
				longitude);
		sendOrderMap.put(getAttributeHandle(LONGITUDE_ATTRIBUTE), 
				OrderType.TIMESTAMP);

		// create the altitude data element, add it as an attribute, 
		// and set the send order
		altitude = encoderFactory.createHLAfloat64BE();
		attributeValues.put(getAttributeHandle(ALTITUDE_ATTRIBUTE), 
				altitude);
		sendOrderMap.put(getAttributeHandle(ALTITUDE_ATTRIBUTE), 
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
	 * @see edu.mit.sips.hla.HLAobject#getObjectClassName()
	 */
	@Override
	public String getObjectClassName() {
		return CLASS_NAME;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSelement#setAttributes(java.lang.Object)
	 */
	@Override
	public void setAttributes(SimObject object) {
		super.setAttributes(object);
		if(object instanceof SurfaceElement) {
			SurfaceElement surfaceElement = (SurfaceElement) object;
			latitude.setValue(surfaceElement.getLatitude());
			longitude.setValue(surfaceElement.getLongitude());
			altitude.setValue(surfaceElement.getAltitude());
		} else {
			logger.warn("Incompatible object passed: expected " 
					+ SurfaceElement.class + " but received "
					+ object.getClass() + ".");
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getElevation()
	 */
	@Override
	public double getAltitude() {
		return altitude.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getLatitude()
	 */
	@Override
	public double getLatitude() {
		return latitude.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SurfaceElement#getLongitude()
	 */
	@Override
	public double getLongitude() {
		return longitude.getValue();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAobject#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("FSSsurfaceElement { ")
				.append("name: ").append(getName())
				.append(", frame: ").append(getFrame())
				.append(", position: ").append(getPosition())
				.append(", velocity: ").append(getVelocity())
				.append(", latitude: ").append(getLatitude())
				.append(", longitude: ").append(getLongitude())
				.append(", altitude: ").append(getAltitude())
				.append("}").toString();
	}
}
