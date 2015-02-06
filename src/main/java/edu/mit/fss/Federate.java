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

import edu.mit.fss.event.ExecutionControlListener;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.event.SimulationTimeListener;
import edu.mit.fss.hla.FederationConnection;

/**
 * A federate controls a simulation execution, notifying any associated 
 * {@link ExecutionControlListener}, {@link ObjectChangeListener}, or 
 * {@link SimulationTimeListener} objects of events. 
 * <p>
 * Before running a simulation execution, the initial time should be set
 * with the {@link #setInitialTime(long)} method and the simulation time
 * step with the {@link #setTimeStep(long)} method. A mutable federation 
 * connection is retrieved with the {@link #getConnection()} method and 
 * the federate can connect or disconnect from a federation using the 
 * {@link #connect()} and {@link #disconnect()} methods.
 * <p>
 * To run a simulation execution, the federate must first initialize 
 * it using {@link #initialize()} method, then run the simulation using the
 * {@link #run()} method. The {@link #stop()} method stops a simulation
 * execution before the next time step, and the {@link #terminate()} 
 * method terminates a simulation execution. Finally, the {@link #exit()} 
 * method exits the application.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public interface Federate {
	
	/**
	 * Adds a execution control listener to this federate.
	 *
	 * @param listener the listener
	 */
	public void addExecutionControlListener(ExecutionControlListener listener);
	
	/**
	 * Adds a simulation object to this federate.
	 *
	 * @param object the object
	 */
	public void addObject(SimObject object);
	
	/**
	 * Adds an object change listener to this federate.
	 *
	 * @param listener the listener
	 */
	public void addObjectChangeListener(ObjectChangeListener listener);
	
	/**
	 * Adds a simulation time listener to this federate.
	 *
	 * @param listener the listener
	 */
	public void addSimulationTimeListener(SimulationTimeListener listener);
	
	/**
	 * Connects this federate to a federation execution specified by the 
	 * connection in {@link #getConnection()}.
	 */
	public void connect();
	
	/**
	 * Disconnects this federate from a federation execution specified by the 
	 * connection in {@link #getConnection()}.
	 */
	public void disconnect();
	
	/**
	 * Exits this federate's application.
	 */
	public void exit();
	
	/**
	 * Gets this federate's federation execution connection.
	 *
	 * @return the connection
	 */
	public FederationConnection getConnection();
	
	/**
	 * Gets this federate's final time.
	 *
	 * @return the final time
	 */
	public long getFinalTime();
	
	/**
	 * Gets this federate's initial time.
	 *
	 * @return the initial time
	 */
	public long getInitialTime();
	
	/**
	 * Gets this federate's minimum step duration measured in milliseconds. 
	 * The minimum step duration is the minimum amount of wallclock (real) 
	 * time for each {@link @tickTock()} method call.
	 *
	 * @return the minimum step duration
	 */
	public long getMinimumStepDuration();
	
	/**
	 * Gets this federate's simulation time step measured in milliseconds. 
	 * The time step is the duration of simulation time advanced for each 
	 * simulation state update.
	 *
	 * @return the time step
	 */
	public long getTimeStep();
	
	/**
	 * Initializes this federate's federation execution and notifies any
	 * associated execution control event listeners. This method acquires 
	 * any required resources and invokes the 
	 * {@link SimObject.initialize(long)} method for each simulation object
	 * using this federate's initial time specified by {@link #getInitialTime()}.
	 */
	public void initialize();
	
	/**
	 * Removes an execution control listener from this federate.
	 *
	 * @param listener the listener
	 */
	public void removeExecutionControlListener(ExecutionControlListener listener);
	
	/**
	 * Removes a simulation object from this federate.
	 *
	 * @param object the object
	 */
	public void removeObject(SimObject object);
	
	/**
	 * Removes an object change listener from this federate.
	 *
	 * @param listener the listener
	 */
	public void removeObjectChangeListener(ObjectChangeListener listener);
	
	/**
	 * Removes a simulation time listener from this federate.
	 *
	 * @param listener the listener
	 */
	public void removeSimulationTimeListener(SimulationTimeListener listener);
	
	/**
	 * Runs this federate's simulation execution until the {@link #getFinalTime()}
	 * is achieved while notifying any
	 * associated execution control event listeners. Issues continuous calls 
	 * to the {@link #tickTock()} method which may be stopped using the 
	 * {@link #stop()} method or terminated using the {@link #terminate()} 
	 * method. Note the {@link #stop()} or {@link #terminate()} methods must
	 * be called from a different thread.
	 */
	public void run();
	
	/**
	 * Sends an interaction from this federate.
	 *
	 * @param interaction the interaction
	 */
	public void sendInteraction(SimInteraction interaction);
	
	/**
	 * Sets this federate's final time for the next time step 
	 * measured in milliseconds since January 1, 1970 (Unix Epoch).
	 *
	 * @param finalTime the new final time
	 */
	public void setFinalTime(long finalTime);
	
	/**
	 * Sets this federate's initial time for the next initialization to 
	 * {@link initialTime} measured in milliseconds since January 1, 1970 
	 * (Unix Epoch).
	 *
	 * @param initialTime the new initial time
	 */
	public void setInitialTime(long initialTime);
	
	/**
	 * Sets this federate's minimum step duration for the next time step
	 * measured in milliseconds. The minimum step duration is the minimum 
	 * amount of wallclock (real) time for each {@link @tickTock()} method 
	 * call.
	 *
	 * @param minimumStepDuration the new minimum step duration
	 */
	public void setMinimumStepDuration(long minimumStepDuration);
	
	/**
	 * Sets this federate's simulation next time step to {@link timeStep} 
	 * measured in milliseconds. Smaller time steps lessen error 
	 * in component models at the cost of reduced performance.
	 *
	 * @param timeStep the new time step
	 */
	public void setTimeStep(long timeStep);
	
	/**
	 * Stops this federate's simulation execution before the next time step 
	 * and notifies any associated execution control event listeners. 
	 * The execution may be resumed using the {@link #run()} method or 
	 * terminated using the {@link #terminate()} method.
	 */
	public void stop();
	
	/**
	 * Terminates this federate's simulation execution and notifies any
	 * associated execution control event listeners. This method stops the
	 * simulation execution and releases required resources.
	 */
	public void terminate();
	
	/**
	 * Advances the simulation by one time step and notifies any associated
	 * simulation time listeners. Invokes the {@link SimObject.tick(long)} 
	 * method for each simulation object using the time step specified 
	 * in {@link #getTimeStep()} followed by the {@link SimObject.tock()} 
	 * method for each object. This method shall have a minimum wallclock 
	 * duration specified by {@link #getMinimumStepDuration()}.
	 */
	public void tickTock();
}