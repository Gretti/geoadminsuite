/*
 * GeometryAccess.java
 *
 * Created on 26 aout 2005, 14:00
 */

package org.kaboum.server;

import com.vividsolutions.jts.geom.Envelope;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Kaboum Server public interface to access Geometries.
 * <p>
 * Third party applications may provide KaboumServer with a class implementing 
 * this interface to get/set geometries in a custom manner
 * </p>
 * <p>
 * Kaboum Server framework provides its own implementation, using Geotools as the 
 * data access library
 * </p>
 * <p>
 * For Kaboum Server, a geometry is a JTS Geometry which userData field is set to 
 * a org.kaboum.server.UserData object, with an id field set to a non-null value.<br>
 * It is vital to ensure that all given geometries contain a globally unique identifier
 * that Kaboum Applet will use as a key to store geometries in its internal structure.<br>
 * a common acceptable value is, for instance, "featureName"."featureId".
 * </>
 * @author Nicolas Ribot
 */
public interface GeometryAccess {
    public static final String KABOUM_USER_METADATA = "KABOUM_USER_METADATA";
    
    /**
     * Returns the current KaboumFeatureModes code (see this class).
     * <p>
     * implementors of this interface must provide a meaningful error code when dealing
     * with geometries. In case of error, they must set this code in order for the caller
     * to propagate this code to the end-user client.
     *</p>
     *@return the error code
     */
    short getErrorCode();
    
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
    Hashtable getGeometries(Envelope mapExtent/*, int scale*/);
    
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
    Hashtable addGeometries(Hashtable geometries);
    
    /**
     * removes the given collection of features from the underlying physical storage
     *
     *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the 
     * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
     *@return a code from KaboumFeatureModes if an error occured, or K_REMOVE_FEATURE if all was ok
     */
    short removeGeometries(Hashtable geometries);
    
    /**
     * updates the given collection of features in the underlying physical storage
     *
     * <p>
     * in case of error, implementors must set the error code in order for getErrorCode
     * to return a pertinent code for the caller
     * </p>
     *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the 
     * value is a vector of JTS Geometry with the userData field set to a KaboumServer UserData object.
     *@return a Hashtable containing added geomtries' with their identifiers unchanged, in order
     * for the caller to reinsert given geometries.
     */
    Hashtable updateGeometries(Hashtable geometries);
    
    /**
     * Gets the Kaboum Server properties object.
     *@return  the Properties object containing Kaboum parameters
     */
    public Properties getKaboumServerProperties();
    
    /**
     * Sets the Kaboum Server properties object
     *@param kaboumProps the Properties object containing Kaboum parameters
     */
    public void setKaboumServerProperties(Properties kaboumProps);
    
    /**
     * Gets the userData object.
     *@return the userData object
     */
    public Object getUserData();
    /**
     * Sets the userData object.
     */
    public void setUserData(Object o);
    
}
