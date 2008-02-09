/*
 * MapServerObject.java
 *
 * Created on 25 mars 2002, 10:46
 */

package org.geogurus.mapserver.objects;

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
    protected String errorMessage;
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
    
    public String getErrorMessage() { return errorMessage; }
    
}

