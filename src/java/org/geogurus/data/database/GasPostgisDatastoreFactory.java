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

package org.geogurus.data.database;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geogurus.data.Factory;
import org.geotools.data.postgis.PostgisDataStore;
import org.geotools.data.postgis.PostgisDataStoreFactory;

/**
 * Creates a postgis datastore
 * 
 * @author jesse
 */
final class GasPostgisDatastoreFactory implements
        Factory<PostgisDataStore, Map<String, Object>> {
    private static final PostgisDataStoreFactory POSTGIS_DATA_STORE_FACTORY = new PostgisDataStoreFactory();
    private Logger logger;

    /**
     * Creates a new instance of type
     * PostgisDataAccess.GasPostgisDatastoreFactory
     * 
     */
    public GasPostgisDatastoreFactory(Logger logger) {
        this.logger = logger;
    }

    public boolean canCreateFrom(Map<String, Object> params) {
        return POSTGIS_DATA_STORE_FACTORY.canProcess(params);
    }

    public PostgisDataStore create(Map<String, Object> params) {
        PostgisDataStore newDataStore;
        try {
            newDataStore = (PostgisDataStore) POSTGIS_DATA_STORE_FACTORY
                    .createDataStore(params);
            return newDataStore;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating PostgisDataStore from: "
                    + params, e);
        }
        return null;
    }
}