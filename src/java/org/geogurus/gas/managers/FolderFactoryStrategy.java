package org.geogurus.gas.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geogurus.data.Datasource;
import org.geogurus.data.files.FolderDatasource;
import org.geogurus.gas.objects.HostDescriptorBean;
import org.geogurus.tools.util.FileLister;
import org.geogurus.tools.util.MapfileFilter;

public class FolderFactoryStrategy extends AbstractDatasourceFactoryStrategy {

    @Override
    public boolean canCreateFrom(HostDescriptorBean host) {
        return getTypeString().equalsIgnoreCase(host.getType());
    }

    public List<Datasource> create(HostDescriptorBean host) {
        List<Datasource> res = new ArrayList<Datasource>();
        boolean recurse = host.parseRecurse();

        // looks if mapfiles are present as files in the given folder. if so,
        // creates a new datasource for each mapfile
        Collection<File> mapfiles = FileLister.listFiles(new File(host
                .getPath()), new MapfileFilter(), recurse);
        for (File f : mapfiles) {
            logger.fine("mapfile found: " + f.getAbsolutePath());
            MapFileFactoryStrategy strategy = new MapFileFactoryStrategy();
            HostDescriptorBean bean = strategy.fileToBean(f, host, recurse);
            List<Datasource> list = strategy.create(bean);
            if (list == null || list.isEmpty()) {
                logger.warning("getGeoSources failed on datasource: "
                        + host.getName());
            } else {
                // a valid datasource containing geo data: either files or geo
                // tables or mapfile layers
                res.addAll(list);
            }
        }
        // Now mapfiles are managed for the given path, treats all other files
        // as possible candidates for a folder datasource type
        FolderDatasource ds = new FolderDatasource(host.getPath(), host
                .getName(), recurse);

        if (ds.load()) {
            // a valid datasource containing geo data: either file or geo tables
            res.add(ds);
        } else {
            logger.warning("getGeoSources failed on datasource: "
                    + host.getName() + "\n\tmessage is: " + ds.errorMessage);
        }

        if (res.isEmpty()) {
            return null;
        }

        return res;
    }

    @Override
    public boolean canCreateFrom(File file) {
        return file.isDirectory();
    }

    @Override
    protected String getTypeString() {
        return "FOLDER";
    }

}
