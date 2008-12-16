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
 * ClassStyle.java
 *
 * Created on 20 mars 2002, 11:07
 */
package org.geogurus.mapserver.objects;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * This obect is used to define a label, which is in turn usually
 * used to annotate a feature with a piece of text.
 * Labels can however also be used as symbols through the use of various TrueType fonts.
 * Note: does not check for validity of PRIORITY syntax (int or [item].
 * @author  Bastien VIALADE
 */
public class ClassStyle extends MapServerObject implements java.io.Serializable {
    /** Angle, given in degrees, to draw the line work. Default is 0. 
     * or symbols of Type HATCH, this is the angle of the hatched lines. 
     * For its use with hatched lines, see Example#8 in the SYMBOL examples.
     */
    private Double angle;
    /**
     * Attribute/field that stores the angle to be used in rendering. 
     * Angle is given in degrees with 0 meaning no rotation.
     */
    private String angleItem;
    /** 
     * Should TrueType fonts and Cartoline symbols be antialiased.
     */
    private Boolean antialias;
    /** Color to use for non-transparent symbols. */
    private RGB backgroundColor;
    /** Color to draw a background rectangle /i.e. billboard) shadow */
    /* Color to use for drawing features. */
    private RGB color;
    /** Maximum size in pixels to draw a symbol. Default is 50. */
    private Integer maxSize;
    /**Minimum size in pixels to draw a symbol. Default is 0. */
    private Integer minSize;
    /**Minimum width in pixels to draw the line work. */
    private Integer minWidth;
    
    /** 
     * Offset values for shadows, hollow symbols, etc ...
     */
    private Dimension offset;
    /** Color to use for outlining polygons and certain marker symbols. 
     * Line symbols do not support outline colors. */
    private RGB outlineColor;
    /** Height, in pixels, of the symbol/pattern to be used. 
     * Only useful with scalable symbols. Default is 1. 
     * For symbols of Type HATCH, the SIZE is the distance between hatched lines. 
     * For its use with hatched lines, see Example#8 in the SYMBOL examples.
     */
    private Integer size;
    /**
     * Attribute/field that stores the size to be used in rendering. Value is given in pixels.
     */
    private String sizeItem;
    /**
     * The symbol name or number to use for all features if attribute tables are not used. 
     * The number is the index of the symbol in the symbol file, starting at 1, 
     * the 5th symbol in the file is therefore symbol number 5. 
     * You can also give your symbols names using the NAME keyword in the symbol definition file, 
     * and use those to refer to them. Default is 0, which results in a single pixel, single width line, 
     * or solid polygon fill, depending on layer type. 
     * You can also specify a gif or png filename. 
     * The path is relative to the location of the mapfile.
     */
    private String symbol;
    /**
     * 
     */
    private Integer width;

    /** Empty constructor */
    public ClassStyle() {
        this(null, null, null, null, null, null, null, null, null, null, null,
                null, null, null);
    }

    /** Creates a new instance of ClassStyle */
    public ClassStyle(Double angle_, String angleItem_, Boolean antialias_, RGB backgroundColor_,
            RGB color_, Integer maxSize_, Integer minSize_, Integer minWidth_, 
            Dimension offset_, RGB outlineColor_, Integer size_, String sizeItem_,
            String symbol_, Integer width_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        angle = angle_;
        angleItem = angleItem_;
        antialias = antialias_;
        backgroundColor = backgroundColor_;
        color = color_;
        maxSize = maxSize_;
        minSize = minSize_;
        minWidth = minWidth_;
        offset = offset_;
        outlineColor = outlineColor_;
        size = size_;
        sizeItem = sizeItem_;
        symbol = symbol_;
        width = width_;
    }

    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public synchronized boolean load(BufferedReader br) {
        boolean result = true;
        try {
            String[] tokens;
            String line;

            while ((line = br.readLine()) != null) {

                // Looking for the first util line
                while ((line.trim().length() == 0) || (line.trim().startsWith("#")) || (line.trim().startsWith("%"))) {
                    line = br.readLine();
                }

                tokens = ConversionUtilities.tokenize(line.trim());

                if (tokens[0].equalsIgnoreCase("ANGLE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for ANGLE: " + line);
                        return false;
                    }
                    this.angle = new Double(ConversionUtilities.getValueFromMapfileLine(line)).doubleValue();
                } else if (tokens[0].equalsIgnoreCase("ANGLEITEM")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for ANGLEITEM: " + line);
                        return false;
                    }
                    angleItem = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("ANTIALIAS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for ANTIALIAS: " + line);
                        return false;
                    }
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        this.antialias = new Boolean(true);
                    } else {
                        this.antialias = new Boolean(false);
                    }
                } else if (tokens[0].equalsIgnoreCase("BACKGROUNDCOLOR")) {
                    backgroundColor = new RGB();
                    result = backgroundColor.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("ClassStyle.load: cannot load BACKGROUNDCOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("COLOR")) {
                    color = new RGB();
                    result = color.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("ClassStyle.load: cannot load COLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("MAXSIZE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for MAXSIZE: " + line);
                        return false;
                    }
                    maxSize = new Integer(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MINSIZE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for MINSIZE: " + line);
                        return false;
                    }
                    minSize = new Integer(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MINWIDTH")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for MINWIDTH: " + line);
                        return false;
                    }
                    minWidth = new Integer(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("OFFSET")) {
                    if (tokens.length < 3) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for OFFSET: " + line);
                        return false;
                    }
                    offset = new Dimension();
                    offset.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    offset.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                } else if (tokens[0].equalsIgnoreCase("OUTLINECOLOR")) {
                    outlineColor = new RGB();
                    result = outlineColor.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("ClassStyle.load: cannot load OUTLINECOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("SIZE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for SIZE: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.getValueFromMapfileLine(line);
                    size = new Integer(tokens[1]);
                } else if (tokens[0].equalsIgnoreCase("SIZEITEM")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for SIZEITEM: " + line);
                        return false;
                    }
                    sizeItem = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("SYMBOL")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for SYMBOL: " + line);
                        return false;
                    }
                    sizeItem = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("WIDTH")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ClassStyle.load: Invalid syntax for WIDTH: " + line);
                        return false;
                    }
                    size = new Integer(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("ClassStyle.load: unknown token: " + line);
                    return false;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
        } catch (Exception e) { // Bad coding, but works...
            logger.warning("ClassStyle.load(). Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**  Saves LABEL object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t\t style\n");
            if (angle != null) {
                bw.write("\t\t\t angle " + angle.toString() + "\n");
            }
            if (angleItem != null) {
                bw.write("\t\t\t angleitem " + angleItem + "\n");
            }
            if (antialias != null) {
                bw.write("\t\t\t antialias " + antialias.toString() + "\n");
            }
            if (backgroundColor != null) {
                bw.write("\t\t\t backgroundcolor ");
                backgroundColor.saveAsMapFile(bw);
            }
            if (color != null) {
                bw.write("\t\t\t color ");
                color.saveAsMapFile(bw);
            }
            if (maxSize != null) {
                bw.write("\t\t\t maxsize " + maxSize.toString() + "\n");
            }
            if (minSize != null) {
                bw.write("\t\t\t minsize " + minSize.toString() + "\n");
            }
            if (minWidth != null) {
                bw.write("\t\t\t minwidth " + minWidth.toString() + "\n");
            }
            if (offset != null) {
                bw.write("\t\t\t offset " + offset.width + " " + offset.height + "\n");
            }
            if (outlineColor != null) {
                bw.write("\t\t\t outlinecolor ");
                outlineColor.saveAsMapFile(bw);
            }
            if (size != null) {
                bw.write("\t\t\t size " + size.toString()+ "\n");
            }
            if (sizeItem != null) {
                bw.write("\t\t\t sizeitem " + sizeItem + "\n");
            }
            if (symbol != null) {
                bw.write("\t\t\t symbol " + symbol + "\n");
            }
            if (width != null) {
                bw.write("\t\t\t width " + width.toString() + "\n");
            }
            bw.write("\t\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    /** Returns a string representation of the LABEL Object
     * @return a string representation of the LABEL Object.
     */
    public String toString() {
        return "not yet implemented. Use a BufferedWriter to write this object to";
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public String getAngleItem() {
        return angleItem;
    }

    public void setAngleItem(String angleItem) {
        this.angleItem = angleItem;
    }

    public Boolean getAntialias() {
        return antialias;
    }

    public void setAntialias(Boolean antialias) {
        this.antialias = antialias;
    }

    public RGB getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(RGB backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public RGB getColor() {
        return color;
    }

    public void setColor(RGB color) {
        this.color = color;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getMinSize() {
        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    public Integer getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
    }

    public Dimension getOffset() {
        return offset;
    }

    public void setOffset(Dimension offset) {
        this.offset = offset;
    }

    public RGB getOutlineColor() {
        return outlineColor;
    }

    public void setOutlineColor(RGB outlineColor) {
        this.outlineColor = outlineColor;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSizeItem() {
        return sizeItem;
    }

    public void setSizeItem(String sizeItem) {
        this.sizeItem = sizeItem;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }
}
