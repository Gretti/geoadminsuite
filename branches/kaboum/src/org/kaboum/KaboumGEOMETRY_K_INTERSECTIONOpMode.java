/*
 *
 * Class KaboumGEOMETRYINTERSECTIONOpMode from the Kaboum project.
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
 * parent.geoObjectList and send a K_INTERSECTION operation request
 * to KaboumServer
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGEOMETRY_K_INTERSECTIONOpMode extends KaboumGEOMETRY_SYMETRIC_OPERATIONOpMode {
   
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumGEOMETRY_K_INTERSECTIONOpMode(Kaboum parent) {
        super(parent, KaboumFeatureModes.K_INTERSECTION);
    }
    
    public KaboumGEOMETRY_K_INTERSECTIONOpMode(Kaboum parent, String list) {
        super(parent, KaboumFeatureModes.K_INTERSECTION);
    }
    
}
