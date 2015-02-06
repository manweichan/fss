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
package edu.mit.fss.examples.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;

import edu.mit.fss.Federate;
import edu.mit.fss.event.ConnectionEvent;
import edu.mit.fss.event.ConnectionListener;
import edu.mit.fss.event.ExecutionControlEvent;
import edu.mit.fss.event.ExecutionControlListener;
import edu.mit.fss.hla.FederationConnection;

/**
 * A graphical user interface component to control a simulation execution
 * with actions to initialize, run, stop, and terminate. Also includes options
 * to modify the simulation time step and associated minimum duration. This 
 * component must be registered with a {@link FederationConnection} as a 
 * {@link ConnectionListener} and with a {@link Federate} as a 
 * {@link ExecutionControlListener}.
 *  
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class ExecutionControlPanel extends JPanel 
		implements ConnectionListener, ExecutionControlListener {
	private static enum TimeUnit {
		MILLISECONDS("ms", 1),
		SECONDS("s", 1000), 
		MINUTES("min", 60*1000), 
		HOURS("hr", 60*60*1000), 
		DAYS("day", 24*60*60*1000);
		
		public final String abbreviation;
		public final long milliseconds;
		
		private TimeUnit(String abbreviation, long milliseconds) {
			this.abbreviation = abbreviation;
			this.milliseconds = milliseconds;
		}
		
		public String toString() {
			return abbreviation;
		}
	}
	private static Logger logger = Logger.getLogger(ExecutionControlPanel.class);
	private static final long serialVersionUID = -7014074954503228524L;
	private static ImageIcon initializeIcon = new ImageIcon(
			ExecutionControlPanel.class.getResource(
					"/images/silk/connect.png"));
	private static ImageIcon terminateIcon = new ImageIcon(
			ExecutionControlPanel.class.getResource(
					"/images/silk/disconnect.png"));
	private static ImageIcon playIcon = new ImageIcon(
			ExecutionControlPanel.class.getResource(
					"/images/silk/control_play_blue.png"));
	private static ImageIcon stopIcon = new ImageIcon(
			ExecutionControlPanel.class.getResource(
					"/images/silk/control_stop_blue.png"));
	
	private Federate federate;
	private final JFormattedTextField timeStepField, stepDurationField;
	private final JComboBox<TimeUnit> 
			timeStepUnits = new JComboBox<TimeUnit>(TimeUnit.values()),
			stepDurationUnits = new JComboBox<TimeUnit>(TimeUnit.values());
	
	private AtomicBoolean initializing = new AtomicBoolean(false),
			running = new AtomicBoolean(false),
			stopping = new AtomicBoolean(false),
			terminating = new AtomicBoolean(false);

	private final Action initializeAction = 
			new AbstractAction(null, initializeIcon) {
		private static final long serialVersionUID = 4589751151727368209L;
		@Override
		public void actionPerformed(ActionEvent e) {
			initializing.set(true);
			initializeAction.setEnabled(false);
			runAction.setEnabled(false);
			stopAction.setEnabled(false);
			terminateAction.setEnabled(false);

			logger.info("Initializing the federate.");
			// use swing worker to avoid long-running process
			// in event dispatch thread
			new SwingWorker<Void,Void>() {
				@Override
				protected Void doInBackground() {
					if(!federate.getConnection().isOfflineMode()) {
						federate.connect();
					}
					federate.initialize();
					initializing.set(false);
					return null;
				}
			}.execute();
		}
	};
	private final Action runAction = new AbstractAction(null, playIcon) {
		private static final long serialVersionUID = 4589751151727368209L;
		@Override
		public void actionPerformed(ActionEvent e) {
			running.set(true);
			initializeAction.setEnabled(false);
			runAction.setEnabled(false);
			stopAction.setEnabled(false);
			terminateAction.setEnabled(false);

			logger.info("Running the federate.");
			// use swing worker to avoid long-running process
			// in event dispatch thread
			new SwingWorker<Void,Void>() {
				@Override
				protected Void doInBackground() {
					federate.run();
					running.set(false);
					return null;
				}
			}.execute();
		}
	};
	private final Action stopAction = new AbstractAction(null, stopIcon) {
		private static final long serialVersionUID = 4589751151727368209L;
		@Override
		public void actionPerformed(ActionEvent e) {
			stopping.set(true);
			initializeAction.setEnabled(false);
			runAction.setEnabled(false);
			stopAction.setEnabled(false);
			terminateAction.setEnabled(false);
			
			logger.info("Stopping the federate.");
			// use swing worker to avoid long-running process
			// in event dispatch thread
			new SwingWorker<Void,Void>() {
				@Override
				protected Void doInBackground() {
					federate.stop();
					stopping.set(false);
					return null;
				}
			}.execute();
		}
	};
	private final Action terminateAction = 
			new AbstractAction(null, terminateIcon) {
		private static final long serialVersionUID = 4589751151727368209L;
		@Override
		public void actionPerformed(ActionEvent e) {
			terminating.set(true);
			initializeAction.setEnabled(false);
			runAction.setEnabled(false);
			stopAction.setEnabled(false);
			terminateAction.setEnabled(false);

			logger.info("Terminating the federate.");
			// use swing worker to avoid long-running process
			// in event dispatch thread
			new SwingWorker<Void,Void>() {
				@Override
				protected Void doInBackground() {
					federate.terminate();
					terminating.set(false);
					return null;
				}
			}.execute();
		}
	};
	private final Action setTimeStepAction = new AbstractAction("Set") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			long unitConversion = getTimeStepUnits();
			// convert field value to milliseconds
			long timeStep = FastMath.max(1000, FastMath.round(unitConversion
					* ((Number)timeStepField.getValue()).doubleValue()));
			federate.setTimeStep(timeStep);
			// reset converted value
			timeStepField.setValue(((double) timeStep) / unitConversion);
		}
	};
	private final Action setMinStepDurationAction = new AbstractAction("Set") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			long unitConversion = getStepDurationUnits();
			// convert field value to milliseconds
			long stepDuration = FastMath.max(0, FastMath.round(unitConversion
					* ((Number)stepDurationField.getValue()).doubleValue()));
			federate.setMinimumStepDuration(stepDuration);
			// reset converted value
			stepDurationField.setValue(((double) stepDuration) / unitConversion);
		}
	};

	/**
	 * Instantiates a new execution control panel.
	 */
	public ExecutionControlPanel() {
		initializeAction.putValue(Action.SHORT_DESCRIPTION, 
				"Initialize the execution.");
		initializeAction.setEnabled(false);
		runAction.putValue(Action.SHORT_DESCRIPTION, 
				"Run the execution.");
		runAction.setEnabled(false);
		stopAction.putValue(Action.SHORT_DESCRIPTION, 
				"Stop the execution.");
		stopAction.setEnabled(false);
		terminateAction.putValue(Action.SHORT_DESCRIPTION, 
				"Terminate the execution.");
		terminateAction.setEnabled(false);
		
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		buttonPanel.add(new JButton(initializeAction));
		buttonPanel.add(new JButton(runAction));
		buttonPanel.add(new JButton(stopAction));
		buttonPanel.add(new JButton(terminateAction));

		timeStepField = new JFormattedTextField(NumberFormat.getNumberInstance());
		timeStepField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) { checkUpdate(); }
			private void checkUpdate() {
				long unitConversion = getTimeStepUnits();
				long timeStep = FastMath.round(unitConversion
						* ((Number)timeStepField.getValue()).doubleValue());
				setTimeStepAction.setEnabled(timeStep != federate.getTimeStep());
			}
			@Override
			public void insertUpdate(DocumentEvent e) { checkUpdate(); }
			
			@Override
			public void removeUpdate(DocumentEvent e) { checkUpdate(); }
		});
		timeStepField.setColumns(4);
		buttonPanel.add(new JLabel("Time Step: "));
		buttonPanel.add(timeStepField);
		timeStepUnits.setSelectedItem(TimeUnit.SECONDS);
		buttonPanel.add(timeStepUnits);
		timeStepUnits.addActionListener(new ActionListener() {
			private long lastUnitConversion = getTimeStepUnits();
			@Override
			public void actionPerformed(ActionEvent e) {
				long unitConversion = getTimeStepUnits();
				// update field value using new units
				timeStepField.setValue(((Double)timeStepField.getValue())
						* lastUnitConversion / unitConversion);
				// update stored value
				lastUnitConversion = unitConversion;
			}
		});
		buttonPanel.add(new JButton(setTimeStepAction));
		
		buttonPanel.add(new JLabel("Min. Step Duration"));
		stepDurationField = new JFormattedTextField(NumberFormat.getNumberInstance());
		stepDurationField.setColumns(4);
		stepDurationField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) { checkUpdate(); }
			private void checkUpdate() {
				long unitConversion = getStepDurationUnits();
				long minStepDuration = FastMath.round(unitConversion
						* ((Number)stepDurationField.getValue()).doubleValue());
				setMinStepDurationAction.setEnabled(minStepDuration 
						!= federate.getMinimumStepDuration());
			}
			@Override
			public void insertUpdate(DocumentEvent e) { checkUpdate(); }
			
			@Override
			public void removeUpdate(DocumentEvent e) { checkUpdate(); }
		});
		buttonPanel.add(stepDurationField);
		stepDurationUnits.setSelectedItem(TimeUnit.MILLISECONDS);
		buttonPanel.add(stepDurationUnits);
		stepDurationUnits.addActionListener(new ActionListener() {
			private long lastUnitConversion = getStepDurationUnits();
			@Override
			public void actionPerformed(ActionEvent e) {
				long unitConversion = getStepDurationUnits();
				// update field value using new units
				stepDurationField.setValue(((Double)stepDurationField.getValue())
						* lastUnitConversion / unitConversion);
				// update stored value
				lastUnitConversion = unitConversion;
			}
		});
		buttonPanel.add(new JButton(setMinStepDurationAction));
		
		add(buttonPanel, BorderLayout.CENTER);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.sips.gui.event.ConnectionListener#connectionEventOccurred(edu.mit.sips.gui.event.ConnectionEvent)
	 */
	@Override
	public void connectionEventOccurred(final ConnectionEvent e) {
		// run in the event dispatch thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				if(!e.getConnection().isConnected()) {
					// disable all actions if disconnected
					runAction.setEnabled(false);
					stopAction.setEnabled(false);
					terminateAction.setEnabled(false);
					initializeAction.setEnabled(
							federate.getConnection().isOfflineMode());
				} else {
					// enable initialize action if connected and not initializing
					initializeAction.setEnabled(!initializing.get());
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.ExecutionControlListener#executionInitialized(edu.mit.fss.gui.ExecutionControlEvent)
	 */
	@Override
	public void executionInitialized(ExecutionControlEvent event) {
		// run in the event dispatch thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// disable initialize/stop actions
				initializeAction.setEnabled(false);
				stopAction.setEnabled(false);
				
				// enable run/terminate actions
				runAction.setEnabled(true);
				terminateAction.setEnabled(true);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.ExecutionControlListener#executionStarted(edu.mit.fss.gui.ExecutionControlEvent)
	 */
	@Override
	public void executionStarted(ExecutionControlEvent event) {
		// run in the event dispatch thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// disable initialize/run actions
				initializeAction.setEnabled(false);
				runAction.setEnabled(false);
				
				// enable stop/terminate actions
				stopAction.setEnabled(true);
				terminateAction.setEnabled(true);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.ExecutionControlListener#executionStopped(edu.mit.fss.gui.ExecutionControlEvent)
	 */
	@Override
	public void executionStopped(ExecutionControlEvent event) {
		// run in the event dispatch thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// disable initialize action and run action (if not terminating)
				initializeAction.setEnabled(false);
				runAction.setEnabled(!terminating.get());

				// enable stop action and terminate action (if not terminating)
				stopAction.setEnabled(false);
				terminateAction.setEnabled(!terminating.get());
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.ExecutionControlListener#executionTerminated(edu.mit.fss.gui.ExecutionControlEvent)
	 */
	@Override
	public void executionTerminated(ExecutionControlEvent event) {
		// run in the event dispatch thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// enable initialize action
				initializeAction.setEnabled(true);
				
				// disable run/stop/terminate actions
				runAction.setEnabled(false);
				stopAction.setEnabled(false);
				terminateAction.setEnabled(false);
			}
		});
	}

	/**
	 * Gets this panel's step duration units value in milliseconds.
	 *
	 * @return the step duration units
	 */
	private long getStepDurationUnits() {
		return ((TimeUnit) stepDurationUnits.getSelectedItem()).milliseconds;
	}

	/**
	 * Gets this panel's time step units value in milliseconds.
	 *
	 * @return the time step units
	 */
	private long getTimeStepUnits() {
		return ((TimeUnit) timeStepUnits.getSelectedItem()).milliseconds;
	}

	/**
	 * Initializes this execution control panel for a federate.
	 *
	 * @param federate the federate
	 */
	public void initialize(Federate federate) {
		this.federate = federate;
		
		// enable initialize action (if connected or offline mode selected)
		initializeAction.setEnabled(federate.getConnection().isConnected() 
				|| federate.getConnection().isOfflineMode());

		// disable run/stop/terminate actions
		runAction.setEnabled(false);
		stopAction.setEnabled(false);
		terminateAction.setEnabled(false);

		// set initial time step and step duration fields
		timeStepField.setValue(federate.getTimeStep() / (double) getTimeStepUnits());
		stepDurationField.setValue(federate.getMinimumStepDuration() 
				/ (double) getStepDurationUnits());
	}
}
