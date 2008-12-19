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