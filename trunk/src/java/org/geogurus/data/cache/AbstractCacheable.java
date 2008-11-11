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
