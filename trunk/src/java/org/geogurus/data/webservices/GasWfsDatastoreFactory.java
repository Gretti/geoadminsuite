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

package org.geogurus.data.webservices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geogurus.data.Factory;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;

/**
 * Creates a postgis datastore
 * 
 * @author jesse
 */
final class GasWfsDatastoreFactory implements
		Factory<WFSDataStore, Map<String, Object>> {
	private static final WFSDataStoreFactory WFS_DATA_STORE_FACTORY = new WFSDataStoreFactory();
	private Logger logger;

	/**
	 * Creates a new instance of type
	 * PostgisDataAccess.GasPostgisDatastoreFactory
	 * 
	 */
	public GasWfsDatastoreFactory(Logger logger) {
		this.logger = logger;
	}

	public boolean canCreateFrom(Map<String, Object> params) {
		return WFS_DATA_STORE_FACTORY.canProcess(params);
	}

	public WFSDataStore create(Map<String, Object> params) {
		WFSDataStore newDataStore;
		try {
			newDataStore = (WFSDataStore) WFS_DATA_STORE_FACTORY
					.createDataStore(params);
			return newDataStore;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating WfsDataStore from: "
					+ params, e);
		}
		return null;
	}

	/**
	 * @param host
	 */
	public static Map<String, Object> createParams(String host, Logger logger) {
		try {
			String getCapsURL = host;
			if (!host.contains("?")) {
				getCapsURL += "?";
			}
			if (!host.toUpperCase().contains("SERVICE")) {
				getCapsURL += "&SERVICE=WFS";
			}
			if (!host.toUpperCase().contains("REQUEST")) {
				getCapsURL += "&REQUEST=GetCapabilities";
			}
			URL url;
			url = new URL(getCapsURL);
			Map<String, Object> factoryParams = new HashMap<String, Object>();
			factoryParams.put(WFSDataStoreFactory.URL.key, url);
			factoryParams.put(WFSDataStoreFactory.LENIENT.key, true);
			factoryParams.put(WFSDataStoreFactory.PROTOCOL.key, true);
			return factoryParams;
		} catch (MalformedURLException e1) {
			logger.log(Level.SEVERE, "Unable to create url from " + host, e1);
			return null;
		}
	}
}