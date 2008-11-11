/*
 * Label.java
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
public class Label extends MapServerObject implements java.io.Serializable {

    // Constant for Auto feature size and position
    public static final int AUTO = -1000;
    public static final int UNDEF = -1001;
    // Constants for defined position
    public static final int UL = 1;
    public static final int UC = 2;
    public static final int UR = 3;
    public static final int CL = 4;
    public static final int CC = 5;
    public static final int CR = 6;
    public static final int LL = 7;
    public static final int LC = 8;
    public static final int LR = 9;
    // Constants for predifined sizes
    public static final int TINY = -1;
    public static final int SMALL = -2;
    public static final int MEDIUM = -3;
    public static final int LARGE = -4;
    public static final int GIANT = -5;
    // Constants for font type
    public static final int BITMAP = -6;
    public static final int TRUETYPE = -7;
    
    //constants for text alignment
    public static final int LEFT=-8;
    public static final int RIGHT=-9;
    public static final int CENTER=-10;
    
    /* label alignment (left,right, center) 
     * requires ms>=5.3)
     */
    private Integer align;
    /** Angle, given in degrees, to draw the label
     * or AUTO to allow the software to compute the angle,
     * AUTO is valid for LINE layers only. */
    private double angle;
    /** Should text be antialiased?
     * Note that this requires more available colors and results
     * in slightly larger output images. */
    private boolean antialias;
    /** Color to draw the background rectangle (i.e. billboard). */
    private RGB backgroundColor;
    /** Color to draw a background rectangle /i.e. billboard) shadow */
    private RGB backgroundShadowColor;
    /** How far should the background rectangle be offset? */
    private Dimension backgroundShadowSize;
    /** Padding, in pixels, around labels.
     * Useful for maintaining spacing around text to enhance readability.
     * Available only for cached labels. */
    private int buffer;
    /* Color to draw the text with */
    private RGB color;
    /**
     * Supported encoding format to be used for labels. 
     * If the format is not supported, the label will not be drawn. 
     * Requires the iconv library (present on most systems). 
     * The library is always detected if present on the system, but if not the label will not be drawn.
     * Required for displaying international characters in MapServer. 
     * More information can be found at: http://www.foss4g.org/FOSS4G/MAPSERVER/mpsnf-i18n-en.html.
     */
    private  String encoding;
    /** Font alias as defined in the FONTSET) to use for labeling */
    private String font;
    /** Forces labels for a particular class on, regardless of collisions.
     * Available only for cached labels. */
    private boolean force;
    /** maximum line length when wrapping labels */
    private Integer maxLength;
    /** Maximum font size to use when scaling text (pixels). */
    private int maxSize;
    /** Minimum distance between duplicate labels. Given in pixels */
    private int minDistance;
    /** Minimum size a feature must be to be labeled.
     * Given in pixels.
     * For line data the overall length of the displayed line is used,
     * for polygons features the smallest dimension of the bounding box is used.
     * "Auto" keyword tells MapServer to only label features that are larger than their corresponding label.
     * Available for cached labels only. */
    private int minFeatureSize;
    /** Minimum font size to use when scaling text (pixels). */
    private int minSize;
    /** Offset values for labels, relative to the lower left hand corner
     * of the label and the label point.
     * Given in pixels.
     * In the case of rotated text specify the values as if all labels
     * are horizontal and any rotation will be compensated for. */
    private Dimension offset;
    /** Color to draw a one pixel outline around the text */
    private RGB outlineColor;
    /** Width of outline around text. >1 gives a soft halo-like outline */
    private Integer outlineWidth;
    /** Can text run off the edge of the map? */
    private boolean partials;
    /** Position of the label relative to the labeling point (layers only).
     * First letter is "Y" position,
     * second letter is "X" position.
     * "Auto" tells MapServer to calculate a label position
     * that will not interfere with other labels.
     * With points and polygons, MapServer selects from the 8 outer positions (i.e. excluding cc).
     * With lines, it only uses lc or uc, until it finds a position
     * that doesn't collide with labels that have already been drawn.
     * If all positions cause a conflict, then the label is not drawn (Unless the label's FORCE a parameter is set to "true").
     * "Auto" placement is only available with cached labels. */
    private int position;
    /**
     * The priority parameter (added in v5.0) takes an integer value between 1 (lowest) and 10 (highest). 
     * The default value is 1. 
     * It is also possible to bind the priority to an attribute (item_name) 
     * using square brackets around the [item_name]. e.g. "PRIORITY [someattribute]"
     * Labels are stored in the label cache and rendered in order of priority, 
     * with the highest priority levels rendered first. 
     * Specifying an out of range PRIORITY value inside a map file will result in a parsing error. 
     * An out of range value set via MapScript or coming from a shape attribute 
     * will be clamped to the min/max values at rendering time. 
     * There is no expected impact on performance for using label priorities
     */
    private String priority;
    /** Color to drop shadow */
    private RGB shadowColor;
    /** Shadow offset in pixels */
    private Dimension shadowSize;
    /** Text size.
     * Use "integer" to give the size in pixels of your TrueType font based label,
     * or any of the other 5 listed keywords to bitmap fonts. */
    private int size;
    /** Type of font to use.
     * Generally bitmap fonts are faster to draw then TrueType fonts.
     * However, TrueType fonts are scalable and available in a variety of faces.
     * Be sure to set the FONT parameter if you select TrueType. */
    private int type;
    /** Character that represents an end-of-line condition in label text,
     * thus resulting in a multi-line label. */
    private String wrap;

    /** Empty constructor */
    public Label() {
        this(UNDEF, false, new RGB(0, 0, 0), null, null, 0, null, null, Label.CC, null, null, null, Label.SMALL, BITMAP, null);
    }

    /** Creates a new instance of Label */
    public Label(int angle_, boolean antialias_, RGB color_, String encoding_, String font_,
            int minDistance_, Dimension offset_, RGB outlineColor_, int position_, String priority_,
            RGB shadowColor_, Dimension shadowSize_, int size_, int type_, String wrap_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        align=null;
        angle = angle_;
        antialias = antialias_;
        buffer = -1;
        color = color_;
        encoding = encoding_;
        font = font_;
        force = false;
        maxSize = 256;
        maxLength=null;
        minDistance = minDistance_;
        minFeatureSize = UNDEF;
        offset = offset_;
        outlineColor = outlineColor_;
        outlineWidth=null;
        partials = true;
        position = position_;
        priority = priority_;
        shadowColor = shadowColor_;
        shadowSize = shadowSize_;
        minSize = 4;
        size = size_;
        type = type_;
        wrap = wrap_;
    }
    public Integer getAlign() {
        return align;
    }
    
    public void setAlign(Integer align) {
        this.align=align;
    }
    
    public Integer getOutlineWidth() {
        return outlineWidth;
    }

    public void setOutlineWidth(Integer outlineWidth) {
        this.outlineWidth = outlineWidth;
    }

    // set methods
    public void setAngle(double angle_) {
        angle = angle_;
    }

    public void setAntialias(boolean antialias_) {
        antialias = antialias_;
    }

    public void setBackgroundColor(RGB backgroundColor_) {
        backgroundColor = backgroundColor_;
    }

    public void setBackgroundShadowColor(RGB backgroundShadowColor_) {
        backgroundShadowColor = backgroundShadowColor_;
    }

    public void setBackgroundShadowSize(Dimension backgroundShadowSize_) {
        backgroundShadowSize = backgroundShadowSize_;
    }

    public void setBuffer(int buffer_) {
        buffer = buffer_;
    }

    public void setColor(RGB color_) {
        color = color_;
    }

    public void setFont(String font_) {
        font = font_;
    }

    public void setForce(boolean force_) {
        force = force_;
    }

    public void setMaxSize(int maxSize_) {
        maxSize = maxSize_;
    }

    public void setMinDistance(int minDistance_) {
        minDistance = minDistance_;
    }

    public void setMinFeatureSize(int minFeatureSize_) {
        minFeatureSize = minFeatureSize_;
    }

    public void setMinSize(int minSize_) {
        minSize = minSize_;
    }

    public void setOffset(Dimension offset_) {
        offset = offset_;
    }

    public void setOutlineColor(RGB outlineColor_) {
        outlineColor = outlineColor_;
    }

    public void setPartials(boolean partials_) {
        partials = partials_;
    }

    public void setPosition(int position_) {
        position = position_;
    }

    public void setShadowColor(RGB shadowColor_) {
        shadowColor = shadowColor_;
    }

    public void setShadowSize(Dimension shadowSize_) {
        shadowSize = shadowSize_;
    }

    public void setSize(int size_) {
        size = size_;
    }

    public void setType(int type_) {
        type = type_;
    }

    public void setWrap(String wrap_) {
        wrap = wrap_;
    }

    // get methods
    public double getAngle() {
        return angle;
    }

    public boolean isAntialias() {
        return antialias;
    }

    public RGB getBackgroundColor() {
        return backgroundColor;
    }

    public RGB getBackgroundShadowColor() {
        return backgroundShadowColor;
    }

    public Dimension getBackgroundShadowSize() {
        return backgroundShadowSize;
    }

    public int getBuffer() {
        return buffer;
    }

    public RGB getColor() {
        return color;
    }

    public String getFont() {
        return font;
    }

    public boolean isForce() {
        return force;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public int getMinFeatureSize() {
        return minFeatureSize;
    }

    public int getMinSize() {
        return minSize;
    }

    public Dimension getOffset() {
        return offset;
    }

    public RGB getOutlineColor() {
        return outlineColor;
    }

    public boolean isPartials() {
        return partials;
    }

    public int getPosition() {
        return position;
    }

    public RGB getShadowColor() {
        return shadowColor;
    }

    public Dimension getShadowSize() {
        return shadowSize;
    }

    public int getSize() {
        return size;
    }

    public int getType() {
        return type;
    }

    public String getWrap() {
        return wrap;
    }
    
    public Integer getMaxLength() {
        return maxLength;
    }
    
    public void setMaxLength(Integer maxLength) {
        this.maxLength=maxLength;
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
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for ALIGN: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.getValueFromMapfileLine(line);
                    if (tokens[1].equalsIgnoreCase("LEFT")) {
                        this.align = LEFT;
                    } else if (tokens[1].equalsIgnoreCase("RIGHT")) {
                        this.align = RIGHT;
                    } else if (tokens[1].equalsIgnoreCase("CENTER")) {
                        this.align = CENTER;
                    } 
                } else if (tokens[0].equalsIgnoreCase("ANGLE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for ANGLE: " + line);
                        return false;
                    }
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("AUTO")) {
                        this.angle = AUTO;
                    } else {
                        this.angle = new Double(ConversionUtilities.getValueFromMapfileLine(line)).doubleValue();
                    }
                } else if (tokens[0].equalsIgnoreCase("ANTIALIAS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for ANTIALIAS: " + line);
                        return false;
                    }
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        this.antialias = true;
                    } else {
                        this.antialias = false;
                    }
                } else if (tokens[0].equalsIgnoreCase("BACKGROUNDCOLOR")) {
                    backgroundColor = new RGB();
                    result = backgroundColor.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("Label.load: cannot load BACKGROUNDCOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("BACKGROUNDSHADOWCOLOR")) {
                    backgroundShadowColor = new RGB();
                    result = backgroundShadowColor.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("Label.load: cannot load BACKGROUNDSHADOWCOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("BACKGROUNDSHADOWSIZE")) {
                    if (tokens.length < 3) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for BACKGROUNDSHADOWSIZE: " + line);
                        return false;
                    }
                    backgroundShadowSize = new Dimension();
                    backgroundShadowSize.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    backgroundShadowSize.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                } else if (tokens[0].equalsIgnoreCase("BUFFER")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for BUFFER: " + line);
                        return false;
                    }
                    this.buffer = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("COLOR")) {
                    color = new RGB();
                    result = color.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("Label.load: cannot load COLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("ENCODING")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for ENCODING: " + line);
                        return false;
                    }
                    this.encoding = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("FONT")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for FONT: " + line);
                        return false;
                    }
                    this.font = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("FORCE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for FORCE: " + line);
                        return false;
                    }
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        this.force = true;
                    } else {
                        this.force = false;
                    }
                } else if (tokens[0].equalsIgnoreCase("MAXSIZE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for TMAXSIZEEXT: " + line);
                        return false;
                    }
                    maxSize = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MAXLENGTH")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for MAXLENGTH: " + line);
                        return false;
                    }
                    maxLength = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MINDISTANCE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for MINDISTANCE: " + line);
                        return false;
                    }
                    minDistance = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MINFEATURESIZE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for MINFEATURESIZE: " + line);
                        return false;
                    }
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("AUTO")) {
                        this.minFeatureSize = AUTO;
                    } else {
                        this.minFeatureSize = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                    }
                } else if (tokens[0].equalsIgnoreCase("MINSIZE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for MINSIZE: " + line);
                        return false;
                    }
                    minSize = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("OFFSET")) {
                    if (tokens.length < 3) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for OFFSET: " + line);
                        return false;
                    }
                    offset = new Dimension();
                    offset.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    offset.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                } else if (tokens[0].equalsIgnoreCase("OUTLINECOLOR")) {
                    outlineColor = new RGB();
                    result = outlineColor.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("Label.load: cannot load OUTLINECOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("OUTLINEWIDTH")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for OUTLINEWIDTH: " + line);
                        return false;
                    }
                    outlineWidth = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("PARTIALS")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for PARTIALS: " + line);
                        return false;
                    }
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        this.partials = true;
                    } else {
                        this.partials = false;
                    }
                } else if (tokens[0].equalsIgnoreCase("POSITION")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for POSITION: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.getValueFromMapfileLine(line);
                    if (tokens[1].equalsIgnoreCase("UL")) {
                        position = this.UL;
                    } else if (tokens[1].equalsIgnoreCase("UC")) {
                        position = this.UC;
                    } else if (tokens[1].equalsIgnoreCase("UR")) {
                        position = this.UR;
                    } else if (tokens[1].equalsIgnoreCase("CL")) {
                        position = this.CL;
                    } else if (tokens[1].equalsIgnoreCase("CC")) {
                        position = this.CC;
                    } else if (tokens[1].equalsIgnoreCase("CR")) {
                        position = this.CR;
                    } else if (tokens[1].equalsIgnoreCase("LL")) {
                        position = this.LL;
                    } else if (tokens[1].equalsIgnoreCase("LC")) {
                        position = this.LC;
                    } else if (tokens[1].equalsIgnoreCase("LR")) {
                        position = this.LR;
                    } else if (tokens[1].equalsIgnoreCase("AUTO")) {
                        position = this.AUTO;
                    } else {
                        MapServerObject.setErrorMessage("Label.load: Invalid value for POSITION: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("PRIORITY")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for PRIORITY: " + line);
                        return false;
                    }
                    // todo: handle int / [item] values correctly
                    priority = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("SHADOWCOLOR")) {
                    shadowColor = new RGB();
                    result = shadowColor.load(tokens);
                    if (!result) {
                        MapServerObject.setErrorMessage("Label.load: cannot load SHADOWCOLOR object");
                    }
                } else if (tokens[0].equalsIgnoreCase("SHADOWSIZE")) {
                    if (tokens.length < 3) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for SHADOWSIZE: " + line);
                        return false;
                    }
                    shadowSize = new Dimension();
                    shadowSize.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    shadowSize.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                } else if (tokens[0].equalsIgnoreCase("SIZE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for SIZE: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.getValueFromMapfileLine(line);
                    if (tokens[1].equalsIgnoreCase("TINY")) {
                        size = this.TINY;
                    } else if (tokens[1].equalsIgnoreCase("SMALL")) {
                        size = this.SMALL;
                    } else if (tokens[1].equalsIgnoreCase("MEDIUM")) {
                        size = this.MEDIUM;
                    } else if (tokens[1].equalsIgnoreCase("LARGE")) {
                        size = this.LARGE;
                    } else if (tokens[1].equalsIgnoreCase("GIANT")) {
                        size = this.GIANT;
                    } else {
                        size = Integer.parseInt(tokens[1]);
                    }
                } else if (tokens[0].equalsIgnoreCase("TYPE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for TYPE: " + line);
                        return false;
                    }
                    tokens[1] = ConversionUtilities.getValueFromMapfileLine(line);
                    if (tokens[1].equalsIgnoreCase("BITMAP")) {
                        type = this.BITMAP;
                    } else if (tokens[1].equalsIgnoreCase("TRUETYPE")) {
                        type = this.TRUETYPE;
                    } else {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for TRUETYPE: " + line);
                        return false;
                    }
                }
                else if (tokens[0].equalsIgnoreCase("WRAP")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Label.load: Invalid syntax for WRAP: " + line);
                        return false;
                    }
                    wrap = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("Label.load: unknown token: " + line);
                    return false;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
        } catch (Exception e) { // Bad coding, but works...
            logger.warning("Label.load(). Exception: " + e.getMessage());
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
            bw.write("\t\t label\n");
            if(align!=null) {
                switch(align) {
                    case RIGHT:
                        bw.write("\t\t\t align RIGHT\n");
                        break;
                    case CENTER:
                        bw.write("\t\t\t align CENTER\n");
                        break;
                }
            }
            if (angle == AUTO) {
                bw.write("\t\t\t angle AUTO\n");
            } else if (angle == UNDEF) {
            } else {
                bw.write("\t\t\t angle " + angle + "\n");
            }
            if (antialias == true) {
                bw.write("\t\t\t antialias TRUE" + "\n");
            } else {
                bw.write("\t\t\t antialias FALSE" + "\n");
            }
            if (backgroundColor != null) {
                bw.write("\t\t\t backgroundcolor ");
                backgroundColor.saveAsMapFile(bw);
            }
            if (backgroundShadowColor != null) {
                bw.write("\t\t\t backgroundshadowcolor ");
                backgroundShadowColor.saveAsMapFile(bw);
            }
            if (backgroundShadowSize != null) {
                bw.write("\t\t\t backgroundshadowsize " + backgroundShadowSize.width + " " + backgroundShadowSize.height + "\n");
            }

            bw.write(buffer > 0 ? "\t\t\t buffer " + buffer + "\n" : "");

            if (color != null) {
                bw.write("\t\t\t color ");
                color.saveAsMapFile(bw);
            }
            if (encoding != null) {
                bw.write("\t\t\t encoding " + encoding + "\n");
            }
            if (font != null) {
                bw.write("\t\t\t font " + font + "\n");
            }
            if (force == true) {
                bw.write("\t\t\t force TRUE");
            }

            bw.write("\t\t\t maxsize " + maxSize + "\n");
            if(maxLength!=null) {
                bw.write("\t\t\t maxlength " + maxLength + "\n");
            }
            if (minDistance > 0) {
                bw.write("\t\t\t mindistance " + minDistance + "\n");
            }
            switch (minFeatureSize) {
                case AUTO:
                    bw.write("\t\t\t minfeaturesize AUTO" + "\n");
                    break;
                case UNDEF:
                    break;
                default:
                    bw.write("\t\t\t minfeaturesize " + this.minFeatureSize + "\n");
            }
            bw.write("\t\t\t minsize " + minSize + "\n");
            if (offset != null) {
                bw.write("\t\t\t offset " + offset.width + " " + offset.height + "\n");
            }
            if (outlineColor != null) {
                bw.write("\t\t\t outlinecolor ");
                outlineColor.saveAsMapFile(bw);
            }
            if(outlineWidth!=null)
                bw.write("\t\t\t outlinewidth " + outlineWidth + "\n");
            if (partials == true) {
                bw.write("\t\t\t partials TRUE\n");
            } else {
                bw.write("\t\t\t partials FALSE\n");
            }
            switch (position) {
                case UL:
                    bw.write("\t\t\t position UL\n");
                    break;
                case UC:
                    bw.write("\t\t\t position UC\n");
                    break;
                case UR:
                    bw.write("\t\t\t position UR\n");
                    break;
                case CL:
                    bw.write("\t\t\t position CL\n");
                    break;
                case CC:
                    bw.write("\t\t\t position CC\n");
                    break;
                case CR:
                    bw.write("\t\t\t position CR\n");
                    break;
                case LL:
                    bw.write("\t\t\t position LL\n");
                    break;
                case LC:
                    bw.write("\t\t\t position LC\n");
                    break;
                case LR:
                    bw.write("\t\t\t position LR\n");
                    break;
                case AUTO:
                    bw.write("\t\t\t position AUTO\n");
                    break;
            }
            if (priority != null) {
                bw.write("\t\t\t priority " + priority + "\n");
            }
            if (shadowColor != null) {
                bw.write("\t\t\t shadowcolor ");
                shadowColor.saveAsMapFile(bw);
            }
            if (shadowSize != null) {
                bw.write("\t\t\t shadowsize " + shadowSize.width + " " + shadowSize.height + "\n");
            }
            switch (size) {
                case TINY:
                    bw.write("\t\t\t size TINY\n");
                    break;
                case SMALL:
                    bw.write("\t\t\t size SMALL\n");
                    break;
                case MEDIUM:
                    bw.write("\t\t\t size MEDIUM\n");
                    break;
                case LARGE:
                    bw.write("\t\t\t size LARGE\n");
                    break;
                case GIANT:
                    bw.write("\t\t\t size GIANT\n");
                    break;
                default:
                    bw.write("\t\t\t size " + size + "\n");
            }
            switch (type) {
                case BITMAP:
                    bw.write("\t\t\t type BITMAP\n");
                    break;
                case TRUETYPE:
                    bw.write("\t\t\t type TRUETYPE\n");
                    break;
                default:
                    bw.write("\t\t\t type BITMAP\n");
            }
            if (wrap != null) {
                bw.write("\t\t\t wrap \"" + wrap + "\"\n");
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
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("LABEL OBJECT ");
            if (color != null) {
                buffer.append("\n* LABEL color           = ").append(color.toString());
            }
            if (outlineColor != null) {
                buffer.append("\n* LABEL outlineColor    = ").append(outlineColor.toString());
            }
            buffer.append("\n* LABEL size            = ").append(size);
            buffer.append("\n* LABEL position        = ").append(position);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY LABEL OBJECT\n\n" + ex;
        }
        return buffer.toString();
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
