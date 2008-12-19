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

package org.geogurus.tools.graphics;
/*
 * FreeArrowCanvas.java
 *
 * Created on 11 mars 2002, 17:10
 */

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * This canvas draws an arrow and its associated grid.
 * It can be translated or rotated, depending on selected zone.
 * Use is very simple:
 * Just add this canvas to your applet or graphic component.
 * ex: add(new FreeArrowCanvas(p, 1.2, Color.blue));
 *
 *
 * @author Bastien VIALADE
 */


public class FreeArrowCanvas extends java.awt.Canvas implements java.awt.event.MouseListener, java.awt.event.MouseMotionListener {
    /** Multiplicator factor for arrow display */
    double 	hmult = 0;
    /** Polygon with no rotation distorsions */
    private RotablePolygon baseOriginal=null;
    private RotablePolygon picOriginal=null;
    /** Displayed polygon */
    private RotablePolygon base=null;
    private RotablePolygon pic=null;
    /** last clicked point */
    int xprev =-1;
    int yprev =-1;
    /** Arrow center - Center used for rotations */
    Point center = null;
    /** Display only per step degrees */
    int step;
    /** Is it rotation mode ? True if mouse clicked on pic zone
     */
    boolean rotationMode =false;
    /** Is it translation mode ? True if mouse clicked on base zone
     */
    boolean translationMode =false;
    
    /** Angle evolution from the original position */
    int angleFromBegining = 0;
    /** Position where to display arrow at the begining
     */
    Point arrowOrigin;
    /** Position where grid is begining
     */
    Point gridOrigin;
    /** The given arrow color
     */
    Color arrowColor;
    /** Specifies if the associated grid has to be displayed
     */
    boolean isGridDisplayed;
    /** The given step between 2 lines of the grid
     */
    int stepGrid;
    /** The given color grid
     */
    Color colorGrid;
    /** Arrays describing points of the grid to display
     */
    int[] xh;
    int[] yh;
    /** Arrays describing points of the reference grid
     */
    int[] xOriginH;
    int[] yOriginH;
    /** Horizontal grid's points number
     */
    int nbPointsH;
    /** Grid dimension
     */
    int height;
    int width;
    int stepGridOrigin;
    
    
    /** Full Constructor.
     * Creates a new arrow canvas with needed parameters,
     * and specifies grid parameters.
     * @param origin Point where to display arrow
     * @param mult Mulplication factor. With it arrow size is free.
     * @param color The arrow color
     * @param displayGrid Is the grid displayed ?
     * @param stepGrid_ Pixels between lines.
     * @param colorGrid_ The grid color
     */
    public FreeArrowCanvas(Point origin, double mult, Color color, boolean displayGrid, int stepGrid_, Color colorGrid_, int height_, int width_) {
        hmult = mult;
        arrowColor = color;
        arrowOrigin = origin;
        height = height_;
        width = width_;
        gridOrigin = new Point(-width,-height);
        step = 4;
        isGridDisplayed = displayGrid;
        stepGridOrigin = stepGrid_;
        stepGrid = (int)(stepGrid_*mult);
        colorGrid = colorGrid_;
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    /**
     * Constructor.
     * Creates a new arrow canvas with needed parameters.
     *
     * @param origin Point where to display arrow
     * @param mult Mulplication factor. With it arrow size is free.
     * @param color The arrow color
     */
    public FreeArrowCanvas(Point origin, double mult, Color color) {
        this(origin, mult, color, false, 15, Color.black, 400, 400) ;
    }
    
    
    /** Returns if the grid grid is currently displayed.
     * @return the displaying state of the grid
     */
    public boolean isGridDisplayed() {
        return isGridDisplayed;
    }
    
    /** Display or not the grid
     * @param visible True to display the grid
     */
    public void setGridVisible(boolean visible) {
        isGridDisplayed = visible;
        repaint();
    }
    
    /** Gets the current angle done by the arrow with horizontal origin.
     * @return Arrow angle with horizontal origin
     */
    public int getCurrentAngle() {
        if (angleFromBegining<0)
            return (360+(angleFromBegining%360));
        else return(angleFromBegining%360);
    }
    
    /** Returns the position of the arrow.
     * @return Pic arrow position
     */
    public Point getPosition() {
        return new Point(pic.xpoints[2], pic.ypoints[2]);
        
    }
    
    /** Change the multiplicity factor for this arrow
     * @param mult New multiplicity factor
     */
    public void setMultiplicityFactor(double mult) {
        if (mult>0) {
            hmult = mult;
            // Rotations from begining
            int currentBaseX = baseOriginal.xpoints[0];
            int currentBaseY = baseOriginal.ypoints[0];
            double radAngle = degToRad(angleFromBegining);
            
            // Creates a new arrow and sets the pixels between line the same as the arrow basis width
            initArrow();
            // Translation still done from origin
            int deltaX = currentBaseX - arrowOrigin.x;
            int deltaY = currentBaseY - arrowOrigin.y ;
            
            setStepGrid((int)(Math.abs(baseOriginal.ypoints[0]-baseOriginal.ypoints[1])));
            base.translate(deltaX,deltaY);
            pic.translate(deltaX,deltaY);
            center.translate(deltaX,deltaY);
            baseOriginal.translate(deltaX,deltaY);
            picOriginal.translate(deltaX,deltaY);
            adjustArrow();
            picOriginal.rotatePoly(center, pic, radAngle) ;
            baseOriginal.rotatePoly(center, base, radAngle) ;
            rotatePolyline(center,xOriginH,yOriginH,xh,yh,nbPointsH,radAngle);
            repaint();
        }
    }
    
    /** Changes the arrow color
     * @param color New arrow color
     */
    public void setArrowColor(Color color) {
        arrowColor = color;
    }
    
    /** Changes degree step. Default value is 4 (For rotation jump 4� per 4�)
     *
     * @param step_ New degree step
     */
    public void setDegreeStep(int step_) {
        step = step_ ;
    }
    
    /** Changes the space between 2 lines of the grid
     *
     * @param pixels New space
     */
    public void setStepGrid(int pixels) {
        if (pixels>0) {
            stepGrid = pixels;
            initGrid();
            double radAngle = degToRad(angleFromBegining);
            rotatePolyline(center,xOriginH,yOriginH,xh,yh,nbPointsH,radAngle);
        }
    }
    
    /** Creates polygons representing arrow to display.
     * NOTE: Arrow is created with 2 polygons: Base and Pic
     * Each polygon is clone. One to keep a polygon with no rotation distorsion,
     * and this other to display the rotated polygon.
     */
    private void initArrow() {
        xprev = arrowOrigin.x;
        yprev = arrowOrigin.y;
        java.awt.Dimension dim = new java.awt.Dimension((int)(15*hmult),(int)(10*hmult));
        // Creates arrow basis:
        baseOriginal = new RotablePolygon();
        baseOriginal.addPoint(arrowOrigin.x,arrowOrigin.y);
        baseOriginal.addPoint(arrowOrigin.x,(arrowOrigin.y+dim.height));
        baseOriginal.addPoint((arrowOrigin.x+dim.width),(arrowOrigin.y+dim.height));
        baseOriginal.addPoint((arrowOrigin.x+dim.width),arrowOrigin.y);
        base = new RotablePolygon(baseOriginal.xpoints, baseOriginal.ypoints, baseOriginal.npoints);
        base = new RotablePolygon(baseOriginal.xpoints, baseOriginal.ypoints, baseOriginal.npoints);
        // Creates arrow extremity: |  >
        picOriginal = new RotablePolygon();
        picOriginal.addPoint(arrowOrigin.x+dim.width-1,arrowOrigin.y-(int)(dim.height/2));
        picOriginal.addPoint(arrowOrigin.x+dim.width-1,arrowOrigin.y+(int)(1.5*dim.height));
        picOriginal.addPoint(arrowOrigin.x+(int)(1.5*dim.width),arrowOrigin.y+(int)(dim.height/2));
        pic = new RotablePolygon(picOriginal.xpoints, picOriginal.ypoints, picOriginal.npoints);
        // Creates the arrow center point used for rotations
        center = new Point();
        center.x = arrowOrigin.x+(int)(0.75*dim.width);
        center.y = arrowOrigin.y+(int)(dim.height/2);
        repaint();
    }
    
    /** Creates points arrays representing grid to display.
     */
    public void initGrid() {
        int lines = (int) (3*height/stepGrid);
        // Number of points for polyline horizontal display
        nbPointsH = 2*(1+lines);
        xh= new int[nbPointsH];
        yh= new int[nbPointsH];
        
        int stepH=0; // Vertical height step from the beginning
        // Fills the horizontal polyline
        for (int i=0; i<nbPointsH; i+=4) {
            xh[i] = gridOrigin.x+0;
            yh[i] = gridOrigin.y+stepH;
            xh[i+1] = gridOrigin.x+3*width;
            yh[i+1] = gridOrigin.y+stepH;
            stepH+=stepGrid;
            // Break if no more points to calculate !
            if ((i+2)>=nbPointsH) break;
            xh[i+2] = gridOrigin.x+3*width;
            yh[i+2] = gridOrigin.y+stepH;
            xh[i+3] = gridOrigin.x+0;
            yh[i+3] = gridOrigin.y+stepH;
            stepH+=stepGrid;
        }
        // Keep an original copy of this polyline never rotated
        // (reused for prevent rotation distorsions)
        xOriginH = new int[nbPointsH];
        System.arraycopy(xh,0,xOriginH,0,xh.length);
        yOriginH = new int[nbPointsH];
        System.arraycopy(yh,0,yOriginH,0,yh.length);
    }
    
    
    /** Adjusts the arrow to lines after zooming
     */
    private void adjustArrow() {
        // Displays the arrow in the center of a band.
        // Finds the nearest line of the arrow
        int yArrow = baseOriginal.ypoints[0];
        int deltaY = 0;
        // There is 2 points per line so
        for (int i=0; i<nbPointsH; i++) {
            if (yOriginH[i]>yArrow) {
                deltaY = yOriginH[i] - yArrow ;
                break;
            }
        }
        // Delta defined, so let's translate arrows (original and displaying)
        baseOriginal.translate(0,deltaY);
        base.translate(0,deltaY);
        picOriginal.translate(0,deltaY);
        pic.translate(0,deltaY);
        center.translate(0,deltaY);
    }
    
    
    /** Not used
     */
    public void mouseClicked(MouseEvent e) {
    }
    
    /** Set the current mode to rotation if user click in the pic zone.
     * or translation if it's the base zone.
     * Then, all mouse directions will representing a rotation,
     * as if mouse is not in the pic zone or a translation.
     */
    public void mousePressed(MouseEvent e) {
        xprev = e.getX();
        yprev = e.getY();
        if (pic.contains(xprev,yprev)) {
            rotationMode=true;
        } else if (base.contains(xprev,yprev)) {
            translationMode=true;
        }
    }
    
    /**
     */
    public void mouseReleased(MouseEvent e) {
        rotationMode=false;
        translationMode=false;
        e.consume();
    }
    
    /** Not used
     */
    public void mouseEntered(MouseEvent e) {
        e.consume();
    }
    
    /** Not used
     */
    public void mouseExited(MouseEvent e) {
    }
    
    /** Mouse is dragged so done and display wanted action: Translation or rotation
     */
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (rotationMode) {
            if (xprev<x) {
                angleFromBegining -= step;
            } else angleFromBegining += step;
            double radAngle = degToRad(angleFromBegining);
            picOriginal.rotatePoly(center, pic, radAngle) ;
            baseOriginal.rotatePoly(center, base, radAngle) ;
            rotatePolyline(center,xOriginH,yOriginH,xh,yh,nbPointsH,radAngle);
            xprev = x;
            yprev = y;
            repaint();
        } else if (translationMode) {
            // Can't translate over defined dimensions
            if ((x>width)||(y>height)||(x<0)||(y<0)) { e.consume(); return ; }
            // Calculate the new mouse position and drag
            int deltaX = x - xprev;
            int deltaY = y - yprev;
            baseOriginal.translate(deltaX, deltaY);
            picOriginal.translate(deltaX, deltaY);
            base.translate(deltaX, deltaY);
            pic.translate(deltaX, deltaY);
            center.translate(deltaX, deltaY);
            translatePolylign(xh,yh,nbPointsH,deltaX,deltaY);
            translatePolylign(xOriginH,yOriginH,nbPointsH,deltaX,deltaY);
            xprev = x;
            yprev = y;
            repaint();
        }
        e.consume();
    }
    
    private void translatePolylign(int[] xPoints, int[] yPoints, int nbPoints, int deltaX, int deltaY){
        for (int i = 0; i < nbPoints; i++) {
            xPoints[i] += deltaX;
            yPoints[i] += deltaY;
        }
    }
    
    /** Not used
     */
    public void mouseMoved(MouseEvent e) {
    }
    
    
    /** Converts a degree to a radiant.
     * @param degree Degree to convert
     * @return The radian value
     */
    private static double degToRad(double degree) {
        return (degree * Math.PI) / 180.0;
    }
    
    
    /** Rotates a given array of points in to another given one.
     * @param center_ Rotation center point
     * @param coordOriginX X points array used to extract information from. Don't rotate it.
     * @param coordOriginY Y points array used to extract information from. Don't rotate it.
     * @param coordX Rotated points. Array points informations are updated based on original X array points
     * @param coordY Rotated points. Array points informations are updated based on original Y array points
     * @param nbPoints Points number to rotate
     * @param angle Rotation angle.
     */
    public void rotatePolyline(Point center_, int[] coordOriginX, int[] coordOriginY, int[] coordX, int[] coordY, int nbPoints, double angle) {
        double cos_ang = Math.cos( angle );
        double sin_ang = Math.sin( angle );
        int cx = center_.x;
        int cy = center_.y;
        for(int i=0; i < nbPoints; i++) {
            int x  = coordOriginX[i] - cx;
            int y  = coordOriginY[i] - cy;
            coordX[i] = (int)( x*cos_ang  -  y*sin_ang ) + cx;
            coordY[i] = (int)( x*sin_ang  +  y*cos_ang ) + cy;
        }
    }
    
    
    
    /** Display polygons that represents arrow form
     * @param g inner graphics
     */
    public void paint(java.awt.Graphics g) {
        if (base==null) {
            initArrow();
            initGrid();
            adjustArrow();
        } else {
            if (isGridDisplayed) {
                g.setColor(colorGrid);
                g.drawPolyline(xh,yh,nbPointsH);
            }
            g.setColor(arrowColor);
            g.fillPolygon(base);
            g.fillPolygon(pic);
        }
    }
}

