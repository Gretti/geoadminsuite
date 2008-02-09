/*
 * RotablePolygon.java
 *
 * Created on 18 mars 2002, 14:06
 */

package org.geogurus.tools.graphics;
import java.awt.Point;
import java.awt.Polygon;

/** A rotable polygon is a polygon with rotation methods
 * @author Bastien VIALADE
 */
public class RotablePolygon extends java.awt.Polygon {
    
    /** Creates a new instance of RotablePolygon */
    public RotablePolygon() {
        super();
    }
    
    /** Constructs and initializes a RotablePolygon from the specified parameters
     * @param xpoints an array of x coordinates
     * @param ypoints an array of y coordinates
     * @param npoints the total number of points in the RotablePolygon
     */    
    public RotablePolygon(int[] xpoints, int[] ypoints, int npoints) {
        super(xpoints,ypoints,npoints);
    }
    
    /** Rotates a current polygon in to another given one.
     * @param center_ Rotation center point
     * @param poly Polygon rotated. Its informations are updated based on current polygon
     * @param angle Rotation angle. Finaly <I>poly</I> will be rotated of <I>angle</I> from <B>polyOriginal</B>
     * @return The rotated polygon
     */
    public RotablePolygon rotatePoly(Point center_, RotablePolygon poly, double angle) {
        double cos_ang = Math.cos( angle );
        double sin_ang = Math.sin( angle );
        int cx = center_.x;
        int cy = center_.y;
        poly.invalidate();
        // If null polygon given, creates a new one
        if (poly==null) poly = new RotablePolygon(this.xpoints, this.ypoints, this.npoints);
        for (int i=0; i < poly.npoints; i++) {
            int x  = this.xpoints[i] - cx;
            int y  = this.ypoints[i] - cy;
            poly.xpoints[i] = (int)( x*cos_ang  -  y*sin_ang ) + cx;
            poly.ypoints[i] = (int)( x*sin_ang  +  y*cos_ang ) + cy;
        }
        return poly;
    }
       
    /** Rotates a current polygon in to another given one.
     * @param center_ Rotation center point
     * @param angle Rotation angle. Finaly <I>poly</I> will be rotated of <I>angle</I> from <B>polyOriginal</B>
     * @return The rotated polygon
     */
    public RotablePolygon rotatePoly(Point center_, double angle) {
        return this.rotatePoly(center_, null, angle);
    }
    
    /** Rotates a current polygon in itself.
     * @param center_ Rotation center point
     * @param angle Rotation angle. Finaly <I>poly</I> will be rotated of <I>angle</I> from <B>polyOriginal</B>
     */
    public void rotate(Point center_, double angle) {
        this.rotatePoly(center_, this, angle);
    }
    
    /**
     * Invalidates or flushes any internally-cached data that depends
     * on the vertex coordinates of this <code>Polygon</code>.
     * This method should be called after any direct manipulation
     * of the coordinates in the <code>xpoints</code> or
     * <code>ypoints</code> arrays to avoid inconsistent results
     * from methods such as <code>getBounds</code> or <code>contains</code>
     * that might cache data from earlier computations relating to
     * the vertex coordinates.
     */
    public void invalidate() {
        this.bounds = null;
    }
 }

