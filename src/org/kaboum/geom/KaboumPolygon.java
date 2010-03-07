/*
 *
 * Class KaboumPolygon from the Kaboum project.
 * This class define a geometry i.e. a geometrical object such
 * as a Point, a Linestring or a polygon object.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is inspired by JTS Polygon class
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
import org.kaboum.algorithm.KaboumAlgorithms;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumExtent;

/**
 *
 * Basic implementation of <code>Polygon</code>.
 *
 */
public class KaboumPolygon extends KaboumGeometry implements Serializable {
    
    /** The exterior boundary */
    protected KaboumLinearRing shell = null;
    
    /** The interior boundaries, if any. */
    protected KaboumLinearRing[] holes;
    
    /**
     *
     * Constructs a <code>Polygon</code> with the given exterior boundary.
     * The shell and holes must conform to the assertions specified in the <A
     * HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
     * Specification for SQL</A>.
     *
     * @param  shell   the outer boundary of the new <code>Polygon</code>.
     *                 Must be oriented clockwise.
     *
     */
    public KaboumPolygon(KaboumLinearRing shell) {
        this(shell, new KaboumLinearRing[]{});
    }
    
    /**
     *
     * Constructs a <code>Polygon</code> with the given exterior boundary and
     * interior boundaries.
     *
     * @param  shell           the outer boundary of the new <code>Polygon</code>.
     *                         Must be oriented clockwise.
     * @param  holes           the inner boundaries of the new <code>Polygon</code>.
     *                         Each must be oriented counterclockwise.
     *
     */
    public KaboumPolygon(KaboumLinearRing shell, KaboumLinearRing[] holes) {
        if (shell == null) {
            shell = new KaboumLinearRing(null);
        }
        if (holes == null) {
            holes = new KaboumLinearRing[]{};
        }
        if (hasNullElements(holes)) {
            throw new IllegalArgumentException("holes must not contain null elements");
        }
        if (shell.isEmpty() && hasNonEmptyElements(holes)) {
            throw new IllegalArgumentException("shell is empty but holes are not");
        }
        this.shell = shell;
        this.holes = holes;
        
        this.normalize();
    }
    
    public KaboumCoordinate[] getCoordinates() {
        if (isEmpty()) {
            return new KaboumCoordinate[]{};
        }
        KaboumCoordinate[] coordinates = new KaboumCoordinate[getNumPoints()];
        int k = -1;
        KaboumCoordinate[] shellCoordinates = shell.getCoordinates();
        for (int x = 0; x < shellCoordinates.length; x++) {
            k++;
            coordinates[k] = shellCoordinates[x];
        }
        for (int i = 0; i < holes.length; i++) {
            KaboumCoordinate[] childCoordinates = holes[i].getCoordinates();
            for (int j = 0; j < childCoordinates.length; j++) {
                k++;
                coordinates[k] = childCoordinates[j];
            }
        }
        return coordinates;
    }
    
    public KaboumCoordinate[] getFilledCoordinates() {
        
        if (isEmpty()) {
            return new KaboumCoordinate[]{};
        }
        
        KaboumCoordinate[] coordinates = new KaboumCoordinate[getNumPoints() + holes.length];
        
        int k = -1;
        
        KaboumCoordinate[] shellCoordinates = shell.getCoordinates();
        
        for (int x = 0; x < shellCoordinates.length; x++) {
            k++;
            coordinates[k] = shellCoordinates[x];
        }
        
        for (int i = 0; i < holes.length; i++) {
            KaboumCoordinate[] childCoordinates = holes[i].getCoordinates();
            
            k++;
            coordinates[k] = shellCoordinates[0];
            
            for (int j = 0; j < childCoordinates.length; j++) {
                k++;
                coordinates[k] = childCoordinates[j];
            }
            
        }
        
        return coordinates;
    }
    
    public KaboumCoordinate[] getExteriorCoordinates() {
        return this.getExteriorRing().getCoordinates();
    }
    
    
    /**
     *
     * By default, adding coordinates to a Polygon is equivalent
     * to adding coordinates to the shell
     *
     */
    public boolean setCoordinates(KaboumCoordinate[] internals) {
        return this.shell.setCoordinates(internals);
    }
    
    
    public int getNumPoints() {
        int numPoints = shell.getNumPoints();
        for (int i = 0; i < holes.length; i++) {
            numPoints += holes[i].getNumPoints();
        }
        return numPoints;
    }
    
    
    public int getExteriorNumPoints() {
        return shell.getNumPoints();
    }
    
    
    public int getDimension() {
        return 2;
    }
    
    
    public boolean isEmpty() {
        return shell.isEmpty();
    }
    
    
    public KaboumLineString getExteriorRing() {
        return shell;
    }
    
    public int getNumInteriorRing() {
        return holes.length;
    }
    
    public KaboumLineString getInteriorRingN(int n) {
        return holes[n];
    }
    
    public String getGeometryType() {
        return "Polygon";
    }
    
    protected KaboumExtent computeExtentInternal() {
        return shell.computeExtentInternal();
    }
    
    public boolean equalsExact(KaboumGeometry other) {
        if (!isEquivalentClass(other)) {
            return false;
        }
        KaboumPolygon otherPolygon = (KaboumPolygon) other;
        if (!(shell instanceof KaboumGeometry)) {
            return false;
        }
        KaboumGeometry thisShell = shell;
        if (!(otherPolygon.shell instanceof KaboumGeometry)) {
            return false;
        }
        KaboumGeometry otherPolygonShell = otherPolygon.shell;
        if (!thisShell.equalsExact(otherPolygonShell)) {
            return false;
        }
        if (holes.length != otherPolygon.holes.length) {
            return false;
        }
        if (holes.length != otherPolygon.holes.length) {
            return false;
        }
        for (int i = 0; i < holes.length; i++) {
            if (!(holes[i] instanceof KaboumGeometry)) {
                return false;
            }
            if (!(otherPolygon.holes[i] instanceof KaboumGeometry)) {
                return false;
            }
            if (!((KaboumGeometry) holes[i]).equalsExact((KaboumGeometry) otherPolygon.holes[i])) {
                return false;
            }
        }
        return true;
    }
    
    public Object clone() {
        KaboumPolygon poly = (KaboumPolygon) super.clone();
        poly.shell = (KaboumLinearRing) shell.clone();
        poly.holes = new KaboumLinearRing[holes.length];
        for (int i = 0; i < holes.length; i++) {
            poly.holes[i] = (KaboumLinearRing) holes[i].clone();
        }
        return poly;// return the clone
    }
    
    public void normalize() {
        normalize(shell, true);
        for (int i = 0; i < holes.length; i++) {
            normalize(holes[i], false);
        }
        // Not implemented JAVA 1.2
        //Arrays.sort(holes);
    }
    
    private void normalize(KaboumLinearRing ring, boolean clockwise) {
        if (ring.isEmpty()) {
            return;
        }
        KaboumCoordinate[] uniqueCoordinates = new KaboumCoordinate[ring.getCoordinates().length - 1];
        System.arraycopy(ring.getCoordinates(), 0, uniqueCoordinates, 0, uniqueCoordinates.length);
        KaboumCoordinate minCoordinate = minCoordinate(ring.getCoordinates());
        scroll(uniqueCoordinates, minCoordinate);
        System.arraycopy(uniqueCoordinates, 0, ring.getCoordinates(), 0, uniqueCoordinates.length);
        ring.getCoordinates()[uniqueCoordinates.length] = uniqueCoordinates[0];
        if (KaboumAlgorithms.isCCW(ring.getCoordinates()) == clockwise) {
            reversePointOrder(ring.getCoordinates());
        }
    }
    
    public boolean isClosed() {
        return true;
    }
    
    public KaboumExtent getExtent() {
        return shell.getExtent();
    }
    
    /**
     * Compute area
     * @return double Area of the polygon
     */
    public double getArea() {
        
        double area = 0;
        
        // Get the shell area
        area = this.shell.getArea();
        
        // Get the holes area
        for (int i = 0; i < this.holes.length; i++) {
            area -= this.holes[i].getArea();
        }
        
        return area;
        
    }
    
    /**
     * Compute perimeter
     * @return double Perimeter of the polygon
     */
    public double getPerimeter() {
        
        double perimeter = 0;
        
        // Get the shell perimeter
        perimeter = this.shell.getPerimeter();
        
        // Get the holes perimeter
        for (int i = 0; i < this.holes.length; i++) {
            perimeter += this.holes[i].getPerimeter();
        }
        return perimeter;
    }
    
    
    public void addCoordinate(KaboumCoordinate internal) {
        this.shell.addCoordinate(internal);
        
        return;
    }
    
    
    public void addCoordinateInHoleN(KaboumCoordinate internal, int i) {
        KaboumLineString ls = this.getInteriorRingN(i);
        if (ls != null) {
            ls.addCoordinate(internal);
        }
        
        return;
    }
    
    public void setHoles(KaboumLinearRing[] _holes) {
        this.holes = _holes;
    }
}

