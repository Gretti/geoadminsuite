/*
 * Feature.java
 *
 * Created on 20 mars 2002, 13:09
 */

package org.geogurus.mapserver.objects;
import java.awt.Point;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * Defines inline features. 
 * You can use inline features when it's not possible (or too much trouble) to create a shapefile. 
 * Inline features can also be built via urls or forms. 
 * Starts with the keyword FEATURE and terminates with the keyword END.
 *
 * @author  Bastien VIALADE
 */
public class Feature extends MapServerObject  implements java.io.Serializable {
    
    /** A set of xy pairs terminated with an END, for example:
     * POINTS 1 1 50 50 1 50 1 1 END
     * Note that with POLYGON/POLYLINE layers POINTS must start 
     * and end with the same point (i.e. close the feature). */
    private Points points;
    /** String to use for labelin this feature*/
    private String text;
    
        
    /** Creates a new instance of Feature */
    public Feature() {
        points = new Points();
        text = "";
    }
    
    // Get and set methods
    public boolean addPoint (Point p)        { 
        if (points==null) points = new Points();
        return points.add(p);
    }
    public void setPoints(Points points_) { points = points_;}
    public void setText(String text_)        { text = text_;}
    
    public Points getPointsList()         { return points; }
    public String getText()                  { return text;}
    
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
                while ((line.trim().length()==0)||(line.trim().startsWith("#"))||(line.trim().startsWith("%"))) {
                    line = br.readLine();
                }
                tokens = ConversionUtilities.tokenize(line.trim());
                if (tokens[0].equalsIgnoreCase("TEXT")) {
                    if (tokens.length<2) return false;
                    this.text = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("POINTS")) {
                    points = new Points();
                    result = points.load(tokens,br);
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
            System.out.println(".load(). Exception: " +  e.getMessage());
            e.printStackTrace();
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
            bw.write("\t feature\n");
            if (text!=null)             bw.write("\t\t text "+text+"\n");
            if (points!=null)           points.saveAsMapFile(bw);
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
}
