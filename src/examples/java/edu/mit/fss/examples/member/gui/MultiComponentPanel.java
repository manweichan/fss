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

import java.awt.Component;
import java.util.Collection;

import javax.swing.JTabbedPane;

import edu.mit.fss.event.ObjectChangeEvent;
import edu.mit.fss.event.ObjectChangeListener;
import edu.mit.fss.event.SimulationTimeEvent;
import edu.mit.fss.event.SimulationTimeListener;

/**
 * An extension of the {@link JTabbedPane} class to display multiple
 * components in tabs. Adds each component as a {@link SimulationTimeListener}
 * and a {@link ObjectChangeListener} if possible to notify of relevant events.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class MultiComponentPanel extends JTabbedPane implements
		SimulationTimeListener, ObjectChangeListener {
	private static final long serialVersionUID = -7970867146667484278L;
	
	/**
	 * Instantiates a new multi component panel.
	 *
	 * @param components the components
	 */
	public MultiComponentPanel(Collection<? extends Component> components) {
		for(Component component : components) {
			addTab(component.getName(), component);
			if(component instanceof SimulationTimeListener) {
				listenerList.add(SimulationTimeListener.class, 
						(SimulationTimeListener) component);
			}
			if(component instanceof ObjectChangeListener) {
				listenerList.add(ObjectChangeListener.class, 
						(ObjectChangeListener) component);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.fss.event.SimulationTimeListener#timeAdvanced(edu.mit.fss.event.SimulationTimeEvent)
	 */
	@Override
	public void timeAdvanced(SimulationTimeEvent event) {
		for(SimulationTimeListener l : listenerList.getListeners(
				SimulationTimeListener.class)) {
			l.timeAdvanced(event);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectDiscovered(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectDiscovered(ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectDiscovered(event);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectRemoved(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectRemoved(ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectRemoved(event);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.event.ObjectChangeListener#objectChanged(edu.mit.fss.event.ObjectChangeEvent)
	 */
	@Override
	public void objectChanged(ObjectChangeEvent event) {
		for(ObjectChangeListener l : listenerList.getListeners(
				ObjectChangeListener.class)) {
			l.objectChanged(event);
		}
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
}
