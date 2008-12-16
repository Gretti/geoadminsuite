/*
 * Copyright (C) 2007-2008  Camptocamp
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

package org.geogurus.cartoweb;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Represents a Cartoweb location.ini "scale" set of properties:<br/>
 * scales.0.label = 1/20000
 *  scales.0.value = 20000
 * scales.0.visible = true
<br/>
 * This object is used by the LocationConf object to manage a location.ini cartoweb
 * configuration file.
 * @author nicolas
 */
public class CartowebScale extends CartowebObject {
    /** the object's id as defined in the location.ini list of scales
     * 
     */
    /** the shortcut's label */
    private String label;
    /** the scale's denominator: 200000 means a 1/200000 scale */
    private Double value;
    /** is this shortcut visible in Cartoweb user interface */
    private Boolean visible;
    
    public CartowebScale(String id) {
        super(id);
        logger = Logger.getLogger(this.getClass().getName()); 
   }
    
     /**
     * Gets this object's representation as key=value pairs.
     * Each object's attribute will be rendered on a single line, with its name
     * as a key and its value as a value.
     * @see Properties
     * @param id: the identifier to use when rendering this object. it will be used as:
     *        scales.id.[attribute] = [value]
     * @return the string represntation of this object, or null if id is null
     */
   public String toString (){
        if (id == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (label != null) {
            builder.append("scales.").append(id).append(".label = ");
            builder.append(getLabel()).append(System.getProperty("line.separator"));
        }
        if (value != null) {
            builder.append("scales.").append(id).append(".value=");
            // cast value to int, if possible, to avoid 10000.0 notation when not necessary
            String s = null;
            if (value.doubleValue() == value.intValue()) {
                s = String.valueOf(value.intValue());
            } else {
                s = value.toString();
            }
            builder.append(s).append(System.getProperty("line.separator"));
        }
        if (visible != null) {
            builder.append("scales.").append(id).append(".visible=");
            builder.append(Boolean.toString(getVisible())).append(System.getProperty("line.separator"));
        }
        
        return builder.toString();
    }
   
    /** writes this object into the given properties object, using
     * scales.<id>.key=value syntax
     * See derived classes
     * @param props the Properties to write into
     * @param id the identifier to write for the object
     */
    public void writeToProperties(Properties props) {
        if (props == null || id == null) {
            return;
        }
        if (getLabel() != null) {
            props.setProperty("scales."+id+".label",getLabel());
        }
        if (getValue() != null) {
            props.setProperty("scales."+id+".value", getValue().toString());
        }
        if (getVisible() != null) {
            props.setProperty("scales."+id+".visible", getVisible().toString());
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    /**
     * Returns the display string representing this scale:
     * id - label - value - visible
     * @return
     */
    @Override
    public String getDisplayString() {
        displayString = id + " - ";
        displayString += getLabel() == null ? "null" : getLabel();
        displayString += " - ";
        displayString += getValue() == null ? "null" : getValue().toString();
        displayString += " - ";
        displayString += getVisible() == null ? "null" : getVisible().toString();
        return displayString;
    }
}
