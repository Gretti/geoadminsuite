/*
 * Projection.java
 *
 * Created on 20 mars 2002, 14:41
 */
package org.geogurus.mapserver.objects;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * To set up projections you must define two projection objects:
 * one for the output image (In the MAP object)
 * and one for each layer (In the LAYER objects) to be projected.
 * Projection objects simply consist of a series of PROJ.4 keywords.
 * Here is an example defining UTM zone 15, NAD83:
 * PROJECTION
 *   "proj=utm"
 *   "ellps=GRS80"
 *   "zone=15"
 *   "north"
 *   "no_defs"
 * END
 * Geographic coordinates are defined as:
 * PROJECTION
 *   "proj=latlong"
 * END
 *
 * @author  Bastien VIALADE
 */
public class Projection extends MapServerObject implements java.io.Serializable {

    private ArrayList attributes;

    /** Creates a new instance of Projection */
    public Projection() {
        this.logger = Logger.getLogger(this.getClass().getName());
        attributes = new ArrayList();
    }

    // Set and get methods
    public boolean addAttribute(String attribute) {
        if (attributes == null) {
            attributes = new ArrayList();
        }
        return attributes.add(attribute);
    }

    public void setAttributes(ArrayList attributes_) {
        attributes = attributes_;
    }

    public ArrayList getAttributes() {
        return attributes;
    }

    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
        boolean result = true;

        attributes.clear();

        try {
            String[] tokens;
            String line;

            while ((line = br.readLine()) != null) {

                // Looking for the first util line
                while ((line.trim().length() == 0) || (line.trim().startsWith("#")) || (line.trim().startsWith("%"))) {
                    line = br.readLine();
                }
                tokens = ConversionUtilities.tokenize(line.trim());
                if (!tokens[0].equalsIgnoreCase("END")) {
                    result = this.addAttribute(line.trim());
                } else {
                    return true;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
        } catch (Exception e) { 
            logger.warning("Projection.load(). Exception: " + e.getMessage());
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
            bw.write("\t projection\n");
            if (attributes != null) {
                for (int i = 0; i < attributes.size(); i++) {
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

