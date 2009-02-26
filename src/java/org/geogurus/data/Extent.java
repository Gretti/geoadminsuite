/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geogurus.data;


import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * A very simple class for spatial extension.
 * The 2 <code>Coord</code> public fields (easier access) represent respectively
 * the lower-left and upper-right corners of the spatial extension
 * Several convenient methods are provided to increase the extent size by adding
 * <code>double</code>, <code>Coord</code>, <code>Extent</code> objects.
 * <br> A new <code>Extent<code> is created with the lower-left corner set to <code>Double.POSITIVE_INFINITY</code>
 * and the upper-right corner set to <code>Double.NEGATIVE_INFINITY</code> to guarantee a impossible Extent
 *
 */
public class Extent implements java.io.Serializable  {
	private static final long serialVersionUID = 1L;
	public Coord ll;
    public Coord ur;
    
    /**
     * Creates an empty <code>Extent</code> with ll <code>Coord</code> set to <code>Double.POSITIVE_INFINITY</code>
     * and ur <code>Coord</code> set to <code>-Double.NEGATIVE_INFINITY</code>
     */
    public Extent() {
        ll = new Coord(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        ur = new Coord(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY);
    }
    /**
     * Creates a new <code>Extent</code> with the specified <code>Coord</code>
     */
    public Extent(Coord ll, Coord ur) {
        this.ll = ll;
        this.ur = ur;
    }
    
    /**
     *
     * Constructor
     *
     * @param extent Extent
     *
     */
    public Extent(Extent extent) {
        this(extent.ll, extent.ur);
    }
    
    /**
     * Creates a new <code>Extent</code> with the specified four <code>double</code> values representing
     * the x-coord of the lower-left corner, etc...
     */
    public Extent(double llx, double lly, double urx, double ury) {
        ll = new Coord(llx, lly);
        ur = new Coord(urx, ury);
    }
    
    /**
     * Returns true if the specified <code>Coord</code> is inside (or on the edge of) this <code>Extent</code>
     *
     * @param co the <code>Coord</code> to test
     * @return true if co is inside this <code>Extent</code>
     */
    public boolean contains(Coord co) {
        boolean tx, ty;
        
        tx = ((co.x >= ll.x) && (co.x <= ur.x));
        ty = ((co.y >= ll.y) && (co.y <= ur.y));
        
        return (tx && ty);
    }
    
    /**
     * Returns true if the specified <code>Extent</code> is inside (or on the edge of) this <code>Extent</code>
     *
     * @param ex the <code>Extent</code> to test
     * @return true if ex is inside this <code>Extent</code>
     */
    public boolean contains(Extent ex) {
        boolean tx, ty;
        
        tx = ((ex.ll.x >= ll.x) && (ex.ur.x <= ur.x));
        ty = ((ex.ll.y >= ll.y) && (ex.ur.y <= ur.y));
        
        return tx && ty;
    }
    
    /**
     * Adds the given pair of double if they are outside this </code>Extent</code>
     */
    public void add(double x, double y)	{
        ll.x = (x < ll.x)? x : ll.x;
        ll.y = (y < ll.y)? y : ll.y;
        ur.x = (x > ur.x)? x : ur.x;
        ur.y = (y > ur.y)? y : ur.y;
    }
    /**
     * Adds the given double values if they are outside this </code>Extent</code>
     */
    public void add(double xmin, double ymin, double xmax, double ymax)	{
        add(new Extent(xmin, ymin, xmax, ymax));
    }
    /**
     * Adds the given <code>coord</code> to expand extent if co is outside the current </code>Extent</code>
     */
    public void add(Coord co) {
        add(co.x, co.y);
    }
    /**
     * Adds the given <code>Extent</code> to expand extent if ex is bigger than the current </code>Extent</code>
     */
    public void add(Extent ex) {
        // error with this code,
        //add(ex.ll);
        //add(ex.ur);
        // replaced by this one:
        ll.x = (ex.ll.x < ll.x)? ex.ll.x : ll.x;
        ll.y = (ex.ll.y < ll.y)? ex.ll.y : ll.y;
        ur.x = (ex.ur.x > ur.x)? ex.ur.x : ur.x;
        ur.y = (ex.ur.y > ur.y)? ex.ur.y : ur.y;
    }
    /**
     * Sets this <code>Extent</code> to be the same as the given double values
     */
    public void reset(double X, double Y) {
        reset(new Coord(X,Y));
    }
    /**
     * Sets this <code>Extent</code> to be the same as the given <code>Coord</code>
     */
    public void reset(Coord co) {
        ll.x = co.x;
        ll.y = co.y;
        ur.x = co.x;
        ur.y = co.y;
    }
    /**
     * Resets this <code>Extent</code> and add co2 to it.
     * <br> This method gives this <code>Extent</code> the spatial extention of the given
     * <code>Coord</code>
     */
    public void reset(Coord co1, Coord co2) {
        reset(co1);
        add(co2);
    }
    /**
     * Returns the comma-separated String with the 4 coordinates of this extent
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(ll.x).append(",").append(ll.y).append(",").append(ur.x).append(",").append(ur.y);
        
        return buf.toString();
    }
    
    /**
     * Returns an Array of <code>double</code> representing the 4 coordinates of this <code>Extent</code>
     * @return a new array with 4 double values: index 0 is the x-coordinate of the lower-left corner,
     * index1 is the the y-coordinate of the lower-left corner, index 2 is the x-coordinate of the upper-right
     * corner, index 3 is the y-coordinate of the upper-right corner,
     */
    public double[] getExtentAsDouble() {
        double[] e = new double[4];
        e[0] = ll.x;
        e[1] = ll.y;
        e[2] = ur.x;
        e[3] = ur.y;
        return e;
    }
    
    /**
     *
     * Constructor from a BOX3D postgresql output
     * A BOX3D output has the following form:
     *     BOX3D(XMIN YMIN ZMIN, XMAX YMAX ZMAX)
     *
     * @param box3dStr BOX3D postgresql SELECT output
     *
     */
    public Extent(String box3dStr) {
        this(getExtentFromBOX3D(box3dStr));
    }
    
    
    /**
     *
     * Set the extent e to the current Extent
     *
     */
    public void set(Extent e) {
        this.ll = e.ll;
        this.ur = e.ur;
    }
    
    /**
     *
     * Checks if extent e overlap currentExtent
     *
     * @param e Extent to check overlapping with the current one
     *
     */
    public boolean overlap(Extent e) {
        return !( this.ur.x < e.ll.x ||
                e.ur.x < this.ll.x ||
                this.ur.y < e.ll.y ||
                e.ur.y < this.ll.y);
    }
    
    /**
     *
     * Checks if extent contains (x,y)
     *
     * @param x X map coordinate
     * @param y Y map coordinate
     *
     */
    public boolean contains(double x, double y) {
        return ( this.ur.x >= x  &&
                this.ll.x <= x  &&
                this.ur.y >= y  &&
                this.ll.y <= y);
    }
    
    
    /**
     *
     * Return the deltaX i.e. (xMax - xMin)
     *
     */
    public double dx() {
        return this.ur.x - this.ll.x;
    }
    
    
    /**
     *
     * Return the deltaY i.e. (yMax - yMin)
     *
     */
    public double dy() {
        return this.ur.y - this.ll.y;
    }
    
    
    /**
     *
     * Return an extent from a BOX3D postgresql
     * SELECT result
     * A BOX3D output has the following form:
     *     BOX3D(XMIN YMIN ZMIN, XMAX YMAX ZMAX)
     *
     * @param box3dStr BOX3D postgresql SELECT output
     *
     */
    public static Extent getExtentFromBOX3D(String box3dStr) {
        
        if (box3dStr == null) {
            return null;
        }
        
        // Only gets the string inside the BOX3D( ... ) string
        String str = box3dStr.substring(box3dStr.lastIndexOf("(") + 1, box3dStr.indexOf(")"));
        
        // Separate the two triplets of coordinates
        StringTokenizer tk = new StringTokenizer(str, ",");
        StringTokenizer tk1 = new StringTokenizer(tk.nextToken());
        StringTokenizer tk2 = new StringTokenizer(tk.nextToken());
        
        return new Extent(ConversionUtilities.stod(tk1.nextToken()),
                ConversionUtilities.stod(tk1.nextToken()),
                ConversionUtilities.stod(tk2.nextToken()),
                ConversionUtilities.stod(tk2.nextToken()));
    }
    
    /**
     *
     * Return an extent from a Cartoweb location.conf ini file:
     * "xmin, ymin, xmax, ymax" (with or without double quotesIfNeeded)
     *
     * @param cw3dStr Cartoweb3 extent string as expected in a location.ini
     *
     */
    public static Extent getExtentFromCW3(String cw3Str) {
        if (cw3Str == null) {
            return null;
        }
        
        // Separate the two triplets of coordinates
        StringTokenizer tk = new StringTokenizer(cw3Str.replace('"', ' ').trim(), ",");
        if (tk.countTokens() != 4) {
            return null;
        }
        return new Extent(ConversionUtilities.stod(tk.nextToken()),
                ConversionUtilities.stod(tk.nextToken()),
                ConversionUtilities.stod(tk.nextToken()),
                ConversionUtilities.stod(tk.nextToken()));
    }
    
    /**
     *
     * Return the Java Image coordinate equivalent
     * to the real coordinate x.
     *
     * @param size Width of the image
     * @param x Real coordinate
     *
     */
    public int mapCoordToX(int width, double x) {
        return (int) (((this.ll.x - x) * (1 - width)) / (this.dx()));
    }
    
    
    /**
     *
     * Return the Java Image coordinate equivalent
     * to the real coordinate y.
     *
     * @param size Height of the image
     * @param y Real coordinate
     *
     */
    public int mapCoordToY(int height, double y) {
        return (int) (((this.ur.y - y) * (height - 1)) / (this.dy()));
    }
    
    
    /**
     *
     * Return the map coordinate (in extent unit) corresponding
     * to the Java Image coordinate along the x axis
     *
     * @param width Width of the image (in pixels)
     * @param x Java Image x coordinate
     *
     */
    public double xToMapCoord(int width, int x) {
        return this.ll.x + (x * (this.dx())) / (width - 1);
    }
    
    
    /**
     *
     * Return the map coordinate (in extent unit) corresponding
     * to the Java Image coordinate along the y axis
     *
     * @param height Height of the image (in pixels)
     * @param y Java Image y coordinate
     *
     */
    public double yToMapCoord(int height, int y) {
        return this.ur.y + (y * (this.dy())) / (1 - height);
    }
    
    
    /**
     *
     * Return the width of the extent, in number
     * of pixels, for a given pixel width.
     *
     * @param pixDim Input given pixel width (in map units)
     *
     */
    public int getWidthInPixels(double pixWidth) {
        return (int) (this.dx() / pixWidth);
    }
    
    
    /**
     *
     * Return the height of the extent, in number
     * of pixels, for a given pixel height.
     *
     * @param pixDim Input given pixel height (in map units)
     *
     */
    public int getHeightInPixels(double pixHeight) {
        return (int) (this.dy() / pixHeight);
    }
    
    
    /**
     *
     * Return an awt polygon (in Image coordinate) from a WKT
     * representation of the polygon in map coordinate within
     * this extent.
     * If polygon contains holes, the exterior ring is taken
     *
     * @param WKTPolygon WKT representation for the polygon
     * @param size Dimension of the Image (in pixels)
     *
     */
    public Polygon getPolygonFromWKT(String WKTPolygon, Dimension size) {
        
        Polygon polygon = new Polygon();
        
        //
        // Parse the WKT representation
        //
        StringTokenizer stCoord;
        String str = WKTPolygon.substring(WKTPolygon.indexOf("("), WKTPolygon.indexOf(")") + 1);
        str = str.substring(str.lastIndexOf("(") + 1, str.lastIndexOf(")"));
        StringTokenizer tk = new StringTokenizer(str, ",");
        while (tk.hasMoreTokens()) {
            stCoord = new StringTokenizer(tk.nextToken());
            polygon.addPoint(this.mapCoordToX(size.width, ConversionUtilities.stod(stCoord.nextToken())),
                    this.mapCoordToY(size.height, ConversionUtilities.stod(stCoord.nextToken())));
        }
        
        return polygon;
    }
    
    
    /**
     *
     * Return an vector of polygons (in Image coordinate) from a WKT
     * representation of the multipolygon in map coordinate within
     * this extent.
     * If polygons contains holes, the exterior ring is taken
     * Done for tallage: not AT ALL optimized !!
     * Must do the job with a StreamTokenizer, in one pass
     *
     * @param WKTPolygon WKT representation for the multipolygon
     * @param size Dimension of the Image (in pixels)
     *
     */
    public java.util.Vector<Polygon> getMultiPolygonFromWKT(String WKTPolygon, Dimension size) {
        java.util.Vector<Polygon> res = new java.util.Vector<Polygon>();
        
        if (WKTPolygon.indexOf("MULTIPOLYGON") == -1 && WKTPolygon.indexOf("POLYGON") != -1) {
            // not a multi, call getPolygonFromWKT
            res.add(getPolygonFromWKT(WKTPolygon, size));
            return res;
        }
        
        //
        // Parse the WKT representation
        //
        // remove the multipolygon part.
        String str = WKTPolygon.substring(WKTPolygon.indexOf("("), WKTPolygon.lastIndexOf(")"));
        StringTokenizer stCoord = null;
        // current str subtract.
        int begin = 0, end = 0;
        begin = str.indexOf("((");
        
        if (begin == -1) {
            return null;
        } else {
            begin += 2;
        }
        
        String curPg = null;
        
        while (true) {
            end = begin;
            // the first closing parenthesis inside cur poly delimitates the first ring => exterior
            end = str.indexOf(")", end);
            // treats each polygon
            curPg = str.substring(begin, end);
            
            StringTokenizer tk = new StringTokenizer(curPg, ",");
            Polygon p = new Polygon();
            while (tk.hasMoreTokens()) {
                stCoord = new StringTokenizer(tk.nextToken());
                p.addPoint(this.mapCoordToX(size.width, ConversionUtilities.stod(stCoord.nextToken())),
                        this.mapCoordToY(size.height, ConversionUtilities.stod(stCoord.nextToken())));
            }
            res.add(p);
            // moves cursors to the next polygon
            begin = str.indexOf("((", end);
            
            if (begin == -1) {
                // end of job
                break;
            } else {
                begin += 2;
            }
        }
        
        return res;
    }
    
    
    /**
     *
     * Return the upper left Java Point of the input
     * extent within this extent
     *
     * @param extent Input extent
     * @param size Dimension of this extent
     *
     */
    public Point getULPoint(Extent extent, Dimension size) {
        
        // X
        int x = this.mapCoordToX(size.width, extent.ll.x);
        
        // Y
        int y = this.mapCoordToY(size.height, extent.ur.y);
        
        return new Point(x, y);
    }
    
    /**
     *
     * Return the Dimension of the input extent within this
     * extent as a reference
     *
     * @param extent Input extent
     * @param size Dimension of this extent
     *
     */
    public Dimension getRelativeSize(Extent extent, Dimension size) {
        
        // Calculate width
        int width = (int) ((extent.dx()) * size.width / (this.dx()));
        
        // Calculate height
        int height = (int) ((extent.dy()) * size.height / (this.dy()));
        
        return new Dimension(width, height);
    }
    
    /**
     *
     * Return a string version for this extent
     * in a mapserver cgi readable form
     *
     */
    public String toMSString() {
        return Double.toString(this.ll.x)+"+"+Double.toString(this.ll.y)+"+"+Double.toString(this.ur.x)+"+"+Double.toString(this.ur.y);
    }
    
    
    /**
     * Returns the space-separated <code>String</code> with the 4 coordinates of this <code>Extent</code>
     * <br>(Used mainly in the mapFile)
     */
    public String toMapFileString() {
        StringBuilder buf = new StringBuilder();
        buf.append(ll.x).append(" ").append(ll.y).append(" ").append(ur.x).append(" ").append(ur.y);
        
        return buf.toString();
    }
    
    /**
     * Returns the kaboum <code>String</code> with the 4 coordinates of this <code>Extent</code>
     * (x1,y1;x2,y2)
     *
     */
    public String toKaboumString() {
        StringBuilder buf = new StringBuilder();
        buf.append(ll.x).append(",").append(ll.y).append(";").append(ur.x).append(",").append(ur.y);
        
        return buf.toString();
    }
    
    /**
     * Returns the Cartoweb shortcuts.bbox string with the 4 coordinates of this <code>Extent</code>:<br/>
     * (x1,y1,x2,y2)
     *
     */
    public String toCartowebString() {
        StringBuilder buf = new StringBuilder();
        buf.append(ll.x).append(",").append(ll.y).append(",").append(ur.x).append(",").append(ur.y);
        
        return buf.toString();
    }
    
    /**
     * Getter method for struts to get the Kaboum Extent's Format string
     */
    public String getKaboumString(){
        return toKaboumString();
    }
    
    /**
     * Getter method for struts to get the Kaboum Extent's Format string
     */
    public String getMapfileString(){
        return toMapFileString();
    }
    
    /**
     * Getter method for struts to get the Openlayers' Format string
     */
    public String getBounds(){
        StringBuilder buf = new StringBuilder();
        buf.append(ll.x).append(",").append(ll.y).append(",").append(ur.x).append(",").append(ur.y);
        
        return buf.toString();
    }
    
    /**
     *
     * Return a BOX3D string version
     * for this extent
     *
     */
    public String box3DString() {
        return "BOX3D("+Double.toString(this.ll.x)+" "+Double.toString(this.ll.y)+" 0, "+Double.toString(this.ur.x)+" "+Double.toString(this.ur.y)+" 0)";
    }
    
}

