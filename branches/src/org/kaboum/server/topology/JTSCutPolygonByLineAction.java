/*
 * JTSCutPolygonAction.java
 *
 * Created on 18 aout 2005, 16:43
 */
package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * Class to cut a given polygon or MultiPG (the first one in the geometries) with the given linstring (the second geometry)
 * The geometries Vector MUST contains 1 polygon or multipolygon and 1 linestring.
 * todo: use types collections
 * @author Nicolas Ribot
 */
public class JTSCutPolygonByLineAction extends JTSAction {

        private static Logger logger = Logger.getLogger(JTSCutPolygonAction.class);

        /**
      * The constructor
      * @param geometries The vector of geometries to cut
      */
        public JTSCutPolygonByLineAction(Vector geometries) {
                super(geometries);
        }

        /**
         * The constructor
         * @param geometries The vector of geometries to cut
         * @param keepID Should resulting geometry's id be set to the first geometry one
         */
        public JTSCutPolygonByLineAction(Vector geometries, boolean keepID) {
                super(geometries, keepID);
        }

        /**
         * returns the JTS Multipolygon geometry corresponding to the cutting of the first polygon contained
         *  into the geometries Vector with the second object that must be a linestring.
         * <p>
         * a result will be returned iff it is composed of polygons and multipolygons
         * </p>
         * @return A MultiPolygon resulting from the spatial analysis operation, or null if
         * geometries vector does not contain 2 polygons, or if cutting operations produced
         * something else than polygons or multipolygons
         * @throws com.vividsolutions.jts.geom.TopologyException If a JTS error occured
         * @throws java.lang.IllegalArgumentException If a JTS error occured
         * @throws com.vividsolutions.jts.util.AssertionFailedException If a JTS error occured
         */
        public Geometry getResult()
                throws TopologyException, IllegalArgumentException, AssertionFailedException {

                if (geometries == null || geometries.size() != 2) {
                        logger.error("null geoms or size != 2");
                        return null;
                }
                if (!((geometries.get(0) instanceof Polygon) || (geometries.get(0) instanceof MultiPolygon))
                        || !((geometries.get(1) instanceof LineString) || (geometries.get(1) instanceof MultiLineString))) {
                        //System.out.println(geometries.get(0).getClass() + " " + geometries.get(1).getClass());
                        logger.error("geometries are not poylgon or multipg types for the firt one, or linestring for the second one.");
                        return null;
                }
                Geometry sourceGeom = (Geometry) geometries.get(0);
                Geometry geom = sourceGeom.getBoundary();
                Geometry splitter = (Geometry) geometries.get(1);
                Geometry result = null;

                // tests if geom and cutter intersect.
                if (!geom.intersects(splitter)) {
                        return geom;
                }

                // split-by-line algorithm is taken from PostGIS wiki:
                // http://wiki.postgis.org/support/wiki/index.php?SplitPolygonWithLineString
                // basically, it consists of extracting linear rings, ,unioning them with given splitter line,
                // noding the linestring together and
                // polygonizing the result and removing from the result polygons that are holes.
                // build collection of lines in an heavy way, must be easier:

                GeometryFactory geomFactory = new GeometryFactory();
                //Vector<Geometry> lines = new Vector();
                Vector lines = new Vector();
                for (int i = 0; i < geom.getNumGeometries(); i++) {
                        lines.add(geom.getGeometryN(i));
                }
                for (int i = 0; i < splitter.getNumGeometries(); i++) {
                        lines.add(splitter.getGeometryN(i));
                }
                Geometry mls = geomFactory.createMultiLineString((LineString[]) lines.toArray(new LineString[0]));
                Point mlsPt = geomFactory.createPoint(mls.getCoordinate());
                Geometry nodedLines = mls.union(mlsPt);
                Polygonizer polygonizer = new Polygonizer();
                polygonizer.add(nodedLines);

                Collection polygons = polygonizer.getPolygons();
                //ArrayList<Polygon> polys = new ArrayList<Polygon>();
                ArrayList polys = new ArrayList();
                int i = 0;
                for (Iterator iter = polygons.iterator(); iter.hasNext();) {
                        // checks if given polygon is contained inside source pg, otherwise exclude it
                        Polygon pg = (Polygon) iter.next();
                        if (sourceGeom.contains(pg.getInteriorPoint())) {
                                polys.add(pg);
                        } else {
                                //logger.info("removing polygon: " + pg.toString());
                        }
                }
                result = geomFactory.createMultiPolygon((Polygon[]) polys.toArray(new Polygon[0]));

                copyUserData(result, (Geometry) geometries.get(0));
                result = createCollection(result);

                return result;
        }
}
