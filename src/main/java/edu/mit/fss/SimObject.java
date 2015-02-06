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

import java.util.Collection;

/**
 * A persistent object to be time-stepped during simulation. The 
 * {@link #initialize(long)} method initializes the object to an initial
 * time. The {@link #tick(long)} method computes state changes for a 
 * specified duration and the {@link #tock()} method commits these changes.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public interface SimObject {
	
	/**
	 * Initializes this simulation object to an initial time {@link time}
	 * measured in milliseconds since January 1, 1970 (Unix Epoch).
	 *
	 * @param time the time
	 */
	public void initialize(long time);
	
	/**
	 * Computes state changes for this simulation object over
	 * time duration {@link duration} in milliseconds. This method should
	 * not change any visible state attribute for this object.
	 *
	 * @param duration the duration
	 */
	public void tick(long duration);
	
	/**
	 * Commits state changes for this simulation object.
	 */
	public void tock();
	
	/**
	 * Gets a collection of all nested simulation objects contained w
	 * ithin this simulation object.
	 *
	 * @return the nested objects
	 */
	public Collection<? extends SimObject> getNestedObjects();
	
	/**
	 * Gets this object's unique name. Global uniqueness is required to
	 * establish object associations.
	 *
	 * @return the name
	 */
	public String getName();
}
