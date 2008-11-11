/**
 * 
 */
package org.geogurus.gas.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geogurus.data.Datasource;
import org.geogurus.data.Factory;
import org.geogurus.data.webservices.WmsDatasource;
import org.geogurus.gas.objects.HostDescriptorBean;

/**
 * Strategy for processing WMS into Datasources
 * 
 * @author jeichar
 */
public final class WmsFactoryStrategy implements
        Factory<List<Datasource>, HostDescriptorBean> {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    public boolean canCreateFrom(HostDescriptorBean host) {
        return "WMS".equalsIgnoreCase(host.getType());
    }

    public List<Datasource> create(HostDescriptorBean host) {
        List<Datasource> res = new ArrayList<Datasource>();
        WmsDatasource ds = new WmsDatasource(host.getName(), host.getPath(), 
                false);
        if (ds.load()) {
            // a valid datasource containing geo data: either file or geo tables
            res.add(ds);
            logger.fine("datasource added, contains: "
                    + ds.getDataList().size());
        } else {
            logger.warning("getDataInformation failed on datasource: "
                    + host.getName() + "\n\tmessage is: " + ds.errorMessage);
        }
        return res;
    }

}
