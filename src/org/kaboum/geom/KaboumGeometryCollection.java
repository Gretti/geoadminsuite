/*
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is inspired by JTS GeometryCollection class
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
import org.kaboum.util.KaboumUtil;

/**
 *
 * Implementation of <code>GeometryCollection</code>.
 *
 */
public class KaboumGeometryCollection extends KaboumGeometry implements Serializable {
    
    /** Internal representation of this <code>GeometryCollection</code>.*/
    protected KaboumGeometry[] geometries;
    
    /**
     *
     * Constructs a <code>GeometryCollection</code>.
     *
     * @param  geometries      the <code>Geometry</code>s for this <code>GeometryCollection</code>
     *      , or <code>null</code> or an empty array to create the empty geometry.
     *      Elements may be empty <code>Geometry</code>s, but not <code>null</code>
     *      s.
     *
     */
    public KaboumGeometryCollection(KaboumGeometry[] geometries) {
        if (geometries == null) {
            geometries = new KaboumGeometry[]{};
        }
        this.geometries = geometries;
    }
    
    
    public KaboumCoordinate[] getCoordinates() {
        KaboumCoordinate[] coordinates = new KaboumCoordinate[getNumPoints()];
        int k = -1;
        for (int i = 0; i < geometries.length; i++) {
            KaboumCoordinate[] childCoordinates = ((KaboumGeometry) geometries[i]).getCoordinates();
            for (int j = 0; j < childCoordinates.length; j++) {
                k++;
                coordinates[k] = childCoordinates[j];
            }
        }
        return coordinates;
    }
    
    
    public KaboumCoordinate[] getExteriorCoordinates() {
        KaboumCoordinate[] coordinates = new KaboumCoordinate[getNumPoints()];
        int k = -1;
        for (int i = 0; i < geometries.length; i++) {
            KaboumCoordinate[] childCoordinates = ((KaboumGeometry) geometries[i]).getExteriorCoordinates();
            for (int j = 0; j < childCoordinates.length; j++) {
                k++;
                coordinates[k] = childCoordinates[j];
            }
        }
        return coordinates;
    }
    
    
    /**
     *
     * You CANNOT set coordinate of a Geometry Collection
     *
     */
    public boolean setCoordinates(KaboumCoordinate[] internals) {
        return false;
    }
    
    
    public boolean isEmpty() {
        for (int i = 0; i < geometries.length; i++) {
            if (!geometries[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    
    public int getDimension() {
        
        int dimension = -1;
        
        for (int i = 0; i < geometries.length; i++) {
            dimension = Math.max(dimension, geometries[i].getDimension());
        }
        
        return dimension;
        
    }
    
    public int getNumGeometries() {
        return geometries.length;
    }
    
    
    public KaboumGeometry getGeometryN(int n) {
        return geometries[n];
    }
    
    
    public boolean removeGeometry(KaboumGeometry inputGeometry) {
        
        int numGeometries = this.getNumGeometries();
        KaboumGeometry tmpGeometry = null;
        
        for (int i = 0; i < numGeometries; i++) {
            tmpGeometry = (KaboumGeometry) geometries[i];
            if (tmpGeometry.equalsExact(inputGeometry)) {
                this.removeGeometryN(i);
                return true;
            }
        }
        
        return false;
    }
    
    
    public boolean removeGeometryN(int n) {
        
        int numGeometries = this.getNumGeometries();
        
        if (numGeometries == 0) {
            return false;
        }
        
        if (n > numGeometries) {
            return false;
        }
        
        KaboumGeometry[] tmpGeometries = new KaboumGeometry[numGeometries - 1];
        
        if (n == 0) {
            System.arraycopy(this.geometries, 1, tmpGeometries, 0, numGeometries - 1);
        }
        else if (n == numGeometries) {
            System.arraycopy(this.geometries, 0, tmpGeometries, 0, numGeometries - 1);
        }
        else {
            System.arraycopy(this.geometries, 0, tmpGeometries, 0, n);
            System.arraycopy(this.geometries, n + 1, tmpGeometries, n, numGeometries - n - 1);
        }
        
        this.geometries = tmpGeometries;
        
        return false;
    }
    
    
    public int getNumPoints() {
        int numPoints = 0;
        for (int i = 0; i < geometries.length; i++) {
            numPoints += ((KaboumGeometry) geometries[i]).getNumPoints();
        }
        return numPoints;
    }
    
    
    public int getExteriorNumPoints() {
        int numPoints = 0;
        for (int i = 0; i < geometries.length; i++) {
            numPoints += ((KaboumGeometry) geometries[i]).getExteriorNumPoints();
        }
        return numPoints;
    }
    
    
    public String getGeometryType() {
        return "GeometryCollection";
    }
    
    
    public boolean equalsExact(KaboumGeometry other) {
        if (!isEquivalentClass(other)) {
            return false;
        }
        KaboumGeometryCollection otherCollection = (KaboumGeometryCollection) other;
        if (geometries.length != otherCollection.geometries.length) {
            return false;
        }
        if (geometries.length != otherCollection.geometries.length) {
            return false;
        }
        for (int i = 0; i < geometries.length; i++) {
            if (!(geometries[i] instanceof KaboumGeometry)) {
                return false;
            }
            if (!(otherCollection.geometries[i] instanceof KaboumGeometry)) {
                return false;
            }
            if (!((KaboumGeometry) geometries[i]).equalsExact((KaboumGeometry) otherCollection.geometries[i])) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isClosed() {
        if (isEmpty()) {
            return false;
        }
        for (int i = 0; i < geometries.length; i++) {
            if (!geometries[i].isClosed()) {
                return false;
            }
        }
        
        return true;
        
    }
    
    public void normalize() {
        for (int i = 0; i < geometries.length; i++) {
            geometries[i].normalize();
        }
    }
    
    public KaboumExtent getExtent() {
        KaboumExtent envelope = new KaboumExtent();
        for (int i = 0; i < geometries.length; i++) {
            envelope.expandToInclude(geometries[i].getExtent());
        }
        return envelope;
    }
    
    public double getArea() {
        if (isEmpty()) {
            return 0;
        }
        if (!isClosed()) {
            return 0;
        }
        
        double area = 0;
        
        for (int i = 0; i < geometries.length; i++) {
            area = area  + geometries[i].getArea();
        }
        
        return area;
        
    }
    
    public double getPerimeter() {
        
        double perimeter = 0;
        
        for (int i = 0; i < geometries.length; i++) {
            perimeter = perimeter  + geometries[i].getPerimeter();
        }
        
        return perimeter;
        
    }
    
    public void addCoordinateInGeometryN(KaboumCoordinate internal, int i) {
        KaboumGeometry tmpGeometry = this.getGeometryN(i);
        if (tmpGeometry != null) {
            tmpGeometry.addCoordinate(internal);
        }
    }
    
    /** 
     * returns the given geometry forced to be a MULTIxxx
     * @param geom the simple geometry to force into a collection
     * @return the input geometry forced to a collection, or the input geometry
     * if it is already a Multi
     */
    public static KaboumGeometryCollection forceCollection(KaboumGeometry geom) {
        if (geom == null) {
            return null;
        }
        KaboumUtil.debug("geom type: " + geom.getGeometryType());
        if (geom.getGeometryType().equalsIgnoreCase("POINT")) {
            KaboumPoint[] points = new KaboumPoint[1];
            points[0] = (KaboumPoint)geom;
            KaboumMultiPoint mpt = new KaboumMultiPoint(points);
            return mpt;
        } else if (geom.getGeometryType().equalsIgnoreCase("LINESTRING")) {
            KaboumLineString[] lines = new KaboumLineString[1];
            lines[0] = (KaboumLineString)geom;
            KaboumMultiLineString mln = new KaboumMultiLineString(lines);
            return mln;
        } else if (geom.getGeometryType().equalsIgnoreCase("POLYGON")) {
            KaboumPolygon[] polys = new KaboumPolygon[1];
            polys[0] = (KaboumPolygon)geom;
            KaboumMultiPolygon mpg = new KaboumMultiPolygon(polys);
            return mpg;
        } else {
            return (KaboumGeometryCollection)geom;
        }
    }
    
}


