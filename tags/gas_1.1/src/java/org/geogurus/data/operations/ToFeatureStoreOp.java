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

package org.geogurus.data.operations;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geogurus.data.OperationAdapter;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Writes each feature to the FeatureStore
 * 
 * @author jesse
 */
public class ToFeatureStoreOp extends OperationAdapter<SimpleFeature, FeatureStore<SimpleFeatureType,SimpleFeature>> {

    @Override
    public void start(FeatureStore<SimpleFeatureType,SimpleFeature> context) {
        Transaction transaction = new DefaultTransaction(getClass()
                .getSimpleName());
        context.setTransaction(transaction);
    }

    public boolean operate(SimpleFeature operatee, FeatureStore<SimpleFeatureType,SimpleFeature> context) {
        FeatureCollection<SimpleFeatureType,SimpleFeature> collectionOfOne = DataUtilities.collection(operatee);
        try {
            context.addFeatures(collectionOfOne);
        } catch (IOException e) {
            e.printStackTrace();
            // bail out something is going wrong
            return false;
        }
        return true;
    }

    @Override
    public void end(FeatureStore<SimpleFeatureType,SimpleFeature> context, boolean finished) {
        try {
            context.getTransaction().commit();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "error committing transaction", e);
        }
    }

}
