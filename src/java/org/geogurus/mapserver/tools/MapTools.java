/*
 * MapTools.java
 *
 * Created on 18 juin 2003, 16:28
 */
package org.geogurus.mapserver.tools;

import java.io.*;
import java.util.*;
import org.geogurus.mapserver.objects.Symbol;
import org.geogurus.tools.string.ConversionUtilities;

/**
 *
 * @author  gng
 */
public class MapTools {

    public static File buildFileFromMapPath(String mapFilePath, String shapePath, String dataName) {
        //mapFilePath shall not be null
        if (mapFilePath == null || dataName == null) {
            return null;
        }

        //gets the file separator
        String fs = System.getProperty("file.separator");
        boolean isUnix, hasShapePath, hasDataPath;
        if (fs.equalsIgnoreCase("\\")) {
            isUnix = false;
        } else if (fs.equalsIgnoreCase("/")) {
            isUnix = true;
        } else {
            return null;
        }

        hasShapePath = shapePath == null ? false : true;

        if (hasShapePath && isRelativePath(shapePath, isUnix)) {
            shapePath = mapFilePath + (mapFilePath.endsWith(fs) ? shapePath : fs + shapePath);
        }

        if (isRelativePath(dataName, isUnix)) {
            if (hasShapePath) {
                dataName = shapePath + (shapePath.endsWith(fs) ? dataName : fs + dataName);
            } else {
                dataName = mapFilePath + (mapFilePath.endsWith(fs) ? dataName : fs + dataName);
            }
        }

        //creates the file to be returned
        return new File(dataName);
    }

    public static Hashtable makeHashtableFromArrayList(ArrayList symbols) {
        Hashtable hashSym = new Hashtable();
        Symbol s;
        for (int i = 0; i < symbols.size(); i++) {
            s = (Symbol) symbols.get(i);
            hashSym.put(s.getName(), s);
        }
        return hashSym;
    }

    public static ArrayList getSymbolsFromSym(File symFile) {
        ArrayList symArray = new ArrayList();
        Symbol s = null;
        boolean done;
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(symFile));
            String line = null;
            // Looking for the first util line
            while ((line = br.readLine()) != null) {
                while ((line.trim().equals("")) || (line.trim().startsWith("#"))) {
                    line = br.readLine();
                }
                // Gets array of words of the line
                String[] tokens = ConversionUtilities.tokenize(line.trim());
                if (tokens.length > 1) {
                    return null;
                }
                if (tokens[0].equalsIgnoreCase("SYMBOL")) {
                    s = new Symbol();
                    done = s.load(br);
                    if (done) {
                        symArray.add(s);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("MapTools.getSymbolsFromSym: Exception when opening file " + symFile.getPath() + " : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return symArray;
    }

    public static boolean isRelativePath(String path, boolean isUnix) {
        if (isUnix) {
            if (path.charAt(0) == '/') {
                return false;
            }
        } else {
            if (path.length() > 1 && path.charAt(1) == ':') {
                return false;
            }
        }
        return true;
    }
}