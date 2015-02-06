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

import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfloat64BE;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * FSScartesianVector implements the CartesianVector FOM data element, 
 * corresponding to the {@link Vector3D} implementation. It stores three 
 * {@link HLAfloat64BE} data elements (x, y, and z) using a
 * {@link HLAfixedArray} array encoding.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public final class FSScartesianVector implements DataElement {
	private final HLAfixedArray<HLAfloat64BE> fixedArray;
	private final HLAfloat64BE x, y, z;
	
	/**
	 * Instantiates a new FSS Cartesian vector with component (x, y, z) 
	 * elements initialized with values from a passed {@link vector}.
	 *
	 * @param encoderFactory the encoder factory
	 * @param vector the vector
	 */
	public FSScartesianVector(EncoderFactory encoderFactory, Vector3D vector) {
		this(encoderFactory);
		setValue(vector);
	}
	
	/**
	 * Instantiates a new FSS Cartesian vector with component (x, y, z) 
	 * elements initialized to zero.
	 *
	 * @param encoderFactory the encoder factory
	 */
	public FSScartesianVector(EncoderFactory encoderFactory) {
		x = encoderFactory.createHLAfloat64BE();
		y = encoderFactory.createHLAfloat64BE();
		z = encoderFactory.createHLAfloat64BE();
		fixedArray = encoderFactory.createHLAfixedArray(x, y, z);
	}
	
	/**
	 * Gets this vector as a {@link Vector3D} object.
	 *
	 * @return the value
	 */
	public Vector3D getValue() {
		return new Vector3D(x.getValue(), y.getValue(), z.getValue());
	}
	
	/**
	 * Sets this vector from a {@link Vector3D} object.
	 *
	 * @param value the new value
	 */
	public void setValue(Vector3D value) {
		this.x.setValue(value.getX());
		this.y.setValue(value.getY());
		this.z.setValue(value.getZ());
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#getOctetBoundary()
	 */
	@Override
	public int getOctetBoundary() {
		return fixedArray.getOctetBoundary();
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#encode(hla.rti1516e.encoding.ByteWrapper)
	 */
	@Override
	public void encode(ByteWrapper byteWrapper) throws EncoderException {
		fixedArray.encode(byteWrapper);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#getEncodedLength()
	 */
	@Override
	public int getEncodedLength() {
		return fixedArray.getEncodedLength();
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#toByteArray()
	 */
	@Override
	public byte[] toByteArray() throws EncoderException {
		return fixedArray.toByteArray();
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#decode(hla.rti1516e.encoding.ByteWrapper)
	 */
	@Override
	public void decode(ByteWrapper byteWrapper) throws DecoderException {
		fixedArray.decode(byteWrapper);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#decode(byte[])
	 */
	@Override
	public void decode(byte[] bytes) throws DecoderException {
		fixedArray.decode(bytes);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getValue().toString();
	}
}