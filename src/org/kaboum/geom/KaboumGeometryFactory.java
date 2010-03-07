/*
 *
 * Class KaboumGeometryFactory from the Kaboum project.
 * This class is infered from GeometryFactory class from the
 * the Java Topology Suite project (www.vividsolutions.com).
 *
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is inspired by JTS GeometryFactory class
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
import org.kaboum.util.KaboumExtent;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumUtil;
import org.kaboum.util.KaboumList;

/**
 *
 * Basic implementation of <code>GeometryFactory</code>.
 *
 */
public class KaboumGeometryFactory implements Serializable  {
    
    /**
     *
     * Converts the <code>KaboumList</code> to an array.
     *
     * @param  points  the <code>KaboumList</code> to convert
     *
     */
    public static KaboumPoint[] toPointArray(KaboumList points) {
        return points.toPointArray();
    }
    
    
    /**
     *
     * Converts the <code>KaboumList</code> to an array.
     *
     * @param  geometrys  the KaboumList of <code>Geometrys</code> to convert
     *
     */
    public static KaboumGeometry[] toGeometryArray(KaboumList geometrys) {
        return geometrys.toGeometryArray();
    }
    
    
    /**
     *
     * Converts the <code>KaboumList</code> to an array.
     *
     *@param  linearRings  the <code>KaboumList</code> to convert
     *
     */
    public static KaboumLinearRing[] toLinearRingArray(KaboumList linearRings) {
        return linearRings.toLinearRingArray();
    }
    
    
    /**
     *
     * Converts the <code>KaboumList</code> to an array.
     *
     *@param  lineStrings  the <code>KaboumList</code> to convert
     *
     */
    public static KaboumLineString[] toLineStringArray(KaboumList lineStrings) {
        return lineStrings.toLineStringArray();
    }
    
    
    /**
     *
     * Converts the <code>KaboumList</code> to an array.
     *
     *@param  polygons  the <code>KaboumList</code> to convert
     *
     */
    public static KaboumPolygon[] toPolygonArray(KaboumList polygons) {
        return polygons.toPolygonArray();
    }
    
    
    /**
     *
     * Converts the <code>KaboumList</code> to an array.
     *
     * @param  multiPolygons  the <code>KaboumList</code> to convert
     *
     */
    public static KaboumMultiPolygon[] toMultiPolygonArray(KaboumList multiPolygons) {
        return multiPolygons.toMultiPolygonArray();
    }
    
    
    /**
     *
     * Converts the <code>KaboumList</code> to an array.
     *
     * @param  multiLineStrings  the <code>KaboumList</code> to convert
     *
     */
    public static KaboumMultiLineString[] toMultiLineStringArray(KaboumList multiLineStrings) {
        return multiLineStrings.toMultiLineStringArray();
    }
    
    
    /**
     *
     * Converts the <code>KaboumList</code> to an array.
     *
     * @param  multiPoints  the <code>KaboumList</code> to convert
     *
     */
    public static KaboumMultiPoint[] toMultiPointArray(KaboumList multiPoints) {
        return multiPoints.toMultiPointArray();
    }
    
    
    /**
     *
     * If the <code>Extent</code> is a null <code>Extent</code>, returns an
     * empty <code>Point</code>. If the <code>Extent</code> is a point, returns
     * a non-empty <code>Point</code>. If the <code>Extent</code> is a
     * rectangle, returns a <code>Polygon</code> whose points are (minx, miny),
     *  (maxx, miny), (maxx, maxy), (minx, maxy), (minx, miny).
     *
     * @param  extent        the <code>Extent</code> to convert to a <code>Geometry</code>
     *
     */
    public static KaboumGeometry toGeometry(KaboumExtent extent) {
        
        if (extent.isNull()) {
            return new KaboumPoint(null);
        }
        if (extent.xMin == extent.xMax && extent.yMin == extent.yMax) {
            return new KaboumPoint(new KaboumCoordinate(extent.xMin, extent.yMin));
        }
        return new KaboumPolygon(new KaboumLinearRing(new KaboumCoordinate[]{
            new KaboumCoordinate(extent.xMin, extent.yMin),
            new KaboumCoordinate(extent.xMax, extent.yMin),
            new KaboumCoordinate(extent.xMax, extent.yMax),
            new KaboumCoordinate(extent.xMin, extent.yMax),
            new KaboumCoordinate(extent.xMin, extent.yMin)
        }));
    }
    
    
    public static KaboumPoint createPoint(KaboumCoordinate coordinate) {
        return new KaboumPoint(coordinate);
    }

    
    public static KaboumMultiLineString createMultiLineString(KaboumLineString[] lineStrings) {
        return new KaboumMultiLineString(lineStrings);
    }
    
    
    public static KaboumGeometryCollection createGeometryCollection(KaboumGeometry[] geometries) {
        return new KaboumGeometryCollection(geometries);
    }
    
    
    public static KaboumMultiPolygon createMultiPolygon(KaboumPolygon[] polygons) {
        return new KaboumMultiPolygon(polygons);
    }
    
    
    public static KaboumLinearRing createLinearRing(KaboumCoordinate[] coordinates) {
    
        KaboumLinearRing linearRing = new KaboumLinearRing(coordinates);
        
        if (coordinates != null
            && coordinates.length > 0
            && !coordinates[0].equals(coordinates[coordinates.length - 1])) {
            KaboumUtil.debug(" Error creating LinearRing : first and last point differ");
            return null;
        }
        return linearRing;
    }
    
    
    public static KaboumMultiPoint createMultiPoint(KaboumPoint[] point) {
        return new KaboumMultiPoint(point);
    }
    
    
    public static KaboumMultiPoint createMultiPoint(KaboumCoordinate[] coordinates) {
        if (coordinates == null) {
            coordinates = new KaboumCoordinate[]{};
        }
        KaboumList points = new KaboumList();
        for (int i = 0; i < coordinates.length; i++) {
            points.addElement(createPoint(coordinates[i]));
        }
        return createMultiPoint(points.toPointArray());
    }
    
    
    public static KaboumPolygon createPolygon(KaboumLinearRing shell, KaboumLinearRing[] holes) {
        return new KaboumPolygon(shell, holes);

    }
    
    
    /**
     *
     * Build an appropriate <code>Geometry</code>, <code>MultiGeometry</code>, or
     * <code>GeometryCollection</code> to contain the <code>Geometry</code>s in
     * it; for example,<br>
     *
     *  <ul>
     *    <li> If <code>geomKaboumList</code> contains a single <code>Polygon</code>,
     *    the <code>Polygon</code> is returned.
     *    <li> If <code>geomKaboumList</code> contains several <code>Polygon</code>s, a
     *    <code>MultiPolygon</code> is returned.
     *    <li> If <code>geomKaboumList</code> contains some <code>Polygon</code>s and
     *    some <code>LineString</code>s, a <code>GeometryCollection</code> is
     *    returned.
     *    <li> If <code>geomKaboumList</code> is empty, an empty <code>GeometryCollection</code>
     *    is returned
     *  </ul>
     *
     * @param  geomKaboumList  the <code>Geometry</code>s to combine
     *
     */
    public static KaboumGeometry buildGeometry(KaboumList geomKaboumList) {
    
        Class geomClass = null;
        boolean isHeterogeneous = false;
        boolean isCollection = geomKaboumList.size() > 1;
        int numGeom = geomKaboumList.size();
        
        for (int i = 0; i < numGeom; i++) {
            KaboumGeometry geom = (KaboumGeometry) geomKaboumList.elementAt(i);
            Class partClass = geom.getClass();
            if (geomClass == null) {
                geomClass = partClass;
            }
            if (partClass != geomClass) {
                isHeterogeneous = true;
            }
        }
    
        // for the empty geometry, return an empty GeometryCollection
        if (geomClass == null) {
            return createGeometryCollection(null);
        }
        if (isHeterogeneous) {
            return createGeometryCollection(toGeometryArray(geomKaboumList));
        }
        KaboumGeometry geom0 = (KaboumGeometry) geomKaboumList.elementAt(0);
        if (isCollection) {
            if (geom0 instanceof KaboumPolygon) {
                return createMultiPolygon(toPolygonArray(geomKaboumList));
            }
            else if (geom0 instanceof KaboumLineString) {
                return createMultiLineString(toLineStringArray(geomKaboumList));
            }
            else if (geom0 instanceof KaboumPoint) {
                return createMultiPoint(toPointArray(geomKaboumList));
            }
            return null;
        }
         
        return geom0;
    }
    
    
    public static KaboumLineString createLineString(KaboumCoordinate[] coordinates) {
        return new KaboumLineString(coordinates);
    }
    
}

