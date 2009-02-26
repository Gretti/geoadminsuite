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
 * Join.java
 *
 * Created on 20 mars 2002, 10:46
 */
package org.geogurus.mapserver.objects;

import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * Defines how a specific join is handled.
 * Starts with the keyword JOIN and terminate with the keywrod END.
 * Joins are defined within a query object.
 *
 * @author Nicolas Ribot
 */
public class OutputFormat extends MapServerObject implements java.io.Serializable {

    // Constants. Join types
    public static final byte PC256 = 0;
    public static final byte RGB = 1;
    public static final byte RGBA = 2;
    public static final byte INT16 = 3;
    public static final byte FLOAT32 = 4;
    /**
     * From MapFile reference:
     * The name to use use in the IMAGETYPE keyword of the map file to select this output format.(optional)
     */
    private String name;
    /**
     * From MapFile reference:
     * The name of the driver to use to generate this output format. 
     * Some driver names include the definition of the format if the driver supports multiple formats. 
     * For GD the possible driver names are "GD/Gif", "GD/PNG", "GD/WBMP" and "GD/JPEG".
     * For flash the driver is just called "SWF". 
     * For output through GDAL the GDAL shortname for the format is appeneded, such as "GDAL/GTiff". 
     * Note that PNG, JPEG and GIF output can be generated with either GDAL or GD (GD is generally more efficient).(manditory)
     */
    private String driver;
    /**
     * From MapFile reference:
     * Selects the imaging mode in which the output is generated. 
     * Does matter for non-raster formats like Flash. 
     * Not all formats support all combinations. 
     * For instance GD/GIF supports only PC256. (optional)
     * <ul>
     * <li>PC256: Produced a pseudocolored result with up to 256 colors in the palette (traditional MapServer mode)</li>
     * <li>RGB: Render in 24bit Red/Green/Blue mode. Supports all colors but does not support transparency.</li>
     * <li>RGBA: Render in 32bit Red/Green/Blue/Alpha mode. Supports all colors, and alpha based transparency.</li>
     * <li>BYTE: Render raw 8bit pixel values (no presentation). Only works for RASTER layers (through GDAL) and WMS layers currently.</li>
     * <li>INT16: Render raw 16bit signed pixel values (no presentation). Only works for RASTER layers (through GDAL) and WMS layers currently.</li>
     * <li>FLOAT32: Render raw 32bit floating point pixel values (no presentation). Only works for RASTER layers (through GDAL) and WMS layers currently.</li>
     * </ul>
     */
    private Byte imageMode;
    /**
     * From MapFile reference:
     * Provide the mime type to be used when returning results over the web. (optional)
     */
    private String mimeType;
    /**
     * From MapFile reference:
     * Provide the extension to use when creating files of this type. (optional)
     */
    private String extension;
    /**
     * From MapFile reference:
     * Indicates whether transparency should be enabled for this format. 
     * Note that transparency does not work for IMAGEMODE RGB output. 
     * Not all formats support transparency (optional). 
     * When transparency is enabled for the typical case of 8-bit pseudocolored map generation, 
     * the IMAGECOLOR color will be marked as transparent in the output file palette. 
     * Any other map components drawn in this color will also be transparent, 
     * so for map generation with transparency it is best to use an otherwise unused color as the background color.
     */
    private Boolean transparent;
    /**
     * From MapFile reference:
     * Provides a driver or format specific option. 
     * Zero or more FORMATOPTION statement may be present within a OUTPUTFORMAT declaration. (optional)
     */
    private String formatOption;

    /** Empty constructor */
    public OutputFormat() {
        this(null, "GD/Gif", null, null, null, null, null);
    }

    /** Creates a new instance of Join */
    public OutputFormat(String name_, String driver_, Byte imageMode_, String mimeType_, String extension_, Boolean transparent_, String formatOption_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.name = name_;
        this.driver = driver_;
        this.imageMode = imageMode_;
        this.mimeType = mimeType_;
        this.extension = extension_;
        this.transparent = transparent_;
        this.formatOption = formatOption_;
    }

    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
        boolean result = true;

        boolean isName = false;

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
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid syntax for NAME: " + line);
                        return false;
                    }
                    setName(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("DRIVER")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid syntax for DRIVER: " + line);
                        return false;
                    }
                    setDriver(ConversionUtilities.getValueFromMapfileLine(line));
                    isName = true;
                } else if (tokens[0].equalsIgnoreCase("IMAGEMODE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid syntax for IMAGEMODE: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("PC256")) {
                        imageMode = new Byte(OutputFormat.PC256);
                    } else if (tokens[1].equalsIgnoreCase("RGB")) {
                        imageMode = new Byte(OutputFormat.RGB);
                    } else if (tokens[1].equalsIgnoreCase("RGBA")) {
                        imageMode = new Byte(OutputFormat.RGBA);
                    } else if (tokens[1].equalsIgnoreCase("INT16")) {
                        imageMode = new Byte(OutputFormat.INT16);
                    } else if (tokens[1].equalsIgnoreCase("FLOAT32")) {
                        imageMode = new Byte(OutputFormat.FLOAT32);
                    } else {
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid value for IMAGEMODE: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("MIMETYPE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid syntax for MIMETYPE: " + line);
                        return false;
                    }
                    setMimeType(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("EXTENSION")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid syntax for MAEXTENSIONXINTERVAL: " + line);
                        return false;
                    }
                    setExtension(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("TRANSPARENT")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid syntax for TRANSPARENT: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        setTransparent(new Boolean(true));
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        setTransparent(new Boolean(false));
                    } else {
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid value for TRANSPARENT: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("FORMATOPTION")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("OutputFormat.load: Invalid syntax for FORMATOPTION: " + line);
                        return false;
                    }
                    setFormatOption(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("OutputFormat.load: unknown token: " + line);
                    return false;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
        } catch (Exception e) { // Bad coding, but works...
            logger.warning("OutputFormat.load(). Exception: " + e.getMessage());
            return false;
        }

        if (!isName) {
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
            bw.write("\t outputformat\n");
            if (getName() != null) {
                bw.write("\t\t name " + ConversionUtilities.quotesIfNeeded(name) + "\n");
            }
            if (getDriver() != null) {
                bw.write("\t\t driver " + ConversionUtilities.quotesIfNeeded(getDriver()) + "\n");
            }
            if (getImageMode() != null) {
                bw.write("\t\t imagemode " + OutputFormat.getImageModeAsString(getImageMode().byteValue()) + "\n");

            }
            if (getMimeType() != null) {
                bw.write("\t\t mimetype " + ConversionUtilities.quotesIfNeeded(getMimeType()) + "\n");
            }
            if (getExtension() != null) {
                bw.write("\t\t extension " + ConversionUtilities.quotesIfNeeded(getExtension()) + "\n");
            }
            if (getFormatOption() != null) {
                bw.write("\t\t formatoption " + ConversionUtilities.quotesIfNeeded(getFormatOption()) + "\n");
            }
            if (isTransparent() != null) {
                String t = (isTransparent().booleanValue()) ? "ON" : "OFF";
                bw.write("\t\t transparent " + t + "\n");
            }
            bw.write("\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    public String toString() {
        return "Not yet implemented";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Byte getImageMode() {
        return imageMode.byteValue();
    }

    public void setImageMode(Byte imageMode_) {
        this.imageMode = imageMode_;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public String getFormatOption() {
        return formatOption;
    }

    public void setFormatOption(String formatOption) {
        this.formatOption = formatOption;
    }

    /** 
     * gets the image mode
     * @param mode the image mode as a class constant
     * @return the imageMode as a string, or "PC256" if not a supported mode
     */
    public static String getImageModeAsString(byte mode) {
        switch (mode) {
            case OutputFormat.FLOAT32:
                return "FLOAT32";
            case OutputFormat.INT16:
                return "INT16";
            case OutputFormat.RGB:
                return "RGB";
            case OutputFormat.RGBA:
                return "RGBA";
            default:
                return "PC256";
        }
    }
}

