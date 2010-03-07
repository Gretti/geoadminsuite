/*
 * ValidateListener.java
 *
 * Created on 19 aout 2005, 13:57
 */

package org.kaboum.server.events;

import java.util.EventListener;

/**
/**
 * the listener interface for receiving features modification events occuring on the KaboumServer
 * when receiving from the client a command to process a add, remove or udpate feature(s).
 * The class that is interested in processing a validation event implements this interface, 
 *  and the object created with that class is registered with a KaboumFeatureServer, 
 *  using the KaboumFeatureServer's addValidationListener method. 
 *  When a validation event occurs, one of that object's method is invoked, according to 
 * the topology operation
 * @author Nicolas
 */
public interface ValidateListener extends EventListener {
    
    /** Triggered before a add feature operation occured */
    public void beforeAddPerformed(KaboumEvent ev);
    /** Triggered after a add feature operation occured */
    public void afterAddPerformed(KaboumEvent ev);
    
    /** Triggered before a remove feature operation occured */
    public void beforeRemovePerformed(KaboumEvent ev);
    /** Triggered after a remove feature operation on occured */
    public void afterRemovePerformed(KaboumEvent ev);

    /** Triggered before a uppdate feature operation occured */
    public void beforeUpdatePerformed(KaboumEvent ev);
    /** Triggered after a update feature operation occured */
    public void afterUpdatePerformed(KaboumEvent ev);
}
