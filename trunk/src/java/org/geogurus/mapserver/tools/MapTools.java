/*
 * MapTools.java
 *
 * Created on 18 juin 2003, 16:28
 */
package org.geogurus.mapserver.tools;

import java.io.File;

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