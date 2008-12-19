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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math.stat.StatUtils;
import org.geogurus.data.OperationAdapter;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Calculates the minimum and maximum values for a feature's attribute.
 * 
 * @author jesse
 */
public class RangeAttributeOperation<T extends Comparable<T>> extends OperationAdapter<SimpleFeature, Object> {

    private String attName;
    private double min,  max;
    private List<Double> listValues;
    private double[] values;

    public RangeAttributeOperation(String attName, Double startingMin, Double startingMax) {
        super();
        this.attName = attName;
        this.max = startingMax;
        this.min = startingMin;
        this.listValues = new ArrayList<Double>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean operate(SimpleFeature operatee, Object context) {
        //As attribute can be of all numeric types, need to transit by a string
        String strValue = String.valueOf(operatee.getAttribute(attName));
        double value = (new Double(strValue)).doubleValue();
        listValues.add(value);
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

    public void sortValues() {
        values = new double[listValues.size()];
        for (int pos = 0; pos < listValues.size(); pos++) {
            values[pos] = listValues.get(pos).doubleValue();
        }
        Arrays.sort(values);
    }

    public double percentile(double p) {
        return StatUtils.percentile(values, p);
    }

    public double mean() {
        return StatUtils.mean(values);
    }

    public double stddev() {
        return Math.sqrt(StatUtils.variance(values, mean()));
    }
}
