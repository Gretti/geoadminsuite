package org.geogurus.data;


/**
 * A strategy for taking a HostDescriptorBean and constructing one or more T
 * objects from the bean.
 * 
 * @param <T>
 *            the type of object that the factory will create
 * @param <P>
 *            the type of object used to describe the connection parameters
 * 
 * @author jesse
 * 
 */
public interface Factory<T, P> {
    /**
     * Returns true if the bean can be used by this strategy
     * 
     * @param params
     *            the bean to test
     * @return true if the params can be used by this strategy
     */
    public abstract boolean canCreateFrom(P params);

    /**
     * Construct one or most <T> objects
     * 
     * @param params
     *            the parameters to use for constructing the object
     * @return return the "good" <T>s created from the params or null if there
     *         is a problem connecting to the resource
     */
    public abstract T create(P params);
}
