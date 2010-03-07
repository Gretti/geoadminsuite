/*
 * TopologyListener.java
 *
 * Created on 19 aout 2005, 14:06
 */

package org.kaboum.server.events;

import java.util.EventListener;

/**
 * the listener interface for receiving topology events occuring on the KaboumServer
 * when receiving from the client a command to process a spatial analysis function.
 * The class that is interested in processing an topology event implements this interface, 
 *  and the object created with that class is registered with a KaboumFeatureServer, 
 *  using the KaboumFeatureServer's addTopologyListener method. 
 *  When a topology event occurs, one of that object's method is invoked, according to 
 * the topology operation
 * @author Nicolas
 */
public interface TopologyListener extends EventListener {
    /** Invoked just before a UNION command occurs. */
    public void beforeUnionPerformed(KaboumEvent ev);
    /** Invoked just after a UNION operation occurs. */
    public void afterUnionPerformed(KaboumEvent ev);
    
    /** Invoked just before a intersection command occurs. */
    public void beforeIntersectionPerformed(KaboumEvent ev);
    /** Invoked just after a intersection operation occurs. */
    public void afterIntersectionPerformed(KaboumEvent ev);

    /** Invoked just before a difference command occurs. */
    public void beforeDifferencePerformed(KaboumEvent ev);
    /** Invoked just after a difference operation occurs. */
    public void afterDifferencePerformed(KaboumEvent ev);

    /** Invoked just before a symmetric difference command occurs. */
    public void beforeSymDifferencePerformed(KaboumEvent ev);
    /** Invoked just after a symmetric difference operation occurs. */
    public void afterSymDifferencePerformed(KaboumEvent ev);

    /** Invoked just before a Cut polygon command occurs. */
    public void beforeCutPolygonPerformed(KaboumEvent ev);
    /** Invoked just after a Cut polygon operation occurs. */
    public void afterCutPolygonPerformed(KaboumEvent ev);

    /** Invoked just before a fill holes command occurs. */
    public void beforeFillHolesPerformed(KaboumEvent ev);
    /** Invoked just after a fill holes operation occurs. */
    public void afterFillHolesPerformed(KaboumEvent ev);

}
