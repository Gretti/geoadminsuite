package org.geogurus.data;

import java.io.Serializable;

/**
 * Represents a Value that may or may not have a value. There are two types a
 * "None" option that indicates there is no value. A "Some" option which
 * indicates there is a value to be used.
 * 
 * <p>
 * An example usage is the return type of a find method. A search is performed
 * and a result may or may not have been found. If not a None option is returned
 * otherwise a Some option is returned. The normal Java pattern for this is to
 * return null or a value but the Option class makes the fact that there may not
 * be a value explicit rather than having to read the Javadocs
 * </p>
 * 
 * @author jesse
 */
public class Option<T> implements Serializable {

    private static final long serialVersionUID = -6950381122893950501L;

    /**
     * Creates a None option.
     * 
     * @param <T>
     * @return
     */
    public static <T> Option<T> none() {
        return new Option<T>();
    }

    public static <T> Option<T> some(T value) {
        return new Option<T>(value);
    }

    final T value;

    /**
     * Creates a new (Null) instance of type Option
     */
    private Option() {
        value = null;
    }

    /**
     * Creates a new instance (non-null) of type Option
     * 
     * @param value
     *            the value of the Option
     */
    private Option(T value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "value is null.  Use no-arg constructor if you want a None Option");
        }
        this.value = value;
    }

    public boolean isNone() {
        return value == null;
    }

    public T get() {
        if (isNone()) {
            throw new IllegalStateException(
                    "This is a None object.  You must do a check before calling this method");
        }
        return value;
    }

    public T getOrElse(T defaultValue) {
        if (isNone()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    /**
     * Returns true is this is a "Some" option
     * 
     * @return
     */
    public boolean isSome() {
        return !isNone();
    }

    @Override
    public String toString() {
        if (isNone()) {
            return "None";
        }
        return "Some(" + value + ")";
    }
}
