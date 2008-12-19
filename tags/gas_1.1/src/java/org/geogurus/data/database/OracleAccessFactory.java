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
import org.geogurus.data.DataAccess;
import org.geogurus.data.Datasource;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.MsLayer;

/**
 * A Factory for creating {@link OracleDataAccess} objects
 * 
 * @author jesse
 */
public class OracleAccessFactory extends AbstractDataAccessFactory {

    public boolean canCreateFrom(ConnectionParams host) {
        return "ORA".equalsIgnoreCase(host.type)
                || "Oracle".equalsIgnoreCase(host.type);
    }

    public DataAccess createOne(ConnectionParams params) {
        // GeometryClass dataAccess = new OracleDataAccess(host.getName(), host
        // .getInstance(), host.getSchema(), host.getTablename(), host
        // .getPort(), host.getUname(), host.getUpwd());
        DataAccess dataAccess = new OracleDataAccess(params.host,
                params.dbname, params.schema, params.table, params.port,
                params.username, params.password, params.owner);
        return dataAccess;
    }

    @Override
    public Option<ConnectionParams> createConnectionParameters(Map map,
            Layer layer, Datasource owner) {

        if (layer.getConnection() == null
                || layer.getConnectionType() != MsLayer.ORACLESPATIAL) {
            return Option.none();
        }
        ConnectionParams connectionParams = new ConnectionParams(owner);

        connectionParams.type = "oracle";
        // todo: how to handle oracle host is not contained in
        // connection string ?

        // must parse data and connectionString parameters to guess
        // table name
        // and host configuration. table name must be known to allow
        // GC to retrieve
        // attributes types and values.
        // should use regexp to extract connection attributes with
        // proper mask.
        // connectionString is of the form: user/pass[@db]
        String[] params = layer.getConnection().split("/");
        if (params.length == 1) {
            // no pwd provided
            params = layer.getConnection().split("@");
            if (params.length == 1) {
                // no user@host name, consider given value is host
                // name
                connectionParams.host = params[0];
            } else {
                connectionParams.username = params[0];
                connectionParams.host = params[1];
            }
        } else {
            connectionParams.username = params[0];
            params = params[1].split("@");
            connectionParams.password = params[0];
            connectionParams.host = params[1];
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
