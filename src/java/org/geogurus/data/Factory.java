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
