/*
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is inspired by JTS LineString class
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
 * Basic implementation of <code>LineString</code>.
 *
 */
public class KaboumLineString extends KaboumGeometry implements Serializable {
    
    /** The points of this <code>LineString</code>. */
    protected KaboumCoordinate points[];
    
    /**
     *
     * Constructs a <code>LineString</code> with the given points.
     *
     * @param  points          the points of the linestring
     *
     */
    public KaboumLineString(KaboumCoordinate[] points) {
        if (points == null) {
            points = new KaboumCoordinate[]{};
        }
        if (hasNullElements(points)) {
            throw new IllegalArgumentException("point array must not contain null elements");
        }
        if (points.length == 1) {
            throw new IllegalArgumentException("point array must contain 0 or >1 elements");
        }
        this.points = points;
    }
    
    
    public KaboumCoordinate[] getCoordinates() {
        return points;
    }
    
    
    public KaboumCoordinate[] getExteriorCoordinates() {
        return getCoordinates();
    }
    
    
    public KaboumCoordinate getCoordinateN(int n) {
        return points[n];
    }
    
    
    public int getDimension() {
        return 1;
    }
    
    
    public boolean isEmpty() {
        return points.length == 0;
    }
    
    
    public int getNumPoints() {
        return points.length;
    }
    
    
    public int getExteriorNumPoints() {
        return this.getNumPoints();
    }
    
    
    public KaboumPoint getPointN(int n) {
        return new KaboumPoint(points[n]);
    }
    
    
    public KaboumPoint getStartPoint() {
        if (isEmpty()) {
            return null;
        }
        return getPointN(0);
    }
    
    
    public KaboumPoint getEndPoint() {
        if (isEmpty()) {
            return null;
        }
        return getPointN(getNumPoints() - 1);
    }
    
    
    public boolean isClosed() {
        if (isEmpty()) {
            return false;
        }
        return getCoordinateN(0).equals(getCoordinateN(getNumPoints() - 1));
    }
    

    
    public String getGeometryType() {
        return "LineString";
    }
    
    
    /**
     *
     * Returns true if the given point is a vertex of this <code>LineString</code>
     *
     * @param      pt  the <code>Coordinate</code> to check
     * @return     <code>true</code> if <code>pt</code> is one of this <code>LineString</code>
     *             vertices
     *
     */
    public boolean isCoordinate(KaboumCoordinate pt) {
        for (int i = 1; i < points.length; i++) {
            if (points[i].equals(pt)) {
                return true;
            }
        }
        return false;
    }
    
    
    protected KaboumExtent computeExtentInternal() {
        if (isEmpty()) {
            return new KaboumExtent();
        }
        double minx = points[0].x;
        double miny = points[0].y;
        double maxx = points[0].x;
        double maxy = points[0].y;
        for (int i = 1; i < points.length; i++) {
            minx = Math.min(minx, points[i].x);
            maxx = Math.max(maxx, points[i].x);
            miny = Math.min(miny, points[i].y);
            maxy = Math.max(maxy, points[i].y);
        }
        return new KaboumExtent(minx, maxx, miny, maxy);
    }
    
    
    public boolean equalsExact(KaboumGeometry other) {
        if (!isEquivalentClass(other)) {
            return false;
        }
        KaboumLineString otherLineString = (KaboumLineString) other;
        if (points.length != otherLineString.points.length) {
            return false;
        }
        for (int i = 0; i < points.length; i++) {
            if (!points[i].equals(otherLineString.points[i])) {
                return false;
            }
        }
        return true;
    }
    
    
    public Object clone() {
        KaboumLineString ls = (KaboumLineString) super.clone();
        KaboumCoordinate[] pts = new KaboumCoordinate[points.length];
        ls.points = pts;
        for (int i = 0; i < points.length; i++) {
            ls.points[i] = (KaboumCoordinate) points[i].clone();
        }
        return ls;// return the clone
    }
    
    
    public void normalize() {
        for (int i = 0; i < points.length; i++) {
            int j = points.length - 1 - i;
            if (!points[i].equals(points[j])) {
                if (points[i].compareTo(points[j]) > 0) {
                    reversePointOrder(points);
                }
                return;
            }
        }
    }
    
    
    protected boolean isEquivalentClass(KaboumGeometry other) {
        return other instanceof KaboumLineString;
    }
    
    public KaboumExtent getExtent() {
        if (isEmpty()) {
            return new KaboumExtent();
        }
        double minx = points[0].x;
        double miny = points[0].y;
        double maxx = points[0].x;
        double maxy = points[0].y;
        for (int i = 1; i < points.length; i++) {
            minx = Math.min(minx, points[i].x);
            maxx = Math.max(maxx, points[i].x);
            miny = Math.min(miny, points[i].y);
            maxy = Math.max(maxy, points[i].y);
        }
        return new KaboumExtent(minx, miny, maxx, maxy);
    }
    
    public double getArea() {
        return 0;
    }
    
    public double getPerimeter() {
        
        double perimeter = 0;
        
        int numPointsMinusOne = this.points.length - 1;
       
        if (numPointsMinusOne < 1) {
            return 0;
        }
        
        for (int i = 0; i < numPointsMinusOne; i++) {
            perimeter = perimeter + this.points[i].distance(this.points[i + 1]);
        }
        
        return perimeter;
        
    }
    
    public void addCoordinate(KaboumCoordinate internal) {
        
        int size = this.points.length;
        
        KaboumCoordinate[] tmpInternals = new KaboumCoordinate[size + 1];
        System.arraycopy(this.points, 0, tmpInternals, 0, size);
        tmpInternals[size] = internal;
        this.points = tmpInternals;
        
        return;
    }
    
    
    public boolean setCoordinates(KaboumCoordinate[] internals) {
        
        if (internals != null) {
            this.points = internals;
            return true;
        }
        
        return false;
        
    }
    
   /**
     * Override the equals method so that 2 LineStrings with same points are equals
     *@see equals in Object
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if((obj == null) || (obj.getClass() != this.getClass())) return false;
        
        boolean b = super.equals(obj);
        
        if (!b) return false;

        // we have a KaboumLineString here
        KaboumLineString ls = (KaboumLineString)obj;
        
        if (points == null && ls.points == null) return true;
        
        if (points != null && ls.points != null && points.length == ls.points.length) {
            for (int i = 0; i < points.length; i++) {
                if (!points[i].equals(ls.points[i])) return false;
            }
        } else {
            return false;
        }
        return true;
    }
    
    
}

