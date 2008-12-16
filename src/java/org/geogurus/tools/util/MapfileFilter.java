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

import org.geogurus.gas.managers.MapFileFactoryStrategy;

/**
 *
 * @author nicolas
 */
public class MapfileFilter implements FilenameFilter {

	MapFileFactoryStrategy s = new MapFileFactoryStrategy();
	
	/**
     * FilenameFilter interface implementation: 
     * Returns true if given name is a .map file.
     * @param dir
     * @param name
     * @return if and only if the name should be included in the file list; false otherwise.
     */
    public boolean accept(File dir, String name) {
        return s.canCreateFrom(new File(dir, name));
    }

}
