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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.cartoweb;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * The base class for Cartoweb configuration objects (scales, shortcuts, mapsize, etc)
 * @author nicolas
 */
public abstract class CartowebObject {

    /** the class' logger object */
    /** the object's identifier */
    protected String id;
    /** the string used to represent this object in display mode:
     * id [- <object attribute>]
     */
    protected String displayString;

    protected Logger logger;
    protected String lineSep = System.getProperty("line.separator");

    public CartowebObject(String id) {
        this.id = id;
        logger = Logger.getLogger(this.getClass().getName());
    }

    /** writes this object into the given properties object, using
     * object.id.key=value syntax
     * See derived classes
     * @param props the Properties to write into
     * @param id the identifier to write for the object
     */
    public abstract void writeToProperties(Properties props);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayString() {
        return displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }
}