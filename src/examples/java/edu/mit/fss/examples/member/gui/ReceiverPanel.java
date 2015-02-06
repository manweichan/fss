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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.mit.fss.Receiver;
import edu.mit.fss.Signal;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.examples.member.DefaultReceiver;

/**
 * A graphical user interface component for a {@link Receiver} object.
 * Includes fields for name, element name, and type.
 * <p>
 * Also handles a {@link DefaultReceiver} objects by displaying active and
 * inactive states and a table of signals received.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class ReceiverPanel extends JPanel implements ObjectChangeListener {
	private static Logger logger = Logger.getLogger(ReceiverPanel.class);
	private static final long serialVersionUID = -1086431223715782245L;
	
	private final Receiver receiver;
	private final JTextField nameField, elementNameField, typeField;
	private final JLabel stateLabel;
	private final ImageIcon inactiveIcon = new ImageIcon(
			getClass().getResource("/images/silk/bullet_white.png"));
	private final ImageIcon activeIcon = new ImageIcon(
			getClass().getResource("/images/silk/bullet_green.png"));
	private final List<Signal> signalList = new ArrayList<Signal>();
	private final List<Boolean> signalReceivedList = new ArrayList<Boolean>();
	
	// custom table model to display signal information
	private final AbstractTableModel signalTableModel = new AbstractTableModel() {
		private static final long serialVersionUID = 3001625889661871005L;

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0: // signal content
				return String.class;
			case 1: // received
				return Boolean.class;
			default:
				return null;
			}
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0: // signal content
				return "Signal Content";
			case 1: // received
				return "Received";
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
			case 1: // received
				return signalReceivedList.get(rowIndex);
			default:
				return null;
			}
		}
	};
	private final Action toggleReceiverState = 
			new AbstractAction("Toggle RX") {
		private static final long serialVersionUID = 1040399168242433731L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if(receiver instanceof DefaultReceiver) {
				((DefaultReceiver) receiver).setActive(
						!((DefaultReceiver) receiver).isActive());
			}
		}
	};
	
	/**
	 * Instantiates a new receiver panel for a receiver.
	 *
	 * @param receiver the receiver
	 */
	public ReceiverPanel(Receiver receiver) {
		this.receiver = receiver;

		toggleReceiverState.setEnabled(receiver instanceof DefaultReceiver);
		
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
		add(new JLabel("Receiver Type: "), c);
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
		if(receiver instanceof DefaultReceiver) {
			// can toggle state
			add(new JButton(toggleReceiverState), c);
			
			// can display signal information
			c.gridy++;
			c.weightx = 0;
			c.gridx = 0;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			add(new JLabel("Signals Received: "), c);
			c.gridx++;
			c.gridwidth = 2;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			JTable signalTable = new JTable(signalTableModel);
			signalTable.setPreferredScrollableViewportSize(
					new Dimension(400,250));
			add(new JScrollPane(signalTable), c);
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
					// add signals sent by other transmitters
					if(event.getObject() instanceof Signal
							&& receiver instanceof DefaultReceiver
							&& !((Signal)event.getObject())
							.getTransmitterName().equals(
									receiver.getTransmitterName())) {
						Signal signal = ((Signal)event.getObject());
						logger.trace("Adding signal " + signal + ".");
						boolean received = ((DefaultReceiver)receiver)
								.isSignalReceived(signal);
						signalList.add(signal);
						signalReceivedList.add(received);
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
					if(receiver == object) {
						logger.trace("Updating receiver " 
								+ receiver.getName() + ".");
						nameField.setText(receiver.getName());
						elementNameField.setText(receiver.getElementName());
						typeField.setText(receiver.getReceiverType());
						if(receiver instanceof DefaultReceiver) {
							stateLabel.setText(receiver.getReceiverState().equals(
									DefaultReceiver.STATE_ACTIVE)? 
											"Active" : "Inactive");
							stateLabel.setIcon(receiver.getReceiverState().equals(
									DefaultReceiver.STATE_ACTIVE)? 
											activeIcon : inactiveIcon);
						} else {
							stateLabel.setText(receiver.getReceiverState());
						}
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
