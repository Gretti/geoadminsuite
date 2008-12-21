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
package org.geogurus.data.operations;

import org.geogurus.data.OperationAdapter;
import org.opengis.feature.simple.SimpleFeature;
import java.util.Vector;
/**
 * Finds features from spatial query.
 * 
 * @author jesse
 */
public class SpatialQueryOperation<T extends Comparable<T>> extends OperationAdapter<SimpleFeature, Object> {

    public SpatialQueryOperation() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean operate(SimpleFeature operatee, Object context) {
        ((Vector<SimpleFeature>) context).add(operatee);
        return true;
    }
}
