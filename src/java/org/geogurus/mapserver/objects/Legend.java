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
 * Legend.java
 *
 * Created on 20 mars 2002, 16:04
 */
package org.geogurus.mapserver.objects;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 *Defines how a legend is to be built.
 * Legend components are built automatically from class objects from individual layers.
 * Starts with the keyword LEGEND and terminates with the keyword END.
 * The size of the legend image is NOT known prior to creation
 * so be careful not to hard-code width and height in the <IMG> tag in the template file.
 *
 *
 * @author  Bastien VIALADE
 */
public class Legend extends MapServerObject implements java.io.Serializable {
    // Constants for interlaced mode, transparence and status
    public static final byte ON = 0;
    public static final byte OFF = 1;
    // Dedicated to status
    public static final byte EMBED = 2;
    // Constants representing positions
    public static final byte UL = 0;
    public static final byte UC = 1;
    public static final byte UR = 2;
    public static final byte LL = 3;
    public static final byte LC = 4;
    public static final byte LR = 5;
    /** Color to initialize the legend with (i.e. the background). */
    private RGB imageColor;
    /** Should the output image be interlaced? */
    private byte interlace;
    /**Signals the start of a LABEL object*/
    private Label label;
    /** Color to use for outlining symbol key boxes.*/
    private RGB outlineColor;
    /** Where to place an embedded legend in the map. */
    private byte position;
    /** Size of symbol key boxes in pixels.*/
    private Dimension keySize;
    /** Spacing between symbol key boxes and labels in pixels. */
    private Dimension keySpacing;
    /** Tells MapServer to render this legend after all labels
     * in the cache have been drawn.
     * Useful for adding neatlines and similar elements */
    private boolean postLabelCache;
    /** Is the legend image to be created */
    private byte status;
    /** The template file to use to generate HTML Legends */
    private File template;
    /** Should the background color for the legend be transparent. */
    private byte transparent;

    /** Empty constructor */
    public Legend() {
        this(new RGB(255, 255, 255), null, new RGB(0, 0, 0), Legend.ON);
    }

    /** Creates a new instance of Legend */
    public Legend(RGB imageColor_, Label label_, RGB outlineColor_, byte status_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        imageColor = imageColor_;
        interlace = Legend.ON;
        label = label_;
        outlineColor = outlineColor_;
        position = Legend.LR;
        keySize = new Dimension(20, 10);
        keySpacing = new Dimension(5, 5);
        postLabelCache = false;
        status = status_;
        template = null;
        transparent = Legend.OFF;
    }

    // Get and set methods
    public RGB getImageColor() {
        return imageColor;
    }

    public byte getInterlace() {
        return interlace;
    }

    public Label getLabel() {
        return label;
    }

    public RGB getOutlineColor() {
        return outlineColor;
    }

    public byte getPosition() {
        return position;
    }

    public Dimension getKeySize() {
        return keySize;
    }

    public Dimension getKeySpacing() {
        return keySpacing;
    }

    public boolean isPostLabelCache() {
        return postLabelCache;
    }

    public byte getStatus() {
        return status;
    }

    public byte getTransparence() {
        return transparent;
    }

    public File getTemplate() {
        return template;
    }

    public void setImageColor(RGB imageColor_) {
        imageColor = imageColor_;
    }

    public void setInterlace(byte interlace_) {
        interlace = interlace_;
    }

    public void setLabel(Label label_) {
        label = label_;
    }

    public void setOutlineColor(RGB outlineColor_) {
        outlineColor = outlineColor_;
    }

    public void setPosition(byte position_) {
        position = position_;
    }

    public void setKeySize(Dimension keySize_) {
        keySize = keySize_;
    }

    public void setKeySpacing(Dimension keySpacing_) {
        keySpacing = keySpacing_;
    }

    public void setPostLabelCache(boolean postLabelCache_) {
        postLabelCache = postLabelCache_;
    }

    public void setStatus(byte status_) {
        status = status_;
    }

    public void setTransparence(byte transparent_) {
        transparent = transparent_;
    }

    public void setTemplate(File template_) {
        template = template_;
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
                if (tokens[0].equalsIgnoreCase("IMAGECOLOR")) {
                    imageColor = new RGB();
                    result = imageColor.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("Legend.load: cannot load IMAGECOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("INTERLACE")) {
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        interlace = Legend.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        interlace = Legend.OFF;
                    } else {
                        MapServerObject.setErrorMessage("Legend.load: Invalid value for INTERLACE: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("LABEL")) {
                    label = new Label();
                    result = label.load(br);
                    if (!result) {
                        MapServerObject.setErrorMessage("Legend.load: cannot load LABEL object");
                    }
                } else if (tokens[0].equalsIgnoreCase("OUTLINECOLOR")) {
                    outlineColor = new RGB();
                    result = outlineColor.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("Legend.load: cannot load OUTLINECOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("POSITION")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Legend.load: Invalid syntax for POSITION: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.getValueFromMapfileLine(line);
                    if (tokens[1].equalsIgnoreCase("UL")) {
                        position = Legend.UL;
                    } else if (tokens[1].equalsIgnoreCase("UC")) {
                        position = Legend.UC;
                    } else if (tokens[1].equalsIgnoreCase("UR")) {
                        position = Legend.UR;
                    } else if (tokens[1].equalsIgnoreCase("LL")) {
                        position = Legend.LL;
                    } else if (tokens[1].equalsIgnoreCase("LC")) {
                        position = Legend.LC;
                    } else if (tokens[1].equalsIgnoreCase("LR")) {
                        position = Legend.LR;
                    } else {
                        MapServerObject.setErrorMessage("Legend.load: Invalid value for POSITION: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("KEYSIZE")) {
                    if (tokens.length < 3) {
                        MapServerObject.setErrorMessage("Legend.load: Invalid syntax for KEYSIZE: " + line);
                        return false;
                    }
                    keySize = new Dimension();
                    keySize.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    keySize.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                } else if (tokens[0].equalsIgnoreCase("KEYSPACING")) {
                    if (tokens.length < 3) {
                        MapServerObject.setErrorMessage("Legend.load: Invalid syntax for KEYSPACING: " + line);
                        return false;
                    }
                    keySpacing = new Dimension();
                    keySpacing.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    keySpacing.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                } else if (tokens[0].equalsIgnoreCase("POSTLABELCACHE")) {
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        postLabelCache = true;
                    } else {
                        postLabelCache = false;
                    }
                } else if (tokens[0].equalsIgnoreCase("STATUS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Legend.load: Invalid syntax for STATUS: " + line);
                        return false;
                    }
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        status = Legend.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        status = Legend.OFF;
                    } else if (tokens[1].equalsIgnoreCase("EMBED")) {
                        status = Legend.EMBED;
                    } else {
                        MapServerObject.setErrorMessage("Legend.load: Invalid value for STATUS: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("TEMPLATE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Legend.load: Invalid syntax for TEMPLATE: " + line);
                        return false;
                    }
                    String templatePathString = ConversionUtilities.getValueFromMapfileLine(line);
                    template = new File(templatePathString);
                } else if (tokens[0].equalsIgnoreCase("TRANSPARENT")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Legend.load: Invalid syntax for TRANSPARENT: " + line);
                        return false;
                    }
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        transparent = Legend.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        transparent = Legend.OFF;
                    } else {
                        MapServerObject.setErrorMessage("Legend.load: Invalid value for TRANSPARENT: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    return false;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
        } catch (Exception e) { // Bad coding, but works...
            logger.warning("Legend.load(). Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**  Saves LEGEND object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t legend\n");
            if (imageColor != null) {
                bw.write("\t\t imagecolor ");
                imageColor.saveAsMapFile(bw);
            }
            switch (interlace) {
                case ON:
                    bw.write("\t\t interlace ON\n");
                    break;
                case OFF:
                    bw.write("\t\t interlace OFF\n");
                    break;
                default:
                    break;
            }
            if (label != null) {
                label.saveAsMapFile(bw);
            }
            if (outlineColor != null) {
                bw.write("\t\t outlinecolor ");
                outlineColor.saveAsMapFile(bw);
            }
            switch (position) {
                case UL:
                    bw.write("\t\t position UL\n");
                    break;
                case UC:
                    bw.write("\t\t position UC\n");
                    break;
                case UR:
                    bw.write("\t\t position UR\n");
                    break;
                case LL:
                    bw.write("\t\t position LL\n");
                    break;
                case LC:
                    bw.write("\t\t position LC\n");
                    break;
                case LR:
                    bw.write("\t\t position LR\n");
                    break;
                default:
                    break;
            }
            if (keySize != null) {
                bw.write("\t\t keysize " + keySize.width + " " + keySize.height + "\n");
            }
            if (keySpacing != null) {
                bw.write("\t\t keyspacing " + keySpacing.width + " " + keySpacing.height + "\n");
            }
            if (postLabelCache == true) {
                bw.write("\t\t postlabelcache TRUE\n");
            }
            switch (status) {
                case ON:
                    bw.write("\t\t status ON\n");
                    break;
                case OFF:
                    bw.write("\t\t status OFF\n");
                    break;
                case EMBED:
                    bw.write("\t\t status EMBED\n");
                    break;
            }
            if (template != null) {
                bw.write("\t template " + ConversionUtilities.quotesIfNeeded(template.getPath().replace('\\', '/')) + "\n");
            }
            switch (transparent) {
                case ON:
                    bw.write("\t\t transparent ON\n");
                    break;
                case OFF:
                    bw.write("\t\t transparent OFF\n");
                    break;
                default:
                    break;
            }
            bw.write("\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    /** Returns a string representation of the LEGEND Object
     * @return a string representation of the LEGEND Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("LEGEND OBJECT ");
            if (keySize != null) {
                buffer.append("\n* LEGEND keySize           = ").append(keySize);
            }
            buffer.append("\n* LEGEND status            = ").append(status);
            if (label != null) {
                buffer.append("\n* LEGEND label           = ").append(label.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY LEGEND OBJECT\n\n" + ex;
        }
        return buffer.toString();
    }
}
