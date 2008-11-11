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