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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

import edu.mit.fss.Federate;
import edu.mit.fss.Transmitter;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import edu.mit.fss.examples.member.CommSubsystem;

/**
 * A graphical user interface component for a {@link CommSubsystem} object.
 * Composes a {@link ReceiverPanel} and {@link TransmitterPanel} in separate
 * tabs and a plot of connectivity.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class CommSubsystemPanel extends JTabbedPane implements
		SimulationTimeListener, ObjectChangeListener {
	private static Logger logger = Logger.getLogger(CommSubsystemPanel.class);
	private static final long serialVersionUID = -6333181963327977328L;
	
	private final CommSubsystem subsystem;
	private final ReceiverPanel receiverPanel;
	private final TransmitterPanel transmitterPanel;
	private final JFreeChart connectivityChart;
	private final TimeSeriesCollection connectDataset = 
			new TimeSeriesCollection();
	// use synchronized map for thread safety
	private final Map<Transmitter, TimeSeries> connectSeriesMap = 
			Collections.synchronizedMap(
					new HashMap<Transmitter, TimeSeries>());
	private final JFileChooser fileChooser = new JFileChooser();
	private final Action exportAction = new AbstractAction("Export") {
		private static final long serialVersionUID = -7171676288524836282L;

		public void actionPerformed(ActionEvent e) {
			exportDataset();
		}
	};
	
	/**
	 * Instantiates a new communications subsystem panel for a subsystem. 
	 * Signals are sent via the associated {@link federate}.
	 *
	 * @param federate the federate
	 * @param subsystem the subsystem
	 */
	public CommSubsystemPanel(Federate federate, CommSubsystem subsystem) {
		this.subsystem = subsystem;
		
		logger.trace("Creating and adding receiver panel.");
		receiverPanel = new ReceiverPanel(subsystem.getReceiver());
		logger.trace("Adding receiver panel as an object listener.");
		listenerList.add(ObjectChangeListener.class, receiverPanel);
		addTab("Receiver", receiverPanel);

		logger.trace("Creating and adding transmitter panel.");
		transmitterPanel = new TransmitterPanel(federate, 
				subsystem.getTransmitter());
		logger.trace("Adding transmitter panel as an object listener.");
		listenerList.add(ObjectChangeListener.class, transmitterPanel);
		addTab("Transmitter", transmitterPanel);

		logger.trace("Creating and adding connectivity chart panel.");
		connectivityChart = ChartFactory.createTimeSeriesChart(null, 
				"Time", "Connectivity", connectDataset, 
				true, false, false);
		connectivityChart.setBackgroundPaint(getBackground());
		if(connectivityChart.getPlot() instanceof XYPlot) {
			XYPlot xyPlot = (XYPlot) connectivityChart.getPlot();
			XYItemRenderer renderer = new StandardXYItemRenderer(
					StandardXYItemRenderer.SHAPES_AND_LINES);
			renderer.setSeriesShape(0, new Ellipse2D.Double(-2,-2,4,4));
			xyPlot.setRenderer(renderer);
			xyPlot.setBackgroundPaint(Color.WHITE);
			xyPlot.setDomainGridlinePaint(Color.GRAY);
			xyPlot.setRangeGridlinePaint(Color.GRAY);
		}
		JPanel chartPanel = new JPanel(new BorderLayout());
		chartPanel.add(new ChartPanel(connectivityChart), BorderLayout.CENTER);
		chartPanel.add(new JButton(exportAction), BorderLayout.SOUTH);
		addTab("Connectivity", chartPanel);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#interactionOccurred(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void interactionOccurred(ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.interactionOccurred(event);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectChanged(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectChanged(final ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectChanged(event);
		}
		// update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if(connectSeriesMap.containsKey(event.getObject())) {
						// update transmitter series key because transmitter
						// name may have changed
						Transmitter tx = (Transmitter) event.getObject();
						logger.trace("Updating series key for transmitter " 
								+ tx.getName() + ".");
						TimeSeries series = connectSeriesMap.get(
								event.getObject());
						series.setKey(tx.getName());
					} else if(event.getObject() != subsystem.getTransmitter() 
							&& event.getObject() instanceof Transmitter
							&& !connectSeriesMap.containsKey(event.getObject())) {
						// add new series for transmitter
						Transmitter tx = (Transmitter) event.getObject();
						logger.trace("Adding series for transmitter " 
								+ tx.getName() + ".");
						TimeSeries series = new TimeSeries(tx.getName());
						connectSeriesMap.put(tx, series);
						connectDataset.addSeries(series);
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
	public void objectDiscovered(final ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectDiscovered(event);
		}
		// update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if(event.getObject() != subsystem.getTransmitter() 
							&& event.getObject() instanceof Transmitter
							&& !connectSeriesMap.containsKey(event.getObject())) {
						// add new series for transmitter
						Transmitter tx = (Transmitter) event.getObject();
						logger.trace("Adding series for transmitter " 
								+ tx.getName() + " .");
						TimeSeries series = new TimeSeries(tx.getName());
						connectSeriesMap.put(tx, series);
						connectDataset.addSeries(series);
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
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectRemoved(event);
		}
		// update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if(connectSeriesMap.containsKey(event.getObject())) {
						TimeSeries series = connectSeriesMap.remove(
								event.getObject());
						connectDataset.removeSeries(series);
						logger.trace("Removing series " + series.getKey() + ".");
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.SimulationTimeListener#timeAdvanced(edu.mit.fss.event.SimulationTimeEvent)
	 */
	@Override
	public void timeAdvanced(final SimulationTimeEvent event) {
		// make a copy of state updates to prevent late-running threads from
		// posting out-of-date information
		final Map<Transmitter,Boolean> canReceiveMap = 
				new HashMap<Transmitter,Boolean>();

		// synchronize on map for thread safety
		synchronized(connectSeriesMap) {
			for(Transmitter transmitter : connectSeriesMap.keySet()) {
				canReceiveMap.put(transmitter, 
						subsystem.getReceiver().canReceiveFrom(transmitter));
			}
		}
		
		// update in event dispatch thread for thread safety
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					for(Transmitter transmitter : canReceiveMap.keySet()) {
						TimeSeries series = connectSeriesMap.get(transmitter);
						logger.trace("Adding/updating series " 
								+ series.getKey() + ".");
						series.addOrUpdate(RegularTimePeriod.createInstance(
							Minute.class, new Date(event.getTime()), 
							TimeZone.getTimeZone("UTC")), 
							canReceiveMap.get(transmitter)?1:0);
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Exports this panel's connectivity dataset.
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

					// synchronize on map for thread safety
					synchronized(connectSeriesMap) {
						for(Transmitter tx : connectSeriesMap.keySet()) {
							TimeSeries s = connectSeriesMap.get(tx);
							b.append(tx.getName())
								.append(" Connectivity\n")
								.append("Time, Value\n");
							for(int j = 0; j < s.getItemCount(); j++) {
								b.append(s.getTimePeriod(j).getStart().getTime())
									.append(", ")
									.append(s.getValue(j))
									.append("\n");
							}
						}
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
