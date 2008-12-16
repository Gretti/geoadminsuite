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
package org.geogurus.data.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.Datasource;
import org.geogurus.data.DatasourceType;
import org.geogurus.data.Option;
import org.geogurus.tools.sql.ConPool2;

/**
 * Can connect to and load data from oracle
 * 
 * @author jesse
 */
public class OracleDatasource extends Datasource {
    private static final long serialVersionUID = 1L;
    protected String dbPort = "5432";
    protected String userName = null;
    protected String userPwd = null;

    public OracleDatasource(String name, String dbPort, String host,
            String userName, String userPwd) {
        super(name, host, DatasourceType.VECTOR);
        this.dbPort = dbPort;
        this.userName = userName;
        this.userPwd = userPwd;
    }

    /**
     * Creates a new instance of type OracleDatasource
     * 
     */
    public OracleDatasource() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Finds all geographic tables for this db, and construct their
     * geometryClass equivalent. All these geometryClasses are stored in the
     * Datasource dataList hashtable, with the gc'id as a key. The
     * geometryClasses built here have the minimal set of information.
     */
    public boolean load() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        boolean ret = false;
        DataAccess gc = null;
        try {
            ConPool2 conPool = ConPool2.getInstance();
            con = conPool.getConnection(conPool.getConnectionURI(this.host,
                    this.dbPort, this.name, this.userName, this.userPwd,
                    ConPool2.DBTYPE_ORACLE));
            if (con == null) {
                // not a valid host: skip it
                logger.warning("Cannot get a connection (null) for: "
                        + this.host + " " + this.dbPort + " " + this.name + " "
                        + this.userName + " *****");
                return false;
            }

            stmt = con.createStatement();
            // this query gives geographic tables for the configure oracle
            // instance
            query = "select TABLE_NAME, TABLE_SCHEMA from MDSYS.USER_SDO_GEOM_METADATA order by TABLE_SCHEMA, TABLE_NAME";
            logger.fine("Query to get geo tables: " + query);

            rs = stmt.executeQuery(query.toString());

            final int tableNameColumnIndex = 1;
            final int schemaColumnIndex = 2;
            OracleAccessFactory factory = new OracleAccessFactory();
            while (rs.next()) {
                ConnectionParams bean = new ConnectionParams(this);
                bean.host = host;
                bean.dbname = name;
                bean.port = dbPort;
                bean.username = userName;
                bean.password = userPwd;
                bean.schema = rs.getString(schemaColumnIndex);
                bean.table = rs.getString(tableNameColumnIndex);
                gc = factory.createOne(bean);
                // no unique field test for Oracle (has to be confirmed...
                getDataList().put(gc.getID(), gc);
            }
            rs.close();
            stmt.close();
            ret = true;

        } catch (SQLException sqle) {
            String error = "HostLoader getDatasources: SQLException: "
                    + sqle.getMessage();
            error += "<br>query was: <code>" + query.toString() + "</code>";
            logger.warning(error);
            sqle.printStackTrace();
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
                sqle2.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public <T> Option<T> resource(Class<T> resourceType) {
        return null;
    }

}
