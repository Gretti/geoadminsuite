/*
 * JTSUnionAction.java
 *
 * Created on 17 aout 2005, 20:34
 */

package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.Vector;
import org.apache.log4j.Logger;



/**
 * Performs a JTS union operation on configured JTS Geometry objects
 *(contained in the geometries property)
 *
 * @author Nicolas Ribot
 */
public class JTSUnionAction extends JTSAction {
        private static Logger logger = Logger.getLogger(JTSUnionAction.class);
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     */
    public JTSUnionAction(Vector geometries) {
        super(geometries);
    }
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     * @param keepID Should resulting geometry's id be set to the first geometry one
     */
    public JTSUnionAction(Vector geometries, boolean keepID) {
        super(geometries, keepID);
    }
    
    /** Returns the JTS geometry corresponding to the spatial analysis UNION operation done
     * on the geomtries property.
     * <p>
     * This method will perform a recursive union operation with geometries contained in
     * the geometries Vector, taken the first one and 'unioning' it with the second object.
     * The result is then unioned with the next object, and so on. <br>
     * The order of geometries in the geometries Vector is not important.
     *</p>
     * If geometries contains only one element, it will be returned without JTS operation
     *@return the JTS Geometry resulting from the UNION spatial analysis operation
     * @throws com.vividsolutions.jts.geom.TopologyException If a JTS error occured
     * @throws java.lang.IllegalArgumentException If a JTS error occured
     * @throws com.vividsolutions.jts.util.AssertionFailedException If a JTS error occured
     */
    public Geometry getResult() 
    throws TopologyException, IllegalArgumentException, AssertionFailedException {
        if (geometries == null || geometries.size() == 0) {
            logger.error("getResultAsJTS received null or empty geoms");
            return null;
        }
        
        if (geometries.size() == 1) {
            // union is geometry itself
            return (Geometry) geometries.get(0);
        }
        
        Geometry originalGeom = (Geometry) geometries.get(0);
        
        try {
            for (int i = 1; i < geometries.size(); i++) {
                originalGeom = originalGeom.union( (Geometry) geometries.get(i) );
            }
        } catch (TopologyException te) {
            throw (te);
        } catch (IllegalArgumentException iae) {
            throw (iae);
        } catch (NullPointerException npe) {
            return null;
        }
         if (keepID) {
            copyUserData(originalGeom, (Geometry) geometries.get(0));
         } else {
            copyUserData(originalGeom, null);
         }
        
        return createCollection(originalGeom);
    }
}
