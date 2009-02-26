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
 * MapClass.java
 *
 * Created on 20 mars 2002, 10:41
 */

package org.geogurus.mapserver.objects;
import java.io.File;
import java.io.BufferedReader;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * Defines thematic classes for a given layer and each layer must have at least one class.
 * In cases with more than one class, membership is determined using attributes values and expressions.
 * Starts with the keyword CLASS and terminates with the keyword END.
 *
 * @author  Bastien VIALADE
 */
public class MapClass extends MapServerObject implements java.io.Serializable {
    /** Color to use for non-transparent symbols */
    private RGB backgroundColor;
    /** Color to use for dawing features */
    private RGB color;
    
    private String expression;
    /** Signals the start of a Join Object */
    private Join join;
    /** Signals the start of a Label Object */
    private Label label;
    /** Maximum size in pixels to draw a symbol. */
    private int maxSize;
    /** Minimum size in pixels to draw a symbol. */
    private int minSize;
    /**Minimum scale at which this class applies.*/
    private double minScale;
    /**Maximum scale at which this class applies.*/
    private double maxScale;
    /** Name to use in legends for this class.
     * If not set class won't show up in legend. */
    private String name;
    /** Color to use for outlining polygons and certain marker symbols.
     * Line symbols do not support outline colors. */
    private RGB outlineColor;
    /** Height, in pixels, of the symbol/pattern to be used.
     * Only useful with scalable symbols.   */
    private int size;
    /** The symbol name or number to use for all features if attribute tables are not used.
     * The number is the index of the symbol in the symbol file, starting at 1,
     * the 5th symbol in the file is therefore symbol number 5.
     * You can also give your symbols names using the NAME keyword in the symbol definition file,
     * and use those to refer to them.
     * Default is 0, which results in a single pixel, single width line,
     * or solid polygon fill, depending on layer type. */
    private String symbol;
    /** Template file or URL to use in presenting query results to the user. */
    private File template;
    /** Static text to label features in this class with.
     * This overrides values obtained from the LABELTIEM.
     * The string may be given as an expression delimited using the ()'s.
     * This allows you to concatenate multiple attributes into a single label.
     * For example: ([FIRSTNAME],[LASTNAME]).*/
    private String text;
    /** Symbol definition */
    private RGB overlayBackgroundColor;
    private RGB overlayColor;
    private RGB overlayOutlineColor;
    private int overlaySize;
    private int overlayMinSize;
    private int overlayMaxSize;
    private String overlaySymbol;
    
    
    /** Empty constructor */
    public MapClass() {
        this(null, null, null, null, null, null, null, null, null);
    }
    
    
    /** Creates a new instance of Class */
    public MapClass(RGB backgroundColor_, RGB color_, String expression_, Join join_, 
                    Label label_, String name_, RGB outlineColor_, File template_, String text_) {
        backgroundColor = backgroundColor_;
        color = color_;
        expression = expression_;
        join = join_;
        label = label_;
        maxSize = 0;
        minSize = 0;
        minScale = 0;
        maxScale = 0;
        name = name_;
        outlineColor = outlineColor_;
        size = 0;
        symbol = null;
        template = template_;
        text = text_;
        overlayMaxSize=0;
        overlayMinSize=0;
        overlaySize=0;
        overlayBackgroundColor=null;
        overlayColor=null;
        overlaySymbol=null;
        overlayOutlineColor=null;
    }
    
    
    // Set methods
    public void setBackgroundColor(RGB backgroundColor_)    {backgroundColor = backgroundColor_;}
    public void setColor(RGB color_)                        {color = color_;}
    public void setExpression(String expression_)           {expression = expression_;}
    public void setJoin(Join join_)                         {join = join_;}
    public void setLabel(Label label_)                      {label = label_;}
    public void setMaxSize(int maxSize_)                    {maxSize = maxSize_;}
    public void setMinSize(int minSize_)                    {minSize = minSize_;}
    public void setMaxScale(double maxScale_)               {maxScale = maxScale_;}
    public void setMinScale(double minScale_)               {minScale = minScale_;}
    public void setName(String name_)                       {name = name_;}
    public void setOutlineColor(RGB outlineColor_)          {outlineColor = outlineColor_;}
    public void setSize(int size_)                          {size = size_;}
    public void setSymbol(String symbol_)                   {symbol = symbol_;}
    public void setTemplate(File template_)                 {template = template_;}
    public void setText(String text_)                       {text = text_;}
    public void setOverlayBackgroundColor(RGB overlayBackgroundColor_){overlayBackgroundColor = overlayBackgroundColor_;}
    public void setOverlayColor(RGB overlayColor_)          {overlayColor = overlayColor_;}
    public void setOverlayOutlineColor(RGB overlayOutlineColor_) {overlayOutlineColor=overlayOutlineColor_;}
    public void setOverlaySize(int overlaySize_)            {overlaySize = overlaySize_;}
    public void setOverlayMaxSize(int overlayMaxSize_)      {overlayMaxSize = overlayMaxSize_;}
    public void setOverlayMinSize(int overlayMinSize_)      {overlayMinSize = overlayMinSize_;}
    public void setOverlaySymbol(String overlaySymbol_)      {overlaySymbol = overlaySymbol_;}
    
    
    // Get methods
    public RGB getBackgroundColor()     { return backgroundColor;}
    public RGB getColor()               { return color;}
    public String getExpression()       { return expression;}
    public Join getJoin()               { return join;}
    public Label getLabel()             { return label;}
    public int getMaxSize()             { return maxSize;}
    public int getMinSize()             { return minSize;}
    public double getMaxScale()         { return maxScale;}
    public double getMinScale()         { return minScale;}
    public String getName()             { return name;}
    public RGB getOutlineColor()        { return outlineColor;}
    public int getSize()                { return size;}
    public String getSymbol()           { return symbol;}
    public File getTemplate()           { return template;}
    public String getText()             { return text;}
    public RGB getOverlayBackgroundColor()   { return overlayBackgroundColor;}
    public RGB getOverlayColor()        { return overlayColor;}
    public RGB getOverlayOutlineColor() { return overlayOutlineColor;}
    public int getOverlaySize()         { return overlaySize;}
    public int getOverlayMaxSize()      { return overlayMaxSize;}
    public int getOverlayMinSize()      { return overlayMinSize;}
    public String getOverlaySymbol()    { return overlaySymbol;}
    // returns a unique identifier
    public int getID()                  {return System.identityHashCode(this);}
    
    
    
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
                while ((line.trim().length()==0)||(line.trim().startsWith("#"))||(line.trim().startsWith("%"))) {
                    line = br.readLine();
                }
                tokens = ConversionUtilities.tokenize(line.trim());
                
                if (tokens[0].equalsIgnoreCase("BACKGROUNDCOLOR")) {
                    backgroundColor = new RGB();
                    result = backgroundColor.load(tokens);
                }
                else if (tokens[0].equalsIgnoreCase("COLOR")) {
                    color = new RGB();
                    result = color.load(tokens);
                }
                else if (tokens[0].equalsIgnoreCase("EXPRESSION")) {
                    if (tokens.length<2) return false;
                    expression = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("JOIN")) {
                    join = new Join();
                    result = join.load(br);
                }
                else if (tokens[0].equalsIgnoreCase("LABEL")) {
                    label = new Label();
                    result = label.load(br);
                }
                else if (tokens[0].equalsIgnoreCase("MAXSCALE")) {
                    if (tokens.length<2) return false;
                    maxScale = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("MINSCALE")) {
                    if (tokens.length<2) return false;
                    minScale = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("MAXSIZE")) {
                    if (tokens.length<2) return false;
                    maxSize = Integer.parseInt(ConversionUtilities.quotesIfNeeded(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("MINSIZE")) {
                    if (tokens.length<2) return false;
                    minSize = Integer.parseInt(ConversionUtilities.quotesIfNeeded(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("NAME")) {
                    if (tokens.length<2) return false;
                    name = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("OUTLINECOLOR")) {
                    outlineColor = new RGB();
                    result = outlineColor.load(tokens);
                }
                else if (tokens[0].equalsIgnoreCase("SIZE")) {
                    if (tokens.length<2) return false;
                    size = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("SYMBOL")) {
                    if (tokens.length<2) return false;
                    symbol = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("TEXT")) {
                    if (tokens.length<2) return false;
                    text = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("SIZE")) {
                    if (tokens.length<2) return false;
                    size = Integer.parseInt(tokens[1]);
                }
                else if (tokens[0].equalsIgnoreCase("OVERLAYBACKGROUNDCOLOR")) {
                    overlayBackgroundColor = new RGB();
                    result = overlayBackgroundColor.load(tokens);
                }
                else if (tokens[0].equalsIgnoreCase("OVERLAYCOLOR")) {
                    overlayColor = new RGB();
                    result = overlayColor.load(tokens);
                }
                else if (tokens[0].equalsIgnoreCase("OVERLAYOUTLINECOLOR")) {
                    overlayOutlineColor = new RGB();
                    result = overlayOutlineColor.load(tokens);
                }
                else if (tokens[0].equalsIgnoreCase("OVERLAYSIZE")) {
                    if (tokens.length<2) return false;
                    overlaySize = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("OVERLAYMAXSIZE")) {
                    if (tokens.length<2) return false;
                    overlayMaxSize = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("OVERLAYMINSIZE")) {
                    if (tokens.length<2) return false;
                    overlayMinSize = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("OVERLAYSYMBOL")) {
                    if (tokens.length<2) return false;
                    overlaySymbol = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("END")) {
                    return true ;
                }
                else return false;
                
                // Stop parse file if error detected
                if (!result) return false;
            }
        } catch (Exception e) {
            System.out.println("MapClass.load(). Exception: " +  e.getMessage());
            e.printStackTrace();
            e.printStackTrace();
            return false;
        }
        return result;
    }
    
    
    /**  Saves CLASS object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t\t class\n");
            if (name!=null)             bw.write("\t\t\t name "+ConversionUtilities.quotesIfNeeded(name)+"\n");
            if (expression!=null) {
                // quotes expression only if it si not a regular expression or logical expression: begins and ends with /
                // or begin and ends with ()
                if (expression.length() > 0 && ( (expression.charAt(0) == '/' && expression.charAt(expression.length()-1) == '/') ||
                                                 (expression.charAt(0) == '(' && expression.charAt(expression.length()-1) == ')'))) {
                    bw.write("\t\t\t expression "+ expression+"\n");
                } else {
                    bw.write("\t\t\t expression "+ConversionUtilities.quotesIfNeeded(expression)+"\n");
                }
            }
            if (backgroundColor!=null) {
                bw.write("\t\t\t backgroundcolor ");
                backgroundColor.saveAsMapFile(bw);
            }
            if (color!=null) {
                bw.write("\t\t\t color ");
                color.saveAsMapFile(bw);
            }
            if (outlineColor!=null) {
                bw.write("\t\t\t outlinecolor ");
                outlineColor.saveAsMapFile(bw);
            }
            if (symbol != null) {
                bw.write("\t\t\t symbol "+ConversionUtilities.quotesIfNeeded(symbol)+"\n");
            }   
            if (size > 0) {
                bw.write("\t\t\t size "+size+"\n");
            }
            if (minSize > 0) {
                bw.write("\t\t\t minsize "+minSize+"\n");
            }
            if (maxSize > 0) {
                bw.write("\t\t\t maxsize "+maxSize+"\n");
            }
            if (overlayBackgroundColor!=null) {
                bw.write("\t\t\t overlaybackgroundcolor ");
                overlayBackgroundColor.saveAsMapFile(bw);
            }
            if (overlayColor!=null) {
                bw.write("\t\t\t overlaycolor ");
                overlayColor.saveAsMapFile(bw);
            }
            if (overlayOutlineColor!=null) {
                bw.write("\t\t\t overlayoutlinecolor ");
                overlayOutlineColor.saveAsMapFile(bw);
            }
            if (overlaySymbol != null) {
                bw.write("\t\t\t overlaysymbol "+ConversionUtilities.quotesIfNeeded(overlaySymbol)+"\n");
            }   
            if (overlaySize > 0) {
                bw.write("\t\t\t overlaysize "+overlaySize+"\n");
            }
            if (overlayMinSize > 0) {
                bw.write("\t\t\t overlayminsize "+overlayMinSize+"\n");
            }
            if (overlayMaxSize > 0) {
                bw.write("\t\t\t overlaymaxsize "+overlayMaxSize+"\n");
            }
            if (template != null)   bw.write("\t\t template "+ConversionUtilities.quotesIfNeeded(template.getPath().replace('\\','/'))+"\n");
            if (text != null)   bw.write("\t\t text "+ConversionUtilities.quotesIfNeeded(text)+"\n");
            if (label!=null)            label.saveAsMapFile(bw);
            if (join!=null)             join.saveAsMapFile(bw);
            bw.write("\t\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }
    
    /** Returns a string representation of the CLASS Object
     * @return a string representation of the CLASS Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("CLASS OBJECT ");
            if (name!=null)
                buffer.append("\n* CLASS name              = ").append(name);
            if (expression!=null)
                buffer.append("\n* CLASS expression        = ").append(expression);
            if (symbol!=null)
                buffer.append("\n* CLASS symbol            = ").append(symbol);
            if (color!=null)
                buffer.append("\n* CLASS color             = ").append(color.toString());
            buffer.append("\n* CLASS size              = ").append(size);
            if (overlaySymbol!=null)
                buffer.append("\n* CLASS overlaySymbol     = ").append(overlaySymbol);
            buffer.append("\n* CLASS overlaySize       = ").append(overlaySize);
            if (overlayColor!=null)
                buffer.append("\n* CLASS overlayColor      = ").append(overlayColor.toString());
            if (outlineColor!=null)
                buffer.append("\n* CLASS outlineColor      = ").append(outlineColor.toString());
            if (label!=null)
                buffer.append("\n* CLASS label             = ").append(label.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY CLASS OBJECT\n\n"+ex;
        }
        return buffer.toString();
    }
    
    
    
    
    
    
}

