/*
 * ValidateAdapter.java
 *
 * Created on 19 aout 2005, 22:09
 */

package org.kaboum.server.events;

import org.apache.log4j.Logger;


/**
 * <P>
 *An  adapter class for receiving validation events.
 * This class exists as convenience for creating listener objects.
 * <P>
 * validation events let you track when a feature validation operation will be performed, and
 * after it is performed (validate one feature, validate all features, etc.)
 * <P>
 * Extend this class to create a <code>ValidateEvent</code> listener
 * and override the methods for the events of interest. (If you implement the
 * <code>ValidateListener</code> interface, you have to define all of
 * the methods in it. This class defines null methods for them
 * all, or methods just logging the action currently done,
 * so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using the extended class and then register it with
 * a Kabou server using the server's <code>addValidateListener</code>
 * method. When a validation operation is performed,
 * the relevant method in the listener object is invoked
 * and the <code>KaboumEvent</code> is passed to it.
 *
 * * @author Nicolas Ribot
 */
public class ValidateAdapter implements ValidateListener {
    private boolean logInfo;
    
    private final static Logger logger = Logger.getLogger(FeatureAdapter.class);
    
    /** Creates a new instance of ValidateAdapter */
    public ValidateAdapter() {
        this(false);
    }
    
    /** Creates a new instance of ValidateAdapter with logging mechanism set to the given boolean value.
     * if true, each adapter's method will log to the console the current event occuring.
     */
    public ValidateAdapter(boolean shouldLog) {
        logInfo = shouldLog;
    }
    
    /** Triggered before a add feature operation occured */
    public void beforeAddPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeAddPerformed event");
        }
    }
    /** Triggered after a add feature operation occured */
    public void afterAddPerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterAddPerformed event");
        }
    }
    
    /** Triggered before a remove feature operation occured */
    public void beforeRemovePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeRemovePerformed event");
        }
    }
    /** Triggered after a remove feature operation on occured */
    public void afterRemovePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterRemovePerformed event");
        }
    }
    
    /** Triggered before a uppdate feature operation occured */
    public void beforeUpdatePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("beforeUpdatePerformed event");
        }
    }
    /** Triggered after a update feature operation occured */
    public void afterUpdatePerformed(KaboumEvent ev) {
        if (logInfo) {
            logger.info("afterUpdatePerformed event");
        }
    }
}
