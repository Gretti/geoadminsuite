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

package org.geogurus.data.files;

import java.util.Vector;

import org.geogurus.data.AbstractDataAccessFactory;
import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.DatasourceType;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;

/**
 * A Datasource that is a MapFile the contained DataAccess objects may be almost
 * any type.
 * 
 * @author jesse
 */
public class MapFileDatasource extends Datasource {
    private static final long serialVersionUID = 1L;
    /** the mapfile object in case of MAP datasource type (null for other types) */
    protected Map mapfile;

    public MapFileDatasource(String name, String host) {
        super(name, host, DatasourceType.MAPFILE);
    }

    /**
     * Creates a new instance of type MapFileDatasource
     * 
     */
    public MapFileDatasource() {
    }

    public boolean load() {
        org.geogurus.mapserver.MapFile mf = new org.geogurus.mapserver.MapFile(
                this.name);
        org.geogurus.mapserver.objects.Map map = mf.load();
        this.mapfile = map;

        if (map != null) {
            Vector<Layer> layers = map.getLayers();
            if (layers == null) {
                logger.warning("no layers for mapfile: "
                        + map.getMapFile().getAbsolutePath());
                return false;
            }

            for (Layer layer : layers) {
                DataAccess gc = null;
                for (DataAccessType factoryEnum : DataAccessType.values()) {
                    AbstractDataAccessFactory factory = factoryEnum.factory();
                    Option<ConnectionParams> params = factory
                            .createConnectionParameters(map, layer, this);
                    if (params.isSome() && factory.canCreateFrom(params.get())) {
                        ConnectionParams connectionParams = params.get();
                        gc = factory.createOne(connectionParams);
                        logger.info("adding layer: " + connectionParams);
                        break;
                    }
                }
                if (gc != null) {
                    if (layer.getName() != null) {
                        gc.setName(layer.getName());
                    }
                    gc.setMSLayer(layer);
                    getDataList().put(gc.getID(), gc);
                } else {
                    logger.warning("Unable to load MapServer layer: " + layer
                            + "\nlayer not added into dataList");
                }
            }
            return true;
        } else {
            logger
                    .warning("Datasource.getMapFileDataInformation(): cannot load mapfile: "
                            + this.name);
            return false;
        }
    }

    @Override
    public <T> Option<T> resource(Class<T> resourceType) {
        if (Map.class.isAssignableFrom(resourceType)) {
            return Option.some(resourceType.cast(getMapfile()));
        }
        return null;
    }

    /**
     * returns the mapfile in case of MAP ds type, or null in other cases
     * 
     * @return
     */
    public Map getMapfile() {
        return mapfile;
    }

}
