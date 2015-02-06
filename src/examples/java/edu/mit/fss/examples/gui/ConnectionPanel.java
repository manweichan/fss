/*
 * Copyright (c) 2014, Paul T. Grogan/M.I.T., All rights reserved.
 * 
 * This file is a part of the FSS Simulation Toolkit. 
 * Please see license.txt for details.
 */
package edu.mit.fss.examples.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import edu.mit.fss.Federate;
import edu.mit.fss.event.ConnectionEvent;
import edu.mit.fss.event.ConnectionListener;
import edu.mit.fss.hla.FederationConnection;

/**
 * A graphical user interface component to edit parameters for a
 * {@link FederationConnection} object and connect or disconnect
 * a {@link Federate} object from a federation. This component must be 
 * registered with a {@link FederationConnection} as a 
 * {@link ConnectionListener}.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public final class ConnectionPanel extends JPanel implements ConnectionListener {
	private static Logger logger = Logger.getLogger(ConnectionPanel.class);
	
	private static final long serialVersionUID = 8697615119488025958L;

	private static final ImageIcon loadingCompleteIcon = new ImageIcon(
			ConnectionPanel.class.getResource(
					"/images/loading_complete.png"));
	private static final ImageIcon loadingIcon = new ImageIcon(
			ConnectionPanel.class.getResource(
					"/images/loading.gif"));
	private static final String CONNECTION_DATA = "connection.data";
	
	/**
	 * Clears the saved connection data file.
	 */
	private static void clearData() {
		new File(CONNECTION_DATA).delete();
	}
	
	/**
	 * Checks if there is connection data saved.
	 *
	 * @return true, if there is data saved
	 */
	private static boolean isDataSaved() {
		InputStream input;
		try {
			input = new FileInputStream(new File(CONNECTION_DATA));
			input.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			logger.error(e);
		}
		return true;
	}
	
	private final Action browseFomAction = new AbstractAction("Browse") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if(new File(fomPath.getText()).exists()) {
				fomChooser.setCurrentDirectory(
						new File(fomPath.getText()).getParentFile());
			}
			fomChooser.setDialogTitle("Select FOM File");
			fomChooser.setFileFilter(new FileNameExtensionFilter(
					"FOM Files","xml"));
			if(fomChooser.showOpenDialog(thisPanel) 
					== JFileChooser.APPROVE_OPTION) {
				fomPath.setText(fomChooser.getSelectedFile().getAbsolutePath());
			}
		}
	};
	
	private final Action rememberInfoAction = 
			new AbstractAction("Remember connection information") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if(rememberCheck.isSelected()) {
				saveData();
			} else {
				clearData();
			}
		}
	};
	private final Action toggleOfflineAction = 
			new AbstractAction("Offline mode") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			federate.getConnection().setOfflineMode(offlineCheck.isSelected());
		}
	};
	private final Action connectAction = new AbstractAction("Connect") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if(rememberCheck.isSelected()) {
				saveData();
			}
			toggleConnection();
		}
	};
	
	private final JTextField federateName,federateType, federationName,fomPath;
	private final JCheckBox offlineCheck, rememberCheck;
	private final JLabel statusLabel;
	private final JFileChooser fomChooser = 
			new JFileChooser(System.getProperty("user.dir"));
	private final ConnectionPanel thisPanel = this;
	private Federate federate;
	
	/**
	 * Instantiates a new connection panel.
	 */
	public ConnectionPanel() {
		federateName = new JTextField(12);
		federateName.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				event.getComponent().requestFocusInWindow();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) { }

			@Override
			public void ancestorRemoved(AncestorEvent event) { }
		});
		federateName.setToolTipText("Your user name.");
		federateType = new JTextField(12);
		federateName.setToolTipText("Your organization name.");
		federationName = new JTextField(12);
		federationName.setToolTipText("The federation to join.");
		fomPath = new JTextField(20);
		fomPath.setToolTipText("The Federation Object Model (FOM) file location.");
		offlineCheck = new JCheckBox(toggleOfflineAction);
		rememberCheck = new JCheckBox(rememberInfoAction);
		JButton connectButton = new JButton(connectAction);
		JButton browseButton = new JButton(browseFomAction);
		statusLabel = new JLabel();
		
		setMinimumSize(new Dimension(400,200));
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		add(new JLabel("Name: "), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(federateName, c);
		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		add(new JLabel("Type: "), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(federateType, c);
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_START;
		add(offlineCheck, c);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		add(new JLabel("Federation: "), c);
		c.gridx++;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(federationName, c);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		add(new JLabel("FOM File: "), c);
		c.gridx++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		fomPath.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				isFomValid();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				isFomValid();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				isFomValid();
			}
		});
		add(fomPath, c);
		c.gridx += 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		add(browseButton, c);
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 3;
		add(rememberCheck, c);
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		add(connectButton, c);
		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		statusLabel.setPreferredSize(new Dimension(200,35));
		add(statusLabel, c);
	}
	
	/**
	 * Connects this panel's federate to the RTI (if not connected) or 
	 * disconnects this panel's federate from the RTI (if connected).
	 */
	private void toggleConnection() {
		logger.trace("Disable UI components while toggling connection.");
		federateName.setEnabled(false);
		federateType.setEnabled(false);
		federationName.setEnabled(false);
		fomPath.setEnabled(false);
		browseFomAction.setEnabled(false);
		rememberCheck.setEnabled(false);
		offlineCheck.setEnabled(false);
		connectAction.setEnabled(false);
		
		if(!federate.getConnection().isConnected()) {
			logger.trace("Update status label.");
			statusLabel.setText("Connecting to RTI...");
			statusLabel.setIcon(loadingIcon);

			logger.trace("Update the connection parameters.");
			federate.getConnection().setFederationName(federationName.getText());
			federate.getConnection().setFomPath(fomPath.getText());
			federate.getConnection().setFederateName(federateName.getText());
			federate.getConnection().setFederateType(federateType.getText());
			
			// use a swing worker to avoid long-running connection
			// process in event dispatch thread (and allow loading animation)
			logger.trace("Create and execute Swing Worker to connect.");
			new SwingWorker<Void,Void>() {
				@Override
				protected Void doInBackground() {
					try {
						federate.connect();
					} catch (Exception e) {
						logger.error(e);
						federateName.setEnabled(true);
						federateType.setEnabled(true);
						federationName.setEnabled(true);
						fomPath.setEnabled(true);
						browseFomAction.setEnabled(true);
						rememberCheck.setEnabled(true);
						offlineCheck.setEnabled(true);
						connectAction.setEnabled(!offlineCheck.isSelected());
						connectAction.putValue(Action.NAME, "Connect");
						statusLabel.setIcon(null);
						try {
							federate.disconnect();
						} catch (Exception ignored) { }
						statusLabel.setText("Failed (" + e.getMessage() + ")");
					}
					return null;
				}
			}.execute();
		} else {
			logger.trace("Update status label.");
			statusLabel.setText("Disconnecting...");
			statusLabel.setIcon(loadingIcon);
			
			// use a swing worker to avoid long-running connection
			// process in event dispatch thread (and allow loading animation)
			logger.trace("Create and execute Swing Worker to disconnect.");
			new SwingWorker<Void,Void>() {
				@Override
				protected Void doInBackground() {
					try {
						federate.disconnect();
					} catch (Exception e) {
						logger.error(e);
						statusLabel.setText("Failed (" + e.getMessage() + ")");
					}
					return null;
				}
			}.execute();
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.sips.gui.event.ConnectionListener#connectionEventOccurred(edu.mit.sips.gui.event.ConnectionEvent)
	 */
	@Override
	public void connectionEventOccurred(final ConnectionEvent e) {
		// use swing utilities to run in the event dispatch thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				boolean isConnected = e.getConnection().isConnected();
				logger.trace("Update UI components after connection event.");
				federateName.setEnabled(!isConnected);
				federateType.setEnabled(!isConnected);
				federationName.setEnabled(!isConnected && !e.getConnection().isOfflineMode());
				fomPath.setEnabled(!isConnected && !e.getConnection().isOfflineMode());
				browseFomAction.setEnabled(!isConnected && !e.getConnection().isOfflineMode());
				rememberCheck.setEnabled(!isConnected);
				offlineCheck.setEnabled(!isConnected);
				connectAction.setEnabled(!e.getConnection().isOfflineMode());
				connectAction.putValue(Action.NAME, 
						isConnected ? "Disconnect" : "Connect");
				statusLabel.setIcon(isConnected ? loadingCompleteIcon : null);
				
				if(isConnected) {
					statusLabel.setText("Connected to RTI.");
				} else {
					statusLabel.setText("");
				}
			}
		});
	}
	
	/**
	 * Initializes this connection panel for a federate.
	 *
	 * @param federate the federate
	 */
	public void initialize(Federate federate) {
		this.federate = federate;
		
		loadData();
		updateFields();
		isDataValid();
	}

	/**
	 * Checks if this panel has valid data inputs.
	 *
	 * @return true, if the data is valid
	 */
	private boolean isDataValid() {
		return !federateName.getText().isEmpty() 
				&& !federationName.getText().isEmpty()
				&& isFomValid();
	}
	
	/**
	 * Checks if the FOM specified in this panel is valid.
	 *
	 * @return true, if the FOM is valid
	 */
	private boolean isFomValid() {
		if(new File(fomPath.getText()).exists()) {
			fomPath.setForeground(Color.green);
			return true;
		} else {
			fomPath.setForeground(Color.red);
			return false;
		}
	}
	
	/**
	 * Loads this connection panel's data from file. Does nothing if the
	 * saved file does not exist.
	 */
	private void loadData() {
		InputStream input;
		Properties properties = new Properties();
		try {
			input = new FileInputStream(new File(CONNECTION_DATA));
			properties.loadFromXML(input);
			input.close();
		} catch (FileNotFoundException ignored) {
			// ignore file not found exception
			return;
		} catch (IOException e) {
			logger.error(e);
		}
		federate.getConnection().setOfflineMode(
				Boolean.parseBoolean(properties.getProperty("offline")));
		federate.getConnection().setFederateName(
				properties.getProperty("name"));
		federate.getConnection().setFederateType(
				properties.getProperty("type"));
		federate.getConnection().setFederationName(
				properties.getProperty("federation"));
		federate.getConnection().setFomPath(
				properties.getProperty("fom"));
	}
	
	/**
	 * Saves this connection panel's data to file.
	 */
	private void saveData() {
		OutputStream output;
		Properties properties = new Properties();
		properties.setProperty("offline", 
				new Boolean(offlineCheck.isSelected()).toString());
		properties.setProperty("name", federateName.getText());
		properties.setProperty("type", federateType.getText());
		properties.setProperty("federation", federationName.getText());
		properties.setProperty("fom", fomPath.getText());
		try {
			output = new FileOutputStream(new File(CONNECTION_DATA));
			properties.storeToXML(output, null);
			output.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	/**
	 * Updates this connection panel's fields with data from the federate.
	 */
	public void updateFields() {
		connectAction.putValue(Action.NAME, 
				federate.getConnection().isConnected() ? 
						"Disconnect" : "Connect");
		federateName.setText(federate.getConnection().getFederateName());
		federateType.setText(federate.getConnection().getFederateType());
		federationName.setText(federate.getConnection().getFederationName());
		fomPath.setText(federate.getConnection().getFomPath());
		rememberCheck.setSelected(isDataSaved());
		offlineCheck.setSelected(federate.getConnection().isOfflineMode());
	}
}
