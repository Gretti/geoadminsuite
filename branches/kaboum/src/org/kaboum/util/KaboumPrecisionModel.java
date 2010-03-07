/*
 *
 * Class KaboumUnits from the Kaboum project.
 * This class manage the current map units.
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
package org.kaboum.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


/**
 *
 * This class manage the map units and the precision Model
 *
 * Some parts of this code are rewritten from JTS PrecisionModel class
 * (cf. http://www.vividsolutions.com)
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumPrecisionModel {
    
    /** Constants definition */
    public static final int MS_METERS = 0;
    public static final int MS_KILOMETERS = 1;
    public static final int MS_DD = 2;
    
    /** Surface definition */
    public static final int MS_SQUARE_METERS = 3;
    public static final int MS_ARES = 4;
    public static final int MS_HECTARES = 5;
    public static final int MS_SQUARE_KILOMETERS = 6;
    public static final int MS_DECARES = 7;
    
    /** Current unit */
    private int unitType;
    
    /** Precision scale */
    public double scale;
    
    /** X offset */
    private double offsetX = 0;
    
    /** Y offset */
    private double offsetY = 0;
    
    /** Formated output cutted to 4 digits */
    private DecimalFormat df = null;
    
    /** Equivalence meters <--> degrees on the equator */
    private final double K_D2M = 111317.099692;
    
    /** Dots per inch = 72 */
    public final int DPI = 72;
    
    /** This is used to computed scale */
    public double magicalNumber;
    
    /** Used for scale computation */
    private int mFactor;

    /** Used for surface transform */
    private int sFactor;
    
    /** Formated output cutted to 4 digits for surface */
    private DecimalFormat sf = null;

    // Used to create the formated text output
    /** unit of x axis */
    public String xStrUnit;
    /** unit of y axis */
    public String yStrUnit;
    // NRI: the 
    /** general unit (ex: degree when axis are E, N) */
    public String strUnit;
    /** the surface unit */
    public String sUnit;
    
    /** Used to calculate distance between two points */
    private double xDistanceMod;
    private double yDistanceMod;
    
    
    /**
     *
     * Constructor
     *
     */
    public KaboumPrecisionModel() {
        this(MS_METERS, MS_SQUARE_METERS, 1);
    }
    

    /**
     *
     * Constructor
     *
     */
    public KaboumPrecisionModel(int unitType) {
        this(unitType, MS_SQUARE_METERS, 1);
    }
    

    /**
     *
     * Constructor.
     *Note: precision parameter is no more used, as it was very buggy.
     *
     *
     */
    public KaboumPrecisionModel(int unitType, int surfaceType, float surfacePrecision) {

        /** Avoid "," to be the current decimal separator */
        DecimalFormatSymbols dfSym = new DecimalFormatSymbols();
        dfSym.setDecimalSeparator('.');
        this.df = new DecimalFormat("#####0.0000", dfSym);
        this.sf = new DecimalFormat("#####0.0000", dfSym);

        this.unitType = unitType;

        // Units
        switch (unitType) {
            
            case MS_METERS:
                this.magicalNumber = 39.3701;
                this.mFactor = 1;
                this.xStrUnit = "m";
                this.yStrUnit = "m";
                this.strUnit = "m";
                break;
            case MS_KILOMETERS:
                this.magicalNumber = 39370.1;
                this.mFactor = 1000;
                this.xStrUnit = "km";
                this.yStrUnit = "km";
                this.strUnit = "km";
                break;
            case MS_DD:
                this.magicalNumber = 4374754;
                this.mFactor = 100000;
                this.xStrUnit = "E";
                this.yStrUnit = "N";
                this.strUnit = "deg";
                break;
                
                // Default is MS_METERS
            default:
                this.unitType = MS_METERS;
                this.magicalNumber = 39.3701;
                this.mFactor = 1;
                this.xStrUnit = "m";
                this.yStrUnit = "m";
        }

        // Surface representation
        switch (surfaceType) {
            
            case MS_SQUARE_METERS :
                this.sFactor = 1;
                this.sUnit = "m2";
                break;
            case MS_SQUARE_KILOMETERS :
                this.sFactor = 1000000;
                this.sUnit = "km2";
                break;
            case MS_ARES :
                this.sFactor = 100;
                this.sUnit = "a";
                break;
            case MS_DECARES :
                this.sFactor = 1000;
                this.sUnit = "da";
                break;
            case MS_HECTARES:
                this.sFactor = 10000;
                this.sUnit = "ha";
                break;
                
            // Default is SURFACE_IN_SQUARE_METERS
            default:
                this.sFactor = 1;
        }

        // precision is no more used as it is VERY buggy.
        // see commit comments
        // fixme: clean up this code to remove all references to old precision
        //this.scale = 1 / precision;
        this.scale = 1;
        
        if (this.scale > 1) {
            String tmpStr = (int) scale + "";
            this.df = new DecimalFormat("#####0." + tmpStr.substring(1), dfSym);
        } else {
            this.df = new DecimalFormat("#####0", dfSym);
        }
        
        int sScale = (int) (1 / surfacePrecision);
        
        if (sScale > 1) {
            String tmpStr = sScale + "";
            this.sf = new DecimalFormat("#####0." + tmpStr.substring(1), dfSym);
        } else {
            this.sf = new DecimalFormat("#####0", dfSym);
        }
        
        KaboumUtil.debug("Scale used for precision model is " + this.scale);
        
    }
    
    
    /**
     *
     * Return the formated output of the pointer in map coordinate
     * assuming the current unit.
     *
     * @param external  Coordinate (in internal representation)
     *
     */
    public String writeMapCoords(KaboumCoordinate internal) {
        KaboumCoordinate external = this.toExternal(internal);
        return df.format(external.x) + " " + this.xStrUnit + ", " + df.format(external.y) + " " + this.yStrUnit;
    }
    
    
    /**
     *
     * Return an integered value of val
     *
     */
    public static double makePrecise(double val) {
        //return Math.rint(val);
        return val;
    }
    
    
    /**
     *
     * Sets <code>internal</code> to the precise representation of <code>external</code>
     *
     * @param x   External representation of x coordinate
     * @param y   External representation of x coordinate
     *
     */
    public KaboumCoordinate toInternal(double xExternal, double yExternal) {
        
        double x = makePrecise((xExternal - this.offsetX) * this.scale);
        double y = makePrecise((yExternal - this.offsetY) * this.scale);
        
        return new KaboumCoordinate(x, y);
        
    }
    
    
    /**
     *
     * Sets <code>external</code> to the external representation of <code>internal</code>
     *
     * @param x   Internal representation of x coordinate
     * @param y   Internal representation of y coordinate
     *
     */
    public KaboumCoordinate toExternal(double xInternal, double yInternal) {
        
        double x = (xInternal / this.scale) + this.offsetX;
        double y = (yInternal / this.scale) + this.offsetY;
        
        return new KaboumCoordinate(x, y);
        
    }
    
    
    /**
     *
     * Sets <code>internal</code> to the precise representation of <code>external</code>
     *
     * @param external the original coordinate
     *
     */
    public KaboumCoordinate toInternal(KaboumCoordinate external) {
        return this.toInternal(external.x, external.y);
    }
    
    
    /**
     *
     * Sets <code>external</code> to the external representation of <code>internal</code>
     *
     * @param internal  the original coordinate
     *
     */
    public KaboumCoordinate toExternal(KaboumCoordinate internal) {
        return this.toExternal(internal.x, internal.y);
    }
    
    
    /**
     *
     * Convert an input string to the corresponding
     * int value
     *
     */
    public static int sToUnit(String str) {
        
        // Default: return MS_METERS
        if (str == null) {
            return MS_METERS;
        }
        
        if (str.equals("MS_METERS")) { return MS_METERS; }
        else if (str.equals("MS_KILOMETERS")) { return MS_KILOMETERS; }
        else if (str.equals("MS_DD")) { return MS_DD; }
        else { return MS_METERS; }
    }
    
    
    /**
     *
     * Convert an input string to the corresponding
     * int value
     *
     */
    public static int sToSurface(String str) {
        
        // Default: return MS_SQUARE_METERS
        if (str == null) {
            return MS_SQUARE_METERS;
        }
        
        if (str.equals("MS_SQUARE_METERS")) { return MS_SQUARE_METERS; }
        else if (str.equals("MS_ARES")) { return MS_ARES; }
        else if (str.equals("MS_DECARES")) { return MS_DECARES; }
        else if (str.equals("MS_HECTARES")) { return MS_HECTARES; }
        else if (str.equals("MS_SQUARE_KILOMETERS")) { return MS_SQUARE_KILOMETERS; }
        else { return MS_SQUARE_METERS; }
    }
    
    
    /**
     *
     * Return the distance between two point in meters.
     * The calculation for points in geographical coordinates
     * only works for close points since it assume the mean
     * latitude between these two points as the disance reference.
     *
     * @param coordA    Coordinate A (internal representation)
     * @param coordB    Coordinate B (internal representation)
     *
     */
    public double getDistance(KaboumCoordinate coordA, KaboumCoordinate coordB) {

        KaboumCoordinate externalA = this.toExternal(coordA);
        KaboumCoordinate externalB = this.toExternal(coordB);
        
        switch (this.unitType) {
            
            case MS_METERS:
                this.xDistanceMod = 1;
                this.yDistanceMod = 1;
                break;
            case MS_KILOMETERS:
                this.xDistanceMod = 1000;
                this.yDistanceMod = 1000;
                break;
            case MS_DD:
                this.xDistanceMod = K_D2M * Math.cos(externalA.y - externalB.y);
                this.yDistanceMod = K_D2M;
                break;
                
                // Default is MS_METERS
            default:
                this.xDistanceMod = 1;
                this.yDistanceMod = 1;
        }
        
        return Math.sqrt(
                Math.pow((externalA.x-externalB.x)*xDistanceMod, 2.0) + 
                Math.pow((externalA.y-externalB.y)*yDistanceMod, 2.0));
    }
    
    /**
     * Return the signed area formed by the given 3 points, in coordinates units.
     *<p>
     * this method can be iteratively called on polygon coordinates to compute a polygon
     * area.
     *</p>
     *<p>
     * The calculation for points in geographical coordinates
     * only works for close points since it assumes the mean
     * latitude between these two points as the disance reference.
     *</p>
     * @param a - point one (internal representation)
     * @param b - point two (internal representation)
     * @param c - point three (internal representation)
     */
    public double getSurface(KaboumCoordinate a, KaboumCoordinate b, KaboumCoordinate c) {
        return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y);
    }
    
    /**
     *
     * Get the magicalNumber
     *
     */
    public double getMagicalNumber() {
        return this.magicalNumber;
    }
    
        
    /**
     *
     * Return a formated output from an input number
     *
     */
    public String getNumberFormated(double number) {
        return this.df.format(number);
    }

    
    /**
     *
     * Return a string representation of an area expressed in
     * internals coordinates
     *
     */
    public String areaToString(double area) {
        return this.sf.format(area / (Math.pow(scale, 2) * this.sFactor));
    }
    
    
    /**
     *
     * Return a string representation of a perimeter expressed in
     * internals coordinates
     *
     */
    public String perimeterToString(double perimeter) {
        return this.df.format(perimeter / scale);
    }
    
}
