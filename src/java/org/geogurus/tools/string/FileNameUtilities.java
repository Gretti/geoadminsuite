/*
 * FileNameUtilities.java
 *
 * Created on October 11, 2002, 1:45 PM
 */

package org.geogurus.tools.string;

import java.util.*;


/**
 *
 * @author  jrom
 *
 */
public class FileNameUtilities {
    
   
    /** 
     *
     * Return the file extension
     *
     */
    public static String getExtension(String fn) {
        
        String res = "";
        int pt = fn.lastIndexOf(".");
        try {
            res = fn.substring(pt+1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        
        return res;
        
    }    
    
    
    /** 
     *
     * Return the file name without extension
     *
     */
    public static String getFileNameWithoutExtension(String fn) {
        
        String res = "";
        int pt = fn.lastIndexOf(".");
        try {
            res = fn.substring(0, pt);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        
        return res;
        
    }    
    
    /** 
     *
     * Return the file name without the path
     *
     */
    public static String getFileNameWithoutPath(String fn) {
        
        String res = fn;
        int pt = fn.lastIndexOf(System.getProperty("file.separator"));
        
        if (pt == -1) {
            return fn;
        }
        else {
            try {
                res = fn.substring(pt+1);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
        
        return res;
    }
    
}

