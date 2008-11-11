package org.geogurus.gas.managers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.geogurus.data.Datasource;
import org.geogurus.data.Factory;
import org.geogurus.gas.objects.HostDescriptorBean;

/**
 * A Static manager class that deals with Datasource(s) object:
 * <ul>
 * <li>Build list of datasources for an HostDescriptorBean</li>
 * </ul>
 * 
 * @author nicolas
 */
public class DatasourceManager implements Serializable {

    private static final long serialVersionUID = 5125904750879571049L;

    protected static transient Logger logger = Logger
            .getLogger(DatasourceManager.class.getName());

    private static DatasourceManager instance = new DatasourceManager();

    private Collection<Factory<List<Datasource>, HostDescriptorBean>> strategies = new ArrayList<Factory<List<Datasource>, HostDescriptorBean>>();
    {
        strategies.add(new MapFileFactoryStrategy());
        strategies.add(new FolderFactoryStrategy());
        strategies.add(new WmsFactoryStrategy());
        strategies.add(new WfsFactoryStrategy());
        strategies.add(new PostgisFactoryStrategy());
        strategies.add(new OracleFactoryStrategy());
    }

    /**
     * This will need to be replaced in the future by an injection mechanism but
     * works for now
     */
    private Collection<Factory<List<Datasource>, HostDescriptorBean>> getStrategies() {
        return strategies;
    }

    /**
     * Returns the number of valid datasources in the given host descriptor or
     * -1 if host is invalid.
     * 
     * @param host
     * @return the number of valid datasources or -1 if host is not a valid GAS
     *         host
     */
    public int getDatasourcesCountM(HostDescriptorBean host) {
        Vector<Datasource> vec = DatasourceManager.getInstance()
                .getDatasourcesM(host);
        if (vec == null) {
            return -1;
        }
        int cnt = 0;
        for (Iterator<Datasource> iter = vec.iterator(); iter.hasNext();) {
            Datasource ds = iter.next();
            cnt += ds.getDataList().size();
        }
        return cnt;
    }

    /**
     * Returns a vector of Datasources for the given parameters
     */
    public Vector<Datasource> getDatasourcesM(HostDescriptorBean host) {

        Collection<Factory<List<Datasource>, HostDescriptorBean>> strat = getStrategies();

        for (Factory<List<Datasource>, HostDescriptorBean> factoryStrategy : strat) {
            if (factoryStrategy.canCreateFrom(host)) {
                final List<Datasource> sources = factoryStrategy.create(host);
                if (sources != null) {
                    return new Vector<Datasource>(sources);
                }
            }
        }
        return null;
    }

    /**
     * Returns the DatasourceManager instance to use
     */
    public static DatasourceManager getInstance() {
        return instance;
    }
} // end class

