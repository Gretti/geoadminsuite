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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Accept all files that are accepted by one (or more) of the Factories
 * 
 * @author jesse
 */
public class FactoryFileFilter implements FileFilter {

	private final List<AbstractFileAccessFactory> factories;

	/**
	 * Creates a new instance of type FactoryFileFilter
	 *
	 * @param factories
	 */
	public FactoryFileFilter(List<? extends AbstractFileAccessFactory> factories) {
		List<AbstractFileAccessFactory> fileFactories = new ArrayList<AbstractFileAccessFactory>();
		
		for (AbstractFileAccessFactory factoryStrategy : factories) {
				fileFactories.add(factoryStrategy);
		}
		
		this.factories = Collections.unmodifiableList(fileFactories);
	}

	public boolean accept(File file) {
		for (AbstractFileAccessFactory factory : factories) {
			if( factory.canCreateFrom(file)){
				return true;
			}
		}
		return false;
	}

}
