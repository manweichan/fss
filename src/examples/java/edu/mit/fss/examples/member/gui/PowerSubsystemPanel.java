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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import edu.mit.fss.examples.member.SpacePowerSubsystem;

/**
 * A graphical user interface component for a {@link SpacePowerSubsystem} 
 * object. Composes plots of energy storage and transformation (generation
 * and consumption).
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class PowerSubsystemPanel extends JTabbedPane 
		implements SimulationTimeListener {
	private static Logger logger = Logger.getLogger(PowerSubsystemPanel.class);
	private static final long serialVersionUID = 8300971564851772607L;

	private final SpacePowerSubsystem subsystem;
	private final JFreeChart storageChart, powerChart;
	private final TimeSeriesCollection storageDataset, powerDataset;
	private final TimeSeries storageSeries, generationSeries, consumptionSeries;
	private final JFileChooser fileChooser = new JFileChooser();
	private final Action exportAction = new AbstractAction("Export") {
		private static final long serialVersionUID = -7171676288524836282L;

		public void actionPerformed(ActionEvent e) {
			exportDataset();
		}
	};
	
	/**
	 * Instantiates a new power subsystem panel for a subsystem.
	 *
	 * @param subsystem the subsystem
	 */
	public PowerSubsystemPanel(SpacePowerSubsystem subsystem) {
		this.subsystem = subsystem;

		logger.trace("Creating and adding energy storage chart panel.");
		storageDataset = new TimeSeriesCollection();
		storageSeries = new TimeSeries("Storage");
		storageDataset.addSeries(storageSeries);
		storageChart = ChartFactory.createTimeSeriesChart(null, 
				"Time", "Stored Energy (W-hr)", storageDataset, 
				false, false, false);
		storageChart.setBackgroundPaint(getBackground());
		if(storageChart.getPlot() instanceof XYPlot) {
			XYPlot xyPlot = (XYPlot) storageChart.getPlot();
			XYItemRenderer renderer = new StandardXYItemRenderer(
					StandardXYItemRenderer.SHAPES_AND_LINES);
			renderer.setSeriesShape(0, new Ellipse2D.Double(-2,-2,4,4));
			xyPlot.setRenderer(renderer);
			xyPlot.setBackgroundPaint(Color.WHITE);
			xyPlot.setDomainGridlinePaint(Color.GRAY);
			xyPlot.setRangeGridlinePaint(Color.GRAY);
		}
		addTab("Storing", new ChartPanel(storageChart));

		logger.trace("Creating and adding energy transformation chart panel.");
		powerDataset = new TimeSeriesCollection();
		generationSeries = new TimeSeries("Generation");
		powerDataset.addSeries(generationSeries);
		consumptionSeries = new TimeSeries("Consumption");
		powerDataset.addSeries(consumptionSeries);
		powerChart = ChartFactory.createTimeSeriesChart(null, 
				"Time", "Power (W)", powerDataset, 
				true, false, false);
		powerChart.setBackgroundPaint(getBackground());
		if(powerChart.getPlot() instanceof XYPlot) {
			XYPlot xyPlot = (XYPlot) powerChart.getPlot();
			XYItemRenderer renderer = new StandardXYItemRenderer(
					StandardXYItemRenderer.SHAPES_AND_LINES);
			renderer.setSeriesShape(0, new Ellipse2D.Double(-2,-2,4,4));
			renderer.setSeriesPaint(0, Color.red);
			renderer.setSeriesShape(1, new Ellipse2D.Double(-2,-2,4,4));
			renderer.setSeriesPaint(1, Color.green);
			xyPlot.setRenderer(renderer);
			xyPlot.setBackgroundPaint(Color.WHITE);
			xyPlot.setDomainGridlinePaint(Color.GRAY);
			xyPlot.setRangeGridlinePaint(Color.GRAY);
		}
		JPanel chartPanel = new JPanel(new BorderLayout());
		chartPanel.add(new ChartPanel(powerChart), BorderLayout.CENTER);
		chartPanel.add(new JButton(exportAction), BorderLayout.SOUTH);
		addTab("Transforming", chartPanel);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.SimulationTimeListener#timeAdvanced(edu.mit.fss.event.SimulationTimeEvent)
	 */
	@Override
	public void timeAdvanced(final SimulationTimeEvent event) {
		// make a copy of state updates to prevent late-running threads from
		// posting out-of-date information
		final double storedEnergy = subsystem.getPowerStored();
		final double powerGeneration = subsystem.getPowerGeneration();
		final double powerConsumption = subsystem.getTotalPowerConsumption();

		// update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					storageSeries.addOrUpdate(RegularTimePeriod.createInstance(
							Minute.class, new Date(event.getTime()), 
							TimeZone.getTimeZone("UTC")), storedEnergy);
					generationSeries.addOrUpdate(RegularTimePeriod.createInstance(
							Minute.class, new Date(event.getTime()), 
							TimeZone.getTimeZone("UTC")), powerGeneration);
					consumptionSeries.addOrUpdate(RegularTimePeriod.createInstance(
							Minute.class, new Date(event.getTime()), 
							TimeZone.getTimeZone("UTC")), powerConsumption);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Export this panel's generation dataset.
	 */
	private void exportDataset() {
		if(JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
			File f = fileChooser.getSelectedFile();
			
			if(!f.exists() || JOptionPane.YES_OPTION == 
					JOptionPane.showConfirmDialog(this, 
							"Overwrite existing file " + f.getName() + "?",
							"File Exists", JOptionPane.YES_NO_OPTION)) {

				logger.info("Exporting dataset to file " + f.getPath() + ".");
				try {
					BufferedWriter bw = Files.newBufferedWriter(
							Paths.get(f.getPath()), Charset.defaultCharset());

					StringBuilder b = new StringBuilder();
					b.append(" Generation\n")
						.append("Time, Value\n");
					for(int j = 0; j < generationSeries.getItemCount(); j++) {
						b.append(generationSeries.getTimePeriod(j).getStart().getTime())
							.append(", ")
							.append(generationSeries.getValue(j))
							.append("\n");
					}
					
					bw.write(b.toString());
					bw.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}
}
