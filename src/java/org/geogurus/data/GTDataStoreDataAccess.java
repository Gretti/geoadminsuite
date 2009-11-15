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

package org.geogurus.data;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Vector;

import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * An abstract DataAccess class for all DataAccess implementation that are based
 * on Geotools DataStores
 * 
 * @author jesse
 */
public abstract class GTDataStoreDataAccess extends DataAccess {

    private static final long serialVersionUID = -5466459247891878493L;
    /** the DB major version for this datasource */
    protected int dbVersion;

    /**
     * Creates a new instance of type GTDataStoreDataAccess
     */
    public GTDataStoreDataAccess(String name, Datasource owner,
            DataAccessType type) {
        super(name, owner, type);
    }

    /**
     * Gets metadata information of a shapefile. Uses the SCOT-modified
     * geotools.jar to read file informations.
     */
    public boolean loadMetadata() {
        if (metadata != null) {
            return true;
        }
        boolean ret = false;

        if (geometryAttributeName != null) {
            ret = true;
        }
        try {
            // get feature results
            Option<FeatureSource<SimpleFeatureType, SimpleFeature>> option = createFeatureSource();
            if (option.isNone()) {
                logger.warning(getClass().getName() + " is not a valid datasource");
                return false;
            }
            FeatureSource<SimpleFeatureType, SimpleFeature> source = option.get();
            // Number of geometries
            numGeometries = source.getCount(Query.ALL);

            // Envelope
            Envelope ext = source.getBounds();
            extent = new Extent(ext.getMinX(), ext.getMinY(), ext.getMaxX(),
                    ext.getMaxY());
            // Attribute types
            SimpleFeatureType ft = source.getSchema();
            GeometryClassFieldBean as;
            GeometryDescriptor geomAttr = ft.getGeometryDescriptor();
            // Geometry column name
            geometryAttributeName = geomAttr.getLocalName();
            // Geometry type
            String geomType = geomAttr.getType().getBinding().getSimpleName();
            if (geomType.equalsIgnoreCase("Polygon")) {
                geomTypeCode = Geometry.POLYGON;
            } else if (geomType.equalsIgnoreCase("MultiPolygon")) {
                geomTypeCode = Geometry.MULTIPOLYGON;
            } else if (geomType.equalsIgnoreCase("LineString")) {
                geomTypeCode = Geometry.LINESTRING;
            } else if (geomType.equalsIgnoreCase("LineSegment")) {
                geomTypeCode = Geometry.LINESTRING;
            } else if (geomType.equalsIgnoreCase("MultiLineString")) {
                geomTypeCode = Geometry.MULTILINESTRING;
            } else if (geomType.equalsIgnoreCase("Point")) {
                geomTypeCode = Geometry.POINT;
            } else if (geomType.equalsIgnoreCase("MultiPoint")) {
                geomTypeCode = Geometry.MULTIPOINT;
            } else if (geomType.equalsIgnoreCase("Geometry")) {
                geomTypeCode = Geometry.GEOMETRY;
            } else if (geomType.equalsIgnoreCase("GeometryCollection")) {
                geomTypeCode = Geometry.GEOMETRYCOLLECTION;
            } else {
                geomTypeCode = Geometry.NULL;
            }

            // num geometries
            if (metadata == null) {
                metadata = new Vector<GeometryClassFieldBean>();
            }

            // Attributes
            for (int i = 0; i < ft.getAttributeCount(); i++) {
                AttributeDescriptor at = ft.getDescriptor(i);
                if (!at.getLocalName().equalsIgnoreCase(geomAttr.getLocalName())) {
                    as = new GeometryClassFieldBean();
                    // Name
                    as.setName(at.getLocalName());
                    // Length
                    as.setLength(0);
                    // Type
                    as.setType(at.getType().getBinding().getSimpleName());
                    // Nullability
                    as.setNullable(at.isNillable() ? "Nullable"
                            : "Not Nullable");
                    metadata.add(as);
                }
            }
            // the projection info
            CoordinateReferenceSystem cs;
            if (source.getSchema().getCoordinateReferenceSystem() == null) {
                if (ext instanceof ReferencedEnvelope &&
                        ((ReferencedEnvelope) ext).getCoordinateReferenceSystem() != null) {
                    cs = ((ReferencedEnvelope) ext).getCoordinateReferenceSystem();
                    SRText = cs.toWKT();
                    SRID = CRS.lookupEpsgCode(cs, true).intValue();
                } else {
                    SRText = DefaultGeographicCRS.WGS84.toWKT();
                    SRID = 4326;
                }
            } else {
                cs = source.getSchema().getCoordinateReferenceSystem();
                SRText = cs.toWKT();
                try {
                    SRID = CRS.lookupEpsgCode(cs, true).intValue();
                } catch (NullPointerException npe) {
                    SRID = -1;
                }
            }
            ret = true;
        } catch (Exception e) {
            // either a bad URL or another exception
            e.printStackTrace();
            return false;
        }
        return ret;
    }

    @Override
    public boolean isEditable() {
        Option<FeatureSource<SimpleFeatureType, SimpleFeature>> option = createFeatureSource();
        if (option.isNone()) {
            return false;
        }
        return option.get() instanceof FeatureStore;
    }

    @Override
    public <T> Option<Boolean> performOperateStep(Operation<SimpleFeature, T> op,
            T context, Query query) throws IOException {
        Option<FeatureSource<SimpleFeatureType, SimpleFeature>> option = createFeatureSource();
        if (option.isNone()) {
            return Option.none();
        }
        FeatureSource<SimpleFeatureType, SimpleFeature> store = option.get();
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = store.getFeatures(query);
        FeatureIterator<SimpleFeature> iterator = collection.features();

        try {
            while (iterator.hasNext()) {
                SimpleFeature operatee = iterator.next();
                if (!op.operate(operatee, context)) {
                    return Option.some(false);
                }
            }
        } finally {
            iterator.close();
        }

        return Option.some(true);

    }

    @Override
    public <T> Option<Boolean> peformOperateStep(
            Operation<RenderedImage, T> op, T context) {
        return Option.none();
    }

    /**
     * Returns a featuresource for this DataSource. It is unimportant to this
     * implementation if the feature source is the same object or a new one each
     * time.
     */
    protected abstract Option<FeatureSource<SimpleFeatureType, SimpleFeature>> createFeatureSource();

    /**
     * Gets sample data from shapefile attributes.
     */
    public Vector<Vector<Object>> getSampleData(int from, int to) {
        try {
            Option<FeatureSource<SimpleFeatureType, SimpleFeature>> option = createFeatureSource();
            if (option.isNone()) {
                return null;
            }
            FeatureSource<SimpleFeatureType, SimpleFeature> source = option.get();
            FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape = source.getFeatures();
            FeatureIterator<SimpleFeature> iter = fsShape.features();
            try {
                Vector<Vector<Object>> columnValues = new Vector<Vector<Object>>();

                // parses dbf from 'from' to 'limit'
                Vector<Object> currentRecord = null;
                int limit = (to < numGeometries) ? (to + 1) : numGeometries;
                if (limit == -1) {
                    if (to > 0) {
                        limit = to;
                    } else {
                        limit = 100;
                    }
                }
                int cnt = 0;
                SimpleFeature f;
                while (iter.hasNext() && cnt < limit) {
                    f = iter.next();
                    if (cnt >= from) {
                        currentRecord = new Vector<Object>();
                        for (GeometryClassFieldBean object : metadata) {
                            Object o= f.getAttribute(object.getName());
                            currentRecord.add(o);
                        }
                        columnValues.add(currentRecord);
                    }
                    cnt++;
                }
                iter.close();
                return columnValues;
            } finally {
                iter.close();
            }
        } catch (Exception e) {
            // either a bad URL or another exception
            e.printStackTrace();
            return null;
        }
    }

    protected abstract <T> Option<T> doGet(Class<T> request);

    @Override
    public final <T> Option<T> resource(Class<T> request) {
        Option<T> found = doGet(request);
        if (found.isSome()) {
            return found;
        }
        if (FeatureStore.class.isAssignableFrom(request)) {
            Option<FeatureSource<SimpleFeatureType, SimpleFeature>> option = createFeatureSource();
            if (option.isSome()) {
                return Option.some(request.cast(option));
            }
        }
        return Option.none();
    }

    @Override
    public Option<SimpleFeatureType> featureType() {
        Option<FeatureSource<SimpleFeatureType, SimpleFeature>> option = createFeatureSource();
        if (option.isNone()) {
            return Option.none();
        }
        return Option.some(option.get().getSchema());
    }
}
