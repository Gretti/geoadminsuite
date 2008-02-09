/*
 * MapFile.java
 *
 * Created on 26 mars 2002, 10:59
 */

package org.geogurus.mapserver;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * A MapFile is a File witch can be mapped
 * and been written using a given Map Object
 *
 * Copyright:    Copyright (c) 2001
 * Company: SCOT
 * @author  Bastien VIALADE
 */
public class MapFile extends java.io.File implements java.io.Serializable {
    

    /** Creates a new MapFile given its path
     * @param pathName Path where to find linked FILE
     */
    public MapFile(String pathName) {
        super(pathName);
    }
    
    /** This methods changes a file in an editable string format
     * @return String to display
     */
    public String getContentsOfFile() {
        char[] buff = new char[10000];
        if (this.getName().equals("")) {
            return "";
        }
        String s = new String();
        java.io.FileReader reader = null;
        try {
            reader = new java.io.FileReader(this);
            int nch = reader.read(buff, 0, buff.length);
            if (nch != -1) {
                s = new String(buff, 0, nch);
                reader.close();
            }
        }
        catch (java.io.IOException iox) {
            s = "";
        }
        return s;
    }
    
    /** Saves MapServer objects in a MapFile
     * @param map Entry object. Given this object all other can be accessed
     * @return True is save is OK
     */
    public boolean save(Map map) {
        boolean result = false;
        try {
            java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(this)) ;
            result = map.saveAsMapFile(bw);
            bw.close() ;
        } catch (Exception e) {
            System.out.println("Mapfile.save. Exception: " +  e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }
    
    
    /** Parses the current MapFile, and load data in a Map object
     * @return Map object with all MapFile data.
     */
    public Map load() {
        boolean done = false;
        Map map;
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(this));
            String line = br.readLine();
            // Looking for the first util line
            while ((line.trim().equals(""))||(line.trim().startsWith("#"))) line = br.readLine();
            // Gets array of words of the line
            String[] tokens = ConversionUtilities.tokenize(line.trim());
            // MapFile always starts with MAP keyword otherwise ERROR!
            if (tokens.length>1) return null;
            if (tokens[0].equalsIgnoreCase("MAP")) {
                map = new Map(this);
                done = map.load(br);
            } else {
                return null;
            }
            br.close();
        } catch (Exception e) {
            System.out.println("MapFile.load: Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        if (!done) return null;
        else {
            // sets some variables
            //map.setMapFile(this);
            return map;
        }
    }
    
}

