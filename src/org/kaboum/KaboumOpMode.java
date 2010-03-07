package org.kaboum;

/*
 *
 * Class KaboumOpMode from the Kaboum project.
 * Define a OpMode object.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * Copyright (C) 2000 Jerome Gasperi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

import java.awt.Point;
import java.awt.Container;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

/**
 *
 * This abstract class define a OpMode object.
 * A OpMode can be anything that modify or not
 * the content of the Kaboum applet (zoom +/-,
 * navigate, digitalisation).
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public abstract class KaboumOpMode extends Container implements MouseMotionListener, MouseListener, ActionListener {

    public Point mp;
    
    public void mousePressed(MouseEvent e) { }

    
    public void mouseDragged(MouseEvent e) { }


    public void mouseReleased(MouseEvent e) { }


    public void mouseMoved (MouseEvent evt) { }


    public void mouseEntered (MouseEvent evt) { }


    public void mouseExited (MouseEvent evt) { }


    public void mouseClicked (MouseEvent evt) { }

    
    public void actionPerformed(ActionEvent e) { }

    
    public void destroyEvent() { }
    
    /**
     * Returns the name of this OpMode, based on the class name. <br>
     * All operating modes are named "Kaboum" + Operating mode name + "OpMode".
     * Moreover, operating mode name must be in upper case.
     *<p>
     * For instance, the KaboumZOOMINOpMode name is ZOOMIN
     *</p>
     */
    public String getOpModeName() {
        return this.getClass().getName().substring(
                this.getClass().getName().lastIndexOf("Kaboum") + "Kaboum".length(), 
                this.getClass().getName().lastIndexOf("OpMode"));
    }

 
}
