/*
 *
 * Class KaboumGEOMETRYUNIONOpMode from the Kaboum project.
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
 * For more information, contact:
 *
 *     jerome.gasperi at gmail.com
 *
 */
package org.kaboum;

import org.kaboum.util.KaboumFeatureModes;


/**
 *
 * This opMode allows to select one or more object from
 * parent.geoObjectList and send a K_UNION operation request
 * to KaboumServer
 *<p>
 *This opMode supports three sub-modes:
 *<ul>
 *<li>the first one, when using 1 argument constructor allows to select objects
 * to perform operation on. When the operation is performed, the opMOde calls standbyOn
 * until the caller calls standbyOff and recall it</li>
 *<li>the second one, when using constructor with 2 arguments, allows to validate or cancel
 * the result of the current operation. In this case, the second argument is the
 * KaboumGeometry K_NEW_OBECT id and the list of selected geometries' id: 
 * it tells the opMode to display a popup menu allowing to validate or cancel the operation</li>
 *<li>the third one, when using constructor with 2 arguments without K_NEW_OBJECT id removes
 * all the given geometries from Kaboum and sets the opMode to the "select geometries" mode</li>
 *</ul>
 *</p>
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGEOMETRY_K_UNIONOpMode extends KaboumGEOMETRY_SYMETRIC_OPERATIONOpMode {
    
   
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumGEOMETRY_K_UNIONOpMode(Kaboum parent) {   
        super(parent, KaboumFeatureModes.K_UNION);
    }
    
    public KaboumGEOMETRY_K_UNIONOpMode(Kaboum parent, String list) {   
       super(parent, KaboumFeatureModes.K_UNION);
    }
    
}
