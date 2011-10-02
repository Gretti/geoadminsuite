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

import com.vividsolutions.jump.feature.*;

import java.sql.*;

import org.geotools.data.oracle.sdo.GeometryConverter;
import org.freevoice.jumpdbqueryextension.JumpDbQuery;
import org.freevoice.jumpdbqueryextension.AbstractJumpDbQuery;
import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;


/**
 *
 */
public class JumpOracleDbQuery extends AbstractJumpDbQuery implements JumpDbQuery
{



    public FeatureCollection getCollection(String query, int maxFeatures) throws Exception
    {

        Connection connection = getConnection();

        FeatureCollection featureCollection = null;

        //semicolons work in sql*plus, but they give Oracle fits when submitted through JDBC
        // pull semicolon off the query if it exists
        query = query.trim();
        if(query.endsWith(";"))
        {
            query = query.substring(0, query.length() - 1);
        }

        try
        {
            //FIXME - use NUMROWS to set maxFeatures;
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query);

            FeatureSchema featureSchema = createSchemaFromMetadata(results.getMetaData());

            featureCollection = new FeatureDataset(featureSchema);

            //FIXME write resultset count to output window
            while (results.next())
            {
                addFeatureToCollection(results, featureCollection);
            }

        }
        finally
        {
            if(connection != null)
            {
               connection.close();
            }
        }

        return featureCollection;
    }

    protected AttributeType getAttributeTypeForColumn(ResultSetMetaData metaData, int columnIndex) throws SQLException
    {
        AttributeType returnType = null;

        int columnType = metaData.getColumnType(columnIndex+1);

        switch(columnType)
        {
           //FIXME MDSYS_SDO_GEOMETRY is a type of STRUCT.
           //What if other non-geometry structs are selected in the query??
           //I should probably use some heuristic to make sure it is
           //a geometry
            case Types.STRUCT:
                returnType = AttributeType.GEOMETRY;
                break;
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
            default:
                returnType = AttributeType.STRING;
                break;
        }

        return returnType;
    }


    protected Object getAttributeValueFromResults(ResultSet results, FeatureSchema featureSchema, int columnIndex) throws SQLException
    {
        Object returnObject = null;

        int dbColumnIndex = columnIndex + 1;

        AttributeType attributeType = featureSchema.getAttributeType(columnIndex);

        if(AttributeType.STRING.equals(attributeType))
        {
            returnObject = results.getString(dbColumnIndex) == null? "" : results.getString(dbColumnIndex);
        }
        else if(AttributeType.DOUBLE.equals(attributeType))
        {
            returnObject = new Double(results.getDouble(dbColumnIndex));
        }
        else if(AttributeType.DATE.equals(attributeType))
        {
            returnObject = getDate(results, dbColumnIndex);
        }
        else if(AttributeType.INTEGER.equals(attributeType))
        {
            returnObject = new Integer(results.getInt(dbColumnIndex));
        }
        else if(AttributeType.GEOMETRY.equals(attributeType))
        {

            Object geometryObject = results.getObject(dbColumnIndex);
            GeometryConverter geometryConverter = new GeometryConverter((OracleConnection) results.getStatement().getConnection());
            returnObject = geometryConverter.asGeometry((STRUCT) geometryObject);
        }
        else
        {
            throw new SQLException("Uknown attribute type for column index " + dbColumnIndex + ": " + attributeType );
        }


        return returnObject;
    }

    private Date getDate(ResultSet results, int index) throws SQLException
    {
        Date returnDate = new Date(0);
        Timestamp timestamp = results.getTimestamp(index);

        if(timestamp != null)
        {
           returnDate = new Date(timestamp.getTime());
        }

        return returnDate;
     }

}
