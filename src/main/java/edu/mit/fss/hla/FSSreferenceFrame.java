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

import edu.mit.fss.ReferenceFrame;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger32BE;

/**
 * FSSreferenceFrame implements the ReferenceFrame FOM data element, 
 * corresponding to the {@link ReferenceFrame} implementation.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public class FSSreferenceFrame implements DataElement {
	private final HLAinteger32BE value;
	
	/**
	 * Instantiates a new FSS reference frame with value
	 * initialized to the unknown frame.
	 *
	 * @param encoderFactory the encoder factory
	 */
	public FSSreferenceFrame(EncoderFactory encoderFactory) {
		value = encoderFactory.createHLAinteger32BE();
	}
	
	/**
	 * Instantiates a new FSS reference frame with value 
	 * initialized to the passed @link frame}.
	 *
	 * @param encoderFactory the encoder factory
	 * @param frame the frame
	 */
	public FSSreferenceFrame(EncoderFactory encoderFactory, 
			ReferenceFrame frame) {
		this(encoderFactory);
		setValue(frame);
	}
	
	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#decode(byte[])
	 */
	@Override
	public void decode(byte[] bytes) throws DecoderException {
		value.decode(bytes);
	}
	
	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#decode(hla.rti1516e.encoding.ByteWrapper)
	 */
	@Override
	public void decode(ByteWrapper byteWrapper) throws DecoderException {
		value.decode(byteWrapper);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#encode(hla.rti1516e.encoding.ByteWrapper)
	 */
	@Override
	public void encode(ByteWrapper byteWrapper) throws EncoderException {
		value.encode(byteWrapper);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#getEncodedLength()
	 */
	@Override
	public int getEncodedLength() {
		return value.getEncodedLength();
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#getOctetBoundary()
	 */
	@Override
	public int getOctetBoundary() {
		return value.getOctetBoundary();
	}

	/**
	 * Gets this data element as a {@link ReferenceFrame} object.
	 *
	 * @return the value
	 */
	public ReferenceFrame getValue() {
		return ReferenceFrame.getReferenceFrame(value.getValue());
	}

	/**
	 * Sets this data element from a {@link ReferenceFrame} object.
	 *
	 * @param frame the new value
	 */
	public void setValue(ReferenceFrame frame) {
		value.setValue(frame.getId());
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.encoding.DataElement#toByteArray()
	 */
	@Override
	public byte[] toByteArray() throws EncoderException {
		return value.toByteArray();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append(getClass().getSimpleName()).
				append("<").append(getValue().toString()).append(">").toString();
	}
}
