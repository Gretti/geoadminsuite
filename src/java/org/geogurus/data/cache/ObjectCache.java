/**
 * 
 */
package org.geogurus.data.cache;

import org.geogurus.data.Factory;

import com.whirlycott.cache.Cache;
import com.whirlycott.cache.CacheException;
import com.whirlycott.cache.CacheManager;
import com.whirlycott.cache.Cacheable;

/**
 * Provides a single place to cache "heavy" objects such as Connections or Datastores etc...
 * <p>
 * All "disposable" objects will be
 * @author jesse
 */
public final class ObjectCache {
	private final static ObjectCache instance = new ObjectCache();

    /**
     * Retrieves the identified object from the cache or creates it with the
     * factory if it is not in the cache
     * 
     * @param <T>
     *            The required return type.
     * @param <P>
     *            The parameter object for the factory.
     * @param key
     *            The identifier for the object
     * @param factory
     *            The factory for creating desired the object, note.
     * @param factoryParams
     *            the parameters for creating the object
     * @return Returns the cached copy identified by the key or an new instance
     *         created by the factory if the cache does not contain an instance.
     * @throws CacheException 
     */
    @SuppressWarnings("unchecked")
    public final synchronized <T, P> T getCachedObject(Cacheable key,
            Factory<T, P> factory, P factoryParams)  {
        Cache cache;
		try {
			cache = CacheManager.getInstance().getCache();
		} catch (CacheException e) {
			throw new RuntimeException(e);
		}
        Object object = cache.retrieve(key);
        if (object == null) {
            object = factory.create(factoryParams);
          cache.store(key, object, 1000*60*2);
        }
        return (T) object;
    }

    public static ObjectCache getInstance() {
		return instance;
	}
}
