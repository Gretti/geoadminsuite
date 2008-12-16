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
 * QueryMap.java
 *
 * Created on 20 mars 2002, 16:36
 */
package org.geogurus.mapserver.objects;

import java.awt.Dimension;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * Defines a mechanism to map the results of a query.
 * Starts with the keyword QUERYMAP and terminates with the keyword END
 *
 * @author  Bastien VIALADE
 */
public class QueryMap extends MapServerObject implements java.io.Serializable {
    // Constants for status
    public static final byte ON = 0;
    public static final byte OFF = 1;
    // Constants for style
    public static final byte NORMAL = 0;
    public static final byte HILITE = 1;
    public static final byte SELECTED = 2;
    /** Color in which features are highlighted. Default is yellow. */
    private RGB color;
    /** Size of the map in pixels.
     * Defaults to the size defined in the map object. */
    private Dimension size;
    /** Is the query map to be drawn ? */
    private byte status;
    /** Sets how selected features are to be handled.
     * Layers not queried are drawn as usual.
     * Normal: Draws all features according to the settings for that layer.
     * Hilite: Draws selected features using COLOR. Non-selected features are drawn normally.
     * Selected: draws only the selected features normally. */
    private byte style;

    /** Empty constructor */
    public QueryMap() {
        this(null, QueryMap.ON, QueryMap.HILITE);
    }

    /** Creates a new instance of QueryMap */
    public QueryMap(Dimension size_, byte status_, byte style) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.color = new RGB(255, 255, 0);
        this.size = size_;
        this.status = status_;
        this.style = style;
    }

    // Get and set methods
    public void setColor(RGB color_) {
        color = color_;
    }

    public void setSize(Dimension size_) {
        size = size_;
    }

    public void setStatus(byte status_) {
        status = status_;
    }

    public void setStyle(byte style_) {
        style = style_;
    }

    public RGB getColor() {
        return color;
    }

    public Dimension getSize() {
        return size;
    }

    public byte getStatus() {
        return status;
    }

    public byte getStyle() {
        return style;
    }

    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
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
                if (tokens[0].equalsIgnoreCase("COLOR")) {
                    color = new RGB();
                    result = color.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("QueryMap.load: cannot load COLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("SIZE")) {
                    if (tokens.length < 3) {
                        MapServerObject.setErrorMessage("QueryMap.load: Invalid syntax for SIZE: " + line);
                        return false;
                    }
                    size = new Dimension();
                    size.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    size.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                } else if (tokens[0].equalsIgnoreCase("STATUS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("QueryMap.load: Invalid syntax for STATUS: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        status = QueryMap.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        status = QueryMap.OFF;
                    } else {
                        MapServerObject.setErrorMessage("QueryMap.load: Invalid value for STATUS: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("STYLE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("QueryMap.load: Invalid syntax for STYLE: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("NORMAL")) {
                        style = QueryMap.NORMAL;
                    } else if (tokens[1].equalsIgnoreCase("HILITE")) {
                        style = QueryMap.HILITE;
                    } else if (tokens[1].equalsIgnoreCase("SELECTED")) {
                        style = QueryMap.SELECTED;
                    } else {
                        MapServerObject.setErrorMessage("QueryMap.load: Invalid value for STYLE: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("QueryMap.load: unknown token: " + line);
                    return false;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
        } catch (Exception e) { 
            logger.warning("QueryMap.load(). Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
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
            bw.write("\t\t querymap\n");
            if (color != null) {
                bw.write("\t\t\t color ");
                color.saveAsMapFile(bw);
            }
            if (size != null) {
                bw.write("\t\t\t size " + size.width + " " + size.height + "\n");
            }
            switch (status) {
                case ON:
                    bw.write("\t\t\t status ON\n");
                    break;
                case OFF:
                    bw.write("\t\t\t status OFF\n");
                    break;
            }
            switch (style) {
                case NORMAL:
                    bw.write("\t\t\t style NORMAL\n");
                    break;
                case HILITE:
                    bw.write("\t\t\t style HILITE\n");
                    break;
                case SELECTED:
                    bw.write("\t\t\t style SELECTED\n");
                    break;
            }
            bw.write("\t\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;

    }

    public String toString() {
        return "Not yet implemented";
    }
}

