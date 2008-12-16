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

package org.geogurus.gas.managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geogurus.data.Datasource;
import org.geogurus.data.Factory;
import org.geogurus.data.database.PostgisDatasource;
import org.geogurus.gas.objects.HostDescriptorBean;
import org.geogurus.tools.sql.ConPool2;

public final class PostgisFactoryStrategy implements
        Factory<List<Datasource>, HostDescriptorBean> {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    public boolean canCreateFrom(HostDescriptorBean host) {
        return "PG".equalsIgnoreCase(host.getType());
    }

    public List<Datasource> create(HostDescriptorBean host) {
        // an postgres host is a cluster that can contain several databases.
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        try {
            ConPool2 conPool = ConPool2.getInstance();
            String conURI = conPool.getConnectionURI(host.getName(), host
                    .getPort(), host.getInstance(), host.getUname(), host
                    .getUpwd(), ConPool2.DBTYPE_POSTGRES);
            logger.warning("cnx uri: " + conURI);
            con = conPool.getConnection(conURI);
            if (con == null) {
                // not a valid host: skip it
                logger
                        .warning("Cannot get a connection (null) for: "
                                + host.getName() + " " + host.getPort() + " "
                                + host.getInstance() + " " + host.getUname()
                                + " *****");
                return null;
            }
            List<Datasource> res = new ArrayList<Datasource>();
            stmt = con.createStatement();
            // this query gives all available databases for the given host,
            // INCLUDING template1
            // and postgres database instances.
            // New: No more database exclusion to avoid missing some geographic
            // data.
            // PG template mechanism should be used if some instances should not
            // be available
            // in the GAS.
            query = "select datname from pg_database where datallowconn";
            logger.fine("query to get datasources: " + query);
            rs = stmt.executeQuery(query.toString());

            while (rs.next()) {
                PostgisDatasource ds = new PostgisDatasource(rs.getString(1),
                        host.getPort(), host.getName(), host.getUname(), host
                                .getUpwd());
                if (ds.load()) {
                    // a valid datasource containing geo data: either file or
                    // geo tables
                    res.add(ds);
                }
            }
            return res;
        } catch (SQLException sqle) {
            String error = "HostLoader getDatasources: SQLException: "
                    + sqle.getMessage();
            error += "<br>query was: <code>" + query.toString() + "</code>";
            logger.warning(error);
            return null;
            // sqle.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception sqle2) {
                // sqle2.printStackTrace();
            }
        }
    }

}
