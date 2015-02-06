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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import edu.mit.fss.Federate;

/**
 * A top-level graphical user interface component for a {@link Federate} having
 * {@link ConnectionToolbar} and {@link ConnectionPanel} components
 * to display/edit connection parameters and a {@link ExecutionControlPanel}
 * component to control the simulation execution.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class FederateFrame extends JFrame {
	private static Logger logger = Logger.getLogger(FederateFrame.class);
	private static final long serialVersionUID = 620103555577061986L;
	
	protected final Federate federate;
	private final ConnectionToolbar connectionToolbar = 
			new ConnectionToolbar();
	private final ExecutionControlPanel controlPanel = 
			new ExecutionControlPanel();
	private final ConnectionPanel connectionPanel = 
			new ConnectionPanel();
	private final Action exitAction = new AbstractAction("Exit") {
		private static final long serialVersionUID = -64454511324594369L;
		@Override
		public void actionPerformed(ActionEvent e) {
			exit();
		}
	};
	private final Action editConnectionAction = new AbstractAction("Edit Connection") {
		private static final long serialVersionUID = -64454511324594369L;
		@Override
		public void actionPerformed(ActionEvent e) {
			showConnectionDialog();
		}
	};
	
	/**
	 * Instantiates a new federate frame for a federate.
	 *
	 * @param federate the federate
	 */
	public FederateFrame(final Federate federate) {
		logger.trace("Assigning the federate.");
		this.federate = federate;
		
        JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(exitAction));
		menuBar.add(fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		editConnectionAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		editConnectionAction.putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
		editMenu.add(new JMenuItem(editConnectionAction));
		menuBar.add(editMenu);
		setJMenuBar(menuBar);

		connectionPanel.initialize(federate);
		federate.getConnection().addConnectionListener(connectionPanel);
		federate.getConnection().addConnectionListener(connectionToolbar);
		getContentPane().add(connectionToolbar, BorderLayout.SOUTH);
		
		controlPanel.initialize(federate);
		federate.getConnection().addConnectionListener(controlPanel);
		federate.addExecutionControlListener(controlPanel);
		getContentPane().add(controlPanel, BorderLayout.NORTH);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}
	
	/**
	 * Exits the application.
	 */
	private void exit() {
		// use swing worker to avoid RTI processes 
		// in event dispatch thread
		new SwingWorker<Void,Void>() {
			@Override
			protected Void doInBackground() {
				federate.exit();
				return null;
			}
		}.execute();
	}

	/**
	 * Shows this frame's connection dialog.
	 */
	private void showConnectionDialog() {
		connectionPanel.updateFields();
		JOptionPane.showMessageDialog(this, connectionPanel,
				"Edit Connection", JOptionPane.PLAIN_MESSAGE);
	}
}
