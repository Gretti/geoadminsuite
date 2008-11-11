package org.geogurus.data.files;

import static org.geogurus.data.files.FileFactoryHelper.getExtension;
import static org.geogurus.data.files.FileFactoryHelper.getExtensionlessName;

import java.io.File;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;

/**
 * Create {@link ShpDataAccess} objects
 * 
 * @author jesse
 */
public class ShpAccessFactory extends AbstractFileAccessFactory {

    @Override
    public boolean canCreateFrom(File file) {
        String ext = getExtension(file);
        String begin = getExtensionlessName(file);
        if (file.isFile() && ext.equalsIgnoreCase("shp")) {
            boolean dbfExists = new File(file.getParent(), begin + ".dbf").exists();
            boolean shxExists = new File(file.getParent(), begin + ".shx").exists();
            return dbfExists && shxExists;
        }
        if (file.isFile() && ext.equalsIgnoreCase("SHP")) {
            boolean dbfExists = new File(file.getParent(), begin + ".DBF").exists();
            boolean shxExists = new File(file.getParent(), begin + ".SHX").exists();
            return dbfExists && shxExists;
        }
        return false;
    }

    public DataAccess createOne(ConnectionParams host) {
        DataAccess dataAccess = new ShpDataAccess(host.name, host.path,
                host.owner);
        return dataAccess;
    }
}
