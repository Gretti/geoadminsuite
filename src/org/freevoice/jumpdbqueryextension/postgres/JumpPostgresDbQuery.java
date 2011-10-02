package org.freevoice.jumpdbqueryextension.postgres;

import org.freevoice.jumpdbqueryextension.AbstractJumpDbQuery;
import org.freevoice.jumpdbqueryextension.JumpDbQuery;
import org.postgis.PGgeometry;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.io.WKTReader;

import java.sql.*;
import java.io.StringReader;


/**
 *  Handle querying of PostgreSQL Database to get a feature collection
 */
public class JumpPostgresDbQuery extends AbstractJumpDbQuery implements JumpDbQuery
{

    //This is the postgresql "Other" type, that
    //is used for objects not in the standard JDBC types.
    //There's a chance this would not be a geometry type
    //and some inspection of the value in the geometry
    //field is probably a safer way to do this.
    public static final int POSTGRES_TYPES_GEOMETRY = 1111;


    public FeatureCollection getCollection(String query, int maxFeatures) throws Exception
    {

        Connection connection = getConnection();

        FeatureCollection featureCollection = null;

        // pull semicolon off the query if it exists
        query = query.trim();
        if(query.endsWith(";"))
        {
            query = query.substring(0, query.length() - 1);
        }

        try
        {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query);

            FeatureSchema featureSchema = createSchemaFromMetadata(results.getMetaData());

            featureCollection = new FeatureDataset(featureSchema);

            //@todo write resultset count to output window
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
            case POSTGRES_TYPES_GEOMETRY:
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

            PGgeometry geometry = new PGgeometry(results.getString(dbColumnIndex));
            String geometryAsString = geometry.toString();

            //pull out leading SRID if it exists
            if(geometryAsString.startsWith("SRID="))
            {
                int firstSemicolon = geometryAsString.indexOf(';');

                if(firstSemicolon > 0)
                {

                  geometryAsString = geometryAsString.substring(firstSemicolon+1);
                }
            }

            //FIXME - maybe drop this functionality into superclass?
            WKTReader wktReader = new WKTReader();
            StringReader stringReader = new StringReader(geometryAsString);

            try
            {
            FeatureCollection c = wktReader.read(stringReader);
            Feature feature = (Feature) c.getFeatures().get(0);
            returnObject = feature.getGeometry();
            }
            catch(Exception e)
            {
                throw new SQLException(e.getMessage());
            }
            finally
            {
               stringReader.close();
            }

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
