/*
 * AbstractGeometryAccess.java
 *
 * Created on 15 septembre 2005, 09:08
 */

package org.kaboum.server;

import com.vividsolutions.jts.geom.Envelope;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Kaboum Server abstract implementation of GeometryAccess.
 * <p>
 * Third party applications may provide KaboumServer with a class overridding this class
 * to get/set geometries in a custom manner.
 * The class' constructor takes 2 parameters: the KaboumServer properties object and a UserData object 
 * (currently, the HttpServletRequest object)
 * </p>
 * <p>
 * This abstract class defines the userData field, and one constructor.<br>
 * The userData field represents runtime information received by this class
 * when it is instanciated, allowing implementors to access specific, context-related
 * information.
 * </p>
 * <p>
 * Currently, this userData information is represented by the HttpServletRequest object received
 * by the KaboumServer from the Kaboum Applet
 * </p>
 * <p>
 * It is the responsability of the the Implementor to check the actual userData type.
 * </p>
 * <p>
 * Kaboum Server framework provides its own implementation, using Geotools as the
 * data access library
 * </p>
 * <p>
 * For Kaboum Server, a geometry is a JTS Geometry whose userData field is set to
 * a org.kaboum.server.UserData object, with an id field set to a non-null value.<br>
 * It is vital to ensure that all given geometries contain a globally unique identifier
 * that Kaboum Applet will use as a key to store geoemtries in its internal structure.<br>
 * a common acceptable value is, for instance, <featureName>.<featureId>.
 * </>
 * @author Nicolas Ribot
 */
public abstract class AbstractGeometryAccess implements GeometryAccess {
    /**
     * Represents runtime information received by this class
     * when it is instanciated, allowing implementors to access specific, context-related
     * information.
     *<p>
     * Currently, this userData information is represented by the HttpServletRequest object received
     * by the KaboumServer from the Kaboum Applet
     *</p>
     *<p>
     * It is the responsability of the the Implementor to check the actual userData type.
     *</p>
     */
    Object userData;
    
    /**
     * The current kaboumFeatureModes error code.
     * <p>
     * It is up to the implementor of derived class to set the error code correctly, taken
     * available values in the KaboumFeatureModes class
     * </p>
     */
    protected short errorCode;
    
    /**
     * The Kaboum Server properties object
     *
     *<p>
     * Properties are built from a properties file configured in the Kaboum Server
     * environment and passed to an instance of this class when instanciating it by the
     * KaboumFeatureServlet servlet.
     *</p>
     *<p>
     * Third-party applications can manipulate or replace this properties object, for instance to create specific 
     * properties according to context. Be careful not to set this object to null, as it will cause client to 
     * display an error message. Also be sure to respect properties names and values to avoid errors
     *</p>
     */
    protected Properties kaboumServerProperties;
    
    /**
     * Constructor made private to allow only an instantiation with a Properties object.
     */
    protected AbstractGeometryAccess() {}
    
    /**
     * Creates a new instance of AbstractGeometryAccess built
     * with the given KaboumServer <CODE>Properties</CODE> object and userData object.
     * <p>
     * The properties object can be modified to provide Kaboum Applet with custom properties according
     * to application context. Just modify the Properties file or create a new one. It will be automatically
     * passed to the <CODE>KaboumFeatureShuttle</CODE>.<br>
     * A great care must be exercised to ensure the modified <CODE>Properties</CODE> object conforms to the expected properties.
     * Otherwise, Kaboum Applet won't be able to deal with properties and will generate an error.
     * </p>
     * <p>
     * Currently, the userData object is the HttpServletRequest received by the
     * KaboumFeatureServlet servlet from the Kaboum Applet
     * </p>
     * @param kaboumProps the KaboumServer properties object read from the configure properties file. 
     * Users may change properties on the fly.
     * @param userData An object represeting contextual user data. Currently, this object is the 
     * HttpServletRequest object received by the KaboumFeatureServlet when a Kaboum client
     * connects to the servlet.
     */
    public AbstractGeometryAccess(Properties kaboumProps, Object userData) {
        this.userData = userData;
        this.kaboumServerProperties = kaboumProps;
    }
    
    /**
     * Returns the current KaboumFeatureModes code (see this class).
     * <p>
     * implementors of this interface must provide a meaningful error code when dealing
     * with geometries. In case of error, they must set this code in order for the caller
     * to propagate this code to the end-user client.
     * </p>
     * @return the error code
     */
    public abstract short getErrorCode();
    
    /**
     * Gets all the geometries for layers configured in the Kaboum Server Properties file
     * laying in the given spatial extension.
     *<!--
     * For each layer, geometries are only returned if no minscale or maxscale parameter is defined,
     * or if  maxscale < scale < minscale
     *-->
     * <p>
     * in case of error, implementors must set the error code in order for getErrorCode
     * to return a pertinent code for the caller
     * </p>
     *@param mapExtent the current map extent to search features in
     *<!--@param scale the current map scale, used to filter features based on their
     *maxscale and minscale properties.-->
     *@return a Hashtable whose key is the name of the Kaboum geometry class and the 
     * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
     */
    public abstract Hashtable getGeometries(Envelope mapExtent/*, int scale*/);
    
    /**
     * adds the given collection of features to the underlying physical storage
     *
     * <p>
     * in case of error, implementors must set the error code in order for getErrorCode
     * to return a pertinent code for the caller
     * </p>
     *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the
     * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
     *@return a Hashtable containing added geomtries' with their identifiers updated.
     */
    public abstract Hashtable addGeometries(Hashtable geometries);
    
    /**
     * removes the given collection of features from the underlying physical storage
     *
     *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the
     * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
     *@return a code from KaboumFeatureModes if an error occured, or K_REMOVE_FEATURE if all was ok
     */
    public abstract short removeGeometries(Hashtable geometries);
    
    /**
     * updates the given collection of features in the underlying physical storage
     *
     * <p>
     * in case of error, implementors must set the error code in order for getErrorCode
     * to return a pertinent code for the caller
     * </p>
     *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the
     * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
     *@return a Hashtable containing added geomtries' with their identifiers unchanged, in order
     * for the caller to reinsert given geometries.
     */
    public abstract Hashtable updateGeometries(Hashtable geometries);
    
    /**
     * Sets the Kaboum Server properties object
     *@param kaboumProps the Properties object containing Kaboum parameters
     */
    public void setKaboumServerProperties(Properties kaboumProps) {
        this.kaboumServerProperties = kaboumProps;
    }
    
    /**
     * gets the Kaboum Server properties object
     *@return  the Properties object containing Kaboum parameters
     */
    public Properties getKaboumServerProperties() {
        return kaboumServerProperties;
    }
    
    /**
     * Gets the userData object.
     *@return the userData object
     */
    public Object getUserData() {
        return userData;
    }
    /**
     * Sets the userData object.
     */
    public void setUserData(Object o) {
        userData = o;
    }
}
