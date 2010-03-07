/*
 *
 * Class KaboumGEOMETRY_K_POLYGON_ERASINGOpMode from the Kaboum project.
 * Class to erase a part of a polygon with another polygon.
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
 *     jerome dot gasperi at gmail dot com
 *
 */
package org.kaboum;

import org.kaboum.util.KaboumFeatureModes;

/**
 *
 * Class to erase a part of a polygon with another polygon.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGEOMETRY_K_POLYGON_ERASINGOpMode extends KaboumGEOMETRY_K_POLYGON_SPLITTINGOpMode {
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumGEOMETRY_K_POLYGON_ERASINGOpMode(Kaboum parent) {
        super(parent, KaboumFeatureModes.K_POLYGON_ERASING);
    }
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     * @param idList Object return list send by KaboumServer
     *
     */
    public KaboumGEOMETRY_K_POLYGON_ERASINGOpMode(Kaboum parent, short OPERATION) {
        super(parent, KaboumFeatureModes.K_POLYGON_ERASING);
    }
 
}
