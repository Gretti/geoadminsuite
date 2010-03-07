/*
 * List.java
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

package org.kaboum.util;

import java.io.Serializable;
import java.util.Vector;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumPoint;
import org.kaboum.geom.KaboumMultiPoint;
import org.kaboum.geom.KaboumLineString;
import org.kaboum.geom.KaboumMultiLineString;
import org.kaboum.geom.KaboumPolygon;
import org.kaboum.geom.KaboumMultiPolygon;
import org.kaboum.geom.KaboumLinearRing;


/**
 *
 * Basic implementation of List / ArrayList java 1.2 classes
 *
 * @author  jrom
 *
 */
public class KaboumList extends Vector implements Serializable {
   

    /**
     *
     * Constructor
     *
     */
    public KaboumList() {
        super();
    }

    
    /**
     *
     * Constructor
     *
     */
    public KaboumList(int i) {
        super(i);
    }
    
    
    /**
     *
     * Return a vector from an array of coordinates
     *
     */
    public static KaboumList toKaboumList(KaboumCoordinate[] internals) {
        
        int numPoints = internals.length;
        KaboumList list = new KaboumList(numPoints);
        
        for (int i = 0; i < numPoints; i++) {
            list.addElement(internals[i]);
        }
        
        return list;
        
    }
    

    /**
     *
     * Return an array of KaboumCoordinate
     *
     */
    public KaboumCoordinate[] toCoordinateArray() {

        int size = this.size();

        KaboumCoordinate[] outputArray = new KaboumCoordinate[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumCoordinate) this.elementAt(i);
        }        
        return outputArray;
        
    }

    
    /**
     *
     * Return an array of KaboumGeometry
     *
     */
    public KaboumGeometry[] toGeometryArray() {

        int size = this.size();

        KaboumGeometry[] outputArray = new KaboumGeometry[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumGeometry) this.elementAt(i);
        }        
        return outputArray;
        
    }

    /**
     *
     * Return an array of KaboumPoint
     *
     */
    public KaboumPoint[] toPointArray() {

        int size = this.size();
        
        KaboumPoint[] outputArray = new KaboumPoint[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumPoint) this.elementAt(i);
        }        
        return outputArray;
        
    }
    
    /**
     *
     * Return an array of KaboumMultiPoint
     *
     */
    public KaboumMultiPoint[] toMultiPointArray() {

        int size = this.size();
    
        KaboumMultiPoint[] outputArray = new KaboumMultiPoint[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumMultiPoint) this.elementAt(i);
        }        
        return outputArray;
        
    }
    
    /**
     *
     * Return an array of KaboumLineString
     *
     */
    public KaboumLineString[] toLineStringArray() {

        int size = this.size();
        
        KaboumLineString[] outputArray = new KaboumLineString[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumLineString) this.elementAt(i);
        }        
        return outputArray;
        
    }
    
    
    /**
     *
     * Return an array of KaboumMultiLineString
     *
     */
    public KaboumMultiLineString[] toMultiLineStringArray() {

        int size = this.size();
    
        KaboumMultiLineString[] outputArray = new KaboumMultiLineString[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumMultiLineString) this.elementAt(i);
        }        
        return outputArray;
        
    }
    
    /**
     *
     * Return an array of KaboumPolygon
     *
     */
    public KaboumPolygon[] toPolygonArray() {

        int size = this.size();

        KaboumPolygon[] outputArray = new KaboumPolygon[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumPolygon) this.elementAt(i);
        }        
        return outputArray;
        
    }
    
    
    /**
     *
     * Return an array of KaboumMultiPolygon
     *
     */
    public KaboumMultiPolygon[] toMultiPolygonArray() {

        int size = this.size();
    
        KaboumMultiPolygon[] outputArray = new KaboumMultiPolygon[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumMultiPolygon) this.elementAt(i);
        }        
        return outputArray;
        
    }
    
    
    /**
     *
     * Return an array of KaboumLinearRing
     *
     */
    public KaboumLinearRing[] toLinearRingArray() {

        int size = this.size();
        
        KaboumLinearRing[] outputArray = new KaboumLinearRing[size];       

        for (int i = 0; i < size; i++) {
            outputArray[i] = (KaboumLinearRing) this.elementAt(i);
        }        
        return outputArray;
        
    }
}
