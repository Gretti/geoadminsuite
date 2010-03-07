/*
 *
 * Utility class for Kaboum project.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is mostly a rewrite of WKTWriter class from the Java Topology
 * Suite (JTS) package.
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
package org.kaboum.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.Writer;
import java.io.StringWriter;

import org.kaboum.util.KaboumList;

import org.kaboum.geom.KaboumLineString;
import org.kaboum.geom.KaboumMultiPolygon;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumPolygon;
import org.kaboum.geom.KaboumPoint;
import org.kaboum.geom.KaboumMultiPoint;
import org.kaboum.geom.KaboumGeometryCollection;
import org.kaboum.geom.KaboumMultiLineString;


public class KaboumWKTWriter {
    
    /** Precision model */
    private KaboumPrecisionModel precisionModel;
    
    private int level = 0;
    
    
    /**
     *
     * Constructor
     *
     */
    public KaboumWKTWriter(KaboumPrecisionModel precisionModel) {
        this.precisionModel = precisionModel;
    }

    
    /**
     *
     * Converts a <code>Geometry</code> to its Well-known Text representation.
     *
     * @param  geometry  a <code>Geometry</code> to process
     * @return           a <Geometry Tagged Text> string (see the OpenGIS Simple
     *      Features Specification)
     *
     */
    public String write(KaboumGeometry geometry) throws IOException {
        
        Writer sw = new StringWriter();
        try {
            appendGeometryTaggedText(geometry, sw);
        }
        catch (IOException ex) {
            throw new IOException(ex.toString());
        }
        return sw.toString();
    }
    
    
    /**
     *
     * Converts a <code>Geometry</code> to &lt;Geometry Tagged Text&gt; format,
     * then appends it to the writer.
     *
     * @param  geometry  the <code>Geometry</code> to process
     * @param  writer    the output writer to append to
     *
     */
    protected void appendGeometryTaggedText(KaboumGeometry geometry, Writer writer)
    throws IOException {
            
        if (geometry instanceof KaboumPoint) {
            KaboumPoint point = (KaboumPoint) geometry;
            appendPointTaggedText(point.getCoordinate(), writer);
        }
        else if (geometry instanceof KaboumLineString) {
            appendLineStringTaggedText((KaboumLineString) geometry, writer);
        }
        else if (geometry instanceof KaboumPolygon) {
            appendPolygonTaggedText((KaboumPolygon) geometry, writer);
        }
        else if (geometry instanceof KaboumMultiPoint) {
            appendMultiPointTaggedText((KaboumMultiPoint) geometry, writer);
        }
        else if (geometry instanceof KaboumMultiLineString) {
            appendMultiLineStringTaggedText((KaboumMultiLineString) geometry, writer);
        }
        else if (geometry instanceof KaboumMultiPolygon) {
            appendMultiPolygonTaggedText((KaboumMultiPolygon) geometry, writer);
        }
        else if (geometry instanceof KaboumGeometryCollection) {
            appendGeometryCollectionTaggedText((KaboumGeometryCollection) geometry, writer);
        }
        else {
            throw new IOException("ERROR ! : unknown Geometry");
        }
    }
    
    
    /**
     *
     * Converts a <code>Coordinate</code> to &lt;Point Tagged Text&gt; format,
     * then appends it to the writer.
     *
     * @param  coordinate      the <code>Coordinate</code> to process
     * @param  writer          the output writer to append to
     *
     */
    protected void appendPointTaggedText(KaboumCoordinate coordinate, Writer writer)
    throws IOException {
        writer.write("POINT");
        appendPointText(coordinate, writer);
    }
    
    
    /**
     *
     * Converts a <code>LineString</code> to &lt;LineString Tagged Text&gt;
     * format, then appends it to the writer.
     *
     * @param  lineString  the <code>LineString</code> to process
     * @param  writer      the output writer to append to
     *
     */
    protected void appendLineStringTaggedText(KaboumLineString lineString, Writer writer)
    throws IOException {
        writer.write("LINESTRING");
        appendLineStringText(lineString, writer);
    }
    
    
    /**
     *

     * Converts a <code>Polygon</code> to &lt;Polygon Tagged Text&gt; format,
     * then appends it to the writer.
     *
     * @param  polygon  the <code>Polygon</code> to process
     * @param  writer   the output writer to append to
     *
     */
    protected void appendPolygonTaggedText(KaboumPolygon polygon, Writer writer)
    throws IOException {
        writer.write("POLYGON");
        appendPolygonText(polygon, writer);
    }
    
    
    /**
     *
     * Converts a <code>MultiPoint</code> to &lt;MultiPoint Tagged Text&gt;
     * format, then appends it to the writer.
     *
     * @param  multipoint  the <code>MultiPoint</code> to process
     * @param  writer      the output writer to append to
     *
     */
    protected void appendMultiPointTaggedText(KaboumMultiPoint multipoint, Writer writer)
    throws IOException {
        writer.write("MULTIPOINT");
        appendMultiPointText(multipoint, writer);
    }
    
    
    /**
     *
     * Converts a <code>MultiLineString</code> to &lt;MultiLineString Tagged
     * Text&gt; format, then appends it to the writer.
     *
     * @param  multiLineString  the <code>MultiLineString</code> to process
     * @param  writer           the output writer to append to
     *
     */
    protected void appendMultiLineStringTaggedText(KaboumMultiLineString multiLineString, Writer writer)
    throws IOException {
        writer.write("MULTILINESTRING");
        appendMultiLineStringText(multiLineString, writer);
    }
    
    
    /**
     *
     * Converts a <code>MultiPolygon</code> to &lt;MultiPolygon Tagged Text&gt;
     * format, then appends it to the writer.
     *
     * @param  multiPolygon  the <code>MultiPolygon</code> to process
     * @param  writer        the output writer to append to
     *
     */
    protected void appendMultiPolygonTaggedText(KaboumMultiPolygon multiPolygon, Writer writer)
    throws IOException {
        writer.write("MULTIPOLYGON");
        appendMultiPolygonText(multiPolygon, writer);
    }
    
    
    /**
     *
     * Converts a <code>GeometryCollection</code> to &lt;GeometryCollection
     * Tagged Text&gt; format, then appends it to the writer.
     *
     * @param  geometryCollection  the <code>GeometryCollection</code> to process
     * @param  writer              the output writer to append to
     *
     */
    protected void appendGeometryCollectionTaggedText(KaboumGeometryCollection geometryCollection, Writer writer)
    throws IOException {
        writer.write("GEOMETRYCOLLECTION");
        appendGeometryCollectionText(geometryCollection, writer);
    }
    
    
    /**
     *
     * Converts a <code>Coordinate</code> to &lt;Point Text&gt; format, then
     * appends it to the writer.
     *
     * @param  coordinate      the <code>Coordinate</code> to process
     * @param  writer          the output writer to append to
     *
     */
    protected void appendPointText(KaboumCoordinate coordinate, Writer writer)
    throws IOException {
        if (coordinate == null) {
            writer.write(" EMPTY");
        }
        else {
            writer.write("(");
            appendCoordinate(coordinate, writer);
            writer.write(")");
        }
    }
    
    
    /**
     *
     * Converts a <code>Coordinate</code> to &lt;Point&gt; format, then appends
     * it to the writer.
     *
     * @param  coordinate      the <code>Coordinate</code> to process
     * @param  writer          the output writer to append to
     *
     */
    protected void appendCoordinate(KaboumCoordinate coordinate, Writer writer)
    throws IOException {
        KaboumCoordinate external = this.precisionModel.toExternal(coordinate);
        writer.write(external.x + " " + external.y);
    }
    
    
    /**
     *
     * Converts a <code>LineString</code> to &lt;LineString Text&gt; format, then
     * appends it to the writer.
     *
     * @param  lineString  the <code>LineString</code> to process
     * @param  writer      the output writer to append to
     *
     */
    protected void appendLineStringText(KaboumLineString lineString, Writer writer)
    throws IOException {
        if (lineString.isEmpty()) {
            writer.write(" EMPTY");
        }
        else {
            writer.write("(");
            for (int i = 0; i < lineString.getNumPoints(); i++) {
                if (i > 0) {
                    writer.write(",");
                }
                appendCoordinate(lineString.getCoordinateN(i), writer);
            }
            writer.write(")");
        }
    }
    
    
    /**
     *
     * Converts a <code>Polygon</code> to &lt;Polygon Text&gt; format, then
     * appends it to the writer.
     *
     * @param  polygon  the <code>Polygon</code> to process
     * @param  writer   the output writer to append to
     *
     */
    protected void appendPolygonText(KaboumPolygon polygon, Writer writer)
    throws IOException {
        if (polygon.isEmpty()) {
            writer.write(" EMPTY");
        }
        else {
            writer.write("(");
            appendLineStringText(polygon.getExteriorRing(), writer);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                writer.write(",");
                appendLineStringText(polygon.getInteriorRingN(i), writer);
            }
            writer.write(")");
        }
    }
    
    
    /**
     *
     * Converts a <code>MultiPoint</code> to &lt;MultiPoint Text&gt; format, then
     * appends it to the writer.
     *
     * @param  multiPoint  the <code>MultiPoint</code> to process
     * @param  writer      the output writer to append to
     *
     */
    protected void appendMultiPointText(KaboumMultiPoint multiPoint, Writer writer)
    throws IOException {
        if (multiPoint.isEmpty()) {
            writer.write(" EMPTY");
        }
        else {
            writer.write("(");
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                if (i > 0) {
                    writer.write(",");
                }
                appendCoordinate(((KaboumPoint) multiPoint.getGeometryN(i)).getCoordinate(), writer);
            }
            writer.write(")");
        }
    }
    
    
    /**
     *
     * Converts a <code>MultiLineString</code> to &lt;MultiLineString Text&gt;
     * format, then appends it to the writer.
     *
     * @param  multiLineString  the <code>MultiLineString</code> to process
     * @param  writer           the output writer to append to
     *
     */
    protected void appendMultiLineStringText(KaboumMultiLineString multiLineString, Writer writer)
    throws IOException {
        if (multiLineString.isEmpty()) {
            writer.write(" EMPTY");
        }
        else {
            writer.write("(");
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                if (i > 0) {
                    writer.write(",");
                }
                appendLineStringText((KaboumLineString) multiLineString.getGeometryN(i), writer);
            }
            writer.write(")");
        }
    }
    
    
    /**
     *
     * Converts a <code>MultiPolygon</code> to &lt;MultiPolygon Text&gt; format,
     * then appends it to the writer.
     *
     * @param  multiPolygon  the <code>MultiPolygon</code> to process
     * @param  writer        the output writer to append to
     *
     */
    protected void appendMultiPolygonText(KaboumMultiPolygon multiPolygon, Writer writer)
    throws IOException {
        if (multiPolygon.isEmpty()) {
            writer.write(" EMPTY");
        }
        else {
            writer.write("(");
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                if (i > 0) {
                    writer.write(",");
                }
                appendPolygonText((KaboumPolygon) multiPolygon.getGeometryN(i), writer);
            }
            writer.write(")");
        }
    }
    
    
    /**
     *
     * Converts a <code>GeometryCollection</code> to &lt;GeometryCollectionText&gt;
     * format, then appends it to the writer.
     *
     * @param  geometryCollection  the <code>GeometryCollection</code> to process
     * @param  writer              the output writer to append to
     *
     */
    protected void appendGeometryCollectionText(KaboumGeometryCollection geometryCollection, Writer writer)
    throws IOException {
        if (geometryCollection.isEmpty()) {
            writer.write(" EMPTY");
        }
        else {
            writer.write("(");
            for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                if (i > 0) {
                    writer.write(", ");
                }
                appendGeometryTaggedText(geometryCollection.getGeometryN(i), writer);
            }
            writer.write(")");
        }
    }
        
}

