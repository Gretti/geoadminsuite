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
 * ZipEngine.java
 *
 * Created on 22 octobre 2002, 15:04
 */

package org.geogurus.tools.util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Factory useful to zip file(s)
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
     * Zips the given list of files into a the give zipfile name
     * @param zipName the path+name of the zip file to write
     * @param files The array of Files to zip.
     * @throws IOException if an error occured during zipfile creation
     */    
    public static void zipToFile(String zipName, String[] files) throws IOException {
        FileOutputStream zipOut = new FileOutputStream(zipName);
        zipToStream(zipOut, files);
        zipOut.close();
    }
    
    /**
     * Zips the given list of files into a the give output stream
     * @param ops the output stream to write the zipped files to.
     * @param files The array of Files to zip.
     * @throws IOException if an error occured during stream handling
     */    
    public static void zipToStream(OutputStream ops, String[] files) throws IOException {
        zipToStream(ops, files, null);
    }
    /**
     * Zips the given list of files into a the give output stream. The files entries will be
     * equal to the files to zip except if zipPath is not null. If zipPath is not null, the paths contained
     * in this array will be used as zipped file entry
     * @param ops the output stream to write the zipped files to.
     * Use this method with a vaild
     * @param files The array of Files to zip.
     * @param zipPath an array of file paths to use a zip entries for zipped files.
     * @throws IOException if an error occured during stream handling
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
     * Zips the given list of files into a the give output stream, using the given array
     * of file path to qualify zipped files.
     * @param ops the output stream to write the zipped files to.
     * @param files The Vector of Files to zip.
     * @param zipPath a Vector of file paths, with the same size as files parameter, used
     *        to qualify the name of zipped files into the archive.
     * @throws IOException if an error occured during stream handling
     */    
    public static void zipToStream(OutputStream ops, Vector files, Vector zipPath) throws IOException {
        String[] arr1 = new String[files.size()];
        String[] arr2 = new String[zipPath.size()];
        
        zipToStream(ops, (String[])files.toArray(arr1), (String[])zipPath.toArray(arr2));
    }
}

