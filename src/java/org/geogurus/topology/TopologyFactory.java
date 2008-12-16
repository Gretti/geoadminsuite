/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geogurus.topology;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 *
 * Title:
 * Description:  Provides a static class to perform binary CAG
 * (Constructive Area Geometry) operations
 * @author Nicolas Ribot
 * @version 1.0
 *
 */
public class TopologyFactory {
    
    private static double precision = 1.0E-6;

    
    /**
     *
     * This method returns the result shape of the intersection
     * of two wkt geometry
     *
     * @param wkt1 Well Known Text representation of first object
     * @param wkt2 Well Known Text representation of second object
     *
     */
    public static Shape intersect(String wkt1, String wkt2) {
        Area geom1 = new Area(buildGeneralPath(wkt1));
        Area geom2 = new Area(buildGeneralPath(wkt2));
        geom1.intersect(geom2);
        
        //  Test wether or not the intersected object exists
        if (geom1.isEmpty()) {
            return null;
        }
        
        return removeDuplicatePoints((Shape) geom1);
        //return geom1;
        
    }
    
    /**
     *
     * This method returns the result shape of the union
     * of two wkt geometry
     *
     * @param wkt1 Well Known Text representation of first object
     * @param wkt2 Well Known Text representation of second object
     *
     */
    public static Shape union(String wkt1, String wkt2) {
        Area geom1 = new Area(buildGeneralPath(wkt1));
        Area geom2 = new Area(buildGeneralPath(wkt2));
        geom1.add(geom2);
        
        //  Test wether or not the intersected object exists
        if (geom1.isEmpty()) {
            return null;
        }
        
        return removeDuplicatePoints((Shape) geom1);
        //return geom1;
        
    }
    
    /**
     *
     * This method returns the result shape of the union
     * of two wkt geometry
     *
     * @param wkt1 Well Known Text representation of first object
     * @param wkt2 Well Known Text representation of second object
     *
     */
    public static Shape subtract(String wkt1, String wkt2) {
        Area geom1 = new Area(buildGeneralPath(wkt1));
        Area geom2 = new Area(buildGeneralPath(wkt2));
        geom1.subtract(geom2);
        
        //  Test wether or not the intersected object exists
        if (geom1.isEmpty()) {
            return null;
        }
        
System.out.println("area is a polygon ? " + geom1.isPolygonal());        
        return removeDuplicatePoints((Shape) geom1);
        //return geom1;
    }

    /**
     *
     * This method returns the result shape of the symmetric difference
     * of two wkt geometry
     *
     * @param wkt1 Well Known Text representation of first object
     * @param wkt2 Well Known Text representation of second object
     *
     */
    public static Shape symmetricDifference(String wkt1, String wkt2) {
        Area geom1 = new Area(buildGeneralPath(wkt1));
        Area geom2 = new Area(buildGeneralPath(wkt2));
        geom1.exclusiveOr(geom2);
        
        //  Test wether or not the intersected object exists
        if (geom1.isEmpty()) {
            return null;
        }
        
        return removeDuplicatePoints((Shape) geom1);
        //return geom1;
        
    }
    
    /**
     *
     * Precision operators
     *
     */
    public static boolean eqP(double a, double b) {
        return Math.abs(a-b) <= precision;
    }
    
    
    /**
     *
     * Remove duplicate points in the given Shape/Area
     * according to this precision
     *
     */
    private static Shape removeDuplicatePoints(Shape a) {
        
        PathIterator pi = a.getPathIterator(null);
        boolean firstTime = true;
        
        //to get path points
        double[] points = new double[6];
        GeneralPath gp = new GeneralPath();
        
        // first time: get the first point
        pi.currentSegment(points);
        
        Point2D.Double previous = new Point2D.Double(points[0], points[1]);
        // first point, stored to add it at the end, if necessary
        Point2D.Double first = new Point2D.Double(points[0], points[1]);
        Point2D.Double current = null;
        
System.out.println("First point: " + first.getX() + " " + first.getY());        
        pi.next();
        //all other points
        while(!pi.isDone()) {
            
            pi.currentSegment(points);
            
            current = new Point2D.Double(points[0], points[1]);
            
            // test if currrent point is equal to previous point, with precision
            if ( eqP(current.getX(), previous.getX()) && eqP(current.getY(), previous.getY()) ) {
System.out.println("found duplicate points: coords x: " + current.getX() + " y: " + current.getY());
                previous = current;
                pi.next();
                continue;
            }
            
            // current point is different from previous, can build a new Line2D
            gp.append(new Line2D.Double(previous, current), true);
            previous = current;
            pi.next();
        }
        // tests if last point = first point, if not, add first point
        if ( !eqP(current.getX(), first.getX()) || !eqP(current.getY(), first.getY()) ) {
            gp.append(new Line2D.Double(current,first), true);
        }
        return gp;
    }
    
    /**
     *
     *  Constructs a GeneralPath from the given kaboum geometric
     *  object representation
     *  @parameter wkt Well Known Text representation fot this geometry
     *
     */
    private static Shape buildGeneralPath(String wkt) {
        
        GeneralPath gp = new GeneralPath();
        Vector points = new Vector();
        
        //
        // Parse the WKT representation
        //
        StringTokenizer tk2;
        StringTokenizer tk = new StringTokenizer(wkt, "(");
        String type = tk.nextToken().trim();
        
        //
        // Little trick from ElNico to get only the value field
        // WARNING: This does not work for multipolygons or polygons
        // with an interior
        //
        int begin = wkt.lastIndexOf("(");
        int end = wkt.indexOf(")");
        String str = wkt.substring(begin + 1, end);
        
        tk = new StringTokenizer(str, ",");
        
        // extract coordinates from comma-separated list of coords
        while (tk.hasMoreElements()) {
            tk2 = new StringTokenizer(tk.nextToken());
            try {
                Point2D pt = new Point2D.Double(new Double(tk2.nextToken()).doubleValue(),
                new Double(tk2.nextToken()).doubleValue());
                points.addElement(pt);
            } catch (NumberFormatException nfe) {}
        }
        
        // loop through all points to create Line2D and add them to gp
        int max = points.size();
        for (int i = 0; i < max-1; i++) {
            Line2D.Double l = new Line2D.Double((Point2D)points.elementAt(i), (Point2D)points.elementAt(i+1));
            gp.append(l, true);
        }
        
        // closes the path
        Line2D.Double l = new Line2D.Double((Point2D)points.elementAt(max-1), (Point2D)points.elementAt(0));
        gp.append(l, true);
        
        return gp;
    }
    
    
    /**
     *
     * Builds a valid wkt geometry representation from the given Area object.
     *
     */
    public static String getWKT(Shape shp) {
        
        if (shp == null) {
            return null;
        }
        
        StringBuffer kg = new StringBuffer("");
        StringBuffer firstCoord = new StringBuffer("");
        PathIterator pi = shp.getPathIterator(null);
        int status = -1;
        boolean first = true;
        double[] coords = new double[6];
        
        //
        // Extract each coordinate from the area's PathIterator.
        // If the pathIteraror contains subpath, then a MULTIPOLYGON
        // WKT representation is generated instead of a POLYGON one.
        //
        boolean multi = false;
        while (!pi.isDone()) {
            status = pi.currentSegment(coords);
            if (status == PathIterator.SEG_MOVETO) {
                if (multi) {
                    // a new polygon
                    kg.append(",");
                }
                kg.append("(");
                first = true;
                firstCoord = new StringBuffer(coords[0] + " " + coords[1]);
            }
            if (!first) {
                kg.append(",");
            }
            first = false;
            if (status == PathIterator.SEG_CLOSE) {
System.out.println("SEG_CLOSE: appending )");                
                kg.append(firstCoord.toString());
                kg.append(")");
                multi = true;
            }
            else {
                kg.append(coords[0]).append(" ").append(coords[1]);
            }
            pi.next();
        }

        // MULTIPOLYGON case
        if (kg.toString().indexOf("),(") != -1) {
            return "MULTIPOLYGON((" + kg.toString() + "))";
        }
        
        // remove last ","
        /*
        if (kg.toString().lastIndexOf(")") != kg.toString().length()-1) {
            return "POLYGON(" + kg.toString() + ")";
        }
        return "POLYGON(" + kg.toString() + "))";
         */
        return "POLYGON(" + kg.toString() + ")";
    }

    /**
     *
     * Take a MULTIPOLYGON string and return
     * the coresponding vector of POLYGON strings
     *
     */
    public static Vector splitWKTToPolygon(String wkt) {

        // Instantiate the returned vector
        Vector vec = new Vector();
        
        // If the input wkt is not a MULTIPOLYGON return the input wkt
        if (wkt.indexOf("MULTIPOLYGON") == -1) {
            vec.addElement(wkt);
            return vec;
        }
        
        //
        // Assume the input wkt is MULTIPOLYGON
	// WARNING: This code does not work for objects with an interior
	//
	int begin = wkt.indexOf("(((");
	int end = wkt.indexOf(")))");
	String str = wkt.substring(begin + 3, end);
        
	StringTokenizer tk = new StringTokenizer(str, ")(");
        StringBuffer sb = null;
	while (tk.hasMoreTokens()) {
            sb = new StringBuffer("POLYGON((");
            sb.append(tk.nextToken());
            sb.append("))");
            vec.addElement(sb.toString());
        }

        return vec;
        
    }
    
}