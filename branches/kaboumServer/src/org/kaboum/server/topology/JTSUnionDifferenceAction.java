/*
 * JTSUnionDifferenceAction.java
 *
 * Created on 10 octobre 2005, 12:21
 */

package org.kaboum.server.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * Union 2 geometries, cutting this union with all other neighbour geometries.
 * The geometries order in the given geometries vector decides which geometries to union
 * @author Nicolas
 */
public class JTSUnionDifferenceAction extends JTSAction {
        private static Logger logger = Logger.getLogger(JTSUnionDifferenceAction.class);
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     */
    public JTSUnionDifferenceAction(Vector geometries) {
        super(geometries);
    }
    
    /**
     * The constructor
     * @param geometries The vector of geometries to cut
     * @param keepID Should resulting geometry's id be set to the first geometry one
     */
    public JTSUnionDifferenceAction(Vector geometries, boolean keepID) {
        super(geometries, keepID);
    }
    
    /** Returns the JTS geometry corresponding to the spatial analysis UNION operation done
     * on the 2 first geometries in the given geometries vector, and make the difference between
     *  this union and all other intersecting vectors contained in the vector.
     * <p>
     *
     *</p>
     * If geometries contains only one element, it will be returned without JTS operation
     *@return the JTS Geometry resulting from the UNION and DIFFERENCE spatial analysis operation
     * @throws com.vividsolutions.jts.geom.TopologyException If a JTS error occured
     * @throws java.lang.IllegalArgumentException If a JTS error occured
     * @throws com.vividsolutions.jts.util.AssertionFailedException If a JTS error occured
     */
    public Geometry getResult() 
    throws TopologyException, IllegalArgumentException, AssertionFailedException {
        if (geometries == null || geometries.size() < 2) {
            logger.error("Null geometries or size of geometry list is < 2. Cannot process.");
            return null;
        }
        
        // builds a vector with only 2 geometries.
        Vector vec = new Vector(2);
        vec.add(geometries.elementAt(0));
        vec.add(geometries.elementAt(1));
        
        JTSUnionAction union = new JTSUnionAction(vec, true);
        
        Geometry geom = union.getResult();
        
        //System.out.println("union: " + geom.toText());
        
        if (geometries.size() > 2) {
            vec = new Vector(geometries.subList(2, geometries.size()));
            vec.insertElementAt(geom, 0);

            JTSDifferenceAction diff = new JTSDifferenceAction(vec, true);
            geom = diff.getResult();
        }
        return geom;
    }
}
