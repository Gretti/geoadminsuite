/*
 *
 * Class KaboumLinearRing from the Kaboum project.
 * This class define a geometry i.e. a geometrical object such
 * as a Point, a Linestring or a polygon object.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class inspired by JTS LinearRing class
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


/**
 *
 * Basic implementation of <code>LinearRing</code>.
 *
 */
public class KaboumLinearRing extends KaboumLineString implements Serializable {
    
    /**
     *
     * Constructs a <code>LinearRing</code> with the given points.
     * A LinearRing cannot be used as a geometrical object, so it does
     * not get unique id.
     *
     * @param  points         points forming a closed and simple linestring
     *
     */
    public KaboumLinearRing(KaboumCoordinate[] points) {
        super(points);
        if (!isEmpty() && ! super.isClosed()) {
            throw new IllegalArgumentException("points must form a closed linestring");
        }
        if (points != null && (points.length == 1 || points.length == 2)) {
            throw new IllegalArgumentException("points must contain 0 or >2 elements");
        }
        
        this.normalize();
        
    }
    
    
    public String getGeometryType() {
        return "LinearRing";
    }
    
    
    public boolean isClosed() {
        return true;
    }
    
    
    public Object clone() {
        KaboumLinearRing lr = (KaboumLinearRing) super.clone();
        return lr;// return the clone
    }
    
    /**
     *
     * Compute the area of this Linear Ring
     *
     */
    public double getArea() {
        
        double doubleArea = 0.0;
        
        if (this.isEmpty()) {
            return doubleArea;
        }
        
        for (int i = 1; i < this.points.length - 1; i++) {
            doubleArea += doubleArea(this.points[0], this.points[i], this.points[i+1]);
        }

        return Math.abs(doubleArea / 2.0);
    }
    
    
    private double doubleArea(KaboumCoordinate a, KaboumCoordinate b, KaboumCoordinate c) {

        double ax = a.x;
        double ay = a.y;
        double bx = b.x;
        double by = b.y;
        double cx = c.x;
        double cy = c.y;
        
        return (bx - ax) * (cy - ay) - (cx - ax) * (by - ay);
        
    }
    
    
    public boolean setCoordinates(KaboumCoordinate[] internals) {
        
        if (internals != null) {
    
            this.points = internals;
            return true;
            /*
            // 
            // The correct way would be to refused internals array 
            // which are not closed...
            //
             
            // Must be closed
            if (internals.length > 1) {
                
                if (internals[0].equals(internals[internals.length - 1])) {
                    this.points = internals;
                    return true;
                }
            }
             */
        }
        
        return false;
        
    }
    
}

