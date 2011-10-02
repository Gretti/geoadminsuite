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
package org.freevoice.jumpdbqueryextension.mysql;

import org.freevoice.jumpdbqueryextension.JumpDbQuery;
import org.freevoice.jumpdbqueryextension.AbstractJumpDbQuery;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.io.WKTReader;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Types;
import java.sql.ResultSetMetaData;
import java.io.StringReader;


/**
 *  JumpDbQuery instance for querying spatial data from
 * MySQL databases.
 */
public class JumpMysqlDbQuery extends AbstractJumpDbQuery implements JumpDbQuery
{
    private ResultSet resultSet = null;
    private boolean hasOneRow = false;

   @Override
    protected AttributeType getAttributeTypeForColumn(ResultSetMetaData metadata, int columnIndex) throws SQLException
    {
        AttributeType returnType = null;

        String columnName = metadata.getColumnName(columnIndex + 1);

        if (columnName.toLowerCase().startsWith("astext"))
        {
            returnType = AttributeType.GEOMETRY;
        }
        else
        {

            int columnType = metadata.getColumnType(columnIndex + 1);

            switch (columnType)
            {
                case Types.INTEGER:
                    returnType = AttributeType.INTEGER;
                    break;
                case Types.SMALLINT:
                    returnType = AttributeType.INTEGER;
                    break;
                case Types.TINYINT:
                    returnType = AttributeType.INTEGER;
                    break;
                case Types.BOOLEAN:
                    returnType = AttributeType.INTEGER;
                    break;
                case Types.FLOAT:
                    returnType = AttributeType.DOUBLE;
                    break;
                case Types.DOUBLE:
                    returnType = AttributeType.DOUBLE;
                    break;
                case Types.DECIMAL:
                    returnType = AttributeType.DOUBLE;
                    break;
                case Types.NUMERIC:
                    returnType = AttributeType.DOUBLE;
                    break;
                case Types.DATE:
                    returnType = AttributeType.DATE;
                    break;
                case Types.TIMESTAMP:
                    returnType = AttributeType.DATE;
                    break;
                case Types.TIME:
                    returnType = AttributeType.DATE;
                    break;
                case Types.VARCHAR:
                    returnType = getReturnTypeForVarchar(metadata, columnIndex);
                    break;
                default:
                    returnType = AttributeType.STRING;
                    break;
            }
        }

        return returnType;
    }

    private AttributeType getReturnTypeForVarchar(ResultSetMetaData metadata, int columnIndex) throws SQLException
    {
        AttributeType returnType = AttributeType.STRING;

        final String UNKNOWN_COLUMN_TYPE_NAME = "UNKNOWN";

        String columnName = metadata.getColumnName(columnIndex + 1);
        String columnTypeName = metadata.getColumnTypeName(columnIndex + 1);

        //support old astext usage that converted geometry to WKT
        if (columnName.toLowerCase().startsWith("astext"))
        {
           returnType = AttributeType.GEOMETRY;
        }
        //raw geometry types are type VARCHAR, with a typename UNKNOWN
        else if(UNKNOWN_COLUMN_TYPE_NAME.equals(columnTypeName))
        {
           if(hasOneRow)
           {
               String columnValue = resultSet.getString(columnIndex+1);

               try
               {
                   if(columnValue != null &&  appearsToBeNativeFormat(columnValue.getBytes()))
                   {
                       returnType = AttributeType.GEOMETRY;
                   }
               }
               catch (Exception e)
               {
                   throw new SQLException(e.getMessage());
               }
           }
        }

        return returnType;
    }

   @Override
    public FeatureCollection getCollection(String query, int maxFeatures) throws Exception
    {

        Connection connection = getConnection();

        FeatureCollection featureCollection = null;

        try
        {
            //FIXME - use LIMIT to set maxFeatures;
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            hasOneRow = resultSet.next();

            FeatureSchema featureSchema = createSchemaFromMetadata(resultSet.getMetaData());

            featureCollection = new FeatureDataset(featureSchema);

            if(hasOneRow)
            {
               //add the row we just moved the curser to
               addFeatureToCollection(resultSet, featureCollection);

               //add the other rows
               while (resultSet.next())
               {
                 addFeatureToCollection(resultSet, featureCollection);
               }
            }

        }
        finally
        {
            connection.close();
        }

        return featureCollection;
    }


    protected Object getAttributeValueFromResults(ResultSet results, FeatureSchema featureSchema, int columnIndex) throws SQLException
    {
        Object returnObject = null;

        AttributeType attributeType = featureSchema.getAttributeType(columnIndex);
        int dbColumnIndex = columnIndex + 1;

        if (AttributeType.STRING.equals(attributeType))
        {
            returnObject = results.getString(dbColumnIndex) == null ? "" : results.getString(dbColumnIndex);
        }
        else if (AttributeType.DOUBLE.equals(attributeType))
        {
            returnObject = new Double(results.getDouble(dbColumnIndex));
        }
        else if (AttributeType.DATE.equals(attributeType))
        {
            returnObject = new Date(results.getTimestamp(dbColumnIndex).getTime());
        }
        else if (AttributeType.INTEGER.equals(attributeType))
        {
            returnObject = new Integer(results.getInt(dbColumnIndex));
        }
        else if (AttributeType.GEOMETRY.equals(attributeType))
        {
            String geometryAsString = results.getString(dbColumnIndex);

            try
            {
                returnObject = getGeometryFromString(geometryAsString);
            }
            catch (Exception e)
            {
                throw new SQLException(e.getMessage());
            }
            finally
            {
            }
        }
        else
        {
            throw new SQLException("Uknown attribute type for column index " + dbColumnIndex + ": " + attributeType );
        }


        return returnObject;
    }


    private Geometry getGeometryFromString(String geometry) throws Exception
    {
       byte[] geometryAsBytes = geometry.getBytes();

        boolean nativeFormat = appearsToBeNativeFormat(geometryAsBytes);
        Geometry returnGeometry;

        if(nativeFormat)
        {
            WKBReader wkbReader = new WKBReader();

            //copy the byte array, removing the first four
            //zero bytes added by mysql
            byte[] wkb = new byte[geometryAsBytes.length - 4];
            System.arraycopy(geometryAsBytes, 4, wkb, 0, wkb.length);
            returnGeometry = wkbReader.read(wkb);
        }
        else
        {
            //assume the user did an astext, and it's just wkt
            WKTReader wktReader = new WKTReader();
            StringReader stringReader = new StringReader(geometry);

            try
            {
            FeatureCollection c = wktReader.read(stringReader);
            Feature feature = (Feature) c.getFeatures().get(0);
            returnGeometry = feature.getGeometry();
            }
            catch(Exception e)
            {
                throw e;
            }
            finally
            {
               stringReader.close();
            }
        }

        return returnGeometry;
    }

    private boolean appearsToBeNativeFormat(byte[] geometryAsBytes)
            throws Exception
    {
        if(geometryAsBytes.length < 5)
        {
            throw new Exception("Geometry less than five bytes");
        }

        //use a heuristic here.  MySQL seems to store
        //geometries as WKB with four leading zero bytes
        //so, the first four should be zero, with the fifth
        //byte being the byte-order byte, which always seems
        //to be 0x01 in MySQL
        int firstFive = geometryAsBytes[0] |
                         geometryAsBytes[1] |
                         geometryAsBytes[2] |
                         geometryAsBytes[3] |
                         geometryAsBytes[4];

        boolean nativeFormat = false;

        if((firstFive & 0xFF) == 0x01)
        {
            //the next section in WKB is the geometry type.
            //MySql supports types 1-7
            if(geometryAsBytes[5] >= 1 && geometryAsBytes[5] <= 7 )
            {
               nativeFormat = true;
            }

        }

        return nativeFormat;
    }

}
