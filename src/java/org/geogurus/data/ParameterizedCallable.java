package org.geogurus.data;

/**
 * A strategy similar to {@link java.util.concurrent.Callable} except it take a
 * parameter.
 * 
 * @author jesse
 * 
 * @param <T>
 *            The return type of the run method
 * @param <P>
 *            the type of object that the object takes as a parameter to the run
 *            method
 */
public interface ParameterizedCallable<T, P> {
    T run(P param);
}