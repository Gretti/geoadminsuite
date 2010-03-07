/*
 * UserData.java
 *
 * Created on 26 aout 2005, 14:30
 */

package org.kaboum.server;

/**
 * The mandatory kaboum geometry attributes that must be added in a JTS Geometry in order
 * to make it valid for KaboumServer.
 *
 * <p>
 * any JTS geometry whose userData field is not an instance of this class will be rejected
 * by KaboumServer.
 * </p>
 * @author Nicolas
 */
public class UserData {
    /**
     * The geometry's unique identifier.
     * <p>
     * Any KaboumGeometry must have a global unique identifier
     * </p>
     */
    public String id;
    
    /**
     * The KaobumGeometry's toolTip
     * <p>
     * This field is optional. Kaboum Applet will display this tooltip when mouse is
     * over a geometry if toolTip mechanism is configured in the Kaboum Server properties file
     * </p>
     */
     public String toolTip;
    
    
    /** Creates a new instance of UserData */
    public UserData() {
    }
    
    /**
     * Creates a new instance of UserData with the given variables set
     * @param id The geometry's unique ID
     * @param toolTip The geometry's tooltip
     */
    public UserData(String id, String toolTip) {
        this.id = id;
        this.toolTip = toolTip;
    }    
}
