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
package edu.mit.fss.examples.visual.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.Transform;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import edu.mit.fss.Element;
import edu.mit.fss.OrbitalElement;
import edu.mit.fss.ReferenceFrame;
import edu.mit.fss.Signal;
import edu.mit.fss.SurfaceElement;
import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

/**
 * A graphical user interface component containing a {@link WorldWindGLCanvas}
 * for Earth orbit visualization.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class WorldWindVisualization extends JPanel implements 
ObjectChangeListener, SimulationTimeListener {
	private static final long serialVersionUID = 4308386642558774583L;
	private static Logger logger = Logger.getLogger(
			WorldWindVisualization.class);

	/**
	 * Converts a NASA World Wind {@link Vec4} to an Apache Math Commons 
	 * {@link Vector3D}.
	 *
	 * @param vector the NASA World Wind vector
	 * @return the Apache Math Commons vector
	 */
	private static Vector3D convert(Vec4 vector) {
		return new Vector3D(vector.getX(), vector.getY(), vector.getZ());
	}

	/**
	 * Converts an Apache Math Commons {@link Vector3D} to a NASA World Wind
	 * {@link Vec4}.
	 *
	 * @param vector the Apache Math Commons vector
	 * @return the the NASA World Wind vector
	 */
	private static Vec4 convert(Vector3D vector) {
		return new Vec4(vector.getX(), vector.getY(), vector.getZ());
	}

	private AbsoluteDate date = new AbsoluteDate();
	private final Frame eme, itrf, wwj;
	private final WorldWindowGLCanvas wwd;
	private final RenderableLayer displayLayer;
	private final MarkerLayer markerLayer;
	private final Ellipsoid sunShape;
	private final CelestialBody sun = CelestialBodyFactory.getSun();
	private final SurfaceCircle terminatorShape;
	private final OptionsPanel optionsPanel = new OptionsPanel();
	private final Action editOptionsAction = new AbstractAction("Edit Options") {
		private static final long serialVersionUID = -3838501510234972868L;
		@Override
		public void actionPerformed(ActionEvent e) {
			showOptionsDialog();
		}
	};
	private final AtomicBoolean inertialFrame = new AtomicBoolean(false);
	private Vector3D rotationDatum = new Vector3D(0,0,0);

	/**
	 * Instantiates a new world wind visualization.
	 *
	 * @throws OrekitException the orekit exception
	 */
	public WorldWindVisualization() throws OrekitException { 
		logger.trace("Creating Orekit reference frames.");
		eme = ReferenceFrame.EME2000.getOrekitFrame();
		itrf = ReferenceFrame.ITRF2008.getOrekitFrame();
		// world wind frame is a fixed rotation from Earth inertial frame
		wwj = new Frame(itrf, new Transform(date, 
				new Rotation(RotationOrder.ZXZ, 0, -Math.PI/2, -Math.PI/2)), 
				"World Wind");

		logger.trace("Creating World Window GL canvas and adding to panel.");
		wwd = new WorldWindowGLCanvas();
		wwd.setModel(new BasicModel());
		wwd.setPreferredSize(new Dimension(800, 600));
		setLayout(new BorderLayout());
		add(wwd, BorderLayout.CENTER);

		logger.trace("Creating and adding a renderable layer.");
		displayLayer = new RenderableLayer();
		wwd.getModel().getLayers().add(displayLayer);

		logger.trace("Creating and adding a marker layer.");
		markerLayer = new MarkerLayer();
		// allow markers above/below surface
		markerLayer.setOverrideMarkerElevation(false);
		wwd.getModel().getLayers().add(markerLayer);

		logger.trace("Creating and adding a sun renderable.");
		Vector3D position = sun.getPVCoordinates(date, wwj).getPosition();
		sunShape = new Ellipsoid(wwd.getModel().getGlobe().computePositionFromPoint(
				convert(position)), 696000000., 696000000., 696000000.);
		ShapeAttributes sunAttributes = new BasicShapeAttributes();
		sunAttributes.setInteriorMaterial(Material.YELLOW);
		sunAttributes.setInteriorOpacity(1.0);
		sunShape.setAttributes(sunAttributes);
		displayLayer.addRenderable(sunShape);

		logger.trace("Creating and adding a terminator.");
		LatLon antiSun = LatLon.fromRadians(
				-sunShape.getCenterPosition().getLatitude().radians, 
				FastMath.PI + sunShape.getCenterPosition().getLongitude().radians);
		// set radius to a quarter Earth chord at the anti-sun position less
		// a small amount (100 m) to avoid graphics problems
		terminatorShape = new SurfaceCircle(antiSun,
				wwd.getModel().getGlobe().getRadiusAt(antiSun)*FastMath.PI/2 - 100);
		ShapeAttributes nightAttributes = new BasicShapeAttributes();
		nightAttributes.setInteriorMaterial(Material.BLACK);
		nightAttributes.setInteriorOpacity(0.5);
		terminatorShape.setAttributes(nightAttributes);
		displayLayer.addRenderable(terminatorShape);

		logger.trace("Creating and adding a panel for buttons.");
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(new JCheckBox(new AbstractAction("Inertial Frame") {
			private static final long serialVersionUID = 2287109397693524964L;
			@Override
			public void actionPerformed(ActionEvent e) {
				setInertialFrame(((JCheckBox)e.getSource()).isSelected());
			}
		}));
		buttonPanel.add(new JButton(editOptionsAction));
		add(buttonPanel, BorderLayout.SOUTH);

		logger.trace("Creating a timer to rotate the sun renderable, " +
				"terminator surface circle, and stars layer.");
		Timer rotationTimer = new Timer(15, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				wwd.redraw();
				try {
					BasicOrbitView wwdView;
					if(wwd.getView() instanceof BasicOrbitView) {
						wwdView = (BasicOrbitView) wwd.getView();
					} else {
						return;
					}

					// rotate camera to simulate inertial frame
					if(wwd.getView().isAnimating() || !inertialFrame.get()) {
						// update eme datum
						rotationDatum = wwj.getTransformTo(eme, date)
								.transformPosition(convert(
										wwdView.getCenterPoint()));
					} else if(inertialFrame.get()) {
						Position newCenter = wwd.getModel().getGlobe()
								.computePositionFromPoint(convert(
										eme.getTransformTo(wwj, date)
										.transformPosition(rotationDatum)));
						// move to eme datum
						wwdView.setCenterPosition(newCenter);
					} 

					// rotate stars layer
					for(Layer layer : wwd.getModel().getLayers()) {
						if(layer instanceof StarsLayer) {
							StarsLayer stars = (StarsLayer) layer;
							// find the EME coordinates of (0,0)
							Vector3D emeDatum = wwj.getTransformTo(eme, date)
									.transformPosition(convert(
											wwd.getModel().getGlobe()
											.computePointFromLocation(
													LatLon.fromDegrees(0, 0))));
							// find the WWJ coordinates the equivalent point in ITRF
							Vector3D wwjDatum = itrf.getTransformTo(wwj, date)
									.transformPosition(emeDatum);
							// set the longitude offset to the opposite of 
							// the difference in longitude (i.e. from 0)
							stars.setLongitudeOffset(wwd.getModel().getGlobe()
									.computePositionFromPoint(convert(wwjDatum))
									.getLongitude().multiply(-1));
						}
					}
				} catch (OrekitException ex) {
					logger.error(ex);
				}
			}
		});
		// set initial 2-second delay for initialization
		rotationTimer.setInitialDelay(2000); 
		rotationTimer.start();
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.ObjectChangeListener#interactionOccurred(edu.mit.fss.ObjectChangeEvent)
	 */
	@Override
	public void interactionOccurred(ObjectChangeEvent event) {
		if(event.getObject() instanceof Signal) {
			final Signal signal = (Signal) event.getObject();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						if(signal.getElement() instanceof OrbitalElement) {
							// trigger transmitting animation
							optionsPanel.elementTransmitting(
									(OrbitalElement) signal.getElement());
						} else if(signal.getElement() instanceof SurfaceElement) {
							// trigger transmitting animation
							optionsPanel.elementTransmitting(
									(SurfaceElement) signal.getElement());
						}
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.visualization.ElementChangeListener#elementChanged(edu.mit.fss.visualization.ElementChangeEvent)
	 */
	@Override
	public void objectChanged(ObjectChangeEvent event) {
		if(event.getObject() instanceof Element) {
			final Element element = (Element) event.getObject();

			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						if(element.getFrame() == ReferenceFrame.UNKNOWN) {
							// do not consider elements with missing frame
							return;
						}

						// update position and size for shapes
						if(element instanceof OrbitalElement) {
							final OrbitalElement orbital = (OrbitalElement) element;

							// determine cartesian position of element
							Vector3D cartPosition = null;
							try {
								Transform t = orbital.getFrame().getOrekitFrame()
										.getTransformTo(wwj, date);
								cartPosition = t.transformPosition(orbital.getPosition());
							} catch (OrekitException e) {
								logger.error(e);
							}

							// convert cartesian to geodetic position
							final Position geoPosition = wwd.getModel().getGlobe()
									.computePositionFromPoint(convert(cartPosition));

							// compute footprint radius using field-of-view angle
							double earthRadius = wwd.getModel().getGlobe().getRadiusAt(
									geoPosition.getLatitude(), 
									geoPosition.getLongitude());
							final double footRadius = FastMath.max(0, FastMath.min(earthRadius, 
									geoPosition.getElevation()*FastMath.tan(FastMath.toRadians(
											optionsPanel.getFieldOfView(orbital)/2))));

							optionsPanel.getOrbitalShape(orbital).setCenterPosition(
									geoPosition);
							optionsPanel.getFootprintShape(orbital).setCenter(
									new LatLon(geoPosition.getLatitude(), 
											geoPosition.getLongitude()));
							optionsPanel.getFootprintShape(orbital).setMajorRadius(
									footRadius);
							optionsPanel.getFootprintShape(orbital).setMinorRadius(
									footRadius);
						} else if(element instanceof SurfaceElement) {
							final SurfaceElement surface = (SurfaceElement)element;
							optionsPanel.getSurfaceMarker(surface).setPosition(
									Position.fromDegrees(surface.getLatitude(), 
											surface.getLongitude(), 
											surface.getAltitude()));
						}
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.visualization.ElementChangeListener#elementDiscovered(edu.mit.fss.visualization.ElementChangeEvent)
	 */
	@Override
	public void objectDiscovered(ObjectChangeEvent event) {
		if(event.getObject() instanceof Element) {
			final Element element = (Element) event.getObject();

			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						if(element instanceof OrbitalElement) {
							// add element to options panel
							OrbitalElement orbital = (OrbitalElement) element;
							optionsPanel.addElement(orbital);
							// add orbital shape to display layer
							displayLayer.addRenderable(
									optionsPanel.getOrbitalShape(orbital));
							// add footprint shape to display layer
							displayLayer.addRenderable(
									optionsPanel.getFootprintShape(orbital));
						} else if(element instanceof SurfaceElement) {
							// add element to options panel
							optionsPanel.addElement((SurfaceElement) element);
							// update markers layer (cannot add individual markers)
							markerLayer.setMarkers(
									optionsPanel.getSurfaceMarkers());
						}
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.visualization.ElementChangeListener#elementRemoved(edu.mit.fss.visualization.ElementChangeEvent)
	 */
	@Override
	public void objectRemoved(ObjectChangeEvent event) {
		if(event.getObject() instanceof Element) {
			final Element element = (Element) event.getObject();

			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						if(element instanceof OrbitalElement) {
							OrbitalElement orbital = (OrbitalElement) element;
							// remove orbital shape from display layer
							displayLayer.removeRenderable(
									optionsPanel.getOrbitalShape(orbital));
							// remove footprint shape from display layer
							displayLayer.removeRenderable(
									optionsPanel.getFootprintShape(orbital));
							optionsPanel.removeElement(orbital);
						} else if(element instanceof SurfaceElement) {
							optionsPanel.removeElement((SurfaceElement) element);
							// update markers layer (cannot remove individual markers)
							markerLayer.setMarkers(
									optionsPanel.getSurfaceMarkers());
						}
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * Sets this visualization to simulate an inertial frame.
	 *
	 * @param inertialFrame true, to simulate an inertial frame
	 */
	private void setInertialFrame(boolean inertialFrame) {
		this.inertialFrame.set(inertialFrame);
	}

	/**
	 * Shows this visualization's options dialog.
	 */
	private void showOptionsDialog() {
		JOptionPane.showMessageDialog(this, optionsPanel,
				"Edit Options", JOptionPane.PLAIN_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.gui.SimulationTimeListener#timeAdvanced(edu.mit.fss.gui.SimulationTimeEvent)
	 */
	@Override
	public void timeAdvanced(SimulationTimeEvent event) {
		try {
			// update the absolute date
			date = new AbsoluteDate(new Date(event.getTime()), 
					TimeScalesFactory.getUTC());

			// compute the new sun position
			Vector3D position = sun.getPVCoordinates(date, wwj).getPosition();
			sunShape.setCenterPosition(wwd.getModel().getGlobe()
					.computePositionFromPoint(convert(position)));

			// compute the new terminator position
			LatLon antiSun = LatLon.fromRadians(
					-sunShape.getCenterPosition().getLatitude().radians, 
					sunShape.getCenterPosition().getLongitude().radians
					+ FastMath.PI);
			terminatorShape.setCenter(antiSun);
			// set radius to a quarter Earth chord at the anti-sun position less
			// a small amount (100 m) to avoid graphics problems
			terminatorShape.setRadius(Math.PI/2 * 
					wwd.getModel().getGlobe().getRadiusAt(antiSun) - 100);
		} catch (OrekitException e) {
			logger.error(e);
		};
	}
}
