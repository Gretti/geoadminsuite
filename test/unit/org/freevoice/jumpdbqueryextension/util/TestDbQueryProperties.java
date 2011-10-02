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
package unit.org.freevoice.jumpdbqueryextension.util;


import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;
import java.util.List;
import org.freevoice.jumpdbqueryextension.util.DbConnectionParameters;
import org.freevoice.jumpdbqueryextension.util.DbQueryProperties;

public class TestDbQueryProperties extends TestCase
{
   private static final String TEST_FILE = "build/test/classes/unit/org/freevoice/jumpdbqueryextension/util/dbquerytest.properties";

    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(TestDbQueryProperties.class);
    }

    public void setUp() throws Exception
    {
    }

    public void tearDown() throws Exception
    {
    }

    public void testDbQueryProperties() throws IOException
    {
        File propertiesFile = new File(TEST_FILE);
        DbQueryProperties dbQueryProperties = new DbQueryProperties(propertiesFile.getCanonicalPath());

        List<DbConnectionParameters> connectionParameterList = dbQueryProperties.getConnectionParameters();

        assertTrue("Should have two in the list ", connectionParameterList.size() > 0);

        DbConnectionParameters connectionParameters = connectionParameterList.get(0);

        assertEquals("queryclass1", connectionParameters.getClassName());
        assertEquals("driver1", connectionParameters.getDriverClass());
        assertEquals("jdbcurl1", connectionParameters.getJdbcUrl());
        assertEquals("username1", connectionParameters.getUsername());
        assertEquals("password1", connectionParameters.getPassword());

        connectionParameters = connectionParameterList.get(1);

        assertEquals("queryclass2", connectionParameters.getClassName());
        assertEquals("driver2", connectionParameters.getDriverClass());
        assertEquals("jdbcurl2", connectionParameters.getJdbcUrl());
        assertEquals("username2", connectionParameters.getUsername());
        assertEquals("password2", connectionParameters.getPassword());

    }

}
