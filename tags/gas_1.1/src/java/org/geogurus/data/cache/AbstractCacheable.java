/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package org.geogurus.data.cache;

import java.io.Serializable;

import com.whirlycott.cache.Cacheable;

/**
 * A {@link Cacheable} that is also serializable and only requires the implementation
 * of {@link #onRemove(Object)}. 
 * 
 * @author jesse
 */
public abstract class AbstractCacheable implements Cacheable, Serializable {

	private static final long serialVersionUID = 3969928729557248778L;

	private final String internalKey;
	
	public AbstractCacheable(String internalKey) {
		this.internalKey = internalKey;
	}

	/**
	 * does nothing
	 */
	public void onRetrieve(Object _value) {
		// does nothing
	}
	
	/**
	 * does nothing
	 */
	public void onStore(Object _value) {
		// does nothing
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((internalKey == null) ? 0 : internalKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractCacheable other = (AbstractCacheable) obj;
		if (internalKey == null) {
			if (other.internalKey != null)
				return false;
		} else if (!internalKey.equals(other.internalKey))
			return false;
		return true;
	}

	
}
