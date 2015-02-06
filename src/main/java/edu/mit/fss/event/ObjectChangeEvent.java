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
 * An event object which notifies of a change to an object during a simulation.
 * Expected objects include {@link SimObject} or {@link SimInteraction}.
 * 
 * @author Paul T Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class ObjectChangeEvent extends EventObject {
	private static final long serialVersionUID = -8758236693427617554L;

	private Object object;
	
	/**
	 * Instantiates a new object change event.
	 *
	 * @param source the source
	 * @param object the object
	 */
	public ObjectChangeEvent(Object source, Object object) {
		super(source);
		this.object = object;
	}

	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}
}
