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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;

/**
 * A graphical user interface component to display dates. Includes separate
 * text fields for year, month, day, hour, minute, and second fields with
 * a time scale drop-down combo box (currently only one value: UTC).
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class DatePanel extends JPanel implements SimulationTimeListener {
	private static Logger logger = Logger.getLogger(DatePanel.class);
	private static final long serialVersionUID = -8399088532123474707L;
	
	private static final NumberFormat yearFormat = 
			NumberFormat.getIntegerInstance();
	{
		yearFormat.setGroupingUsed(false);
		yearFormat.setMinimumIntegerDigits(4);
		yearFormat.setMaximumIntegerDigits(4);
	}
	private static final NumberFormat twoDigitNumberFormat = 
			NumberFormat.getIntegerInstance();
	{
		twoDigitNumberFormat.setGroupingUsed(false);
		twoDigitNumberFormat.setMinimumIntegerDigits(1);
		twoDigitNumberFormat.setMaximumIntegerDigits(2);
	}
	
	private final JFormattedTextField yearField, monthField, dayField, 
			hourField, minuteField, secondField;
	private final JComboBox<TimeScale> timeScaleCombo;
	
	/**
	 * Instantiates a new date panel.
	 */
	public DatePanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel("Absolute Date: "), c);
		c.gridx++;
		c.gridwidth = 2;
		yearField = new JFormattedTextField(yearFormat);
		yearField.setColumns(4);
		yearField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		monthField = new JFormattedTextField(twoDigitNumberFormat);
		monthField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		monthField.setColumns(2);
		dayField = new JFormattedTextField(twoDigitNumberFormat);
		dayField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		dayField.setColumns(2);
		hourField = new JFormattedTextField(twoDigitNumberFormat);
		hourField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		hourField.setColumns(2);
		minuteField = new JFormattedTextField(twoDigitNumberFormat);
		minuteField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		minuteField.setColumns(2);
		secondField = new JFormattedTextField(NumberFormat.getNumberInstance());
		secondField.setHorizontalAlignment(JFormattedTextField.RIGHT);
		secondField.setColumns(4);
		TimeScale[] timeScales = null;
		try {
			timeScales = new TimeScale[]{TimeScalesFactory.getUTC()};
		} catch (OrekitException e) {
			logger.error(e);
		}
		timeScaleCombo = new JComboBox<TimeScale>(timeScales);
		JPanel datePanel = new JPanel();
		datePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		datePanel.add(yearField);
		datePanel.add(new JLabel("-"));
		datePanel.add(monthField);
		datePanel.add(new JLabel("-"));
		datePanel.add(dayField);
		datePanel.add(new JLabel("T"));
		datePanel.add(hourField);
		datePanel.add(new JLabel(":"));
		datePanel.add(minuteField);
		datePanel.add(new JLabel(":"));
		datePanel.add(secondField);
		datePanel.add(new JLabel(" "));
		datePanel.add(timeScaleCombo);
		add(datePanel, c);
		c.gridy++;
		c.weightx = 0;
		c.gridx = 0;

		// disable all fields
		timeScaleCombo.setEnabled(false);
		yearField.setEditable(false);
		monthField.setEditable(false);
		dayField.setEditable(false);
		hourField.setEditable(false);
		minuteField.setEditable(false);
		secondField.setEditable(false);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.SimulationTimeListener#timeAdvanced(edu.mit.fss.event.SimulationTimeEvent)
	 */
	@Override
	public void timeAdvanced(SimulationTimeEvent event) {
		final AbsoluteDate date;
		try {
			// parse the new date in UTC
			date = new AbsoluteDate(
					new Date(event.getTime()), TimeScalesFactory.getUTC());
		} catch (OrekitException e) {
			logger.error(e);
			return;
		}
		
		// run update in the event dispatch thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				yearField.setValue(date.getComponents(
						(TimeScale)timeScaleCombo.getSelectedItem())
						.getDate().getYear());
				monthField.setValue(date.getComponents(
						(TimeScale)timeScaleCombo.getSelectedItem())
						.getDate().getMonth());
				dayField.setValue(date.getComponents(
						(TimeScale)timeScaleCombo.getSelectedItem())
						.getDate().getDay());
				hourField.setValue(date.getComponents(
						(TimeScale)timeScaleCombo.getSelectedItem())
						.getTime().getHour());
				minuteField.setValue(date.getComponents(
						(TimeScale)timeScaleCombo.getSelectedItem())
						.getTime().getMinute());
				secondField.setValue(date.getComponents(
						(TimeScale)timeScaleCombo.getSelectedItem())
						.getTime().getSecond());
			}
		});
	}
}
