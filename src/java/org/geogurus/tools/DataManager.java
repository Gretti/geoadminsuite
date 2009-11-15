/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * DataManager.java
 *
 * Created on March 6, 2002, 2:08 PM
 */
package org.geogurus.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

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
    private static boolean msVersionGot = false;
    public static final String MS_VERSION_TAG = "MapServer version";
    public static final String MAPSERVERVERSION = "MAPSERVERVERSION";
    public static final String MAPSERVERINPUTS = "MAPSERVERINPUTS";
    public static final String MAPSERVEROUTPUTS = "MAPSERVEROUTPUTS";
    public static final String MAPSERVERSUPPORTS = "MAPSERVERSUPPORTS";
    public static final String MAPSERVERURL = "MAPSERVERURL";
    public static final String PUBLISH_MAPSERVERURL = "PUBLISH_MAPSERVERURL";
    //private static final String MS_VERSION_PATTERN = " 5.0.2 ";
    /** sets the given properties to this class
     * @param ps the properties to add to this current class
     *
     */
    public static void setProperties(Properties ps) {
        p = ps;
    }

    public static Properties getProperties() {
        return p;
    }

    /** sets the given property
     * @param ps the properties to add to this current class
     *
     */
    public static void setProperty(String key, String value) {
        if (p != null) {
            p.setProperty(key, value);
        }
    }

    /** Returns the property's value for the given name.
     * Returns null if properties object is not set, or name not found
     * @param name the name of the property whose value is to get.
     * @return the value for the given property's name, or null if the name is not found.
     */
    public static String getProperty(String name) {
        //properties are now loaded once, with GOFileCleaner servlet initialization
        /*
        if (p == null) {
            // tries to load the property file
            p = new Properties();
            try {
                p.load(DataManager.class.getClassLoader().getResourceAsStream("org/geogurus/gas/resources/gas.properties"));
                // deals with mapserver URL
                // try to get MS version from MS error page
                if (!DataManager.msVersionGot) {
                    Hashtable<String, String> msInfo = DataManager.getMSVersion();
                    if (msInfo != null) {
                        p.setProperty(MAPSERVERVERSION, msInfo.get("MSVERSION"));
                        p.setProperty(MAPSERVERINPUTS, msInfo.get("INPUTS"));
                        p.setProperty(MAPSERVEROUTPUTS, msInfo.get("OUTPUTS"));
                        p.setProperty(MAPSERVERSUPPORTS, msInfo.get("SUPPORTS"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */
        if (p == null) {
            Logger.getLogger(DataManager.class.getName()).warning("Null properties object. Configuration not loaded");
        }
        return p.getProperty(name);
    }
    
    public static Integer getMSVersionMajor() {
        //eg returns the "5" of version 5.2.0
        return Integer.parseInt(getProperty("MAPSERVERVERSION").split("\\.")[0]);
    }
    
    public static Integer getMSVersionMinor() {
        String minor = getProperty("MAPSERVERVERSION").split("\\.")[1];
        Integer mv;
        try {
            //eg returns the "2" of version 5.2.0
            mv = Integer.parseInt(minor);
        } catch(NumberFormatException e) {
            //eg return the "3" of version 5.3-dev
            mv = Integer.parseInt(minor.split("-")[0]);
        }
        return mv;
    }
    
    

    /**
     * Returns the MapServer version by querying mapserver URL as specified in this dataManager
     * property value: MAPSERVERURL.
     * Thus, this method must be called AFTER the gas.properties file is loaded, which
     * occurs when requesting one property with getProperty() and internal properties object is null.<br/>
     * This method is called only once in the GAS lifecycle.
     * @return
     */
    public static Hashtable<String, String> getMSVersion() {
        if (p == null || p.getProperty("MAPSERVERURL") == null) {
            return null;
        }
        Hashtable<String, String> msInfo = new Hashtable<String, String>();
            //insert default values into the hash, to avoid null values
            msInfo.put("MSVERSION", "");
            msInfo.put("OUTPUTS", "");
            msInfo.put("INPUTS", "");
            msInfo.put("SUPPORTS", "");

        StringBuilder msurl = new StringBuilder(p.getProperty("MAPSERVERURL"));
        
        try {
            msurl.append("?mode=amodethatdoesntexist");

            URL u = new URL(msurl.toString());
            URLConnection yc = u.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    yc.getInputStream()));
            String inputLine = null;
            String strOutput = "";
            String strInput = "";
            String strSupports = "";
            /*
             * Getting MS version, outputs, supports and inputs consist in reading
             * the response error of MS on a mode that doesn't exists in an html 
             * comment between head and body looking like this :
             * <!-- MapServer version 5.1-dev OUTPUT=GIF OUTPUT=PNG OUTPUT=JPEG OUTPUT=WBMP OUTPUT=SWF OUTPUT=SVG SUPPORTS=PROJ SUPPORTS=AGG SUPPORTS=FREETYPE SUPPORTS=ICONV SUPPORTS=WMS_SERVER SUPPORTS=WMS_CLIENT SUPPORTS=WFS_SERVER SUPPORTS=FASTCGI SUPPORTS=THREADS SUPPORTS=GEOS SUPPORTS=RGBA_PNG INPUT=EPPL7 INPUT=POSTGIS INPUT=OGR INPUT=GDAL INPUT=SHAPEFILE -->
             */
            while ((inputLine = in.readLine()) != null) {
                Logger.getLogger(DataManager.class.getName()).fine("reading line from MS error page: " + inputLine);
                int index = inputLine.indexOf(MS_VERSION_TAG);
                if (index >= 0) {
                    StringTokenizer tok = new StringTokenizer(inputLine, " ");
                    int i = 0;
                    while (tok.hasMoreTokens()) {
                        String strtok = tok.nextToken();
                        if (i == 3) {
                            msInfo.put("MSVERSION", strtok);
                        } else if (i > 3 && strtok.indexOf("=") > -1) {
                            StringTokenizer tokEq = new StringTokenizer(strtok, "=");
                            String type = tokEq.nextToken();
                            String val = tokEq.nextToken();
                            if (type.equalsIgnoreCase("OUTPUT")) {
                                strOutput += val + "|";
                            } else if (type.equalsIgnoreCase("INPUT")) {
                                strInput += val + "|";
                            } else if (type.equalsIgnoreCase("SUPPORTS")) {
                                strSupports += val + "|";
                            }
                        }
                        i++;
                    }
                    //a version is there
                    //res = inputLine.substring(index + MS_VERSION_TAG.length(), index + MS_VERSION_TAG.length() + MS_VERSION_PATTERN.length()).trim();
                    break;
                }
            }
            in.close();
            msInfo.put("INPUTS", strInput);
            msInfo.put("OUTPUTS", strOutput);
            msInfo.put("SUPPORTS", strSupports);
        } catch (IOException e) {
            Logger.getLogger(DataManager.class.getName()).warning(e.getMessage());
        }
        DataManager.msVersionGot = true;
        Logger.getLogger(DataManager.class.getName()).info("MapServer sniffed from url: " + msurl +
                "\nversion : " + msInfo.get("MSVERSION") +
                "\ninputs : " + msInfo.get("INPUTS") +
                "\noutputs : " + msInfo.get("OUTPUTS") +
                "\nsupports : " + msInfo.get("SUPPORTS"));
        return msInfo;
    }

    /**
     * Gets the mapserver URL for publication
     * If PUBLISH_MAPSERVERURL is defined in the gas.properties, this value will be used,
     * else, the current MAPSERVERURL is used
     * @return the mapserver URL for publication
     */
    public static String getPublishMapserverUrl() {
        return p.getProperty("PUBLISH_MAPSERVERURL") == null ?
            p.getProperty("MAPSERVERURL") :
            p.getProperty("PUBLISH_MAPSERVERURL");
    }
}
