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

/**
 * An Earth surface element with Geodetic coordinate attributes.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public interface SurfaceElement extends Element {
	
	/**
	 * Gets this element's altitude measured in meters above the mean sea 
	 * level (MSL) datum.
	 *
	 * @return the altitude
	 */
	public double getAltitude();
	
	/**
	 * Gets this element's latitude measured in degrees North using the 
	 * WGS 84 datum.
	 *
	 * @return the latitude
	 */
	public double getLatitude();
	
	/**
	 * Gets this element's longitude measured in degrees East using the 
	 * WGS 84 datum.
	 *
	 * @return the longitude
	 */
	public double getLongitude();
}
