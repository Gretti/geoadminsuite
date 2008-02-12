/*
 * Points.java
 *
 * Created on 22 mars 2002, 10:21
 */

package org.geogurus.mapserver.objects;
import java.awt.Point;
import org.geogurus.tools.string.ConversionUtilities;
import java.io.BufferedReader;

/**
 *  Defines Points data structure.
 *  Used by Symbol object
 *
 * @author  Bastien VIALADE
 */

public class Points extends java.util.ArrayList  implements java.io.Serializable {
    
    /** Creates a new instance of Points */
    public Points() {
        super();
    }
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(String[] tokens, BufferedReader br) {
        try {
            Point p;
            
            //
            // All points could be on the same line
            // So POINTS is the first keyword and END the last one
            //
            if (tokens.length > 2) {
                if (tokens[tokens.length - 1].trim().equalsIgnoreCase("END")) {
                    if (tokens.length % 2 == 0) {
                        for (int i = 1; i< tokens.length - 2; i = i + 2) {
                            p = new Point();
                            p.x = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[i]));
                            p.y = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[i + 1]));
                            this.add(p);
                        }
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
            
            // Otherwise there is several Points to load
            else {
                String line;
                while ((line = br.readLine()) != null) {
                    // Looking for the first util line
                    while ((line.trim().length()==0)||(line.trim().startsWith("#"))||(line.trim().startsWith("%"))) {
                        line = br.readLine();
                    }
                    tokens = ConversionUtilities.tokenize(line.trim());
                    if (tokens[0].equalsIgnoreCase("END")) return true ;
                    if (tokens.length!=2) return false;
                    p = new Point();
                    p.x = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[0]));
                    p.y = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    add(p) ;
                }
            }
        } catch (Exception e) {
            System.err.println("Points load Error: "+e);
            return false;
        }
        return true;
    }
    
    /**  Saves POINTS object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        StringBuffer buffer = new StringBuffer();
        try {
            Point p;
            buffer.append("\t\t POINTS");
            if (this.size()==1) {
                p = (Point)this.get(0);
                buffer.append(" "+p.x+" "+p.y+" END\n");
                bw.write(buffer.toString());
                return true;
            }
            for (int i=0; i<this.size(); i++) {
                p = (Point)this.get(i);
                buffer.append("\n\t\t\t "+p.x+" "+p.y);
            }
            buffer.append("\n\t\t END\n");
            bw.write(buffer.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    
    
    
    /** Returns a string representation of the POINTS Object
     * @return a string representation of the POINTS Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("POINTS OBJECT ");
            for (int i=0; i<this.size(); i++) {
                buffer.append("\n* POINT ").append(i).append("     = ").append(((Point)this.get(i)).toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY POINTS OBJECT\n\n"+ex;
        }
        return buffer.toString();
    }
}
