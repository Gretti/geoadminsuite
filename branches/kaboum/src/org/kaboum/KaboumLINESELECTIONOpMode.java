/*
 *
 * Class KaboumLINESELECTIONOpMode from the Kaboum project.
 * Return the coordinates of the two vertices of a line.
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


package org.kaboum;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;

import org.kaboum.util.KaboumUtil;
import org.kaboum.util.KaboumExtent;


/**
 *
 * Send the LowerLeft and Upper Right coordinates of a dragging box
 *
 * @author Jerome Gasperi aka jrom
 */
public class KaboumLINESELECTIONOpMode extends KaboumOpMode {
    
    public static final String PARAM_FOREGROUNDCOLOR = "LINESELECTION_FOREGROUND_COLOR";
    public static String[] getParametersList() {
        String[] list = new String[1];
        list[0] = PARAM_FOREGROUNDCOLOR;
        return list;
    }
    
    /** UL X image coordinate */
    protected int x = 0;
    
    /** UL Y image coordinate */
    protected int y = 0;
    
    /** Width of the dragging box */
    protected int width = 0;
    
    /** Height of the dragging box */
    protected int height = 0;
    
    /** True is dragging box is initialised */
    protected boolean boxInit = false;
    
    /** Parent reference */
    private Kaboum parent;
    
    /** Reference point */
    protected Point refPoint = null;
    
    /** End point */
    protected Point endPoint = null;
    
    /** Dummy test */
    private boolean dummyTest = false;   
    
    private Point tmpStartPoint = null;
    private Point tmpEndPoint = null;
    private boolean isReleased = false;
    
    
    /**
     *
     * Constructor.
     *
     * @param parent Parent
     *
     */
    public KaboumLINESELECTIONOpMode(Kaboum parent) {
        
        // Initialisation
        this.parent = parent;
        
        // Ajout des Listeners au parent
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
        
        // Definit la forme du curseur de la souris
        parent.setCursor("CROSSHAIR");
        
        parent.repaint();
    }
        
    
    /*
     *
     * PAINT
     *
     */
    public void paint(Graphics g) {
        if (this.refPoint == null || this.endPoint == null) {
            return;
        }
        g.setColor(KaboumUtil.getColorParameter(this.parent.getOpModeProperty(PARAM_FOREGROUNDCOLOR), Color.black));        
        g.drawLine(refPoint.x, refPoint.y, endPoint.x, endPoint.y);
        
        // Paint the oriented arrow
        if (isReleased) {
            double hypotenus = Math.sqrt(Math.pow(tmpEndPoint.x - tmpStartPoint.x, 2) + Math.pow(tmpEndPoint.y - tmpStartPoint.y, 2)); 
            double sinAlpha = (double) (tmpEndPoint.y - tmpStartPoint.y) / hypotenus;
            double cosAlpha = (double) (tmpEndPoint.x - tmpStartPoint.x) / hypotenus;
            //System.out.println("SIN " + sinAlpha);
            //System.out.println("COS " + cosAlpha);
            
            int value = 3;
            
            if (tmpEndPoint.x == tmpStartPoint.x) {
                if (tmpEndPoint.y > tmpStartPoint.y) {
                    g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x - value, tmpEndPoint.y - value); 
                    g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x + value, tmpEndPoint.y - value); 
                }
                else {
                    g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x - value, tmpEndPoint.y + value); 
                    g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x + value, tmpEndPoint.y + value); 
                }
            }
            else if (tmpEndPoint.y == tmpStartPoint.y) {
                if (tmpEndPoint.x > tmpStartPoint.x) {
                    g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x - value, tmpEndPoint.y - value); 
                    g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x - value, tmpEndPoint.y + value); 
                }
                else {
                    g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x + value, tmpEndPoint.y - value); 
                    g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x + value, tmpEndPoint.y + value); 
                }
                
            }
            else if (tmpEndPoint.x > tmpStartPoint.x) {
                
                double x1O = (double)  0 - value;
                double y1O = (double)  0 - value;
                double y2O = (double)  value;
                
                int x1R = (int) (cosAlpha * x1O - sinAlpha * y1O);
                int y1R = (int) (sinAlpha * x1O + cosAlpha * y1O);
                int x2R = (int) (cosAlpha * x1O - sinAlpha * y2O);
                int y2R = (int) (sinAlpha * x1O + cosAlpha * y2O);
                
                g.drawLine(tmpEndPoint.x, tmpEndPoint.y, x1R + tmpEndPoint.x, y1R + tmpEndPoint.y); 
                g.drawLine(tmpEndPoint.x, tmpEndPoint.y, x2R + tmpEndPoint.x, y2R + tmpEndPoint.y); 
            }
            else {
                double x1O = (double) value;
                double y1O = (double) 0 - value;
                double y2O = (double) value;
                
                int x1R = (int) (cosAlpha * x1O - sinAlpha * y1O);
                int y1R = (int) (sinAlpha * x1O + cosAlpha * y1O);
                int x2R = (int) (cosAlpha * x1O - sinAlpha * y2O);
                int y2R = (int) (sinAlpha * x1O + cosAlpha * y2O);
                
                g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x - x1R, tmpEndPoint.y - y1R); 
                g.drawLine(tmpEndPoint.x, tmpEndPoint.y, tmpEndPoint.x - x2R, tmpEndPoint.y - y2R); 
                
            }
            
        }
    }
    
    
    /**
     *
     * Pseudo destructeur
     *
     */
    public void destroyEvent() {
        parent.removeMouseListener(this);
        parent.removeMouseMotionListener(this);
    }
    
    
    public void mouseClicked(MouseEvent evt) {
        this.refPoint = evt.getPoint();
        this.endPoint = this.refPoint;
        this.isReleased = false;
        parent.repaint();
        return;
    }
    
    
    public void mousePressed(MouseEvent evt) {
        this.mouseClicked(evt);
        return;
    }
    
    public void mouseDragged(MouseEvent e) {
        if (!this.dummyTest) {
            this.refPoint = e.getPoint();
            this.dummyTest = true;
        }
        this.endPoint = e.getPoint();
        this.parent.showMessage(parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
        parent.repaint();
    }
    
    
    //
    // When mouse is released, LINESELECTION|x1,y1;x2,y2 is sent to 
    // the javascript kaboumResult method
    //
    public void mouseReleased(MouseEvent e) {
        this.dummyTest = false;
        if (this.endPoint == this.refPoint) {
            return;
        }
        
        isReleased = true;
        
        //
        // Northern point first.
        // If there is no northern point, then western point first
        //
        if (this.refPoint.y == this.endPoint.y) {
            if (this.refPoint.x < this.endPoint.x) {
                tmpStartPoint = this.refPoint;
                tmpEndPoint = this.endPoint;
            }
            else {
                tmpStartPoint = this.endPoint;
                tmpEndPoint = this.refPoint;
            }
        }
        else {
            if (this.refPoint.y < this.endPoint.y) {
                tmpStartPoint = this.refPoint;
                tmpEndPoint = this.endPoint;
            }
            else {
                tmpStartPoint = this.endPoint;
                tmpEndPoint = this.refPoint;
            }
        }
        
        
        KaboumExtent tmpExtent = new KaboumExtent(this.parent.mapServerTools.mouseXToInternal(tmpStartPoint.x), this.parent.mapServerTools.mouseYToInternal(tmpStartPoint.y), this.parent.mapServerTools.mouseXToInternal(tmpEndPoint.x), this.parent.mapServerTools.mouseYToInternal(tmpEndPoint.y));
        
        this.parent.kaboumResult("LINESELECTION|" + tmpExtent.kaboumExternalString());
                
        this.parent.setCursor("CROSSHAIR");
        
        this.repaint();
    }


    public void mouseMoved(MouseEvent e) {
        this.mp = e.getPoint();
        //this.parent.refreshTooltip();
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
    }
    

}

