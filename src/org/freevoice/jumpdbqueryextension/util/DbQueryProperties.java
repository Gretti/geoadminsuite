/*
* 
*  The JUMP DB Query Plugin is Copyright (C) 2007  Larry Reeder
*  JUMP is Copyright (C) 2003 Vivid Solutions
* 
*  This file is part of the JUMP DB Query Plugin.
*  
*  The JUMP DB Query Plugin is free software; you can redistribute it and/or 
*  modify it under the terms of the Lesser GNU General Public License as 
*  published *  by the Free Software Foundation; either version 3 of the 
*  License, or  (at your option) any later version.
*  
*  This software is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  Lesser GNU General Public License for more details.
*  
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.freevoice.jumpdbqueryextension.util;

import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Properties class for DB Query Plugin
 */
public class DbQueryProperties
{
    private static final String DBLIST = "jump.dbquery.dblist";
    private static final String QUERY_CLASS_NAME = "jump.dbquery.queryclass.";
    private static final String DRIVER_CLASS_NAME = "jump.dbquery.driver.";
    private static final String JDBC_URL = "jump.dbquery.jdbcurl.";
    private static final String USERNAME = "jump.dbquery.username.";
    private static final String PASSWORD = "jump.dbquery.password.";

    private List<DbConnectionParameters> dbConnectionParameterList = new ArrayList<DbConnectionParameters>();

    /**
     * Create a DbQueryProperties class given the properties file name.
     *
     * @param propertyFileName File to use for DB Query Properties
     * @throws IOException Thrown if there is a problem reading the properties file.
     */
    public DbQueryProperties(String propertyFileName) throws IOException
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertyFileName));


        String dbList = properties.getProperty(DBLIST);

        String[] dbArray = dbList.split(",");

        for (String db : dbArray)
        {
            String queryClassName = properties.getProperty(QUERY_CLASS_NAME + db);

            if (queryClassName == null)
            {
                throw new IOException("Query class name not set for database named \"" + db + "\". Set property " + queryClassName);
            }

            String driverClass = properties.getProperty(DRIVER_CLASS_NAME + db);

            if (driverClass == null)
            {
                throw new IOException("Driver class not set for database named \"" + db + "\". Set property " + driverClass);
            }


            String jdbcUrl = properties.getProperty(JDBC_URL + db, "");
            String username = properties.getProperty(USERNAME + db, "");
            String password = properties.getProperty(PASSWORD + db, "");

            DbConnectionParameters dbConnectionParameters =
                    new DbConnectionParameters(db, queryClassName, driverClass,
                            jdbcUrl,
                            username,
                            password);

            dbConnectionParameterList.add(dbConnectionParameters);
        }


    }


    public List<DbConnectionParameters> getConnectionParameters()
    {
        return dbConnectionParameterList;
    }
}
