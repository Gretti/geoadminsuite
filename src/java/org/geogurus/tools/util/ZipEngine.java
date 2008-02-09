/*
 * ZipEngine.java
 *
 * Created on 22 octobre 2002, 15:04
 */

package org.geogurus.tools.util;
import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
 *
 * @author  gng
 */
public class ZipEngine {
    
    private static final int BUFFER = 2048;
    
    /** Creates a new instance of ZipEngine
     *  declared private in order not to instanciate any object
     */
    private ZipEngine () {
    }
    
    /**
     * @param zipName
     * @param files
     * @throws IOException
     */    
    public static void zipToFile(String zipName, String[] files) throws IOException {
        FileOutputStream zipOut = new FileOutputStream(zipName);
        zipToStream(zipOut, files);
        zipOut.close();
    }
    
    /**
     * @param ops
     * @param files
     * @throws IOException
     */    
    public static void zipToStream(OutputStream ops, String[] files) throws IOException {
        zipToStream(ops, files, null);
    }
    /**
     * @param ops
     * @param files
     * @param zipPath an array of path, to appear in the zip
     * @throws IOException
     */    
    public static void zipToStream(OutputStream ops, String[] files, String[] zipPath) throws IOException {
        String[] zp = zipPath == null ? files : zipPath;
        
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(ops));
        byte data[] = new byte[BUFFER];
        for (int i=0; i<files.length; i++) {
            //System.out.println("Adding: "+files[i]);
            if (files[i] == null) {
                // skip null entries
                continue;
            }
            FileInputStream fi = new FileInputStream(files[i]);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(zp[i]);
            out.putNextEntry(entry);
            int count;
            while((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            out.closeEntry();
            origin.close();
        }
        out.finish();
        out.flush();
    }
    /**
     * @param ops
     * @param files
     * @param zipPath an array of path, to appear in the zip
     * @throws IOException
     */    
    public static void zipToStream(OutputStream ops, Vector files, Vector zipPath) throws IOException {
        String[] arr1 = new String[files.size()];
        String[] arr2 = new String[zipPath.size()];
        
        zipToStream(ops, (String[])files.toArray(arr1), (String[])zipPath.toArray(arr2));
    }
}

