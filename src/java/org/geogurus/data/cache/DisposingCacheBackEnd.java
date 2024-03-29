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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geogurus.data.Pair;

import com.whirlycott.cache.Cacheable;
import com.whirlycott.cache.Item;
import com.whirlycott.cache.impl.AbstractMapBackedCache;

/**
 * This is the backend of the Cache.  On dispose or clear it
 * goes through each element in the cache and if the key is a 
 * Cacheable then calls on Remove on the Cacheable so that the object
 * can be disposed. 
 * 
 * @author jesse
 */
public class DisposingCacheBackEnd extends AbstractMapBackedCache {

	public DisposingCacheBackEnd() {
		c = new ConcurrentHashMap<Object, Object>();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				DisposingCacheBackEnd.this.destroy();
			}
		});
	}

	@Override
	public void setMostlyRead(boolean read) {
		return;
	}

	@Override
	public Object remove(Object key) {
		Object value = super.remove(key);
		notifyRemove(key, value);
		return value;
	}

	private void notifyRemove(Object key, Object value) {
		if (key instanceof Cacheable) {
			try {
				if( value instanceof Item ){
					((Cacheable) key).onRemove(((Item) value).getItem());
				}else {
				((Cacheable) key).onRemove(value);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.global.log(Level.WARNING,
						"Unable to dispose object: " + value
								+ " with key: " + key+": "+e.getMessage(), e);
			}
		}
	}

	@Override
	public void destroy() {
		clear();
		super.destroy();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		Set<Object> keys = c.keySet();
		Set<Pair<Object, Object>> entries = new HashSet<Pair<Object,Object>>();
		for (Object key : keys) {
			entries.add(Pair.read(key, retrieve(key)));
		}
		super.clear();
		for (Pair<Object, Object> entry : entries) {
			if( entry.two()!=null)
				notifyRemove(entry.one(), entry.two());
		}
	}

}
