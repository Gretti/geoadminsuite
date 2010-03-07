/*
 *
 * Class KaboumEdge from the Kaboum project.
 * This class define an edge i.e. an oriented segment defined by a start point
 * and an end point. This edge can be labeled as refered to Klamer Schutte algorithm
 * (cf. "An edge labeling approach to concave polygon cliping", Klamer Schutte,
 * PREPRINT submitted 7 july 1995 to ACM Transcations on Graphics).
 *
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
package org.kaboum.geom;

import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumExtent;

/**
 *
 * This class define an edge i.e. an oriented segment defined by a start point
 * and an end point. This edge can be labeled as refered to Klamer Schutte algorithm
 * (cf. "An edge labeling approach to concave polygon cliping", Klamer Schutte,
 * PREPRINT submitted 7 july 1995 to ACM Transcations on Graphics).
 *
 */
public class KaboumEdge {
    
    // CONSTANTS
    public static final int UNLABELED = -1;
    public static final int INSIDE = 0;
    public static final int OUTSIDE = 1;
    public static final int SHARED = 2;
    
    /** The start point */
    public KaboumCoordinate p1;
    
    /** The end point */
    public KaboumCoordinate p2;
    
    /** The label */
    public int label = UNLABELED;
    
    
    /**
     *
     * Constructs an edge .
     *
     * @param  p1     Start point
     * @param  p2     End point
     * @param  label  Label
     *
     */
    public KaboumEdge(KaboumCoordinate p1, KaboumCoordinate p2, int edge) {
        this.p1 = p1;
        this.p2 = p2;
        this.label = label;
    }
    
    
    /**
     *
     * Constructs an unlabeled edge .
     *
     * @param  p1     Start point
     * @param  p2     End point
     *
     */
    public KaboumEdge(KaboumCoordinate p1, KaboumCoordinate p2) {
        this(p1, p2, UNLABELED);
    }

    
    /**
     *
     * Return the extent of this edge
     *
     */
    public KaboumExtent getExtent() {
        double maxX = Math.max(p1.x, p2.x);
        double minX = Math.min(p1.x, p2.x);
        double maxY = Math.max(p1.y, p2.y);
        double minY = Math.min(p1.y, p2.y);
        
        return new KaboumExtent(minX, minY, maxX, maxY);
    }
    
    
}

