/*
 * KaboumGeometryFactory.java
 *
 * Created on 15 aout 2005, 13:09
 */

package org.kaboum.server.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;

import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumGeometryCollection;
import org.kaboum.geom.KaboumLineString;
import org.kaboum.geom.KaboumLinearRing;
import org.kaboum.geom.KaboumMultiLineString;
import org.kaboum.geom.KaboumMultiPoint;
import org.kaboum.geom.KaboumMultiPolygon;
import org.kaboum.geom.KaboumPoint;
import org.kaboum.geom.KaboumPolygon;
import org.kaboum.server.UserData;
import org.kaboum.util.KaboumCoordinate;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A static factory class to convert from/to JTS Geometry to/from KaboumGeometry objects
 * and to provide some convenient methods to create JTS objects (Envelope for instance)
 * @author Nicolas
 */
public class KaboumJTSFactory {
        private final static Logger logger = Logger.getLogger(KaboumJTSFactory.class);
    
    /** Creates a new instance of KaboumGeometryFactory */
    private KaboumJTSFactory() {
    }
    
    //////////////////////////////////////////////////////////////////////////////
    /////////////// KaboumGeometry -> JTS geometry functions /////////////////////
    //////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a Hashtable of KaboumGeometry converted from the given JTS Geometry contained
     * in the passed Hashtable.
     *<p>
     * Hashtable's keys will be kept unchanged. The value of the Hashtable will be
     * a vector of KaboumGeometry with id and tooltip fields took from the UserData field
     * of the JTS geometry.
     *</p>
     *<p>
     * During conversion, if a JTS Geometry with an invalid UserData field is encountered
     * (not an instanceof org.kaboum.server.UserData)
     * , null will be stored in the resulting Hashtable. <br>
     * So it is up to this method's caller to check
     * Geometry validity before using it.
     *</p>
     *@param jtsGeometries The Hashtable of JTS Geometry to convert.
     * @return a Hashtable of KaboumGeometry or null if input hashtable is null
    public static Hashtable<String, Vector<KaboumGeometry>> 
            getKaboumGeometries(Hashtable<String, Vector<Geometry>> jtsGeometries) {
        if (jtsGeometries == null) return null;
        
        Hashtable<String, Vector<KaboumGeometry>> kGeoms = new Hashtable<String, Vector<KaboumGeometry>>(jtsGeometries.size());
        Enumeration<String> keys = jtsGeometries.keys();
        
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Vector<Geometry> vec = jtsGeometries.get(key);
            
            if (vec == null) {
                kGeoms.put(key, null);
            } else {
                Vector<KaboumGeometry> newVec = new Vector<KaboumGeometry>(vec.size());
                
                for (Geometry geom : vec) {
                    newVec.add(KaboumJTSFactory.getKaboumGeometry(geom));
                }
                kGeoms.put(key, newVec);
            }
        }
        return kGeoms;
    }
     */
    public static Hashtable getKaboumGeometries(Hashtable jtsGeometries) {
        if (jtsGeometries == null) return null;
        
        Hashtable kGeoms = new Hashtable(jtsGeometries.size());
        Enumeration keys = jtsGeometries.keys();
        
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Vector vec = (Vector) jtsGeometries.get(key);
            
            if (vec == null) {
                kGeoms.put(key, null);
            } else {
                Vector newVec = new Vector(vec.size());
                
                for (Iterator iter = vec.iterator(); iter.hasNext(); ) {
                    newVec.add(KaboumJTSFactory.getKaboumGeometry((Geometry)iter.next()));
                }
                kGeoms.put(key, newVec);
            }
        }
        return kGeoms;
    }    
    
    
    /**
     * Returns a KaboumGeometry from the given JTS geometry.
     * The actual type of returned object will be a derived type from KaboumGeometry.
     * <p>
     * The following typing will occur:
     * If geom instanceof Point, returns a KaboumPoint<br>
     * If geom instanceof LineString,returns a KaboumLineString<br>
     * If geom instanceof Polygon, returns a KaboumPolygon<br>
     * If geom instanceof LinearRing, returns a KaboumLinearRing<br>
     * returns the corresponding Kaboum collection if input geometry is an homogene collection
     * </p>
     * @param geom The JTS geometry to convert into a KaboumGeometry
     * @return The converted KaboumGeometry
     */
    public static KaboumGeometry getKaboumGeometry(Geometry geom) {
        if (geom == null) return null;
        KaboumGeometry kg = null;
        
        if (geom instanceof Point) {
            kg = getKaboumPoint((Point) geom);
        } else if (geom instanceof LineString) {
            kg = getKaboumLineString( (LineString) geom);
        } else if (geom instanceof LinearRing) {
            kg = getKaboumLinearRing( (LinearRing)geom);
        } else if (geom instanceof Polygon) {
            kg = getKaboumPolygon( (Polygon) geom);
        } else if (geom instanceof MultiLineString) {
            kg = getKaboumMultiLineString( (MultiLineString) geom);
        } else if (geom instanceof MultiPoint) {
            kg = getKaboumMultiPoint( (MultiPoint)geom);
        } else if (geom instanceof MultiPolygon) {
            kg = getKaboumMultiPolygon( (MultiPolygon) geom);
        } else if (geom instanceof GeometryCollection) {
            kg = getKaboumGeometryCollection( (GeometryCollection) geom);
        } else {
            // not a supported geometry
            logger.warn("getKaboumGeometry: not a supported geometry: " + geom.getClass());
            return null;
        }
        if (geom.getUserData() instanceof UserData ) {
            kg.id = ((UserData)geom.getUserData()).id;
            kg.setToolTip( ((UserData)geom.getUserData()).toolTip );
        } else {
            logger.warn("getKaboumGeometry: JTS geom does not have a valid UserData field");
            return null;
        }
        return kg;
    }
    
    /**
     * Converts an array of JTS coordinates to an array of KaboumCoordinates.
     *
     *@param coords - the array of JTS coordinates to convert.
     *@return an array of KaboumCoordinate
     */
    public static KaboumCoordinate[] getKaboumCoordinates(Coordinate[] coords) {
        if (coords == null) return null;
        
        KaboumCoordinate[] kCoords = new KaboumCoordinate[coords.length];
        
        for (int i = 0; i < coords.length; i++) {
            kCoords[i] = new KaboumCoordinate(coords[i].x, coords[i].y);
        }
        return kCoords;
    }
    
    /**
     * Returns a KaboumPoint built on the given JTS point, by copying its first coordinate
     * @param pt - The JTS point to transform
     * @return a new KaboumPoint from the given JTS point.
     */
    public static KaboumPoint getKaboumPoint(Point pt) {
        if (pt == null ) return null;
        
        KaboumPoint res = new KaboumPoint(getKaboumCoordinates(pt.getCoordinates())[0]);
        return res;
    }
    
    /**
     * Returns a KaboumLineString built on the given JTS LineString, by copying its coordinates
     * @param ln - The JTS LineString to transform
     * @return a new KaboumLineString from the given JTS LineString.
     */
    public static KaboumLineString getKaboumLineString(LineString ln) {
        if (ln == null ) return null;
        
        KaboumLineString res = new KaboumLineString(getKaboumCoordinates(ln.getCoordinates()));
        
        return res;
    }
    
    /**
     * Returns a KaboumLinearRing built on the given JTS LinearRing, by copying its coordinates
     * @param lr - The JTS LinearRing to transform
     * @return a new KaboumLinearRing from the given JTS LinearRing.
     */
    public static KaboumLinearRing getKaboumLinearRing(LinearRing lr) {
        if (lr == null ) return null;
        
        KaboumLinearRing res = new KaboumLinearRing(getKaboumCoordinates(lr.getCoordinates()));
        
        return res;
    }
    
    /**
     * Returns a KaboumLinearRing built on the given JTS LineString, by copying its coordinates.
     * It is up to the caller to ensure that the given lineString is closed and can be converted
     * into a linearRing.
     * @param ln - The JTS LineString to transform
     * @return a new KaboumLinearRing from the given JTS point.
     */
    public static KaboumLinearRing getKaboumLinearRing(LineString ln) {
        if (ln == null ) return null;
        
        KaboumLinearRing res = new KaboumLinearRing(getKaboumCoordinates(ln.getCoordinates()));
        
        return res;
    }
    
    /**
     * Returns a KaboumPolygon built on the given JTS Polygon, by copying its coordinates
     * @param pg - The JTS Polygon to transform
     * @return a new KaboumPolygon from the given JTS Polygon.
     */
    public static KaboumPolygon getKaboumPolygon(Polygon pg) {
        if (pg == null ) return null;
        
        KaboumLinearRing[] linearRings = new KaboumLinearRing[pg.getNumInteriorRing()];
        
        for (int i = 0; i < pg.getNumInteriorRing(); i++) {
            linearRings[i] = getKaboumLinearRing(pg.getInteriorRingN(i));
        }
        
        KaboumPolygon res = new KaboumPolygon(getKaboumLinearRing(pg.getExteriorRing()), linearRings);
        
        return res;
    }
    
    /**
     * Returns a KaboumMultiPoint built on the given JTS Polygon, by copying its coordinates
     * @param mpt - The JTS Polygon to transform
     * @return a new KaboumMultiPoint from the given JTS Polygon.
     */
    public static KaboumMultiPoint getKaboumMultiPoint(MultiPoint mpt) {
        if (mpt == null ) return null;
        
        KaboumPoint[] points = new KaboumPoint[mpt.getNumGeometries()];
        
        for (int i = 0; i < mpt.getNumGeometries(); i++) {
            points[i] = getKaboumPoint((Point)mpt.getGeometryN(i));
        }
        KaboumMultiPoint res = new KaboumMultiPoint(points);
        
        return res;
    }
    
    /**
     * Returns a KaboumMultiLineString built on the given JTS MultiLineString, by copying its coordinates
     * @param mln - The JTS MultiLineString to transform
     * @return a new KaboumMultiLineString from the given JTS point.
     */
    public static KaboumMultiLineString getKaboumMultiLineString(MultiLineString mln) {
        if (mln == null ) return null;
        
        KaboumLineString[] lines = new KaboumLineString[mln.getNumGeometries()];
        
        for (int i = 0; i < mln.getNumGeometries(); i++) {
            lines[i] = getKaboumLineString((LineString)mln.getGeometryN(i));
        }
        KaboumMultiLineString res = new KaboumMultiLineString(lines);
        
        return res;
    }
    
    /**
     * Returns a KaboumMultiLineString built on the given JTS MultiPolygon, by copying its coordinates
     * @param mpg - The JTS MultiPolygon to transform
     * @return a new KaboumMultiPolygon from the given JTS MultiPolygon.
     */
    public static KaboumMultiPolygon getKaboumMultiPolygon(MultiPolygon mpg) {
        if (mpg == null ) return null;
        
        KaboumPolygon[] polygons = new KaboumPolygon[mpg.getNumGeometries()];
        
        for (int i = 0; i < mpg.getNumGeometries(); i++) {
            polygons[i] = getKaboumPolygon((Polygon)mpg.getGeometryN(i));
        }
        KaboumMultiPolygon res = new KaboumMultiPolygon(polygons);
        
        return res;
    }
    
    /**
     * Returns a KaboumGeometryCollection built on the given JTS GeometryCollection, by copying its coordinates
     * @return a new KaboumGeometryCollection from the given JTS point.
     * @param gc The JTS GeometryCollection to convert into KaboumGeometryCollection
     */
    public static KaboumGeometryCollection getKaboumGeometryCollection(GeometryCollection gc) {
        if (gc == null ) return null;
        
        KaboumGeometry[] kGeoms = new KaboumGeometry[gc.getNumGeometries()];
        
        for (int i = 0; i < gc.getNumGeometries(); i++) {
            kGeoms[i] = KaboumJTSFactory.getKaboumGeometry(gc.getGeometryN(i));
        }
        return new KaboumGeometryCollection(kGeoms);
    }
    
    /**
     * transform the given mapserver representation of an extent (space-separated
     * lower-left and upper-right bounding box coordinates) into a JTS envelope
     *@param msExtent the MapServer extention representation, namely: "llx lly urx ury"
     *@return a JTS envelope built from the given coordinates, or null if the conversion could
     * not be done.
     */
    public static Envelope getEnvelope(String msExtent) {
        if (msExtent == null || msExtent.length() == 0) {
            return null;
        }
        StringTokenizer tok = new StringTokenizer(msExtent);
        
        if (tok.countTokens() != 4) {
            return null;
        }
        double llx, lly, urx, ury;
        
        try {
            llx = Double.parseDouble(tok.nextToken());
            lly = Double.parseDouble(tok.nextToken());
            urx = Double.parseDouble(tok.nextToken());
            ury = Double.parseDouble(tok.nextToken());
        } catch (NumberFormatException nfe) {
            return null;
        }
        return new Envelope(llx, urx, lly,  ury);
    }
    
    //////////////////////////////////////////////////////////////////////////////
    /////////////// KaboumGeometry -> JTS geometry functions /////////////////////
    //////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a Hashtable of JTS geometry converted from the given KaboumGeometry contained
     * in the passed Hashtable.
     * <p>
     * Hashtable's keys will be kept unchanged. The value of the Hashtable will be
     * a vector of JTS Geometry with the UserData field set to a org.kaboum.server.UserData
     * </p>
     * <p>
     * During conversion, if an invalid KaboumGeometry is encountered, null will be stored
     * in the resulting Hashtable. So it is up to this method's caller to check
     * Geometry validity before using it.
     * </p>
     * @return a Hashtable of JTS geometry or null if input hashtable is null
     * @param kGeometries The hashtable of KaboumGeometries to convert
    public static Hashtable<String, Vector<Geometry>> getJTSGeometries(Hashtable<String, Vector<KaboumGeometry>> kGeometries) {
        if (kGeometries == null) return null;
        
        Hashtable<String, Vector<Geometry>> jtsGeoms = new Hashtable<String, Vector<Geometry>>(kGeometries.size());
        Enumeration<String> keys = kGeometries.keys();
        
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Vector<KaboumGeometry> vec = kGeometries.get(key);
            
            if (vec == null) {
                jtsGeoms.put(key, null);
            } else {
                Vector<Geometry> newVec = new Vector(vec.size());
                
                for (KaboumGeometry kGeom : vec) {
                    newVec.add(KaboumJTSFactory.getJTSGeometry(kGeom));
                }
                jtsGeoms.put(key, newVec);
            }
        }
        return jtsGeoms;
    }
     */
    
    public static Hashtable getJTSGeometries(Hashtable kGeometries) {
        if (kGeometries == null) return null;
        
        Hashtable jtsGeoms = new Hashtable(kGeometries.size());
        Enumeration keys = kGeometries.keys();
        
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Vector vec = (Vector) kGeometries.get(key);
            
            if (vec == null) {
                jtsGeoms.put(key, null);
            } else {
                Vector newVec = new Vector(vec.size());
                
                for (Iterator iter = vec.iterator(); iter.hasNext(); ) {
                    newVec.add(KaboumJTSFactory.getJTSGeometry( (KaboumGeometry) iter.next() ));
                }
                jtsGeoms.put(key, newVec);
            }
        }
        return jtsGeoms;
    }    
    
    
    /**
     * Returns a JTS geometry from the given KaboumGeometry .
     * The actual type of returned object will be a derived type from KaboumGeometry.
     *<p>
     * The following typing will occur:
     * If geom instanceof KaboumPoint, returns a Point<br>
     * If geom instanceof KaboumLineString,returns a LineString<br>
     * If geom instanceof KaboumPolygon, returns a Polygon<br>
     * If geom instanceof KaboumLinearRing, returns a LinearRing<br>
     *</p>
     *<p>
     *If input geometry have null coordinates, an empty JTS geometry with valid UserData field
     * will be returned.
     *</p>
     *@param geom The KaboumGeometry to convert.
     * @return the corresponding collection if input geometry is an homogene collection
     */
    public static Geometry getJTSGeometry(KaboumGeometry geom) {
        if (geom == null) return null;
        
        Geometry result = null;
        GeometryFactory fact = new GeometryFactory();
        
        if (geom instanceof KaboumPoint) {
            Coordinate coord = geom.isEmpty() ? null : getJTSCoordinates(geom)[0];
            result = fact.createPoint(coord);
        } else if (geom instanceof KaboumLineString) {
            Coordinate[] coords = geom.isEmpty() ? null : getJTSCoordinates(geom);
            result = fact.createLineString(coords);
        } else if (geom instanceof KaboumLinearRing) {
            Coordinate[] coords = geom.isEmpty() ? null : getJTSCoordinates(geom);
            result = fact.createLinearRing(coords);
        } else if (geom instanceof KaboumPolygon) {
            LinearRing shell = geom.isEmpty() ? null : 
                fact.createLinearRing( ((LineString)getJTSGeometry(((KaboumPolygon)geom).getExteriorRing())).getCoordinates());
            LinearRing[] holes = geom.isEmpty() ? new LinearRing[0] : 
                new LinearRing[((KaboumPolygon)geom).getNumInteriorRing()];

            for (int i = 0; i < holes.length; i++) {
                holes[i] = fact.createLinearRing( ((LineString)getJTSGeometry(((KaboumPolygon)geom).getInteriorRingN(i))).getCoordinates());
            }
            result = fact.createPolygon(shell, holes);
        } else if (geom instanceof KaboumMultiLineString) {
            LineString[] lineStrings = geom.isEmpty() ? new LineString[0] : new LineString[((KaboumMultiLineString)geom).getNumGeometries()];

            for (int i = 0; i < lineStrings.length; i++) {
                lineStrings[i] = (LineString) getJTSGeometry(geom.getGeometryN(i));
            }
            result = fact.createMultiLineString(lineStrings);
        } else if (geom instanceof KaboumMultiPoint) {
            Point[] points = geom.isEmpty() ? new Point[0] : new Point[((KaboumMultiPoint)geom).getNumGeometries()];

            for (int i = 0; i < points.length; i++) {
                points[i] = (Point) getJTSGeometry(geom.getGeometryN(i));
            }

            result = fact.createMultiPoint(points);

        } else if (geom instanceof KaboumMultiPolygon) {
            Polygon[] polygons = new Polygon[((KaboumMultiPolygon)geom).getNumGeometries()];

            for (int i = 0; i < polygons.length; i++) {
                polygons[i] = (Polygon) getJTSGeometry(geom.getGeometryN(i));
            }

            result = fact.createMultiPolygon(polygons);

        } else {
            // not a supported geometry
            logger.warn("getJTSGeometry: not a supported geometry: " + geom.getClass());
            return null;
        }
        
        // deals with UserData field
        UserData userData = new UserData(geom.id, geom.getToolTip());
        result.setUserData(userData);
        
        return result;
    }
    
    /**
     * Returns an array of JTS coordinates build from the input KaboumGeometry coordinate list.<br>
     * Works only with KaboumPoint, KaboumLineString and KaboumLinearRing, not KaboumPolygon and KaboumGeometryCollections.<br>
     * for a point, returns a one-element array
     *@param geom - the KaboumGeometry whose coordinates will be converted into JTS coordinates
     *@return an array of JTS coordinates, or null if input geometry's coordinates are null
     */
    public static Coordinate[] getJTSCoordinates(KaboumGeometry geom) {
        Coordinate[] coords = null;
        
        if (geom instanceof KaboumPoint) {
            coords = new Coordinate[] {
                new Coordinate(((KaboumPoint)geom).getCoordinate().x, ((KaboumPoint)geom).getCoordinate().x)
            };
        } else if (geom instanceof KaboumLineString) {
            // KaboumLinearRing inherits from KaboumLineString
            coords  = new Coordinate[((KaboumLineString)geom).getNumPoints()];
            
            for (int i = 0; i < coords.length; i++) {
                coords[i] = new Coordinate(
                        ((KaboumLineString)geom).getCoordinateN(i).x,
                        ((KaboumLineString)geom).getCoordinateN(i).y);
            }
        } else {
            // not a supported type
            return null;
        }
        
        return coords;
    }
    
    /**
     * Returns a FeatureCollection built from the given Vector of JTS Geometries and the given feature type name.
     * @param geometries - a vector containing JTS Geometry or derived objects
     * @param featureType the FeatureType to build the reader upon
     * @return a FeatureCollection built from the conversion of given JTSGeometries
     * into a Feature array
     * @throws org.geotools.feature.SchemaException If given featureType is not valid
     * @throws org.geotools.feature.IllegalAttributeException If Geotools generates such an exception
     * @throws java.io.IOException If Geotools generates such an exception
     */
    public static FeatureCollection getFeatureCollection(Vector geometries, SimpleFeatureType featureType)
                throws SchemaException, IOException {
        if (featureType == null || geometries == null || geometries.size() == 0) {
            return null;
            
        }
        SimpleFeature[] features = new SimpleFeature[geometries.size()];
        SimpleFeature f = null;
        Geometry geom = null;
        Object[] objects = new Object[featureType.getAttributeCount()];
        int geometryAttributeIndex = 0;
        
        // sets all attribue values to null and store the index of the geometric field,
        // the only one to be updated in a Kaboum Server
        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            objects[i] = null;
            if (featureType.getDescriptor(i).getLocalName().equals(featureType.getGeometryDescriptor().getLocalName())) {
                // store the index
                geometryAttributeIndex = i;
            }
        }
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        for (int i = 0; i < geometries.size(); i++) {
            geom = (Geometry)geometries.get(i);
            objects[geometryAttributeIndex] =  geom;
            f = builder.buildFeature(((UserData) geom.getUserData()).id , objects);
            features[i] = f;
        }
        
        return DataUtilities.collection(features);
    }
    
    /**
     * Sets each JTS Geometry UserData's id to the value found at the same index rank in the
     * given Set of identifier.
     * <p>
     * ids and geometries MUST have the same size, otherwise, an IllegalArgumentException is throwm<br>
     * The set values will be converted to String by calling toString() method on set's Object
     * </>
     * @param typeName The name of the FeatureType whose ids are got. Used to guess if geotools was able 
     * to retrieve newly added geometry's ID
     * @param geometries - the Vector of JTSGeometry objects whose ids are to set
     * @param ids - the Set containing new IDs
     * @return false if one FID seems to be incorrect (null value or null string inside the value or 
     * ID not containing the typeName, as it should)
     * @throws java.lang.IllegalArgumentException IllegalArgumentException if sizes of ids and geom are different
     * @throws java.lang.NullPointerException NullPointerException if one of the parameters is null
     */
    public static boolean setObjectIDS(Vector geometries, List ids, String typeName)
    throws IllegalArgumentException, NullPointerException {
        if (geometries == null) {
            throw new NullPointerException("Vector of KaboumGeometry is null");
        }
        if (ids == null) {
            throw new NullPointerException("Set of new identifiers is null");
        }
        
        if (geometries.size() != ids.size()) {
            throw new IllegalArgumentException("Input Vector and Set do not have the same size");
        }
        
        boolean res = true;
        Geometry geom = null;
        Iterator geomIter = null;
        Iterator idIter = null;
        
        for (geomIter = geometries.iterator(), idIter = ids.iterator(); geomIter.hasNext();) {
            Object o = idIter.next();
            geom = (Geometry)geomIter.next();
            
            String id = o.toString();
            if (id.indexOf("null") == (id.length() - "null".length()) ||
                    id.indexOf(typeName) == -1) {
                // we guess it is the real ID of the added feature
                res = false;
            }
            ((UserData)geom.getUserData()).id = id;
        }
        return res;
    }
    
    /**
     * Gets the JTS Geometry <CODE>Class</CODE> corresponding to the given Kaboum Geometry
     * <p>
     * there is a one-to-one relation between a KaboumGeometry and a JTS geometry.
     * see <CODE>getKaboumGeometry</CODE> method for class mappings
     * </p>
     * @param geom The KaboumGeometry geometry
     * @return A JTS <CODE>Class</CODE> object corresponding to the KaboumGeometry <CODE>Class</CODE>
     */
    public static Class getJTSClass(KaboumGeometry geom) {
        if (geom instanceof KaboumPoint) {
            return Point.class;
        } else if (geom instanceof KaboumLineString) {
            return LineString.class;
        } else if (geom instanceof KaboumLinearRing) {
            return LinearRing.class;
        } else if (geom instanceof KaboumPolygon) {
            return Polygon.class;
        } else if (geom instanceof KaboumMultiLineString) {
            return MultiLineString.class;
        } else if (geom instanceof KaboumMultiPoint) {
            return MultiPoint.class;
        } else if (geom instanceof KaboumMultiPolygon) {
            return MultiPolygon.class;
        }
        return Object.class;
    }
    
    /**
     * Sets the userData field of the given JTS <CODE>Geometry</CODE> to the given userData value.
     * <p>
     * for geometryCollections, sets userData field for each geometry contained in the collection
     * </p>
     * @param geom The Geometry whose UserData field will be set 
     * @param data The <CODE>UserData</CODE> object to set
     */
    public static void setUserData(Geometry geom, UserData data) {
        if (geom == null) return;
        
        geom.setUserData(data);
        
        if (geom instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) geom;
            for (int i = 0; i < gc.getNumGeometries(); i++) {
                gc.getGeometryN(i).setUserData(data);
            }
        }
    }
}
