package org.geogurus.data.webservices;

import org.geogurus.data.AbstractDataAccessFactory;
import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.Datasource;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;

/**
 * Creates {@link WmsDataAccess} objects
 * 
 * @author jesse
 */
public class WmsAccessFactory extends AbstractDataAccessFactory {

    public boolean canCreateFrom(ConnectionParams host) {
        return "wms".equalsIgnoreCase(host.type);
    }

    public DataAccess createOne(ConnectionParams bean) {
        DataAccess wms = new WmsDataAccess(bean.host, bean.layer, bean.owner);
        return wms;
    }

    public Option<ConnectionParams> createConnectionParameters(Map map,
            Layer layer, Datasource owner) {
        // COMPLETE so wms layers can be loaded from a MapFile
        return Option.none();
    }

}
