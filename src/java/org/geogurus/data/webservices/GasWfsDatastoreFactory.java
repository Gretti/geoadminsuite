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