/*
 * SymbolSet.java
 *
 * Created on 20 june 2002, 11:42
 */
package org.geogurus.mapserver.objects;
import java.util.ArrayList;
import java.awt.Point;
import java.io.File;
import java.io.BufferedReader;
import org.geogurus.tools.string.ConversionUtilities;
import org.geogurus.mapserver.tools.MapTools;
/**
 *
 * @author  Gretti N'GUESSAN
 */
public class SymbolSet extends MapServerObject  implements java.io.Serializable {
    /** Alias for this font to be used in CLASS objects */
    private File symbolSetFile;
    private ArrayList alSymbols;
    /** Empty constructor */
    public  SymbolSet() {
        this(null, null);
    }
    
    
    /** Creates a new instance of Symbol given a file to write and a list of symbols*/
    public SymbolSet(File symbolSetFile_, ArrayList alSymbols_) {
        symbolSetFile = symbolSetFile_;
        alSymbols = alSymbols_;
    }
    
    /** Creates a new instance of Symbol given a file to write <br>
     * Use this method when the symbolset file already exist
     */
    public SymbolSet(File symbolSetFile_) {
        symbolSetFile = symbolSetFile_;
    }
    
    public void setSymbolSetFile(File symbolSetFile_)    {symbolSetFile = symbolSetFile_;}
    public void setArrayListSymbol(ArrayList alSymbols_)       {alSymbols = alSymbols_;}
    
    public File getSymbolSetFile()          {return symbolSetFile;}
    public ArrayList getArrayListSymbol()   {return alSymbols;}
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
        boolean result = true;
        alSymbols = MapTools.getSymbolsFromSym(symbolSetFile);
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
                // creates it to avoid mapserver crashes
                bwsym = new java.io.BufferedWriter(new java.io.FileWriter(symbolSetFile));
                org.geogurus.tools.LogEngine.log("SymbolSet.saveAsSymFile: writing symbolset file: " + symbolSetFile.getPath());
            }
            
            if (alSymbols != null && alSymbols.size() > 0) {
                if (bwsym == null) {
                    bwsym = new java.io.BufferedWriter(new java.io.FileWriter(symbolSetFile));
                }
                Symbol s = null;
                for (int i = 0 ; i < alSymbols.size(); i++){
                    s = (Symbol)alSymbols.get(i);
                    result = s.saveAsMapFile(bwsym);
                }
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
}
