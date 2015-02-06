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
package edu.mit.fss.tutorial.part1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import edu.mit.fss.SimObject;

/**
 * A simulation object which keeps track of time.
 * 
 * @author Paul T. Grogan, ptgrogan@mit.edu
 * @version 0.2.0
 * @since 0.1.0
 */
public class TrivialClock implements SimObject {
	private String name;
	private long time, nextTime;
	
	/**
	 * Instantiates a new trivial clock.
	 *
	 * @param name the name
	 */
	public TrivialClock(String name) {
		this.name = name;
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public Date getDate() {
		return new Date(time);
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#getNestedObjects()
	 */
	@Override
	public Collection<? extends SimObject> getNestedObjects() {
		return new ArrayList<SimObject>();
	}
	
	/**
	 * Gets this clock's time.
	 *
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#initialize(long)
	 */
	@Override
	public void initialize(long time) {
		this.time = time;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tick(long)
	 */
	@Override
	public void tick(long duration) {
		// Store the next time to be updated on tock().
		nextTime = time + duration;
	}

	/* (non-Javadoc)
	 * @see edu.mit.fss.SimObject#tock()
	 */
	@Override
	public void tock() {
		// Update the time variable.
		time = nextTime;
	}
}
