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
import java.util.Map;
import java.util.logging.Level;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccessHelper;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.GTDataStoreDataAccess;
import org.geogurus.data.Option;
import org.geogurus.data.cache.DataStoreCacheable;
import org.geogurus.data.cache.ObjectCache;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.MetaData;
import org.geogurus.mapserver.objects.MsLayer;
import org.geogurus.mapserver.objects.RGB;
import org.geotools.data.FeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * An implementation that reads from a WFS datastore
 * 
 * @author jesse
 */
public class WfsDataAccess extends GTDataStoreDataAccess {

    /**
     * 
     */
    public static final String MS_METADATA_WFS_TYPENAME = "wfs_typename";
    private static final long serialVersionUID = -5482111522380866974L;
    private String typename;

    /**
     * Creates a new instance of type WfsDataAccess
     * 
     * @param name
     * @param owner
     * @param type
     */
    public WfsDataAccess(String host, String name, String typename,
            Datasource owner) {
        super(name, owner, DataAccessType.WFS);
        this.host = host;
        this.typename = typename;
    }

    @Override
    protected <T> Option<T> doGet(Class<T> request) {
        return null;
    }

    @Override
    protected Option<FeatureSource<SimpleFeatureType,SimpleFeature>> createFeatureSource() {

            Map<String, Object> factoryParams = GasWfsDatastoreFactory
                    .createParams(host, logger);
            DataStoreCacheable key = new DataStoreCacheable(host);
			WFSDataStore datastore = ObjectCache.getInstance().getCachedObject(key, new GasWfsDatastoreFactory(logger), factoryParams);
			
            Option<FeatureSource<SimpleFeatureType, SimpleFeature>> featureSource;
			try {
                FeatureSource<SimpleFeatureType,SimpleFeature> source = datastore.getFeatureSource(typename);
                featureSource = Option.some(source);
            } catch (IOException e) {
                logger.log(Level.SEVERE,
                        "Unable to connect to FeatureSource for: "
                                + getConnectionURI(), e);
                featureSource = Option.none();
            }
        return featureSource;
    }

    @Override
    protected Layer createMSLayerInner(RGB color) {

        // should construct it
        Layer msLayer = new Layer();
        msLayer.setName(name);

        msLayer.setConnection(host);
        msLayer.setConnectionType(MsLayer.WFS);
        MetaData metaData = new MetaData();
        metaData.addAttribute(MS_METADATA_WFS_TYPENAME, typename);
        metaData.addAttribute("wfs_request_method", "GET");
        metaData.addAttribute("wfs_version", "1.0.0");
        msLayer.setMetaData(metaData);

        // a default display class for this geoobject
        org.geogurus.mapserver.objects.Class c = new org.geogurus.mapserver.objects.Class();
        c.setName(name);
        DataAccessHelper.setMSLayerColorProperties(msLayer, geomTypeCode,
                color, c);
        return msLayer;
    }

    @Override
    public ConnectionParams getConnectionParams() {
        ConnectionParams params = new ConnectionParams(owner);
        params.host = host;
        params.name = name;
        params.typename = typename;
        return params;
    }

    @Override
    public String getConnectionURI() {
        return host + "#" + typename;
    }
}
