/*
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is inspired by JTS MultiPolygon class
 * (cf. http://www.vividsolutions.com)
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
package org.kaboum.geom;

import java.io.Serializable;

/**
 *
 * Basic implementation of <code>MultiPolygon</code>.
 *
 */
public class KaboumMultiPolygon extends KaboumGeometryCollection implements Serializable {
    
    /**
     *
     * Constructs a <code>MultiPolygon</code>.
     *
     * @param  polygons        the <code>Polygon</code>s for this <code>MultiPolygon</code>
     *
     */
    public KaboumMultiPolygon(KaboumPolygon[] polygons) {
        super(polygons);
    }
    
    
    public int getDimension() {
        return 2;
    }
    
    
    public String getGeometryType() {
        return "MultiPolygon";
    }
    
    
    public boolean equalsExact(KaboumGeometry other) {
        if (!isEquivalentClass(other)) {
            return false;
        }
        return super.equalsExact(other);
    }
}


