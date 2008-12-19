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
 * Style.java
 *
 * Created on 25 juin 2003, 18:51
 */
package org.geogurus.mapserver.objects;
import java.io.BufferedReader;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;
/**
 *  Defines Style data structure.
 *  Used by Symbol object
 *
 * @author  NRI
 */
public class Style extends java.util.ArrayList  implements java.io.Serializable {
    protected transient Logger logger;
    
    /** Creates a new instance of Points */
    public Style() {
        super();
        this.logger = Logger.getLogger(this.getClass().getName());
    }
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(String[] tokens, BufferedReader br) {
        try {
            Integer pix;
            
            //
            // All numbers could be on the same line
            // So STYLE is the first keyword and END the last one
            //
            if (tokens.length > 2) {
                if (tokens[tokens.length - 1].trim().equalsIgnoreCase("END")) {
                    for (int i = 1; i < tokens.length - 1; i++) {
                        pix = new Integer(ConversionUtilities.removeDoubleQuotes(tokens[i]));
                        this.add(pix);
                    }
                }
                else {
                    return false;
                }
            }
            
            // Otherwise there is several values for this style to load
            else {
                String line;
                while ((line = br.readLine()) != null) {
                    // Looking for the first util line
                    while ((line.trim().length()==0)||(line.trim().startsWith("#"))||(line.trim().startsWith("%"))) {
                        line = br.readLine();
                    }
                    tokens = ConversionUtilities.tokenize(line.trim());
                    if (tokens[0].equalsIgnoreCase("END")) return true ;
                    for (int i = 0; i < tokens.length; i++) {
                        pix = new Integer(ConversionUtilities.removeDoubleQuotes(tokens[i]));
                        this.add(pix);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Style load Error: "+e);
            return false;
        }
        return true;
    }
    
    /**  Saves Style object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        StringBuffer buffer = new StringBuffer();
        try {
            Integer pix;
            buffer.append("\t\t STYLE");
            
            for (int i=0; i < this.size(); i++) {
                pix = (Integer)this.get(i);
                buffer.append(" " + pix);
            }
            buffer.append(" END\n");
            bw.write(buffer.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    
    
    
    /** Returns a string representation of the STYLE Object
     * @return a string representation of the STYLE Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("STYLE OBJECT    = ");
            
            for (int i=0; i<this.size(); i++) {
                buffer.append(((Integer)this.get(i)).toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY STYLE OBJECT\n\n"+ex;
        }
        return buffer.toString();
    }
}
