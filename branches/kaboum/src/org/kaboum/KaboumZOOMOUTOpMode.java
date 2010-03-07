package org.kaboum;

/*
 *
 * Class KaboumZoomOutOpMode from the Kaboum project.
 * Define a zoomout opMode.
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

import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 *
 * This class define a zoomout opMode.
 * A zoomout return a mouse click.
 *
 * @author Jerome Gasperi aka jrom
 */
public class KaboumZOOMOUTOpMode extends KaboumOpMode {

    public static String[] getParametersList() {
        return new String[0];
    }
    
    /** X image coordinate of unzoom center */
    protected int x = 0;

    /** Y image coordinate of unzoom center */
    protected int y = 0;

    /** Parent reference */
    protected Kaboum parent;

    protected boolean actionIsValid = false;
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumZOOMOUTOpMode(Kaboum parent) {
	
	// Add this MouseListener to the parent
	parent.addMouseListener(this);
	parent.addMouseMotionListener(this);

	// Set the pointer mouse shape
	parent.setCursor("CROSSHAIR");

	this.parent = parent;
    }

    
    public void destroyEvent() {
	parent.removeMouseListener(this);
	parent.removeMouseMotionListener(this);
    }


    public void mousePressed (MouseEvent evt) {
	Point tmpPt = evt.getPoint();
	actionIsValid = true;
	this.x = tmpPt.x;
	this.y = tmpPt.y;
    }


    public void mouseDragged (MouseEvent evt) {
	actionIsValid = false;
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(evt.getX(), evt.getY()));
    }


    public void mouseMoved (MouseEvent evt) {
        this.mp = evt.getPoint();
        //this.parent.refreshTooltip();
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(evt.getX(), evt.getY()));
    }
    

    public void mouseReleased (MouseEvent evt) {
	parent.removeMouseListener(this);
	parent.removeMouseMotionListener(this);
	if (actionIsValid) {
            parent.kaboumCommand("EXTENT|IMAGE|"+x+","+y+";-5,-5");
        }
	parent.respawnOpMode();
    }
}
