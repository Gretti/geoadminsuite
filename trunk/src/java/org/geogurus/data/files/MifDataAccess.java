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

package org.geogurus.data.files;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccessHelper;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.GTDataStoreDataAccess;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.MsLayer;
import org.geogurus.mapserver.objects.RGB;
import org.geotools.data.FeatureSource;
import org.geotools.data.mif.MIFDataStore;
import org.geotools.data.mif.MIFDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Support for Data Access for Mif files
 * 
 * @author jesse
 */
public class MifDataAccess extends GTDataStoreDataAccess {

    private static final long serialVersionUID = -6165074587723896275L;
    private File file;

    /**
     * Creates a new instance of type MifDataAccess
     */
    public MifDataAccess(File file, Datasource owner) {
        super(file.getName(), owner, DataAccessType.MIF);
        this.file = file;
    }

    @Override
    protected <T> Option<T> doGet(Class<T> request) {
        if (File.class.isAssignableFrom(request)) {
            return Option.some(request.cast(file));
        }
        return Option.none();
    }

    @Override
    protected synchronized Option<FeatureSource<SimpleFeatureType,SimpleFeature>> createFeatureSource() {
            MIFDataStoreFactory factory = new MIFDataStoreFactory();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(MIFDataStoreFactory.PARAM_DBTYPE.key, "mif");
            params.put(MIFDataStoreFactory.PARAM_PATH.key, file
                    .getAbsolutePath());
            Option<FeatureSource<SimpleFeatureType,SimpleFeature>> featureSource;

            try {
                MIFDataStore dataStore = (MIFDataStore) factory
                        .createDataStore(params);
                String typename = dataStore.getTypeNames()[0];
				featureSource = Option.some(dataStore.getFeatureSource(typename));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to create a Datastore for "
                        + file, e);
                featureSource = Option.none();
            } catch (NullPointerException e) {
                e.printStackTrace();
                featureSource = Option.none();
            }
        return featureSource;
    }

    @Override
    protected Layer createMSLayerInner(RGB color) {
        Layer layer = DataAccessHelper.createMapServerLayer(geomTypeCode, file,
                color);
        layer.setConnectionType(MsLayer.OGR);

        return layer;
    }

    @Override
    public ConnectionParams getConnectionParams() {
        ConnectionParams params = new ConnectionParams(owner);
        params.path = file.getAbsolutePath();
        return params;
    }

    @Override
    public String getConnectionURI() {
        try {
            return file.toURI().toURL().toExternalForm();
        } catch (MalformedURLException e) {
            return "file://" + file.getAbsolutePath();
        }
    }

}
