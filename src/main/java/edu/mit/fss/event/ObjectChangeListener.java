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

import java.util.EventListener;

/**
 * The listener interface for receiving objectChange events.
 * The class that is interested in processing a objectChange
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addObjectChangeListener<code> method. When
 * the objectChange event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ObjectChangeEvent
 * 
 * @author Paul T Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public interface ObjectChangeListener extends EventListener {
	
	/**
	 * Object discovered.
	 *
	 * @param event the event
	 */
	public void objectDiscovered(ObjectChangeEvent event);
	
	/**
	 * Object removed.
	 *
	 * @param event the event
	 */
	public void objectRemoved(ObjectChangeEvent event);
	
	/**
	 * Object changed.
	 *
	 * @param event the event
	 */
	public void objectChanged(ObjectChangeEvent event);
	
	/**
	 * Interaction occurred.
	 *
	 * @param event the event
	 */
	public void interactionOccurred(ObjectChangeEvent event);
}
