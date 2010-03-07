/*
 * JTSCutPolygonAction.java
 *
 * Created on 18 aout 2005, 16:43
 */

package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * Class to cut a given polygon (the first one in the geometries) with another (the second one)
 * The geometries Vector MUST contains 2 polygons or 2 multipolygons.
 * @author Nicolas Ribot
 */
public class JTSCutPolygonAction extends JTSAction {
    private static Logger logger = Logger.getLogger(JTSCutPolygonAction.class);

    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     */
    public JTSCutPolygonAction(Vector geometries) {
        super(geometries);
    }
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     * @param keepID Should resulting geometry's id be set to the first geometry one
     */
    public JTSCutPolygonAction(Vector geometries, boolean keepID) {
        super(geometries, keepID);
    }
    

    /**
     * returns the JTS geometry corresponding to the cutting of the first polygon contained
     *  into the geometries Vector with the second one.
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
    throws TopologyException, IllegalArgumentException, AssertionFailedException{
        Geometry res = null;
        
        if (geometries == null || geometries.size() != 2) {
            logger.error("null geoms or size != 2");
            return null;
        }
        if (! ((geometries.get(0) instanceof Polygon) || (geometries.get(0) instanceof MultiPolygon)) || 
                ! ((geometries.get(1) instanceof Polygon) || (geometries.get(1) instanceof MultiPolygon))) {
            //System.out.println(geometries.get(0).getClass() + " " + geometries.get(1).getClass());
            logger.error("geometries are not poylgon or multipolygon types");
            return null;
        }
        
        Geometry geom = (Geometry) geometries.get(0);
        Geometry cutter = (Geometry) geometries.get(1);
        Geometry geom1 = null;
        Geometry geom2 = null;
        
        // tests if geom and cutter intersect.
        if (! geom.intersects(cutter)) {
            return geom;
        }
        
        try {
            geom1 = geom.intersection(cutter);
            
            if ( geom1.intersects(geom) ) {
                // perform operation only if polygons intersect
                geom2 = geom.difference(cutter);
            } 
        } catch (TopologyException te) {
            throw (te);
        } catch (IllegalArgumentException iae) {
            throw (iae);
        }
        
        // returns a result only if it is polygon
        if ( (geom1 instanceof Polygon || geom1 instanceof MultiPolygon) &&
                (geom2 instanceof Polygon || geom2 instanceof MultiPolygon)) {
            // will now extract all polygons from results and put them in a new Multipolygon
            // containing then all the polygons forming the result.
            Vector vec = new Vector(2);
            
            if (geom1 instanceof Polygon) {
                vec.add((Polygon) geom1);
            } else if (geom1 instanceof MultiPolygon) {
                for (int i = 0; i < ((MultiPolygon) geom1).getNumGeometries(); i++) {
                    vec.add((Polygon) ((MultiPolygon) geom1).getGeometryN(i));
                }
            }
            if (geom2 instanceof Polygon) {
                vec.add((Polygon) geom2);
            } else if (geom2 instanceof MultiPolygon) {
                for (int i = 0; i < ((MultiPolygon) geom2).getNumGeometries(); i++) {
                    vec.add((Polygon) ((MultiPolygon) geom2).getGeometryN(i));
                }
            }
            // dirty way to populate needed Polygon array
            Polygon[] polys = new Polygon[vec.size()];
            
            int i = 0;
            for (Iterator iter = vec.iterator(); iter.hasNext();) {
                polys[i++] = (Polygon)iter.next();
            }
            
            res = new GeometryFactory().createMultiPolygon(polys);
            
            copyUserData(res, (Geometry) geometries.get(0));
            Geometry g = createCollection(res);
            //System.out.println("g: " + g.toText());
            return g;
            
        } else {
            logger.error("invalid geometry type: " + geom1.getClass() + " " + geom2.getClass());
            return null;
        }
    }
}
