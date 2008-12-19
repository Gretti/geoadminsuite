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

/*
 * Attribute.java
 *
 * Created on 27 mars 2002, 13:47
 */
package org.geogurus.raster;
/**
 *
 * @author  Bastien VIALADE
 */
public class Attribute {
    
    public String name;
    public String value;
    public String type;
    public String label;
    
    /** Creates a new instance of Attribute */
    public Attribute() {
        name=null;
        value=null;
        type=null;
        label=null;
    }
    
    public Attribute(String name, String value, String type, String label) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.label = label;
    }
    
}
