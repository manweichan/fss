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

import edu.mit.fss.OrbitalElement;
import edu.mit.fss.SimObject;

/**
 * FSSorbitalElement is the HLA object class implementing the 
 * {@link OrbitalElement} interface for communication with the RTI.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class FSSorbitalElement extends FSSelement implements OrbitalElement {
	private static Logger logger = Logger.getLogger(FSSorbitalElement.class);
	public static final String CLASS_NAME = "HLAobjectRoot.Element.OrbitalElement";
	
	public static final String ECCENTRICITY_ATTRIBUTE = "Eccentricity",
			SEMIMAJOR_AXIS_ATTRIBUTE = "SemimajorAxis",
			INCLINATION_ATTRIBUTE = "Inclination",
			LONGITUDE_ASCENDING_NODE_ATTRIBUTE = "LongitudeOfAscendingNode",
			ARGUMENT_OF_PERIAPSIS_ATTRIBUTE = "ArgumentOfPeriapsis",
			MEAN_ANOMALY_ATTRIBUTE = "MeanAnomaly";
	
	public static final String[] ATTRIBUTES = new String[]{
		NAME_ATTRIBUTE,
		FRAME_ATTRIBUTE,
		POSITION_ATTRIBUTE,
		VELOCITY_ATTRIBUTE,
		ECCENTRICITY_ATTRIBUTE,
		SEMIMAJOR_AXIS_ATTRIBUTE,
		INCLINATION_ATTRIBUTE,
		LONGITUDE_ASCENDING_NODE_ATTRIBUTE,
		ARGUMENT_OF_PERIAPSIS_ATTRIBUTE,
		MEAN_ANOMALY_ATTRIBUTE
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
	
	private final HLAfloat64BE eccentricity;
	private final HLAfloat64BE semimajorAxis;
	private final HLAfloat64BE inclination;
	private final HLAfloat64BE longitudeOfAscendingNode;
	private final HLAfloat64BE argumentOfPeriapsis;
	private final HLAfloat64BE meanAnomaly;
	
	/**
	 * Instantiates a new FSS orbital element. The object is interpreted as 
	 * local if {@link instanceName} is null and is interpreted as remote 
	 * if {@link instanceName} is not null.
	 *
	 * @param rtiAmbassador the RTI ambassador
	 * @param encoderFactory the encoder factory
	 * @param instanceName the instance name
	 * @throws RTIexception the RTI exception
	 */
	public FSSorbitalElement(RTIambassador rtiAmbassador, 
			EncoderFactory encoderFactory, String instanceName) 
					throws RTIexception {
		super(rtiAmbassador, encoderFactory, instanceName);

		// create the eccentricity data element, add it as an attribute, 
		// and set the send order
		eccentricity = encoderFactory.createHLAfloat64BE();
		attributeValues.put(getAttributeHandle(ECCENTRICITY_ATTRIBUTE),  
				eccentricity);
		sendOrderMap.put(getAttributeHandle(ECCENTRICITY_ATTRIBUTE), 
				OrderType.TIMESTAMP);
		
		// create the semimajor axis data element, add it as an attribute, 
		// and set the send order
		semimajorAxis = encoderFactory.createHLAfloat64BE();
		attributeValues.put(getAttributeHandle(SEMIMAJOR_AXIS_ATTRIBUTE), 
				semimajorAxis);
		sendOrderMap.put(getAttributeHandle(SEMIMAJOR_AXIS_ATTRIBUTE), 
				OrderType.TIMESTAMP);

		// create the inclination data element, add it as an attribute, 
		// and set the send order
		inclination = encoderFactory.createHLAfloat64BE();
		attributeValues.put(getAttributeHandle(INCLINATION_ATTRIBUTE), 
				inclination);
		sendOrderMap.put(getAttributeHandle(INCLINATION_ATTRIBUTE), 
				OrderType.TIMESTAMP);

		// create the LAAN data element, add it as an attribute, 
		// and set the send order
		longitudeOfAscendingNode = encoderFactory.createHLAfloat64BE();
		attributeValues.put(
				getAttributeHandle(LONGITUDE_ASCENDING_NODE_ATTRIBUTE), 
				longitudeOfAscendingNode);
		sendOrderMap.put(
				getAttributeHandle(LONGITUDE_ASCENDING_NODE_ATTRIBUTE), 
				OrderType.TIMESTAMP);
		
		// create the argument of periapsis data element, add it as an 
		// attribute, and set the send order
		argumentOfPeriapsis = encoderFactory.createHLAfloat64BE();
		attributeValues.put(
				getAttributeHandle(ARGUMENT_OF_PERIAPSIS_ATTRIBUTE), 
				argumentOfPeriapsis);
		sendOrderMap.put(
				getAttributeHandle(ARGUMENT_OF_PERIAPSIS_ATTRIBUTE), 
				OrderType.TIMESTAMP);

		// create the mean anomaly data element, add it as an attribute, 
		// and set the send order
		meanAnomaly = encoderFactory.createHLAfloat64BE();
		attributeValues.put(getAttributeHandle(MEAN_ANOMALY_ATTRIBUTE), 
				meanAnomaly);
		sendOrderMap.put(getAttributeHandle(MEAN_ANOMALY_ATTRIBUTE), 
				OrderType.TIMESTAMP);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getArgumentOfPeriapsis()
	 */
	@Override
	public double getArgumentOfPeriapsis() {
		return argumentOfPeriapsis.getValue();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.sips.hla.HLAobject#getAttributeNames()
	 */
	@Override
	public String[] getAttributeNames() {
		return ATTRIBUTES;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getEccentricity()
	 */
	@Override
	public double getEccentricity() {
		return eccentricity.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getInclination()
	 */
	@Override
	public double getInclination() {
		return inclination.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getLongitudeOfAscendingNode()
	 */
	@Override
	public double getLongitudeOfAscendingNode() {
		return longitudeOfAscendingNode.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getMeanAnomaly()
	 */
	@Override
	public double getMeanAnomaly() {
		return meanAnomaly.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.sips.hla.HLAobject#getObjectClassName()
	 */
	@Override
	public String getObjectClassName() {
		return CLASS_NAME;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.OrbitalElement#getSemimajorAxis()
	 */
	@Override
	public double getSemimajorAxis() {
		return semimajorAxis.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSelement#setAttributes(java.lang.Object)
	 */
	@Override
	public void setAttributes(SimObject object) {
		super.setAttributes(object);
		if(object instanceof OrbitalElement) {
			OrbitalElement orbitalElement = (OrbitalElement) object;
			eccentricity.setValue(orbitalElement.getEccentricity());
			semimajorAxis.setValue(orbitalElement.getSemimajorAxis());
			inclination.setValue(orbitalElement.getInclination());
			longitudeOfAscendingNode.setValue(
					orbitalElement.getLongitudeOfAscendingNode());
			argumentOfPeriapsis.setValue(
					orbitalElement.getArgumentOfPeriapsis());
			meanAnomaly.setValue(orbitalElement.getMeanAnomaly());
		} else {
			logger.warn("Incompatible object passed: expected " 
					+ OrbitalElement.class + " but received "
					+ object.getClass() + ".");
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.HLAobject#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("FSSorbitalElement { ")
				.append("name: ").append(getName())
				.append(", frame: ").append(getFrame())
				.append(", position: ").append(getPosition())
				.append(", velocity: ").append(getVelocity())
				.append(", eccentricity: ").append(getEccentricity())
				.append(", semimajor axis: ").append(getSemimajorAxis())
				.append(", inclination: ").append(getInclination())
				.append(", longitude of ascending node: ")
				.append(getLongitudeOfAscendingNode())
				.append(", argument of periapsis: ")
				.append(getArgumentOfPeriapsis())
				.append(", mean anomaly: ").append(getMeanAnomaly())
				.append("}").toString();
	}
}
