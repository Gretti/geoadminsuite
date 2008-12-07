package org.geogurus.data.webservices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geogurus.data.Factory;
import org.geotools.data.wms.WebMapServer;
import org.geotools.ows.ServiceException;

/**
 * Creates a postgis datastore
 * 
 * @author jesse
 */
final class GasWmsDatastoreFactory implements
        Factory<WebMapServer, Map<String, Object>> {

    private Logger logger;

    /**
     * Creates a new instance of type
     * PostgisDataAccess.GasPostgisDatastoreFactory
     * 
     */
    public GasWmsDatastoreFactory(Logger logger) {
        this.logger = logger;
    }

    public boolean canCreateFrom(Map<String, Object> params) {
        boolean ret = false;
        if (params.containsKey("url")) {
            try {
                new URL(params.get("url").toString());
                ret = true;
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, "Error in WebMapServer URL : " + params.get("url"), ex);
            }

        }
        return ret;
    }

    public WebMapServer create(Map<String, Object> params) {
        WebMapServer wms = null;
        try {
            wms = new WebMapServer((URL) params.get("url"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating WebMapServer from: " + params, e);
        } catch (ServiceException e) {
            logger.log(Level.SEVERE, "Error creating WebMapServer from: " + params, e);
        }
        return wms;
    }

    public static Map<String, Object> createParams(URL host) {
        Map<String, Object> factoryParams = new HashMap<String, Object>();
        factoryParams.put("url", host);
        return factoryParams;
    }
}