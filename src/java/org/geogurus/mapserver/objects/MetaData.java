/*
 * Join.java
 *
 * Created on 20 mars 2002, 10:46
 */

package org.geogurus.mapserver.objects;

import java.io.File;
import org.geogurus.tools.string.ConversionUtilities;
import java.util.ArrayList;

/**
 * Defines a MetaData object
 * Starts with the keyword METADATA and terminate with the keywrod END.
 *
 * @author  Jerome Gasperi
 */

public class MetaData extends MapServerObject implements java.io.Serializable  {
    
    
    
    /** Text definition */
    private ArrayList attributes;
    
    
    /** Empty constructor */
    public MetaData() {
        this(null);
    }
    
    /** Creates a new instance of MetaData */
    public MetaData(String text) {
        this.addAttribute(text);
    }
    
    // Set and get methods
    public boolean addAttribute(String attribute)      {
        if (attributes==null) attributes=new ArrayList();
        return attributes.add(attribute);
    }
    public void setAttributes(ArrayList attributes_)   {attributes = attributes_;}
    
    public ArrayList getAttributes() {return attributes;}
    
    
    /**
     *
     * Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     *
     */
    public boolean load(java.io.BufferedReader br) {
        boolean result = true;
        
        attributes.clear();
        
        try {
            String[] tokens;
            String line;
            
            while ((line = br.readLine()) != null) {
                
                // Looking for the first util line
                while ((line.trim().length()==0)||(line.trim().startsWith("#"))||(line.trim().startsWith("%"))) {
                    line = br.readLine();
                }
                tokens = ConversionUtilities.tokenize(line.trim());
                if (!tokens[0].equalsIgnoreCase("END")) {
                    result = this.addAttribute(line);
                }
                else {
                    return true;
                }
                
                // Stop parse file if error detected
                if (!result) return false;
            }
        } catch (Exception e) { // Bad coding, but works...
            System.out.println("Metadata.load(). Exception: " +  e.getMessage());
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
            bw.write("\t metadata\n");
            if (attributes!=null) {
                for (int i=0; i<attributes.size(); i++) {
                    String str = (String) attributes.get(i);
                    if (str != null) {
                        bw.write("\t\t " + str + "\n");
                    }
                }
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
}

