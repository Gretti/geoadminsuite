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
 * LogEngine.java
 *
 * Created on March 6, 2002, 2:05 PM
 */

package org.geogurus.tools;

/** A very simple log class.
 * Used to log to system.err if debug mode was set to true
 * @author nri
 */
 public class LogEngine {
     /** boolean value telling if debug should be done or not.
      */     
    protected static boolean debug = true;
     /** boolean value telling if debug should be done in the system.err (true)
      * or system.out (false) stream.
      */     
    protected static boolean debugToErr = false;

    /** Allows to set the debug mode for this class
     * @param d true to debug to system.err, false to do nothing
     */    
    public static void setDebugMode(boolean d) {
        debug = d;
    }
    
    /** Allows to set the debug stream for this class
     * @param err true to debug to system.err, false to debug in system.err
     * (if debug mode is true, of course)
     */    
    public static void setDebugToErr(boolean err) {
        debugToErr = err;
    }
    
    public static boolean getDebugMode() {return debug;}
    /** Logs the given string to system.err if the debug mode of this class was set to true,
     * by prior call to <CODE>setDebugMode()</CODE>
     * @param msg the message to log to System.err
     */    
    public static void log(String msg) {
        if (debug) {
            if (debugToErr) {
                System.err.println(msg);
            } else {
                System.out.println(msg);
            }
        }
    }
}