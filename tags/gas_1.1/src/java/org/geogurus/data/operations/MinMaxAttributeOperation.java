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

/**
 * Calculates the minimum and maximum values for a feature's attribute.
 * 
 * @author jesse
 */
public class MinMaxAttributeOperation<T extends Comparable<T>> extends
        OperationAdapter<SimpleFeature, Object> {

    private String attName;

    private double min, max;

    public MinMaxAttributeOperation(String attName, Double startingMin, Double startingMax) {
        super();
        this.attName = attName;
        this.max = startingMax;
        this.min = startingMin;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean operate(SimpleFeature operatee, Object context) {
        //As attribute can be of all numeric types, need to transit by a string
        String strValue = String.valueOf(operatee.getAttribute(attName));
        double value = (new Double(strValue)).doubleValue();
        if (value < min || min == Double.MIN_VALUE) {
            min = value;
        }

        if (value > max || max == Double.MAX_VALUE) {
            max = value;
        }
        return true;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }
}
