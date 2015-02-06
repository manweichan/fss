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

import hla.rti1516e.NullFederateAmbassador;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

import edu.mit.fss.SimInteraction;
import edu.mit.fss.SimObject;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;

/**
 * A null (non-HLA) implementation of the {@link FSSambassador} interface.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class NullAmbassador extends NullFederateAmbassador implements
		FSSambassador {
	protected static Logger logger = Logger.getLogger(NullAmbassador.class);
	final FederationConnection federationConnection = new FederationConnection();
	private final EventListenerList listenerList = new EventListenerList();
	private final ConcurrentLinkedQueue<Runnable> actionsToProcess =
			new ConcurrentLinkedQueue<Runnable>();
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSambassador#addObjectChangeListener(edu.mit.fss.event.ObjectChangeListener)
	 */
	@Override
	public void addObjectChangeListener(ObjectChangeListener listener) { 
		listenerList.add(ObjectChangeListener.class, listener);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSambassador#advanceTo(long)
	 */
	@Override
	public void advanceTo(long time) {
		logger.debug("Advancing logical time to " + time + ".");

		logger.trace("Processing all queued actions.");
		while(actionsToProcess.peek() != null) {
			actionsToProcess.poll().run();
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSambassador#connect()
	 */
	@Override
	public void connect() { }

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSambassador#disconnect()
	 */
	@Override
	public void disconnect() { }

	/**
	 * Fires an interaction event.
	 *
	 * @param object the object
	 */
	protected final void fireInteractionEvent(Object object) {
		logger.trace("Firing an interaction event.");
		ObjectChangeListener[] listeners = listenerList.getListeners(
				ObjectChangeListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].interactionOccurred(new ObjectChangeEvent(this, object));
		}
	}

	/**
	 * Fires an object changed event.
	 *
	 * @param object the object
	 */
	protected final void fireObjectChangedEvent(Object object) {
		logger.trace("Firing an object changed event.");
		ObjectChangeListener[] listeners = listenerList.getListeners(
				ObjectChangeListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].objectChanged(new ObjectChangeEvent(this, object));
		}
	}

	/**
	 * Fires an object discovered event.
	 *
	 * @param object the object
	 */
	protected final void fireObjectDiscoveredEvent(Object object) {
		logger.trace("Firing an object discovered event.");
		ObjectChangeListener[] listeners = listenerList.getListeners(
				ObjectChangeListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].objectDiscovered(new ObjectChangeEvent(this, object));
		}
	}

	/**
	 * Fires an object removed event.
	 *
	 * @param object the object
	 */
	protected final void fireObjectRemovedEvent(Object object) {
		logger.trace("Firing an object removed event.");
		ObjectChangeListener[] listeners = listenerList.getListeners(
				ObjectChangeListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].objectRemoved(new ObjectChangeEvent(this, object));
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSambassador#getConnection()
	 */
	@Override
	public FederationConnection getConnection() {
		return federationConnection;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSambassador#initialize(long, long)
	 */
	@Override
	public long initialize(long initialTime, long lookahead) {
		logger.debug("Initializing to time " + initialTime 
				+ " with lookahead " + lookahead + ".");
		return initialTime;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSambassador#removeObjectChangeListener(edu.mit.fss.event.ObjectChangeListener)
	 */
	@Override
	public void removeObjectChangeListener(ObjectChangeListener listener) {
		listenerList.remove(ObjectChangeListener.class, listener);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#scheduleInteraction(edu.mit.fss.SimInteraction)
	 */
	@Override
	public void scheduleInteraction(final SimInteraction interaction) {
		logger.debug("Scheduling interaction " + interaction + ".");
		actionsToProcess.add(new Runnable() {
			public void run() {
				logger.debug("Sending interaction " + interaction + ".");
				fireInteractionEvent(interaction);
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#scheduleObjectCreation(edu.mit.fss.SimObject)
	 */
	@Override
	public void scheduleObjectCreation(final SimObject object) {
		logger.debug("Scheduling object creation for " + object.getName() + ".");
		actionsToProcess.add(new Runnable() {
			public void run() {
				logger.debug("Creating object " + object.getName() + ".");
				fireObjectDiscoveredEvent(object);
			}
		});
		logger.trace("Recursively create all nested objects.");
		for(SimObject nestedObject : object.getNestedObjects()) {
			scheduleObjectCreation(nestedObject);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#scheduleObjectDeletion(edu.mit.fss.SimObject)
	 */
	@Override
	public void scheduleObjectDeletion(final SimObject object) {
		logger.debug("Scheduling object deletion for " + object.getName() + ".");
		actionsToProcess.add(new Runnable() {
			public void run() {
				logger.debug("Deleting object " + object.getName() + ".");
				fireObjectRemovedEvent(object);
			}
		});
		logger.trace("Recursively delete all nested objects.");
		for(SimObject nestedObject : object.getNestedObjects()) {
			scheduleObjectDeletion(nestedObject);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#scheduleObjectUpdate(edu.mit.fss.SimObject)
	 */
	@Override
	public void scheduleObjectUpdate(final SimObject object) {
		logger.trace("Scheduling object update for " + object.getName()  + ".");
		actionsToProcess.add(new Runnable() {
			public void run() {
				logger.trace("Updating object " + object.getName() + ".");
				fireObjectChangedEvent(object);
			}
		});
		logger.trace("Recursively update all nested objects.");
		for(SimObject nestedObject : object.getNestedObjects()) {
			scheduleObjectUpdate(nestedObject);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.hla.FSSambassador#terminate()
	 */
	@Override
	public void terminate() { 
		logger.debug("Terminating federate.");
	}
}
