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

package org.geogurus.data.cache;

import org.geotools.data.DataStore;

import com.whirlycott.cache.Cacheable;

/**
 * Handles disposing of Datastores when they are removed from the cache (or the cache is destroyed)
 * 
 * @author jesse
 */
public class DataStoreCacheable extends AbstractCacheable implements Cacheable {

	private static final long serialVersionUID = -55397573286187300L;

	public DataStoreCacheable(String key) {
		super(key);
	}
	
	public void onRemove(Object _value) {
		((DataStore)_value).dispose();
	}

}
