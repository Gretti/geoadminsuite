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
 * Symbol.java
 *
 * Created on 20 mars 2002, 18:03
 */
package org.geogurus.mapserver.objects;

import java.awt.Point;
import java.io.BufferedReader;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * Symbol definitions can be included within the main MapFile or, more commonly,
 * in a separate file.
 * Symbol definitions in a separate file are designated using the SYMBOLSET keyword,
 * as part of the Map Object.
 * This recommended setup is ideal for re-using symbol definitions across multiple MapServer applications.
 *
 * There are 3 main types of symbols in MapServer: Markers, Shadesets, and Lines.
 *
 * Symbol 0 is always the degenerate case for a particular class of symbol.
 * For points, symbol 0 is a single pixel, for shading (i.e. filled polygons)
 * symbol 0 is a solid fill, and for lines, symbol 0 is a single pixel wide line.
 *
 * Symbol definitions contain no color information, colors are set within CLASS objects.
 *
 * There is a maximum of 64 symbols per file.
 * This can be changed by editing mapsymbol.h and changing the value of MS_MAXSYMBOLS at the top of the file.
 *
 * @author  Bastien VIALADE
 */
public class Symbol extends MapServerObject implements java.io.Serializable {
    // Constants that defines types
    public static final byte VECTOR = 0;
    public static final byte ELLIPSE = 1;
    public static final byte PIXMAP = 2;
    public static final byte TRUETYPE = 3;
    public static final byte SIMPLE = 4;
    /** Should TrueType fonts be antialiased. */
    private boolean antialias;
    /** Character used to reference a particular TrueType font character.
     * You'll need to figure out the mapping from the keyboard character to font character. */
    private char character;
    /** Sets the symbol to be filled with a user defined color (See the CLASS object).
     * For marker symbols, if OUTLINECOLOR was specified then the symbol is outlined with it. */
    private boolean filled;
    /** Name of TrueType font to use as defined in the FONTSET.*/
    private String font;
    /** Given in pixels.
     * This defines a distance between symbols for TrueType lines. */
    private int gap;
    /** Image (GIF or PNG) to use as a marker or brush for type PIXMAP symbols.*/
    private String image;
    /** Alias for this font to be used in CLASS objects */
    private String name;
    /** Signifies the start of the definition of points that make up
     * a vector symbol or that define the x and y radius of an ellipse symbol.
     * The end of this section is signified with the keyword END.
     * Coordinates are given in pixels and define the default size
     * of the symbol before any scaling.
     * You can create non-contiguous paths by inserting negative coordinates at the appropriate place.
     * For ellipse symbols you provide a single point that defines
     * the x and y radius of an ellipse. Circles are created when x and y are equal. */
    private Points points;
    /** Sets a transparent color for the input GIF image for pixmap symbols,
     * or determines whether all shade symbols should have a transparent background.
     * For shade symbols it may be desirable to have background features "show through"
     * a transparent hatching pattern, creating a more complex map.
     * By default a symbol's background is the same as the parent image (i.e. color 0).
     * This is user configurable */
    private int transparent;
    /** vector: a simple drawing is used to define the shape of the symbol.
     * ellipse: radius values in the x and y directions define an ellipse.
     * pixmap: a user supplied GIF image will be used as the symbol.
     * truetype: TrueType font to use as defined in the FONTSET. */
    private byte type;
    /**Defines a dash style or pattern.*/
    private Style style;

    /** Empty constructor */
    public Symbol() {
        this(false, '\n', null, 0, null, null, -1, (byte) 0, null);
    }

    /** Creates a new instance of Symbol */
    public Symbol(boolean antialias_, char character_, String font_,
            int gap_, String image_, String name_, int transparent_, byte type_, Style style_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        antialias = antialias_;
        character = character_;
        filled = false;
        font = font_;
        gap = gap_;
        image = image_;
        name = name_;
        points = null;
        transparent = transparent_;
        type = type_;
        style = style_;
    }

    public void setAntialias(boolean antialias_) {
        antialias = antialias_;
    }

    public void setCharacter(char character_) {
        character = character_;
    }

    public void setFilled(boolean filled_) {
        filled = filled_;
    }

    public void setFont(String font_) {
        font = font_;
    }

    public void setGap(int gap_) {
        gap = gap_;
    }

    public void setImage(String image_) {
        image = image_;
    }

    public void setName(String name_) {
        name = name_;
    }

    public void setPoints(Points points_) {
        points = points_;
    }

    public void setTransparent(int transparent_) {
        transparent = transparent_;
    }

    public void setType(byte type_) {
        type = type_;
    }

    public void setStyle(Style style_) {
        style = style_;
    }

    public boolean addPoint(Point point) {
        if (points == null) {
            points = new Points();
        }
        return points.add(point);
    }

    public boolean isAntialias() {
        return antialias;
    }

    public char getCharacter() {
        return character;
    }

    public boolean getFilled() {
        return filled;
    }

    public String getFont() {
        return font;
    }

    public int getGap() {
        return gap;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public Points getPoints() {
        return points;
    }

    public int getTransparent() {
        return transparent;
    }

    public byte getType() {
        return type;
    }

    public Style getStyle() {
        return style;
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
                if (tokens[0].equalsIgnoreCase("NAME")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Symbol.load: Invalid syntax for NAME: " + line);
                        return false;
                    }
                    name = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("ANTIALIAS")) {
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        antialias = true;
                    } else {
                        antialias = false;
                    }
                } else if (tokens[0].equalsIgnoreCase("CHARACTER")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Symbol.load: Invalid syntax for CHARACTER: " + line);
                        return false;
                    }
                    character = ConversionUtilities.getValueFromMapfileLine(line).charAt(0);
                } else if (tokens[0].equalsIgnoreCase("FILLED")) {
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        filled = true;
                    } else {
                        filled = false;
                    }
                } else if (tokens[0].equalsIgnoreCase("FONT")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Symbol.load: Invalid syntax for FONT: " + line);
                        return false;
                    }
                    font = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("GAP")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Symbol.load: Invalid syntax for GAP: " + line);
                        return false;
                    }
                    gap = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("IMAGE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Symbol.load: Invalid syntax for IMAGE: " + line);
                        return false;
                    }
                    image = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("POINTS")) {
                    points = new Points();
                    result = points.load(tokens, br);
                    if (!result) {
                        MapServerObject.setErrorMessage("SYMBOL.load: cannot load POINTS object");
                    }
                } else if (tokens[0].equalsIgnoreCase("STYLE")) {
                    style = new Style();
                    result = style.load(tokens, br);
                    if (!result) {
                        MapServerObject.setErrorMessage("SYMBOL.load: cannot load STYLE object");
                    }
                } else if (tokens[0].equalsIgnoreCase("TRANSPARENT")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Symbol.load: Invalid syntax for TRANSPARENT: " + line);
                        return false;
                    }
                    transparent = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("TYPE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Symbol.load: Invalid syntax for TYPE: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("VECTOR")) {
                        type = Symbol.VECTOR;
                    } else if (tokens[1].equalsIgnoreCase("ELLIPSE")) {
                        type = Symbol.ELLIPSE;
                    } else if (tokens[1].equalsIgnoreCase("PIXMAP")) {
                        type = Symbol.PIXMAP;
                    } else if (tokens[1].equalsIgnoreCase("TRUETYPE")) {
                        type = Symbol.TRUETYPE;
                    } else if (tokens[1].equalsIgnoreCase("SIMPLE")) {
                        type = Symbol.SIMPLE;
                    } else {
                        MapServerObject.setErrorMessage("Symbol.load: Invalid value for TYPE: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("Symbol.load: unknown token: " + line);
                    return false;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warning("Symbol.load(). Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**  Saves SYMBOL object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t symbol\n");
            if (name != null) {
                bw.write("\t\t name " +  ConversionUtilities.quotesIfNeeded(name) + "\n");
            }
            if (antialias == true) {
                bw.write("\t\t antialias TRUE\n");
            } else {
                bw.write("\t\t antialias FALSE\n");
            }
            if (character != '\n') {
                bw.write("\t\t character " + ConversionUtilities.quotesIfNeeded(character + "") + "\n");
            }
            if (filled == true) {
                bw.write("\t\t filled TRUE\n");
            } else {
                bw.write("\t\t filled FALSE\n");
            }
            if (font != null) {
                bw.write("\t\t font " + ConversionUtilities.quotesIfNeeded(font) + "\n");
            }
            if (gap > -1) {
                bw.write("\t\t gap " + gap + "\n");
            }
            if (image != null) {
                bw.write("\t\t image " + ConversionUtilities.quotesIfNeeded(image) + "\n");
            }
            if (transparent > -1) {
                bw.write("\t\t transparent " + transparent + "\n");
            }
            switch (type) {
                case VECTOR:
                    bw.write("\t\t type VECTOR\n");
                    break;
                case ELLIPSE:
                    bw.write("\t\t type ELLIPSE\n");
                    break;
                case PIXMAP:
                    bw.write("\t\t type PIXMAP\n");
                    break;
                case TRUETYPE:
                    bw.write("\t\t type TRUETYPE\n");
                    break;
            }
            if (points != null) {
                points.saveAsMapFile(bw);
            }
            if (style != null) {
                style.saveAsMapFile(bw);
            }
            bw.write("\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    /** Returns a string representation of the SYMBOL Object
     * @return a string representation of the SYMBOL Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("SYMBOL OBJECT ");
            if (name != null) {
                buffer.append("\n* SYMBOL name           = ").append(ConversionUtilities.quotesIfNeeded(name));
            }
            buffer.append("\n* SYMBOL type           = ").append(type);
            if (points != null) {
                buffer.append("\n* SYMBOL points         = ").append(points.toString());
            }
            if (style != null) {
                buffer.append("\n* SYMBOL style         = ").append(style.toString());
            }
            buffer.append("\n* SYMBOL filled         = ").append(filled);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY SYMBOL OBJECT\n\n" + ex;
        }
        return buffer.toString();
    }

    /**
     * Symbols are equals if their names are equalsIgnoreCase
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            if (this.name != null && this.name.equalsIgnoreCase(((Symbol) obj).getName())) {
                return true;
            }
        }
        return false;
    }
}

