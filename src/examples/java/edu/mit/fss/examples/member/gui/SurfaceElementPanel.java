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
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.mit.fss.Element;
import edu.mit.fss.ReferenceFrame;
import edu.mit.fss.SurfaceElement;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.examples.member.OrekitSurfaceElement;

/**
 * A graphical user interface component for a {@link SurfaceElement} object.
 * Includes fields for latitude, longitude, altitude, and reference frame.
 * <p>
 * Also handles a {@link OrekitSurfaceElement} objects by computing slant 
 * range, elevation and azimuth values for all observed elements.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class SurfaceElementPanel extends JPanel implements ObjectChangeListener {
	private static Logger logger = Logger.getLogger(SurfaceElementPanel.class);
	private static final long serialVersionUID = -2716904316482348969L;
	
	private final SurfaceElement element;
	private final JFormattedTextField latitudeField, longitudeField, 
			altitudeField;
	private final JComboBox<ReferenceFrame> frameCombo;
	private final List<Element> observedElements = new ArrayList<Element>();

	// custom table model to display information for observed elements
	private final AbstractTableModel observedElementsModel = 
			new AbstractTableModel() {
		private static final long serialVersionUID = -4709357288050587554L;

		@Override
		public int getRowCount() {
			return observedElements.size();
		}

		@Override
		public int getColumnCount() {
			if(element instanceof OrekitSurfaceElement) {
				// can display all fields
				return 4;
			} else {
				// cannot compute slant range, elevation, or azimuth
				return 1;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return observedElements.get(rowIndex).getName();
			case 1: // slant range
				if(element instanceof OrekitSurfaceElement) {
					return ((OrekitSurfaceElement)element).getSlantRange(
							observedElements.get(rowIndex));
				} else {
					return 0.0;
				}
			case 2: // elevation angle
				if(element instanceof OrekitSurfaceElement) {
					return ((OrekitSurfaceElement)element).getElevation(
							observedElements.get(rowIndex));
				} else {
					return 0.0;
				}
			case 3: // azimuth angle
				if(element instanceof OrekitSurfaceElement) {
					return ((OrekitSurfaceElement)element).getAzimuth(
							observedElements.get(rowIndex));
				} else {
					return 0.0;
				}
			default:
				return null;
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return "Element Name";
			case 1: // slant range
				return "Slant Range (m)";
			case 2: // elevation angle
				return "Elevation (\u00b0)";
			case 3: // azimuth angle
				return "Azimuth (\u00b0)";
			default:
				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return String.class;
			case 1: // slant range
				return Double.class;
			case 2: // elevation angle
				return Double.class;
			case 3: // azimuth angle
				return Double.class;
			default:
				return null;
			}
		}
	};
	
	/**
	 * Instantiates a new surface element panel for a surface element.
	 *
	 * @param element the element
	 */
	public SurfaceElementPanel(SurfaceElement element) {
		this.element = element;
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel("Frame: "), c);
		c.gridx++;
		frameCombo = new JComboBox<ReferenceFrame>(ReferenceFrame.values());
		add(frameCombo, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		add(new JLabel("Latitude: "), c);
		c.gridx++;
		c.weightx = 1;
		latitudeField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		latitudeField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		add(latitudeField, c);
		c.gridx++;
		c.weightx = 0;
		add(new JLabel("\u00b0 N"), c);
		c.gridy++;
		c.weightx = 0;
		c.gridx = 0;
		add(new JLabel("Longitude: "), c);
		c.gridx++;
		longitudeField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		longitudeField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		add(longitudeField, c);
		c.gridx++;
		add(new JLabel("\u00b0 E"), c);
		c.gridy++;
		c.weightx = 0;
		c.gridx = 0;
		add(new JLabel("Altitude: "), c);
		c.gridx++;
		altitudeField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		altitudeField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		add(altitudeField, c);
		c.gridx++;
		add(new JLabel("m"), c);
		if(element instanceof OrekitSurfaceElement) {
			// can display information for observed elements
			c.gridy++;
			c.weightx = 0;
			c.gridx = 0;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			add(new JLabel("Observed Elements: "), c);
			c.gridx++;
			c.gridwidth = 2;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			JTable observedElementsTable = new JTable(observedElementsModel);
			observedElementsTable.setPreferredScrollableViewportSize(
					new Dimension(400,250));
			add(new JScrollPane(observedElementsTable), c);
		}

		// disable all fields
		frameCombo.setEnabled(false);
		latitudeField.setEditable(false);
		longitudeField.setEditable(false);
		altitudeField.setEditable(false);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectDiscovered(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectDiscovered(final ObjectChangeEvent event) {
		// run update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if(event.getObject() instanceof Element) {
						Element e = (Element) event.getObject();
						if(e != element && !observedElements.contains(e)) {
							logger.trace("Adding observed element " 
									+ e.getName() + ".");
							observedElements.add(e);
							observedElementsModel.fireTableRowsInserted(
									observedElements.indexOf(e), 
									observedElements.indexOf(e));
						}
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectRemoved(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectRemoved(final ObjectChangeEvent event) {
		// run update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Object object = event.getObject();
					if(observedElements.contains(object)) {
						int index = observedElements.indexOf(object);
						Element e = observedElements.remove(index);
						logger.trace("Removing observed element " 
								+ e.getName() + ".");
						observedElementsModel.fireTableRowsDeleted(index, index);
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
					
					if(observedElements.contains(object)) {
						int index = observedElements.indexOf(object);
						logger.trace("Updating observed element " 
								+ observedElements.get(index).getName() + ".");
						observedElementsModel.fireTableRowsUpdated(index, index);
					} else if(element == object) {
						logger.trace("Updating own element " 
								+ element.getName() + ".");
						frameCombo.setSelectedItem(element.getFrame());
						latitudeField.setValue(element.getLatitude());
						longitudeField.setValue(element.getLongitude());
						altitudeField.setValue(element.getAltitude());
					} else if(event.getObject() instanceof Element) {
						Element e = (Element) event.getObject();
						observedElements.add(e);
						logger.trace("Adding observed element " 
								+ e.getName() + ".");
						observedElementsModel.fireTableRowsInserted(
								observedElements.indexOf(element), 
								observedElements.indexOf(element));
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#interactionOccurred(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void interactionOccurred(ObjectChangeEvent event) { }
}
