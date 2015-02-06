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
package edu.mit.fss.event;

import java.util.EventObject;

/**
 * An event object which notifies of a change in a simulation time.
 * 
 * @author Paul T Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SimulationTimeEvent extends EventObject {
	private static final long serialVersionUID = -5707468210897815237L;
	
	private final long time;
	
	/**
	 * Instantiates a new execution control event.
	 *
	 * @param source the source
	 * @param time the time
	 */
	public SimulationTimeEvent(Object source, long time) {
		super(source);
		this.time = time;
	}
	
	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
}
