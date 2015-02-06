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

import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.EventListenerList;

import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;

import edu.mit.fss.event.ExecutionControlEvent;
import edu.mit.fss.event.ExecutionControlListener;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import edu.mit.fss.hla.FSSambassador;
import edu.mit.fss.hla.DefaultAmbassador;
import edu.mit.fss.hla.FederationConnection;

/**
 * A default implementation of the {@link Federate} interface. Sets a default
 * initial time of 0 (corresponding to January 1, 1970), a default time step
 * of 1 minute, and a default minimum step duration of 100 milliseconds.
 * <p>
 * Provides thread-safe methods for {@link #addObject(SimObject)}, 
 * {@link #removeObject(SimObject)}, {@link #connect()}, 
 * {@link #initialize()}, {@link #run()}, {@link #tickTock()}, 
 * {@link #stop()}, {@link #terminate()}, {@link #disconnect()}, and 
 * {@link #exit()}.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class DefaultFederate implements Federate {	
	private static Logger logger = Logger.getLogger(DefaultFederate.class);
	private final FSSambassador ambassador;
	private final EventListenerList listenerList = new EventListenerList();
	private final Set<SimObject> localObjects = Collections.synchronizedSet(
			new HashSet<SimObject>());
	private long initialTime, finalTime, timeStep;
	private long minimumStepDuration, lookahead;
	private long time;
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private volatile AtomicBoolean running = new AtomicBoolean(false);
	private volatile AtomicBoolean stopping = new AtomicBoolean(false);
	private volatile AtomicBoolean terminating = new AtomicBoolean(false);
	private volatile long nextTimeStep, nextMinimumStepDuration, nextFinalTime;
	
	/**
	 * Instantiates a new default federate using a {@link DefaultAmbassador} 
	 * federate ambassador implementation.
	 * 
	 * @throws RTIinternalError 
	 */
	public DefaultFederate() throws RTIexception {
		this(new DefaultAmbassador());
	}
	
	/**
	 * Instantiates a new default federate using the provided federate 
	 * ambassador {@link ambassador}. Assigns a default initial time of 0, a
	 * final time of Long.MAX_VALUE, a time step of 60000 ms, a minimum
	 * step duration of 100 ms, and a lookahead of 1000 ms.
	 *
	 * @param ambassador the ambassador
	 */
	public DefaultFederate(FSSambassador ambassador) {
		this.ambassador = ambassador;
		initialTime = 0;
		finalTime = Long.MAX_VALUE;
		timeStep = 60*1000;
		minimumStepDuration = 100;
		lookahead = 1000;
		nextTimeStep = timeStep;
		nextMinimumStepDuration = minimumStepDuration;
		nextFinalTime = finalTime;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#addExecutionControlListener(edu.mit.fss.gui.ExecutionControlListener)
	 */
	@Override
	public void addExecutionControlListener(ExecutionControlListener listener) {
		listenerList.add(ExecutionControlListener.class, listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#addObject(edu.mit.fss.SimObject)
	 */
	@Override
	public synchronized void addObject(SimObject object) {
		if(initialized.get()) {
			// thread safe initialization as tickTock is synchronized
			object.initialize(time);
			// add object to federation if simulation is initialized
			ambassador.scheduleObjectCreation(object);
		}
		// add objec to local simulation objects
		localObjects.add(object);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#addObjectChangeListener(edu.mit.fss.gui.ObjectChangeListener)
	 */
	@Override
	public void addObjectChangeListener(ObjectChangeListener listener) {
		ambassador.addObjectChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#addSimulationTimeListener(edu.mit.fss.gui.SimulationTimeListener)
	 */
	@Override
	public void addSimulationTimeListener(SimulationTimeListener listener) {
		listenerList.add(SimulationTimeListener.class, listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#connect()
	 */
	@Override
	public void connect() {
		ambassador.connect();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#disconnect()
	 */
	@Override
	public void disconnect() {
		// terminate before disconnecting
		terminate();
		ambassador.disconnect();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#exit()
	 */
	@Override
	public void exit() {
		// terminate and disconnect before exiting
		terminate();
		disconnect();
		System.exit(0);
	}
	
	/**
	 * Fires an execution initialized event.
	 */
	protected final void fireExecutionInitializedEvent() {
		ExecutionControlListener[] listeners = listenerList.getListeners(
				ExecutionControlListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].executionInitialized(new ExecutionControlEvent(this));
		}
	}
	
	/**
	 * Fires an execution started event.
	 */
	protected final void fireExecutionStartedEvent() {
		ExecutionControlListener[] listeners = listenerList.getListeners(
				ExecutionControlListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].executionStarted(new ExecutionControlEvent(this));
		}
	}
	
	/**
	 * Fires an execution stopped event.
	 */
	protected final void fireExecutionStoppedEvent() {
		ExecutionControlListener[] listeners = listenerList.getListeners(
				ExecutionControlListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].executionStopped(new ExecutionControlEvent(this));
		}
	}
	
	/**
	 * Fires an execution terminated event.
	 */
	protected final void fireExecutionTerminatedEvent() {
		ExecutionControlListener[] listeners = listenerList.getListeners(
				ExecutionControlListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].executionTerminated(new ExecutionControlEvent(this));
		}
	}
	
	/**
	 * Fires an simulation time advanced event.
	 */
	protected final void fireSimulationTimeAdvancedEvent() {
		SimulationTimeListener[] listeners = listenerList.getListeners(
				SimulationTimeListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].timeAdvanced(new SimulationTimeEvent(this, time));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#getConnection()
	 */
	@Override
	public FederationConnection getConnection() {
		return ambassador.getConnection();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#getFinalTime()
	 */
	@Override
	public long getFinalTime() {
		return finalTime;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#getInitialTime()
	 */
	@Override
	public long getInitialTime() {
		return initialTime;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#getMinimumStepDuration()
	 */
	@Override
	public long getMinimumStepDuration() {
		return minimumStepDuration;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#getTimeStep()
	 */
	@Override
	public long getTimeStep() {
		return timeStep;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#initialize()
	 */
	@Override
	public synchronized void initialize() {
		// update initial time -- federation may be in the future
		time = ambassador.initialize(initialTime, lookahead);

		synchronized(localObjects) {
			for(SimObject object : localObjects) {
				object.initialize(time);
		        ambassador.scheduleObjectCreation(object);
			}
		}

		initialized.set(true);
		fireExecutionInitializedEvent();
		fireSimulationTimeAdvancedEvent();
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#removeExecutionControlListener(edu.mit.fss.gui.ExecutionControlListener)
	 */
	@Override
	public void removeExecutionControlListener(ExecutionControlListener listener) {
		listenerList.remove(ExecutionControlListener.class, listener);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#removeObject(edu.mit.fss.SimObject)
	 */
	@Override
	public synchronized void removeObject(SimObject object) {
		if(initialized.get()) {
			// delete object from federation if simulation is initialized
			ambassador.scheduleObjectDeletion(object);
		}
		// remove from local simulation objects
		localObjects.remove(object);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#removeObjectChangeListener(edu.mit.fss.ObjectChangeListener)
	 */
	@Override
	public void removeObjectChangeListener(ObjectChangeListener listener) {
		ambassador.removeObjectChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#removeSimulationTimeListneer(edu.mit.fss.gui.SimulationTimeListener)
	 */
	@Override
	public void removeSimulationTimeListener(SimulationTimeListener listener) {
		listenerList.remove(SimulationTimeListener.class, listener);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#run(long)
	 */
	@Override
	public void run() {
		if(!initialized.get()) {
			throw new IllegalStateException("Simulation is not initialized.");
		}
		logger.trace("Running the federation.");
		running.set(true);
		fireExecutionStartedEvent();
		while(time < getFinalTime() && !stopping.get() && !terminating.get()) {
			tickTock();
		}
		logger.trace("Exiting the running loop.");
		if(stopping.get()) {
			stopping.set(false);
			logger.trace("Federation has stopped.");
		}
		if(terminating.get()) {
			terminating.set(false);
			logger.trace("Federation has terminated.");
		}
		running.set(false);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#sendInteraction(edu.mit.fss.SimInteraction)
	 */
	@Override
	public void sendInteraction(SimInteraction interaction) {
		if(!initialized.get()) {
			throw new IllegalStateException("Simulation is not initialized.");
		}
		
		ambassador.scheduleInteraction(interaction);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#setFinalTime(long)
	 */
	@Override
	public void setFinalTime(long finalTime) {
		logger.trace("Setting next final time to " + finalTime 
				+ " (" + new Date(finalTime) + ").");
		nextFinalTime = finalTime;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#setInitialTime(long)
	 */
	@Override
	public void setInitialTime(long initialTime) {
		logger.trace("Setting initial time to " + initialTime 
				+ " (" + new Date(initialTime) + ").");
		this.initialTime = initialTime;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#setMinimumStepDuration(long)
	 */
	@Override
	public void setMinimumStepDuration(long minimumStepDuration) {
		logger.trace("Setting next minimum step duration to " 
				+ minimumStepDuration + " ms.");
		nextMinimumStepDuration = minimumStepDuration;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#setTimeStep(long)
	 */
	@Override
	public void setTimeStep(long timeStep) {
		if(timeStep < lookahead) {
			logger.warn("Time step (" + timeStep 
					+ " ms) cannot be smaller than lookahead (" + lookahead 
					+ " ms), setting to lookahead.");
			nextTimeStep = lookahead;
		} else {
			logger.trace("Setting next time step to " + timeStep + " ms.");
			nextTimeStep = timeStep;
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#stop()
	 */
	@Override
	public void stop() {
		logger.trace("Stopping the federation...");
		if(running.get()) {
			stopping.set(true);
			// wait until execution is stopped
			while(stopping.get()) {
				Thread.yield();
			}
		}
		fireExecutionStoppedEvent();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.Federate#terminate()
	 */
	@Override
	public void terminate() {
		logger.trace("Terminating the federate.");
		if(running.get()) {
			terminating.set(true);
			// wait until execution is terminated
			while(terminating.get()) {
				Thread.yield();
			}
		}
		if(initialized.get()) {
			initialized.set(false);
			// delete all simulation objects from federation
			synchronized(localObjects) {
				for(SimObject object : localObjects) {
					ambassador.scheduleObjectDeletion(object);
				}
			}
		}
		ambassador.terminate();
		fireExecutionTerminatedEvent();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Federate#tickTock()
	 */
	@Override
	public synchronized void tickTock() {
		if(!initialized.get()) {
			throw new IllegalStateException("Simulation is not initialized.");
		}
		logger.trace("Tick-tocking federation.");
		
		// log the wallclock time
		long systemTime = new Date().getTime();
		
		timeStep = nextTimeStep;
		minimumStepDuration = nextMinimumStepDuration;
		finalTime = nextFinalTime;

		synchronized(localObjects) {
			logger.trace("Ticking all federate objects.");
			for(SimObject object : localObjects) {
		        object.tick(timeStep);
			}
			logger.trace("Tocking all federate objects.");
			for(SimObject object : localObjects) {
		        object.tock();
				ambassador.scheduleObjectUpdate(object);
			}
		}
		logger.trace("Advancing simulation time.");
		time += timeStep;
		ambassador.advanceTo(time);
		fireSimulationTimeAdvancedEvent();
		
		try {
			logger.trace("Waiting for minimum step duration.");
			Thread.sleep(FastMath.max(0, minimumStepDuration 
					- (new Date().getTime() - systemTime)));
		} catch (InterruptedException ignored) { }
	}
}
