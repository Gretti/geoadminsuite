/*
 * KaboumProperties.java
 *
 * Created on 4 juillet 2003, 10:35
 */

package org.geogurus;

import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.StringBuffer;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Hashtable;
import org.geogurus.gas.forms.KaboumPropertiesForm;

/**
 * extended properties class that enable kaboum to write itself reading a property container (file, hashtable, ...)
 * @author  gng
 */
public class KaboumProperties extends Properties implements Serializable {
    
    public static Hashtable hashAppletParams = new Hashtable();
    
    /** Creates a new instance of KaboumProperties */
    public KaboumProperties() {
        super();
        
        initAppletParams();
    }
    
    /** Creates a new instance of KaboumProperties */
    public KaboumProperties(Properties defaults) {
        super(defaults);
        
        initAppletParams();
    }
    
    /**
     * Load properties from a property file string
     */
    public boolean loadFromFilePath(String propertyFile) {
        try {
            this.load(new FileInputStream(propertyFile));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public String getAppletHtmlRepresentation() {
        StringBuffer result = new StringBuffer();
        Iterator it = this.entrySet().iterator();
        result.append("<applet ");
        String key = "";
        // init applet parameters: hashAppletParams contains default values in case of missing kaboum.properties file
        for (Enumeration e = hashAppletParams.keys() ; e.hasMoreElements() ;){
            key = e.nextElement().toString();
            // treats the MAYSCRIPT parameter. Not a key=value pair
            if (key.equalsIgnoreCase("MAYSCRIPT")) {
                result.append(key + " ");
            } else {
                result.append(key + "=\"" + this.getProperty(key, (String)hashAppletParams.get(key)) + "\" ");
            }
        }
        result.append(">\n");
        //all parameters instead of init ones
        while (it.hasNext()){
            java.util.Map.Entry mapEntry = (java.util.Map.Entry) it.next();
            if (hashAppletParams.get(mapEntry.getKey().toString()) == null) {
                result.append("<PARAM name=\"" + mapEntry.getKey().toString() + "\" value=\"" + mapEntry.getValue().toString() + "\">\n");
            }
        }
        return result.toString();
    }
    
    /**
     *@return the string corresponding to the closing applet tag.
     */
    public String closeAppletTag() {
        return ("</applet>");
    }
    
    private static void initAppletParams(){
        hashAppletParams.put("code", "org.kaboum.Kaboum.class");
        hashAppletParams.put("codebase", "applets/kaboum");
        hashAppletParams.put("archive", "kaboum.jar");
        hashAppletParams.put("alt", "map applet");
        hashAppletParams.put("align", "CENTER");
        hashAppletParams.put("width", "400");
        hashAppletParams.put("height", "400");
        hashAppletParams.put("MAYSCRIPT", "");
        hashAppletParams.put("name", "kaboum");
    }
    
}

