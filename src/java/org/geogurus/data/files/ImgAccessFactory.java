package org.geogurus.data.files;

import static org.geogurus.data.files.FileFactoryHelper.getExtension;
import static org.geogurus.data.files.FileFactoryHelper.getExtensionlessName;

import java.io.File;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;

/**
 * Creates {@link ImgDataAccess} objects
 * 
 * @author jesse
 */
public class ImgAccessFactory extends AbstractFileAccessFactory {

    @Override
    public boolean canCreateFrom(File file) {
        String ext = getExtension(file);
        String begin = getExtensionlessName(file);
        if (file.isFile() && ext.equalsIgnoreCase("img")) {
            return new File(file.getParent(), begin + ".wld").exists()
                    || new File(file.getParent(), begin + ".WLD").exists();
        }
        return false;
    }

    public DataAccess createOne(ConnectionParams host) {
        DataAccess imgDataAccess = new ImgDataAccess(host.name, host.path,
                host.owner);
        return imgDataAccess;
    }

}
