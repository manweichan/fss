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

import javax.swing.JCheckBox;
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
import edu.mit.fss.OrbitalElement;
import edu.mit.fss.ReferenceFrame;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.examples.member.OrekitOrbitalElement;

/**
 * A graphical user interface component for a {@link OrbitalElement} object.
 * Includes fields for semi-major axis, eccentricity, inclination, perigee 
 * argument, right ascension of ascending node, mean anomaly, and reference
 * frame.
 * <p>
 * Also handles a {@link OrekitOrbitalElement} objects by computing slant 
 * range and line-of-sight visibility values for all observed elements 
 * and displaying umbra/penumbra state.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class OrbitalElementPanel extends JPanel implements ObjectChangeListener {
	private static Logger logger = Logger.getLogger(OrbitalElementPanel.class);
	private static final long serialVersionUID = -2716904316482348969L;
	
	private final OrbitalElement element;
	private final JFormattedTextField semiMajorAxisField, eccentricityField, 
		inclinationField, perigeeArgumentField, rightAscensionAscendingNodeField, 
		meanAnomalyField;
	private final JCheckBox inUmbraCheck = new JCheckBox("In Umbra");
	private final JCheckBox inPenumbraCheck = new JCheckBox("In Penumbra");
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
			if(element instanceof OrekitOrbitalElement) {
				// can display all fields
				return 3;
			} else {
				// cannot compute slant range or line-of-sight visibility
				return 1;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return observedElements.get(rowIndex).getName();
			case 1: // slant range
				if(element instanceof OrekitOrbitalElement) {
					return ((OrekitOrbitalElement)element).getSlantRange(
							observedElements.get(rowIndex));
				} else {
					return 0.0;
				}
			case 2: // line-of-sight visibility
				if(element instanceof OrekitOrbitalElement) {
					return ((OrekitOrbitalElement)element).isLineOfSightVisible(
							observedElements.get(rowIndex));
				} else {
					return false;
				}
			default:
				return null;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return "Element Name";
			case 1: // slant range
				return "Slant Range (m)";
			case 2: // line-of-sight visibility
				return "Line-of-sight Visible";
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
			case 2: // line-of-sight visibility
				return Boolean.class;
			default:
				return null;
			}
		}
	};
	
	/**
	 * Instantiates a new orbital element panel for an orbital element.
	 *
	 * @param element the element
	 */
	public OrbitalElementPanel(OrbitalElement element) {
		this.element = element;
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel("Frame: "), c);
		c.gridx++;
		c.gridwidth = 5;
		frameCombo = new JComboBox<ReferenceFrame>(ReferenceFrame.values());
		add(frameCombo, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		add(new JLabel("Semi-major Axis: "), c);
		c.gridx++;
		c.weightx = 1;
		semiMajorAxisField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		semiMajorAxisField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		add(semiMajorAxisField, c);
		c.gridx++;
		c.weightx = 0;
		add(new JLabel("m"), c);
		c.gridy++;
		c.gridx = 0;
		add(new JLabel("Eccentricity: "), c);
		c.gridx++;
		eccentricityField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		eccentricityField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		add(eccentricityField, c);
		c.gridx++;
		add(new JLabel("-"), c);
		c.gridy++;
		c.gridx = 0;
		add(new JLabel("Inclination: "), c);
		c.gridx++;
		inclinationField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		inclinationField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		add(inclinationField, c);
		c.gridx++;
		add(new JLabel("\u00b0"), c);
		c.gridy = 1;
		c.gridx = 3;
		add(new JLabel("Perigee Argument: "), c);
		c.gridx++;
		c.weightx = 1;
		perigeeArgumentField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		perigeeArgumentField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		add(perigeeArgumentField, c);
		c.gridx++;
		c.weightx = 0;
		add(new JLabel("\u00b0"), c);
		c.gridy++;
		c.gridx = 3;
		add(new JLabel("Right Ascention of Ascending Node: "), c);
		c.gridx++;
		rightAscensionAscendingNodeField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		rightAscensionAscendingNodeField.setHorizontalAlignment(
				JFormattedTextField.RIGHT);
		add(rightAscensionAscendingNodeField, c);
		c.gridx++;
		add(new JLabel("\u00b0"), c);
		c.gridy++;
		c.gridx = 3;
		add(new JLabel("Mean Anomaly: "), c);
		c.gridx++;
		meanAnomalyField = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		meanAnomalyField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		add(meanAnomalyField, c);
		c.gridx++;
		add(new JLabel("\u00b0"), c);
		c.gridy++;
		if(element instanceof OrekitOrbitalElement) {
			// can display umbra/penumbra state information
			c.gridx = 1;
			add(inUmbraCheck, c);
			c.gridx+=3;
			add(inPenumbraCheck, c);
			c.gridy++;
			c.gridx = 0;

			// can display information for observed elements
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			add(new JLabel("Observed Elements: "), c);
			c.gridx++;
			c.gridwidth = 5;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			JTable observedElementsTable = new JTable(observedElementsModel);
			observedElementsTable.setPreferredScrollableViewportSize(
					new Dimension(400,250));
			add(new JScrollPane(observedElementsTable), c);
		}

		// disable all fields
		frameCombo.setEnabled(false);
		semiMajorAxisField.setEditable(false);
		eccentricityField.setEditable(false);
		inclinationField.setEditable(false);
		perigeeArgumentField.setEditable(false);
		rightAscensionAscendingNodeField.setEditable(false);
		meanAnomalyField.setEditable(false);
		inUmbraCheck.setEnabled(false);
		inPenumbraCheck.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectDiscovered(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectDiscovered(final ObjectChangeEvent event) {
		if(event.getObject() instanceof Element) {
			// run update in event dispatch thread for thread safety
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
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
				});
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error(e);
			}
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
						semiMajorAxisField.setValue(element.getSemimajorAxis());
						eccentricityField.setValue(element.getEccentricity());
						inclinationField.setValue(element.getInclination());
						perigeeArgumentField.setValue(
								element.getArgumentOfPeriapsis());
						rightAscensionAscendingNodeField.setValue(
								element.getLongitudeOfAscendingNode());
						meanAnomalyField.setValue(element.getMeanAnomaly());
						if(element instanceof OrekitOrbitalElement) {
							inUmbraCheck.setSelected(
									((OrekitOrbitalElement) element).isInUmbra());
							inPenumbraCheck.setSelected(
									((OrekitOrbitalElement) element).isInPenumbra());
						}
						observedElementsModel.fireTableRowsUpdated(0, 
								observedElements.size() - 1);
					} else if(event.getObject() instanceof Element) {
						Element e = (Element) event.getObject();
						logger.trace("Adding observed element " 
								+ e.getName() + ".");
						observedElements.add(e);
						observedElementsModel.fireTableRowsInserted(
								observedElements.indexOf(e), 
								observedElements.indexOf(e));
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
