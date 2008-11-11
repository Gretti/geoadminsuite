package org.geogurus.data.database;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geogurus.data.Factory;
import org.geotools.data.oracle.OracleDataStore;
import org.geotools.data.oracle.OracleDataStoreFactory;

/**
 * Creates a postgis datastore
 * 
 * @author jesse
 */
final class GasOracleDatastoreFactory implements
        Factory<OracleDataStore, Map<String, Object>> {
    private static final OracleDataStoreFactory ORACLE_DATA_STORE_FACTORY = new OracleDataStoreFactory();
    private Logger logger;

    /**
     * Creates a new instance of type
     * PostgisDataAccess.GasPostgisDatastoreFactory
     * 
     */
    public GasOracleDatastoreFactory(Logger logger) {
        this.logger = logger;
    }

    public boolean canCreateFrom(Map<String, Object> params) {
        return ORACLE_DATA_STORE_FACTORY.canProcess(params);
    }

    public OracleDataStore create(Map<String, Object> params) {
        OracleDataStore newDataStore;
        try {
            newDataStore = (OracleDataStore) ORACLE_DATA_STORE_FACTORY
                    .createDataStore(params);
            return newDataStore;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating OracleDataStore from: "
                    + params, e);
        }
        return null;
    }
}