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
 * SymbolSet.java
 *
 * Created on 20 june 2002, 11:42
 */
package org.geogurus.mapserver.objects;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;
/**
 *
 * @author  Gretti N'GUESSAN
 */
public class SymbolSet extends MapServerObject  implements java.io.Serializable {
    /** Alias for this font to be used in CLASS objects */
    private File symbolSetFile;
    private ArrayList<Symbol> alSymbols;

    
    /** Empty constructor */
    public  SymbolSet() {
        this(null, new ArrayList<Symbol>());
    }
    
    /** Creates a new instance of Symbol given a file to write and a list of symbols*/
    public SymbolSet(File symbolSetFile_, ArrayList<Symbol> alSymbols_) {
        this.logger = Logger.getLogger(this.getClass().getName());
        symbolSetFile = symbolSetFile_;
        alSymbols = alSymbols_;
    }
    
    /** Creates a new instance of Symbol given a file to write <br>
     * Use this method when the symbolset file already exist
     */
    public SymbolSet(File symbolSetFile_) {
        this(symbolSetFile_, null);
    }
    
    public void setSymbolSetFile(File symbolSetFile_)    {symbolSetFile = symbolSetFile_;}
    public void setArrayListSymbol(ArrayList<Symbol> alSymbols_)       {alSymbols = alSymbols_;}
    
    public File getSymbolSetFile()          {return symbolSetFile;}
    public ArrayList<Symbol> getArrayListSymbol()   {return alSymbols;}
    
    /** Loads data from object'sfile
     * and fill Object parameters with.
     * @return true is mapping done correctly
     */
    public boolean load() {
        if (symbolSetFile == null) {
            return false;
        }
        boolean res = false;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(symbolSetFile));
            res = load(br);
        } catch (IOException ioe) {
            res = false;
        } finally {
            try {
                br.close();
            } catch (Exception ioe2) {
            }
        }
        return res;
    }
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
        if (br == null) {
            return false;
        }
        boolean result = true;
        String line = null;
        alSymbols = new ArrayList<Symbol>();
        Symbol s = null;
        boolean done = false;
        // Looking for the first util line
        try {
            while ((line = br.readLine()) != null) {
                while ((line.trim().equals("")) || (line.trim().startsWith("#"))) {
                    line = br.readLine();
                }
                // Gets array of words of the line
                String[] tokens = ConversionUtilities.tokenize(line.trim());
                if (tokens.length > 1) {
                    logger.warning("invalid symbol file line: " + line);
                    return false;
                }
                if (tokens[0].equalsIgnoreCase("SYMBOL")) {
                    s = new Symbol();
                    done = s.load(br);
                    if (done) {
                        alSymbols.add(s);
                    } else {
                        return false;
                    }
                }
            }
        } catch (IOException ioe) {
            return false;
        }
        if (alSymbols == null) {
            String f = (symbolSetFile != null ? symbolSetFile.getAbsolutePath() : "null");
            logger.warning("SymbolSet.load(): cannot load symbols from file: " + f);
        }
        result = !(alSymbols == null);
        return result;
    }
    /**  Saves SYMBOL object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            if (symbolSetFile!=null)    bw.write("SYMBOLSET "+symbolSetFile.getPath()+"\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }
    
    /**
     * Saves the symbolset file if some symbols are available
     * Creates the symbolset file if it does not exist
     */
    public synchronized boolean saveAsSymFile() {
        boolean result = true;
        java.io.BufferedWriter bwsym = null;
        try{
            if (symbolSetFile != null && !symbolSetFile.exists()) {
                // creates it, if possible, to avoid mapserver crashes
                bwsym = new java.io.BufferedWriter(new java.io.FileWriter(symbolSetFile));
            }
            
            if (alSymbols != null && alSymbols.size() > 0) {
                if (bwsym == null) {
                    bwsym = new java.io.BufferedWriter(new java.io.FileWriter(symbolSetFile));
                }
                // required for MS >= 5: a symbolset object enclosing all symbols
                bwsym.write("SYMBOLSET");
                bwsym.newLine();
                for (Symbol s : alSymbols){
                    if (s != null) {
                        result = s.saveAsMapFile(bwsym);
                    }
                }
                bwsym.write("END");
            }
            if (bwsym != null) {
                bwsym.flush();
                bwsym.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }
    
    /** Returns a string representation of the SYMBOL Object
     * @return a string representation of the SYMBOL Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("SYMBOLFILE OBJECT ");
            if (symbolSetFile!=null)
                buffer.append("\n* SYMBOLSET name           = ").append(symbolSetFile.getPath());
            if (alSymbols!=null)
                buffer.append("\n* SYMBOLSET nber of symbols= ").append(alSymbols.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY SYMBOLSET OBJECT\n\n"+ex;
        }
        return buffer.toString();
    }
    
    /**
     * returns the symbol which name equals the given name, or null if not found
     * 
     * @param symbolName the symbol to get
     * @return
     */
    public Symbol getSymbol(String symbolName) {
        if (symbolName == null || alSymbols == null) {
            return null;
        }
        for (Symbol s : alSymbols) {
            if (symbolName.equals(s.getName())) {
                return s;
            }
        }
        return null;
    }
    
    /** adds the given symbol to the list, replacing a symbol with the same name
     * by provided one
     * @param s the symbol to add
     * @return true if given symbol was replaced or add, false otherwise (null list for instance)
     */
    public boolean addSymbol(Symbol s) {
        if (alSymbols == null || s == null) {
            return false;
        }
        if (alSymbols.contains(s)) {
            for (int i = 0; i < alSymbols.size(); i++) {
                if (alSymbols.get(i).getName().equalsIgnoreCase(s.getName())) {
                    Symbol newSym = alSymbols.get(i);
                    newSym = s;
                    break;
                }
            }
        } else {
            alSymbols.add(s);
        }
        return true;
    }
    
    /**
     * Removes the given symbol by calling alSymbols.remove()
     * returns false if symbol list is null
     * @return true if given symbol was removed.
     * @see Collection.remove()
     */
    public boolean removeSymbol(Symbol s) {
        if (alSymbols == null) {
            return false;
        }
        return alSymbols.remove(s);
    }
}
