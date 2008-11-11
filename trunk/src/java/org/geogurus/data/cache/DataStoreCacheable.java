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
