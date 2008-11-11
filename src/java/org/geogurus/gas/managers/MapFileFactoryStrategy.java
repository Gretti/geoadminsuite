/**
 * 
 */
package org.geogurus.gas.managers;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.geogurus.data.Datasource;
import org.geogurus.data.files.MapFileDatasource;
import org.geogurus.gas.objects.HostDescriptorBean;

/**
 * Strategy for processing MapFiles into Datasources
 * 
 * @author jeichar
 */
public class MapFileFactoryStrategy extends AbstractDatasourceFactoryStrategy {

    @Override
    public boolean canCreateFrom(File file) {
        String name = file.getName();
        // first gets extension:
        String ext = "";
        int pt = name.lastIndexOf(".");
        try {
            ext = name.substring(pt + 1);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        if (ext.equalsIgnoreCase("map") && file.exists()) {
            return true;
        }
        return false;
    }

    @Override
    protected String getTypeString() {
        return "mapfile";
    }

    @Override
    public boolean canCreateFrom(HostDescriptorBean host) {
        File file = new File(host.getPath());
        return getTypeString().equalsIgnoreCase(host.getType())
                && canCreateFrom(file);
    }

    /**
     * Finds all mapfile layers for the given datasource, and construct their
     * geometryClass equivalent. All these geometryClasses are stored in the
     * Dataousrce's dataList hashtable, with the gc'id as a key. The
     * geometryClasses built here have the minimal set of information.<br>
     * Use the Datasource.getGeometryClasses(id) method to get the list of
     * GeometryClass for a given mapfile.
     * <p>
     * 
     * Geographic files are those whose extensions adhere to the
     * FilenameFilter.accept method
     * @param ds
     *            The Folder-type Datasource object to search geo files for.
     */
    public List<Datasource> create(HostDescriptorBean bean) {
        MapFileDatasource ds = new MapFileDatasource(bean.getPath(), bean
                .getName());

        if (ds.load()) {
            return Collections.singletonList((Datasource) ds);
        } else {
            return null;
        }
    }

}
