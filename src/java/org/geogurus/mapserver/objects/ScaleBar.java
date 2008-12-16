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
 * ScaleBar.java
 *
 * Created on 20 mars 2002, 17:08
 */
package org.geogurus.mapserver.objects;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 *Defines how a scalebar should be built.
 * Starts with the keyword SCALEBAR and terminates with the keyword END.
 * Scalebars currently do not make use of TrueType fonts.
 * The size of the scalebar image is NOT known prior to rendering,
 * so be careful not to hard-code width and height in the <IMG> tag in the template file.
 * Future versions will make the image size available.
 *
 * @author  Bastien VIALADE
 */
public class ScaleBar extends MapServerObject implements java.io.Serializable {
    // Constants for interlace and status
    public static final byte ON = 0;
    public static final byte OFF = 1;
    // Constants for interlace and status
    public static final boolean FALSE = false;
    public static final boolean TRUE = true;
    // Only for status
    public static final byte EMBED = 2;
    // Constants representing positions
    public static final byte UL = 0;
    public static final byte UC = 1;
    public static final byte UR = 2;
    public static final byte LL = 3;
    public static final byte LC = 4;
    public static final byte LR = 5;
    // Constants defines units
    public static final byte PIXEL = 0;
    public static final byte FEET = 1;
    public static final byte INCHES = 2;
    public static final byte KILOMETERS = 3;
    public static final byte METERS = 4;
    public static final byte MILES = 5;
    // constants defining align
    public static final byte LEFT = 10;
    public static final byte CENTER = 11;
    public static final byte RIGHT = 12;
    
    
    /**
     * Defines how the scalebar is aligned within the scalebar image. 
     * Default is center. Available in versions 5.2 and higher.
     */
    private Byte align;
    /** Color to use for scalebar background, not the image background. */
    private RGB backgroundColor;
    /** Color to use for drawing all features if attribute tables are not used.*/
    private RGB color;
    /** Color to initialize the scalebar with (i.e. background). */
    private RGB imageColor;
    /** Should output images be interlaced? Default is [on]. */
    private boolean interlace;
    /** Number of intervals to break the scalebar into. Default is 4. */
    private int intervals;
    /** Signals the start of a LABEL object */
    private Label label;
    /** Color to use for outlining individual intervals.
     * Set any component to -1 for no outline which is the default.*/
    private RGB outlineColor;
    /** Where to place an embedded scalebar in the image. Default is lr. */
    private byte position;
    /** For use with embedded scalebars only.
     * Tells the MapServer to embed the scalebar after all labels in the cache have been drawn.
     * Default is false. */
    private boolean postLabelCache;
    /** Size in pixels of the scalebar. Labeling is not taken into account.*/
    private Dimension size;
    /** Is the scalebar image to be created, and if so should it be embedded into the image?
     * Default is off.
     * (Please note that embedding scalebars require that you define a markerset.
     * In essence the scalebar becomes a custom marker that is handled just like any other annotation.) */
    private byte status;
    /** Chooses the scalebar style. Valid styles are 0 and 1. */
    private int style;
    /** Should the background color for the scalebar be transparent. Default is off . */
    private byte transparent;
    /** Output scalebar units, default is miles.
     * Used in conjunction with the map's units to develop the actual graphic.
     * Note that decimal degrees are not valid scalebar units. */
    private byte units;

    /** Empty constructor */
    public ScaleBar() {
        this(null, null, null, null, null, null, null, (byte) 0);
    }

    /** Creates a new instance of ScaleBar */
    public ScaleBar(Byte align_, RGB backgroundColor_, RGB color_, RGB imageColor_,
            Label label_, RGB outlineColor_, Dimension size_, int style_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        align = align_;
        backgroundColor = backgroundColor_;
        color = color_;
        imageColor = imageColor_;
        interlace = true;
        intervals = 4;
        label = label_;
        outlineColor = outlineColor_;
        position = ScaleBar.LR;
        postLabelCache = false;
        size = size_;
        status = ScaleBar.OFF;
        style = style_;
        transparent = ScaleBar.OFF;
        units = ScaleBar.MILES;
    }

    // Set and get methods
    public void setBackgroundColor(RGB backgroundColor_) {
        backgroundColor = backgroundColor_;
    }

    public void setColor(RGB color_) {
        color = color_;
    }

    public void setImageColor(RGB imageColor_) {
        imageColor = imageColor_;
    }

    public void setInterlace(boolean interlace_) {
        interlace = interlace_;
    }

    public void setIntervals(int intervals_) {
        intervals = intervals_;
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

    public void setPostLabelCache(boolean postLabelCache_) {
        postLabelCache = postLabelCache_;
    }

    public void setSize(Dimension size_) {
        size = size_;
    }

    public void setStatus(byte status_) {
        status = status_;
    }

    public void setStyle(int style_) {
        style = style_;
    }

    public void setTransparent(byte transparent_) {
        transparent = transparent_;
    }

    public void setUnits(byte units_) {
        units = units_;
    }

    public RGB getBackgroundColor() {
        return backgroundColor;
    }

    public RGB getColor() {
        return color;
    }

    public RGB getImageColor() {
        return imageColor;
    }

    public boolean isInterlace() {
        return interlace;
    }

    public int getIntervals() {
        return intervals;
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

    public boolean isPostLabelCache() {
        return postLabelCache;
    }

    public Dimension getSize() {
        return size;
    }

    public byte getStatus() {
        return status;
    }

    public int getStyle() {
        return style;
    }

    public byte getTransparent() {
        return transparent;
    }

    public byte getUnits() {
        return units;
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
                if (tokens[0].equalsIgnoreCase("ALIGN")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid syntax for ALIGN: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("LEFT")) {
                        align = ScaleBar.LEFT;
                    } else if (tokens[1].equalsIgnoreCase("CENTER")) {
                        align = ScaleBar.CENTER;
                    } else if (tokens[1].equalsIgnoreCase("RIGHT")) {
                        align = ScaleBar.RIGHT;
                    } else {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid value for ALIGN: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("BACKGROUNDCOLOR")) {
                    backgroundColor = new RGB();
                    result = backgroundColor.load(tokens);
                     if (!result) {
                        MapServerObject.setErrorMessage("ScaleBar.load: cannot load BACKGROUNDCOLOR object");
                    }
               } else if (tokens[0].equalsIgnoreCase("COLOR")) {
                    color = new RGB();
                    result = color.load(tokens);
                     if (!result) {
                        MapServerObject.setErrorMessage("ScaleBar.load: cannot load COLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("IMAGECOLOR")) {
                    imageColor = new RGB();
                    result = imageColor.load(tokens);
                     if (!result) {
                        MapServerObject.setErrorMessage("ScaleBar.load: cannot load IMAGECOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("INTERLACE")) {
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        interlace = true;
                    } else {
                        interlace = false;
                    }
                } else if (tokens[0].equalsIgnoreCase("INTERVALS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid syntax for INTERVALS: " + line);
                        return false;
                    }
                    intervals = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase("LABEL")) {
                    label = new Label();
                    result = label.load(br);
                     if (!result) {
                        MapServerObject.setErrorMessage("ScaleBar.load: cannot load LABEL object");
                    }
                } else if (tokens[0].equalsIgnoreCase("OUTLINECOLOR")) {
                    outlineColor = new RGB();
                    result = outlineColor.load(tokens);
                     if (!result) {
                        MapServerObject.setErrorMessage("ScaleBar.load: cannot load OUTLINECOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("POSITION")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid syntax for POSITION: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("UL")) {
                        position = ScaleBar.UL;
                    } else if (tokens[1].equalsIgnoreCase("UC")) {
                        position = ScaleBar.UC;
                    } else if (tokens[1].equalsIgnoreCase("UR")) {
                        position = ScaleBar.UR;
                    } else if (tokens[1].equalsIgnoreCase("LL")) {
                        position = ScaleBar.LL;
                    } else if (tokens[1].equalsIgnoreCase("LC")) {
                        position = ScaleBar.LC;
                    } else if (tokens[1].equalsIgnoreCase("LR")) {
                        position = ScaleBar.LR;
                    } else {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid value for POSITION: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("POSTLABELCACHE")) {
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        postLabelCache = true;
                    } else {
                        postLabelCache = false;
                    }
                } else if (tokens[0].equalsIgnoreCase("SIZE")) {
                    if (tokens.length < 3) {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid syntax for SIZE: " + line);
                        return false;
                    }
                    size = new Dimension();
                    size.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    size.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                } else if (tokens[0].equalsIgnoreCase("STATUS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid value for STATUS: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        status = ScaleBar.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        status = ScaleBar.OFF;
                    } else if (tokens[1].equalsIgnoreCase("EMBED")) {
                        status = ScaleBar.EMBED;
                    } else {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid value for STATUS: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("STYLE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid syntax for STYLE: " + line);
                        return false;
                    }
                    style = Byte.parseByte(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase("TRANSPARENT")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid syntax for TRANSPARENT: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON") || tokens[1].equalsIgnoreCase("TRUE")) {
                        transparent = ScaleBar.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF") || tokens[1].equalsIgnoreCase("FALSE")) {
                        transparent = ScaleBar.OFF;
                    } else {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid value for TRANSPARENT: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("UNITS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid syntax for UNITS: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("FEET")) {
                        units = ScaleBar.FEET;
                    } else if (tokens[1].equalsIgnoreCase("INCHES")) {
                        units = ScaleBar.INCHES;
                    } else if (tokens[1].equalsIgnoreCase("KILOMETERS")) {
                        units = ScaleBar.KILOMETERS;
                    } else if (tokens[1].equalsIgnoreCase("METERS")) {
                        units = ScaleBar.METERS;
                    } else if (tokens[1].equalsIgnoreCase("MILES")) {
                        units = ScaleBar.MILES;
                    } else {
                        MapServerObject.setErrorMessage("ScaleBar.load: Invalid value for UNITS: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("ScaleBar.load: unknown token: " + line);
                    return false;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
        } catch (Exception e) { // Bad coding, but works...
            logger.warning("ScaleBar.load(). Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**  Saves SCALEBAR object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t scalebar\n");
            if (align != null) {
                switch (align.byteValue()) {
                    case ScaleBar.CENTER :
                        bw.write("\t\t align CENTER \n");
                        break;
                    case ScaleBar.LEFT :
                        bw.write("\t\t align LEFT \n");
                        break;
                    case ScaleBar.RIGHT :
                        bw.write("\t\t align RIGHT \n");
                        break;
                }
            }
            if (backgroundColor != null) {
                bw.write("\t\t backgroundcolor ");
                backgroundColor.saveAsMapFile(bw);
            }
            if (color != null) {
                bw.write("\t\t color ");
                color.saveAsMapFile(bw);
            }
            if (imageColor != null) {
                bw.write("\t\t imagecolor ");
                imageColor.saveAsMapFile(bw);
            }
            if (interlace == true) {
                bw.write("\t\t interlace TRUE \n");
            } else {
                bw.write("\t\t interlace FALSE \n");
            }

            bw.write("\t\t intervals " + intervals + "\n");

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
            }

            if (postLabelCache == true) {
                bw.write("\t\t postlabelcache TRUE \n");
            } else {
                bw.write("\t\t postlabelcache FALSE \n");
            }
            if (size != null) {
                bw.write("\t\t size " + size.width + " " + size.height + "\n");
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
            bw.write("\t\t style " + style + "\n");
            switch (transparent) {
                case ON:
                    bw.write("\t\t transparent ON\n");
                    break;
                case OFF:
                    bw.write("\t\t transparent OFF\n");
                    break;
            }
            switch (units) {
                case FEET:
                    bw.write("\t\t units FEET\n");
                    break;
                case INCHES:
                    bw.write("\t\t units INCHES\n");
                    break;
                case KILOMETERS:
                    bw.write("\t\t units KILOMETERS\n");
                    break;
                case METERS:
                    bw.write("\t\t units METERS\n");
                    break;
                case MILES:
                    bw.write("\t\t units MILES\n");
                    break;
            }
            if (label != null) {
                label.saveAsMapFile(bw);
            }
            bw.write("\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    /** Returns a string representation of the SCALEBAR Object
     * @return a string representation of the SCALEBAR Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("SCALEBAR OBJECT ");
            if (backgroundColor != null) {
                buffer.append("\n* SCALEBAR backgroundColor = ").append(backgroundColor.toString());
            }
            if (color != null) {
                buffer.append("\n* SCALEBAR color           = ").append(color.toString());
            }
            buffer.append("\n* SCALEBAR intervals       = ").append(intervals);
            if (outlineColor != null) {
                buffer.append("\n* SCALEBAR outlineColor    = ").append(outlineColor.toString());
            }
            buffer.append("\n* SCALEBAR position        = ").append(position);
            if (size != null) {
                buffer.append("\n* SCALEBAR size            = ").append(size);
            }
            buffer.append("\n* SCALEBAR status          = ").append(status);
            buffer.append("\n* SCALEBAR style           = ").append(style);
            buffer.append("\n* SCALEBAR transparent     = ").append(transparent);
            buffer.append("\n* SCALEBAR units           = ").append(units);
            if (label != null) {
                buffer.append("\n* SCALEBAR label           = ").append(label.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY SCALEBAR OBJECT\n\n" + ex;
        }
        return buffer.toString();
    }

    public Byte getAlign() {
        return align;
    }

    public void setAlign(Byte align) {
        this.align = align;
    }
}
