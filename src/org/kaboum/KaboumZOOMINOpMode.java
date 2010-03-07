/*
 *
 * Class KaboumZoominOpMode from the Kaboum project.
 * Define a zoomin opMode.
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


import java.awt.*;
import java.awt.event.*;

import org.kaboum.util.KaboumUtil;

/**
 *
 * This class define a zoomin opMode.
 * A zoomin area is defined by a mouse click,
 * or by a dragging box.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumZOOMINOpMode extends KaboumOpMode {
    
    /** Constant */
    public static final String PARAM_FOREGROUNDCOLOR = "ZOOMIN_FOREGROUND_COLOR";
    public static final String PARAM_MINBOXSIZE = "ZOOMIN_MINIMUM_BOX_SIZE";
    
    public static String[] getParametersList() {
        String[] list = new String[2];
        list[0] = PARAM_FOREGROUNDCOLOR;
        list[1] = PARAM_MINBOXSIZE;
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
    
    /** Parent reference */
    private Kaboum parent;
    
    /** Reference point */
    protected Point refPoint = null;
            
    
    /**
     *
     * Constructor.
     *
     * @param parent Parent
     * @param color OpMode color
     * @param minBoSize Min size of valid zoom box (in pixels)
     *
     */
    public KaboumZOOMINOpMode(Kaboum parent) {
        
        // Initialisation
        this.parent = parent;
        
        // Ajout des Listeners au parent
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
        
        // Definit la forme du curseur de la souris
        parent.setCursor("CROSSHAIR");

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
        
        g.setColor(KaboumUtil.getColorParameter(this.parent.getOpModeProperty(PARAM_FOREGROUNDCOLOR), Color.red));
        
        for (int i = 0; i<2; i++) {
            g.drawRect(x+i, y+i, width-(2*i), height-(2*i));
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
    
    //***************************************LISTENERS********************************
    
    public void mousePressed(MouseEvent e) {
        refPoint = e.getPoint();
        this.x = refPoint.x;
        this.y = refPoint.y;
    }
    
    
    public void mouseDragged(MouseEvent e) {
        newBox(e.getPoint());
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
        parent.repaint();
    }
    
    
    public void mouseMoved(MouseEvent e) {
        this.mp = e.getPoint();
        //this.parent.refreshTooltip();
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
    }
        
    
    public void mouseReleased(MouseEvent e) {
        
        int minBoxSize = KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_MINBOXSIZE), 10);
        
        // Si la boite de selection est suffisament grande, on renvois 2 couples de coordonnees
        if (width >= minBoxSize || height >= minBoxSize) {
            int tmpWidth = x + width;
            int tmpHeight = y + height;
            parent.kaboumCommand("EXTENT|IMAGE|"+x+","+y+";"+tmpWidth+","+tmpHeight);
        }
        else {
            parent.kaboumCommand("EXTENT|IMAGE|"+x+","+y+";-1,-1");
        }
        
        this.parent.repaint();
        
        parent.respawnOpMode();

    }

}
