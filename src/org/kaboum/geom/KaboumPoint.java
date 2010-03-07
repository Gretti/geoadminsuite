/*
 *
 * Class KaboumPoint from the Kaboum project.
 * This class define a geometry i.e. a geometrical object such
 * as a Point, a Linestring or a polygon object.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is inspired by JTS Point class
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
package org.kaboum.geom;

import java.io.Serializable;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumExtent;

/**
 *
 * Basic implementation of <code>Point</code>.
 *
 */
public class KaboumPoint extends KaboumGeometry implements Serializable {
    
    /** The <code>Coordinate</code> wrapped by this <code>Point</code>. */
    protected KaboumCoordinate coordinate;
    
    /**
     *
     * Constructs a <code>Point</code> with the given coordinate.
     *
     * @param  coordinate      the coordinate on which to base this <code>Point</code>
     *
     */
    public KaboumPoint(KaboumCoordinate coordinate) {
        this.coordinate = coordinate;
    }
    
    
    public KaboumCoordinate[] getCoordinates() {
        return isEmpty() ? new KaboumCoordinate[]{} : new KaboumCoordinate[]{
            coordinate
        };
    }
    
    public boolean equalsExact(KaboumGeometry other) {
        if (!isEquivalentClass(other)) {
            return false;
        }
        if (isEmpty() && other.isEmpty()) {
            return true;
        }
        return ((KaboumPoint) other).coordinate.equals(this.coordinate);
    }

    public KaboumCoordinate[] getExteriorCoordinates() {
        return this.getCoordinates();
    }

    public int getNumPoints() {
        return isEmpty() ? 0 : 1;
    }
    

    public int getExteriorNumPoints() {
        return this.getNumPoints();
    }
    

    public boolean isEmpty() {
        return coordinate == null;
    }
    
    
    public int getDimension() {
        return 0;
    }
    
    
    public double getX() {
        if (coordinate == null) {
            throw new IllegalStateException("getX called on empty Point");
        }
        return coordinate.x;
    }
    
    
    public double getY() {
        if (coordinate == null) {
            throw new IllegalStateException("getY called on empty Point");
        }
        return coordinate.y;
    }
    
    
    public KaboumCoordinate getCoordinate() {
        return coordinate;
    }
    
    
    public boolean setCoordinates(KaboumCoordinate[] internals) {
        if (internals != null) {
            if (internals.length == 0) {
                this.coordinate = null;
            }
            else {
                this.coordinate = internals[0];
                return true;
            }
        }
        return false;
    }
    
    public String getGeometryType() {
        return "Point";
    }
    
    
    protected KaboumExtent computeExtentInternal() {
        if (isEmpty()) {
            return new KaboumExtent();
        }
        return new KaboumExtent(coordinate.x, coordinate.x, coordinate.y, coordinate.y);
    }
    
    public Object clone() {
        KaboumPoint p = (KaboumPoint) super.clone();
        p.coordinate = coordinate != null ? (KaboumCoordinate) coordinate.clone() : null;
        return p;// return the clone
    }
    
    
    public boolean isClosed() {
        return false;
    }
    
    public void normalize() { }
    
    public KaboumExtent getExtent() {
        if (isEmpty()) {
            return new KaboumExtent();
        }
        return new KaboumExtent(coordinate.x, coordinate.y, coordinate.x, coordinate.y);
    }
    
    
    public double getArea() {
        return 0;
    }
    
    
    public double getPerimeter() {
        return 0;
    }
    
    
    public void addCoordinate(KaboumCoordinate internal) {
        this.coordinate = internal;
    }
}

