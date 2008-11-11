package org.geogurus.gas.actions;

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
        String strvalue = String.valueOf(operatee.getAttribute(attName));
        double value = (new Double(strvalue)).doubleValue();
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
