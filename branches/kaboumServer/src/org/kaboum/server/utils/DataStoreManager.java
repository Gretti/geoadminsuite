/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kaboum.server.utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.spatialite.SpatiaLiteDataStoreFactory;

/**
 * Singleton storing all loaded Geotools DataStore, to reuse them instead of creating one each time,
 * wich is very time and memory consuming
 * 
 * @author nicolas
 */
public class DataStoreManager {
        private static Logger logger = Logger.getLogger(DataStoreManager.class);

        private static DataStoreManager instance;
        /** map storing available datastores, key is the <PD>_DATASTORE_PARAMS value as found in
         * the kaboumServer.properties files
         */
        //private Hashtable<String, DataStore> dataStores = null;
        private Hashtable dataStores = null;

        private DataStoreManager() {
                //dataStores = new Hashtable<String, DataStore>();
                dataStores = new Hashtable();
        }

        public static DataStoreManager getInstance() {
                if (instance == null) {
                        instance = new DataStoreManager();
                }
                return instance;
        }

        public DataStore getDataStore(String key) {
                if (key == null) {
                        return null;
                }
                return (DataStore) dataStores.get(key);
        }

        public DataStore setDataStore(String key, DataStore ds) {
                if (key == null || ds == null) {
                        return null;
                }
                return (DataStore) dataStores.put(key, ds);
        }

        /**
         * Returns a datastore if any can be found manually by building
         * a DatastoreFactory directy.
         * <br/> Supported datastores:
         * <ul>
         *     <li> SpatialiteDatasStoreFactory</li>
         * </ul>
         * @param params the map of params needed to connect to a datastore
         * @return a DataStore able to process given parameters, or null if
         * no datastore can be found.
         */
        public DataStore getDataStoreManually( Map<String, Serializable> params) {
                if (params == null) {
                        logger.error("Null parameters. cannot find a DataStore manually.");
                        return null;
                }
                DataStore ds = null;

                // try a Spatialite datastore
                logger.debug("Trying to create a Spatialite DataStore...");
                DataStoreFactorySpi factory = new SpatiaLiteDataStoreFactory();
                try {
                        ds = factory.createDataStore(params);
                        if (ds != null) {
                                logger.debug("Spatialite DataStore found.");
                                return ds;
                        }
                } catch (IOException ioe) {
                        logger.warn("Exception when trying to access a Spatialite DataStore.", ioe);
                }

                // try a ... datastore

                return ds;
        }
}
