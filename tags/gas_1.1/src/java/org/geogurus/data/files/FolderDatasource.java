/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package org.geogurus.data.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.DatasourceType;
import org.geogurus.data.Option;

/**
 * Datasource representing a Filesystem folder
 * 
 * @author jesse
 */
public class FolderDatasource extends Datasource {
    private static final long serialVersionUID = 1L;
    /** should recurse into subdrectories */
    protected boolean recurse;

    static final List<AbstractFileAccessFactory> factories;
    static {
        ArrayList<AbstractFileAccessFactory> list = new ArrayList<AbstractFileAccessFactory>();

        for (DataAccessType factory : DataAccessType.values()) {
            if (factory.isFileFormat()) {
                list.add((AbstractFileAccessFactory) factory.factory());
            }
        }

        factories = Collections.unmodifiableList(list);
    }

    /**
     * Creates a new instance of type FolderDatasource
     * 
     */
    public FolderDatasource() {
    }

    public FolderDatasource(String name, String host, boolean recurse) {
        super(name, host, DatasourceType.FOLDER);
        this.recurse = recurse;
    }

    /**
     * Finds all valid geographic files for the given datasource, and construct
     * their geometryClass equivalent. All these geometryClasses are stored in
     * the Dataousrce's dataList hashtable, with the gc'id as a key. The
     * geometryClasses built here have the minimal set of information.<br>
     * Use the Datasource.getGeometryClasses(id) method to get the list of
     * GeometryClass for a given mapfile.
     * <p>
     * 
     * Geographic files are those whose extensions adhere to the
     * FilenameFilter.accept method
     * 
     * @param ds
     *            The Folder-type Datasource object to search geo files for.
     */
    public boolean load() {
        File filepath = new File(this.name);

        if (!filepath.isDirectory()) {
            // path is not a folder path
            logger.warning("The given path (" + this.name + ") is not valid");
            return false;
        }
        this.dataList.putAll(fillDataList(filepath, recurse));
        return true;
    }

    private Hashtable<String, DataAccess> fillDataList(File directory,
            boolean recurse) {
        FactoryFileFilter filter = new FactoryFileFilter(factories);
        Hashtable<String, DataAccess> dataHash = new Hashtable<String, DataAccess>();
        // Get files / directories in the directory
        File[] entries = directory.listFiles();
        // Go over entries
        for (File entry : entries) {

            if (filter.accept(entry)) {
                AbstractFileAccessFactory factory = findFactory(entry);
                ConnectionParams bean = new ConnectionParams(this);
                bean.name = entry.getName();
                bean.path = entry.getAbsolutePath();
                List<DataAccess> dataAccessList = factory.create(bean);
                for (DataAccess dataAccess : dataAccessList) {
                    dataAccess.setDatasourceName(name);
                    dataHash.put(dataAccess.getID(), dataAccess);
                }
            }

            // If the file is a directory and the recurse flag
            // is set, recurse into the directory
            if (recurse && entry.isDirectory()) {
                dataHash.putAll(fillDataList(entry, recurse));
            }
        }

        // Return collection of files
        return dataHash;
    }

    private AbstractFileAccessFactory findFactory(File entry) {
        for (AbstractFileAccessFactory factory : factories) {
            if (factory.canCreateFrom(entry)) {
                return factory;
            }
        }
        return null;
    }

    @Override
    public <T> Option<T> resource(Class<T> resourceType) {
        return null;
    }

}
