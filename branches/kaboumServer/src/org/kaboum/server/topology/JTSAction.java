/*
 * JTSAction.java
 *
 * Created on 17 aout 2005, 18:17
 */
package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.Vector;
import org.apache.log4j.Logger;

import org.kaboum.Kaboum;
import org.kaboum.server.UserData;

/**
 * The base of all JTS spatial analysis operations supported by Kaboum Server.
 * All other JTS-related classes derived from this class.
 * <p>
 * The 'Action' name was taken from STRUTS: each spatial analysis operation is treated as
 * an atomic action with its dedicated class(es).
 * </p>
 * @author Nicolas Ribot
 */
public abstract class JTSAction {

        /** the input JTS geometries to process. */
        protected Vector geometries;
        /** This class's logger */
        private static Logger logger = Logger.getLogger(JTSAction.class);
        /**
         * Tells if first object id contained in geometries <CODE>Vector</CODE> should be
         * set to the spatial analysis result (by default, K_NEW_GEOMETRY is given to result)
         */
        protected boolean keepID;

        /** Creates a new instance of JTSAction with the JTS geometries to process.
         *@param geometries A vector of JTS Geometry objects
         */
        public JTSAction(Vector geometries) {
                this.geometries = geometries;
                keepID = false;
        }

        /**
         * Creates a new instance of JTSAction with the JTS geometries to process.
         * @param keepID Should first geometry's id be kept
         * @param geometries A vector of JTS Geometry objects
         */
        public JTSAction(Vector geometries, boolean keepID) {
                this(geometries);
                this.keepID = keepID;
        }

        /**
         * returns the input hashtable containing the geometries to process with JTS
         *@return an Hashtable containing input (non-modified) geometries.
         */
        public Vector getGeometries() {
                return geometries;
        }

        /**
         * Sets the toGeom Geometry's userData Field with the data contained in
         * the fromGeom Geometry.
         *<p>
         * if fromGeom is null, sets toGeom userData field with K_NEW_GEOMETRY id and no tooltip
         *</p>
         *<p>
         *this method can be overridden to provide specific id and/or tooltip
         *</p>
         *@param fromGeom the source Geometry for userData field
         *@param toGeom the target Geometry for userData field (its userData field will
         *be set with the value contained in toGeom.UserData
         */
        public void copyUserData(Geometry toGeom, Geometry fromGeom) {
                if (toGeom != null) {
                        if (fromGeom == null) {
                                //reset the userData field as it seems to be lost during union operation
                                toGeom.setUserData(new UserData(Kaboum.K_NEW_GEOMETRY, ""));
                        } else {
                                toGeom.setUserData(
                                        new UserData(
                                        ((UserData) fromGeom.getUserData()).id,
                                        ((UserData) fromGeom.getUserData()).toolTip));
                        }
                }
        }

        /**
         * Returns a <CODE>GeometryCollection</CODE> containing the given geometry by comparing
         * the geometries object class and the geom class:
         * if geometries contains a collection type (MultiPoint, MultiLinestring, etc.)
         * and the given geometry is a simple type (point, polygon, etc), then it will
         * return a GeometryCollection containing the given geometry
         * @param geom the geometry to include, eventually, in a collection
         * @return a geometryCollection containing the given geom, or the input geom
         * if no consistent collection can be built
         */
        protected Geometry createCollection(Geometry geom) {
                if (geom == null || geometries == null) {
                        return null;
                }

                if (geometries.size() == 0) {
                        // no possible conversion
                        return geom;
                }

                Class inputClass = geometries.get(0).getClass();
                Class genClass = geom.getClass();

                //System.out.println("input class: " + inputClass + " gen class: " + genClass);

                GeometryFactory fact = new GeometryFactory();
                Geometry g = null;

                if (inputClass == MultiPoint.class && genClass == Point.class) {
                        Point[] p = new Point[]{(Point) geom};
                        g = fact.createMultiPoint(p);
                } else if (inputClass == MultiLineString.class && genClass == LineString.class) {
                        LineString[] l = new LineString[]{(LineString) geom};
                        g = fact.createMultiLineString(l);
                } else if (inputClass == MultiPolygon.class && genClass == Polygon.class) {
                        Polygon[] p = new Polygon[]{(Polygon) geom};
                        g = fact.createMultiPolygon(p);
                } else {
                        g = geom;
                }

                g.setUserData(geom.getUserData());
                return g;
        }

        /**
         * Returns true if input geometry is polygon or Multipolygon type and
         * it contains at least one hole
         *@param g the geometry to test
         *@return true if g has hole(s)
         */
        public boolean geomHasHoles(Geometry g) {
                if (g == null || (!(g instanceof Polygon) && !(g instanceof MultiPolygon))) {
                        return false;
                }
                if (g instanceof Polygon) {
                        return ((Polygon) g).getNumInteriorRing() > 0;
                }
                if (g instanceof MultiPolygon) {
                        for (int i = 0; i < ((MultiPolygon) g).getNumGeometries(); i++) {
                                if (((Polygon) ((MultiPolygon) g).getGeometryN(i)).getNumInteriorRing() > 0) {
                                        return true;
                                }
                        }
                }
                return false;
        }

        /**
         * returns the JTS geometry corresponding to the spatial analysis operation denoted
         * by the class derived from this one.
         * @return the JTS Geometry resulting from the spatial analysis operation
         * @throws com.vividsolutions.jts.util.AssertionFailedException if a such a JTS exception is thrown
         * @throws TopologyException if an error occured during JTS processing
         * @throws IllegalArgumentException if a intermediate operation produced an invalid
         * object used for the next operation (ex: GeometryCollection generated)
         */
        public abstract Geometry getResult()
                throws TopologyException, IllegalArgumentException, AssertionFailedException;
}
