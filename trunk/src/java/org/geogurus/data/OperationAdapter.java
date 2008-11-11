package org.geogurus.data;

/**
 * The generified operation.
 * 
 * @param <S>
 *            The type of object to be operated on
 * @param <T>
 *            the type the context object
 */
public abstract class OperationAdapter<S, T> implements Operation<S, T> {
    public void start(T context) {
    }

    public abstract boolean operate(S operatee, T context);

    public void end(T context, boolean finished) {
    }
}
