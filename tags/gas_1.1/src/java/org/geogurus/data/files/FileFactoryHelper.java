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

package org.geogurus.data.files;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Common Methods used by Filebased factory implementations
 * 
 * @author jesse
 */
public final class FileFactoryHelper {
    /**
     * Creates a new instance of type FileFactoryHelper
     * 
     */
    private FileFactoryHelper() {
    }

    /**
     * Returns the file extension or "" if no extension
     */
    public static String getExtension(File file) {
        String fn = file.getName();
        String ext = "";
        int pt = fn.lastIndexOf(".");
        try {
            if (pt > -1) {
                ext = fn.substring(pt + 1);
            }
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
        return ext;
    }

    /**
     * Returns the name of the file without the extension
     */
    public static String getExtensionlessName(File file) {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index == -1) {
            return name;
        }
        return name.substring(0, index);
    }

    /**
     * Returns true if files with the same name and with all the provided
     * extension exist in the same directory.
     * 
     * @param file
     *            the base file (the .shp file for example)
     * @param requiredExtension
     *            all the extensions that MUST be there
     * @return true if the expected files exist
     */
    public static boolean fileExists(File file, String... requiredExtension) {
        final Set<String> notFound = new HashSet<String>(Arrays
                .asList(requiredExtension));
        file.getParentFile().listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                String ext = getExtension(pathname);
                for (String string : notFound) {
                    if (string.equalsIgnoreCase(ext)) {
                        notFound.remove(string);
                        break;
                    }
                }
                // we don't actually want the files we just want to use this
                // method as a
                // function on the files
                return false;
            }

        });
        return notFound.isEmpty();
    }

}
