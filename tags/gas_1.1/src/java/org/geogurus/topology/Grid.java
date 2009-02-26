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

package org.geogurus.topology;

import java.awt.geom.Point2D;
import java.util.Vector;

import org.geogurus.data.Extent;


/**
 *
 * Title:
 * Description:  Create a rotable grid
 * @author Jerome Gasperi aka jrom
 * @version 1.0
 *
 */
public class Grid extends Vector {
    
    /** Extent of the grid */
    private Extent extent = new Extent();
    
    /** Point origin (in extent coordinates) */
    private Point2D.Double origin;
    
    /** Origin angle of the grid */
    private double angle = 0;
    
    /** Step of the grid (distance between two line grid (in extent coordinate)) */
    private double step = 30;

    /** Arrays describing points of the grid to display */
    private double[] xh;
    private double[] yh;

    /** Arrays describing points of the grid to display */
    private double[] xOrigin;
    private double[] yOrigin;

    /**
     *
     * Creates a new instance of Grid
     *
     * @param extent Spatial extent that must be covered by the grid
     * @param origin Point origin from which the grid must pass
     * @param angle Trigonometrical angle between the grid and the East-West
     * direction (in degrees)
     * @param step Distance between two lines of the grid
     * 
     */
    public Grid(Extent extent, Point2D.Double origin, double angle, double step) {
        this.extent = extent;
        this.origin = origin;
        this.angle = angle;
        this.step = step;
        if (this.init() != -1) {
            this.rotate(this.angle);
            this.createWKT();
        }
    }

    
    /**
     *
     * Set the step of the grid
     *
     */
    public void setStep(double step) {
        this.step = step;
        this.init();
        this.rotate(this.angle);
        this.createWKT();
    }
    

   /**
     *
     * Set the angle of the grid
     *
     */
    public void setAngle(double angle) {
        this.angle = angle;
        System.arraycopy(xOrigin,0,xh,0,xOrigin.length);
        System.arraycopy(yOrigin,0,yh,0,yOrigin.length);
        this.rotate(this.angle);
        this.createWKT();
    }

    
    /**
     *
     * Creates points arrays representing grid to display.
     *
     */
    private int init() {
        
        if (this.step <= 0) {
            return -1;
        }
        
        double gridSize = Math.max(this.extent.dx(), this.extent.dy());

        // 
        // The grid square size must be of twice the max
        // between width and height extent.
        //
        int lines = (int) (4 * gridSize / this.step);
        
        //
        // The grid is centered on the point origin.
        //
        double x = this.origin.x - ( 2 * gridSize);
        double y = this.origin.y - ( 2 * gridSize);
        Point2D.Double gridOrigin = new Point2D.Double(x, y);

        // Number of points for polyline horizontal display
        int nbPointsH = 2 * ( 1 + lines);

        xh = new double[nbPointsH];
        yh = new double[nbPointsH];
        
        double stepH = 0; // Vertical height step from the beginning
        
        // Fills the horizontal polyline
        for (int i = 0; i<nbPointsH; i+=4) {
            xh[i] = gridOrigin.x + 0;
            yh[i] = gridOrigin.y + stepH;
            xh[i+1] = gridOrigin.x + (4 * gridSize);
            yh[i+1] = gridOrigin.y + stepH;
            stepH += this.step;
            // Break if no more points to calculate !
            if ((i+2) >= nbPointsH) break;
            xh[i+2] = gridOrigin.x + (4 * gridSize);
            yh[i+2] = gridOrigin.y + stepH;
            xh[i+3] = gridOrigin.x + 0;
            yh[i+3] = gridOrigin.y + stepH;
            stepH += this.step;
        } 
        // Keep an original copy of this grid never rotated
        // (reused for prevent rotation distorsions)
        xOrigin = new double[nbPointsH];
        System.arraycopy(xh,0,xOrigin,0,xh.length);
        yOrigin = new double[nbPointsH];
        System.arraycopy(yh,0,yOrigin,0,yh.length);
        
        return 1;
    }
    
    
    /**
     *
     * Translate the grid by (x, y) meters
     *
     */
    private void translate(double x, double y){
        
        if (this.xh == null) {
            return;
        }
        
        for (int i = 0; i < xh.length; i++) {
            this.xh[i] += x;
            this.yh[i] += y;
        }
        
    }
    
    
    /**
     *
     * Converts a degree to a radiant.
     * @param degree Degree to convert
     * @return The radian value
     *
     */
    private static double degToRad(double degree) {
        return (degree * Math.PI) / 180.0;
    }
    
    
    /**
     *
     * Rotates a given array of points in to another given one.
     *
     * @param angle Rotation angle.
     *
     */
    public void rotate(double angle) {
        
        double cos_ang = Math.cos(this.degToRad(angle));
        double sin_ang = Math.sin(this.degToRad(angle));
        double cx = this.origin.x;
        double cy = this.origin.y;
        
        for (int i = 0; i < this.xh.length; i++) {
            double x  = this.xOrigin[i] - cx;
            double y  = this.yOrigin[i] - cy;
            xh[i] = ( x*cos_ang  -  y*sin_ang ) + cx;
            yh[i] = ( x*sin_ang  +  y*cos_ang ) + cy;
        }
    }    

    
    /**
     *
     * Create the WKT representation of each polygons formed
     * by the grid
     *
     */
    private void createWKT() {
        
        // Clean the current polygon objects
        this.removeAllElements();
        
        // Instantiate the string buffer
        StringBuffer sb = new StringBuffer();
        
        // Populate the Grid object with the new polygons
        for (int i = 0; i < this.xh.length; i+=2) {
            sb = new StringBuffer("POLYGON((");
            sb.append(this.xh[i] + " " + this.yh[i] + ",");
            if (this.xh.length < (i + 3)) {
                break;
            }
            sb.append(this.xh[i+1] + " " + this.yh[i+1] + ",");
            sb.append(this.xh[i+2] + " " + this.yh[i+2] + ",");
            sb.append(this.xh[i+3] + " " + this.yh[i+3] + ",");
            sb.append(this.xh[i] + " " + this.yh[i]);
            sb.append("))");
            this.addElement(sb.toString());
        }
        
    }
}
