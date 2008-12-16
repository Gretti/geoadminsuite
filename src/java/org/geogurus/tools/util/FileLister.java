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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.tools.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Vector;

/**
 *
 * @author nicolas
 */
public class FileLister {

    public static File[] listFilesAsArray(
            File directory,
            FilenameFilter filter,
            boolean recurse) {
        Collection<File> files = listFiles(directory,
                filter, recurse);

        File[] arr = new File[files.size()];
        return files.toArray(arr);
    }

    public static Collection<File> listFiles(
            File directory,
            FilenameFilter filter,
            boolean recurse) {
        // List of files / directories
        Vector<File> files = new Vector<File>();

        // Get files / directories in the directory
        File[] entries = directory.listFiles();

        if (entries != null) {

            // Go over entries
            for (File entry : entries) {

                // If there is no filter or the filter accepts the 
                // file / directory, add it to the list
                if (filter == null || filter.accept(directory, entry.getName())) {
                    files.add(entry);
                }

                // If the file is a directory and the recurse flag
                // is set, recurse into the directory
                if (recurse && entry.isDirectory()) {
                    files.addAll(listFiles(entry, filter, recurse));
                }
            }
        }
        // Return collection of files
        return files;
    }
}
