/**
 * 
 */
package org.geogurus.data.webservices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.Datasource;
import org.geogurus.data.DatasourceType;
import org.geogurus.data.Option;
import org.geogurus.data.cache.DataStoreCacheable;
import org.geogurus.data.cache.ObjectCache;
import org.geotools.data.wfs.WFSDataStore;

/**
 * Can connect to and load data from Web Map Servers
 * 
 * @author jesse
 */
public class WfsDatasource extends Datasource {

    private static final long serialVersionUID = 1L;

    public WfsDatasource(String host, String name) {
        super(name, host, DatasourceType.VECTOR);
    }

    /**
     * Finds all valid geographic files for the given datasource, and construct
     * their geometryClass equivalent. All these geometryClasses are stored in
     * the Datasource's dataList hashtable, with the gc id as a key. The
     * geometryClasses built here have the minimal set of information.<br>
     * Use the Datasource.getGeometryClasses(id) method to get the list of
     * GeometryClass for a given mapfile.
     * <p>
     * 
     * Geographic files are those returned by getCapabilities.getLayers method
     * 
     */
    public boolean load() {
        URL url;
        try {
            url = new URL(host);

            Map<String, Object> factoryParams = GasWfsDatastoreFactory
                    .createParams(host, logger);
            DataStoreCacheable key = new DataStoreCacheable(host);
			WFSDataStore datastore = ObjectCache.getInstance().getCachedObject(key, new GasWfsDatastoreFactory(logger), factoryParams);

            WfsDataAccessFactory factory = new WfsDataAccessFactory();

            for (String typename : datastore.getTypeNames()) {
                ConnectionParams params = WfsDataAccessFactory.createParams(
                        this, url, typename);
                DataAccess dataAccess = factory.createOne(params);
                dataList.put(dataAccess.getID(), dataAccess);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Host url is not a valid url: " + host, e);
        }
        return true;
    }

    @Override
    public <T> Option<T> resource(Class<T> resourceType) {
        return null;
    }

}
