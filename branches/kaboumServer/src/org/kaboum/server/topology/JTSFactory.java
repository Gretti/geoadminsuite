/*
 * JTSFactory.java
 *
 * Created on 27 aout 2005, 13:49
 */
package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.server.utils.KaboumJTSFactory;
import org.kaboum.util.KaboumFeatureModes;
import org.kaboum.util.KaboumFeatureShuttle;

/**
 * Deals with spatial analysis KaboumFeatureShuttle's modes.
 *<p>
 * Converts KaboumGeometries into JTS one and calls the relevant JTSAction according
 * to the shuttle's mode
 * </p>
 * @author Nicolas
 */
public class JTSFactory {

        /**
         * This class' logger
         */
        protected transient final Logger logger = Logger.getLogger(JTSFactory.class);

        /** Creates a new instance of JTSFactory */
        public JTSFactory() {
        }

        /**
         * Computes a JTS spatial operation (given by the shuttle's mode) on the geometries contained
         * in the given shuttle.
         *<p>
         * Converts KaboumGeometries into JTS one and calls the relevant JTSAction according
         * to the shuttle's mode. Then convert geometries back to KaboumGeometry classes
         * </p>
         * @param shuttle the object containing geometries to process
         * @return a KaboumFeatureShuttle object containg a geometries hashtable populated with
         * the result of UNION performed on each vector of geometries contained in this Hashtable
         */
        public KaboumFeatureShuttle processSpatialAnalysis(KaboumFeatureShuttle shuttle) {
                logger.info("Processing spatial analysis for given shuttle...");
                long begin = System.currentTimeMillis();

                if (shuttle == null) {
                        KaboumFeatureShuttle s = new KaboumFeatureShuttle();
                        s.errorCode = KaboumFeatureModes.K_NULL_SHUTTLE;
                        return s;
                }
                if (shuttle.geometries == null) {
                        // this is an unexpected error. should log it and trace down the cause
                        shuttle.errorCode = KaboumFeatureModes.K_NULL_GEOMETRIES;
                        return shuttle;
                }

                // the new Hashtable containing Spatial Analysis result
                Hashtable newGeoms = new Hashtable();
                // the JTS geometries, after shuttle's geometries conversion
                Hashtable jtsGeoms = KaboumJTSFactory.getJTSGeometries(shuttle.geometries);

                Enumeration keys = jtsGeoms.keys();

                JTSAction act = null;
                while (keys.hasMoreElements()) {
                        String key = (String) keys.nextElement();

                        Vector geoms = (Vector) jtsGeoms.get(key);

                        switch (shuttle.mode) {
                                case KaboumFeatureModes.K_UNION:
                                        act = new JTSUnionAction(geoms);
                                        break;
                                case KaboumFeatureModes.K_FUSION:
                                case KaboumFeatureModes.K_POLYGON_COMPLETION:
                                        act = new JTSUnionAction(geoms, true);
                                        break;
                                case KaboumFeatureModes.K_INTERSECTION:
                                        act = new JTSIntersectionAction(geoms);
                                        break;
                                case KaboumFeatureModes.K_POLYGON_SPLITTING:
                                        act = new JTSCutPolygonAction(geoms);
                                        break;
                                case KaboumFeatureModes.K_POLYGON_SPLITTING_BY_LINE:
                                        act = new JTSCutPolygonByLineAction(geoms);
                                        break;
                                case KaboumFeatureModes.K_DIFFERENCE:
                                case KaboumFeatureModes.K_POLYGON_FITTING:
                                        act = new JTSDifferenceAction(geoms);
                                        break;
                                case KaboumFeatureModes.K_POLYGON_ERASING:
                                        act = new JTSEraserAction(geoms);
                                        break;
                                case KaboumFeatureModes.K_SYM_DIFFERENCE:
                                        act = new JTSSymDifferenceAction(geoms);
                                        break;
                                case KaboumFeatureModes.K_HOLES_COMPLETION:
                                        act = new JTSFillHolesAction(geoms);
                                        break;
                                case KaboumFeatureModes.K_POLYGON_COMPLETION_FITTING:
                                        act = new JTSUnionDifferenceAction(geoms);
                                        break;
                                default:
                                        // should never reach here
                                        return null;
                        }
                        Vector vec = new Vector(1);
                        KaboumGeometry geom = null;
                        Geometry g = null;

                        try {
                                g = act.getResult();
                        } catch (TopologyException te) {
                                logger.error("TopologyException: " + act.getClass().getName() + ": " + te.getMessage());
                                shuttle.errorCode = KaboumFeatureModes.K_TOPOLOGY_EXCEPTION;
                                return shuttle;
                        } catch (IllegalArgumentException iae) {
                                logger.error("IllegalArgumentException: " + act.getClass().getName() + ": " + iae.getMessage());
                                shuttle.errorCode = KaboumFeatureModes.K_GEOMETRY_COLLECTION;
                                return shuttle;
                        } catch (AssertionFailedException afe) {
                                logger.error("AssertionFailedException: " + act.getClass().getName() + ": " + afe.getMessage());
                                shuttle.errorCode = KaboumFeatureModes.K_TOPOLOGY_EXCEPTION;
                                return shuttle;
                        }
                        // test if resulting geometry is compatible with current layer settings,
                        // regarding multi-object support
                        String multi = shuttle.parameters.getProperty(key + "_MULTI_OBJECTS");
                        boolean multiAllowed = (multi != null && "true".equalsIgnoreCase(multi));
                        // no empty result allowed for the moment...
                        if (g.isEmpty()) {
                                logger.error("Empty geometry generated...");
                                shuttle.errorCode = KaboumFeatureModes.K_GEOMETRY_EMPTY;
                                return shuttle;
                        }
                        // no more multi test for SPLITTING mode, to allow single polygons to be split
                        // by the client.
                        if ((shuttle.mode != KaboumFeatureModes.K_POLYGON_SPLITTING
                                && shuttle.mode != KaboumFeatureModes.K_POLYGON_SPLITTING_BY_LINE)
                                && !multiAllowed
                                && ((g instanceof MultiPoint && ((MultiPoint) g).getNumGeometries() > 1)
                                || (g instanceof MultiPolygon && ((MultiPolygon) g).getNumGeometries() > 1)
                                || (g instanceof MultiLineString && ((MultiLineString) g).getNumGeometries() > 1))) {
                                logger.error("MultiObject generated and layer: " + key + " does not allow MULTIxxx");
                                shuttle.errorCode = KaboumFeatureModes.K_MULTI_OBJECT;
                                return shuttle;
                        }
                        // test geometry collection return, which is not currently supported
                        if (g instanceof GeometryCollection
                                && (!(g instanceof MultiPoint)
                                && !(g instanceof MultiPolygon)
                                && !(g instanceof MultiLineString))) {
                                logger.error("kaboum geometry collection generated");
                                shuttle.errorCode = KaboumFeatureModes.K_GEOMETRY_COLLECTION;
                                return shuttle;
                        }
                        geom = KaboumJTSFactory.getKaboumGeometry(g);
                        if (geom == null) {
                                logger.error("null kaboum geometry returned");
                                shuttle.errorCode = KaboumFeatureModes.K_INVALID_OBJECT;
                        } else {
                                vec.add(geom);
                                newGeoms.put(key, vec);
                                shuttle.geometries = newGeoms;
                        }
                } // end while
                long end = System.currentTimeMillis();
                logger.info("Spatial analysis done in: "
                        + ((end-begin)/1000.0) + "s.");

                return shuttle;
        }
}
