/*
 *
 * Utility class for Kaboum project.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * Copyright (C) 2000-2003 Jerome Gasperi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *

 *
 */
package org.kaboum.util;

import java.awt.Color;
import java.awt.Font;
import java.util.StringTokenizer;
import java.net.*;

/**
 *
 * Static methods for Kaboum applet
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumUtil {
    /** the Kaboum applet codebase, allowing to build absolute URLs from relative one */
    public static URL appletCodeBase = null;
    
    /** CONSTANTS */
    public static final int INTERSECTION_WITH_UNION = 0;
    public static final int INTERSECTION_WITHOUT_UNION = 1;
    
    /** Debug mode */
    private static boolean K_DEBUG_MODE = false;
    
    /** sets the applet's codebase URL */
    public static void setAppletCodeBase(URL codeBase) {
        appletCodeBase = codeBase;
    }
    
    /**
     * Set the debug mode
     */
    public static void setDebugMode(boolean b) {
        KaboumUtil.K_DEBUG_MODE = b;
    }
    
    /**
     * Get the debug mode
     */
    public static boolean getDebugMode() {
        return K_DEBUG_MODE;
    }
    
    
    /**
     *
     * Debug method
     *
     */
    public static void debug(String log) {
        if (K_DEBUG_MODE && log.indexOf("COORDINATE_STRING") == -1) { System.out.println("JAVA: "+log); }
    }
    
    
    /**
     *
     * Eval a color value from a r,g,b triplet
     * or from a pre-defined color
     * (From mapplet code source by Stephen Lime)
     *
     * @param s Input string
     * @param Color Default color to return
     *
     */
    public static Color getColorParameter(String s, Color color) {
        
        if (s == null) {
            return color;
        }
        
        StringTokenizer st;
        int r, g, b;
        
        //
        // Check if a pre-defined color is specified
        // Note that TRANSPARENT is a special keyword
        // that return null
        //
        if (s.equalsIgnoreCase("TRANSPARENT"))
            return null;
        if (s.equalsIgnoreCase("black"))
            return(Color.black);
        if (s.equalsIgnoreCase("blue"))
            return(Color.blue);
        if (s.equalsIgnoreCase("cyan"))
            return(Color.cyan);
        if (s.equalsIgnoreCase("darkGray"))
            return(Color.darkGray);
        if (s.equalsIgnoreCase("gray"))
            return(Color.gray);
        if (s.equalsIgnoreCase("green"))
            return(Color.green);
        if (s.equalsIgnoreCase("lightGray"))
            return(Color.lightGray);
        if (s.equalsIgnoreCase("magenta"))
            return(Color.magenta);
        if (s.equalsIgnoreCase("orange"))
            return(Color.orange);
        if (s.equalsIgnoreCase("pink"))
            return(Color.pink);
        if (s.equalsIgnoreCase("red"))
            return(Color.red);
        if (s.equalsIgnoreCase("white"))
            return(Color.white);
        if (s.equalsIgnoreCase("yellow"))
            return(Color.yellow);
        
        // nope, must be an RGB triplet
        st = new StringTokenizer(s, ",");
        
        if (st.countTokens() != 3) {
            return null;
        }
        
        r = Integer.parseInt(st.nextToken());
        g = Integer.parseInt(st.nextToken());
        b = Integer.parseInt(st.nextToken());
        
        return(new Color(r,g,b));
    }
    
    
    /**
     *
     * Eval the font name
     *
     * @param s        Input font name
     *
     */
    public static String getFontName(String s) {
        
        String textName = "Courier";
        
        if (s == null) {
            return textName;
        }
        
        if (s.equalsIgnoreCase("Courier")) {
            textName = "Courier";
        } else if (s.equalsIgnoreCase("Dialog")) {
            textName = "Dialog";
        }
        
        else if (s.equalsIgnoreCase("Helvetica")) {
            textName = "Helvetica";
        } else if (s.equalsIgnoreCase("Symbol")) {
            textName = "Symbol";
        } else if (s.equalsIgnoreCase("TimesRoman")) {
            textName = "TimesRoman";
        }
        
        return textName;
    }
    
    
    /**
     *
     * Return the font style corresponding integer
     *
     */
    public static int getFontStyle(String s) {
        
        int textStyle = Font.PLAIN;
        
        if (s == null) {
            return textStyle;
        }
        
        
        if (s.equalsIgnoreCase("plain")) {
            textStyle = Font.PLAIN;
        } else if (s.equalsIgnoreCase("bold")) {
            textStyle = Font.BOLD;
        } else if (s.equalsIgnoreCase("italic")) {
            textStyle = Font.ITALIC;
        } else if (s.equalsIgnoreCase("boldItalic")) {
            textStyle = Font.BOLD + Font.ITALIC;
        }
        
        return textStyle;
        
    }
    
    
    /**
     *
     * Convert numerical string into integer
     *
     * @param s Input string
     * @param d Default value
     *
     */
    public static int stoi(String s, int d) {
        
        if (s == null) {
            return d;
        }
        
        if (s.equalsIgnoreCase("INTERSECTION_WITH_UNION")) {
            return INTERSECTION_WITH_UNION;
        } else if (s.equalsIgnoreCase("INTERSECTION_WITHOUT_UNION")) {
            return INTERSECTION_WITHOUT_UNION;
        }
        
        int n = 0;
        
        try {
            n = new Integer(s.trim()).intValue();
        } catch(NumberFormatException e) {}
        return(n);
        
    }
    
    
    /**
     *
     * Convert numerical string into integer
     *
     * @param s Input string
     *
     */
    public static int stoi(String s) {
        return stoi(s, -1);
    }
    
    
    /**
     *
     * Convert numerical string into float
     *
     * @param s Input string
     * @param d Default value
     *
     */
    public static float stof(String s, float d) {
        
        if (s == null) {
            return d;
        }
        
        float n = 0;
        
        try {
            n = new Float(s.trim()).floatValue();
        } catch(NumberFormatException e) {}
        return(n);
        
    }
    
    
    /**
     *
     * Convert numerical string into float
     *
     *
     * @param s Input string
     *
     */
    public static float stof(String s) {
        return stof(s, -1);
    }
    
    
    /**
     *
     * Convert numerical string into double
     *
     * @param s Input string
     * @param d Default value
     *
     */
    public static double stod(String s, double d) {
        
        if (s == null) {
            return d;
        }
        
        double n = 0;
        
        try {
            n = new Double(s.trim()).doubleValue();
        } catch(NumberFormatException e) {}
        return(n);
        
    }
    
    
    /**
     *
     * Convert numerical string into double
     *
     * @param s Input string
     *
     */
    public static double stod(String s) {
        return stod(s, -1.0);
    }
    
    
    /**
     *
     * Convert boolean string into boolean
     *
     * @param s Input string
     *
     */
    public static boolean stob(String s) {
        return stob(s, false);
    }
    
    
    /**
     *
     * Convert boolean string into boolean
     *
     * @param s Input string
     * @param d Default value
     *
     */
    public static boolean stob(String s, boolean d) {
        
        if (s == null) {
            return d;
        }
        
        if (s.equalsIgnoreCase("FALSE")) {
            return false;
        }
        
        return true;
    }
    
    /**
     *
     * Convert string into URL. <p>
     * If an exception occured during URL conversion, try to build a relative URL from the applet's context
     *
     * @param strName Input string
     *
     */
    public static URL toURL(String strName) {
        
        URL strURL = null;
        
        if (strName == null) { return null; }
        
        try {
            strURL = new URL(strName);
        } catch(MalformedURLException e) {
            try {
                strURL = new URL(appletCodeBase, strName);
            } catch(MalformedURLException mue) {}
        }
        return strURL;
    }
    
    /** 
     * returns a string containing the current JVM memory usage, in Kb
     */
    public static String getMemoryUsage() {
        StringBuffer buf = new StringBuffer();
        Runtime r = Runtime.getRuntime();
        
        buf.append("maxMemory: ");
        buf.append(r.maxMemory() / 1024);
        buf.append(" totalMemory: ");
        buf.append(r.totalMemory() / 1024);
        buf.append(" freeMemory: ");
        buf.append(r.freeMemory() / 1024);
        return buf.toString();
        
    }
}
