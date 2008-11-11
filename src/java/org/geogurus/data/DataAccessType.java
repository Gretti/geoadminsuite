package org.geogurus.data;

import static org.geogurus.data.DatasourceType.RASTER;
import static org.geogurus.data.DatasourceType.VECTOR;

import java.io.Serializable;

import org.geogurus.data.database.OracleAccessFactory;
import org.geogurus.data.database.PostgisAccessFactory;
import org.geogurus.data.files.AbstractFileAccessFactory;
import org.geogurus.data.files.EcwAccessFactory;
import org.geogurus.data.files.ImgAccessFactory;
import org.geogurus.data.files.MifAccessFactory;
import org.geogurus.data.files.ShpAccessFactory;
import org.geogurus.data.files.TiffAccessFactory;
import org.geogurus.data.webservices.WfsDataAccessFactory;
import org.geogurus.data.webservices.WmsAccessFactory;

/**
 * Enumerates all of the DataAccessTypes. This is used by the jsp files as well
 * as is used to find the available Factories
 * 
 * @author jesse
 */
public enum DataAccessType implements Serializable {
    /** ECW file type declaration */
    ECW(RASTER, new EcwAccessFactory(), "ecwfile"),
    /** IMG file type declaration */
    IMG(RASTER, new ImgAccessFactory(), "imgfile"),
    /** Shape File type declaration */
    SHP(VECTOR, new ShpAccessFactory(), "shapefile"),
    /** Tiff file type declaration */
    TIFF(RASTER, new TiffAccessFactory(), "tifffile"),
    /** Oracle database type declaration */
    ORACLE(VECTOR, new OracleAccessFactory(), "oracle"),
    /** Postgis Database type declaration */
    POSTGIS(VECTOR, new PostgisAccessFactory(), "postgis"),
    /** WMS type declaration */
    WMS(RASTER, new WmsAccessFactory(), "wms"),
    /** MIF file type declaration */
    MIF(VECTOR, new MifAccessFactory(), "mif"),
    /** WFS type declaration */
    WFS(VECTOR, new WfsDataAccessFactory(), "wfs");

    private final AbstractDataAccessFactory factory;
    private final boolean isFileFormat;
    private String displayname;
    private DatasourceType type;

    private DataAccessType(DatasourceType type,
            AbstractDataAccessFactory factory, String displayname) {
        this.factory = factory;
        isFileFormat = factory instanceof AbstractFileAccessFactory;
        this.displayname = displayname;
        this.type = type;
    }

    /**
     * returns the factory used to create this type of DataAccess
     */
    public AbstractDataAccessFactory factory() {
        return factory;
    }

    /**
     * Returns true if the file format is a file format and the factory is of
     * type {@link AbstractFileAccessFactory}
     */
    public boolean isFileFormat() {
        return isFileFormat;
    }

    /**
     * Returns the "displayable" name of the DataAccess type
     */
    public String displayname() {
        return displayname;
    }

    public DatasourceType getType() {
        return type;
    }

}
