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