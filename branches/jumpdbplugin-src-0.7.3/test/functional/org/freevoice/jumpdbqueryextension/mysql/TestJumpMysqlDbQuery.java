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
package functional.org.freevoice.jumpdbqueryextension.mysql;

import junit.framework.TestCase;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

import org.freevoice.jumpdbqueryextension.spatialite.JumpSpatialiteDbQuery;

/**
 *
 */
public class TestJumpMysqlDbQuery extends TestCase
{

    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(TestJumpMysqlDbQuery.class);
    }

    public void setUp() throws Exception
    {
    }

    public void tearDown() throws Exception
    {
    }

    public void testDbQuery() throws Exception
    {

       JumpSpatialiteDbQuery dbQuery = new JumpSpatialiteDbQuery();

        //FIXME load from testing properties file
        dbQuery.setupDb("org.gjt.mm.mysql.Driver", "jdbc:mysql://localhost/test",
                "yourusername", "yourpassword");

        String testQuery = "SELECT AsText(g), color FROM geomtest";

        FeatureCollection featureCollection = dbQuery.getCollection(testQuery, 100);

       assertEquals("Should only be one feature",  1, featureCollection.size());

        List features = featureCollection.getFeatures();

       for (int i = 0; i < features.size(); i++)
       {
          Feature feature =  (Feature) features.get(i);

          String color = feature.getString("color");

          assertEquals("Color should be red", "red", color);


          Geometry geometry =  feature.getGeometry();

       }


    }
}
