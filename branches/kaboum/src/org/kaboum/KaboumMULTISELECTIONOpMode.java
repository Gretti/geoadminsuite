/*
 *
 * Class KaboumMULTISELECTIONOpMode from the Kaboum project.
 * Multi-selection opMode.
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

import java.util.StringTokenizer;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;

/**
 *
 * This opMode allows to select one or more object from
 * parent.geoObjectList
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumMULTISELECTIONOpMode extends KaboumSELECTIONOpMode {
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumMULTISELECTIONOpMode(Kaboum parent) {
        super(parent, true);
    }
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     * @param list List of pre-selectionned objects
     *
     */
    public KaboumMULTISELECTIONOpMode(Kaboum parent, String list) {
        
        this(parent);
        
        //
        // Hilite pre-selectionned objects (list)
        // List form is : idA;idB;...
        //
        StringTokenizer st = new StringTokenizer(list, ";");
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        
        while (st.hasMoreTokens()) {
            int position = this.parent.GGDIndex.getGGDIndex(st.nextToken());
            
            if (position != -1) {
                
                tmpGGD = (KaboumGeometryGlobalDescriptor) this.parent.GGDIndex.elementAt(position);
                // Add the new list
                this.selectionList.put(new Integer(position), tmpGGD);
                this.selectGeometry(tmpGGD);
            }
        }
        
        this.parent.repaint();
    }
}