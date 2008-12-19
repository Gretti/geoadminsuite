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

/**
 * 
 */
package org.geogurus.data.files;

import java.io.File;
import java.io.IOException;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccessHelper;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.GTDataStoreDataAccess;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Access a Shapefile
 * 
 * @author jesse
 */
public class ShpDataAccess extends GTDataStoreDataAccess {

	private static final long serialVersionUID = 1L;
	private File file;

	public ShpDataAccess(String name, String fullpath, Datasource owner) {
		super(name, owner, DataAccessType.SHP);

		file = new File(fullpath);
	}

	protected synchronized Option<FeatureSource<SimpleFeatureType,SimpleFeature>> createFeatureSource() {
		try {
			ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI()
					.toURL());
			String typename = dataStore.getTypeNames()[0];
			return Option.some(dataStore.getFeatureSource(typename));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Option.none();
	}

	@Override
	protected Layer createMSLayerInner(RGB color) {
		return DataAccessHelper.createMapServerLayer(geomTypeCode, file, color);
	}

	protected String getFileName() {
		return file.getAbsolutePath();
	}

	@Override
	public String getConnectionURI() {
		return "file://" + getFileName();
	}

	@Override
	public <T> Option<T> doGet(Class<T> request) {
		if (File.class.isAssignableFrom(request)) {
			return Option.some(request.cast(file));
		}
		return Option.none();
	}

	@Override
	public ConnectionParams getConnectionParams() {
		ConnectionParams params = new ConnectionParams(owner);
		params.name = this.name;
		params.path = file.getAbsolutePath();
		params.type = datasourceType.name();
		return params;
	}
}
