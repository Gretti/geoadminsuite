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
