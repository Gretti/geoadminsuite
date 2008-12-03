/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    public static SimpleFeature getFeature(DataAccess gc, String lon, String lat)
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

        Filter filter = CQL.toFilter("INTERSECT (the_geom, POINT(" + lon + " "
                + lat + "))");
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
