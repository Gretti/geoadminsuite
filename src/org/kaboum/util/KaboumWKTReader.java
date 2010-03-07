/*
 *
 * Utility class for Kaboum project.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * This class is mostly a rewrite of WKTReader class from the
 * Java Topology Suite (JTS) package
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

 *
 */
package org.kaboum.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.kaboum.util.KaboumList;

import org.kaboum.geom.KaboumLineString;
import org.kaboum.geom.KaboumMultiPolygon;
import org.kaboum.geom.KaboumGeometryFactory;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumPolygon;
import org.kaboum.geom.KaboumPoint;
import org.kaboum.geom.KaboumMultiPoint;
import org.kaboum.geom.KaboumGeometryCollection;
import org.kaboum.geom.KaboumMultiLineString;
import org.kaboum.geom.KaboumLinearRing;


/**
 *
 * Converts a Well-known Text string to a <code>Geometry</code>. The Well-known
 * Text format is defined in the <A HREF="http://www.opengis.org/techno/specs.htm">
 * OpenGIS Simple Features Specification for SQL</A> . <P>
 *
 * The <code>WKTReader</code> assume that the input numbers are in external representation.
 * It will convert the input numbers to the internal representation
 *
 */
public class KaboumWKTReader {
    
    
    /** Precision model */
    private KaboumPrecisionModel precisionModel;
    
    
    /**
     *
     * Default constructor
     *
     */
    public KaboumWKTReader(KaboumPrecisionModel precisionModel) {
        this.precisionModel = precisionModel;
    }
    
    
    /**
     *
     * Converts a Well-known Text representation to a <code>Geometry</code>.
     *
     * @param  wellKnownText    a WKT string
     *
     * @throws Exception        if a parsing problem occurs
     *
     */
    public KaboumGeometry read(String wellKnownText) throws Exception {
        
        if (wellKnownText == null) {
            return null;
        }
        
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(wellKnownText.toUpperCase()));
        try {
            return readGeometryTaggedText(tokenizer);
        }
        catch (IOException e) {
            throw new Exception(e.toString());
        }
    }
    
    
    /**
     *
     * Returns the next array of <code>Coordinate</code>s in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next element returned by the stream should be "(" (the
     *      beginning of "(x1 y1, x2 y2, ..., xn yn)") or "EMPTY".
     *
     * @throws Exception        if an unexpected token was encountered
     *
     */
    protected KaboumCoordinate[] getCoordinates(StreamTokenizer tokenizer) throws Exception {
    
        String nextToken = getNextEmptyOrOpener(tokenizer);
        
        if (nextToken.equals("EMPTY")) {
            return new KaboumCoordinate[]{};
        }
        KaboumList coordinates = new KaboumList();
        KaboumCoordinate externalCoordinate = new KaboumCoordinate();
        externalCoordinate.x = getNextNumber(tokenizer);
        externalCoordinate.y = getNextNumber(tokenizer);
        KaboumCoordinate internalCoordinate = precisionModel.toInternal(externalCoordinate);
        coordinates.addElement(internalCoordinate);
        
        nextToken = getNextCloserOrComma(tokenizer);
        while (nextToken.equals(",")) {
            externalCoordinate.x = getNextNumber(tokenizer);
            externalCoordinate.y = getNextNumber(tokenizer);
            internalCoordinate = precisionModel.toInternal(externalCoordinate);
            coordinates.addElement(internalCoordinate);
            nextToken = getNextCloserOrComma(tokenizer);
        }
        return coordinates.toCoordinateArray();
    }
    
    
    /**
     *
     * Returns the next number in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next token must be a number.
     *
     * @throws Exception         if an I/O error occurs
     *
     */
    protected double getNextNumber(StreamTokenizer tokenizer) throws Exception {
        
        int type = tokenizer.nextToken();
        switch (type) {
            case StreamTokenizer.TT_EOF:
                throw new Exception("Expected number but encountered end of stream");
            case StreamTokenizer.TT_EOL:
                throw new Exception("Expected number but encountered end of line");
            case StreamTokenizer.TT_NUMBER:
                return tokenizer.nval;
            case StreamTokenizer.TT_WORD:
                throw new Exception("Expected number but encountered word: " +
                tokenizer.sval);
            case '(':
                throw new Exception("Expected number but encountered '('");
            case ')':
                throw new Exception("Expected number but encountered ')'");
            case ',':
                throw new Exception("Expected number but encountered ','");
        }
        return 0;
    }
    
    
    /**
     *
     * Returns the next "EMPTY" or "(" in the stream as uppercase text.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next token must be "EMPTY" or "(".
     *
     * @throws Exception        if an I/O error occurs
     */
    protected String getNextEmptyOrOpener(StreamTokenizer tokenizer) throws Exception {
        String nextWord = getNextWord(tokenizer);
        if (nextWord.equals("EMPTY") || nextWord.equals("(")) {
            return nextWord;
        }
        throw new Exception("Expected 'EMPTY' or '(' but encountered '" + nextWord + "'");
    }
    
    
    /**
     *
     * Returns the next ")" or "," in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next token must be ")" or ",".
     *
     * @throws  IOException     if an I/O error occurs
     *
     */
    protected String getNextCloserOrComma(StreamTokenizer tokenizer) throws Exception {
        String nextWord = getNextWord(tokenizer);
        if (nextWord.equals(",") || nextWord.equals(")")) {
            return nextWord;
        }
        throw new Exception("Expected ')' or ',' but encountered '" + nextWord + "'");
    }
    
    
    /**
     *
     * Returns the next ")" in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next token must be ")".
     * @throws  ParseException  if the next token is not ")"
     *
     */
    protected String getNextCloser(StreamTokenizer tokenizer) throws Exception {
        String nextWord = getNextWord(tokenizer);
        if (nextWord.equals(")")) {
            return nextWord;
        }
        throw new Exception("Expected ')' but encountered '" + nextWord + "'");
    }
    
    
    /**
     *
     * Returns the next word in the stream as uppercase text.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next token must be a word.
     *
     * @throws  ParseException  if the next token is not a word
     *
     */
    protected String getNextWord(StreamTokenizer tokenizer) throws Exception {
        
        int type = tokenizer.nextToken();
        
        switch (type) {
            case StreamTokenizer.TT_EOF:
                throw new Exception("Expected word but encountered end of stream");
            case StreamTokenizer.TT_EOL:
                throw new Exception("Expected word but encountered end of line");
            case StreamTokenizer.TT_NUMBER:
                throw new Exception("Expected word but encountered number: " +
                tokenizer.nval);
            case StreamTokenizer.TT_WORD:
                return tokenizer.sval.toUpperCase();
            case '(':
                return "(";
            case ')':
                return ")";
            case ',':
                return ",";
        }
        return null;
    }
    
    
    /**
     *
     * Creates a <code>Geometry</code> using the next token in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;Geometry Tagged Text&gt;.
     *
     * @throws  Exception      if the coordinates used to create a <code>Polygon</code>
     *      shell and holes do not form closed linestrings, or if an unexpected
     *      token was encountered
     *
     */
    protected KaboumGeometry readGeometryTaggedText(StreamTokenizer tokenizer) throws Exception {
        String type = getNextWord(tokenizer);
        if (type.equals("POINT")) {
            return readPointText(tokenizer);
        }
        else if (type.equals("LINESTRING")) {
            return readLineStringText(tokenizer);
        }
        else if (type.equals("POLYGON")) {
            return readPolygonText(tokenizer);
        }
        else if (type.equals("MULTIPOINT")) {
            return readMultiPointText(tokenizer);
        }
        else if (type.equals("MULTILINESTRING")) {
            return readMultiLineStringText(tokenizer);
        }
        else if (type.equals("MULTIPOLYGON")) {
            return readMultiPolygonText(tokenizer);
        }
        else if (type.equals("GEOMETRYCOLLECTION")) {
            return readGeometryCollectionText(tokenizer);
        }
        throw new Exception("Unknown type: " + type);
    }
    
    
    /**
     *
     * Creates a <code>Point</code> using the next token in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;Point Text&gt;.
     *
     * @throws Exception        if an I/O error occurs
     *
     */
    protected KaboumPoint readPointText(StreamTokenizer tokenizer) throws Exception {
        String nextToken = getNextEmptyOrOpener(tokenizer);
        
        if (nextToken.equals("EMPTY")) {
            return KaboumGeometryFactory.createPoint(null);
        }
        double x = getNextNumber(tokenizer);
        double y = getNextNumber(tokenizer);
        KaboumCoordinate externalCoordinate = new KaboumCoordinate(x, y);
        KaboumCoordinate internalCoordinate = precisionModel.toInternal(externalCoordinate);
        KaboumPoint point = KaboumGeometryFactory.createPoint(internalCoordinate);
        getNextCloser(tokenizer);
        return point;
    }
    
    
    /**
     *
     * Creates a <code>LineString</code> using the next token in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;LineString Text&gt;.
     *
     * @throws Exception        if error occurs
     *
     */
    protected KaboumLineString readLineStringText(StreamTokenizer tokenizer) throws Exception {
        return KaboumGeometryFactory.createLineString(getCoordinates(tokenizer));
    }
    
    
    /**
     *
     * Creates a <code>LinearRing</code> using the next token in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;LineString Text&gt;.
     *
     * @throws Exception  if the coordinates used to create the <code>LinearRing</code>
     *      do not form a closed linestring, or if an unexpected token was
     *      encountered
     */
    protected KaboumLinearRing readLinearRingText(StreamTokenizer tokenizer)
    throws Exception {
        return KaboumGeometryFactory.createLinearRing(getCoordinates(tokenizer));
    }
    
    
    /**
     *
     * Creates a <code>MultiPoint</code> using the next token in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;MultiPoint Text&gt;.
     *
     * @throws  ParseException  if an unexpected token was encountered
     *
     */
    protected KaboumMultiPoint readMultiPointText(StreamTokenizer tokenizer) throws Exception {
        return KaboumGeometryFactory.createMultiPoint(toPoints(getCoordinates(tokenizer)));
    }
    
    
    /**
     *
     * Creates an array of <code>Point</code>s having the given <code>Coordinate</code>
     *
     * @param  coordinates  the <code>Coordinate</code>s with which to create the
     *      <code>Point</code>s
     *
     */
    protected KaboumPoint[] toPoints(KaboumCoordinate[] coordinates) {
        KaboumList points = new KaboumList();
        for (int i = 0; i < coordinates.length; i++) {
            points.addElement(KaboumGeometryFactory.createPoint(coordinates[i]));
        }
        return points.toPointArray();
    }
    
    
    /**
     *
     * Creates a <code>Polygon</code> using the next token in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;Polygon Text&gt;.
     *
     * @throws Exception        if an error occurs
     *
     */
    protected KaboumPolygon readPolygonText(StreamTokenizer tokenizer) throws Exception {
        String nextToken = getNextEmptyOrOpener(tokenizer);
        if (nextToken.equals("EMPTY")) {
            return KaboumGeometryFactory.createPolygon(KaboumGeometryFactory.createLinearRing(
            new KaboumCoordinate[]{}), new KaboumLinearRing[]{});
        }
        KaboumList holes = new KaboumList();
        KaboumLinearRing shell = readLinearRingText(tokenizer);
        nextToken = getNextCloserOrComma(tokenizer);
        while (nextToken.equals(",")) {
            KaboumLinearRing hole = readLinearRingText(tokenizer);
            holes.addElement(hole);
            nextToken = getNextCloserOrComma(tokenizer);
        }
        return KaboumGeometryFactory.createPolygon(shell, holes.toLinearRingArray());
    }
    
    
    /**
     *
     * Creates a <code>MultiLineString</code> using the next token in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;MultiLineString Text&gt;.
     *
     * @throws  Exception  if an unexpected token was encountered
     *
     */
    protected KaboumMultiLineString readMultiLineStringText(StreamTokenizer tokenizer) throws Exception {
        String nextToken = getNextEmptyOrOpener(tokenizer);
        if (nextToken.equals("EMPTY")) {
            return KaboumGeometryFactory.createMultiLineString(new KaboumLineString[]{});
        }
        KaboumList lineStrings = new KaboumList();
        KaboumLineString lineString = readLineStringText(tokenizer);
        lineStrings.addElement(lineString);
        nextToken = getNextCloserOrComma(tokenizer);
        while (nextToken.equals(",")) {
            lineString = readLineStringText(tokenizer);
            lineStrings.addElement(lineString);
            nextToken = getNextCloserOrComma(tokenizer);
        }
        return KaboumGeometryFactory.createMultiLineString(lineStrings.toLineStringArray());
    }
    
    
    /**
     *
     * Creates a <code>MultiPolygon</code> using the next token in the stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;MultiPolygon Text&gt;.
     *
     * @throws Exception       if an unexpected token was encountered
     *
     */
    protected KaboumMultiPolygon readMultiPolygonText(StreamTokenizer tokenizer) throws Exception {
        String nextToken = getNextEmptyOrOpener(tokenizer);
        if (nextToken.equals("EMPTY")) {
            return KaboumGeometryFactory.createMultiPolygon(new KaboumPolygon[]{});
        }
        KaboumList polygons = new KaboumList();
        KaboumPolygon polygon = readPolygonText(tokenizer);
        polygons.addElement(polygon);
        nextToken = getNextCloserOrComma(tokenizer);
        while (nextToken.equals(",")) {
            polygon = readPolygonText(tokenizer);
            polygons.addElement(polygon);
            nextToken = getNextCloserOrComma(tokenizer);
        }
        return KaboumGeometryFactory.createMultiPolygon(polygons.toPolygonArray());
    }
    
    
    /**
     *
     * Creates a <code>GeometryCollection</code> using the next token in the
     * stream.
     *
     * @param  tokenizer        tokenizer over a stream of text in Well-known Text
     *      format. The next tokens must form a &lt;GeometryCollection Text&gt;.
     *
     * @throws  ParseException  if the coordinates used to create a <code>Polygon</code>
     *      shell and holes do not form closed linestrings, or if an unexpected
     *      token was encountered
     *
     */
    protected KaboumGeometryCollection readGeometryCollectionText(StreamTokenizer tokenizer) throws Exception {
        String nextToken = getNextEmptyOrOpener(tokenizer);
        if (nextToken.equals("EMPTY")) {
            return KaboumGeometryFactory.createGeometryCollection(new KaboumGeometry[]{});
        }
        KaboumList geometries = new KaboumList();
        KaboumGeometry geometry = readGeometryTaggedText(tokenizer);
        geometries.addElement(geometry);
        nextToken = getNextCloserOrComma(tokenizer);
        while (nextToken.equals(",")) {
            geometry = readGeometryTaggedText(tokenizer);
            geometries.addElement(geometry);
            nextToken = getNextCloserOrComma(tokenizer);
        }
        return KaboumGeometryFactory.createGeometryCollection(geometries.toGeometryArray());
    }
}

