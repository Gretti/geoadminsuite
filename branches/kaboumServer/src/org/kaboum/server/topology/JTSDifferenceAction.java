/*
 * JTSDifferenceAction.java
 *
 * Created on 17 aout 2005, 20:34
 */

package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.Vector;
import org.kaboum.server.UserData;


/**
 * Performs a JTS Difference operation on configured JTS geometries objects
 *(contained in the geometries property)
 * @author Nicolas Ribot
 */
public class JTSDifferenceAction extends JTSAction {
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     */
    public JTSDifferenceAction(Vector geometries) {
        super(geometries);
    }
    
     /**
     * The constructor
     * @param geometries The vector of geometries to cut
     * @param keepID Should resulting geometry's id be set to the first geometry one
     */
   public JTSDifferenceAction(Vector geometries, boolean keepID) {
        super(geometries, keepID);
    }
    
    /** returns the JTS geometry corresponding to the spatial analysis Difference operation done
     * on the geomtries property.
     * <p>
     * This method will perform a recursive Difference operation with geometries contained in
     * the geometries Vector, taken the first one and subtracting the second object to it.
     * The next object is then subtracted from the result, and so on. <br>
     * The order of geometries in the geometries Vector is then important.<br>
     * If geometries contains only one element, it will be returned without JTS operation
     *</p>
     *@return the JTS Geometry resulting from the Difference spatial analysis operation
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
            // returns the geometry itself: no JTS operation performed
            return (Geometry) geometries.get(0);
        }
        
        Geometry originalGeom = (Geometry) geometries.get(0);
        
        try {
            for (int i = 1; i < geometries.size(); i++) {
                Geometry geom = (Geometry) geometries.get(i);
                
                if (originalGeom.intersects(geom)) {
                    //System.out.println("making difference with " + ((UserData)geom.getUserData()).id);
                    originalGeom = originalGeom.difference((Geometry)geometries.get(i));
                }
            }
        } catch (TopologyException te) {
            throw (te);
        } catch (IllegalArgumentException iae) {
            throw (iae);
        }
        
        if (keepID) {
            copyUserData(originalGeom, (Geometry) geometries.get(0));
        } else {
            copyUserData(originalGeom, null);
        }
        Geometry g = createCollection(originalGeom);
        //System.out.println(g.toText());
        return g;
    }
}
