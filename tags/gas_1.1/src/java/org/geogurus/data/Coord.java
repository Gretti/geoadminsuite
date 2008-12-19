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
 * Very basic 2-dimension coordinate object
 */
public class Coord  implements java.io.Serializable { 
	private static final long serialVersionUID = 1L;
	/** the x-coordinate of this <code>Coord</code>. (public for more efficient access)*/
    public double x;
    /** the y-coordinate of this <code>Coord</code>. (public for more efficient access)*/
    public double y;

    public Coord() {}
    public Coord(double x, double y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Constructor with <code>String</code> representation of double values.
     * <br>Note:<b>Set x and y fields to Double.MIN_VALUE in case of NumberFormatException</b>
     */
     public Coord(String xs, String ys) {
        try {
            x = new Double(xs).doubleValue();
            y = new Double(ys).doubleValue();
        } catch (NumberFormatException nfe) {
            x = Double.MIN_VALUE;
            y = Double.MIN_VALUE;
        }
     }
}

