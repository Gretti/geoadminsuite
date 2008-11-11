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
