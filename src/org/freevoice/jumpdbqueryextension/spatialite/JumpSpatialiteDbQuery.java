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
package org.freevoice.jumpdbqueryextension.spatialite;

import org.freevoice.jumpdbqueryextension.JumpDbQuery;
import org.freevoice.jumpdbqueryextension.AbstractJumpDbQuery;
import org.freevoice.jumpdbqueryextension.util.Logger;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Spatialite verion of Jump DB query
 */
public class JumpSpatialiteDbQuery extends AbstractJumpDbQuery implements JumpDbQuery
{
   private ResultSet resultSet = null;
   private boolean hasOneRow = false;


   private static final String FDO_TABLE_NAME = "f_table_name";
   private static final String FDO_GEOMETRY_COLUMN = "f_geometry_column";
   private static final String FDO_GEOMETRY_FORMAT = "geometry_format";
   private static final String FDO_TABLE_COLUMN_QUERY = "SELECT " + FDO_TABLE_NAME + "," +
         FDO_GEOMETRY_COLUMN +  "," + FDO_GEOMETRY_FORMAT  + " FROM geometry_columns";

   //encodings
   private static final String WKB = "WKB";
   private static final String WKT = "WKT";
   private static final String WKB12 = "WKB12";
   private static final String WKT12 = "WKT12";
   private static final String FSF = "FSF";

   private Map<String, GeometryColumnFormat> fdoFormatMap = null;

   /**
    * Container class for storing the geometry column and its format
    * in a collection class.
    */
   private final class GeometryColumnFormat
   {
      private final String columnName;
      private final String format;

      public GeometryColumnFormat(String columnName, String format)
      {
         this.columnName = columnName;
         this.format  = format;
      }

      public String getColumnName()
      {
         return columnName;
      }

      public String getFormat()
      {
         return format;
      }

      @Override
      public String toString()
      {
         return "GeometryColumnFormat{" +
               "columnName='" + columnName + '\'' +
               ", format='" + format + '\'' +
               '}';
      }
   }


   protected AttributeType getAttributeTypeForColumn(ResultSetMetaData metadata, int columnIndex) throws SQLException
   {
      AttributeType returnType = null;

      int columnType = metadata.getColumnType(columnIndex + 1);
      String tableName = metadata.getTableName(columnIndex + 1);

      GeometryColumnFormat columnFormat = fdoFormatMap.get(tableName);

      String columnName = metadata.getColumnName(columnIndex + 1);

      //if I found something in the fdoFormatMap, that means this database
      //has some FDO RFC 16 metadata (or it's coincidence). And if the current
      //column in the resultset has the same name as the geometry column
      //given in the FDO format, then this is a geometry column, regardless
      //of its type
      if(columnFormat != null && columnName != null &&
            columnName.equalsIgnoreCase(columnFormat.getColumnName() ))
      {
            returnType = AttributeType.GEOMETRY;
      }
      else //this is a standard spatialite table, with geometry stored in blobs
      {
         returnType = getStandardSpatialiteReturnType(columnIndex, columnType);
      }

      return returnType;
   }

   private AttributeType getStandardSpatialiteReturnType(int columnIndex, int columnType)
         throws SQLException
   {
      AttributeType returnType;

      //consider using a Map instead of this huge switch
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
         case Types.BLOB:
            returnType = getReturnTypeForBlob(columnIndex);
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
            returnType = AttributeType.STRING;
            break;
         default:
            returnType = AttributeType.STRING;
            break;
      }
      return returnType;
   }

   private AttributeType getReturnTypeForBlob(int columnIndex) throws SQLException
   {
      AttributeType returnType = AttributeType.STRING;

      if (hasOneRow)
      {
         byte[] columnValue = resultSet.getBytes(columnIndex + 1);

         try
         {
            if (columnValue != null && appearsToBeNativeGeometry(columnValue))
            {
               returnType = AttributeType.GEOMETRY;
            }
         }
         catch (Exception e)
         {
            throw new SQLException(e.getMessage());
         }
      }

      return returnType;
   }

   /**
    * Check to see if there are tables  matching the spec  defined in FDO RFC
    * 16. If so, their geometry encoding format will not match
    * the spatialite "extended WKB" format and will.
    *
    * @param connection Database connection to use when searching for FDO
    * metadata.
    */
   private void checkFdoFormatting(Connection connection)
   {

      fdoFormatMap = new HashMap<String, GeometryColumnFormat>();

      Statement statement = null;

      try
      {
         statement = connection.createStatement();
         resultSet = statement.executeQuery(FDO_TABLE_COLUMN_QUERY);

         while (resultSet.next())
         {

            String tablename = resultSet.getString(FDO_TABLE_NAME);

            if (!resultSet.wasNull())
            {
               String columnName = resultSet.getString(FDO_GEOMETRY_COLUMN);
               if (!resultSet.wasNull())
               {
                  String columnFormat = resultSet.getString(FDO_GEOMETRY_FORMAT);

                  //According to the FDO RFC, the default format should be WKB
                  if (resultSet.wasNull())
                  {
                     columnFormat = "WKB";
                  }

                  fdoFormatMap.put(tablename, new GeometryColumnFormat(columnName, columnFormat));

               }

            }

         }


      }
      catch (SQLException e)
      {
         Logger.logInfo("Warning: Unable to find FDO-OGR metadata:  " + e.getMessage() +
               ".  I'll take this as an indication that this database doesn't conform to FDO RFC 16, but is " +
               "just a regular spatialite (or sqlite) database");
      }
      finally
      {
         if (statement != null)
         {
            try
            {
               statement.close();
            }
            catch (SQLException e)
            {
               //swallow this one
            }
         }
      }


   }

   @Override
   public FeatureCollection getCollection(String query, int maxFeatures) throws Exception
   {

      Connection connection = getConnection();

      FeatureCollection featureCollection = null;

      try
      {

         //get FDO info if it hasn't already been done
         if(fdoFormatMap == null)
         {
             checkFdoFormatting(connection);
         }

         //FIXME - use LIMIT to set maxFeatures;
         Statement statement = connection.createStatement();
         resultSet = statement.executeQuery(query);

         hasOneRow = resultSet.next();

         FeatureSchema featureSchema = createSchemaFromMetadata(resultSet.getMetaData());

         featureCollection = new FeatureDataset(featureSchema);

         if (hasOneRow)
         {
            //add the row we just moved the cursor to
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
         returnObject = results.getDouble(dbColumnIndex);
      }
      else  if (AttributeType.DATE.equals(attributeType))
      {
         returnObject = new Date(results.getTimestamp(dbColumnIndex).getTime());
      }
      else  if (AttributeType.INTEGER.equals(attributeType))
      {
         returnObject = new Integer(results.getInt(dbColumnIndex));
      }
      else if (AttributeType.GEOMETRY.equals(attributeType))
      {
         try
         {
             returnObject = getGeometry(results, dbColumnIndex);
         }
         //FIXME funky exception handling here.....
         catch (Exception e)
         {
             throw new SQLException(e.getMessage());
         }
      }
      else
      {
          throw new SQLException("Uknown attribute type for column index " + dbColumnIndex + ": " + attributeType);
      }

      return returnObject;
   }

   private Geometry getGeometry(ResultSet results, int dbColumnIndex) throws Exception
   {
      ResultSetMetaData metadata = results.getMetaData();
      String tableName = metadata.getTableName(dbColumnIndex);

      GeometryColumnFormat columnFormat = fdoFormatMap.get(tableName);

      Geometry returnGeometry = null;

      if(columnFormat == null)
      {
         //no FDO info for this table, try native spatialite blob encoding
         byte[] geometryBytes = results.getBytes(dbColumnIndex);
         returnGeometry = getNativeGeometryFromBlob(geometryBytes);
      }
      else
      {
         String format = columnFormat.getFormat();

         if(WKB.equals(format))
         {
            WKBReader wkbReader = new WKBReader();
            byte[] geometryBytes = results.getBytes(dbColumnIndex);
            returnGeometry = wkbReader.read(geometryBytes);

            if (returnGeometry == null)
            {
               throw new Exception("Unable to parse WKB");
            }
         }
         else if(WKT.equals(format))
         {
            String geometryString = results.getString(dbColumnIndex);
            WKTReader wktReader = new WKTReader();
            returnGeometry = wktReader.read(geometryString);
         }
         else
         {
            throw new Exception("Unable to handle geometry format " + format);
         }
      }

      return returnGeometry;
   }


   private Geometry getNativeGeometryFromBlob(byte[] blobAsBytes) throws Exception
   {
      Geometry returnGeometry;

      //copy the byte array, removing the MBR at the front,
      //and the ending OxFE byte at the end
      byte[] wkb = new byte[blobAsBytes.length - 39];
      System.arraycopy(blobAsBytes, 39, wkb, 1, blobAsBytes.length - 1 - 39);

      //prepend byte-order byte
      wkb[0] = blobAsBytes[1];

      WKBReader wkbReader = new WKBReader();
      returnGeometry = wkbReader.read(wkb);

      if (returnGeometry == null)
      {
         throw new Exception("Unable to parse WKB");
      }

      return returnGeometry;
   }

   private boolean appearsToBeNativeGeometry(byte[] geometryAsBytes)
         throws Exception
   {
      boolean blobIsGeometry = false;

      //From http://www.gaia-gis.it/spatialite-2.1/SpatiaLite-manual.html
      //Spatialite geometry blobs are WKB-like, with some specifics to
      //spatialite:  For our purposes, this should be good enough:
      //the 39th byte must be 0x7C (marks MBR end)
      //and the blob must end with 0xFE
      int numBytes = geometryAsBytes.length;

      if (numBytes > 39
            && geometryAsBytes[38] == (byte) 0x7C
            && geometryAsBytes[numBytes - 1] == (byte) 0xFE)
      {
         blobIsGeometry = true;
      }

      return blobIsGeometry;
   }

}
