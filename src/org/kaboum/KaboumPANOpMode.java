/*
 *
 * Class KaboumPANOpMode from the Kaboum project.
 * Define a pan opMode.
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


/**
 *
 * This class define a pan OpMode.
 *
 * @author Jerome Gasperi aka jrom
 */
public class KaboumPANOpMode extends KaboumOpMode {

    /** CONSTANT */
    public static String[] getParametersList() {
        return new String[0];
    }
    
    /** Anchor for the first point clicked */
    private Point anchor = new Point(0,0);

    /** True --> anchor is set */
    private boolean first = true;

    /** Parent reference */
    private Kaboum parent;

    private boolean actionIsValid = false;

    /**
     *
     * Constructor.
     *
     * @param parent Parent
     *
     */
    public KaboumPANOpMode(Kaboum parent) {

	// Add this MouseListener to the parent
	parent.addMouseListener(this);
	parent.addMouseMotionListener(this);

	// Set the pointer mouse shape
	parent.setCursor("MOVE");

	this.parent = parent;
        
    }


    public void mousePressed (MouseEvent evt) {
        actionIsValid = false;
    }


    public void mouseDragged (MouseEvent evt) {
	Point tmpPt = evt.getPoint();
	if (this.first) {
           this.anchor = evt.getPoint();
	   this.first = false;
	}
	actionIsValid = true;
	parent.setPanCoordinates(tmpPt.x - anchor.x, tmpPt.y - anchor.y);
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(evt.getX(), evt.getY()));
        parent.repaint();
    }
    
    
    public void mouseMoved (MouseEvent evt) {
        this.mp = evt.getPoint();
        //this.parent.refreshTooltip();
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(evt.getX(), evt.getY()));
    }


    public void mouseReleased (MouseEvent evt) {
        this.first = false;
	parent.setPanCoordinates(evt.getPoint().x - anchor.x,evt.getPoint().y - anchor.y);
	int x = (int) (((this.parent.screenSize.width - 1) / 2) - (evt.getPoint().x - anchor.x));
        int y = (int) (((this.parent.screenSize.height - 1) / 2) - (evt.getPoint().y - anchor.y));
	parent.removeMouseListener(this);
	parent.removeMouseMotionListener(this);
	if (actionIsValid) {
            parent.kaboumCommand("EXTENT|IMAGE|"+x+","+y+";-10,-10");
        }
	parent.setPanCoordinates(0,0);
	parent.respawnOpMode();
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
    
}
