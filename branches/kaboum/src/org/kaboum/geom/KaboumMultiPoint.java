/*
 *
 *
 * Class KaboumMultiPoint from the Kaboum project.
 * This class define a geometry i.e. a geometrical object such
 * as a Point, a Linestring or a polygon object.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class inspired by JTS MultiPoint class
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
import org.kaboum.util.KaboumCoordinate;

/**
 *
 * Basic implementation of <code>MultiPoint</code>.
 *
 */
public class KaboumMultiPoint extends KaboumGeometryCollection implements Serializable {
    
    /**
     *
     * Constructs a <code>MultiPoint</code>.
     *
     * @param  points          the <code>Point</code>s for this <code>MultiPoint</code>
     *
     */
    public KaboumMultiPoint(KaboumPoint[] points) {
        super(points);
    }
    
    public int getDimension() {
        return 0;
    }
        
    public String getGeometryType() {
        return "MultiPoint";
    }
        
    public boolean equalsExact(KaboumGeometry other) {
        if (!isEquivalentClass(other)) {
            return false;
        }
        return super.equalsExact(other);
    }
    
    /**
     *
     * Returns the <code>Coordinate</code> at the given position.
     *
     * @param  n  the index of the <code>Coordinate</code> to retrieve, beginning
     *            at 0
     *
     */
    protected KaboumCoordinate getCoordinate(int n) {
        return ((KaboumPoint) geometries[n]).getCoordinate();
    }
    
}

