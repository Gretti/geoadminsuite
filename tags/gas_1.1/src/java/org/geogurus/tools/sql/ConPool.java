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

package org.geogurus.tools.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Description: provides a dummy method to get a SQL connection to a DB
 * MUST change this class name.
 * @author Nicolas Ribot
 * @version 1.0
 */
public class ConPool {
    /** the keyword to get a Simple Connection from the default PostgreSQL JDBC driver
     *  postgresql.jar must be in the classpath
     */
    public static final byte POSTGRES = 0;
    /** the keyword to get a Simple Connection from the jXDBCon JDBC driver
     *  jxDBCon-net-jdbc3-0.9b.jar must be in the classpath
     */
    public static final byte JXDBCON  = 1;
    /** the keyword to get a Poolman Pooled Connection.
     * Underlying physical driver is the default postgresql one
     *  All poolman jars must be in the classpath
     */
    public static final byte POOLMAN  = 2;
    
    /**types of database*/
    public static final String DBTYPE_POSTGRES = "postgres";
    public static final String DBTYPE_ORACLE = "oracle";
    public static final String DBTYPE_DB2 = "DB2";
    
    
    /**The message generated by this Class in case of error*/
    public static String msg = "";
    
    public static transient Logger logger = Logger.getLogger(ConPool.class.getName());
    
    public static Connection getConnection(String dbhost, String dbport, String dbname, String dbuser, String dbpwd) {
        java.sql.Connection conn= null;
        try {
            String dbURL="jdbc:postgresql://" + dbhost + ":" + dbport + "/" + dbname;
            Class driver=Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(dbURL,dbuser,dbpwd);
        } catch (ClassNotFoundException cnfe) {
            msg += "\n<br>" + cnfe.getMessage();
            logger.severe(msg);
        } catch (SQLException sqle) {
            msg += "\n<br>" + sqle.getMessage();
            logger.severe(msg);
        }
        return conn;
    }
    public static Connection getConnection(String dbhost,
                                           String dbport,
                                           String dbname,
                                           String dbuser,
                                           String dbpwd,
                                           String conType) {
        java.sql.Connection conn= null;
        String driverClass = "";
        String dbURL = "";
        
        if(conType.equalsIgnoreCase(DBTYPE_POSTGRES)) {
            dbURL = "jdbc:postgresql:";
            
            if (conType == null || conType.equals("jxdb")) {
                driverClass = "org.sourceforge.jxdbcon.JXDBConDriver";
                dbURL += "net//";
            } else {
                driverClass = "org.postgresql.Driver";
                dbURL += "//";
            }
            dbURL += dbhost + ":" + dbport + "/" + dbname;
        } else if(conType.equalsIgnoreCase(DBTYPE_ORACLE)) {
            driverClass = "oracle.jdbc.driver.OracleDriver";
            dbURL = "jdbc:oracle:thin:@";
            dbURL += dbhost + ":" + dbport + ":" + dbname;
        }
        
        try {
            Class driver=Class.forName(driverClass);
            conn = DriverManager.getConnection(dbURL,dbuser,dbpwd);
        } catch (ClassNotFoundException cnfe) {
            logger.severe(cnfe.getMessage());
        } catch (Exception sqle) {
            logger.severe(sqle.getMessage());
        }
        return conn;
    }
    
}