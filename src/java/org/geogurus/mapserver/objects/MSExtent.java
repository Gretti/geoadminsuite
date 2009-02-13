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
 * MSExtent.java
 *
 * Created on 20 mars 2002, 09:53
 */

package org.geogurus.mapserver.objects;

import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;
/**
 * This class defines a simple Extent object
 *
 * @author  Bastien VIALADE
 */
public class MSExtent implements java.io.Serializable {
    private transient Logger logger;
    
    private double minx;
    private double miny;
    private double maxx;
    private double maxy;
    
    /** Empty constructor */
    public MSExtent() {
        this(0,0,0,0);
    }
    
    /** Creates a new instance of Extent */
    public MSExtent(double minx_, double miny_, double maxx_, double maxy_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        minx = minx_;
        miny = miny_;
        maxx = maxx_;
        maxy = maxy_;
    }
    
    /** Sets extent value */
    public void setMinx(double minx_) {minx = minx_;}
    public void setMiny(double miny_) {miny = miny_;}
    public void setMaxx(double maxx_) {maxx = maxx_;}
    public void setMaxy(double maxy_) {maxy = maxy_;}
    /** Gets each extent value*/
    public double getMinx(){return minx;}
    public double getMiny(){return miny;}
    public double getMaxx(){return maxx;}
    public double getMaxy(){return maxy;}
    
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public synchronized boolean load(String[] tokens) {
        try {
            if (tokens.length<5) return false;
            minx = Double.parseDouble(ConversionUtilities.removeDoubleQuotes(tokens[1]));
            miny = Double.parseDouble(tokens[2]);
            maxx = Double.parseDouble(tokens[3]);
            maxy = Double.parseDouble(ConversionUtilities.removeDoubleQuotes(tokens[4]));
        } catch (NumberFormatException ex) {
            logger.warning("Error while loading extent: "+ex);
            return false;
        }
        return true;
    }
    
    /**  Saves EXTENT object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        return saveAsMapFile(bw, "");
    }
    /**  Saves EXTENT object to the given BufferedWriter
     * with MapFile style, adding the given string indentation before writing
     * (tab or CR for instance)
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw, String ident) {
        boolean result = true;
        try {
            bw.write(ident + "\t extent "+minx+" "+miny+" "+maxx+" "+maxy+"\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }
    
    
    /** Returns a string representation of the EXTENT Object
     * @return a string representation of the EXTENT Object.
     */
    public String toString() {
        /*
        return "EXTENT OBJECT: "+
        "\n    . minx = "+minx+
        "\n    . miny = "+miny+
        "\n    . maxx = "+maxx+
        "\n    . maxy = "+maxy+"\n";
         */
        return "" + minx + " " + miny + " " + maxx + " " + maxy;
    }
    
    public String toKaboumString() {
        /*
        return "EXTENT OBJECT: "+
        "\n    . minx = "+minx+
        "\n    . miny = "+miny+
        "\n    . maxx = "+maxx+
        "\n    . maxy = "+maxy+"\n";
         */
        return "" + minx + "," + miny + "," + maxx + "," + maxy;
    }
    
}



