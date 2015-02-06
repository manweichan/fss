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
 * The listener interface for receiving executionControl events.
 * The class that is interested in processing a executionControl
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addExecutionControlListener<code> method. When
 * the executionControl event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ExecutionControlEvent
 * 
 * @author Paul T Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public interface ExecutionControlListener extends EventListener {
	
	/**
	 * Execution terminated.
	 *
	 * @param event the event
	 */
	public void executionTerminated(ExecutionControlEvent event);
	
	/**
	 * Execution initialized.
	 *
	 * @param event the event
	 */
	public void executionInitialized(ExecutionControlEvent event);
	
	/**
	 * Execution started.
	 *
	 * @param event the event
	 */
	public void executionStarted(ExecutionControlEvent event);
	
	/**
	 * Execution stopped.
	 *
	 * @param event the event
	 */
	public void executionStopped(ExecutionControlEvent event);
}
