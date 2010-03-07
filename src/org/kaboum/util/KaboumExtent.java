package org.kaboum.util;

/*
 *
 * Class KaboumExtent from the Kaboum project.
 * This class define a spatial extent.
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

import java.io.Serializable;


/**
 *
 * This class define a spatial extent.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumExtent implements Serializable  {
    
    /** Lower left X coordinate */
    public double xMin;
    
    /** Lower left Y coordinate */
    public double yMin;
    
    /** Upper right X coordinate */
    public double xMax;
    
    /** Upper right Y coordinate */
    public double yMax;
    
    /** Precision Model */
    private static KaboumPrecisionModel pm = new KaboumPrecisionModel();
    
    /** in case of item query map mode, tells if kaboum should zoom on the found features.
     * true to zoom on the found features, false to keep current extent.
     *
     */
    public boolean zoomOnShapes = false;
    
    /**
     *
     * Default Constructor
     *
     *
     */
    public KaboumExtent() {
        this.init();
    }
    
    
    /**
     *
     * Constructor
     *
     * @param xMin Lower left X coordinate
     * @param yMin Lower left Y coordinate
     * @param xMax Upper right X coordinate
     * @param yMax Upper right Y coordinate
     *
     */
    public KaboumExtent(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = this.makePrecise(xMin);
        this.yMin = this.makePrecise(yMin);
        this.xMax = this.makePrecise(xMax);
        this.yMax = this.makePrecise(yMax);
    }
    
    
    /**
     *
     * Constructor
     *
     * @param coordUL  Lower left  coordinate
     * @param coordUR  Upper right coordinate
     *
     */
    public KaboumExtent(KaboumCoordinate coordLL, KaboumCoordinate coordUR) {
        this.xMin = this.makePrecise(coordLL.x);
        this.yMin = this.makePrecise(coordLL.y);
        this.xMax = this.makePrecise(coordUR.x);
        this.yMax = this.makePrecise(coordUR.y);
    }
    
    /**
     *
     * Set the extent e to the current Extent
     *
     */
    public void set(KaboumExtent e) {
        this.xMin = this.makePrecise(e.xMin);
        this.yMin = this.makePrecise(e.yMin);
        this.xMax = this.makePrecise(e.xMax);
        this.yMax = this.makePrecise(e.yMax);
    }
    

    /**
     *
     * Conform extent coordinates to the precision model
     *
     */
    private double makePrecise(double val) {
        if (this.pm != null) {
            return this.pm.makePrecise(val);
        }
        
        return val;
        
    }
    
    
    /**
     *
     * Checks if extent e overlap currentExtent
     *
     * @param e Extent to check overlapping with the current one
     *
     */
    public boolean overlap(KaboumExtent e) {
        return !( this.makePrecise(e.xMin) > this.xMax  ||
        this.makePrecise(e.xMax) < this.xMin ||
        this.makePrecise(e.yMin) > this.yMax ||
        this.makePrecise(e.yMax) < this.yMin);
    }
    
    
    /**
     *
     * Checks if currentExtent contains another extent e
     *
     * @param e Extent
     *
     */
    public boolean contains(KaboumExtent e) {
        return ( this.xMax >= this.makePrecise(e.xMax)  &&
        this.xMin <= this.makePrecise(e.xMin)  &&
        this.yMax >= this.makePrecise(e.yMax)  &&
        this.yMin <= this.makePrecise(e.yMin));
    }
    
    
    /**
     *
     * Checks if extent contains Coordinate c
     *
     * @param c coordinate
     *
     */
    public boolean contains(KaboumCoordinate c) {
        
        double x = this.makePrecise(c.x);
        double y = this.makePrecise(c.y);
        
        return ( this.xMax >= x  &&
        this.xMin <= x  &&
        this.yMax >= y  &&
        this.yMin <= y);
    }
    
    
    /**
     *
     * Checks if extent truly contains coordinate c.
     * Point that lie under the limit of the extent
     * are not inside the extent.
     *
     * @param c coordinate
     *
     */
    public boolean trulyContains(KaboumCoordinate c) {
        
        double x = this.makePrecise(c.x);
        double y = this.makePrecise(c.y);
        
        return ( this.xMax > x  &&
        this.xMin < x  &&
        this.yMax > y  &&
        this.yMin < y);
    }
    
    
    /**
     *
     * Return the deltaX i.e. (xMax - xMin)
     *
     */
    public double dx() {
        return this.xMax - this.xMin;
    }
    
    
    /**
     *
     * Return the deltaY i.e. (yMax - yMin)
     *
     */
    public double dy() {
        return this.yMax - this.yMin;
    }
    
    
    /**
     *
     * Return the equivalent size in pixel of a distance
     * in map unit within a given size
     *
     */
    public int getSizeInPixels(double d, int width) {
        return (int) ((d * width) / this.dx());
    }
    
    
    /**
     *
     * Enlarges the boundary of the <code>Envelope</code> so that it contains
     * (x,y). Does nothing if (x,y) is already on or within the boundaries.
     *
     * @param  p  input coordinate
     *
     */
    public void expandToInclude(KaboumCoordinate p) {
        
        double x = this.makePrecise(p.x);
        double y = this.makePrecise(p.y);
        
        if (isNull()) {
            xMin = x;
            xMax = x;
            yMin = y;
            yMax = y;
        }
        else {
            if (x < xMin) {
                xMin = x;
            }
            if (x > xMax) {
                xMax = x;
            }
            if (y < yMin) {
                yMin = y;
            }
            if (y > yMax) {
                yMax = y;
            }
        }
    }

    
    /**
     *
     * Enlarges the boundary of the <code>Extent</code> so that it contains
     * <code>other</code>. Does nothing if <code>other</code> is wholly on or
     * within the boundaries.
     *
     * @param  other  the <code>Extent</code> to merge with
     */
    public void expandToInclude(KaboumExtent other) {
        if (other.isNull()) {
            return;
        }
        
        double tmpXMin = this.makePrecise(other.xMin);
        double tmpXMax = this.makePrecise(other.xMax);
        double tmpYMin = this.makePrecise(other.yMin);
        double tmpYMax = this.makePrecise(other.yMax);
        
        if (isNull()) {
            xMin = tmpXMin;
            xMax = tmpXMax;
            yMin = tmpYMin;
            yMax = tmpYMax;
        }
        else {
            if (tmpXMin < xMin) {
                xMin = tmpXMin;
            }
            if (tmpXMax > xMax) {
                xMax = tmpXMax;
            }
            if (tmpYMin < yMin) {
                yMin = tmpYMin;
            }
            if (tmpYMax > yMax) {
                yMax = tmpYMax;
            }
        }
    }
    
    
    /**
     *
     * Returns <code>true</code> if this <code>Envelope</code> is a "null"
     * extent.
     *
     * @return    <code>true</code> if this <code>Extent</code> is uninitialized
     *
     */
    public boolean isNull() {
        return xMax < xMin;
    }
    
    
    /**
     *
     * Initialize this extent
     *
     */
    public void init() {
        this.xMin = 0.0;
        this.yMin = 0.0;
        this.xMax = -1.0;
        this.yMax = -1.0;
    }
    
    
    /**
     *
     * Return a string version of this extent
     * in a Kaboum output readable format
     *
     */
    public String kaboumExternalString() {
        return this.externalString(",", ";");
    }
    
    
    /**
     *
     * Return a string version of this extent
     * in a mapserver cgi readable form
     *
     * @param pm Precision Model
     *
     */
    public String msString() {
        if (zoomOnShapes) {
            // special extent value in case of item query map mode, to tell
            // mapserver to zoom on the found features;
            return "shapes";
        }
        return this.externalString("+", "+");
    }
    

    /**
     *
     * Return a string version of this extent
     * in a mapserver cgi readable form
     *
     * @param pm Precision Model
     *
     */
    public String externalString(String xySeparator, String coordSeparator) {
        if (pm == null) {
            return Double.toString(this.xMin)+xySeparator+Double.toString(this.yMin)+coordSeparator+Double.toString(this.xMax)+xySeparator+Double.toString(this.yMax);
        }
        
        KaboumCoordinate externalLL = pm.toExternal(this.xMin, this.yMin);
        KaboumCoordinate externalUR = pm.toExternal(this.xMax, this.yMax);
        
        return Double.toString(externalLL.x)+xySeparator+Double.toString(externalLL.y)+coordSeparator+Double.toString(externalUR.x)+xySeparator+Double.toString(externalUR.y);

    }

    
    /**
     *
     * Set the precision model 
     *
     */
    public static void setPM(KaboumPrecisionModel _pm) {
        pm = _pm;
    }
    
    
}
