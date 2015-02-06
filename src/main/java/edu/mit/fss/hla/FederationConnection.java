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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.EventListenerList;

import edu.mit.fss.event.ConnectionEvent;
import edu.mit.fss.event.ConnectionListener;

/**
 * A mutable connection object includes a host name and port number, 
 * federation name, path to the federation object model (FOM), and a
 * federate name and type. The parameters can only be modified while
 * the connection is not connected.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class FederationConnection implements Serializable {
	private static final long serialVersionUID = -4649447975331252471L;
	private final transient EventListenerList listenerList = new EventListenerList();
	private boolean offlineMode;
	private String federationName, fomPath, federateName, federateType;
	private transient volatile AtomicBoolean connected = new AtomicBoolean(false);
	
	/**
	 * Instantiates a new federation connection using default (empty) 
	 * parameters.
	 */
	public FederationConnection() {
		this(true, "", 0, "", "", "", "");
	}
	
	/**
	 * Instantiates a new federation connection using supplied parameters.
	 *
	 * @param offlineMode the offline mode
	 * @param host the host name
	 * @param port the port number
	 * @param federationName the federation name
	 * @param fomPath the federation object model path
	 * @param federateName the federate name
	 * @param federateType the federate type
	 */
	public FederationConnection(boolean offlineMode, String host, int port, 
			String federationName, String fomPath, String federateName,
			String federateType) {
		setOfflineMode(offlineMode);
		setFederationName(federationName);
		setFomPath(fomPath);
		setFederateName(federateName);
		setFederateType(federateType);
	}
	
	/**
	 * Adds a connection listener to this connection.
	 *
	 * @param listener the listener
	 */
	public void addConnectionListener(ConnectionListener listener) {
		listenerList.add(ConnectionListener.class, listener);
	}
	
	/**
	 * Fires a connection event.
	 */
	private void fireConnectionEvent() {
		ConnectionListener[] listeners = listenerList.getListeners(ConnectionListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listeners[i].connectionEventOccurred(new ConnectionEvent(this, this));
		}
	}
	
	/**
	 * Gets this connection's federate name.
	 *
	 * @return the federate name
	 */
	public String getFederateName() {
		return federateName;
	}
	
	/**
	 * Gets this connection's federate type.
	 *
	 * @return the federate type
	 */
	public String getFederateType() {
		return federateType;
	}

	/**
	 * Gets this connection's federation name.
	 *
	 * @return the federation name
	 */
	public String getFederationName() {
		return federationName;
	}

	/**
	 * Gets this connection's federation object model (FOM) path.
	 *
	 * @return the fom path
	 */
	public String getFomPath() {
		return fomPath;
	}

	/**
	 * Checks if this connection is connected.
	 *
	 * @return true, if is connected
	 */
	public boolean isConnected() {
		return connected.get();
	}

	/**
	 * Checks if is offline mode.
	 *
	 * @return true, if is offline mode
	 */
	public boolean isOfflineMode() {
		return offlineMode;
	}

	/**
	 * Removes a connection listener from this connection.
	 *
	 * @param listener the listener
	 */
	public void removeConnectionListener(ConnectionListener listener) {
		listenerList.remove(ConnectionListener.class, listener);
	}
	
	/**
	 * Sets this connection to be connected or disconnected and notifies
	 * any associated connection event listeners.
	 *
	 * @param connected the new connected
	 */
	public void setConnected(boolean connected) {
		this.connected.set(connected);
		fireConnectionEvent();
	}

	/**
	 * Sets this connection's federate name. Throws a
	 *
	 * @param federateName the new federate name
	 * {@link IllegalStateException} if called while connected. Throws a
	 * {@link IllegalArgumentException} if the name is null.
	 */
	public void setFederateName(String federateName) {
		if(connected.get()) {
			throw new IllegalStateException(
					"Cannot modify a connection while connected.");
		}
		if(federateName == null) {
			throw new IllegalArgumentException("Federate name cannot be null.");
		}
		this.federateName = federateName;
	}
	
	/**
	 * Sets this connection's federate type. Throws a
	 *
	 * @param federateType the new federate type
	 * {@link IllegalStateException} if called while connected. Throws a
	 * {@link IllegalArgumentException} if the type is null.
	 */
	public void setFederateType(String federateType) {
		if(connected.get()) {
			throw new IllegalStateException(
					"Cannot modify a connection while connected.");
		}
		if(federateType == null) {
			throw new IllegalArgumentException("Federate type cannot be null.");
		}
		this.federateType = federateType;
	}
	
	/**
	 * Sets this connection's federation name. Throws a
	 *
	 * @param federationName the new federation name
	 * {@link IllegalStateException} if called while connected. Throws a
	 * {@link IllegalArgumentException} if the name is null.
	 */
	public void setFederationName(String federationName) {
		if(connected.get()) {
			throw new IllegalStateException(
					"Cannot modify a connection while connected.");
		}
		if(federationName == null) {
			throw new IllegalArgumentException(
					"Federation name cannot be null.");
		}
		this.federationName = federationName;
	}
	
	/**
	 * Sets this connection's federation object model (FOM) path. Throws a
	 *
	 * @param fomPath the new fom path
	 * {@link IllegalStateException} if called while connected. Throws
	 * a {@link IllegalArgumentException} if the path is null.
	 */
	public void setFomPath(String fomPath) {
		if(connected.get()) {
			throw new IllegalStateException(
					"Cannot modify a connection while connected.");
		}
		if(fomPath == null) {
			throw new IllegalArgumentException("FOM path cannot be null.");
		}
		this.fomPath = fomPath;
	}
	
	/**
	 * Sets the offline mode.
	 *
	 * @param offlineMode the new offline mode
	 */
	public void setOfflineMode(boolean offlineMode) {
		this.offlineMode = offlineMode;
		fireConnectionEvent();
	}
}
