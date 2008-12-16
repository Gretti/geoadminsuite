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

package org.geogurus.data.database;

import org.geogurus.data.AbstractDataAccessFactory;
import org.geogurus.data.ConnectionParams;
import org.geogurus.data.Datasource;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.MsLayer;

/**
 * Factory for creating {@link PostgisDataAccess} objects
 * 
 * @author jesse
 */
public class PostgisAccessFactory extends AbstractDataAccessFactory {

    public boolean canCreateFrom(ConnectionParams host) {
        return "PG".equalsIgnoreCase(host.type)
                || "Postgis".equalsIgnoreCase(host.type);
    }

    public PostgisDataAccess createOne(ConnectionParams params) {
        PostgisDataAccess dataAccess = new PostgisDataAccess(params.host,
                params.dbname, params.schema, params.table, params.port,
                params.username, params.password, params.owner);
        return dataAccess;
    }

    public Option<ConnectionParams> createConnectionParameters(Map map,
            Layer layer, Datasource owner) {
        if (layer.getConnection() == null
                || layer.getConnectionType() != MsLayer.POSTGIS) {
            return Option.none();
        }
        // must parse data and connection String parameters to guess
        // table name
        // and host configuration
        String[] params = layer.getConnection().split("host=");
        ConnectionParams connectionParams = new ConnectionParams(owner);
        String[] paramParts = params[1].split(" ");
        if (params.length > 1) {
            connectionParams.host = paramParts[0];
        }
        params = layer.getConnection().split("dbname=");
        if (params.length > 1) {
            connectionParams.dbname = paramParts[0];
        }
        params = layer.getConnection().split("schema=");
        if (params.length > 1) {
            connectionParams.schema = paramParts[0];
        }
        params = layer.getConnection().split("user=");
        if (params.length > 1) {
            connectionParams.username = paramParts[0];
        }
        params = layer.getConnection().split("password=");
        if (params.length > 1) {
            connectionParams.password = paramParts[0];
        }
        params = layer.getConnection().split("port=");
        if (params.length > 1) {
            connectionParams.port = paramParts[0];
        }
        // should also handle complex queries (shape from
        // (select...)
        int idx = layer.getData().toUpperCase().indexOf("FROM");
        if (idx >= 0) {
            // table is the first word after from keyword
            connectionParams.table = layer.getData().substring(idx).split(" ")[1];
        } else {
            logger
                    .warning("cannot find from clause for OracleSpatial layer. data is: "
                            + layer.getData());
            return Option.none();
        }
        return Option.some(connectionParams);

    }
}
