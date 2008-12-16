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

import java.util.Set;

import org.geogurus.data.OperationAdapter;
import org.geogurus.gas.objects.ListClassesBean;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.gas.utils.ColorGenerator;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Creates a classification for each unique value in the features.
 * 
 * @author jesse
 */
public class UniqueValueFeatureClassification extends OperationAdapter<SimpleFeature, Set<Integer>> {

    private final String attributeName;
    private final ColorGenerator colorGenerator;
    private final ListClassesBean list;
    private final int classLimit;
    private final String symName;
    private final int symSize;

    public UniqueValueFeatureClassification(String attributeName,
            int classLimit, ColorGenerator colorGenerator,
            ListClassesBean list, String symName, int symSize) {
        this.attributeName = attributeName;
        this.classLimit = classLimit;
        this.colorGenerator = colorGenerator;
        this.list = list;
        this.symName = symName;
        this.symSize = symSize;
    }

    public boolean operate(SimpleFeature operatee, Set<Integer> context) {

        String attribute = String.valueOf(operatee.getAttribute(attributeName));

        if (context.size() < classLimit) {
            if (context.add(attribute.hashCode())) {
                Class cl = new Class();
                cl.setColor(colorGenerator.getNextColor());
                cl.setOutlineColor(new RGB(0, 0, 0));
                // Uses first class of defaultMsLayer of gc to assign to all
                // other classes
                cl.setSymbol(symName);
                cl.setSize(symSize);
                // trim expression and name: expression with leading or
                // trailing spaces are not
                // handled correctly by MapServer
                cl.setName(attribute.trim());
                cl.setExpression(attribute.trim());
                list.addClass(cl);
            }
            return true;
        } else {
            list.setMessage("classlimitation," + classLimit);
            return false;
        }

    }

    public String getAttributeName() {
        return attributeName;
    }

    public ColorGenerator getColorGenerator() {
        return colorGenerator;
    }

    public ListClassesBean getList() {
        return list;
    }

    public int getClassLimit() {
        return classLimit;
    }

    public String getSymName() {
        return symName;
    }

    public int getSymSize() {
        return symSize;
    }
}
