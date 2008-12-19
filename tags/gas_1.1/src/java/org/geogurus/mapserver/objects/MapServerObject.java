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
 * MapServerObject.java
 *
 * Created on 25 mars 2002, 10:46
 */

package org.geogurus.mapserver.objects;

import java.util.logging.Logger;

/**
 * This abstract class contains methods to implements
 * by a MapFile Object.
 * It also provides a basic errorMessage string.
 * Each derived object is in charge to provide a consistent error Message
 * according to context.
 * Callers can get this error message in case of error during mapserver object
 * loading or saving
 *
 * @author  Bastien VIALADE
 * Modified by NRI, 11 june 2002 to change it as an abstract class
 */
public abstract class MapServerObject implements java.io.Serializable {
    /** object's logger */
    protected transient Logger logger;
    /** 
     * the error message generated during file io on a MapServer object (exception for instance);
     * set static to share the same variable across objects hierarchy
     */
    protected static String errorMessage;
    
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */    
    public abstract boolean load(java.io.BufferedReader br);
    /** Saves data to file
     * using Object parameters with mapFile format.
     * @param bw BufferWriter containing file data to write
     * in linked file.
     * @return true is mapping done correctly
     */    
    public abstract boolean saveAsMapFile(java.io.BufferedWriter bw) ;
     /** Returns a string representation of the MapServer Object
      * @return a string representation of the MapServer Object.
      */     
    public abstract String toString() ;
    
    public static String getErrorMessage() { return errorMessage; }
    
    /**
     * Appends the given string to the errorMessage
     * @param error the error to append to the existing error message
     * @return the appended errorMessage
     */
    public static String setErrorMessage(String error) {
        if (error == null) {
            return null;
        }
        if (errorMessage == null) {
            errorMessage = "";
        }
        errorMessage = error + System.getProperty("line.separator") + errorMessage;
        return errorMessage;
    }
    
}

