/*
 * KaboumEvent.java
 *
 * Created on 19 aout 2005, 13:54
 */

package org.kaboum.server.events;

import java.util.EventObject;
import org.kaboum.util.KaboumFeatureShuttle;

/**
 * Class representing an event on the kaboum Server.
 * The source object will be a KaboumFeatureShuttle
 * @author Nicolas
 */
public class KaboumEvent extends EventObject {
    
    /** Creates a new instance of KaboumEvent */
    public KaboumEvent(KaboumFeatureShuttle source) {
        super(source);
    }
}
