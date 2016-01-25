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
package edu.mit.fss;

import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;

/**
 * An enumeration of known reference frames.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public enum ReferenceFrame {
	
	/** The unknown or undefined reference frame. */
	UNKNOWN(0), 
	
	/** The EME2000 Earth fixed reference frame used in Orekit,
	 *  also known as J2000. */
	EME2000(1), 
	
	/** The ITRF2008 Earth inertial reference frame as used in Orekit, 
	 * conforming to IERS 2010 conventions ignoring tidal effects. */
	ITRF2008(2), 
	
	/** The ITRF2008 Earth inertial reference frame as used in Orekit, 
	 * conforming to IERS 2010 conventions with tidal effects. */
	ITRF2008_TE(3), 
	
	/** The TEME Earth inertial reference frame as used in Orekit, 
	 * used for SGP4 propagation model for two line elements (TLEs). */
	TEME(4);
	
	/**
	 * Gets a reference frame associated with an Orekit frame. Returns
	 * {@link UNKNOWN} if no such reference frame can be determined.
	 *
	 * @param frame the Orekit frame
	 * @return the reference frame
	 * @throws OrekitException the orekit exception
	 */
	public static ReferenceFrame getReferenceFrame(Frame frame) 
			throws OrekitException {
		if(frame.equals(FramesFactory.getEME2000())) {
			return EME2000;
		}
		if(frame.equals(FramesFactory.getITRF(org.orekit.utils.IERSConventions.IERS_2010, true))) {
			return ITRF2008;
		}
		if(frame.equals(FramesFactory.getITRF(org.orekit.utils.IERSConventions.IERS_2010, false))) {
			return ITRF2008_TE;
		}
		if(frame.equals(FramesFactory.getTEME())) {
			return TEME;
		}
		return UNKNOWN; // frame can not be determined
	}
	
	/**
	 * Gets a reference frame from the passed {@link id}. Returns 
	 * {@link UNKNOWN} if no such reference frame can be determined.
	 *
	 * @param id the id
	 * @return the reference frame
	 */
	public static ReferenceFrame getReferenceFrame(int id) {
		for(ReferenceFrame frame : values()) {
			if(frame.getId()==id) {
				return frame;
			}
		}
		return UNKNOWN;
	}
	
	private final int id;
	
	/**
	 * Instantiates a new reference frame.
	 *
	 * @param id the id
	 */
	private ReferenceFrame(int id) {
		this.id = id;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets this reference frame's associated Orekit frame. Returns null if
	 * no such Orekit frame can be determined.
	 *
	 * @return the Orekit frame
	 * @throws OrekitException the Orekit exception
	 */
	public Frame getOrekitFrame() throws OrekitException {
		switch(this) {
		case UNKNOWN: return null;
		case EME2000: return FramesFactory.getEME2000();
		case ITRF2008: return FramesFactory.getITRF(org.orekit.utils.IERSConventions.IERS_2010, true);
		case ITRF2008_TE: return FramesFactory.getITRF(org.orekit.utils.IERSConventions.IERS_2010, false);
		case TEME: return FramesFactory.getTEME();
		default: return null; // frame can not be determined
		}
	}
}
