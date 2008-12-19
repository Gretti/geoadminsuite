/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geogurus.data.database;

import static org.geotools.data.postgis.PostgisDataStoreFactory.DATABASE;
import static org.geotools.data.postgis.PostgisDataStoreFactory.DBTYPE;
import static org.geotools.data.postgis.PostgisDataStoreFactory.HOST;
import static org.geotools.data.postgis.PostgisDataStoreFactory.PASSWD;
import static org.geotools.data.postgis.PostgisDataStoreFactory.PORT;
import static org.geotools.data.postgis.PostgisDataStoreFactory.SCHEMA;
import static org.geotools.data.postgis.PostgisDataStoreFactory.USER;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessHelper;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.Extent;
import org.geogurus.data.Factory;
import org.geogurus.data.Geometry;
import org.geogurus.data.Operation;
import org.geogurus.data.Option;
import org.geogurus.data.ParameterizedCallable;
import org.geogurus.data.cache.DataStoreCacheable;
import org.geogurus.data.cache.ObjectCache;
import org.geogurus.data.operations.UniqueValueFeatureClassification;
import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.MsLayer;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.tools.sql.ConPool2;
import org.geogurus.tools.string.ConversionUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.postgis.PostgisDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * An Object for access the Features in a Postgis Table
 * 
 * @author jesse
 */
public class PostgisDataAccess extends DataAccess {

    private static final long serialVersionUID = 1L;

    private final class LoadPostgisSampleData implements
            ParameterizedCallable<Vector<Vector<Object>>, Connection> {
        private int from;
        private int to;

        /**
         * Creates a new instance of type
         * PostgisDataAccess.LoadPostgisSampleData
         * 
         */
        public LoadPostgisSampleData(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public Vector<Vector<Object>> run(Connection con) {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.createStatement();

                // gets the first n records
                StringBuffer query = new StringBuffer("select ");
                // uses columnInfo definition to get values
                for (int i = 0; i < metadata.size(); i++) {
                    if (i != 0) {
                        query.append(", ");
                    }
                    query.append(ConversionUtilities.quotes(((GeometryClassFieldBean) metadata.get(i)).getName()));
                }
                // finishes query
                query.append(" from ").append(ConversionUtilities.quotes(schemaName)).append(".").append(
                        ConversionUtilities.quotes(tableName)).append(" limit ").append((to - from))
                        .append(" offset ").append(from);
                logger.info("QUERY DATA :" + query.toString());

                // executes query
                rs = stmt.executeQuery(query.toString());
                // parses result and fills columnValues
                Vector<Vector<Object>> columnValues = new Vector<Vector<Object>>();
                while (rs.next()) {
                    Vector<Object> currentRecord = new Vector<Object>();
                    for (int n = 1; n <= metadata.size(); n++) {
                        currentRecord.add(rs.getString(n));
                    }
                    columnValues.add(currentRecord);
                }
                rs.close();
                stmt.close();
                return columnValues;

            } catch (SQLException sqle) {
                errorMessage = "getTableMetadata: SQLException: "
                        + sqle.getMessage();
                sqle.printStackTrace();
                return null;
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                }
            }
        }

    }

    private final class LoadPostgisMetadata implements
            ParameterizedCallable<Boolean, Connection> {
        public Boolean run(Connection con) {

            Statement stmt = null;
            ResultSet rs = null;
            boolean ret = false;

            if (geometryAttributeName != null) {
                // job already done
                ret = true;
            } else {
                try {
                    stmt = con.createStatement();

                    // gets the specific geometric column type
                    StringBuffer query = new StringBuffer(
                            "select f_geometry_column,type from geometry_columns");
                    query.append(" where lower(f_table_name)='").append(
                            getTableName().toLowerCase()).append(
                            "' and lower(f_table_schema)='").append(
                            getSchemaName().toLowerCase()).append("'");
                    String dbtype = "";
                    rs = stmt.executeQuery(query.toString());

                    if (rs.next()) {
                        // for the moment, only one geo column in a table
                        geometryAttributeName = rs.getString(1);
                        dbtype = rs.getString(2);
                    }

                    if (geometryAttributeName == null) {
                        errorMessage = "getColumnName: cannot get geometry_column for table: "
                                + schemaName + "." + tableName;
                        errorMessage += ".\n<br>Is the table is registered in metadata (call to addGeometryColumn)";
                    } else {
                        // gets metadata for the table
                        DatabaseMetaData dbm = con.getMetaData();

                        dbVersion = dbm.getDatabaseMajorVersion();

                        setUniqueField(con);

                        if (metadata == null) {
                            metadata = new Vector<GeometryClassFieldBean>();
                        }

                        rs = dbm.getColumns(null, getSchemaName(), getTableName(), "%");

                        while (rs.next()) {
                            // skip geo column
                            if (rs.getString(4).equals(geometryAttributeName)) {
                                continue;
                            }
                            // nullable field:
                            String nullable = rs.getInt(11) == DatabaseMetaData.columnNullable ? "Nullable"
                                    : "Not Null";
                            GeometryClassFieldBean as = new GeometryClassFieldBean(
                                    rs.getString(4), rs.getString(6), rs
                                            .getInt(7), nullable);
                            metadata.add(as);
                        }
                        // set the type
                        if (dbtype.equals("POINT")) {
                            geomTypeCode = Geometry.POINT;
                        } else if (dbtype.equals("LINESTRING")) {
                            geomTypeCode = Geometry.LINESTRING;
                        } else if (dbtype.equals("POLYGON")) {
                            geomTypeCode = Geometry.POLYGON;
                        } else if (dbtype.equals("MULTIPOLYGON")) {
                            geomTypeCode = Geometry.MULTIPOLYGON;
                        } else if (dbtype.equals("MULTILINESTRING")) {
                            geomTypeCode = Geometry.MULTILINESTRING;
                        } else if (dbtype.equals("MULTIPOINT")) {
                            geomTypeCode = Geometry.MULTIPOINT;
                        } else if (dbtype.equals("GEOMETRY")) {
                            geomTypeCode = Geometry.GEOMETRY;
                        } else {
                            // other OGIS types not yet supported
                            geomTypeCode = Geometry.NULL;
                        }
                        // gets the number of geometries in this table
                        query = new StringBuffer("select count(*) from ");
                        query.append(schemaName).append(".").append(getTableName());
                        query.append(" where ").append(geometryAttributeName)
                                .append(" is not null");
                        rs = stmt.executeQuery(query.toString());
                        rs.next();
                        numGeometries = rs.getInt(1);

                        // this query gives the SRTEXT and SRID of the geo
                        // table:
                        query = new StringBuffer(
                                "select SRTEXT, spatial_ref_sys.SRID from spatial_ref_sys, geometry_columns ");
                        query
                                .append("where f_table_name='")
                                .append(getTableName())
                                .append("' and f_table_schema='")
                                .append(schemaName)
                                .append(
                                        "' and geometry_columns.srid=spatial_ref_sys.srid");
                        rs = stmt.executeQuery(query.toString());
                        if (rs.next()) {
                            SRText = rs.getString(1);
                            SRID = rs.getInt(2);
                        }

                        // this query gives the geodata extent, based on the
                        // OpenGIS "envelope" method
                        if (numGeometries > 0) {
                            query = new StringBuffer("select extent(");
                            query.append(geometryAttributeName);
                            query.append(") from ");
                            query.append(schemaName);
                            query.append(".");
                            query.append(getTableName());
                            logger.fine(query.toString());

                            rs = stmt.executeQuery(query.toString());
                            if (rs.next()) {
                                extent = Extent.getExtentFromBOX3D(rs
                                        .getString(1));
                            }
                        }
                    }

                    ret = true;

                } catch (SQLException sqle) {
                    errorMessage = "getTableMetadata: SQLException: "
                            + sqle.getMessage();
                    sqle.printStackTrace();
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        if (stmt != null) {
                            stmt.close();
                        }
                    } catch (SQLException e) {
                    }
                }
            }
            return ret;
        }
    }

    /** Unique field name */
    protected String uniqueField;

    /** The name of the Schema representing this class */
    protected String schemaName;
    /** The name of the DB table, or file representing this class */
    protected String tableName;
    /** The DB port */
    protected String dbPort;
    /** the username which can connect to this database */
    protected String userName;
    protected String userPwd;
    protected String dbName;

    /** the DB major version for this datasource */
    protected int dbVersion;

    /**
     * Creates a new instance of type PostgisDataAccess
     * 
     * @param postgisConnectionParams
     */
    public PostgisDataAccess(String host, String instance, String schema,
            String table, String port, String username, String password,
            Datasource owner) {
        super(table, owner, DataAccessType.POSTGIS);

        this.tableName = table;
        this.geometryAttributeName = null;
        this.host = host;
        this.dbName = this.datasourceName = instance;
        this.dbPort = port;
        this.userName = username;
        this.userPwd = password;
        this.schemaName = schema;

    }

    @Override
    public boolean loadMetadata() {
        ParameterizedCallable<Boolean, Connection> operation = new LoadPostgisMetadata();
        return DatabaseDataAccessHelper.runOperation(getConnectionURI(),
                operation, logger);
    }

    /**
     * retrieves the GC table's unique index by querying the database metadata
     * only valid for PGCLASS or ORACLASS geometry class
     * 
     * @param con
     *            the database connection to use, or null to create a new
     *            connection for this layer
     * 
     */
    void setUniqueField(Connection con) {
        boolean localConnection = false;
        if (con == null) {
            try {
                con = ConPool2.getInstance().getConnection(getConnectionURI());
                if (con == null) {
                    logger.warning("Cannot get a connection (null) for: "
                            + datasourceName + " " + dbPort + " " + userName
                            + " " + userPwd);
                    return;
                }
                localConnection = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            DatabaseMetaData dbm = con.getMetaData();
            dbVersion = dbm.getDatabaseMajorVersion();
            ResultSet rs = dbm.getIndexInfo(null, schemaName, getTableName(), true,
                    false);
            if (rs.next()) {
                uniqueField = rs.getString("COLUMN_NAME");
            }
            if (localConnection) {
                try {
                    con.close();
                } catch (Exception e) {
                    logger.warning(e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    public Vector<Vector<Object>> getSampleData(int from, int to) {
        LoadPostgisSampleData operation = new LoadPostgisSampleData(from, to);
        return DatabaseDataAccessHelper.runOperation(getConnectionURI(),
                operation, logger);
    }

    public String getConnectionURI() {
        String connectionURI = "jdbc:postgresql://" + getHost() + ":"
                + this.dbPort + "/" + getOwner().getName() + "?user="
                + this.userName + "&password=" + this.userPwd;
        return connectionURI;
    }

    @Override
    protected Layer createMSLayerInner(RGB color) {
        msLayer = new Layer();
        msLayer.setName(name);

        msLayer.setConnection("dbname=" + dbName + " host=" + host + " port="
                + dbPort + " user=" + userName + " password=" + userPwd);
        msLayer.setConnectionType(MsLayer.POSTGIS);
        // original data implementation from super class
        // msLayer.setData(columnName + " from " + schemaName + "." + name
        // + " USING UNIQUE " + uniqueField
        // + (SRID <= 0 ? "" : " USING SRID=" + SRID));

        // from original LayerGeneralProperties. I brought it here because it
        // seems more complete
        String strData = this.geometryAttributeName + " from "
                + this.schemaName + "." + getTableName();
        if (uniqueField != null || getSRText() != null) {
            if (uniqueField != null) {
                strData += " USING UNIQUE " + uniqueField;
            }
            if (getSRText() != null) {
                strData += " USING SRID=" + getSrid();
            }
        }
        msLayer.setData(strData);

        // a default display class for this geo object
        org.geogurus.mapserver.objects.Class c = new org.geogurus.mapserver.objects.Class();
        // sets the name to the theme name, by default, without extension
        if (tableName.lastIndexOf(".") > -1) {
            c.setName(tableName.substring(0, tableName.lastIndexOf(".")));
        } else {
            c.setName(tableName);
        }

        DataAccessHelper.setMSLayerColorProperties(msLayer, geomTypeCode,
                color, c);
        return msLayer;
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public <T> Option<Boolean> performOperateStep(Operation<SimpleFeature, T> op,
            T context, Query query) throws IOException {
        if (op.getClass() == UniqueValueFeatureClassification.class) {
            DatabaseDataAccessHelper.performUniqueValueFeatureClassification(
                    getConnectionURI(), schemaName, tableName,
                    geometryAttributeName,
                    (UniqueValueFeatureClassification) op);
            return Option.some(true);
        }

        Option<FeatureSource<SimpleFeatureType,SimpleFeature>> store = getFeatureSource();
        if (store.isNone()) {
            return Option.some(false);
        }
        FeatureCollection<SimpleFeatureType,SimpleFeature> collection = store.get().getFeatures(query);
        FeatureIterator<SimpleFeature> iterator = collection.features();

        try {
            while (iterator.hasNext()) {
            	SimpleFeature operatee = iterator.next();
                if (!op.operate(operatee, context)) {
                    return Option.some(false);
                }
            }
        } finally {
            iterator.close();
        }

        return Option.some(true);
    }

    @Override
    public <T> Option<Boolean> peformOperateStep(
            Operation<RenderedImage, T> op, T context) {
        return Option.none();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Option<T> resource(Class<T> request) {
        if (FeatureStore.class.isAssignableFrom(request)) {
            return (Option<T>) getFeatureSource();
        }
        if (Connection.class.isAssignableFrom(request)) {
            ConPool2 conPool = ConPool2.getInstance();
            Connection con = conPool.getConnection(conPool.getConnectionURI(
                    getHost(), this.dbPort, dbName, this.userName,
                    this.userPwd, ConPool2.DBTYPE_POSTGRES));
            return Option.some(request.cast(con));
        }
        return Option.none();
    }

    private Option<PostgisDataStore> getDatastore() {
        Factory<PostgisDataStore, Map<String, Object>> factory = new GasPostgisDatastoreFactory(
                logger);
        Map<String, Object> factoryParams = new HashMap<String, Object>();
        factoryParams.put(DBTYPE.key, DBTYPE.sample);
        factoryParams.put(HOST.key, this.host);
        factoryParams.put(DATABASE.key, this.datasourceName);
        factoryParams.put(PORT.key, this.dbPort);
        factoryParams.put(USER.key, userName);
        factoryParams.put(PASSWD.key, this.userPwd);
        factoryParams.put(SCHEMA.key, this.schemaName);

        PostgisDataStore dataStore = ObjectCache.getInstance().getCachedObject(new DataStoreCacheable(getConnectionURI()), factory, factoryParams);
        if (dataStore == null) {
            return Option.none();
        }
        return Option.some(dataStore);
    }

    private Option<FeatureSource<SimpleFeatureType,SimpleFeature>> getFeatureSource() {
        try {
            Option<PostgisDataStore> ds = getDatastore();
            if (ds.isSome()) {
                return Option.some(ds.get().getFeatureSource(tableName));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE,
                    "Error creating obtaining FeatureSource from: "
                            + getConnectionURI(), e);
        }
        return Option.none();
    }

    @Override
    public Option<SimpleFeatureType> featureType() {
        Option<FeatureSource<SimpleFeatureType, SimpleFeature>> option = getFeatureSource();
        if (option.isNone()) {
            return Option.none();
        }
        return Option.some(option.get().getSchema());
    }

    /**
     * Return unique field name (can be null if datasource is not a DBMS)
     */
    protected String getUniqueField() {
        return "null".equalsIgnoreCase(uniqueField) ? null : uniqueField;
    }

    @Override
    public ConnectionParams getConnectionParams() {
        ConnectionParams params = new ConnectionParams(owner);
        params.dbname = this.dbName;
        params.host = this.host;
        params.password = this.userPwd;
        params.port = this.dbPort;
        params.schema = this.schemaName;
        params.table = this.tableName;
        params.username = this.userName;
        params.type = datasourceType.name();
        return params;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }
    
    
}
