package org.geogurus.data.files;

import java.io.File;

import org.geogurus.data.ConnectionParams;

/**
 * Creates MifDataAccess objects
 * 
 * @author jesse
 */
public class MifAccessFactory extends AbstractFileAccessFactory {

    @Override
    public boolean canCreateFrom(File file) {
        return FileFactoryHelper.getExtension(file).equalsIgnoreCase("mif")
                && (FileFactoryHelper.fileExists(file, "mid") || FileFactoryHelper.fileExists(file, "MID"));
    }

    public MifDataAccess createOne(ConnectionParams params) {
        return new MifDataAccess(new File(params.path), params.owner);
    }

}
