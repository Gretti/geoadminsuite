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

package org.geogurus.data.webservices;

import java.net.URL;

import org.geogurus.data.AbstractDataAccessFactory;
import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.Datasource;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.MetaData;
import org.geogurus.mapserver.objects.MsLayer;

/**
 * @author jesse
 * 
 */
public class WfsDataAccessFactory extends AbstractDataAccessFactory {

    @Override
    public Option<ConnectionParams> createConnectionParameters(Map mapfile,
            Layer layer, Datasource owner) {
        if (layer.getConnectionType() != MsLayer.WFS) {
            return Option.none();
        }
        MetaData metaData = layer.getMetaData();
        if (metaData == null) {
            return Option.none();
        }
        Option<String> typename = metaData
                .getAttributes(WfsDataAccess.MS_METADATA_WFS_TYPENAME);
        if (typename.isNone()) {
            return Option.none();
        }
        String connection = layer.getConnection();
        if (connection == null) {
            return Option.none();
        }

        ConnectionParams params = new ConnectionParams(owner);
        params.type = "wfs";
        params.host = connection;
        params.name = layer.getName();
        params.typename = typename.get();
        return Option.some(params);
    }

    @Override
    public DataAccess createOne(ConnectionParams params) {
        String host = params.host;
        String name = params.name;
        String typename = params.typename;
        Datasource owner = params.owner;

        return new WfsDataAccess(host, name, typename, owner);
    }

    public boolean canCreateFrom(ConnectionParams params) {
        return "wfs".equalsIgnoreCase(params.type) && params.host != null
                && params.name != null;
    }

    public static ConnectionParams createParams(WfsDatasource owner,
            URL serverURL, String typename) {
        ConnectionParams params = new ConnectionParams(owner);
        params.type = "wfs";
        params.name = typename;
        params.typename = typename;
        params.host = serverURL.toExternalForm();
        return params;
    }
}
