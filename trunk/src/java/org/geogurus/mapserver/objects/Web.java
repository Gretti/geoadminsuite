/*
 * Web.java
 *
 * Created on 20 mars 2002, 18:38
 */

package org.geogurus.mapserver.objects;

import java.net.URL;
import java.io.File;
import java.util.Vector;
import java.io.BufferedReader;
import org.geogurus.tools.string.ConversionUtilities;


/**
 * Defines how a web interface will operate.
 * Starts with the keyword WEB and terminates with the keyword END.
 *
 * @authorBastien VIALADE
 */
public class Web extends MapServerObject  implements java.io.Serializable {
    
    
    /** URL to forward users to if a query fails.
     * If not defined the value for ERROR is used */
    private String empty;
    /** URL to forward users to if an error occurs.
     * Ugly old MapServer error messages will appear if this is not defined */
    private URL error;
    /** Template to use AFTER anything else is sent.
     * Multiresult query modes only. */
    private File footer;
    /** Template to use BEFORE everything else has been sent.
     * Multiresult query modes only. */
    private File header;
    /** Path to the temporary directory fro writing temporary files and images.
     * Must be writable by the user the web server is running as.
     * Must end with a / or \ depending on your platform. */
    private File imagePath;
    /** Base URL for IMAGEPATH.
     * This is the URL that will take the web browser to IMAGEPATH to get the images */
    private String imageURL;
    /** File to log MapServer activity in.
     * Must be writable by the user the web server is running as.*/
    private File log;
    /** Maximum scale at which this interface is valid.
     * When a user requests a map at a bigger scale,
     * MapServer automatically returns the map at this scale.
     * This effectively prevents user from zooming too far out. */
    private double maxScale;
    /** Template to be used if above the maximum scale for the app,
     * useful for nesting apps. */
    private String maxTemplate;
    /** This keyword allows for arbitrary data to be stored as name value pairs.
     * This is used with OGC WMS to define things such as layer title.
     * It can also allow more flexibility in creating templates,
     * as anything you put in here will be accessible via template tags.
     * Example:
     * METADATA
     *title "My layer title"
     *author "Me!"
     * END */
    private MetaData metadata;
    /** Minimum scale at which this interface is valid.
     * When a user reuqests a map at a smaller scale,
     * MapServer automatically returns the map at this scale.
     * This effectively prevents the user from zooming in too far. */
    private double minScale;
    /** Template to be used if above the minimum scale for the app,
     * useful for nesting apps. */
    private String minTemplate;
    /** Template file or URL to use in presenting the results
     * to the user in an interactive mode (i.e. map generates map and so on ... ) */
    private String template;
    
    /** Empty constructor */
    public Web() {
        this(null, null, null, null, null, null, null, -1.0, -1.0, null, null, null, null);
    }
    
    /** Creates a new instance of Web */
    public Web(String empty_, URL error_, File footer_, File header_, File imagePath_, String imageURL_,
    File log_, double maxScale_, double minScale_, String maxTemplate_,
    String minTemplate_, MetaData metadata_, String template_) {
        empty       = empty_;
        error       = error_;
        footer      = footer_;
        header      = header;
        imagePath   = imagePath_;
        imageURL    = imageURL_;
        log         = log_;
        maxScale    = maxScale_;
        maxTemplate = maxTemplate_;
        metadata    = metadata_;
        minScale    = minScale_;
        minTemplate = minTemplate_;
        template    = template_;
    }
    
    // Set and get methods
    public void setEmpty(String empty_)             {empty = empty_;}
    public void setError(URL error_)                {error = error_;}
    public void setFooter(File footer_)             {footer = footer_;}
    public void setHeader(File header_)             {header = header;}
    public void setImagePath(File imagePath_)       {imagePath = imagePath_;}
    public void setImageURL(String imageURL_)       {imageURL = imageURL_;}
    public void setLog(File log_)                   {log = log_;}
    public void setMaxScale(double maxScale_)       {maxScale = maxScale_;}
    public void setMaxTemplate(String maxTemplate_) {maxTemplate = maxTemplate_;}
    public void setMinScale(double minScale_)       {minScale = minScale_;}
    public void setMinTemplate(String minTemplate_) {minTemplate = minTemplate_;}
    public void setTemplate(String template_)       {template = template_;}
    public void setMetaData(MetaData metadata_)     {metadata = metadata_;}
    
    public String getEmpty()        {return empty;}
    public URL getError()           {return error;}
    public File getFooter()         {return footer;}
    public File getHeader()         {return header;}
    public File getImagePath()      {return imagePath;}
    public String getImageURL()     {return imageURL;}
    public File getLog()            {return log ;}
    public double getMaxScale()     {return maxScale;}
    public String getMaxTemplate()  {return maxTemplate;}
    public MetaData getMetaData(){return metadata;}
    public double getMinScale()     {return minScale;}
    public String getMinTemplate()  {return minTemplate;}
    public String getTemplate()     {return template;}
    
    
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
                if (tokens[0].equalsIgnoreCase("IMAGEPATH")) {
                    if (tokens.length<2) return false;
                    String imagePathString = ConversionUtilities.getValueFromMapfileLine(line);
                    imagePath = new File(imagePathString);
                } else if (tokens[0].equalsIgnoreCase("EMPTY")) {
                    if (tokens.length<2) return false;
                    empty = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("ERROR")) {
                    if (tokens.length<2) return false;
                    error = new URL(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("FOOTER")) {
                    if (tokens.length<2) return false;
                    footer = new File(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("HEADER")) {
                    if (tokens.length<2) return false;
                    header = new File(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("LOG")) {
                    if (tokens.length<2) return false;
                    log = new File(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("MAXSCALE")) {
                    if (tokens.length<2) return false;
                    maxScale = new Double(tokens[1]).doubleValue();
                } else if (tokens[0].equalsIgnoreCase("MINSCALE")) {
                    if (tokens.length<2) return false;
                    minScale = new Double(tokens[1]).doubleValue();
                } else if (tokens[0].equalsIgnoreCase("IMAGEURL")) {
                    if (tokens.length<2) return false;
                    imageURL = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("TEMPLATE")) {
                    if (tokens.length<2) return false;
                    template = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("MAXTEMPLATE")) {
                    if (tokens.length<2) return false;
                    maxTemplate = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("MINTEMPLATE")) {
                    if (tokens.length<2) return false;
                    minTemplate = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("METADATA")) {
                    if (metadata == null) {
                        metadata = new MetaData();
                    }
                    result = metadata.load(br);
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true ;
                } else return false;
                
                // Stop parse file if error detected
                if (!result) return false;
            }
        } catch (Exception e) {
            System.out.println("Web.load. Exception: " +  e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }
    
    
    /**  Saves WEB object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t web\n");
            if (imagePath!=null)            bw.write("\t\t imagepath "+'"'+imagePath.getPath().replace('\\','/')+'/'+'"'+"\n");
            if (imageURL!=null)             bw.write("\t\t imageURL "+'"'+imageURL+'"'+"\n");
            if (template!=null)             bw.write("\t\t template "+'"'+template+'"'+"\n");
            if (minTemplate!=null)          bw.write("\t\t mintemplate "+'"'+minTemplate+'"'+"\n");
            if (maxTemplate!=null)          bw.write("\t\t maxtemplate "+'"'+maxTemplate+'"'+"\n");
            if (empty!=null)                bw.write("\t\t empty "+'"'+empty+'"'+"\n");
            if (error!=null)                bw.write("\t\t error "+'"'+error.toString()+'"'+"\n");
            if (footer!=null)               bw.write("\t\t footer "+'"'+footer.getPath().replace('\\','/')+'/'+'"'+"\n");
            if (header!=null)               bw.write("\t\t header "+'"'+header.getPath().replace('\\','/')+'/'+'"'+"\n");
            if (log!=null)                  bw.write("\t\t log "+'"'+log.getPath().replace('\\','/')+'/'+'"'+"\n");
            if (maxScale>0)                 bw.write("\t\t maxscale "+maxScale+"\n");
            if (minScale>0)                 bw.write("\t\t minscale "+minScale+"\n");
            if (metadata != null) result = metadata.saveAsMapFile(bw);
            bw.write("\t\t end\n");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }
    
    
    /** Returns a string representation of the WEB Object
     * @return a string representation of the WEB Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("WEB OBJECT ");
            if (imagePath!=null)
                buffer.append("\n* WEB imagePath          = ").append(imagePath.getPath());
            if (imageURL!=null)
                buffer.append("\n* WEB imageURL           = ").append(imageURL);
            if (template!=null)
                buffer.append("\n* WEB template           = ").append(template);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY WEB OBJECT\n\n"+ex;
        }
        return buffer.toString();
    }
    
    
    
    
    
}

