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
package edu.mit.fss.hla;

import hla.rti1516e.FederateAmbassador;
import edu.mit.fss.SimInteraction;
import edu.mit.fss.SimObject;
import edu.mit.fss.event.ObjectChangeListener;

/**
 * An ambassador interfaces with an HLA runtime implementation (RTI) to
 * send and receive object attribute updates and interactions. The general
 * life cycle of a federation execution includes: connecting to the RTI with
 * {@link #connect()}, initializing the federation execution with 
 * {@link #initialize(long, long)}, advancing time with {@link #advanceTo()}, 
 * terminating a federation execution with {@link #terminate()}, and finally
 * disconnecting from the RTI with {@link #disconnect()}.
 * <p>
 * The ambassador fires object change events when local objects are created, 
 * updated, or deleted (via {@link #scheduleObjectCreation(SimObject)}, 
 * {@link #scheduleObjectUpdate(SimObject)}, and 
 * {@link #scheduleObjectDeletion(SimObject)} methods), when local interactions
 * are sent (via the {@link #scheduleInteraction(SimInteraction)} method), when
 * remote objects are discovered, updated, or deleted (via 
 * {@link #discoverObjectInstance(ObjectInstanceHandle, ObjectClassHandle, 
 * String)}, {@link #reflectAttributeValues(ObjectInstanceHandle, 
 * AttributeHandleValueMap, byte[], OrderType, TransportationTypeHandle, 
 * SupplementalReflectInfo)}, and {@link #removeObjectInstance(
 * ObjectInstanceHandle, byte[], OrderType, SupplementalRemoveInfo)}
 * methods), and when remote interactions are received (via the
 * {@link #receiveInteraction(InteractionClassHandle, ParameterHandleValueMap, 
 * byte[], OrderType, TransportationTypeHandle, SupplementalReceiveInfo)}
 * method).
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public interface FSSambassador extends FederateAmbassador {
	
	/**
	 * Adds an object change listener to this ambassador.
	 *
	 * @param listener the listener
	 */
	public void addObjectChangeListener(ObjectChangeListener listener);
	
	/**
	 * Advances simulation time to {@link time}.
	 *
	 * @param time the time
	 */
	public void advanceTo(long time);
	
	/**
	 * Connects this ambassador to the HLA runtime infrastructure (RTI).
	 */
	public void connect();
	
	/**
	 * Disconnects this ambassador from the HLA runtime infrastructure (RTI).
	 */
	public void disconnect();
	
	/**
	 * Gets this ambassador's federation connection.
	 *
	 * @return the connection
	 */
	public FederationConnection getConnection();
	
	/**
	 * Initializes this ambassador's federation execution with an initial time 
	 * and lookahead interval. Initialization shall create and join a federation
	 * execution, enable asynchronous delivery and time constrained and time
	 * regulating behavior, publish and subscribe all desired object and 
	 * interaction classes, and advance to the initial (or current) logical
	 * simulation time. Returns the current logical time which may not
	 * equal the initial time if other federates have previously joined and 
	 * advanced time beyond it.
	 *
	 * @param initialTime the initial time
	 * @param lookahead the lookahead
	 */
	public long initialize(long initialTime, long lookahead);
	
	/**
	 * Removes an object change listener from this ambassador at the next 
	 * opportunity.
	 *
	 * @param listener the listener
	 */
	public void removeObjectChangeListener(ObjectChangeListener listener);
	
	/**
	 * Schedules an interaction to be sent by this ambassador at the next 
	 * opportunity and notifies any associated object change listeners. 
	 *
	 * @param interaction the interaction
	 */
	public void scheduleInteraction(SimInteraction interaction);
	
	/**
	 * Schedules an object to be created by this ambassador at the next 
	 * opportunity and notifies any associated object change listeners. 
	 * If the object was previously created, this method should do nothing.
	 *
	 * @param object the object
	 */
	public void scheduleObjectCreation(SimObject object);
	
	/**
	 * Schedules an object to be deleted by this ambassador at the next 
	 * opportunity and notifies any associated object change listeners. 
	 * If the object was never created or previously deleted this
	 * method should do nothing.
	 *
	 * @param object the object
	 */
	public void scheduleObjectDeletion(SimObject object);
	
	/**
	 * Schedules and object to be updated by this ambassador at the next 
	 * opportunity and notifies any associated object change listeners. 
	 * If the object was never created or previously deleted 
	 * this method should do nothing.
	 *
	 * @param object the object
	 */
	public void scheduleObjectUpdate(SimObject object);
	
	/**
	 * Terminates this ambassador's federation execution. Any previously-
	 * scheduled actions shall be processed before termination. Termination
	 * shall disable time constrained and time regulating behavior, and resign
	 * from and attempt to destroy the federation execution.
	 */
	public void terminate();
}
