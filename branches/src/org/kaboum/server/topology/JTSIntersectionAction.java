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



/**
 * Performs a JTS intersection operation on configured JTS Geometry objects
 *(contained in the geometries property)
 * @author Nicolas Ribot
 */
public class JTSIntersectionAction extends JTSAction {
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     */
    public JTSIntersectionAction(Vector geometries) {
        super(geometries);
    }
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     * @param keepID Should resulting geometry's id be set to the first geometry one
     */
    public JTSIntersectionAction(Vector geometries, boolean keepID) {
        super(geometries, keepID);
    }

     /** Returns the JTS geometry corresponding to the spatial analysis Intersection operation done
     * on the geomtries property.
     * <p>
     * This method will perform a recursive intersection operation with geometries contained in
     * the geometries Vector, taken the first one and intersecting it with the second object.
     * The result is then intersected with the next object, and so on. <br>
     * The order of geometries in the geometries Vector is then important.
     *</p>
     * If geometries contains only one element, it will be returned without JTS operation
     *@return the JTS Geometry resulting from the Intersection spatial analysis operation
     * @throws com.vividsolutions.jts.geom.TopologyException If a JTS error occured
     * @throws java.lang.IllegalArgumentException If a JTS error occured
     * @throws com.vividsolutions.jts.util.AssertionFailedException If a JTS error occured
     */
    public Geometry getResult()  
    throws TopologyException, IllegalArgumentException, AssertionFailedException {
        if (geometries == null || geometries.size() == 0) {
            return null;
        }
        
        if (geometries.size() == 1) {
            // Intersection is geometry itself
            return (Geometry) geometries.get(0);
        }
        
        Geometry originalGeom = (Geometry) geometries.get(0);
        
        try {
            for (int i = 1; i < geometries.size(); i++) {
                Geometry geom = (Geometry)geometries.get(i);
                originalGeom = originalGeom.intersection( (Geometry) geometries.get(i) );
            }
        } catch (TopologyException te) {
            throw (te);
        } catch (IllegalArgumentException iae) {
            throw (iae);
        }
        
        copyUserData(originalGeom, null);
        return createCollection(originalGeom);
    }
}
