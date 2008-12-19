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
 * RGB.java
 *
 * Created on 20 mars 2002, 10:11
 */

package org.geogurus.mapserver.objects;

import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * This class defines a simple RGB object
 *
 * @author  Bastien VIALADE
 */
public class RGB  implements java.io.Serializable {
    private  transient Logger logger;
    private int red;
    private int green;
    private int blue;
    
    
    /** Empty constructor */
    public  RGB() {
        this(0,0,0);
    }
    
    /** Creates a new instance of RGB */
    public RGB(int red_, int green_, int blue_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        red = red_;
        green = green_;
        blue = blue_;
    }
    /**
     * Creates a new instance of RGB with the given string, containing space-separated RGB values,
     * as int. Should only be used by servlets receiving this string as a JS-controled parameter,
     * for example generated by the colorChooser.
     */
    public RGB(String col) {
        this.logger = Logger.getLogger(this.getClass().getName());
        java.util.StringTokenizer tok = new java.util.StringTokenizer(col);
        try {
            red = new Integer(tok.nextToken()).intValue();
            green = new Integer(tok.nextToken()).intValue();
            blue = new Integer(tok.nextToken()).intValue();
        } catch (NumberFormatException nfe) {
            // default color
            red =0;green=0;blue=0;
        }
    }
    
    /** Get methods to get an RGB tab or individual color value*/
    public int[] getRGB() {
        int[] rgb = {red,green,blue};
        return rgb;
    }
    
    public int getRed()     {return red;}
    public int getGreen()   {return green;}
    public int getBlue()    {return blue;}
    
    /** Set methods to changes RGB values*/
    public void setRed(int red_)        {red = red_;}
    public void setGreen(int green_)    {green = green_;}
    public void setBlue(int blue_)      {blue = blue_;}
    
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */    
    public synchronized boolean load(String[] tokens) {
        if (tokens.length<4) return false;
        try {
            red     = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
            green   = Integer.parseInt(tokens[2]);
            blue    = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[3]));
        } catch (NumberFormatException nfe) {
            System.err.println("Can't cast color: "+nfe);
            return false;
        }
        return true;
    }
    
    /**  Saves RGB object to the given BufferedWriter
     * with MapFile style.
     */
   public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
       boolean result = true;
        try {
            bw.write(red+" "+green+" "+blue+"\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
       return result;
   }
    
     /** Returns a string representation of the RGB Object
      * @return a string representation of the RGB Object.
      */     
   @Override
    public String toString() {
        /*
        return "RGB OBJECT: "+
               "\n  . Red   = "+red+
               "\n  . Green = "+green+
               "\n  . Blue  = "+blue+"\n";
         */
        return "" + red + " " + green + " " + blue;
    }
    
     /** Returns a string representation of the RGB Object as a comma-separated 
      * list of r, g, b values
      * @return a string representation of the RGB Object.
      */     
    public String toCWString() {
        return "" + red + ", " + green + ", " + blue;
    }
}

