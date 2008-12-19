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
 * FileNameUtilities.java
 *
 * Created on October 11, 2002, 1:45 PM
 */

package org.geogurus.tools.string;



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

