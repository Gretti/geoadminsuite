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
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Access a Tiff Format
 * 
 * @author jesse
 */
public class TiffDataAccess extends DataAccess {

    private static final long serialVersionUID = 1L;
    private File file;
    /** the DB major version for this datasource */
    protected int dbVersion;

    public TiffDataAccess(String name, String fullpath, Datasource owner) {
        super(name, owner, DataAccessType.TIFF);

        file = new File(fullpath);
    }

    @Override
    public boolean loadMetadata() {
        numGeometries = 1;
        geometryAttributeName = "_tiff_file_";
        geomTypeCode = Geometry.RASTER;
        if (metadata == null) {
            // empty column information for rasters
            metadata = new Vector<GeometryClassFieldBean>();
        }
        try {
            GeoTiffReader reader = new GeoTiffReader(file, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
            GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
            CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
            SRText = crs.toWKT();
            SRID = CRS.lookupEpsgCode(crs, true).intValue();
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            SRText = DefaultGeographicCRS.WGS84.toWKT();
            SRID = 4326;
        }

        // computes tif extent based on its tfw file
        this.extent = RasterRegistrar.getTifExtent(file.getAbsolutePath());
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
