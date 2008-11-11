/*
 * Join.java
 *
 */
package org.geogurus.mapserver.objects;

import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * Defines a file to be included in the mapfile parsing.
 *
 * @author  Nicolas Ribot
 */
public class Include extends MapServerObject implements java.io.Serializable {

    /** file to join */
    private String include;

    /** Empty constructor */
    public Include() {
        this(null);
    }

    /** Creates a new instance of Join */
    public Include(String include_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        include = include_;
    }

    /** Get methods to get join parameters */
    public String getInclude() {
        return include;
    }

    /** Sets methods*/
    public void setInclude(String include_) {
        include = include_;
    }

    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
        try {
            String[] tokens;
            String line;

            while ((line = br.readLine()) != null) {

                // Looking for the first util line
                while ((line.trim().length() == 0) || (line.trim().startsWith("#")) || (line.trim().startsWith("%"))) {
                    line = br.readLine();
                }
                tokens = ConversionUtilities.tokenize(line.trim());
                if (tokens[0].equalsIgnoreCase("INCLUDE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Include.load: Invalid syntax for INCLUDE: " + line);
                        return false;
                    }
                    include = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("Include.load: unknown token: " + line);
                    return false;
                }
            }
        } catch (Exception e) { // Bad coding, but works...
            logger.warning("Include.load(). Exception: " + e.getMessage());
            return false;
        }

        return true;
    }

    /** Saves data to file
     * using Object parameters with mapFile format.
     * @param bw BufferWriter containing file data to write
     * in linked file.
     * @return true is mapping done correctly
     */
    public boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        // no write at all if property is null
        //Note: should propagate this mechanism to all other objects ?
        if (include == null) {
            return result;
        }
        try {
            bw.write("\t include\n");
            if (include != null) {
                bw.write("\t\t include " + include + "\n");
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

