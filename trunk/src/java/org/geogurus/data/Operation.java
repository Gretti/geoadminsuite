package org.geogurus.data;

/**
 * The generified operation.
 * 
 * @param <S>
 *            The type of object to be operated on
 * @param <T>
 *            the type the context object
 */
public interface Operation<S, T> {
    /**
     * Called before the operations starts
     * 
     * @param context
     *            An object that is passed to all calls to this operation
     */
    void start(T context);

    /**
     * Performs the operation on the operatee.
     * 
     * @param operatee
     *            the object that will be operated on
     * @param context
     *            A results accumulator. Must be threadsafe
     * 
     * @return return false to stop operation.
     */
    boolean operate(S operatee, T context);

    /**
     * Called when the operation has visited all objects. This is a
     * wrap-up/clean up method. This is always called. Even if the method was
     * cancelled.
     * 
     * @param finished
     *            true if the {@link #operate(Object, Object)} method never
     *            returned false.
     * @param context
     *            An object that is passed to all calls to this operation
     */
    void end(T context, boolean finished);
}
