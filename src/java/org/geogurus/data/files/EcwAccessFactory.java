package org.geogurus.data.files;

import static org.geogurus.data.files.FileFactoryHelper.getExtension;
import static org.geogurus.data.files.FileFactoryHelper.getExtensionlessName;

import java.io.File;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;

/**
 * Creates {@link EcwDataAccess} objects
 * 
 * @author jesse
 */
public class EcwAccessFactory extends AbstractFileAccessFactory {

    @Override
    public boolean canCreateFrom(File file) {
        String ext = getExtension(file);
        String begin = getExtensionlessName(file);
        if (file.isFile() && ext.equalsIgnoreCase("ecw")) {
            return (new File(file.getParent(), begin + ".ers").exists() || new File(
                    file.getParent(), begin + ".ERS").exists());
        }

        return false;
    }

    public DataAccess createOne(ConnectionParams host) {
        DataAccess dataAccess = new EcwDataAccess(host.name, host.path,
                host.owner);
        return dataAccess;
    }

}
