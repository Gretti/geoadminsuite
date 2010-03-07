/*
 * TopologyAdapter.java
 *
 * Created on 19 aout 2005, 19:25
 */

package org.kaboum.server.events;

import org.apache.log4j.Logger;


/**
 * <P>
 *An  adapter class for receiving topology events.
 * This class exists as convenience for creating listener objects.
 * <P>
 * topology events let you track when a spatial analysis function will be performed, and
 * after it is performed (union, intersection, holes filling, etc.)
 * <P>
 * Extend this class to create a <code>TopologyEvent</code> listener
 * and override the methods for the events of interest. (If you implement the
 * <code>TopologyListener</code> interface, you have to define all of
 * the methods in it. This class defines null methods for them
 * all, or methods just logging the action currently done, 
 * so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using the extended class and then register it with
 * a Kabou server using the server's <code>addTopologyListener</code>
 * method. When a spatial analysis operation is performed,
 * the relevant method in the listener object is invoked
 * and the <code>KaboumEvent</code> is passed to it.
 *
 *@author Nicolas Ribot
 */
public class TopologyAdapter implements TopologyListener {
    private boolean logInfo;
    
    private final static Logger logger = Logger.getLogger(FeatureAdapter.class);
    
    /** Creates a new instance of TopologyAdapter */
    public TopologyAdapter() {
        this(false);
    }
    
    /** Creates a new instance of TopologyAdapter with logging mechanism set to the given boolean value.
     * if true, each adapter's method will log to the console the current event occuring.
     */
    public TopologyAdapter(boolean shouldLog) {
        logInfo = shouldLog;
        
    }
    
    /** Invoked just before a UNION command occurs. */
    public void beforeUnionPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeUnionPerformed event");
        }
    }
    /** Invoked just after a UNION operation occurs. */
    public void afterUnionPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterUnionPerformed event");
        }
    }
    
    /** Invoked just before a intersection command occurs. */
    public void beforeIntersectionPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeIntersectionPerformed event");
        }
    }
    /** Invoked just after a intersection operation occurs. */
    public void afterIntersectionPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterIntersectionPerformed event");
        }
    }

    /** Invoked just before a difference command occurs. */
    public void beforeDifferencePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeDifferencePerformed event");
        }
    }
    /** Invoked just after a difference operation occurs. */
    public void afterDifferencePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterDifferencePerformed event");
        }
    }

    /** Invoked just before a symmetric difference command occurs. */
    public void beforeSymDifferencePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeSymDifferencePerformed event");
        }
    }
    /** Invoked just after a symmetric difference operation occurs. */
    public void afterSymDifferencePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterSymDifferencePerformed event");
        }
    }

    /** Invoked just before a Cut polygon command occurs. */
    public void beforeCutPolygonPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeCutPolygonPerformed event");
        }
    }
    /** Invoked just after a Cut polygon operation occurs. */
    public void afterCutPolygonPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterCutPolygonPerformed event");
        }
    }

    /** Invoked just before a fill holes command occurs. */
    public void beforeFillHolesPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeFillHolesPerformed event");
        }
    }
    /** Invoked just after a fill holes operation occurs. */
    public void afterFillHolesPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterFillHolesPerformed event");
        }
    }
    
}
