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

import edu.mit.fss.hla.FederationConnection;

/**
 * An event object which notifies of a change to a {@link FederationConnection}
 * object.
 * 
 * @author Paul T Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class ConnectionEvent extends EventObject {
	private static final long serialVersionUID = -389874206436130676L;
	
	private final FederationConnection connection;
	
	/**
	 * Instantiates a new connection event.
	 *
	 * @param source the source
	 * @param connected the connected
	 * @param host the host
	 * @param federationName the federation name
	 */
	public ConnectionEvent(Object source, FederationConnection connection) {
		super(source);
		this.connection = connection;
	}
	
	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 */
	public FederationConnection getConnection() {
		return connection;
	}
}
