/*
 * FeatureListener.java
 *
 * Created on 19 aout 2005, 14:02
 */

package org.kaboum.server.events;

import java.util.EventListener;

/**
 * Interface defining methods triggered when a GET_FEATURE command was sent by the client
 *@author Nicolas
 */
public interface FeatureListener extends EventListener {
    /** Called before a get_feature command is processed by Kaboum Server.
     * 
     *@param ev the KaboumEvent object, whose source is a KaboumFeatureShuttle
     */
    public void beforeGetFeaturePerformed(KaboumEvent ev);
    
    /** Called after a get_feature command is processed by Kaboum Server 
     *@param ev the KaboumEvent object, whose source is a KaboumFeatureShuttle
     */
    public void afterGetFeaturePerformed(KaboumEvent ev);
}
