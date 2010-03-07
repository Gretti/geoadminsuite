/*
 * FeatureAdapter.java
 *
 * Created on 19 aout 2005, 22:16
 */

package org.kaboum.server.events;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.kaboum.util.KaboumFeatureShuttle;

/**
 * <P>
 *An  adapter class for receiving feature retrieval events.
 * This class exists as convenience for creating listener objects.
 * <P>
 * feature retrieval events let you track when a feature retrieval operation will be performed, and
 * after it is performed (getting features, etc.)
 * <P>
 * Extend this class to create a <code>FeatureEvent</code> listener
 * and override the methods for the events of interest. (If you implement the
 * <code>FeatureListener</code> interface, you have to define all of
 * the methods in it. This class defines null methods for them
 * all, or methods just logging the action currently done,
 * so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using the extended class and then register it with
 * a Kabou server using the server's <code>addFeatureListener</code>
 * method. When a feature retrieval  is performed,
 * the relevant method in the listener object is invoked
 * and the <code>KaboumEvent</code> is passed to it.
 *
 *@author Nicolas Ribot
 */
public class FeatureAdapter implements FeatureListener {
    private boolean logInfo;
    
    private final static Logger logger = Logger.getLogger(FeatureAdapter.class);
    
    /** Creates a new instance of ValidateAdapter */
    public FeatureAdapter() {
        this(false);
    }
    
    /** Creates a new instance of ValidateAdapter with logging mechanism set to the given boolean value.
     * if true, each adapter's method will log to the console the current event occuring.
     */
    public FeatureAdapter(boolean shouldLog) {
        logInfo = shouldLog;
        
    }
    /** Called before a get_feature command is processed by Kaboum Server.
     * 
     *@param ev the KaboumEvent object, whose source is a KaboumFeatureShuttle
     */
    public void beforeGetFeaturePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeGetFeaturePerformed event");
        }
    }
    
    /** Called after a get_feature command is processed by Kaboum Server 
     *@param ev the KaboumEvent object, whose source is a KaboumFeatureShuttle
     */
    public void afterGetFeaturePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterGetFeaturePerformed event");
        }
        Object o = ev.getSource();
        
        if (! (o instanceof KaboumFeatureShuttle)) {
            logger.error("event source is not a KaboumFeatureShuttle");
            return;
        }
        
        Hashtable geoms = ((KaboumFeatureShuttle)o).geometries;
        
        if (geoms != null) {
            Enumeration keys = geoms.keys();
            
            while (keys.hasMoreElements()) {
                String s = (String)keys.nextElement();
                
                logger.info("num obj for class: " + s + " " + ((Vector)geoms.get(s)).size());
            }
        } else {
            logger.error("null geometries in shuttle");
        }
    }
}
