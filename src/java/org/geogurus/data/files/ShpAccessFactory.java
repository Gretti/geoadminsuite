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

import static org.geogurus.data.files.FileFactoryHelper.getExtension;
import static org.geogurus.data.files.FileFactoryHelper.getExtensionlessName;

import java.io.File;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;

/**
 * Create {@link ShpDataAccess} objects
 * 
 * @author jesse
 */
public class ShpAccessFactory extends AbstractFileAccessFactory {

    @Override
    public boolean canCreateFrom(File file) {
        String ext = getExtension(file);
        String begin = getExtensionlessName(file);
        if (file.isFile() && ext.equalsIgnoreCase("shp")) {
            boolean dbfExists = new File(file.getParent(), begin + ".dbf").exists();
            boolean shxExists = new File(file.getParent(), begin + ".shx").exists();
            return dbfExists && shxExists;
        }
        if (file.isFile() && ext.equalsIgnoreCase("SHP")) {
            boolean dbfExists = new File(file.getParent(), begin + ".DBF").exists();
            boolean shxExists = new File(file.getParent(), begin + ".SHX").exists();
            return dbfExists && shxExists;
        }
        return false;
    }

    public DataAccess createOne(ConnectionParams host) {
        DataAccess dataAccess = new ShpDataAccess(host.name, host.path,
                host.owner);
        return dataAccess;
    }
}
