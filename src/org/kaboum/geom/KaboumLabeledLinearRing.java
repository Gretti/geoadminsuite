/*
 *
 * Class KaboumLabeldLinearRing from the Kaboum project.
 * This class define a LinearRing with labeled edge.
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
 *
 */
package org.kaboum.geom;

import java.io.Serializable;
import java.util.Vector;
import org.kaboum.util.KaboumCoordinate;


/**
 *
 * This class define a LinearRing with labeled edge.
 *
 */
public class KaboumLabeledLinearRing extends KaboumLinearRing implements Serializable {
    
    /** Vector of edges */
    private Vector edges = new Vector();
    
    
    /**
     *
     * Constructs a <code>LabeledLinearRing</code> with the given points.
     *
     * @param  points         points forming a closed and simple linestring
     *
     */
    public KaboumLabeledLinearRing(KaboumCoordinate[] points) {
        super(points);
        int numPointsSubOne = this.getNumPoints() - 1;
        if (numPointsSubOne > 0) {
            for (int i = 0; i < numPointsSubOne; i++) {
                this.edges.addElement(new KaboumEdge(points[i], points[i+1]));
            }
        }
    }
    
    
    public String getGeometryType() {
        return "LabeledLinearRing";
    }
    
}

