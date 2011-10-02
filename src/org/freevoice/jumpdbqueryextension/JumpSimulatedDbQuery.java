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
package org.freevoice.jumpdbqueryextension;

import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 *
 */
public class JumpSimulatedDbQuery implements JumpDbQuery
{
    GeometryFactory _geometryFactory = null;

    public void setupDb(String driverClass, String dbUrl, String username, String password) throws Exception
    {
        _geometryFactory = new GeometryFactory();
    }

    public FeatureCollection getCollection(String query, int maxFeatures) throws Exception
    {

        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("Geometry", AttributeType.GEOMETRY);
        featureSchema.addAttribute("Name", AttributeType.STRING);
        featureSchema.addAttribute("Code", AttributeType.INTEGER);

        FeatureCollection featureCollection = new FeatureDataset(featureSchema);

        for(int i=0; i<10; i++)
        {
          addFeature(randomTriangle(), featureCollection);
        }


        return featureCollection;
    }

   public boolean collectionHasNulls()
   {
      return false; 
   }


   private void addFeature(Geometry geometry,
                            FeatureCollection featureCollection)
    {
        Feature feature =
                new BasicFeature(featureCollection.getFeatureSchema());
        feature.setAttribute("Geometry", geometry);
        feature.setAttribute("Name",  "" + (int) (Math.random() * 100000));
        feature.setAttribute("Code", new Integer((int) (Math.random() * 100)));

        featureCollection.add(feature);
    }


    private Geometry randomTriangle()
    {
        int perturbation = 30;

        int x = (int) (Math.random() * 700);
        int y = (int) (Math.random() * 700);
        Coordinate firstPoint = perturbedPoint(x, y, perturbation);


        return _geometryFactory.createPolygon(_geometryFactory.createLinearRing(new Coordinate[]{
            firstPoint,
            perturbedPoint(x, y, perturbation),
            perturbedPoint(x, y, perturbation),
            firstPoint}),
                null);
    }

    private Coordinate perturbedPoint(int x, int y, int perturbation)
    {
        return new Coordinate(x + (Math.random() * perturbation),
                y + (Math.random() * perturbation));
    }

}
