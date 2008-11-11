package org.geogurus.data.files;

import static org.geogurus.data.files.FileFactoryHelper.getExtension;
import static org.geogurus.data.files.FileFactoryHelper.getExtensionlessName;

import java.io.File;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.raster.RasterRegistrar;

/**
 * Creates {@link TiffDataAccess} objects
 * 
 * @author jesse
 */
public class TiffAccessFactory extends AbstractFileAccessFactory {

    @Override
    public boolean canCreateFrom(File file) {
        String ext = getExtension(file);
        String begin = getExtensionlessName(file);
        if (file.isFile()
                && (ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff"))) {
            // verify if a tfw exists, or a wld, or
            boolean isWorldImage = new File(file.getParent(), begin + ".tfw")
                    .exists()
                    || new File(file.getParent(), begin + ".TFW").exists()
                    || new File(file.getParent(), begin + ".wld").exists()
                    || new File(file.getParent(), begin + ".WLD").exists();
            return RasterRegistrar.isGeoTIFF(file.getAbsolutePath())
                    || isWorldImage;
        }
        return false;
    }

    public DataAccess createOne(ConnectionParams host) {
        DataAccess dataAccess = new TiffDataAccess(host.name, host.path,
                host.owner);
        return dataAccess;
    }

}
