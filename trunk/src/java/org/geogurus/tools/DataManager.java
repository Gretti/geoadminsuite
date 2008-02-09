/*
 * DataManager.java
 *
 * Created on March 6, 2002, 2:08 PM
 */
package org.geogurus.tools;

import java.util.Properties;

/** A class storing application-level parameters. All other classes
 * can query this class to get an initialisation parameter.
 *
 * Properties must be loaded for this class by calling <CODE>setProperties()</CODE>.
 * @author Nicolas Ribot
 * @version 1.0
 */
 public class DataManager {
     /** The properties list for this class.
      * (should include a way to add properties from another properties
      * object in the current one)
      */     
    protected static Properties p;

    /** sets the given properties to this class
     * @param ps the properties to add to this current class
     *
     */    
    public static void setProperties(Properties ps) {
        p = ps;
    }

    /** Returns the property's value for the given name.
     * Returns null if properties object is not set, or name not found
     * @param name the name of the property whose value is to get.
     * @return the value for the given property's name, or null if the name is not found.
     */
    public static String getProperty(String name) {
        if (p == null) {
            // tries to load the property file
            p = new Properties();
            try {
                p.load(DataManager.class.getClassLoader().getResourceAsStream("org/geogurus/gas/resources/geonline.properties"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return p.getProperty(name);
    }

}