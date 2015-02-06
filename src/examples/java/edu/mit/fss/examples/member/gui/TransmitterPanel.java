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
package edu.mit.fss.examples.member.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.mit.fss.Federate;
import edu.mit.fss.Transmitter;
import edu.mit.fss.Signal;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.examples.member.DefaultSignal;
import edu.mit.fss.examples.member.DefaultTransmitter;

/**
 * A graphical user interface component for a {@link Transmitter} object.
 * Includes fields for name, element name, and type.
 * <p>
 * Also handles a {@link DefaultTransmitter} objects by displaying active and
 * inactive states and a table of signals sent.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class TransmitterPanel extends JPanel implements ObjectChangeListener {
	private static Logger logger = Logger.getLogger(TransmitterPanel.class);
	private static final long serialVersionUID = 7958408451507702190L;

	private final Federate federate;
	private final Transmitter transmitter;
	private final JTextField nameField, elementNameField, typeField;
	private final JLabel stateLabel;
	private final ImageIcon inactiveIcon = new ImageIcon(
			getClass().getResource("/images/silk/bullet_white.png"));
	private final ImageIcon activeIcon = new ImageIcon(
			getClass().getResource("/images/silk/bullet_green.png"));
	private final List<Signal> signalList = new ArrayList<Signal>();
	
	// custom table model to display signal information
	private final AbstractTableModel signalTableModel = 
			new AbstractTableModel() {
		private static final long serialVersionUID = 3001625889661871005L;

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0: // signal content
				return String.class;
			default:
				return null;
			}
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0: // signal content
				return "Signal Content";
			default:
				return null;
			}
		}

		@Override
		public int getRowCount() {
			return signalList.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // signal content
				return signalList.get(rowIndex).getContent();
			default:
				return null;
			}
		}
	};
	private final Action toggleTransmitterState = 
			new AbstractAction("Toggle TX") {
		private static final long serialVersionUID = 1040399168242433731L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if(transmitter instanceof DefaultTransmitter) {
				((DefaultTransmitter) transmitter).setActive(
						!((DefaultTransmitter) transmitter).isActive());
			}
		}
	};
	
	private final TransmitterPanel thisPanel = this;
	private final Action sendTransmissionAction = 
			new AbstractAction("Send TX") {
		private static final long serialVersionUID = 1040399168242433731L;

		@Override
		public void actionPerformed(ActionEvent e) {
			final String content = JOptionPane.showInputDialog(thisPanel,
					"Enter transmission content");
			if(content != null) {
				federate.sendInteraction(new DefaultSignal(
						((DefaultTransmitter)transmitter).getElement(), 
						transmitter, content));
				
			}
		}
	};
	
	/**
	 * Instantiates a new transmitter panel for a transmitter. Signals are
	 * sent via the associated {@link federate}.
	 *
	 * @param federate the federate
	 * @param transmitter the transmitter
	 */
	public TransmitterPanel(Federate federate, Transmitter transmitter) {
		this.federate = federate;
		this.transmitter = transmitter;

		toggleTransmitterState.setEnabled(
				transmitter instanceof DefaultTransmitter);
		sendTransmissionAction.setEnabled(
				transmitter instanceof DefaultTransmitter 
				&& ((DefaultTransmitter)transmitter).isActive());
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.weighty = 0;
		c.weightx = 0;
		add(new JLabel("Name: "), c);
		c.gridx++;
		c.gridwidth = 2;
		nameField = new JTextField(10);
		nameField.setEnabled(false);
		add(nameField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		add(new JLabel("Element Name: "), c);
		c.gridx++;
		c.gridwidth = 2;
		elementNameField = new JTextField(10);
		elementNameField.setEnabled(false);
		add(elementNameField, c);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.gridwidth = 1;
		add(new JLabel("Transmitter Type: "), c);
		c.gridx++;
		c.gridwidth = 2;
		typeField = new JTextField(10);
		typeField.setEnabled(false);
		add(typeField, c);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.gridwidth = 1;
		add(new JLabel("State: "), c);
		c.gridx++;
		c.weightx = 1;
		stateLabel = new JLabel("Active");
		stateLabel.setIcon(inactiveIcon);
		add(stateLabel, c);
		c.gridx++;
		c.weightx = 0;
		if(transmitter instanceof DefaultTransmitter) {
			// can toggle state
			add(new JButton(toggleTransmitterState), c);
			
			// can display signal information
			c.gridy++;
			c.weightx = 0;
			c.gridx = 0;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			add(new JLabel("Signals Transmitted: "), c);
			c.gridx++;
			c.gridwidth = 2;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			JTable signalTable = new JTable(signalTableModel);
			signalTable.setPreferredScrollableViewportSize(new Dimension(400,250));
			add(new JScrollPane(signalTable), c);
			c.gridy++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.LINE_END;
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0;
			c.gridwidth = 3;
			c.weighty = 0;
			add(new JButton(sendTransmissionAction), c);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#interactionOccurred(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void interactionOccurred(final ObjectChangeEvent event) {
		// run update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					// check if signal is sent by this transmitter
					if(event.getObject() instanceof Signal 
							&& ((Signal)event.getObject()).getTransmitter() 
							== transmitter) {
						Signal signal = ((Signal)event.getObject());
						logger.trace("Adding signal " + signal + ".");
						signalList.add(signal);
						signalTableModel.fireTableRowsInserted(
								signalList.indexOf(signal), 
								signalList.indexOf(signal));
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectChanged(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectChanged(final ObjectChangeEvent event) {
		// run update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Object object = event.getObject();
					if(transmitter == object) {
						logger.trace("Updating transmitter " 
								+ transmitter.getName() + ".");
						nameField.setText(transmitter.getName());
						elementNameField.setText(transmitter.getElementName());
						typeField.setText(transmitter.getTransmitterType());
						stateLabel.setText(transmitter.getTransmitterState().equals(
								DefaultTransmitter.STATE_ACTIVE)? 
										"Active" : "Inactive");
						stateLabel.setIcon(transmitter.getTransmitterState().equals(
								DefaultTransmitter.STATE_ACTIVE)? 
										activeIcon : inactiveIcon);
						sendTransmissionAction.setEnabled(
								transmitter instanceof DefaultTransmitter 
								&& ((DefaultTransmitter)transmitter).isActive());
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectDiscovered(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectDiscovered(ObjectChangeEvent event) { }

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectRemoved(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectRemoved(ObjectChangeEvent event) { }
}
