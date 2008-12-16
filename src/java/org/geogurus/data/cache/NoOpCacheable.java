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

import com.whirlycott.cache.Cacheable;

/**
 * Does nothing on remove because object does not need it.
 * @author jesse
 */
public class NoOpCacheable extends AbstractCacheable implements Cacheable {

	private static final long serialVersionUID = 6587022722537186303L;

	public NoOpCacheable(String internalKey) {
		super(internalKey);
	}

	public void onRemove(Object _value) {
		// do nothing
	}

}
