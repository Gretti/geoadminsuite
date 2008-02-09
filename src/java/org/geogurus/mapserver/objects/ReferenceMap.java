/*
 * ReferenceMap.java
 *
 * Created on 20 mars 2002, 16:52
 */
package org.geogurus.mapserver.objects;
import java.io.File;
import java.awt.Dimension;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * Defines how reference maps are to be created.
 * Starts with the keyword REFERENCE and terminates with the keyword END.
 * Three types of reference maps are supported.
 * The most common would be one showing the extent of a map in an interactive interface.
 * It is also possible to request reference maps as part of a query.
 * Point queries will generate an image with a marker (see below) placed at the query point.
 * Region based queries will depict the extent of the area of interest.
 * Finally, feature based queries will display the selection feature(s) used.
 *
 * @author  Bastien VIALADE
 */
public class ReferenceMap extends MapServerObject  implements java.io.Serializable {
    // Constants for status
    public static final byte ON     = 0;
    public static final byte OFF    = 1;
    
    /** Color in which the reference box is drawn.
     * Set any component to -1 for no fill.
     * Default is red. */
    private RGB color;
    /** The spatial extent of the base reference image. */
    private MSExtent extent;
    /** Full filename of the base reference image.
     * Must be a GIF image. */
    private File image;
    /** Color to use for outlining the reference box.
     * Set any component to -1 for no outline. */
    private RGB outlineColor;
    /** Size, in pixels, of the base reference image. */
    private Dimension size;
    /** Is the reference map to be created? Default it off. */
    private byte status;
    
    // NRI: new attributes since mapserver 3.6:
    /** Defines a symbol (from the symbol file) to use when the box becomes too small 
     * (see MINBOXSIZE and MAXBOXSIZE below). Uses a crosshair by default.
     */
    private String marker;
    /** Defines the size of the symbol to use instead of a box (see MARKER above).
     */
    private int markerSize;
    /** If box is smaller than MINBOXSIZE (use box width or height) then use the 
     * symbol defined by MARKER and MARKERSIZE. 
     */
    private int minBoxSize;
    /** If box is greater than MAXBOXSIZE (use box width or height) then draw nothing 
     * (Often the whole map gets covered when zoomed way out and it's perfectly 
     * obvious where you are).
     */ 
    private int maxBoxSize;
    
    
    /** Empty constructor */
    public ReferenceMap() {
        this(null, null, null, null, null, -1, -1, -1);
    }
       
    
    /** Creates a new instance of ReferenceMap */
    public ReferenceMap(MSExtent extent_, File image_, RGB outlineColor_, Dimension size_,
                        String marker_, int markerSize_, int minBoxSize_, int maxBoxSize_) {
        color = new RGB(255,0,0);
        extent = extent_;
        image = image_;
        outlineColor = outlineColor_;
        size = size_;
        status = this.OFF;
        marker = marker_;
        markerSize = markerSize_;
        minBoxSize = minBoxSize_;
        maxBoxSize = maxBoxSize_;
    }
    
    // Set and get methods
    public void setColor(RGB color_)                {color = color_;}
    public void setExtent(MSExtent extent_)           {extent = extent_;}
    public void setImage(File image_)               {image = image_;}
    public void setOutlineColor(RGB outlineColor_)  {outlineColor = outlineColor_;}
    public void setSize(Dimension size_)            {size = size_;}
    public void setStatus(byte status_)             {status = status_;}
    public void setMarker(String marker_)           {marker = marker_;}
    public void setMarkerSize(int markerSize_)      {markerSize = markerSize_;}
    public void setMinBoxSize(int minBoxSize_)      {minBoxSize = minBoxSize_;}
    public void setMaxBoxSize(int maxBoxSize_)      {maxBoxSize = maxBoxSize_;}
    
    public RGB getColor()           {return color;}
    public MSExtent getExtent()       {return extent;}
    public File getImage()          {return image;}
    public RGB getOutlineColor()    {return outlineColor;}
    public Dimension getSize()      {return size;}
    public byte getStatus()         {return status;}
    public String getMarker()       {return marker;}
    public int getMarkerSize()      {return markerSize;}
    public int getMinBoxSize()      {return minBoxSize;}
    public int getMaxBoxSize()      {return maxBoxSize;}
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public synchronized boolean load(java.io.BufferedReader br) {
        boolean result = true;
        
        boolean isExtent = false;
        
        try {
            String[] tokens;
            String line;
            
            while ((line = br.readLine()) != null) {
                
                // Looking for the first util line
                while ((line.trim().length()==0)||(line.trim().startsWith("#"))||(line.trim().startsWith("%"))) {
                    line = br.readLine();
                }
                tokens = ConversionUtilities.tokenize(line.trim());
                if (tokens[0].equalsIgnoreCase("COLOR")) {
                    color = new RGB();
                    result = color.load(tokens);
                }
                else if(tokens[0].equalsIgnoreCase("EXTENT")) {
                    extent = new MSExtent();
                    result = extent.load(tokens);
                    isExtent = true;
                }
                else if(tokens[0].equalsIgnoreCase("IMAGE")) {
                    if (tokens.length<2) return false;
                    String imagePathString = ConversionUtilities.getValueFromMapfileLine(line);
                    image = new File(imagePathString);
                }
                else if (tokens[0].equalsIgnoreCase("MARKER")) {
                    if (tokens.length<2) return false;
                    marker = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("MARKERSIZE")) {
                    if (tokens.length<2) return false;
                    markerSize = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("MINBOXSIZE")) {
                    if (tokens.length<2) return false;
                    minBoxSize = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("MAXBOXSIZE")) {
                    if (tokens.length<2) return false;
                    maxBoxSize = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                }
                else if (tokens[0].equalsIgnoreCase("OUTLINECOLOR")) {
                    outlineColor = new RGB();
                    result = outlineColor.load(tokens);
                }
                else if (tokens[0].equalsIgnoreCase("SIZE")) {
                    if (tokens.length<3) return false;
                    size = new Dimension();
                    size.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    size.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                }
                else if (tokens[0].equalsIgnoreCase("STATUS")) {
                    if (tokens.length<2) return false;
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON")) status = this.ON;
                    else if (tokens[1].equalsIgnoreCase("OFF")) status = this.OFF;
                    else return false;
                }
                else if (tokens[0].equalsIgnoreCase("END")) {
                    return true ;
                }
                else {
                    return false;
                }
                
                // Stop parse file if error detected
                if (!result) return false;
            }
        } catch (Exception e) { // Bad coding, but works...
            System.out.println("ReferenceMap.load(). Exception: " +  e.getMessage());
            e.printStackTrace();
            e.printStackTrace();
            return false;
        }
        
        if (!isExtent) 
            return false;
        
        return result;
    
    }
    
    /** Saves data to file
     * using Object parameters with mapFile format.
     * @param bw BufferWriter containing file data to write
     * in linked file.
     * @return true is mapping done correctly
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t\t reference\n");
            if (color!=null) {
                bw.write("\t\t\t color ");
                color.saveAsMapFile(bw);
            }
            if (extent != null) {
                extent.saveAsMapFile(bw);
            }
            if (image != null) {
                bw.write("\t\t\t image " + ConversionUtilities.quotes(image.getPath().replace('\\','/')) + "\n");
            }
            if (outlineColor!=null) {
                bw.write("\t\t\t outlinecolor ");
                outlineColor.saveAsMapFile(bw);
            }
            if (size != null) {
                bw.write("\t\t\t size " + size.width + " " + size.height + "\n");
            }
            if (marker != null) {
                bw.write("\t\t\t marker " + marker + "\n");
            }
            if (markerSize >= 0) {
                bw.write("\t\t\t markersize " + markerSize + "\n");
            }
            if (minBoxSize >= 0) {
                bw.write("\t\t\t minboxsize " + minBoxSize + "\n");
            }
            if (maxBoxSize >= 0) {
                bw.write("\t\t\t maxboxsize " + maxBoxSize + "\n");
            }
            switch (status) {
                case ON:              bw.write("\t\t\t status ON\n"); break;
                case OFF:             bw.write("\t\t\t status OFF\n"); break;
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
