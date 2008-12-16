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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geogurus.data.DataAccess;
import org.geogurus.data.files.ShpDataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * 
 * @author gnguessan
 */
public class DatasourceBuilder {

    public static SimpleFeature getFeature(DataAccess gc, String lon, String lat, String tolerance, String toleranceUnit)
            throws MalformedURLException, IOException, CQLException {
    	SimpleFeature feature = null;

        DataStore dataStore = DatasourceBuilder.getDataStore(gc);
        FeatureSource<SimpleFeatureType,SimpleFeature> source;
        if (gc instanceof ShpDataAccess) {
            String typename = dataStore.getTypeNames()[0];
            source = dataStore.getFeatureSource(typename);
        } else {
            source = dataStore.getFeatureSource(gc.getName());
        }

        Filter filter = CQL.toFilter("DWITHIN (the_geom, POINT(" + lon + " " + lat + ")," + tolerance + "," + toleranceUnit + ")");
        FeatureCollection<SimpleFeatureType,SimpleFeature> collection = source.getFeatures(filter);
        Iterator<SimpleFeature> iterator = collection.iterator();
        try {
            if (iterator.hasNext()) {
                feature = (SimpleFeature) iterator.next();
            }
        } catch (NoSuchElementException nsee) {
            nsee.printStackTrace();
        } finally {
            collection.close(iterator);
            dataStore.dispose();
        }

        return feature;
    }

    public static DataStore getDataStore(DataAccess gc)
            throws MalformedURLException, IOException {
        DataStore ds = null;

        if (gc instanceof ShpDataAccess) {
            String fileName = gc.getOwner().getName();
            if (!gc.getOwner().getName().endsWith("\\")
                    && !gc.getOwner().getName().endsWith("/")) {
                fileName += File.separator;
            }
            File file = gc.resource(File.class).get();
            ds = new ShapefileDataStore(file.toURI().toURL());
        }
        return ds;
    }
}
