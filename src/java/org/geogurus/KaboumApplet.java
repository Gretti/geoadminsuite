/*
 *
 * KaboumApplet.java
 *
 * Created on 06 june 2002, 14:05
 *
 */

package org.geogurus;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;


/**
 *
 * Defines a KaboumApplet object
 *
 * @author Jerome Gasperi
 */
public class KaboumApplet {
    
    // Kaboum initialisation keywords
    public static final String KABOUM_BACKGROUND_COLOR = "KABOUM_BACKGROUND_COLOR";
    public static final String KABOUM_BUSY_IMAGE_URL = "KABOUM_BUSY_IMAGE_URL";
    public static final String KABOUM_DEBUG_MODE = "KABOUM_DEBUG_MODE";
    public static final String KABOUM_DEFAULT_DRAWER = "KABOUM_DEFAULT_DRAWER";
    public static final String KABOUM_DRAWERS_LIST = "KABOUM_DRAWERS_LIST";
    public static final String KABOUM_HISTORY_SIZE = "KABOUM_HISTORY_SIZE";
    public static final String KABOUM_IMAGE_QUALITY = "KABOUM_IMAGE_QUALITY";
    public static final String KABOUM_IMAGE_TYPE = "KABOUM_IMAGE_TYPE";
    public static final String KABOUM_LANG = "KABOUM_LANG";
    public static final String KABOUM_MAPSERVER_CGI_URL = "KABOUM_MAPSERVER_CGI_URL";
    public static final String KABOUM_MINIMUM_SCALE = "KABOUM_MINIMUM_SCALE";
    public static final String KABOUM_REFERENCE_MAP_IS_APPLET = "KABOUM_REFERENCE_MAP_IS_APPLET";
    public static final String KABOUM_RESTRICT_EXPLORATION = "KABOUM_RESTRICT_EXPLORATION";
    public static final String KABOUM_USE_IMAGE_CACHING = "KABOUM_USE_IMAGE_CACHING";
    public static final String KABOUM_USE_LIVECONNECT = "KABOUM_USE_LIVECONNECT";
    public static final String KABOUM_FONT_NAME= "KABOUM_FONT_NAME";
    public static final String KABOUM_FONT_SIZE= "KABOUM_FONT_SIZE";
    public static final String KABOUM_FONT_STYLE= "KABOUM_FONT_STYLE";
    public static final String KABOUM_MAXIMUM_SCALE= "KABOUM_MAXIMUM_SCALE";
    public static final String KABOUM_MAXIMUM_EXTENT= "KABOUM_MAXIMUM_EXTENT";
    public static final String KABOUM_SEND_POSITION_COORDINATES_TO_JS= "KABOUM_SEND_POSITION_COORDINATES_TO_JS";
    public static final String KABOUM_USE_TOOLTIP= "KABOUM_USE_TOOLTIP";
    
    public static final String LAYERS = "LAYERS";
    public static final String MAPFILE_EXTENT = "MAPFILE_EXTENT";
    public static final String MAPFILE_PATH = "MAPFILE_PATH";
    public static final String MAPFILE_UNITS = "MAPFILE_UNITS";
    public static final String QUERY_LAYERS = "QUERY_LAYERS";
    
    
    /** Properties list */
    private Properties properties = null;
    
    
    /**
     *
     * Empty constructor
     *
     * Default attributes are Kaboum default attributes
     *
     */
    public KaboumApplet() {
        this.properties = new Properties();
    }
    
    public void addProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new Properties();
        }
        this.properties.put(key, value);
    }
    
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    public Properties getProperties() {
        return this.properties;
    }
    
    /**
     *
     * Load properties from a property file
     *
     */
    public boolean load(String propertyFile) {

        try {
            this.properties.load(new FileInputStream(propertyFile));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
        
    }
    
    
    /**
     *
     * Return the html representation of the
     * applet parameters
     *
     */
    public String getParametersHtmlRepresentation() {
        
        String result = "";
        
        Iterator it = this.properties.entrySet().iterator();
        
        while (it.hasNext()){
            java.util.Map.Entry mapEntry = (java.util.Map.Entry) it.next();
            result += "<PARAM name=\"" + mapEntry.getKey().toString() + "\" value=\"" + mapEntry.getValue().toString() + "\">\n";
        }
        
        return result;
    }
    
}

