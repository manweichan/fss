/*
 * Copyright (c) 2014, Paul T. Grogan/M.I.T., All rights reserved.
 * 
 * This file is a part of the FSS Simulation Toolkit. 
 * Please see license.txt for details.
 */
package edu.mit.fss.examples.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.mit.fss.event.ConnectionEvent;
import edu.mit.fss.event.ConnectionListener;

/**
 * A graphical user interface component to show whether a federate is 
 * connected or not from the RTI.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public final class ConnectionToolbar extends JPanel implements ConnectionListener {
	private static final long serialVersionUID = -2072804446821201002L;	
	private JLabel iconLabel;
	private static ImageIcon notConnectedIcon = 
			new ImageIcon(ConnectionToolbar.class.getResource(
					"/images/silk/bullet_red.png"));
	private static ImageIcon connectedIcon = 
			new ImageIcon(ConnectionToolbar.class.getResource(
					"/images/silk/bullet_green.png"));
	
	/**
	 * Instantiates a new connection toolbar.
	 */
	public ConnectionToolbar() {
		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
		setLayout(new BorderLayout());
		iconLabel = new JLabel("Offline");
		iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		iconLabel.setIcon(notConnectedIcon);
		iconLabel.setFont(iconLabel.getFont()
				.deriveFont(Font.PLAIN).deriveFont(9f));
		add(iconLabel, BorderLayout.EAST);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ConnectionListener#connectionEventOccurred(edu.mit.fss.event.ConnectionEvent)
	 */
	public void connectionEventOccurred(final ConnectionEvent e) {
		// run in the event dispatch thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				iconLabel.setIcon(e.getConnection().isConnected() ?
						connectedIcon : notConnectedIcon);
				iconLabel.setText(e.getConnection().isConnected() ?
						e.getConnection().getFederationName() : "Offline");
			}
		});
	}
}
