package org.kaboum;

/*
 * Class KaboumSurfaceOpMode from the Kaboum project.
 * Calculate surface represented by the drawn fence, in the square map units
 *<p>
 *based on KaboumDistanceOpMode
 *</p>
 *
 * Created on 21 septembre 2005, 18:02
 *@author Nicolas Ribot
 */

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumUtil;


/**
 *
 * This opMode is used to calculate distance
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumSURFACEOpMode extends KaboumOpMode {
    
    /** Constant */
    public static final String PARAM_FOREGROUNDCOLOR = "SURFACE_FOREGROUND_COLOR";
    public static final String PARAM_POINTTYPE = "SURFACE_POINT_TYPE";
    public static final String PARAM_POINTHEIGHT = "SURFACE_POINT_HEIGHT";
    public static final String PARAM_POINTWIDTH = "SURFACE_POINT_WIDTH";
    
    public static String[] getParametersList() {
        String[] list = new String[4];
        list[0] = PARAM_FOREGROUNDCOLOR;
        list[1] = PARAM_POINTTYPE;
        list[2] = PARAM_POINTHEIGHT;
        list[3] = PARAM_POINTWIDTH;
        return list;
    }
    
    /** Parent reference */
    private Kaboum parent;
    
    /** Anchor for the first point clicked */
    private Point anchor = null;
    
    /** Surface */
    protected double surface = 0;
    
    /** Frozen state */
    private boolean freezed = false;
    
    
    /**
     *
     * Vector containing the cliqued points.
     * Coordinates are stored in internal representation
     *
     * N.B. : this vector is static, so changing opMode
     * does not erase the current surface area
     *
     */
    private static Vector path = new Vector();
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumSURFACEOpMode(Kaboum parent) {
        
        this.parent = parent;
        this.surface = 0;
        this.mp = new Point(-100, -100);
        
        // Ajoute les listeners au parent
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
        
        int numPointsMinusOne = KaboumSURFACEOpMode.path.size() - 1;
        
        // Check for a non-empty preexisting vector path
        if (numPointsMinusOne > -1) {
            this.anchor = this.parent.mapServerTools.internalToMouseXY(
                    (KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(numPointsMinusOne));
            for (int i = 1; i < numPointsMinusOne; i++) {
                this.surface = this.surface + (this.parent.mapServerTools.getSurface(
                        (KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(0),
                        (KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(i),
                        (KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(i + 1))) / 2;
            }
        }
        parent.setCursor("CROSSHAIR");
    }
    
    
    /*
     *
     * PAINT
     *
     */
    public void paint(Graphics g) {
        
        double tmpSurface = 0;
        
        g.setColor(KaboumUtil.getColorParameter(this.parent.getOpModeProperty(PARAM_FOREGROUNDCOLOR), Color.black));
        
        //
        // DRAW THE FENCE
        //
        int numPoints = KaboumSURFACEOpMode.path.size();
        Point pointA = null;
        Point pointB = null;
        Point first = null;
        for (int i = 0; i < numPoints - 1; i++) {
            pointA = this.parent.mapServerTools.internalToMouseXY((KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(i));
            pointB = this.parent.mapServerTools.internalToMouseXY((KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(i + 1));
            
            g.drawLine(pointA.x, pointA.y, pointB.x, pointB.y);
        }
        // draw last segment if tool is freezed
        if (freezed && numPoints > 0) {
            first = this.parent.mapServerTools.internalToMouseXY((KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(0));
            Point last = this.parent.mapServerTools.internalToMouseXY((KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(numPoints - 1));
            g.drawLine(last.x, last.y, first.x, first.y);
        }
        
        // Draw the vertices
        int type = KaboumCoordinate.stoi(this.parent.getOpModeProperty(PARAM_POINTTYPE));
        int pointHeight = KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_POINTHEIGHT), 5);
        int pointWidth = KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_POINTWIDTH), 5);
        
        for (int i = 0; i < numPoints; i++) {
            this.paintVertices(
                    this.parent.mapServerTools.internalToMouseXY((KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(i)),
                    type,
                    pointHeight,
                    pointWidth,
                    g);
        }
        
        // draw the surface label only if there is at least 3 points
        // and the control list is not frozen
        if (this.anchor != null && !this.freezed) {
            
            // Draw the current point position
            pointA = this.parent.mapServerTools.internalToMouseXY(
                    (KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(numPoints - 1));
            g.drawLine(pointA.x, pointA.y, this.mp.x, this.mp.y);
            //closes the fence
            first = this.parent.mapServerTools.internalToMouseXY((KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(0));
            g.drawLine(this.mp.x, this.mp.y, first.x, first.y);
            this.paintVertices(this.mp, type, pointHeight, pointWidth, g);
            
            KaboumCoordinate coordA = this.parent.mapServerTools.mouseXYToInternal(this.anchor.x, this.anchor.y);
            KaboumCoordinate coordB = this.parent.mapServerTools.mouseXYToInternal(this.mp.x, this.mp.y);
            KaboumCoordinate coordC = (KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(0);
            
            tmpSurface = this.surface + (this.parent.mapServerTools.getSurface(coordA, coordB, coordC) / 2);
            
            String tmpStr = "s = " + this.parent.mapServerTools.pm.areaToString(Math.abs(tmpSurface)) + 
                    this.parent.mapServerTools.pm.sUnit;
            int tmpWidth = g.getFontMetrics().stringWidth(tmpStr);
            int tmpHeight = g.getFontMetrics().getHeight();
            
            // revert the surface drawing if label is outside the applet
            int shiftX = -15;
            int shiftY = -15;
            int xbox = this.mp.x + shiftX + tmpWidth;
            int ybox = this.mp.y + shiftY - tmpHeight;
            
            if (xbox > parent.screenSize.width) {
                // box_width negative shift
                shiftX = - tmpWidth;
            }
            if (ybox < 0) {
                // positive shift
                shiftY = 30;
            }
            
            
            g.setColor(Color.black);
            g.fillRect(this.mp.x + shiftX ,
                    (int) this.mp.y + shiftY - tmpHeight,
                    tmpWidth,
                    tmpHeight);
            g.setColor(Color.white);
            g.drawString(tmpStr, this.mp.x + shiftX, this.mp.y + shiftY - 3);
        }
    }
    
    public void  destroyEvent() {
        parent.removeMouseListener(this);
        parent.removeMouseMotionListener(this);
    }
    
    
    public void mouseDragged(MouseEvent evt) {
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(evt.getX(), evt.getY()));
    }
    
    
    public void mouseReleased(MouseEvent evt) { }
    
    
    public void mouseMoved(MouseEvent evt) {
        this.mp = evt.getPoint();
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(evt.getX(), evt.getY()));
        this.parent.repaint();
    }
    
    
    public void mouseClicked(MouseEvent evt) {
        
        int mods = evt.getModifiers();
        
        Point tmpAnchor = evt.getPoint();
        
        if (this.anchor != null) {
            KaboumCoordinate coordA = (KaboumCoordinate) KaboumSURFACEOpMode.path.elementAt(0);
            KaboumCoordinate coordB = this.parent.mapServerTools.mouseXYToInternal(this.anchor.x, this.anchor.y);
            KaboumCoordinate coordC = this.parent.mapServerTools.mouseXYToInternal(tmpAnchor.x, tmpAnchor.y);
            this.surface += this.parent.mapServerTools.getSurface(coordA, coordB, coordC) / 2;
        }
        
        this.anchor = tmpAnchor;
        
        if ((mods & InputEvent.BUTTON1_MASK) != 0) {
            // Nico: if geoObject is frozen: no more action on click: must clean the opMode
            // to reinit a distance mode
            if (this.freezed) {
                return;
            }
            
            KaboumCoordinate internal = this.parent.mapServerTools.mouseXYToInternal(this.anchor.x, this.anchor.y);
            
            // Add this coordinate to the path vector
            KaboumSURFACEOpMode.path.addElement(internal);
            
            // deals with the double click: stop the drawing without erasing the polygon
            if (evt.getClickCount() == 2) {
                this.freezed = true;
                parent.kaboumResult("SURFACE|" + this.parent.mapServerTools.pm.areaToString(Math.abs(this.surface)));
                return;
            }
        } else {
            KaboumSURFACEOpMode.path.removeAllElements();
            this.destroyEvent();
            this.parent.kaboumResult("SURFACE|");
            this.parent.respawnOpMode();
        }
    }
   

    /**
     *
     * Paint one vertice
     *
     */
    private void paintVertices(Point point, int type, int pointHeight, int pointWidth, Graphics g) {
        
        switch (type) {
            case KaboumCoordinate.K_TYPE_CIRCLE:
                g.drawOval((int)(point.x-pointWidth/2), (int)(point.y-pointHeight/2), pointWidth, pointHeight);
                break;
            case KaboumCoordinate.K_TYPE_BOX:
                g.fillRect((int)(point.x-pointWidth/2), (int)(point.y-pointHeight/2), pointWidth, pointHeight);
                break;
            case KaboumCoordinate.K_TYPE_POINT:
                g.drawOval(point.x, point.y, 1, 1);
                break;
                // Default is K_TYPE_POINT
            default:
                g.drawOval(point.x, point.y, 1, 1);
        }
    }
}
