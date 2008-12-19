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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.mapserver.objects;

import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 *
 * This obect is used to define a Grid object<br/>
 * The GRID object defines a map graticule as a LAYER. 
 * Starts with the keyword GRID and terminates with the keyword END.
 *
 * @author  Bastien VIALADE
 */
public class Grid extends MapServerObject implements java.io.Serializable {
    // constants for LabelFormat string
    public static final byte DD = 1;
    public static final byte DDMM = 2;
    public static final byte DDMMSS = 3;
    /**
     * [From mapfile doc]
     * Format of the label. 
     * "DD" for degrees, "DDMM" for degrees minutes, and "DDMMSS" for degrees, minutes, seconds. 
     * A C-style formatting string is also allowed, such as "%g°" to show decimal degrees with a degree symbol. 
     * The default is decimal display of whatever SRS you're rendering the GRID with.
     */
    private String labelFormat;
    /**
     * [From mapfile doc]
     * The minimum number of arcs to draw. Increase this parameter to get more lines. Optional.
     */
    private double minArcs;
    /**
     * [From mapfile doc]
     * The maximum number of arcs to draw. Decrease this parameter to get fewer lines. Optional.
     */
    private double maxArcs;
    /**
     * [From mapfile doc]
     * The minimum number of intervals to try to use. The distance between the grid lines, in the units of the grid's coordinate system. Optional.
     */
    private double minInterval;
    /**
     * [From mapfile doc]
     * The maximum number of intervals to try to use. The distance between the grid lines, in the units of the grid's coordinate system. Optional.
     */
    private double maxInterval;
    /**
     * [From mapfile doc]
     * The minimum number of segments to use when rendering an arc. If the lines should be very curved, use this to smooth the lines by adding more segments. Optional.
     */
    private double minSubdivide;
    /**
     * [From mapfile doc]
     * The maximum number of segments to use when rendering an arc. If the graticule should be very straight, use this to minimize the number of points for faster rendering. Optional, default 256.    
     */
    private double maxSubdivide;

    /** 
     * default ctor, initialize object with its default values (see MapFile reference)
     */
    public Grid() {
        this(getLabelFormat(Grid.DD), Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE,
                Double.MIN_VALUE, Double.MIN_VALUE, 256);
    }

    /** 
     * initialize object with the given values
     */
    public Grid(String labelFormat_, double minArcs_, double maxArcs_,
            double minInterval_, double maxInterval_, double minSubdivide_, double maxSubdivide_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.labelFormat = labelFormat_;
        this.minArcs = minArcs_;
        this.maxArcs = maxArcs_;
        this.minInterval = minInterval_;
        this.maxInterval = maxInterval_;
        this.minSubdivide = minSubdivide_;
        this.maxSubdivide = maxSubdivide_;
    }

    /** 
     * gets the labelFormat string according to given byte constant
     * see class constants
     * @param format
     * @return the labelFormat (see mapFile doc) or "" if format is unsupported
     */
    public static String getLabelFormat(byte format) {
        switch (format) {
            case Grid.DD:
                return "DD";
            case Grid.DDMM:
                return "DDMM";
            case Grid.DDMMSS:
                return "DDMMSS";
            default:
                return "";
        }
    }

    /** 
     * Loads data from file
     * and fills Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
        try {
            String[] tokens;
            String line;

            while ((line = br.readLine()) != null) {

                // Looking for the first util line
                while ((line.trim().length() == 0) || (line.trim().startsWith("#")) || (line.trim().startsWith("%"))) {
                    line = br.readLine();
                }

                tokens = ConversionUtilities.tokenize(line.trim());

                if (tokens[0].equalsIgnoreCase("LABELFORMAT")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Grid.load: Invalid syntax for LABELFORMAT: " + line);
                        return false;
                    }
                    this.labelFormat = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("MINARCS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Grid.load: Invalid syntax for MINARCS: " + line);
                        return false;
                    }
                    this.minArcs = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MAXARCS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Grid.load: Invalid syntax for LABELMAXARCSFORMAT: " + line);
                        return false;
                    }
                    this.maxArcs = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MININTERVAL")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Grid.load: Invalid syntax for MININTERVAL: " + line);
                        return false;
                    }
                    this.minInterval = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MAXINTERVAL")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Grid.load: Invalid syntax for MAXINTERVAL: " + line);
                        return false;
                    }
                    this.maxInterval = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MINSUBDIVIDE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Grid.load: Invalid syntax for MINSUBDIVIDE: " + line);
                        return false;
                    }
                    this.minSubdivide = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MAXSUBDIVIDE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Grid.load: Invalid syntax for MAXSUBDIVIDE: " + line);
                        return false;
                    }
                    this.maxSubdivide = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("Grid.load: unknown token: " + line);
                    return false;
                }
            }
        } catch (Exception e) { // Bad coding, but works...
            logger.warning("MapFile.Grid.load(). Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /** Saves data to file
     * using Object parameters with mapFile format.
     * @param bw BufferWriter containing file data to write
     * in linked file.
     * @return true is mapping done correctly
     */
    public boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t grid\n");
            if (labelFormat != null && labelFormat.length() > 0) {
                bw.write("\t\t labelformat " + labelFormat + "\n");
            }
            if (minArcs != Double.MIN_VALUE) {
                bw.write("\t\t minarcs " + minArcs + "\n");
            }
            if (maxArcs != Double.MIN_VALUE) {
                bw.write("\t\t maxarcs " + maxArcs + "\n");
            }
            if (minInterval != Double.MIN_VALUE) {
                bw.write("\t\t mininterval " + minInterval + "\n");
            }
            if (minSubdivide != Double.MIN_VALUE) {
                bw.write("\t\t maxinterval " + minSubdivide + "\n");
            }
            if (maxInterval != Double.MIN_VALUE) {
                bw.write("\t\t maxinterval " + maxInterval + "\n");
            }
            if (maxSubdivide != Double.MIN_VALUE) {
                bw.write("\t\t maxinterval " + maxSubdivide + "\n");
            }
            bw.write("\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    /** Returns a string representation of the MapServer Object
     * @return a string representation of the MapServer Object.
     */
    public String toString() {
        return "";
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    public void setLabelFormat(String labelFormat) {
        this.labelFormat = labelFormat;
    }

    public double getMinArcs() {
        return minArcs;
    }

    public void setMinArcs(double MinArcs) {
        this.minArcs = MinArcs;
    }

    public double getMaxArcs() {
        return maxArcs;
    }

    public void setMaxArcs(double MaxArcs) {
        this.maxArcs = MaxArcs;
    }

    public double getMinInterval() {
        return minInterval;
    }

    public void setMinInterval(double MinInterval) {
        this.minInterval = MinInterval;
    }

    public double getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(double MaxInterval) {
        this.maxInterval = MaxInterval;
    }

    public double getMinSubdivide() {
        return minSubdivide;
    }

    public void setMinSubdivide(double MinSubdivide) {
        this.minSubdivide = MinSubdivide;
    }

    public double getMaxSubdivide() {
        return maxSubdivide;
    }

    public void setMaxSubdivide(double MaxSubdivide) {
        this.maxSubdivide = MaxSubdivide;
    }
}
