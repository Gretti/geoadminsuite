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
