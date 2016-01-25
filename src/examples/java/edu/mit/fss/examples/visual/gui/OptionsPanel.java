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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import edu.mit.fss.OrbitalElement;
import edu.mit.fss.SurfaceElement;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

/**
 * A graphical user interface component to set options in 
 * the {@link WorldWindVisualization}. This class also stores the mutable
 * World Wind objects to display in the {@link WorldWindVisualization} 
 * component.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.1
 * @since 0.1.0
 */
public class OptionsPanel extends JSplitPane {
	private static final long serialVersionUID = 8080077334633108138L;

	private final ShapeAttributes defaultShapeAttributes;
	private final ShapeAttributes defaultTxShapeAttributes;
	private final MarkerAttributes defaultMarkerAttributes;
	private final MarkerAttributes defaultTxMarkerAttributes;
	
	// orbital element shapes
	private final List<OrbitalElement> orbitalElements = 
			new ArrayList<OrbitalElement>();
	private final List<Ellipsoid> orbitalShapes = 
			new ArrayList<Ellipsoid>();
	private final List<Double> fieldOfView = new ArrayList<Double>();
	private final List<SurfaceCircle> footprintShapes = 
			new ArrayList<SurfaceCircle>();
	private final List<ShapeAttributes> txAttributes = 
			new ArrayList<ShapeAttributes>();
	
	// surface element markers
	private final List<SurfaceElement> surfaceElements = 
			new ArrayList<SurfaceElement>();
	private final List<Marker> surfaceMarkers = new ArrayList<Marker>();
	private final List<MarkerAttributes> txMarkerAttributes = 
			new ArrayList<MarkerAttributes>();

	private final List<Material> materials = Arrays.asList(
			Material.BLACK, Material.BLUE, Material.CYAN, 
			Material.DARK_GRAY, Material.GRAY, Material.GREEN,
			Material.LIGHT_GRAY, Material.MAGENTA, Material.ORANGE,
			Material.PINK, Material.RED, Material.WHITE, Material.YELLOW);
	private final List<String> materialNames = Arrays.asList(
			"Black", "Blue", "Cyan", "Dark Gray", 
			"Gray", "Green", "Light Gray", "Magenta", 
			"Orange", "Pink", "Red", "White", "Yellow");
	
	// custom list cell renderer to display the name of the material
	private final ListCellRenderer<? super Material> materialListCellRenderer = 
			new DefaultListCellRenderer() {
				private static final long serialVersionUID = 3641765337246417706L;
		
				@Override
				public Component getListCellRendererComponent(
						JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					JLabel label = (JLabel) super.getListCellRendererComponent(
							list, value, index, isSelected, cellHasFocus);
					if(materials.contains(value)) {
						label.setText(materialNames.get(materials.indexOf(value)));
					}
					return label;
				}
		    };
    
	// custom table cell renderer to display the name of the material
    private final TableCellRenderer materialTableCellRenderer =
    		new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 3752846327305199916L;
		
				@Override
				public Component getTableCellRendererComponent(JTable table,
						Object value, boolean isSelected, boolean hasFocus, 
						int row, int column) {
					JLabel label = (JLabel) super.getTableCellRendererComponent(
							table, value, isSelected, hasFocus, row, column);
					if(materials.contains(value)) {
						label.setText(materialNames.get(materials.indexOf(value)));
					}
					return label;
				}
			};
	
	// custom table model to handle editing orbital element properties
	private final AbstractTableModel orbitalElementTableModel = 
			new AbstractTableModel() {
		private static final long serialVersionUID = 3438895243365315052L;

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return String.class;
			case 1: // visibility
				return Boolean.class;
			case 2: // color
				return Material.class;
			case 3: // opacity
				return Double.class;
			case 4: // footprint visibility
				return Boolean.class;
			case 5: // field of view angle
				return Double.class;
			case 6: // footprint color
				return Material.class;
			case 7: // footprint opacity
				return Double.class;
			case 8: // transmitting color
				return Material.class;
			case 9: // transmitting opacity
				return Double.class;
			default:
				return null;
			}
		}

		@Override
		public int getColumnCount() {
			return 10;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return "Orbital Element";
			case 1: // visibility
				return "Visible";
			case 2: // color
				return "Color";
			case 3: // opacity
				return "Opacity";
			case 4: // footprint visibility
				return "Footprint Visible";
			case 5: // field of view angle
				return "Field of View (\u00b0)";
			case 6: // footprint color
				return "Footprint Color";
			case 7: // footprint opacity
				return "Footprint Opacity";
			case 8: // transmitting color
				return "Transmitting Color";
			case 9: // transmitting opacity
				return "Transmitting Opacity";
			default:
				return null;
			}
		}

		@Override
		public int getRowCount() {
			return orbitalElements.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return orbitalElements.get(rowIndex).getName();
			case 1: // visibility
				return orbitalShapes.get(rowIndex).isVisible();
			case 2: // color
				return orbitalShapes.get(rowIndex)
						.getAttributes().getInteriorMaterial(); 
			case 3: // opacity
				return orbitalShapes.get(rowIndex)
						.getAttributes().getInteriorOpacity();
			case 4: // footprint visibility
				return footprintShapes.get(rowIndex).isVisible();
			case 5: // field of view angle
				return fieldOfView.get(rowIndex);
			case 6: // footprint color
				return footprintShapes.get(rowIndex)
						.getAttributes().getInteriorMaterial();
			case 7: // footprint opacity
				return footprintShapes.get(rowIndex)
						.getAttributes().getInteriorOpacity();
			case 8: // transmitting color
				return txAttributes.get(rowIndex)
						.getInteriorMaterial();
			case 9: // transmitting opacity
				return txAttributes.get(rowIndex)
						.getInteriorOpacity();
			default:
				return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return false;
			case 1: // visibility
				return true;
			case 2: // color
				return true;
			case 3: // opacity
				return true;
			case 4: // footprint visibility
				return true;
			case 5: // field of view angle
				return true;
			case 6: // footprint color
				return true;
			case 7: // footprint opacity
				return true;
			case 8: // transmitting color
				return true;
			case 9: // transmitting opacity
				return true;
			default:
				return false;
			}
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				break;
			case 1: // visibility
				if(value instanceof Boolean) {
					orbitalShapes.get(rowIndex).setVisible((Boolean) value);
				}
				// also set footprint visibility to be the same
				setValueAt(value, rowIndex, 4);
				fireTableRowsUpdated(rowIndex, rowIndex);
				break;
			case 2: // color
				if(value instanceof Material) {
					orbitalShapes.get(rowIndex)
					.getAttributes().setInteriorMaterial((Material) value);
				}
				// also set footprint color to be the same
				setValueAt(value, rowIndex, 6);
				fireTableRowsUpdated(rowIndex, rowIndex);
				break;
			case 3: // opacity
				if(value instanceof Double) {
					orbitalShapes.get(rowIndex)
					.getAttributes().setInteriorOpacity((Double) value);
				}
				break;
			case 4: // footprint visibility
				if(value instanceof Boolean) {
					footprintShapes.get(rowIndex).setVisible((Boolean) value);
				}
				break;
			case 5: // field of view angle
				if(value instanceof Double) {
					fieldOfView.set(rowIndex, (Double) value);
				}
				break;
			case 6: // footprint color
				if(value instanceof Material) {
					footprintShapes.get(rowIndex)
					.getAttributes().setInteriorMaterial((Material) value);
				}
				break;
			case 7: // footprint opacity
				if(value instanceof Double) {
					footprintShapes.get(rowIndex)
					.getAttributes().setInteriorOpacity((Double) value);
				}
				break;
			case 8: // transmitting color
				if(value instanceof Material) {
					txAttributes.get(rowIndex)
					.setInteriorMaterial((Material) value);
				}
				break;
			case 9: // transmitting opacity
				if(value instanceof Double) {
					txAttributes.get(rowIndex)
					.setInteriorOpacity((Double) value);
				}
				break;
			}
		}
	};
	
	// custom table model to handle editing surface element properties
	private final AbstractTableModel surfaceElementTableModel = 
			new AbstractTableModel() {
		private static final long serialVersionUID = 3438895243365315052L;

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return String.class;
			case 1: // color
				return Material.class;
			case 2: // opacity
				return Double.class;
			case 3: // transmitting color
				return Material.class;
			case 4: // transmitting opacity
				return Double.class;
			default:
				return null;
			}
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return "Surface Element";
			case 1: // color
				return "Color";
			case 2: // opacity
				return "Opacity";
			case 3: // transmitting color
				return "Transmitting Color";
			case 4: // transmitting opacity
				return "Transmitting Opacity";
			default:
				return null;
			}
		}

		@Override
		public int getRowCount() {
			return surfaceElements.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return surfaceElements.get(rowIndex).getName();
			case 1: // color
				return surfaceMarkers.get(rowIndex)
						.getAttributes().getMaterial(); 
			case 2: // opacity
				return surfaceMarkers.get(rowIndex)
						.getAttributes().getOpacity();
			case 3: // transmitting color
				return txMarkerAttributes
						.get(rowIndex).getMaterial();
			case 4: // transmitting opacity
				return txMarkerAttributes
						.get(rowIndex).getOpacity();
			default:
				return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				return false;
			case 1: // color
				return true;
			case 2: // opacity
				return true;
			case 3: // transmitting color
				return true;
			case 4: // transmitting opacity
				return true;
			default:
				return false;
			}
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0: // element name
				break;
			case 1: // color
				if(value instanceof Material) {
					surfaceMarkers.get(rowIndex).getAttributes()
					.setMaterial((Material) value);
				}
				break;
			case 2: // opacity
				if(value instanceof Double) {
					surfaceMarkers.get(rowIndex).getAttributes()
					.setOpacity((Double) value);
				}
				break;
			case 3: // transmitting color
				if(value instanceof Boolean) {
					txMarkerAttributes.get(rowIndex)
					.setMaterial((Material) value);
				}
				break;
			case 4: // transmitting opacity
				if(value instanceof Double) {
					txMarkerAttributes.get(rowIndex)
					.setOpacity((Double) value);
				}
				break;
			}
		}
	};

	
	/**
	 * Instantiates a new options panel.
	 */
	public OptionsPanel() {
		super(VERTICAL_SPLIT);
		setDividerSize(0);
		
		// define default shape attributes
        defaultShapeAttributes = new BasicShapeAttributes();
        defaultShapeAttributes.setInteriorMaterial(Material.YELLOW);
        defaultShapeAttributes.setInteriorOpacity(0.7);
        defaultShapeAttributes.setEnableLighting(true);
        defaultShapeAttributes.setDrawInterior(true);
        defaultShapeAttributes.setDrawOutline(false);
        // define default transmitting shape attributes
        defaultTxShapeAttributes = new BasicShapeAttributes();
        defaultTxShapeAttributes.setInteriorMaterial(Material.GREEN);
        defaultTxShapeAttributes.setInteriorOpacity(0.7);
        defaultTxShapeAttributes.setEnableLighting(true);
        defaultTxShapeAttributes.setDrawInterior(true);
        defaultTxShapeAttributes.setDrawOutline(false);
		
        // create a combo to select materials (for use in tables)
        JComboBox<Material> materialCombo = new JComboBox<Material>(
        		materials.toArray(new Material[0]));
        materialCombo.setRenderer(materialListCellRenderer);
        
        // create and add the table of orbital elements
		JTable orbitalElementTable = new JTable(orbitalElementTableModel);
		orbitalElementTable.setDefaultEditor(Material.class, 
				new DefaultCellEditor(materialCombo));
		orbitalElementTable.setDefaultRenderer(Material.class, 
				materialTableCellRenderer);
		JScrollPane orbitalScroll = new JScrollPane(orbitalElementTable);
		orbitalScroll.setPreferredSize(new Dimension(800,150));
		setTopComponent(orbitalScroll);
		
		// define default marker attributes
        defaultMarkerAttributes = new BasicMarkerAttributes(
        		Material.RED, BasicMarkerShape.CYLINDER, 0.7);
        // define default transmitting marker attributes
        defaultTxMarkerAttributes = new BasicMarkerAttributes(
        		Material.GREEN, BasicMarkerShape.CYLINDER, 0.7);
		
        // create and add the table of surface elements
		JTable surfaceElementTable = new JTable(surfaceElementTableModel);
		surfaceElementTable.setDefaultEditor(Material.class, 
				new DefaultCellEditor(materialCombo));
		surfaceElementTable.setDefaultRenderer(Material.class, 
				materialTableCellRenderer);
		JScrollPane surfaceScroll = new JScrollPane(surfaceElementTable);
		surfaceScroll.setPreferredSize(new Dimension(800,100));
		setBottomComponent(surfaceScroll);
	}
	
	/**
	 * Adds an orbital element to this options panel.
	 *
	 * @param element the orbital element
	 */
	public void addElement(OrbitalElement element) {
		orbitalElements.add(element);
		Ellipsoid orbitalShape = new Ellipsoid(
				Position.fromDegrees(0, 0), 100000, 100000, 100000);
		orbitalShape.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
		orbitalShape.setAttributes(defaultShapeAttributes.copy());
		orbitalShape.setVisible(true);
		orbitalShapes.add(orbitalShape);
		SurfaceCircle footprintShape = new SurfaceCircle(
				Position.fromDegrees(0, 0), 100000);
		footprintShape.setAttributes(defaultShapeAttributes.copy());
		footprintShape.getAttributes().setInteriorOpacity(0.25);
		footprintShape.setVisible(true);
		fieldOfView.add(60.0);
		footprintShapes.add(footprintShape);
		txAttributes.add(defaultTxShapeAttributes.copy());
		orbitalElementTableModel.fireTableRowsInserted(
				orbitalElements.indexOf(element), 
				orbitalElements.indexOf(element));
	}
	
	/**
	 * Adds a surface element to this options panel.
	 *
	 * @param element the surface element
	 */
	public void addElement(SurfaceElement element) {
		surfaceElements.add(element);
		Marker surfaceMarker = new BasicMarker(Position.fromDegrees(0, 0), 
				new BasicMarkerAttributes(defaultMarkerAttributes.getMaterial(),
						defaultMarkerAttributes.getShapeType(), 
						defaultMarkerAttributes.getOpacity()));
		surfaceMarkers.add(surfaceMarker);
		txMarkerAttributes.add(new BasicMarkerAttributes(
				defaultTxMarkerAttributes.getMaterial(),
				defaultTxMarkerAttributes.getShapeType(), 
				defaultTxMarkerAttributes.getOpacity()));
		surfaceElementTableModel.fireTableRowsInserted(
				surfaceElements.indexOf(element), 
				surfaceElements.indexOf(element));
	}
	
	/**
	 * Triggers the transmitting color change animation for an orbital element.
	 *
	 * @param element the orbital element
	 */
	public void elementTransmitting(final OrbitalElement element) {
		int index = orbitalElements.indexOf(element);
		if(index >= 0) {
			final ShapeAttributes prevOrbitalAttributes = 
					orbitalShapes.get(index).getAttributes();
			orbitalShapes.get(index).setAttributes(
					txAttributes.get(index));
			final ShapeAttributes prevFootprintAttributes = 
					footprintShapes.get(index).getAttributes();
			footprintShapes.get(index).setAttributes(
					txAttributes.get(index));
			Timer t = new Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					int index = orbitalElements.indexOf(element);
					if(index >= 0) {
						orbitalShapes.get(index).setAttributes(
								prevOrbitalAttributes);
						footprintShapes.get(index).setAttributes(
								prevFootprintAttributes);
					}
				}
			});
			t.setRepeats(false);
			t.start();
		}
	}
	
	/**
	 * Triggers the transmitting color change animation for a surface element.
	 *
	 * @param element the surface element
	 */
	public void elementTransmitting(final SurfaceElement element) {
		int index = surfaceElements.indexOf(element);
		if(index >= 0) {
			final MarkerAttributes prevMarkerAttributes = 
					surfaceMarkers.get(index).getAttributes();
			surfaceMarkers.get(index).setAttributes(
					txMarkerAttributes.get(index));
			Timer t = new Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					int index = orbitalElements.indexOf(element);
					if(index >= 0) {
						surfaceMarkers.get(index).setAttributes(
								prevMarkerAttributes);
					}
				}
			});
			t.setRepeats(false);
			t.start();
		}
	}

	/**
	 * Gets the field of view angle for an orbital element's footprint.
	 *
	 * @param element the orbital element
	 * @return the field of view angle
	 */
	public double getFieldOfView(OrbitalElement element) {
		int index = orbitalElements.indexOf(element);
		if(index >= 0 && index < fieldOfView.size()) {
			return fieldOfView.get(index);
		} else {
			return 0;
		}
	}
	
	/**
	 * Gets this panel's shape for an orbital element's footprint.
	 *
	 * @param element the orbital element
	 * @return the footprint shape
	 */
	public SurfaceCircle getFootprintShape(OrbitalElement element) {
		int index = orbitalElements.indexOf(element);
		if(index >= 0) {
			return footprintShapes.get(index);
		}
		return null;
	}
	
	/**
	 * Gets this panel's shape for an orbital element.
	 *
	 * @param element the element
	 * @return the orbital shape
	 */
	public Ellipsoid getOrbitalShape(OrbitalElement element) {
		int index = orbitalElements.indexOf(element);
		if(index >= 0) {
			return orbitalShapes.get(index);
		}
		return null;
	}
	
	/**
	 * Gets this panel's marker for a surface element.
	 *
	 * @param element the surface element
	 * @return the surface marker
	 */
	public Marker getSurfaceMarker(SurfaceElement element) {
		int index = surfaceElements.indexOf(element);
		if(index >= 0) {
			return surfaceMarkers.get(index);
		}
		return null;
	}
	
	/**
	 * Gets the surface markers.
	 *
	 * @return the surface markers
	 */
	public Set<Marker> getSurfaceMarkers() {
		return new HashSet<Marker>(surfaceMarkers);
	}
	
	/**
	 * Gets this panel's transmitting attributes for an orbital element.
	 *
	 * @param element the orbital element
	 * @return the transmitting attributes
	 */
	public ShapeAttributes getTransmittingAttributes(OrbitalElement element) {
		int index = orbitalElements.indexOf(element);
		if(index >= 0 && index < txAttributes.size()) {
			return txAttributes.get(index);
		} else {
			return null;
		}
	}
	
	/**
	 * Gets this panel's transmitting attributes for a surface element.
	 *
	 * @param element the surface element
	 * @return the transmitting attributes
	 */
	public MarkerAttributes getTransmittingAttributes(SurfaceElement element) {
		int index = surfaceElements.indexOf(element);
		if(index >= 0 && index < txMarkerAttributes.size()) {
			return txMarkerAttributes.get(index);
		} else {
			return null;
		}
	}
	
	/**
	 * Removes an orbital element from this options panel.
	 *
	 * @param element the orbital element
	 */
	public void removeElement(OrbitalElement element) {
		int index = orbitalElements.indexOf(element);
		if(index >= 0) {
			orbitalElements.remove(index);
			orbitalShapes.remove(index);
			footprintShapes.remove(index);
			fieldOfView.remove(index);
			txAttributes.remove(index);
			orbitalElementTableModel.fireTableRowsDeleted(index, index);
		}
	}
	
	/**
	 * Removes a surface element from this options panel.
	 *
	 * @param element the surface element
	 */
	public void removeElement(SurfaceElement element) {
		int index = surfaceElements.indexOf(element);
		if(index >= 0) {
			surfaceElements.remove(index);
			surfaceMarkers.remove(index);
			surfaceElementTableModel.fireTableRowsDeleted(index, index);
		}
	}
}
