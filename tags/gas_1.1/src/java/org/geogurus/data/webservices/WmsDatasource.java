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

/**
 * 
 */
package org.geogurus.data.webservices;

import java.net.MalformedURLException;
import java.net.URL;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.Datasource;
import org.geogurus.data.DatasourceType;
import org.geogurus.data.Option;
import org.geogurus.data.cache.DataStoreCacheable;
import org.geogurus.data.cache.NoOpCacheable;
import org.geogurus.data.cache.ObjectCache;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;

/**
 * Can connect to and load data from Web Map Servers
 * 
 * @author jesse
 */
public class WmsDatasource extends Datasource {

    private static final long serialVersionUID = 1L;

    public WmsDatasource(String name, String host, boolean recurse) {
        super(name, host, DatasourceType.RASTER);
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
        URL url = null;
        WebMapServer wms = null;
        DataAccess gc = null;
        try {
            WmsAccessFactory factory = new WmsAccessFactory();

            url = new URL(this.host);

            wms = ObjectCache.getInstance().getCachedObject(new NoOpCacheable(host), new GasWmsDatastoreFactory(logger), GasWmsDatastoreFactory.createParams(url));

            if (wms == null) {
                // The server returned a ServiceException (unusual in this case)
                logger.warning("The server " + this.host
                        + " returned an exception");
                return false;
            }
            WMSCapabilities wmsCap = wms.getCapabilities();
            org.geotools.data.ows.Layer[] layers = wmsCap.getLayer()
                    .getChildren();
            for (Layer layer : layers) {
                if (layer.getName() == null) {
                    continue;
                }
                ConnectionParams params = new ConnectionParams(this);
                params.host = host;
                params.type = "Wms";
                params.layer = layer.getName();
                gc = factory.createOne(params);
                getDataList().put(gc.getID(), gc);
            }
        } catch (MalformedURLException e) {
            // There was an error communicating with the server
            logger.warning("Wrong URL for WMS server " + this.host);
            return false;
        }
        return true;
    }

    @Override
    public <T> Option<T> resource(Class<T> resourceType) {
        return null;
    }

}
