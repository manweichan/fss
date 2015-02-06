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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * An arbitrary element having Cartesian position and velocity 
 * attributes measured in an associated reference frame.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public interface Element extends SimObject {
	
	/**
	 * Gets this element's reference frame.
	 *
	 * @return the reference frame
	 */
	public ReferenceFrame getFrame();
	
	/**
	 * Gets this element's Cartesian position measured in its 
	 * associated reference frame.
	 *
	 * @return the position
	 */
	public Vector3D getPosition();
	
	/**
	 * Gets this element's Cartesian velocity measured in its 
	 * associated reference frame.
	 *
	 * @return the velocity
	 */
	public Vector3D getVelocity();
}
