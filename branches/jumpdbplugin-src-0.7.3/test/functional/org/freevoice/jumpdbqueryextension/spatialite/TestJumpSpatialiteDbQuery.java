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
package functional.org.freevoice.jumpdbqueryextension.spatialite;

import junit.framework.TestCase;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import java.util.List;
import java.io.File;

import org.freevoice.jumpdbqueryextension.spatialite.JumpSpatialiteDbQuery;

/**
 *
 */
public class TestJumpSpatialiteDbQuery extends TestCase
{
   JumpSpatialiteDbQuery dbQuery = new JumpSpatialiteDbQuery();


   public static void main(String[] args) throws Exception
   {
      junit.textui.TestRunner.run(TestJumpSpatialiteDbQuery.class);
   }

   public void setUp() throws Exception
   {

      File dbFile = new File("./test/data/spatialite_test.db");
      dbQuery.setupDb("org.sqlite.JDBC", "jdbc:sqlite:" + dbFile.getCanonicalPath(),
            "no username needed", "no password needed");
   }

   public void tearDown() throws Exception
   {
   }

   //Test case where database follows FDO RFC 16, and uses WKB encoding for the
   //geometry
   public void testFdoWkb() throws Exception
   {
      String testQuery = "SELECT GEOMETRY, iso_code, name FROM country_shape_wkb";

      FeatureCollection featureCollection = dbQuery.getCollection(testQuery, 100);

      verifyFdo(featureCollection);
   }

   //Test case where database follows FDO RFC 16, and uses WKT encoding for the
   //geometry
   public void testFdoWkt() throws Exception
   {
      String testQuery = "SELECT GEOMETRY, iso_code, name FROM country_shape_wkt";

      FeatureCollection featureCollection = dbQuery.getCollection(testQuery, 100);

      verifyFdo(featureCollection);

   }

   private void verifyFdo(FeatureCollection featureCollection)
   {
      assertEquals("Should only be one feature", 1, featureCollection.size());

      List features = featureCollection.getFeatures();
      Feature feature = (Feature) features.get(0);

      String isoCode = feature.getString("iso_code");
      assertEquals("isoCode should be USA", "USA", isoCode);

      String name = feature.getString("name");
      assertEquals("name should be United States", "United States", name);

      Geometry geometry = feature.getGeometry();

      printGeometry(geometry);


      assertTrue("Geometry should be a polygon was a " + geometry.getClass().getName(), geometry instanceof Polygon);
   }

   public void testPlainSpatialite() throws Exception
   {


      String testQuery = "SELECT g, color FROM geomtest where color = 'redpoint'";

      FeatureCollection featureCollection = dbQuery.getCollection(testQuery, 100);

      assertEquals("Should only be one feature", 1, featureCollection.size());

      List features = featureCollection.getFeatures();

      Geometry geometry = null;
      for (int i = 0; i < features.size(); i++)
      {
         Feature feature = (Feature) features.get(i);

         String color = feature.getString("color");

         assertEquals("Color should be redpoint", "redpoint", color);

         geometry = feature.getGeometry();
      }


      printGeometry(geometry);


      assertTrue("Geometry should be a point was a " + geometry.getClass().getName(),
            geometry instanceof Point);

      Coordinate coordinate = ((Point) geometry).getCoordinate();

      assertEquals("X should be 1 ", 1.0D, coordinate.x, 0.0001D);
      assertEquals("Y should be 1 ", 1.0D, coordinate.y, 0.0001D);

   }

   private void printGeometry(Geometry geometry)
   {
      Coordinate[] coordinates = geometry.getCoordinates();

      for (int i = 0; i < coordinates.length; i++)
      {
         Coordinate coordinate = coordinates[i];
         System.err.println("Coordinate " + i + ": " + coordinate);
      }

   }
}
