/*
 *
 * KaboumAlgorithms class from the Kaboum project. Kaboum is a frontend
 * to mapserver (http://mapserver.gis.umn.edu)
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
package org.kaboum.algorithm;

import java.awt.Point;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumExtent;
import org.kaboum.geom.KaboumLineString;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumPolygon;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;

public class KaboumAlgorithms {
    
    /**
     *
     * This class is a rewrite of orientationIndex(Coordinate p1, Coordinate p2, Coordinate q)
     * from JTS RobustCGAlgorithms class
     * (cf. http://www.vividsolution.com)
     *
     */
    public static int orientationIndex(KaboumCoordinate p1, KaboumCoordinate p2, KaboumCoordinate q) {
        // travelling along p1->p2, turn counter clockwise to get to q return 1,
        // travelling along p1->p2, turn clockwise to get to q return -1,
        // p1, p2 and q are colinear return 0.
        double dx1 = p2.x - p1.x;
        double dy1 = p2.y - p1.y;
        double dx2 = q.x - p2.x;
        double dy2 = q.y - p2.y;
        return signOfDet2x2(dx1, dy1, dx2, dy2);
    }
    
    /**
     *
     * This class is a rewrite of isCCW(Coordinate[] ring) from JTS
     * RobustCGAlgorithms class
     * (cf. http://www.vividsolutions.com)
     *
     */
    public static boolean isCCW(KaboumCoordinate[] ring) {
        KaboumCoordinate hip;
        KaboumCoordinate p;
        KaboumCoordinate prev;
        KaboumCoordinate next;
        int hii;
        int i;
        int nPts = ring.length;
        // algorithm to check if a Ring is stored in CCW order
        // find highest point
        hip = ring[0];
        hii = 0;
        for (i = 1; i < nPts; i++) {
            p = ring[i];
            if (p.y > hip.y) {
                hip = p;
                hii = i;
            }
        }
        // find points on either side of highest
        int iPrev = hii - 1;
        if (iPrev < 0) {
            iPrev = nPts - 2;
        }
        int iNext = hii + 1;
        if (iNext >= nPts) {
            iNext = 1;
        }
        prev = ring[iPrev];
        next = ring[iNext];
        int disc = computeOrientation(prev, hip, next);
    /*
     *  If disc is exactly 0, lines are collinear.  There are two possible cases:
     *  (1) the lines lie along the x axis in opposite directions
     *  (2) the line lie on top of one another
     *  (2) should never happen, so we're going to ignore it!
     *  (Might want to assert this)
     *  (1) is handled by checking if next is left of prev ==> CCW
     */
        if (disc == 0) {
            // poly is CCW if prev x is right of next x
            return (prev.x > next.x);
        }
        else {
            // if area is positive, points are ordered CCW
            return (disc > 0);
        }
        
    }
    
    
    /**
     *
     * Check if a point lies in or near a GEOMETRY
     *
     */
    public static boolean doesPointLieInGeometry(KaboumCoordinate p, KaboumGeometry geometry, double precision) {
        
        // CASE 1 : closed geometry
        if (geometry.isClosed()) {
            return isPointInPolygon(p, geometry);
        }
        
        // CASE 2 : non-closed geometry
        return distance(p, geometry) < precision;
        
    }
    
    
    /**
     *
     * Check if a point is on the border of a GEOMETRY
     *
     */
    public static boolean doesPointBorderGeometry(KaboumCoordinate p, KaboumGeometry geometry, double precision) {
        
        // CASE 2 : non-closed geometry
        return distance(p, geometry) < precision;
        
    }
    
    
    /**
     *
     * Check if a point is inside a GEOMETRY
     *
     */
    public static boolean isPointInPolygon(KaboumCoordinate p, KaboumGeometry geometry) {
        
        // Geometry must be closed
        if (!geometry.isClosed()) {
            return false;
        }
        
        // Multi geometry
        int numGeometries = geometry.getNumGeometries();
        boolean isInside = false;
        boolean isInHole = false;
        KaboumGeometry tmpGeom;
        
        for (int i = 0; i < numGeometries; i++) {
            
            tmpGeom = geometry.getGeometryN(i);
            
            if (tmpGeom.getGeometryType().equals("Polygon")) {
                
                isInside = isPointInPolygon(p, ((KaboumPolygon) tmpGeom).getExteriorRing().getCoordinates());
                
                if (!isInside) {
                    continue;
                }
                
                isInHole = false;
                
                int numHoles = ((KaboumPolygon) tmpGeom).getNumInteriorRing();
                for (int j = 0; j < numHoles; j++) {
                    isInHole = isPointInPolygon(p, ((KaboumPolygon) tmpGeom).getInteriorRingN(j).getCoordinates());
                    if (isInHole) {
                        isInside = false;
                        continue;
                    }
                }
                
            }
            else {
                isInside = isPointInPolygon(p, tmpGeom.getCoordinates());
            }
            
            if (isInside) {
                return true;
            }
            
        }
        
        return false;
        
    }
    
    
    /**
     * This algorithm does not attempt to first check the point against the envelope
     * of the ring.
     * This class is a rewrite of isPointInPolygon(Coordinate p, Coordinate[] ring) method
     * from JTS RobustCGAlgorithms class
     * (cf. http://www.vividsolutions.com)
     *
     * @param ring assumed to have first point identical to last point
     */
    public static boolean isPointInPolygon(KaboumCoordinate p, KaboumCoordinate[] ring) {
        int i;
        int i1;       // point index; i1 = i-1
        double xInt;  // x intersection of segment with ray
        int crossings = 0;  // number of segment/ray crossings
        double x1;    // translated coordinates
        double y1;
        double x2;
        double y2;
        int nPts = ring.length;
        
        if (nPts > 0) {
            if (!ring[0].equals(ring[nPts - 1])) {
                return false;
            }
        }
        
    /*
     *  For each segment l = (i-1, i), see if it crosses ray from test point in positive x direction.
     */
        for (i = 1; i < nPts; i++) {
            i1 = i - 1;
            KaboumCoordinate p1 = ring[i];
            KaboumCoordinate p2 = ring[i1];
            x1 = p1.x - p.x;
            y1 = p1.y - p.y;
            x2 = p2.x - p.x;
            y2 = p2.y - p.y;
            
            if (((y1 > 0) && (y2 <= 0)) ||
            ((y2 > 0) && (y1 <= 0))) {
        /*
         *  segment straddles x axis, so compute intersection.
         */
                xInt = signOfDet2x2(x1, y1, x2, y2) / (y2 - y1);
                //xsave = xInt;
        /*
         *  crosses ray if strictly positive intersection.
         */
                if (0.0 < xInt) {
                    crossings++;
                }
            }
        }
    /*
     *  p is inside if number of crossings is odd.
     */
        if ((crossings % 2) == 1) {
            return true;
        }
        else {
            return false;
        }
    }
    
    
    public static int computeOrientation(KaboumCoordinate p1, KaboumCoordinate p2, KaboumCoordinate q) {
        return orientationIndex(p1, p2, q);
    }
    
    private static boolean isInExtent(KaboumCoordinate p, KaboumCoordinate[] ring) {
        KaboumExtent envelope = new KaboumExtent();
        for (int i = 0; i < ring.length; i++) {
            envelope.expandToInclude(ring[i]);
        }
        return envelope.contains(p);
    }
    
    
    /**
     *
     * Return the distance between a point and a geometry
     *
     */
    public static double distance(KaboumCoordinate p, KaboumGeometry geometry) {
        
        double distance = Double.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
        
        int numGeometries = geometry.getNumGeometries();
        KaboumGeometry tmpGeom;
        
        for (int i = 0; i < numGeometries; i++) {
            tmpGeom = geometry.getGeometryN(i);
            if (tmpGeom.getGeometryType().equals("Polygon")) {
                distance = distance(p, ((KaboumPolygon) tmpGeom).getExteriorRing().getCoordinates());
            }
            else {
                distance = distance(p, tmpGeom.getCoordinates());
            }
            
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        
        return minDistance;
        
    }
    
    
    /**
     *
     * Compute the distance between a point P and a PolyLine [A1...An].
     *
     * @param p Point P
     * @param polyLine PolyLine (Polygon representation)
     * @return The square distance between the point and the polyline.
     *
     */
    public static double distance(KaboumCoordinate p, KaboumCoordinate[] internals) {
        
        int numPoints = internals.length;
        
        if (numPoints == 1) {
            return distance(p, internals[0]);
        }
        
        double tmpDist;
        double minDist;
        
        // Initial shortest distance is the distance between
        // P and the first segment of the polyline
        minDist = distance(p, internals[0], internals[1]);
        
        // Now loops over all other segments
        for (int i = 1; i < numPoints - 1; i++) {
            tmpDist = distance(p, internals[i], internals[i+1]);
            if (tmpDist < minDist) {
                minDist = tmpDist;
            }
        }
        
        return minDist;
        
    }
    
    
    /**
     *
     * Compute the distance between a point P and a segment [AB].
     *
     * @param p Point P
     * @param a Point A
     * @param b Point B
     * @return The distance between the point and the segment.
     *
     */
    public static double distance(KaboumCoordinate p, KaboumCoordinate a, KaboumCoordinate b) {
        
        double x = p.x;
        double y = p.y;
        double ax = a.x;
        double ay = a.y;
        double bx = b.x;
        double by = b.y;
        
        // If the projection of P doesn't lies into segment [AB]
        // the min distance is the distance to the closest end of
        // the segment
        if ((bx - ax) * (x - ax) + (by - ay) * (y - ay) <= 0) {
            return ((y - ay) * (y - ay) + (x - ax) * (x - ax));
        }
        else if ((x - bx) * (ax - bx) + (y - by) * (ay - by) <= 0) {
            return ((y - by) * (y - by) + ( x - bx) * (x - bx));
        }
        
        return Math.abs(area2(a, b, p) / distance(a, b));
    }
    
    
    /**
     *
     * Compute the magnitude of the cross Product between two
     * Vectors AB and AC. The magnitude of the cross product is twice
     * the area of the triangle they determine.
     * (From: "Computational Geometry in C" by Joseph O'Rourke)
     * (Image coordinates version)
     *
     * @param a Point A
     * @param b Point B
     * @param c Point C
     *
     */
    public static double area2(KaboumCoordinate a, KaboumCoordinate b, KaboumCoordinate c) {
        double ax = a.x;
        double ay = a.y;
        double bx = b.x;
        double by = b.y;
        double cx = c.x;
        double cy = c.y;
        
        return (bx - ax) * (cy - ay) - (cx - ax) * (by - ay);
    }
    
    
    /**
     *
     * Compute the Euclidian distance between to point.
     *
     */
    public static double distance(KaboumCoordinate a, KaboumCoordinate b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
    
    /**
     *
     * Compute the squre Euclidian distance between to point.
     *
     */
    public static int squareDistance(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }
    
    
    /**
     * <code>RobustDeterminant</code> implements an algorithm to compute the
     * sign of a 2x2 determinant for double precision values robustly.
     * It is a direct translation of code developed by Olivier Devillers.
     * <p>
     * The original code carries the following copyright notice:
     *
     * <pre>
     *************************************************************************
     * Author : Olivier Devillers
     * Olivier.Devillers@sophia.inria.fr
     * http:/www.inria.fr:/prisme/personnel/devillers/anglais/determinant.html
     **************************************************************************
     *
     **************************************************************************
     *              Copyright (c) 1995  by  INRIA Prisme Project
     *                  BP 93 06902 Sophia Antipolis Cedex, France.
     *                           All rights reserved
     **************************************************************************
     * </pre>
     */
    public static int signOfDet2x2(double x1, double y1, double x2, double y2) {
        // returns -1 if the determinant is negative,
        // returns  1 if the determinant is positive,
        // retunrs  0 if the determinant is null.
        int sign;
        double swap;
        double k;
        long count = 0;
        
        sign = 1;
        
    /*
     *  testing null entries
     */
        if ((x1 == 0.0) || (y2 == 0.0)) {
            if ((y1 == 0.0) || (x2 == 0.0)) {
                return 0;
            }
            else if (y1 > 0) {
                if (x2 > 0) {
                    return -sign;
                }
                else {
                    return sign;
                }
            }
            else {
                if (x2 > 0) {
                    return sign;
                }
                else {
                    return -sign;
                }
            }
        }
        if ((y1 == 0.0) || (x2 == 0.0)) {
            if (y2 > 0) {
                if (x1 > 0) {
                    return sign;
                }
                else {
                    return -sign;
                }
            }
            else {
                if (x1 > 0) {
                    return -sign;
                }
                else {
                    return sign;
                }
            }
        }
        
    /*
     *  making y coordinates positive and permuting the entries
     */
    /*
     *  so that y2 is the biggest one
     */
        if (0.0 < y1) {
            if (0.0 < y2) {
                if (y1 <= y2) {
                    ;
                }
                else {
                    sign = -sign;
                    swap = x1;
                    x1 = x2;
                    x2 = swap;
                    swap = y1;
                    y1 = y2;
                    y2 = swap;
                }
            }
            else {
                if (y1 <= -y2) {
                    sign = -sign;
                    x2 = -x2;
                    y2 = -y2;
                }
                else {
                    swap = x1;
                    x1 = -x2;
                    x2 = swap;
                    swap = y1;
                    y1 = -y2;
                    y2 = swap;
                }
            }
        }
        else {
            if (0.0 < y2) {
                if (-y1 <= y2) {
                    sign = -sign;
                    x1 = -x1;
                    y1 = -y1;
                }
                else {
                    swap = -x1;
                    x1 = x2;
                    x2 = swap;
                    swap = -y1;
                    y1 = y2;
                    y2 = swap;
                }
            }
            else {
                if (y1 >= y2) {
                    x1 = -x1;
                    y1 = -y1;
                    x2 = -x2;
                    y2 = -y2;
                    ;
                }
                else {
                    sign = -sign;
                    swap = -x1;
                    x1 = -x2;
                    x2 = swap;
                    swap = -y1;
                    y1 = -y2;
                    y2 = swap;
                }
            }
        }
        
    /*
     *  making x coordinates positive
     */
    /*
     *  if |x2| < |x1| one can conclude
     */
        if (0.0 < x1) {
            if (0.0 < x2) {
                if (x1 <= x2) {
                    ;
                }
                else {
                    return sign;
                }
            }
            else {
                return sign;
            }
        }
        else {
            if (0.0 < x2) {
                return -sign;
            }
            else {

                if (x1 >= x2) {
                    sign = -sign;
                    x1 = -x1;
                    x2 = -x2;
                    ;
                }
                else {
                    return -sign;
                }
            }
        }
        
    /*
     *  all entries strictly positive   x1 <= x2 and y1 <= y2
     */
        while (true) {
            count = count + 1;
            k = Math.floor(x2 / x1);
            x2 = x2 - k * x1;
            y2 = y2 - k * y1;
            
      /*
       *  testing if R (new U2) is in U1 rectangle
       */
            if (y2 < 0.0) {
                return -sign;
            }
            if (y2 > y1) {
                return sign;
            }
            
      /*
       *  finding R'
       */
            if (x1 > x2 + x2) {
                if (y1 < y2 + y2) {
                    return sign;
                }
            }
            else {
                if (y1 > y2 + y2) {
                    return -sign;
                }
                else {
                    x2 = x1 - x2;
                    y2 = y1 - y2;
                    sign = -sign;
                }
            }
            if (y2 == 0.0) {
                if (x2 == 0.0) {
                    return 0;
                }
                else {
                    return -sign;
                }
            }
            if (x2 == 0.0) {
                return sign;
            }
            
      /*
       *  exchange 1 and 2 role.
       */
            k = Math.floor(x1 / x2);
            x1 = x1 - k * x2;
            y1 = y1 - k * y2;
            
      /*
       *  testing if R (new U1) is in U2 rectangle
       */
            if (y1 < 0.0) {
                return sign;
            }
            if (y1 > y2) {
                return -sign;
            }
            
      /*
       *  finding R'
       */
            if (x2 > x1 + x1) {
                if (y2 < y1 + y1) {
                    return -sign;
                }
            }
            else {
                if (y2 > y1 + y1) {
                    return sign;
                }
                else {
                    x1 = x2 - x1;
                    y1 = y2 - y1;
                    sign = -sign;
                }
            }
            if (y1 == 0.0) {
                if (x1 == 0.0) {
                    return 0;
                }
                else {
                    return sign;
                }
            }
            if (x1 == 0.0) {
                return -sign;
            }
        }
        
    }
    
    
    /**
     *
     * Return the position of the closest vertex from an input Coordinate
     *
     * @param internals    Vertices list
     * @param coord        Input coordinate (internal representation)
     *
     */
    public static int getClosestPointPosition(KaboumCoordinate[] internals, KaboumCoordinate coord) {
        
        int position = -1;
        int numPointsMinusOne = internals.length - 1;
        
        if(numPointsMinusOne < 0) {
            return -1;
        }
        
        if(numPointsMinusOne == 0) {
            return 0;
        }
        
        double minDist = Double.MAX_VALUE;
        double tmpDist = Double.MAX_VALUE;
        for (int i = 0; i < numPointsMinusOne; i++) {
            if (!KaboumAlgorithms.isBetweenAndNotColinear(internals[i], internals[i+1], coord)) {
                continue;
            }
            tmpDist = KaboumAlgorithms.distance(coord, internals[i], internals[i+1]);
            if(Math.abs(tmpDist) < Math.abs(minDist)) {
                position = i + 1;
                minDist = tmpDist;
            }
        }
        
        return position;
        
    }
    
    
    public static boolean isBetweenAndNotColinear(KaboumCoordinate a, KaboumCoordinate b, KaboumCoordinate c) {
        double ax = a.x;
        double ay = a.y;
        double bx = b.x;
        double by = b.y;
        double cx = c.x;
        double cy = c.y;
        
        if ((cx >= ax && cx <= bx) || (cx <= ax && cx >= bx)) {
            return true;
        }
        if ((cy >= ay && cy <= by) || (cy <= ay && cy >= by)) {
            return true;
        }
        
        return false;
    }
    
    
    /**
     *
     * Determine if a point c is colinear with segment [ab].
     * This is true if the area of triangle (a,b,c) is null.
     * (From: "Computational Geometry in C" by Joseph O'Rourke)
     * (Image coordinates version)
     *
     * @param a Point a
     * @param b Point b
     * @param c Point c
     *
     */
    public static boolean isColinear(KaboumCoordinate a, KaboumCoordinate b, KaboumCoordinate c) {
        return area2(a, b, c) == 0;
    }
    
    
    /**
     *
     * Determine if a point c is to the left of segment [ab].
     * This is true if the area of triangle (a,b,c) is positive
     * (From: "Computational Geometry in C" by Joseph O'Rourke)
     * (Image coordinates version)
     *
     * @param a Point a
     * @param b Point b
     * @param c Point c
     *
     */
    public static boolean isLeft(KaboumCoordinate a, KaboumCoordinate b, KaboumCoordinate c) {
        return area2(a, b, c) > 0;
    }
    
    
    /**
     *
     * First point and last point MUST be different
     *
     */
    public static boolean autoIntersect(KaboumCoordinate[] internals) {
        
        int numPoints = internals.length;
        
        if (numPoints < 3) {
            return true;
        }
        
        // Suppress the last point if first and last points are the same
        if (internals[0].equals(internals[numPoints - 1])) {
            numPoints = numPoints - 1;
        }
        
        if (numPoints < 3) {
            return true;
        }
        
        org.kaboum.util.KaboumUtil.debug("NOMBRE DE POINTS " + numPoints);
        
        int positionA = -1;
        int positionB = -1;
        int positionC = -1;
        int positionD = -1;
        int tmpPositionExternal = -1;
        int tmpPosition = -1;
        
        for (int i = 0; i < numPoints; i++) {
            
            positionA = i;
            
            positionB = i < numPoints - 1 ? i + 1 : 0;
            
            for (int j = 0; j < numPoints; j++) {
                
                if (i == j) {
                    continue;
                }
                
                if (positionB == j) {
                    continue;
                }
                
                positionC = j;
                
                positionD = j < numPoints - 1 ? j + 1 : 0;
                
                if (positionD == positionA) {
                    continue;
                }
                
                if (intersect(internals[positionA], internals[positionB], internals[positionC], internals[positionD])) {
                    return true;
                }
                
            }
        }
        
        return false;
        
    }
    
    
    /**
     *
     * Return true if segment [ab] intersects segment [cd]
     *
     */
    public static boolean intersect(KaboumCoordinate a, KaboumCoordinate b, KaboumCoordinate c, KaboumCoordinate d) {
        
        // Eliminates improper cases
        if (isColinear(a, b, c) || isColinear(a, b, d) || isColinear(c, d, a) || isColinear(c, d, b)) {
            return false;
        }
        
        return xor(isLeft(a, b, c), isLeft(a, b, d)) && xor(isLeft(c, d, a), isLeft(c, d, b));
        
    }
    
    
    /**
     *
     * Return true if ring1 and ring2 intersect
     *
     */
    public static boolean intersect(KaboumLineString ring1, KaboumLineString ring2) {
        
        // Dumb case
        if (ring1 == null || ring2 == null) {
            return false;
        }
        
        KaboumCoordinate[] internals1 = ring1.getCoordinates();
        KaboumCoordinate[] internals2 = ring2.getCoordinates();
        
        int numPoints1 = internals1.length - 1;
        int numPoints2 = internals2.length - 1;
        
        for (int i = 0; i < numPoints1; i++) {
            
            for (int j = 0; j < numPoints2; j++) {
                
                if (intersect(internals1[i], internals1[i + 1], internals2[j], internals2[j + 1])) {
                    
                    return true;
                    
                }
            }
            
        }
        
        return false;
        
    }
    
    
    /**
     *
     * Return true if ring2 is inside ring1
     *
     */
    public static boolean inside(KaboumLineString ring1, KaboumLineString ring2) {
        
        KaboumCoordinate[] internals1 = ring1.getCoordinates();
        KaboumCoordinate[] internals2 = ring2.getCoordinates();
        
        int numPoints2 = internals2.length;
        
        for (int i = 0; i < numPoints2; i++) {
            
            if (!isPointInPolygon(internals2[i], internals1)) {
                return false;
            }
        }
        
        return true;
        
    }
    
    
    /**
     *
     * Exclusive or: true if exactly one argument is true
     * (From: "Computational Geometry in C" by Joseph O'Rourke)
     *
     * @param x Condition 1
     * @param y Condition 2
     *
     */
    public static boolean xor(boolean x, boolean y) {
        return x ^ y;
    }
    
    
    /**
     *
     * Check that a geometry does not intersect with itself.
     * Algorithm always returns true for Point, MultiPoint, LineString
     * and MultiLineString.
     * For Polygon and MultiPolygon geometries, algorithm checks each exterior and
     * interior ring.
     * Algorithm returns false if (in order):
     *
     *          1. one of the exterior or interior rings intersect with itself
     *          2. exterior ring intersects or is inside another exterior ring
     *          3. interior ring intersects or is ouside from it exterior ring
     *          4. interior ring intersects another interior ring within the
     *             same exterior ring
     *
     */
    public static boolean geometryIsValid(KaboumGeometry geometry) {
        
        // Non polygonal geometry ---> Always true
        if (geometry.getGeometryType().indexOf("Polygon") == -1) {
            return true;
        }
        
        // Multi geometry
        int numGeometries = geometry.getNumGeometries();
        int numHoles = 0;
        boolean intersect = false;
        boolean isInHole = false;
        KaboumGeometry simpleGeometry;
        
        // 1. one of the exterior or interior rings intersect with itself
        for (int i = 0; i < numGeometries; i++) {
            
            simpleGeometry = geometry.getGeometryN(i);
            
            intersect = autoIntersect(((KaboumPolygon) simpleGeometry).getExteriorRing().getCoordinates());
            
            if (intersect) {
                return false;
            }
            
            numHoles = ((KaboumPolygon) simpleGeometry).getNumInteriorRing();
            
            for (int j = 0; j < numHoles; j++) {
                
                intersect = autoIntersect(((KaboumPolygon) simpleGeometry).getInteriorRingN(j).getCoordinates());
                
                if (intersect) {
                    return false;
                }
                
            }
        }
        
        KaboumGeometry geom1;
        KaboumGeometry geom2;
        
        // 2. exterior ring intersects or is inside another exterior ring
        for (int i = 0; i < numGeometries; i++) {
            
            geom1 = geometry.getGeometryN(i);
            
            for (int j = 0; j < numGeometries; j++) {
                
                if (i == j) {
                    continue;
                }
                
                geom2 = geometry.getGeometryN(j);
                
                
                if (inside(((KaboumPolygon) geom1).getExteriorRing(), ((KaboumPolygon) geom2).getExteriorRing())) {
                    return false;
                }
                
                if (intersect(((KaboumPolygon) geom1).getExteriorRing(), ((KaboumPolygon) geom2).getExteriorRing())) {
                    return false;
                }
                
            }
            
        }
        
        KaboumLineString ls1;
        
        // 3. interior ring intersects or is ouside from it exterior ring
        for (int i = 0; i < numGeometries; i++) {
            
            geom1 = geometry.getGeometryN(i);
            numHoles = ((KaboumPolygon) geom1).getNumInteriorRing();
            
            for (int j = 0; j < numHoles; j++) {
                
                ls1 = ((KaboumPolygon) geom1).getInteriorRingN(j);
                
                if (intersect(((KaboumPolygon) geom1).getExteriorRing(), ls1)) {
                    return false;
                }
                
                if (!inside(((KaboumPolygon) geom1).getExteriorRing(), ls1)) {
                    return false;
                }
            }
            
        }
        
        KaboumLineString hole1;
        KaboumLineString hole2;
        
        // 4. interior ring intersects another interior ring within the same exterior ring
        for (int i = 0; i < numGeometries; i++) {
            
            geom1 = geometry.getGeometryN(i);
            numHoles = ((KaboumPolygon) geom1).getNumInteriorRing();
            
            for (int j = 0; j < numHoles; j++) {
                
                hole1 = ((KaboumPolygon) geom1).getInteriorRingN(j);
                
                for (int k = 0; k < numHoles; k++) {
                    
                    if (k == j) {
                        continue;
                    }
                    
                    hole2 = ((KaboumPolygon) geom1).getInteriorRingN(k);
                    
                    if (intersect(hole1, hole2)) {
                        return false;
                    }
                    
                }
            }
            
        }
        
        return true;
        
    }
    
    
    /**
     *
     * Check if a geometry surrounds other geometries.
     * Algorithm only checks for Polygon and MultiPolygon geometries.
     *
     * Algorithm returns false if :
     *
     *          1. geometry does not surround other polygonals geometries
     *          2. geometry is not a Polygon or MultiPolygon
     *
     */
    public static boolean geometrySurroundsOther(KaboumGeometryGlobalDescriptor ggd, KaboumGeometryGlobalDescriptor[] ggds) {
        
        KaboumGeometry geometry = ggd.geometry;
        
        // Non polygonal geometry ---> Always false
        if (geometry.getGeometryType().indexOf("Polygon") == -1) {
            return false;
        }
        
        // Multi geometry
        int numGeometries = ggds.length;
        int numExteriors = -1;
        int numCoordinates = -1;
        int countInside = 0;
        KaboumGeometry simpleGeometry;
        KaboumGeometry multipleGeometry;
        KaboumCoordinate[] coordinates;
        
        // 1. Check surround only for Exterior rings
        for (int i = 0; i < numGeometries; i++) {
            
            if (ggds[i].id.equals(ggd.id)) {
                continue;
            }
            
            if (!ggds[i].pd.isComputed()) {
                continue;
            }
            
            multipleGeometry = ggds[i].geometry;
            
            if (multipleGeometry.getGeometryType().indexOf("Polygon") == -1) {
                continue;
            }
            
            numExteriors = multipleGeometry.getNumGeometries();
            
            for (int j = 0; j < numExteriors; j++) {
                
                simpleGeometry = multipleGeometry.getGeometryN(j);
                coordinates = ((KaboumPolygon) simpleGeometry).getExteriorCoordinates();
                numCoordinates = coordinates.length;
                
                countInside = 0;
                
                for (int k = 0; k < numCoordinates; k++) {
                    
                    if (KaboumAlgorithms.isPointInPolygon(coordinates[k], geometry)) {
                        countInside++;
                    }
                    else {
                        break;
                    }
                }
                
                // One of the exterior ring is inside geometry
                if (countInside == numCoordinates) {
                    return true;
                }
                
            }
            
        }
        
        return false;
        
    }
    
    
    /**
     *
     * Check if a geometry intersects other geometries.
     * Algorithm only checks for Polygon and MultiPolygon geometries.
     *
     * Algorithm returns false if :
     *
     *          1. geometry does not intersect other polygonals geometries
     *          2. geometry is not a Polygon or MultiPolygon
     *
     */
    public static boolean geometryIntersectsOther(KaboumGeometryGlobalDescriptor ggd, KaboumGeometryGlobalDescriptor[] ggds) {
        
        KaboumGeometry geometry = ggd.geometry;
        
        // Non polygonal geometry ---> Always false
        if (geometry.getGeometryType().indexOf("Polygon") == -1) {
            return false;
        }
        
        // Multi geometry
        int numOthers = ggds.length;
        int numCurrentGeometries = geometry.getNumGeometries();
        int numHoles = -1;
        int numExteriors = -1;
        int numSimpleGeometries = -1;
        int countInside = 0;
        KaboumGeometry simpleGeometry;
        KaboumGeometry simpleCurrentGeometry;
        KaboumGeometry multipleGeometry;
        KaboumLineString simpleHole;
        
        // 1. Check intersection
        
        for (int i = 0; i < numOthers; i++) {
            
            if (ggds[i].id.equals(ggd.id)) {
                continue;
            }
            
            if (!ggds[i].pd.isComputed()) {
                continue;
            }
            
            multipleGeometry = ggds[i].geometry;
            
            if (multipleGeometry.getGeometryType().indexOf("Polygon") == -1) {
                continue;
            }
            
            numSimpleGeometries = multipleGeometry.getNumGeometries();
            
            for (int j = 0; j < numSimpleGeometries; j++) {
                
                simpleGeometry = multipleGeometry.getGeometryN(j);
                
                for (int k = 0; k < numCurrentGeometries; k++) {
                    
                    simpleCurrentGeometry = geometry.getGeometryN(k);
                    
                    // Exterior ring
                    if (KaboumAlgorithms.intersect(((KaboumPolygon) simpleCurrentGeometry).getExteriorRing(), ((KaboumPolygon) simpleGeometry).getExteriorRing())) {
                        return true;
                    }
                    
                    // Holes
                    numHoles = ((KaboumPolygon) simpleGeometry).getNumInteriorRing();
                    
                    for (int l = 0; l < numHoles; l++) {
                        
                        simpleHole = ((KaboumPolygon) simpleGeometry).getInteriorRingN(l);
                        
                        if (KaboumAlgorithms.intersect(simpleHole, ((KaboumPolygon) simpleCurrentGeometry).getExteriorRing())) {
                            return true;
                        }
                        
                    }
                }
                
            }
        }
        
        return false;
        
    }
    
    
    /**
     *
     * Check if a geometry is inside other geometries.
     * Algorithm only checks for Polygon and MultiPolygon geometries.
     *
     * Algorithm returns false if :
     *
     *          1. geometry is not inside other polygonals geometries
     *          2. geometry is not a Polygon or MultiPolygon
     *
     */
    public static boolean geometryIsInsideOther(KaboumGeometryGlobalDescriptor ggd, KaboumGeometryGlobalDescriptor[] ggds) {
        
        KaboumGeometry geometry = ggd.geometry;
        
        // Non polygonal geometry ---> Always false
        if (geometry.getGeometryType().indexOf("Polygon") == -1) {
            return false;
        }
        
        int numGeometries = ggds.length;
        KaboumGeometryGlobalDescriptor[] tmpGGDArray = new KaboumGeometryGlobalDescriptor[1];
        tmpGGDArray[0] = ggd;
        
        boolean result = false;
        
        for (int i = 0; i < numGeometries; i++) {
            
            result = KaboumAlgorithms.geometrySurroundsOther(ggds[i], tmpGGDArray);
            
            if (result) {
                return true;
            }
            
        }
        
        return false;
    }
    
}
