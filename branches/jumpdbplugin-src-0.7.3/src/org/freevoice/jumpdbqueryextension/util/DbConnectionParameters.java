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

/**
 * Parameters for the database connection
 */
public class DbConnectionParameters
{
    private String className = null;
    private String displayName = null;
    private String driverClass = null;
    private String jdbcUrl = "";
    private String username = "";
    private String password = "";


    public DbConnectionParameters(String displayName, String className, String driverClass, String jdbcUrl, String username, String password)
    {
        this.className = className;
        this.displayName = displayName;
        this.driverClass = driverClass;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }


    public String getClassName()
    {
        return className;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getDriverClass()
    {
        return driverClass;
    }

    public String getJdbcUrl()
    {
        return jdbcUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String toString()
    {
        return displayName;
    }
}
