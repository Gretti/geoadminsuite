/*
 * ObjectKeys.java
 *
 * Created on 31 ao�t 2002, 16:41
 */
package org.geogurus.gas.utils;
/**
 * The class that defines:
 * 1�) the constants that are used to identify the objects stored in different 
 * scope objects (session, request, page).<br>
 * All servlets and JSP pages should use the constants defined here.
 * <p>This class is never instantiated. 
 */
public class ObjectKeys {
//    public static final String = ;
    // constants for base servlet;
    /** The key under which the servlet error message is stored */
    public final static String SERVLET_ERROR = "SERVLET_ERROR";
    /** The key under which the servlet information message is stored */
    public final static String SERVLET_MESSAGE = "SERVLET_MESSAGE";
    /** The key under which the SIG (kaboum Object validation) error message is stored */
    public final static String SIG_ERROR = "SIG_ERROR";
    /** The key under which the SIG (kaboum Object validation) message is stored */
    public final static String SIG_MESSAGE = "SIG_MESSAGE";
    /** the key to store the current mapfile object (com.scot.geonline.mapserver.objects.Map) */
    public final static String  CURRENT_MAPFILE = "CURRENT_MAPFILE";
    /** the key to store the current object treated, set by each <obj>Configurator servlet 
     *  the full name of the object class must be used as a value: ex:
     * com.scot.geonline.mapserver.objects.Legend
     */
    public final static String  CURRENT_OBJECT = "CURRENT_OBJECT";
    
    // constants for GOClassificationProperties
    /** The key under which the current GeometryClass identifier is stored.*/
    public final static String  CURRENT_GC = "CURRENT_GC";
    /** The key under which the current UserMapBean object is stored.
     *  (central object in GAS application).
     */
    public final static String  USER_MAP_BEAN = "usermapbean";
    /** The key under which the current KaboumBean object is stored.*/
    public final static String  KABOUM_BEAN = "kaboumbean";
    /** The key under which the bean representing the Uploaded map file object is stored.
     */
    public final static String  USER_MAPFILE_BEAN = "USER_MAPFILE_BEAN";
    /** the key under which the Map storing all available hosts for this session is stored */
    public final static String  HOST_LIST = "HOST_LIST";
    /** the key under which the Supported Projections for this session are stored */
    public final static String  PROJECTION_LIST = "PROJECTION_LIST";
    /** the key under which the Supported Projections for this session are stored */
    public final static String  CURRENT_PROJECTION = "CURRENT_PROJECTION";
    /** the key under which the KaboumApplet Object is stored in session */
    public final static String  KABOUM = "CURRENT_KABOUM_APPLET";
    /** the key under which the List of layer (comma-separated list of GeometryClass id) is stored in session */
    public final static String  LAYER_ORDER_LIST = "LAYER_ORDER_LIST";
    /** the key under which the ColorGenerator class is stored in session */
    public final static String  COLOR_GENERATOR = "COLOR_GENERATOR";
    /** the key under which the list of GeometryClass is stored in session */
    public final static String  USER_LAYER_LIST = "USER_LAYER_LIST";
    /** the key under which the list of selected catalog datasources is stored in request */
    public final static String  SELECTED_IDS = "SELECTED_IDS";
    
    // for classification part
    /** the key under which the classification type is stored in session.<br>
     *  (see this class for list of supported classification types
     */
    public final static String  CLASSIF_TYPE = "CLASSIF_TYPE";
    /** the key under which the classification message is stored in session.<br>
     *  This message is used by JSP pages to inform end-user
     */
    public final static String  CLASSIF_MESSAGE = "CLASSIF_MESSAGE";
    /** the key under which the List of layer (comma-separated list of GeometryClass id) is stored in session */
    public final static String  TMP_CLASSIFICATION = "TMP_CLASSIFICATION";
    /** the key under which the where clause for a GeometryClass is stored*/
    public final static String GC_WHERE_CLAUSE = "GC_WHERE_CLAUSE";
    /** the key under which the Hashtable containing all symbols is stored (in context) */
    public final static String GAS_SYMBOL_LIST = "GAS_SYMBOL_LIST";
    /** the constant of a range classification (n classes from min to max value) */
    public final static short RANGE = 0;
    /** the constant of a unique value-based classification (each class represent a unique value) */
    public final static short UNIQUE = 1;
    /** the constant of a Single class classification (only one class generated) */
    public final static short SINGLE = 2;
    /** refresh key */
    public final static String  REFRESH_KEY = "REFRESH";
    /** size of the map changed */
    public final static String  MAP_SIZE_CHANGED = "MAP_SIZE_CHANGED";
}
