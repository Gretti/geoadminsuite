package org.kaboum.geom;

/*
 *
 * Class KaboumGeometry from the Kaboum project.
 * This class define a geometry i.e. a geometrical object such
 * as a Point, a Linestring or a polygon object.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is inspired by JTS Geometry class
 * (cf. http://www.vividsolutions.com)
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

import java.io.Serializable;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumExtent;

/**
 *
 * This class define a geometry i.e. a geometrical object such
 * as a Point, a Linestring or a polygon object.
 *
 * @author Jerome Gasperi aka jrom
 *
 */

public abstract class KaboumGeometry implements Serializable {
    
    /** CONSTANTS Definition */
    public static final String WKT_POINT = "POINT";
    public static final String WKT_LINESTRING = "LINESTRING";
    public static final String WKT_POLYGON = "POLYGON";
    
    /** the user-defined or server defined identifier for this geometry.
     * It is vital that users adding geometries to Kaboum provide globally unique
     * identifiers for each geometries, as this id is used as a key in a hashtable
     * to store all geometries.
     * Thus, id cannot be null.
     */
    public String id;
    
    /** the tooltip text for this geometry */
    protected String toolTip;
    
    public KaboumGeometry() {
        toolTip = "";
    }
    
    /**
     *
     * Return true if this geometry got the same class of other
     * geometry
     *
     */
    protected boolean isEquivalentClass(KaboumGeometry other) {
        return this.getClass().getName().equals(other.getClass().getName());
    }

    /**
     * Flips the positions of the elements in the array so that the last is
     * first.
     *
     * @param coordinates the array to rearrange
     *
     */
    protected static void reversePointOrder(KaboumCoordinate[] coordinates) {
        
        KaboumCoordinate[] newCoordinates = new KaboumCoordinate[coordinates.length];
        
        for (int i = 0; i < coordinates.length; i++) {
            newCoordinates[i] = coordinates[coordinates.length - 1 - i];
        }
        
        System.arraycopy(newCoordinates, 0, coordinates, 0, coordinates.length);
        
    }
    
    /**
     *
     * Returns the minimum coordinate.
     *
     * @param coordinates the array to search in
     * @return the minimum coordinate in the array
     *
     */
    protected static KaboumCoordinate minCoordinate(KaboumCoordinate[] coordinates) {
        //
        return coordinates[0];
        
    }
    
    /**
     *
     * Shifts the positions of the coordinates until firstCoordinate
     * is first.
     *
     * @param coordinates      the array to rearrange
     * @param firstCoordinate  the coordinate to make first
     *
     */
    protected static void scroll(KaboumCoordinate[] coordinates, KaboumCoordinate firstCoordinate) {
        
        int i = indexOf(firstCoordinate, coordinates);
        
        if (i == -1) {
            return;
        }
        
        KaboumCoordinate[] newCoordinates = new KaboumCoordinate[coordinates.length];
        System.arraycopy(coordinates, i, newCoordinates, 0, coordinates.length - i);
        System.arraycopy(coordinates, 0, newCoordinates, coordinates.length - i, i);
        System.arraycopy(newCoordinates, 0, coordinates, 0, coordinates.length);
    }
    
    
    /**
     *
     * Returns true if the array contains any <code>null</code> elements.
     *
     * @param  array  an array to validate
     * @return        <code>true</code> if any of <code>array</code>s elements are
     *      <code>null</code>
     *
     */
    protected static boolean hasNullElements(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     *
     * Returns true if the array contains any non-empty <code>Geometry</code>s.
     *
     * @param  geometries  an array of <code>Geometry</code>s
     *
     */
    protected static boolean hasNonEmptyElements(KaboumGeometry[] geometries) {
        for (int i = 0; i < geometries.length; i++) {
            if (!geometries[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     *
     * Returns the index of <code>coordinate</code> in <code>coordinates</code>.
     * The first position is 0; the second, 1; etc.
     *
     * @param  coordinate   the <code>Coordinate</code> to search for
     * @param  coordinates  the array to search
     * @return              the position of <code>coordinate</code>, or -1 if it is
     *                      not found
     *
     */
    protected static int indexOf(KaboumCoordinate coordinate, KaboumCoordinate[] coordinates) {
        for (int i = 0; i < coordinates.length; i++) {
            if (coordinate.equals(coordinates[i])) {
                return i;
            }
        }
        return -1;
    }
    
    
    /**
     *
     * Perform an exact copy of this object
     *
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }
    
    
    /**
     *
     * Return the number og geometries
     *
     */
    public int getNumGeometries() {
        return 1;
    }
    
    
    /**
     *
     * Always returns false
     *
     */
    public boolean removeGeometry(KaboumGeometry inputGeometry) {
        return false;
    }
    
    
    /**
     *
     * Return the number og geometries
     *
     */
    public KaboumGeometry getGeometryN(int n) {
        return this;
    }
    
    
    public void addCoordinate(KaboumCoordinate internal) { }
    
    
    public void addCoordinateInGeometryN(KaboumCoordinate internal, int i) {
        this.addCoordinate(internal);
    }
    
    
    /**
     *
     * Returns this <code>Geometry</code> s vertices.
     *
     * @return    the vertices of this <code>Geometry</code>
     */
    public abstract KaboumCoordinate[] getCoordinates();
    

    public KaboumCoordinate[] getFilledCoordinates() {
        return new KaboumCoordinate[0];
    }
    
    /**
     *
     * Returns this <code>Geometry</code> s exterior vertices.
     *
     * @return    the vertices of this <code>Geometry</code>
     */
    public abstract KaboumCoordinate[] getExteriorCoordinates();
    
    
    /**
     *
     * Fill a SIMPLE geometry with input coordinates list
     *
     * @return    the vertices of this <code>Geometry</code>
     */
    public abstract boolean setCoordinates(KaboumCoordinate[] internals);
    
    
    /**
     *
     * Returns the count of this <code>Geometry</code>s vertices.
     *
     * @return    the number of vertices in this <code>Geometry</code>
     */
    public abstract int getNumPoints();
    
    public abstract int getExteriorNumPoints();
    
    public abstract boolean isClosed();
    
    public abstract int getDimension();
    
    public abstract String getGeometryType();
    
    public abstract void normalize();
    
    public abstract boolean equalsExact(KaboumGeometry other);
    
    public abstract KaboumExtent getExtent();
    
    public abstract double getArea();
    
    public abstract double getPerimeter();

    public String getToolTip() {
        return toolTip;
    }

    /**
     * Sets tooltip for this geometry.
     * If given string contains one of the special keywords denoting area or perimeter geometry value
     * ($$AREA$$ or $$PERIMETER$$), replaces this keyword by its runtime value
     * t
     * @param toolTip
     */
    public void setToolTip(String toolTip) {
        /*
        if (toolTip != null && (toolTip.contains("$$AREA$$") || toolTip.contains("$$PERIMETER$$"))) {
            String res = toolTip;
            String regex = null;
            regex = "\\[\\$\\$AREA\\$\\$\\]";
            res = res.replaceAll(regex, String.valueOf(this.getArea()));
            regex = "\\[\\$\\$PERIMETER\\$\\$\\]";
            res = res.replaceAll(regex, String.valueOf(this.getPerimeter()));
            this.toolTip = res;
        } else {
         */
            this.toolTip = toolTip;
        //}
    }
    
    public abstract boolean isEmpty();
    
    
}

