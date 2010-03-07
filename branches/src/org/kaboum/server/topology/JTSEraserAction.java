/*
 * JTSEraserAction.java
 *
 * Created on 20 septembre 2005, 11:00
 */
package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * Performs geometry erasing by taking one geometry and one polygon:
 * the geometry to erase partially and the eraser pattern.<br>
 * The geometries Vector MUST contains 2 polygons or 2 multipolygons.
 *<p>
 * The difference between first geometry and second polygon will be made.
 *</p>
 * @author Nicolas Ribot
 */
public class JTSEraserAction extends JTSAction {
        private static Logger logger = Logger.getLogger(JTSEraserAction.class);

        /**
         *
         * @param geometries
         */
        public JTSEraserAction(Vector geometries) {
                super(geometries);
        }

        /**
         * The constructor
         * @param geometries The vector of geometries to cut
         * @param keepID Should resulting geometry's id be set to the first geometry one
         */
        public JTSEraserAction(Vector geometries, boolean keepID) {
                super(geometries, keepID);
        }

        /** returns the JTS geometry corresponding to the erasing of the first geometry by the second polygon:<br>
         *<p>
         * The difference between first geometry and second geometry will be made.
         *</p>
         *@return the JTS Geometry resulting from the spatial analysis operation, or null if
         *geometries vector does not contain one geometry and one polygon or multipolygon,
         * @throws com.vividsolutions.jts.geom.TopologyException If a JTS error occured
         * @throws java.lang.IllegalArgumentException If a JTS error occured
         * @throws com.vividsolutions.jts.util.AssertionFailedException If a JTS error occured
         */
        public Geometry getResult()
                throws TopologyException, IllegalArgumentException, AssertionFailedException {
                if (geometries == null) {
                        return null;
                }

                if (geometries == null || geometries.size() != 2) {
                        return null;
                }

                Geometry geomToErase = (Geometry) geometries.get(0);
                Geometry eraser = (Geometry) geometries.get(1);

                if (!(eraser instanceof Polygon) && !(eraser instanceof MultiPolygon)) {
                        logger.error("eraser geometry is not poylgon or multipolygon type");
                        return null;

                }
                Geometry geom = null;

                try {
                        geom = geomToErase.difference(eraser);
                } catch (TopologyException te) {
                        throw (te);
                } catch (IllegalArgumentException iae) {
                        throw (iae);
                }
                copyUserData(geom, geomToErase);
                return createCollection(geom);
        }
}
