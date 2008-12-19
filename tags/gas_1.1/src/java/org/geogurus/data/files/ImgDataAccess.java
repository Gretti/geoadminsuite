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
package org.geogurus.data.files;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessHelper;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.Geometry;
import org.geogurus.data.Operation;
import org.geogurus.data.Option;
import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.raster.RasterRegistrar;
import org.geotools.data.Query;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Access to Img Files
 * 
 * @author jesse
 */
public class ImgDataAccess extends DataAccess {
    private static final long serialVersionUID = 1L;
    private File file;
    /** the DB major version for this datasource */
    protected int dbVersion;

    public ImgDataAccess(String name, String fullpath, Datasource owner) {
        super(name, owner, DataAccessType.IMG);

        file = new File(fullpath);
    }

    @Override
    public boolean loadMetadata() {
        numGeometries = 1;
        geometryAttributeName = "_img_file_";
        geomTypeCode = Geometry.RASTER;
        if (metadata == null) {
            // empty column information for rasters
            metadata = new Vector<GeometryClassFieldBean>();
        }
        SRText = null;

        // computes tif extent based on its tfw file
        this.extent = RasterRegistrar.getTifExtent(file.getAbsolutePath());
        // should manage the SRtext based on the ERS header file
        return true;
    }

    @Override
    public Vector<Vector<Object>> getSampleData(int from, int to) {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    protected Layer createMSLayerInner(RGB color) {
        return DataAccessHelper.createMapServerLayer(geomTypeCode, file, color);
    }

    protected String getFileName() {
        return file.getAbsolutePath();
    }

    @Override
    public String getConnectionURI() {
        return "file://" + getFileName();
    }

    @Override
    public <T> Option<Boolean> performOperateStep(Operation<SimpleFeature, T> op,
            T context, Query query) throws IOException {
        return Option.none();
    }

    @Override
    public <T> Option<Boolean> peformOperateStep(
            Operation<RenderedImage, T> op, T context) {
        // COMPLETE Auto-generated method stub
        return null;
    }

    @Override
    public <T> Option<T> resource(Class<T> request) {
        if (File.class.isAssignableFrom(request)) {
            return Option.some(request.cast(file));
        }
        return Option.none();
    }

    @Override
    public Option<SimpleFeatureType> featureType() {
        return Option.none();
    }

    @Override
    public ConnectionParams getConnectionParams() {
        ConnectionParams params = new ConnectionParams(owner);
        params.name = this.name;
        params.path = file.getAbsolutePath();
        params.type = datasourceType.name();
        return params;
    }
}
