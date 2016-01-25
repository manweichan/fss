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

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyEnabled;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.exceptions.TimeConstrainedIsNotEnabled;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

import edu.mit.fss.OrbitalElement;
import edu.mit.fss.Receiver;
import edu.mit.fss.Signal;
import edu.mit.fss.SimInteraction;
import edu.mit.fss.SimObject;
import edu.mit.fss.SurfaceElement;
import edu.mit.fss.Transmitter;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;

/**
 * A default implementation of the {@link FSSambassador} interface. Uses a default
 * federation connection. Also allows an offline mode without an active RTI
 * connection.
 * <p>
 * Provides thread-safe methods for 
 * {@link #scheduleInteraction(SimInteraction)}, 
 * {@link #scheduleObjectCreation(SimObject)}, 
 * {@link #scheduleObjectDeletion(SimObject)}, and
 * {@link #scheduleObjectUpdate(SimObject)}.
 * <p>
 * Publishes and subscribes attributes for {@link FSSsurfaceElement}, 
 * {@link FSSorbtialElement}, {@link FSStransmitter}, and 
 * {@link FSSreceiver} objects and publishes and subscribes 
 * {@link FSSsignal} interactions.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class DefaultAmbassador extends NullFederateAmbassador implements FSSambassador {
	public static final String OHLA_RTI = "OHLA";
	public static final String PORTICO_RTI = "portico";
	public static final String PITCH_RTI = "pRTI 1516";
	
	protected static Logger logger = Logger.getLogger(DefaultAmbassador.class);
	private final EventListenerList listenerList = new EventListenerList();
	protected final FederationConnection connection = new FederationConnection();
	protected final RTIambassador rtiAmbassador;
	private final EncoderFactory encoderFactory;
	private HLAfloat64TimeFactory timeFactory;
	private volatile HLAfloat64Time logicalTime;
	private HLAfloat64Interval lookaheadInterval;
	private volatile AtomicBoolean timeConstrained = new AtomicBoolean(false);
	private volatile AtomicBoolean timeRegulating = new AtomicBoolean(false);
	private volatile AtomicBoolean timeAdvancing =  new AtomicBoolean(false);
	private final ConcurrentLinkedQueue<Runnable> actionsToProcess =
			new ConcurrentLinkedQueue<Runnable>();
	// map from HLA instance handles to HLA objects
	private final Map<ObjectInstanceHandle, HLAobject> objectInstanceHandleMap = 
			Collections.synchronizedMap(
					new HashMap<ObjectInstanceHandle, HLAobject>());
	// map from simulation objects to HLA objects
	private final Map<SimObject, HLAobject> localObjects = 
			Collections.synchronizedMap(
					new HashMap<SimObject, HLAobject>());
	// identifies RTI
	private final String rtiName;
	// timer for evoked callbacks
	private Timer timer = null;
	
	/**
	 * Instantiates a new default ambassador using the 
	 * {@link RtiFactoryFactory} to set the RTI ambassador and 
	 * encoder factory and immediate callbacks. Defaults to the Portico RTI.
	 *
	 * @throws RTIexception the RTI exception
	 */
	public DefaultAmbassador() throws RTIexception {
		this(PORTICO_RTI);
	}
	
	/**
	 * Instantiates a new default ambassador using the
	 * {@link RtiFactoryFactory} to set the RTI ambassador and 
	 * encoder factory and immediate callbacks.
	 *
	 * @param rtiName the RTI name
	 * @throws RTIexception the RTI exception
	 */
	public DefaultAmbassador(String rtiName) throws RTIexception {
		this.rtiName = rtiName;

		logger.trace("Getting the RTI factory.");
		RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory(rtiName);

		logger.trace("Getting the RTI ambassador.");
		rtiAmbassador = rtiFactory.getRtiAmbassador();

		logger.trace("Getting the encoder factory.");
		encoderFactory = rtiFactory.getEncoderFactory();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#addObjectChangeListener(edu.mit.fss.ObjectChangeListener)
	 */
	@Override
	public void addObjectChangeListener(ObjectChangeListener listener) {
		listenerList.add(ObjectChangeListener.class, listener);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#advanceTo(long)
	 */
	@Override
	public void advanceTo(long time) {
		logger.debug("Advancing logical time to " + time + ".");

		logger.trace("Processing all queued actions.");
		while(actionsToProcess.peek() != null) {
			actionsToProcess.poll().run();
		}

		if(!connection.isConnected()) {
			if(!connection.isOfflineMode()) {
				logger.warn("Not connected: continuing in offline mode.");
			}
			return;
		}

		logger.debug("Requesting time advance to " + timeFactory.makeTime(time));
		timeAdvancing.set(true);
		try {
			rtiAmbassador.timeAdvanceRequest(timeFactory.makeTime(time));
		} catch (RTIexception e) {
			logger.error(e);
		}
		logger.trace("Waiting for time advance grant.");
		while(timeAdvancing.get()) {
			Thread.yield();
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#connect()
	 */
	@Override
	public void connect() {
		logger.debug("Connecting to the RTI.");

		if(rtiName.equals(OHLA_RTI)) {
			try {
				rtiAmbassador.connect(this, 
						CallbackModel.HLA_EVOKED,
						"edu/mit/fss/hla/ohla-lrc.properties");
			} catch(AlreadyConnected ignored) {
			} catch (RTIexception e) {
				e.printStackTrace();
				logger.error(e);
			}
			logger.debug("Creating and starting callback-evoking timer.");
			if(timer != null) {
				timer.cancel();
			}
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						logger.trace("Evoking multiple callbacks.");
						rtiAmbassador.evokeMultipleCallbacks(0, 1);
					} catch (CallNotAllowedFromWithinCallback 
							| RTIinternalError e) {
						logger.error(e);
					}
				}
			}, 0, 100);
		} else if(rtiName.equals(PORTICO_RTI) || rtiName.equals(PITCH_RTI)) {
			try {
				rtiAmbassador.connect(this, 
						CallbackModel.HLA_IMMEDIATE);
			} catch(AlreadyConnected ignored) {
			} catch (RTIexception e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		logger.info("Connected to the RTI.");
		connection.setConnected(true);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#connectionLost(java.lang.String)
	 */
	@Override
	public void connectionLost(String faultDescription) throws FederateInternalError  {
		logger.error("Connection lost.");
		connection.setConnected(false);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#disconnect()
	 */
	@Override
	public void disconnect() {
		logger.debug("Disconnecting from the RTI.");
		try {
			rtiAmbassador.disconnect();
			logger.info("Disconnected from the RTI.");
			connection.setConnected(false);
		} catch (RTIexception e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#discoverObjectInstance(hla.rti1516e.ObjectInstanceHandle, hla.rti1516e.ObjectClassHandle, java.lang.String)
	 */
	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject,
			ObjectClassHandle theObjectClass, String objectName) {
		logger.info("Discovering object instance " + theObject + ".");

		if(objectInstanceHandleMap.containsKey(theObject)) {
			logger.warn("Already discovered object instance " 
					+ theObject + ", skipping.");
			return;
		}

		logger.trace("Searching for the correct object subclass.");
		HLAobject hlaObject = null;
		try {
			if(theObjectClass.equals(rtiAmbassador.getObjectClassHandle(
					FSSorbitalElement.CLASS_NAME))) {
				logger.debug("Creating an orbital element.");
				hlaObject = new FSSorbitalElement(
						rtiAmbassador, encoderFactory, objectName);
			} else if(theObjectClass.equals(
					rtiAmbassador.getObjectClassHandle(
							FSSsurfaceElement.CLASS_NAME))) {
				logger.debug("Creating a surface element.");
				hlaObject = new FSSsurfaceElement(
						rtiAmbassador, encoderFactory, objectName);
			} else if(theObjectClass.equals(
					rtiAmbassador.getObjectClassHandle(
							FSStransmitter.CLASS_NAME))) {
				logger.debug("Creating a radio transmitter.");
				hlaObject = new FSStransmitter(
						rtiAmbassador, encoderFactory, objectName);
			} else if(theObjectClass.equals(
					rtiAmbassador.getObjectClassHandle(
							FSSreceiver.CLASS_NAME))) {
				logger.debug("Creating a radio receiver.");
				hlaObject = new FSSreceiver(
						rtiAmbassador, encoderFactory, objectName);
			} else {
				logger.warn("Unknown object class " + theObjectClass + ", skipping.");
				return;
			}
			logger.trace("Requesting object attribute value update.");
			hlaObject.requestAttributeValueUpdate();
			
			logger.trace("Adding object to known instances.");
			objectInstanceHandleMap.put(theObject, hlaObject);
			fireObjectDiscoveredEvent(hlaObject);
		} catch (RTIexception e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#discoverObjectInstance(hla.rti1516e.ObjectInstanceHandle, hla.rti1516e.ObjectClassHandle, java.lang.String)
	 */
	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject,
			ObjectClassHandle theObjectClass, String objectName, 
			FederateHandle producingFederate) {
		logger.trace("Redirecting to common callback method.");
		discoverObjectInstance(theObject, theObjectClass, objectName);
	}

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
	 * @see edu.mit.fss.hla.Ambassador#getConnection()
	 */
	@Override
	public FederationConnection getConnection() {
		return connection;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#initialize(long, long)
	 */
	@Override
	public long initialize(long initialTime, long lookahead) {
		logger.debug("Initializing to time " + initialTime 
				+ " with lookahead " + lookahead + ".");

		if(!connection.isConnected()) {
			if(!connection.isOfflineMode()) {
				logger.warn("Not connected: continuing in offline mode.");
			}
			return initialTime;
		}

		logger.debug("Creating the federation execution.");
		try {
			rtiAmbassador.createFederationExecution(connection.getFederationName(), 
					new URL[]{new File(connection.getFomPath()).toURI().toURL()},
					"HLAfloat64Time");
			logger.info("Federation execution " 
					+ connection.getFederationName() + " created.");
		} catch(FederationExecutionAlreadyExists ignored) {
			logger.trace("Federation execution already exists.");
		} catch (RTIexception | MalformedURLException e) {
			logger.error(e);
		}

		logger.debug("Joining the federation execution.");
		try {
			rtiAmbassador.joinFederationExecution(connection.getFederateName(), 
					connection.getFederateType(), connection.getFederationName());
			logger.info("Joined federation execution " 
					+ connection.getFederationName() + " as federate " 
					+ connection.getFederateName() + " of type " 
					+ connection.getFederateType() + ".");
		} catch(FederateAlreadyExecutionMember ignored) { 
			logger.trace("Already joined to the federation execution.");
		} catch (RTIexception e) {
			logger.error(e);
		}

		logger.debug("Making the initial time and lookahead intervals.");
		try {
			logger.trace("Getting the time factory.");
			if(rtiAmbassador.getTimeFactory() instanceof HLAfloat64TimeFactory) {
				timeFactory = (HLAfloat64TimeFactory) rtiAmbassador.getTimeFactory();
			} else {
				String message = "Time factory is not compatible. Expected " 
						+ HLAfloat64TimeFactory.class + " but received " 
						+ rtiAmbassador.getTimeFactory().getClass() + ".";
				logger.fatal(message);
			}
		} catch (RTIexception e) {
			logger.error(e);
		}
		logger.trace("Making the initial time and lookahead interval.");
		logicalTime = timeFactory.makeTime(initialTime);
		lookaheadInterval = timeFactory.makeInterval(lookahead);

		logger.debug("Enabling asynchronous message delivery.");
		try {
			rtiAmbassador.enableAsynchronousDelivery();
			logger.info("Asynchronous message delivery enabled.");
		} catch (AsynchronousDeliveryAlreadyEnabled ignored) {
		} catch (RTIexception e) {
			logger.error(e);
		}

		logger.debug("Enabling time constrained behavior.");
		try {
			rtiAmbassador.enableTimeConstrained();
		} catch (TimeConstrainedAlreadyEnabled ignored) { 
		} catch (RTIexception e) {
			logger.error(e);
		}
		logger.trace("Waiting for time constrained callback service.");
		while(!timeConstrained.get()) {
			Thread.yield();
		}
		logger.info("Time constrained behavior enabled.");

		logger.debug("Enabling time regulating behavior.");
		try {
			rtiAmbassador.enableTimeRegulation(lookaheadInterval);
		} catch (TimeRegulationAlreadyEnabled ignored) { 
		} catch (RTIexception e) {
			logger.error(e);
		}
		logger.trace("Waiting for time regulating callback service.");
		while(!timeRegulating.get()) {
			Thread.yield();
		}
		logger.info("Time regulating behavior enabled.");

		logger.debug("Publishing and subscribing all objects and interactions.");
		try {
			FSSorbitalElement.publishAll(rtiAmbassador);
			FSSsurfaceElement.publishAll(rtiAmbassador);
			FSSorbitalElement.subscribeAll(rtiAmbassador);
			FSSsurfaceElement.subscribeAll(rtiAmbassador);

			FSStransmitter.publishAll(rtiAmbassador);
			FSSreceiver.publishAll(rtiAmbassador);
			FSStransmitter.subscribeAll(rtiAmbassador);
			FSSreceiver.subscribeAll(rtiAmbassador);

			FSSsignal.publish(rtiAmbassador);
			FSSsignal.subscribe(rtiAmbassador);
			logger.info("Published and subscribed all objects and interactions.");
		} catch (RTIexception e) {
			logger.error(e);
		}

		if(initialTime > logicalTime.getValue()) {
			logger.trace("Federate is ahead of federation: advance time.");
			advanceTo(initialTime);
		}

		// return the current logical time
		return (long) logicalTime.getValue();
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#provideAttributeValueUpdate(hla.rti1516e.ObjectInstanceHandle, hla.rti1516e.AttributeHandleSet, byte[])
	 */
	@Override
	public void provideAttributeValueUpdate(final ObjectInstanceHandle theObject,
			final AttributeHandleSet theAttributes, byte[] userSuppliedTag) {
		logger.debug("Provide attribute updates for object " + theObject + ".");
		actionsToProcess.add(new Runnable() {
			public void run() {
				if(objectInstanceHandleMap.get(theObject) != null) {
					try {
						logger.debug("Providing attributes for known instance " 
								+ objectInstanceHandleMap.get(theObject));
						objectInstanceHandleMap.get(theObject)
						.provideAttributes(theAttributes);
					} catch (RTIexception e) {
						logger.error(e);
					}
				} else {
					logger.warn("Object " + theObject + " is not a known instance.");
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#receiveInteraction(hla.rti1516e.InteractionClassHandle, hla.rti1516e.ParameterHandleValueMap, byte[], hla.rti1516e.OrderType, hla.rti1516e.TransportationTypeHandle, hla.rti1516e.LogicalTime, hla.rti1516e.OrderType, hla.rti1516e.MessageRetractionHandle, hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo)
	 */
	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass,
			ParameterHandleValueMap theParameters, byte[] userSuppliedTag, 
			OrderType sentOrdering, TransportationTypeHandle theTransport, 
			LogicalTime theTime,  OrderType receivedOrdering, 
			MessageRetractionHandle retractionHandle, 
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
		logger.trace("Redirecting to common callback method.");
		receiveInteraction(interactionClass, theParameters, userSuppliedTag, 
				sentOrdering, theTransport, receiveInfo);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#receiveInteraction(hla.rti1516e.InteractionClassHandle, hla.rti1516e.ParameterHandleValueMap, byte[], hla.rti1516e.OrderType, hla.rti1516e.TransportationTypeHandle, hla.rti1516e.LogicalTime, hla.rti1516e.OrderType, hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo)
	 */
	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass,
			ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
			OrderType sentOrdering, TransportationTypeHandle theTransport,
			LogicalTime theTime, OrderType receivedOrdering, 
			SupplementalReceiveInfo receiveInfo)  throws FederateInternalError {
		logger.trace("Redirecting to common callback method.");
		receiveInteraction(interactionClass, theParameters, userSuppliedTag, 
				sentOrdering, theTransport, receiveInfo);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#receiveInteraction(hla.rti1516e.InteractionClassHandle, hla.rti1516e.ParameterHandleValueMap, byte[], hla.rti1516e.OrderType, hla.rti1516e.TransportationTypeHandle, hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo)
	 */
	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, 
			ParameterHandleValueMap theParameters, byte[] userSuppliedTag, 
			OrderType sentOrdering, TransportationTypeHandle theTransport,
			SupplementalReceiveInfo receiveInfo)  throws FederateInternalError  {
		logger.info("Receive interaction " + interactionClass + ".");

		logger.trace("Searching for the correct interaction subclass.");
		HLAinteraction hlaInteraction = null;
		try {
			if(interactionClass.equals(rtiAmbassador.getInteractionClassHandle(
					FSSsignal.CLASS_NAME))) {
				logger.trace("Creating a signal interaction.");
				FSSsignal fssSignal = new FSSsignal(rtiAmbassador, encoderFactory);
				fssSignal.setParameters(theParameters);
				fssSignal.setElement(objectInstanceHandleMap.values());
				fssSignal.setTransmitter(objectInstanceHandleMap.values());
				hlaInteraction = fssSignal;
			} else {
				logger.warn("Unknown interaction class " + interactionClass + ", skipping.");
				return;
			}
			fireInteractionEvent(hlaInteraction);
		} catch (RTIexception | DecoderException e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#reflectAttributeValues(hla.rti1516e.ObjectInstanceHandle, hla.rti1516e.AttributeHandleValueMap, byte[], hla.rti1516e.OrderType, hla.rti1516e.TransportationTypeHandle, hla.rti1516e.LogicalTime, hla.rti1516e.OrderType, hla.rti1516e.MessageRetractionHandle, hla.rti1516e.FederateAmbassador.SupplementalReflectInfo)
	 */
	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject,
			AttributeHandleValueMap theAttributes,
			byte[] userSuppliedTag,
			OrderType sentOrdering,
			TransportationTypeHandle theTransport,
			LogicalTime theTime,
			OrderType receivedOrdering,
			MessageRetractionHandle retractionHandle,
			SupplementalReflectInfo reflectInfo) {
		logger.trace("Redirecting to common callback method.");
		reflectAttributeValues(theObject, theAttributes, userSuppliedTag, 
				sentOrdering, theTransport, reflectInfo);

	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#reflectAttributeValues(hla.rti1516e.ObjectInstanceHandle, hla.rti1516e.AttributeHandleValueMap, byte[], hla.rti1516e.OrderType, hla.rti1516e.TransportationTypeHandle, hla.rti1516e.LogicalTime, hla.rti1516e.OrderType, hla.rti1516e.FederateAmbassador.SupplementalReflectInfo)
	 */
	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject,
			AttributeHandleValueMap theAttributes,
			byte[] userSuppliedTag,
			OrderType sentOrdering,
			TransportationTypeHandle theTransport,
			LogicalTime theTime,
			OrderType receivedOrdering,
			SupplementalReflectInfo reflectInfo) {
		logger.trace("Redirecting to common callback method.");
		reflectAttributeValues(theObject, theAttributes, userSuppliedTag,
				sentOrdering, theTransport, reflectInfo);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#reflectAttributeValues(hla.rti1516e.ObjectInstanceHandle, hla.rti1516e.AttributeHandleValueMap, byte[], hla.rti1516e.OrderType, hla.rti1516e.TransportationTypeHandle, hla.rti1516e.FederateAmbassador.SupplementalReflectInfo)
	 */
	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject,
			AttributeHandleValueMap theAttributes,
			byte[] userSuppliedTag,
			OrderType sentOrdering,
			TransportationTypeHandle theTransport,
			SupplementalReflectInfo reflectInfo) {
		logger.debug("Reflect attributes for object " + theObject);
		try {
			if(objectInstanceHandleMap.containsKey(theObject)) {
				logger.trace("Reflecting attributes for known object " 
						+ objectInstanceHandleMap.get(theObject));
				HLAobject object = objectInstanceHandleMap.get(theObject);
				object.setAllAttributes(theAttributes);
				if(object instanceof FSStransmitter && theAttributes.containsKey(
						rtiAmbassador.getAttributeHandle(
								rtiAmbassador.getObjectClassHandle(
										FSStransmitter.CLASS_NAME), 
										FSStransmitter.ELEMENT_ATTRIBUTE))) {
					logger.trace("Setting the associated element reference.");
					((FSStransmitter)object).setElement(
							objectInstanceHandleMap.values());
				}
				if(object instanceof FSSreceiver && theAttributes.containsKey(
						rtiAmbassador.getAttributeHandle(
								rtiAmbassador.getObjectClassHandle(
										FSSreceiver.CLASS_NAME), 
										FSSreceiver.ELEMENT_ATTRIBUTE))) {
					logger.trace("Setting the associated element reference.");
					((FSSreceiver)object).setElement(
							objectInstanceHandleMap.values());
				}
				if(object instanceof FSSreceiver && theAttributes.containsKey(
						rtiAmbassador.getAttributeHandle(
								rtiAmbassador.getObjectClassHandle(
										FSSreceiver.CLASS_NAME), 
										FSSreceiver.TRANSMITTER_ATTRIBUTE))) {
					logger.trace("Setting the associated transmitter reference.");
					((FSSreceiver)object).setTransmitter(
							objectInstanceHandleMap.values());
				}
				fireObjectChangedEvent(objectInstanceHandleMap.get(theObject));
			} 
		} catch (RTIexception | DecoderException e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#removeObjectChangeListener(edu.mit.fss.ObjectChangeListener)
	 */
	@Override
	public void removeObjectChangeListener(ObjectChangeListener listener) {
		listenerList.remove(ObjectChangeListener.class, listener);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#removeObjectInstance(hla.rti1516e.ObjectInstanceHandle, byte[], hla.rti1516e.OrderType, hla.rti1516e.LogicalTime, hla.rti1516e.OrderType, hla.rti1516e.MessageRetractionHandle, hla.rti1516e.FederateAmbassador.SupplementalRemoveInfo)
	 */
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject,
			byte[] userSuppliedTag, OrderType sentOrdering, LogicalTime theTime,
			OrderType receivedOrdering, MessageRetractionHandle retractionHandle,
			SupplementalRemoveInfo removeInfo) throws FederateInternalError {
		logger.trace("Redirecting to common callback method.");
		removeObjectInstance(theObject,userSuppliedTag,sentOrdering,removeInfo);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#removeObjectInstance(hla.rti1516e.ObjectInstanceHandle, byte[], hla.rti1516e.OrderType, hla.rti1516e.LogicalTime, hla.rti1516e.OrderType, hla.rti1516e.FederateAmbassador.SupplementalRemoveInfo)
	 */
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject,
			byte[] userSuppliedTag, OrderType sentOrdering, LogicalTime theTime,
			OrderType receivedOrdering, SupplementalRemoveInfo removeInfo) 
					throws FederateInternalError {
		logger.trace("Redirecting to common callback method.");
		removeObjectInstance(theObject,userSuppliedTag,sentOrdering,removeInfo);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#removeObjectInstance(hla.rti1516e.ObjectInstanceHandle, byte[], hla.rti1516e.OrderType, hla.rti1516e.FederateAmbassador.SupplementalRemoveInfo)
	 */
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject,
			byte[] userSuppliedTag, OrderType sentOrdering,
			SupplementalRemoveInfo removeInfo) throws FederateInternalError {
		logger.info("Remove object " + theObject + ".");
		if(objectInstanceHandleMap.containsKey(theObject)) {
			HLAobject object = objectInstanceHandleMap.remove(theObject);
			fireObjectRemovedEvent(object);
			logger.debug("Removed object " + object.getName() + ".");
		} else {
			logger.warn("Object " + theObject + " is not a known instance.");
		}
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
				if(!connection.isConnected()) {
					if(!connection.isOfflineMode()) {
						logger.warn("Not connected: continuing in offline mode.");
					}
					fireInteractionEvent(interaction);
				} else {
					try {
						logger.trace("Searching for the correct interaction subclass.");
						HLAinteraction hlaInteraction = null;
						if(interaction instanceof Signal) {
							logger.debug("Creating a signal interaction.");
							hlaInteraction = new FSSsignal(rtiAmbassador, encoderFactory);
						} else {
							logger.warn("Unknown interaction type for class " 
									+ interaction.getClass() + ", skipping.");
							return;
						}
						hlaInteraction.setParameters(interaction);
						hlaInteraction.send();
						fireInteractionEvent(interaction);
					} catch(RTIexception e) {
						logger.error(e);
					}
				}
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
				if(!connection.isConnected()) {
					if(!connection.isOfflineMode()) {
						logger.warn("Not connected: continuing in offline mode.");
					}
					fireObjectDiscoveredEvent(object);
				} else if(!localObjects.containsKey(object)) {
					try {
						logger.trace("Searching for the correct object subclass.");
						HLAobject hlaObject = null;
						if(object instanceof OrbitalElement) {
							logger.debug("Creating an orbital element.");
							hlaObject = new FSSorbitalElement(
									rtiAmbassador, encoderFactory, null);
						} else if(object instanceof SurfaceElement) {
							logger.debug("Creating a surface element.");
							hlaObject = new FSSsurfaceElement(
									rtiAmbassador, encoderFactory, null);
						} else if(object instanceof Transmitter) {
							logger.debug("Creating a transmitter.");
							hlaObject = new FSStransmitter(
									rtiAmbassador, encoderFactory, null);
						} else if(object instanceof Receiver) {
							logger.debug("Creating a receiver.");
							hlaObject = new FSSreceiver(
									rtiAmbassador, encoderFactory, null);
						} else {
							logger.warn("Unknown HLA object type for class " 
									+ object.getClass() + ", skipping");
							return;
						}
						logger.trace("Adding " + object.getName() 
								+ " to known instances.");
						objectInstanceHandleMap.put(
								hlaObject.getObjectInstanceHandle(), hlaObject);
						logger.trace("Adding " + object.getName() 
								+ " to local objects.");
						localObjects.put(object, hlaObject);
						hlaObject.setAttributes(object);
						fireObjectDiscoveredEvent(object);
					} catch (RTIexception e) {
						logger.error(e);
					}
				}
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
				if(!connection.isConnected()) {
					if(!connection.isOfflineMode()) {
						logger.warn("Not connected: continuing in offline mode.");
					}
					fireObjectRemovedEvent(object);
				} else if(localObjects.containsKey(object)) {
					try {
						localObjects.get(object).delete();
					} catch (RTIexception e) {
						logger.error(e);
					}
					logger.trace("Removing from local objects and known objects.");
					HLAobject hlaObject = localObjects.remove(object);
					objectInstanceHandleMap.remove(hlaObject.getObjectInstanceHandle());
					fireObjectRemovedEvent(object);
				} else {
					logger.warn("Object " + object.getName() 
							+ " is not a known object instance.");
				}
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
				if(!connection.isConnected()) {
					if(!connection.isOfflineMode()) {
						logger.warn("Not connected: continuing in offline mode.");
					}
					fireObjectChangedEvent(object);
				} else if(localObjects.containsKey(object)) {
					logger.trace("Updating attributes.");
					localObjects.get(object).setAttributes(object);

					try {
						localObjects.get(object).updateChangedAttributes();
						fireObjectChangedEvent(object);
					} catch (RTIexception e) {
						logger.error(e);
					}
				} else {
					logger.warn("Object " + object.getName() 
							+ " is not a known object instance.");
				}
			}
		});
		logger.trace("Recursively update all nested objects.");
		for(SimObject nestedObject : object.getNestedObjects()) {
			scheduleObjectUpdate(nestedObject);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.Ambassador#terminate()
	 */
	@Override
	public void terminate() {
		logger.debug("Terminating federation execution.");
		
		if(timer != null) {
			logger.debug("Cancelling callback-evoking timer.");
			timer.cancel();
		}
		
		logger.trace("Processing queued actions.");
		while(actionsToProcess.peek() != null) {
			actionsToProcess.poll().run();
		}

		if(!connection.isConnected()) {
			if(!connection.isOfflineMode()) {
				logger.warn("Not connected: continuing in offline mode.");
			}
			return;
		}

		logger.debug("Disabling time constrained behavior.");
		try {
			rtiAmbassador.disableTimeConstrained();
		} catch (FederateNotExecutionMember ignored) {
			logger.trace("Federate is not an execution member.");
		} catch (TimeConstrainedIsNotEnabled ignored) {
			logger.trace("Time constrained is not enabled.");
		} catch(NotConnected ignored) {
			logger.trace("Federate is not connected.");
		} catch (RTIexception e) {
			logger.error(e);
		}
		timeConstrained.set(false);

		logger.debug("Disabling time regulation.");
		try {
			rtiAmbassador.disableTimeRegulation();
		} catch (FederateNotExecutionMember ignored) {
			logger.trace("Federate is not an execution member.");
		} catch (TimeRegulationIsNotEnabled ignored) {
			logger.trace("Time regulation is not enabled.");
		} catch(NotConnected ignored) {
			logger.trace("Federate is not connected.");
		} catch (RTIexception e) {
			logger.error(e);
		}
		timeRegulating.set(false);

		logger.debug("Resigning from the federation execution.");
		try {
			rtiAmbassador.resignFederationExecution(ResignAction.DELETE_OBJECTS_THEN_DIVEST);
		} catch (FederateNotExecutionMember ignored) {
			logger.trace("Federate is not an execution member.");
		} catch (NotConnected ignored) { 
			logger.trace("Federate is not connected.");
		} catch (RTIexception e) {
			logger.error(e);
		}

		logger.debug("Destroying the federation execution.");
		try {
			rtiAmbassador.destroyFederationExecution(connection.getFederationName());
		} catch (FederatesCurrentlyJoined ignored) {
			logger.trace("Other federates are currently joined.");
		} catch (FederationExecutionDoesNotExist ignored) {
			logger.trace("Federation execution does not exist.");
		} catch (NotConnected ignored) {
			logger.trace("Federate is not connected.");
		} catch (RTIexception e) {
			logger.error(e);
		}

		logger.debug("Removing all remaining object instances.");
		synchronized(objectInstanceHandleMap) {
			for(HLAobject hlaObject : objectInstanceHandleMap.values()) {
				fireObjectRemovedEvent(hlaObject);
			}
			objectInstanceHandleMap.clear();
		}
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#timeAdvanceGrant(hla.rti1516e.LogicalTime)
	 */
	@Override
	public void timeAdvanceGrant(LogicalTime theTime)
			throws FederateInternalError {
		if(theTime instanceof HLAfloat64Time) {
			logicalTime = (HLAfloat64Time) theTime;
		} else {
			String message = "Incompatible time. Expected " 
					+ HLAfloat64Time.class + " but received " 
					+ theTime.getClass() + ".";
			logger.fatal(message);
		}
		logger.info("Time advance granted to logical time " 
				+ logicalTime.getValue());
		timeAdvancing.set(false);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#timeConstrainedEnabled(hla.rti1516e.LogicalTime)
	 */
	@Override
	public void timeConstrainedEnabled(LogicalTime time)
			throws FederateInternalError {
		if(time instanceof HLAfloat64Time) {
			logicalTime = (HLAfloat64Time) time;
		} else {
			String message = "Incompatible time. Expected " 
					+ HLAfloat64Time.class + " but received " 
					+ time.getClass() + ".";
			logger.fatal(message);
		}
		logger.info("Time constrained enabled with logical time " 
				+ logicalTime.getValue() + ".");
		timeConstrained.set(true);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#timeRegulationEnabled(hla.rti1516e.LogicalTime)
	 */
	@Override
	public void timeRegulationEnabled(LogicalTime time)
			throws FederateInternalError {
		if(time instanceof HLAfloat64Time) {
			logicalTime = (HLAfloat64Time) time;
		} else {
			String message = "Incompatible time. Expected " 
					+ HLAfloat64Time.class + " but received " 
					+ time.getClass() + ".";
			logger.fatal(message);
		}
		logger.info("Time regulation enabled with logical time " 
				+ logicalTime.getValue() + ".");
		timeRegulating.set(true);
	}
}
