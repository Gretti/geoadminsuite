/*
 * KaboumFeatureModes.java
 *
 * Created on 6 aout 2005, 19:25
 */

package org.kaboum.util;

import java.lang.reflect.Field;

/**
 * The interface defining all supported modes for feature processing.
 *
 * @author Nicolas
 */
public class KaboumFeatureModes {
    ///////// Validation MODES ////////////////
    /** The mode for adding geometries */
    public final static short K_ADD_FEATURES = 2;
    /** The mode for removing geometries*/
    public final static short K_REMOVE_FEATURES = 3;
    /** The mode for updating geometries*/
    public final static short K_UPDATE_FEATURES = 4;
    
    ///////// DATA retrieval modes ////////////////
    /** Ask server to get all the features for the given mapextent (a mandatory parameter) */
    public final static short K_GET_FEATURES = 5;
    //// The KaboumFeatureShuttle modes
    ///////// SPATIAL ANALYSIS MODES ////////////////
    /** The mode for poylgon union */
    public final static short K_UNION = 1;
    /** The mode for poylgon intersection*/
    public final static short K_INTERSECTION = 6;
    /** The mode for poylgon difference */
    public final static short K_DIFFERENCE = 7;
    /** The mode for poylgon symmetric difference */
    public final static short K_SYM_DIFFERENCE = 8;
    /** The mode for poylgon hole completion */
    public final static short K_HOLES_COMPLETION = 9;
    /** The mode for poylgon cutting */
    public final static short K_POLYGON_SPLITTING = 10;
    public final static short K_POLYGON_SPLITTING_BY_LINE = 35;
    /** The mode for poylgon partial erasing, a special case of poylgon difference
     * where only 2 polygons are processed.
     */
    public final static short K_POLYGON_ERASING = 11;
    
    /** Mode for polygon union by drawing an arrow. Special UNION case
     * where only 2 polygons are processed, and target polygon is preserved (= polygon update)
     */
    public final static short K_FUSION = 12;
    
    /** mode to cut the drawn polygon with all overlapping polygons. New polygon is created */
    public final static short K_POLYGON_FITTING = 13;
    /** mode to union selected polygon with a drawn polygon. Selected is updated*/
    public final static short K_POLYGON_COMPLETION = 14;
    /** mode to union selected polygon with a drawn polygon. Selected is updated. Result is cut to fit neighbour
     * polygons.
     */
    public final static short K_POLYGON_COMPLETION_FITTING = 15;
    /** The mode for poylgon OpMode*/
    public final static short POLYGON = 16;
    /** The mode for point OpMode*/
    public final static short POINT = 17;
    /** The mode for Linestring OpMode*/
    public final static short LINESTRING = 18;
    public final static short BOXSELECTION = 19;
    public final static short CENTER = 20;
    public final static short DISTANCE = 21;
    public final static short GEOMETRY = 22;
    public final static short K_ASYMETRIC_OPERATION = 23;
    public final static short K_SYMETRIC_OPERATION = 24;
    public final static short LINESELECTION = 25;
    public final static short MODIFY = 26;
    public final static short MULTISELECTION = 26;
    public final static short PAN = 28;
    public final static short QUERY = 29;
    public final static short ROISELECTION = 30;
    public final static short SELECTION = 31;
    public final static short SURFACE = 32;
    public final static short ZOOMIN = 33;
    public final static short ZOOMOUT = 34;
            
    
    ///// The KaboumFeatureShuttle error codes ////////////
    /** One topological operation produced an invalid object, or third-party application
     * returned invalid geomtries objects.
     */
    public final static short K_INVALID_OBJECT = 1024;
    /** One topological operation produced a result mixing several types of geoemtries:
     * polygon, point, linestring, etc.
     */
    public final static short K_HETEROGENE_RESULT = 1025;
    /** The Kaboum Map Extent was not provided by the client when querying Feature objects */
    public final static short K_MISSING_MAP_EXTENT = 1026;
    /** One topological operation produced a geometryCollection.
     * (support for geometry collections is a kaboum parameter)
     */
    public final static short K_GEOMETRY_COLLECTION = 1027;
    /** the path to the server Feature file is missing in the servlet's configuration */
    public final static short K_MISSING_FEATURE_FILE = 1028;
    /** the path to the server feature file is wrong */
    public final static short K_INVALID_FEATURE_FILE = 1029;
    /** the received shuttle is null*/
    public final static short K_NULL_SHUTTLE = 1030;
    /** the received shuttle's parameters are null*/
    public final static short K_NULL_SHUTTLE_PARAMETERS = 1031;
    /** the received shuttle's parameter mode is not supported*/
    public final static short K_UNSUPPORTED_MODE = 1032;
    /** the received shuttle's geometries are null */
    public final static short K_NULL_GEOMETRIES = 1033;
    /** DataStore is badly configured and cannot be instanciated */
    public final static short K_INVALID_DATASTORE_PROPERTIES = 1034;
    /** Feature adding failed */
    public final static short K_ADD_FEATURES_FAILED = 1035;
    /** Feature update failed */
    public final static short K_UPDATE_FEATURES_FAILED = 1036;
    /** Feature removal failed */
    public final static short K_REMOVE_FEATURES_FAILED = 1037;
    /** No GeometryAccess class available */
    public final static short K_INVALID_GEOMETRY_ACCESS_CLASS = 1038;
    /** the underlying feature datastore is read-only */
    public final static short K_READ_ONLY_DATASTORE = 1040;
    /** the underlying feature datastore is not available and write acces cannot be guessed */
    public final static short K_UNAVAILABLE_DATASTORE = 1041;
    /** the access to the datastore is not allowed, though its configuration is valid:
     * probably a user-access problem (file rights, DB grant access)
     */
    public final static short K_DATASTORE_ACCESS_DENIED = 1042;
    /** the configured maximum number of features to get is reached
     */
    public final static short K_MAX_FEATURES_REACHED = 1043;
    /** a JTS Topology exception was thrown */
    public final static short K_TOPOLOGY_EXCEPTION = 1044;
    /** unique feature id cannot be retrived after an add operation
     * kaboum should force the layer reload to get the feature id from datastore directly
     */
    public final static short K_NO_GENERATED_ID = 1045;
    /** The Kaboum scale was not provided by the client when querying Feature objects */
    public final static short K_MISSING_SCALE = 1046;
    /** applet's memory is full... Should advice user to zoomin, to limit the number of features,
     * or to increase the JVM memory
     */
    public final static short K_OUT_OF_MEMORY = 1047;
    /** an operation generated a multi object for a layer that was configured to disallow
     * this kind of object
     */
    public final static short K_MULTI_OBJECT = 1048;
    /** storage expected a certain geometry type, but a bad type was sent
     */
    public final static short K_INCONSISTENT_GEOM_TYPE = 1049;
    /** Empty geometry generated by JTS spatial analysis */
    public final static short K_GEOMETRY_EMPTY = 1050;
    
    // various keys to store objects into application context (session, request, etc).
    /** the key under which the user Metadata string (probably a Json object) is stored into
     * the request, on KaboumServer side
     * 
     */
    public final static String K_USER_METADATA = "K_USER_METADATA";
    
    /** the keyword for tooltip replacement (see kaboumServer.properties) to get 
     * the Feature id in a Kaboum tooltip
     */
    public final static String K_TOOLTIP_FEATURE_ID_KEY = "$$FID$$";
    /** the keyword for tooltip replacement (see kaboumServer.properties) to get 
     * the polygon surface id in a Kaboum tooltip
     */
    public final static String K_TOOLTIP_AREA_KEY = "$$AREA$$";
    /** the keyword for tooltip replacement (see kaboumServer.properties) to get 
     * the polygon perimeter or linestring length in a Kaboum tooltip
     */
    public final static String K_TOOLTIP_PERIMETER_KEY = "$$PERIMETER$$";
    
    
    /**
     * returns an error String corresponding to the error code.
     * this keyword can be sent to the client to take appropriate action upon error
     *@param errorCode - The KaboumFeatureShuttle's errorCode. One of those defined in this class
     *@return a Kaboum keyword representing this errorCode
     */
    public static String getErrorCode(short errorCode) {
        switch( errorCode ) {
            case K_INVALID_OBJECT:
                return "K_INVALID_OBJECT";
            case K_HETEROGENE_RESULT:
                return "K_HETEROGENE_RESULT";
            case K_MISSING_MAP_EXTENT:
                return "K_MISSING_MAP_EXTENT";
            case K_GEOMETRY_COLLECTION:
                return "K_GEOMETRY_COLLECTION";
            case K_MISSING_FEATURE_FILE:
                return "K_MISSING_FEATURE_FILE";
            case K_INVALID_FEATURE_FILE:
                return "K_INVALID_FEATURE_FILE";
            case K_NULL_SHUTTLE:
                return "K_NULL_SHUTTLE";
            case K_NULL_SHUTTLE_PARAMETERS:
                return "K_NULL_SHUTTLE_PARAMETERS";
            case K_UNSUPPORTED_MODE:
                return "K_UNSUPPORTED_MODE";
            case K_NULL_GEOMETRIES:
                return "K_NULL_GEOMETRIES";
            case K_INVALID_DATASTORE_PROPERTIES:
                return "K_INVALID_DATASTORE_PROPERTIES";
            case K_ADD_FEATURES_FAILED:
                return "K_ADD_FEATURES_FAILED";
            case K_UPDATE_FEATURES_FAILED:
                return "K_UPDATE_FEATURES_FAILED";
            case K_REMOVE_FEATURES_FAILED:
                return "K_REMOVE_FEATURES_FAILED";
            case K_INVALID_GEOMETRY_ACCESS_CLASS:
                return "K_INVALID_GEOMETRY_ACCESS_CLASS";
            case K_READ_ONLY_DATASTORE:
                return "K_READ_ONLY_DATASTORE";
            case K_UNAVAILABLE_DATASTORE:
                return "K_UNAVAILABLE_DATASTORE";
            case K_DATASTORE_ACCESS_DENIED:
                return "K_DATASTORE_ACCESS_DENIED";
            case K_MAX_FEATURES_REACHED:
                return "K_MAX_FEATURES_REACHED";
            case K_TOPOLOGY_EXCEPTION:
                return "K_TOPOLOGY_EXCEPTION";
            case K_NO_GENERATED_ID:
                return "K_NO_GENERATED_ID";
            case K_MISSING_SCALE:
                return "K_MISSING_SCALE";
            case K_OUT_OF_MEMORY:
                return "K_OUT_OF_MEMORY";
            case K_MULTI_OBJECT:
                return "K_MULTI_OBJECT";
            case K_INCONSISTENT_GEOM_TYPE:
                return "K_INCONSISTENT_GEOM_TYPE";
            case K_GEOMETRY_EMPTY:
                return "K_GEOMETRY_EMPTY";
            default:
                return "K_INVALID_ERROR_CODE: " + errorCode;
        }
    }
    
    /**
     * returns the String corresponding to the given mode.
     *@param mode - The KaboumFeatureShuttle's errorCode. One of those defined in this class
     *@return a Kaboum keyword representing this mode
     */
    public static String getMode(short mode) {
        switch( mode ) {
            case K_UNION:
                return "K_UNION";
            case POLYGON:
                return "POLYGON";
            case POINT:
                return "POINT";
            case LINESTRING:
                return "LINESTRING";
            case K_INTERSECTION:
                return "K_INTERSECTION";
            case K_DIFFERENCE:
                return "K_DIFFERENCE";
            case K_SYM_DIFFERENCE:
                return "K_SYM_DIFFERENCE";
            case K_HOLES_COMPLETION:
                return "K_HOLES_COMPLETION";
            case K_POLYGON_SPLITTING:
                return "K_POLYGON_SPLITTING";
            case K_POLYGON_SPLITTING_BY_LINE:
                return "K_POLYGON_SPLITTING_BY_LINE";
            case K_ADD_FEATURES:
                return "K_ADD_FEATURES";
            case K_REMOVE_FEATURES:
                return "K_REMOVE_FEATURES";
            case K_UPDATE_FEATURES:
                return "K_UPDATE_FEATURES";
            case K_GET_FEATURES:
                return "K_GET_FEATURES";
            case K_POLYGON_ERASING:
                return "K_POLYGON_ERASING";
            case K_FUSION:
                return "K_FUSION";
            case K_POLYGON_FITTING:
                return "K_POLYGON_FITTING";
            case K_POLYGON_COMPLETION:
                return "K_POLYGON_COMPLETION";
            case K_POLYGON_COMPLETION_FITTING:
                return "K_POLYGON_COMPLETION_FITTING";
            case BOXSELECTION:
                return "BOXSELECTION";
            case CENTER:
                return "CENTER";
            case DISTANCE:
                return "DISTANCE";
            case GEOMETRY:
                return "GEOMETRY";
            case K_ASYMETRIC_OPERATION:
                return "K_ASYMETRIC_OPERATION";
            case K_SYMETRIC_OPERATION:
                return "K_SYMETRIC_OPERATION";
            case LINESELECTION:
                return "LINESELECTION";
            case MODIFY:
                return "MODIFY";
            case PAN:
                return "PAN";
            case QUERY:
                return "QUERY";
            case ROISELECTION:
                return "ROISELECTION";
            case SELECTION:
                return "SELECTION";
            case SURFACE:
                return "SURFACE";
            case ZOOMIN:
                return "ZOOMIN";
            case ZOOMOUT:
                return "ZOOMOUT";
            default:
                return "K_INVALID_MODE: " + mode;
        }
    }
    
    /**
     * Returns the code of the given OpMode, expressed as a String
     *<p>
     *an introspection mechanism will be used to map the given string with one of this class'
     *fields.<br>
     *In case of NoSuchFieldException, -1 is returned
     *</p>
     *@param mode: the name of the OpMode (see kaboum doc for OpMode naming rules
     *@return the OpMode code or -1 if given string does not correspond to a known OpMode
     */ 
    public static short getModeCode(String mode) {
        if (mode == null) {
            return -1;
        }
        try {
            Field f = Class.forName("org.kaboum.util.KaboumFeatureModes").getField(mode);
            return f.getShort(f);
        } catch (NoSuchFieldException nsfe) {
            return -1;
        } catch (ClassNotFoundException cnfe) {
            return -1;
        } catch (IllegalAccessException iae) {
            return -1;
        }
    }
}
