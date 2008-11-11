package org.geogurus.data.files;

import java.io.File;

import org.geogurus.data.AbstractDataAccessFactory;
import org.geogurus.data.ConnectionParams;
import org.geogurus.data.Datasource;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.MsLayer;

/**
 * Super class for all Factories that create filebased DataAccess objects
 * 
 * @author jesse
 */
public abstract class AbstractFileAccessFactory extends
        AbstractDataAccessFactory {

    public final boolean canCreateFrom(ConnectionParams host) {
        String path = host.path;
        if (path != null) {
            return canCreateFrom(new File(path));
        }
        return false;
    }

    public abstract boolean canCreateFrom(File file);

    @Override
    public final Option<ConnectionParams> createConnectionParameters(Map map,
            Layer layer, Datasource owner) {

        if (layer.getConnectionType() != MsLayer.LOCAL) {
            return Option.none();
        }

        File mf = map.getMapFile();

        File dataFile;
        if (layer.getData() != null) {
            dataFile = new File(layer.getData());

            if (!dataFile.exists()) {
                if (map.getShapePath() != null) {
                    String shapePath = map.getShapePath().getAbsolutePath();
                    dataFile = new File(map.getShapePath(), layer.getData());
                    if (!dataFile.exists()) {
                        dataFile = new File(mf.getParentFile(), shapePath
                                + File.separator + layer.getData());
                        if (!dataFile.exists()) {
                            dataFile = new File(mf.getParent(), layer.getData());
                            if (!dataFile.exists()) {
                                logger
                                        .warning("Source file not found for layer : "
                                                + layer.getData());
                                dataFile = null;
                            }
                        }
                    }
                } else {
                    // a local name for layer, relative to mapfile
                    dataFile = new File(mf.getParent() + File.separator
                            + layer.getData());
                    if (!dataFile.exists()) {
                        dataFile = null;
                    }
                }

            }
        } else {
            logger
                    .warning("INLINE layers currently not supported (no DATA attribute for layer)");
            dataFile = null;
        }

        if (dataFile != null) {
            if (canCreateFrom(dataFile)) {
                layer.setData(dataFile.getParent() + File.separator
                        + dataFile.getName());
                ConnectionParams connectionParams = new ConnectionParams(owner);
                connectionParams.path = dataFile.getAbsolutePath();
                connectionParams.name = dataFile.getName();

                return Option.some(connectionParams);
            }
        }
        return Option.none();
    }
}
