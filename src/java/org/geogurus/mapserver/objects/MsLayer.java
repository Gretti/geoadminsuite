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

package org.geogurus.mapserver.objects;

/**
 * Enumeration of MapServer Layer types.
 * 
 * @author jesse
 */
public enum MsLayer {
    LOCAL("Label"), SDE("sde"), OGR("ogr"), POSTGIS("Postgis"), ORACLESPATIAL(
            "Oracle Spatial"), WMS("Web Map Server"), WFS("Web Feature Server"),
            SPATIALITE("Spatialite");

    private final String label;

    /**
     * Creates a new instance of type MsLayer
     */
    private MsLayer(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
