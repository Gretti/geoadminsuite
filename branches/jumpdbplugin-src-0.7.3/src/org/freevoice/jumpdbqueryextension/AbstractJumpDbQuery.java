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

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jts.geom.*;

import java.util.Properties;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.freevoice.jumpdbqueryextension.util.Logger;

/**
 *
 */
public abstract class AbstractJumpDbQuery implements JumpDbQuery
{
    private static final String GEOMETRY_PLACEHOLDER = "JDBQ_GEOMETRY_PLACEHOLDER";

    private Connection _connection = null;
    private GeometryFactory geometryFactory = new GeometryFactory();
    /** a reference to the statement created when getting features collecion */
    protected Statement statement = null;

    private boolean hasNullGeometries = false;

    public void setupDb(String driverClass, String dbUrl, String username, String password) throws Exception
    {
        Properties connectionProperties = new Properties();
        connectionProperties.put("jumpdb.databaseDriverClass", driverClass);
        connectionProperties.put("jumpdb.databaseUrl", dbUrl);
        connectionProperties.put("jumpdb.databaseUsername", username);
        connectionProperties.put("jumpdb.databasePassword", password);

        _connection = getConnection(connectionProperties);
    }

    protected Connection getConnection() throws IllegalStateException
    {
        if (_connection == null)
        {
            throw new IllegalStateException("Connection is null.  Call setupDb first.");
        }

        return _connection;
    }

    public abstract FeatureCollection getCollection(String query, int maxFeatures) throws Exception;

   /**
    * Return true if feature collection returned by getCollection has null geometries.
    *
    * @return True if feature collection returned by getCollection has null geometries.
    */
   public boolean collectionHasNulls() throws IllegalStateException
   {
      return hasNullGeometries;
   }

    protected FeatureSchema createSchemaFromMetadata(ResultSetMetaData metaData) throws SQLException
    {
        FeatureSchema featureSchema = new FeatureSchema();

        boolean gotGeometry = false;
        for (int i = 0; i < metaData.getColumnCount(); i++)
        {

            AttributeType attributeType = getAttributeTypeForColumn(metaData, i);
            featureSchema.addAttribute(metaData.getColumnLabel(i + 1), attributeType);

            if (AttributeType.GEOMETRY.equals(attributeType))
            {
                gotGeometry = true;
            }
        }

        //if there's no geometry type, add a placeholder so Jump will work with queries that don't have geometries
        if (!gotGeometry)
        {
            featureSchema.addAttribute(GEOMETRY_PLACEHOLDER, AttributeType.GEOMETRY);
        }

        return featureSchema;
    }

    /**
     * @param metaData
     * @param columnIndex 0's based.  0 is first column, 1 is second, etc.
     * @return
     * @throws SQLException
     */
    protected abstract AttributeType getAttributeTypeForColumn(ResultSetMetaData metaData, int columnIndex) throws SQLException;

    protected void addFeatureToCollection(ResultSet results, FeatureCollection featureCollection) throws SQLException
    {
        FeatureSchema featureSchema = featureCollection.getFeatureSchema();

        int attributeCount = featureSchema.getAttributeCount();

        Feature feature = new BasicFeature(featureSchema);


        for (int i = 0; i < attributeCount; i++)
        {
            String attributeName = featureSchema.getAttributeName(i);

            //don't try to get the placeholder geometry from the ResultSet because it won't be there
            if (!GEOMETRY_PLACEHOLDER.equals(attributeName))
            {
                Object attributeValue = getAttributeValueFromResults(results, featureSchema, i);
                feature.setAttribute(attributeName, attributeValue);
            }
        }

       //if the geometry was null (or not selected), set an empty
       //GeometryCollection as a placeholder, as recommended in Jump
       //Developers list:
       //http://sourceforge.net/mailarchive/forum.php?thread_name=4B6473F8.50400%40free.fr&forum_name=jump-pilot-devel
        if (feature.getGeometry() == null)
        {
           feature.setGeometry(new GeometryCollection(new Geometry[0], geometryFactory));
           hasNullGeometries = true;
        }

        featureCollection.add(feature);
    }

    protected abstract Object getAttributeValueFromResults(ResultSet results, FeatureSchema featureSchema, int columnIndex) throws SQLException;

    /**
     * Get a JDBC connection, given the connection properties.   Subclasses that call this method MUST remember
     * to close the connection on their own.
     * @param connectionProperties
     * @return
     * @throws Exception
     */
    private Connection getConnection(Properties connectionProperties) throws Exception
    {

        if (_connection == null)
        {
            String driverClass = connectionProperties.getProperty("jumpdb.databaseDriverClass");
            String databaseUrl = connectionProperties.getProperty("jumpdb.databaseUrl");
            String databaseUsername = connectionProperties.getProperty("jumpdb.databaseUsername");
            String databasePassword = connectionProperties.getProperty("jumpdb.databasePassword");

            Class.forName(driverClass).newInstance();

            _connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
        }

        return _connection;
    }

    /**
     * Cancels the SQL statement
     */
    public String cancelQuery() {
        Logger.logDebug("entering cancelQuery...");
        String ret = "";
        if (this.statement != null) {
            try {
                Logger.logDebug("canceling query...");
                this.statement.cancel();
            } catch (Exception e) {
                ret = e.getMessage();
            }
        }
        return ret;
    }
}
