/*
 * KaboumServerCommand.java
 *
 * Created on 7 aout 2005, 15:38
 */

package org.kaboum.util;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Enumeration;

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import org.kaboum.geom.KaboumGeometry;

/**
 * Provides a container object allowing to send and receive commands, parameters and geographic objects from and to the server.
 * @author Nicolas
 */
public class KaboumFeatureShuttle implements Serializable {
    /** the operation mode: init, validate, union, etc.
     * @see KaboumFeatureModes interface for list of supported modes
     */
    public short mode = 0;
    
    /** the error code: .
     * @see KaboumFeatureModes interface for list of supported error codes
     */
    public short errorCode = -1;
    /**
     * The list of parameters to exchange between client and server.<p>
     * The kaboum feature properties are only exchanged when coming from the server.
     * It is not guaranteed that this object contains valid kaboum feature parameters when passed to the server.
     * When coming from the server, this object is initialized with the default values for parameters
     * The following parameters are supported:<br>
     * key = mapext, value = <the current kaboum mapextent, as a space-separated list of coordinates (ll, ur corners)
     * key = props, value = The path (relative to the KaboumServer context) to the properties file to use
     *                      in place of the default one configured in the KaboumFeatureServlet.
     * key = scale, value = <the current kaboum scale, as a integer, used to control layer visibility
     * key = <one kaboum parameter>, value = <a corresponding value>: see kaboum API for the complete list of parameters.
     */
    public Properties parameters = null;
    
    /** the hashtable of application layers: key is the layer name, value is a Vector of KaboumGeometry objects */
    //public Hashtable<String, Vector<KaboumGeometry>> geometries = null;
    public Hashtable geometries = null;
    
    /** 
     * The Kaboum User metadata object to exchange with the server (json object is a good idea)
     */
    public String userMetadata;
    
    /** Creates a new instance of KaboumServerCommand */
    public KaboumFeatureShuttle() {
        this(null);
    }
    
    /** Creates a new instance */
    public KaboumFeatureShuttle(String mapExt) {
        parameters = new Properties();
        //geometries = new Hashtable<String, Vector<KaboumGeometry>>();
        geometries = new Hashtable();
        
        if (mapExt != null) {
            parameters.put("mapext", mapExt);
        }
    }
    
    /** Creates a new instance of KaboumServerCommand with mapext and scale parameters set to the given values */
    public KaboumFeatureShuttle(String mapExt, int scale) {
        this(mapExt);
        parameters.setProperty("mapscale", String.valueOf(scale));
    }
    
    /** Creates a new instance of KaboumServerCommand with mapext and properties file parameters set to the given values */
    public KaboumFeatureShuttle(String mapExt, String props) {
        this(mapExt);
        if (props != null) {
            parameters.setProperty("props", props);
        }
    }
    
    /** Returns a string representation of a shuttle:<br>
     * mode, parameters, geometries properties.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n  mode: ");
        buf.append(KaboumFeatureModes.getMode(mode)).append(" (").append(mode).append(")");
        buf.append("\n");
        buf.append("\n  errorCode: ");
        buf.append(KaboumFeatureModes.getErrorCode(errorCode)).append(" (").append(errorCode).append(")");
        buf.append("\n");
        
        // parameters
        if (parameters != null) {
            buf.append("  params: ").append(parameters.toString());
            buf.append("\n");
            //            Enumeration en = parameters.keys();
            //
            //            while (en.hasMoreElements()) {
            //                String s = (String)en.nextElement();
            //                buf.append("  param: ").append(s).append(" - value: ").append(())
            //            }
        }
        
        // geometries
        if (geometries != null) {
            Enumeration en = geometries.keys();
            
            while (en.hasMoreElements()) {
                String s = (String)en.nextElement();
                buf.append("  class: ").append(s).append("\n");
                
                //Vector<KaboumGeometry> vec = geometries.get(s);
                Vector vec = (Vector)geometries.get(s);
                
                //for (KaboumGeometry geom : vec) {
                for (Iterator iter = vec.iterator(); iter.hasNext();) {
                    KaboumGeometry geom = (KaboumGeometry)iter.next();
                    buf.append("    geom type: ").append(geom.getGeometryType());
                    buf.append(" - dim: ").append(geom.getDimension());
                    buf.append(" - numpoints: ").append(geom.getNumPoints());
                    buf.append(" - id: ").append(geom.id);
                    buf.append(" - tooltip: ").append(geom.getToolTip()).append("\n");
                }
            }
        }
        return buf.toString();
        
    }
}
