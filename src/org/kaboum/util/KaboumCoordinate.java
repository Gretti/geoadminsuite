package org.kaboum.util;

import java.io.Serializable;

/*
 *
 * Class KaboumCoordinate from the Kaboum project.
 * This class define a control point.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * Copyright (C) 2000-2003 Jerome Gasperi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *

 */

/**
 *
 * This class define a 2D coordinate
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumCoordinate implements Serializable  {
    
    /** Constants definition */
    public static final int K_TYPE_POINT = 0;
    public static final int K_TYPE_BOX = 1;
    public static final int K_TYPE_CIRCLE = 2;
    public static final int K_TYPE_IMAGE = 3;
    
    /** X coordinate */
    public double x;
    
    /** Y coordinate */
    public double y;
    
    
    /**
     *
     * Constructor
     *
     */
    public KaboumCoordinate() {
        this(0.0, 0.0);
    }
    
    
    /**
     *
     * Constructor
     *
     * @param x X coordinate
     * @param y Y coordinate
     *
     */
    public KaboumCoordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    
    /**
     *
     * Sort of copy constructor
     *
     * @param cp Control point
     *
     */
    public KaboumCoordinate(KaboumCoordinate coord) {
        this(coord.x, coord.y);
    }
    
    
    /**
     *
     * Move a point to the current position
     *
     * @param x X coordinate (map)
     * @param y Y coordinate (map)
     *
     */
    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    
    /**
     *
     * Move a point to the current position
     *
     * @param internal Coordinate
     *
     */
    public void moveTo(KaboumCoordinate internal) {
        this.x = internal.x;
        this.y = internal.y;
    }
    
    
    /**
     *
     * Return the distance from this coordinate to
     * the input one
     *
     * @param internal Input coordinate
     *
     */
    public double distance(KaboumCoordinate internal) {
        return Math.sqrt(squareDistance(internal));
    }
    
    /**
     *
     * Return the square distance from this coordinate to
     * the input one
     *
     * @param internal Input coordinate
     *
     */
    public double squareDistance(KaboumCoordinate internal) {
        if (internal == null) {
            return -1;
        }
        return ((internal.x - this.x) * (internal.x - this.x)) + ((internal.y - this.y) * (internal.y - this.y));
    }
    
    /**
     * 2 Coordinates with same x and y fields are equal
     *@see equals in Object
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if((obj == null) || (obj.getClass() != this.getClass())) return false;
        
        // we have a KaboumGeometry here
        KaboumCoordinate coord = (KaboumCoordinate)obj;
        
        return (x == coord.x && y == coord.y);
    }
    
    /**
     * Override the hashCode because equals was overridden
     *@see hashCode in Object
    public int hashCode() {
        int hash = 7;
        long bits = Double.doubleToLongBits(x);
        int var_code = (int)(bits ^ (bits >>> 32));
        hash = 31 * hash + var_code;
        
        bits = Double.doubleToLongBits(y);
        var_code = (int)(bits ^ (bits >>> 32));
        hash = 31 * hash + var_code;
        
        return hash;
    }
     */
    
    /**
     *
     * Return a string of the location of the point
     * Format:  x, y
     *
     */
    public String write() {
        return this.x+", "+this.y;
    }
    
    
    public Object clone() {
        try {
            KaboumCoordinate coord = (KaboumCoordinate) super.clone();
            return coord;// return the clone
        } catch (Exception e) {
            return null;
        }
    }
    
    
    /**
     *
     * Compares this object with the specified object for order.
     *
     * @param  o  the <code>Coordinate</code> with which this <code>Coordinate</code>
     *            is being compared
     * @return    a negative integer, zero, or a positive integer as this <code>Coordinate</code>
     *            is less than, equal to, or greater than the specified <code>Coordinate</code>
     *
     */
    public int compareTo(KaboumCoordinate o) {
        if (x < o.x) {
            return -1;
        }
        if (x > o.x) {
            return 1;
        }
        if (y < o.y) {
            return -1;
        }
        if (y > o.y) {
            return 1;
        }
        return 0;
    }
    
    
    /**
     *
     * Convert an input string to the corresponding
     * int value
     *
     */
    public static int stoi(String str) {
        if (str == null) {
            return K_TYPE_POINT;
        }
        if (str.equals("K_TYPE_CIRCLE")) {
            return K_TYPE_CIRCLE;
        } else if (str.equals("K_TYPE_POINT")) {
            return K_TYPE_POINT;
        } else if (str.equals("K_TYPE_BOX")) {
            return K_TYPE_BOX;
        } else if (str.equals("K_TYPE_IMAGE")) {
            return K_TYPE_IMAGE;
        } else {
            return K_TYPE_POINT;
        }
    }
    
    public String toString() {
        return "" + this.x + " " + this.y;
    }
    
}

