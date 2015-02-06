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
 * An Earth-orbiting element with orbital element attributes. Note all 
 * angular quantities are measured in degrees.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public interface OrbitalElement extends Element {
	
	/**
	 * Gets this element's orbital argument of periapsis in degrees.
	 *
	 * @return the argument of periapsis
	 */
	public double getArgumentOfPeriapsis();
	
	/**
	 * Gets this element's orbital eccentricity.
	 *
	 * @return the eccentricity
	 */
	public double getEccentricity();
	
	/**
	 * Gets this element's orbital inclination in degrees.
	 *
	 * @return the inclination
	 */
	public double getInclination();
	
	/**
	 * Gets this element's orbital longitude of ascending node in degrees.
	 *
	 * @return the longitude of ascending node
	 */
	public double getLongitudeOfAscendingNode();
	
	/**
	 * Gets this element's orbital mean anomaly in degrees.
	 *
	 * @return the mean anomaly
	 */
	public double getMeanAnomaly();
	
	/**
	 * Gets this element's orbital semimajor axis in meters.
	 *
	 * @return the semimajor axis
	 */
	public double getSemimajorAxis();
}
