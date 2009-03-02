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

package org.geogurus.mapserver;

import java.io.IOException;
import java.util.logging.Logger;

import org.geogurus.mapserver.objects.Map;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * A MapFile is a File which can be mapped
 * and written using a given Map Object
 *
 * @author  Bastien VIALADE
 */
public class MapFile extends java.io.File implements java.io.Serializable {
    /** this class' logger */
    private transient Logger logger;
    /** error message generated by loaded Map object */
    private String mapErrorMessage;

    /** should underlying mapfile ignore linked files (font and symbol set) when loading
     * and saving ?
     * Used as a convenient shortcut for the same property
     */
    private boolean ignoreLinkedFiles;

    /** Creates a new MapFile given its path
     * @param pathName Path where to find linked FILE
     */
    public MapFile(String pathName) {
        super(pathName);
        logger = Logger.getLogger(this.getClass().getName());
        ignoreLinkedFiles = false;
    }
    
    /** This methods changes a file in an editable string format
     * @return String to display, or empty string if an IO exception occured during 
     * file processing
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
            logger.warning("Mapfile.save. Exception: " +  e.getMessage());
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

            if (line == null){
                // empty file
                setMapErrorMessage("empty input file. cannot load: " + this.getAbsolutePath());
                throw new IOException(this.mapErrorMessage);
            }
            // Looking for the first util line
            while ((line.trim().equals(""))||(line.trim().startsWith("#"))) line = br.readLine();
            // Gets array of words of the line
            String[] tokens = ConversionUtilities.tokenize(line.trim());
            // MapFile always starts with MAP keyword otherwise ERROR!
            if (tokens.length>1) {
                setMapErrorMessage("invalid/not understood mapfile value: " + line);
                return null;
            }
            if (tokens[0].equalsIgnoreCase("MAP")) {
                map = new Map(this);
                map.setIgnoreLinkedFiles(this.getIgnoreLinkedFiles());
                done = map.load(br);
            } else {
                setMapErrorMessage("mapfile does not start with MAP (read line: " + line + ")");
                return null;
            }
            br.close();
        } catch (Exception e) {
            logger.warning("MapFile.load: Exception: " + e.getMessage());
            setMapErrorMessage(e.getMessage());
            e.printStackTrace();
            return null;
        }
        if (!done) {
            setMapErrorMessage(map.getErrorMessage());
            return null;
        }
        else {
            // sets some variables
            //map.setMapFile(this);
            return map;
        }
    }

    public String getMapErrorMessage() {
        return mapErrorMessage;
    }

    public void setMapErrorMessage(String mapErrorMessage) {
        this.mapErrorMessage = mapErrorMessage;
    }

    public void setIgnoreLinkedFiles(boolean ignore) {
        this.ignoreLinkedFiles = ignore;
    }

    public boolean getIgnoreLinkedFiles() {
        return this.ignoreLinkedFiles;
    }
}

