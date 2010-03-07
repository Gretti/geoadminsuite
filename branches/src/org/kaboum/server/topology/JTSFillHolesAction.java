/*
 * JTSFillHolesAction.java
 *
 * Created on 18 aout 2005, 11:06
 */

package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;


/**
 * Performs polygon holes filling by taking 2 polygons:
 * the polygon to fill and the fill pattern.<br>
 * The geometries Vector MUST contains 2 polygons or 2 multipolygons.
 * Basically, the second polygon will be intersected with the shell of the first polygon,
 * The result will then be unioned with all holes to (eventually) fill them.
 *
 * @author Nicolas Ribot
 */
public class JTSFillHolesAction extends JTSAction {
        private static Logger logger = Logger.getLogger(JTSFillHolesAction.class);
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     */
    public JTSFillHolesAction(Vector geometries) {
        super(geometries);
    }
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     * @param keepID Should resulting geometry's id be set to the first geometry one
     */
    public JTSFillHolesAction(Vector geometries, boolean keepID) {
        super(geometries, keepID);
    }
    
    /** returns the JTS geometry corresponding to the hole filling operation:<br>
     * Basically, the second polygon will be intersected with the shell of the first polygon,
     * The result will then be unioned with all holes to (eventually) fill them.
     *<p>
     *First and second geometries in geometries vector must be polygon or Multipolygon type.<br>
     *second geometry must be a polygon or a multipolygon with only one element.
     *</p>
     *@return the JTS Geometry resulting from the spatial analysis operation, or null if
     *geometries vector does not contain 2 polygons,
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
        if (! ((geometries.get(0) instanceof Polygon) || (geometries.get(0) instanceof MultiPolygon)) ||
                ! ((geometries.get(1) instanceof Polygon) || (geometries.get(1) instanceof MultiPolygon))) {
            logger.warn("geometries are not poylgon or multipg types: (" +
                    geometries.get(0).getClass() + " " + geometries.get(1).getClass() + ")");
            return null;
        }
        Geometry geom = null;
        Geometry inputGeom = (Geometry)geometries.get(0);
        
        // checks if input geometry has holes
        if (!geomHasHoles(inputGeom)) {
            logger.info("no holes in input geometry");
            return inputGeom;
        }
        
        GeometryFactory fact = new GeometryFactory();
        Geometry filling = null;

        // prepare the filling by extracting a polygon from input geometry
        if (geometries.get(1) instanceof Polygon) {
            filling = (Polygon) geometries.get(1);
        } else {
            if (((MultiPolygon) geometries.get(1)).getNumGeometries() != 1) {
                logger.error("Filling polygon contains several parts");
                return null;
            }
            filling = (Polygon)((MultiPolygon) geometries.get(1)).getGeometryN(0);
        }
        
        try {
            if (inputGeom instanceof Polygon) {
                Polygon pgToFill = fact.createPolygon((LinearRing)((Polygon) inputGeom).getExteriorRing(), null);
                filling = pgToFill.intersection(filling);
                
                // then union the complete input polygon with this new result
                geom = inputGeom.union(filling);
            } else {
                //then its a Multipg: makes intersection of input geom with each mpg exterior,
                // build a new mpg with these polygons,
                // union it with multipolygon
                Polygon pgToFill = null;
                MultiPolygon mpg = (MultiPolygon)inputGeom;
                MultiPolygon tmp = null;
                // stores created polygons
                Vector vec = new Vector();
                
                for (int i = 0; i < mpg.getNumGeometries(); i++) {
                    pgToFill = fact.createPolygon((LinearRing) ((Polygon)mpg.getGeometryN(i)).getExteriorRing(), null);
                    Geometry g = pgToFill.intersection(filling);
                    
                    if (! g.isEmpty() && g instanceof Polygon) {
                        vec.add((Polygon)g);
                    } else {
                        logger.warn("result is not polygon ?: " + g.getClass());
                    }
                }
                // build the multipolygon resulting from all intersections
                // dirty way to populate needed Polygon array
                Polygon[] polys = new Polygon[vec.size()];
                
                int i = 0;
                for (Iterator iter = vec.iterator(); iter.hasNext();) {
                    polys[i++] = (Polygon)iter.next();
                }
                
                tmp = fact.createMultiPolygon(polys);
                
                geom = inputGeom.union(tmp);
            }
        } catch (TopologyException te) {
            throw (te);
        } catch (IllegalArgumentException iae) {
            throw (iae);
        }
        
        copyUserData(geom, inputGeom);
        return createCollection(geom);
    }
}
