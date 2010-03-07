/*
 *
 * Class KaboumBOXSELECTIONOpMode from the Kaboum project.
 * Return the coordinate of a dragging BOX.
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
public class KaboumBOXSELECTIONOpMode extends KaboumOpMode {
    
    public static final String PARAM_FOREGROUNDCOLOR = "BOXSELECTION_FOREGROUND_COLOR";
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
    
    /** Dummy test */
    private boolean dummyTest = false;   
    
    
    /**
     *
     * Constructor.
     *
     * @param parent Parent
     *
     */
    public KaboumBOXSELECTIONOpMode(Kaboum parent) {
        
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
     * Define a dragging box
     *
     */
    public void newBox(Point p) {
        int dummyX, dummyY;
        
        dummyX = p.x;
        dummyY = p.y;
        
        if (dummyX < 0) dummyX = 0;
        if (dummyY < 0) dummyY = 0;
        if (dummyX > (this.parent.screenSize.width - 1)) dummyX = (this.parent.screenSize.width - 1);
        if (dummyY > (this.parent.screenSize.height - 1)) dummyY = (this.parent.screenSize.height - 1);
        
        if (dummyX < refPoint.x) {
            x = dummyX;
            width = refPoint.x - x;
        }
        else {
            x = refPoint.x;
            width = dummyX - x;
        }
        
        if (dummyY < refPoint.y) {
            y = dummyY;
            height = refPoint.y - y;
        }
        else {
            y = refPoint.y;
            height = dummyY - y;
        }
    }
    
    
    /*
     *
     * PAINT
     *
     */
    public void paint(Graphics g) {
        int i;
        int thickness = 2;
        
        g.setColor(KaboumUtil.getColorParameter(this.parent.getOpModeProperty(PARAM_FOREGROUNDCOLOR), Color.black));
        
        if ((width > 0) && (height > 0)) {
            for(i=0; i<thickness; i++) { g.drawRect(x+i, y+i, width-(2*i), height-(2*i)); }
        }
        else {
            g.drawLine(x - thickness, y, x + thickness, y);
            g.drawLine(x, y - thickness, x, y + thickness);
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
        this.x = refPoint.x;
        this.y = refPoint.y;
        this.width = 0;
        this.height = 0;
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
        newBox(e.getPoint());
        this.parent.showMessage(parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
        parent.repaint();
    }
    
    
    //
    // When mouse is released, BOXSELECTION|x1,y1;x2,y2 is send to 
    // the javascript kaboumResult method
    //
    public void mouseReleased(MouseEvent e) {
        this.dummyTest = false;
        int tmpWidth = x + width;
        int tmpHeight = y + height;
        KaboumExtent tmpExtent = new KaboumExtent(this.parent.mapServerTools.mouseXToInternal(x), this.parent.mapServerTools.mouseYToInternal(y + height), this.parent.mapServerTools.mouseXToInternal(x + width), this.parent.mapServerTools.mouseYToInternal(y));
        
        this.parent.kaboumResult("BOXSELECTION|" + tmpExtent.kaboumExternalString());
        
//        this.parent.repaint();
        
        this.parent.setCursor("CROSSHAIR");
    }


    public void mouseMoved(MouseEvent e) {
        this.mp = e.getPoint();
        this.parent.refreshTooltip();
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
    }
    

}
