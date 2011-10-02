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
package org.freevoice.jumpdbqueryextension.oracle;

import junit.framework.TestCase;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

/**
 *
 */
public class TestJumpOracleDbQuery extends TestCase
{

    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(TestJumpOracleDbQuery.class);
    }

    public void setUp() throws Exception
    {
    }

    public void tearDown() throws Exception
    {
    }

    public void testDbQuery() throws Exception
    {

       JumpOracleDbQuery dbQuery = new JumpOracleDbQuery();

        //FIXME load from testing properties file
        dbQuery.setupDb("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@localhost:1521:TEST",
                "scott", "tiger");

        String testQuery = "SELECT GEOMETRY, ID FROM GEOMTEST";

        FeatureCollection featureCollection = dbQuery.getCollection(testQuery, 100);
        List features = featureCollection.getFeatures();

        assertTrue("There should be at least one feature", features.size() >=1 );

          for (int i = 0; i < features.size(); i++)
          {
             Feature feature =  (Feature) features.get(i);

              Geometry geometry =  feature.getGeometry();

//              Double id = feature.getDouble(1);
              Object id = feature.getAttribute("ID");

          }


    }
}
