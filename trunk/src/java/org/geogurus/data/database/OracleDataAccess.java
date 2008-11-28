/**
 * 
 */
package org.geogurus.data.database;

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
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.oracle.OracleDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Access and load data from a Oracle Table
 * 
 * @author jesse
 */
public class OracleDataAccess extends DataAccess {
    /**
     * Loads some sample data to be returned by
     * {@link OracleDataAccess#getSampleData(int, int)}
     * 
     * @author jesse
     */
    class LoadOracleSampleData implements
            ParameterizedCallable<Vector<Vector<Object>>, Connection> {

        private int from;
        private int to;

        /**
         * Creates a new instance of type LoadOracleSampleData
         * 
         * @param from
         * @param to
         */
        public LoadOracleSampleData(int from, int to) {
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
                    if (i == 0) {
                        query.append(((GeometryClassFieldBean) metadata.get(i))
                                .getName());
                    } else {
                        query.append(", ").append(
                                ((GeometryClassFieldBean) metadata.get(i))
                                        .getName());
                    }
                }
                // finishes query
                query.append(" from ").append(schemaName.toLowerCase()).append(
                        ".").append(tableName.toLowerCase());
                // executes query
                logger.info("oracle sample data query: " + query.toString());
                rs = stmt.executeQuery(query.toString());
                // parses result and fills columnValues
                Vector<Object> currentRecord = null;
                Vector<Vector<Object>> columnValues = new Vector<Vector<Object>>();
                int counter = 0;
                while (rs.next()) {
                    if (counter >= from && counter <= to) {
                        currentRecord = new Vector<Object>();
                        for (int n = 1; n <= metadata.size(); n++) {
                            currentRecord.add(rs.getString(n));
                        }
                        columnValues.add(currentRecord);
                    }
                    counter++;
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
                    logger.severe(e.getMessage());
                }
            }

        }

    }

    private static final long serialVersionUID = 1L;
    /**
     *Oracle geometry types
     */
    public static final int ORA_UNKNOWN_GEOMETRY = 0;
    public static final int ORA_POINT = 1;
    public static final int ORA_LINE = 2;
    public static final int ORA_POLYGON = 3;
    public static final int ORA_COLLECTION = 4;
    public static final int ORA_MULTIPOINT = 5;
    public static final int ORA_MULTILINE = 6;
    public static final int ORA_MULTIPOLYGON = 7;

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
    private String dbName;
    /** the DB major version for this datasource */
    protected int dbVersion;

    /**
     * Creates a new instance of type OracleDataAccess
     * 
     * @param oracleConnectionParams
     */
    public OracleDataAccess(String host, String instance, String schema,
            String table, String port, String username, String password,
            Datasource owner) {
        super(table, owner, DataAccessType.ORACLE);

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
        ParameterizedCallable<Boolean, Connection> operation = new LoadOracleMetada();
        return DatabaseDataAccessHelper.runOperation(getConnectionURI(),
                operation, logger);
    }

    class LoadOracleMetada implements
            ParameterizedCallable<Boolean, Connection> {
        /**
         * Gets the DB name of the columns of the table representing this
         * geometry Class. DO NOT CLOSE THE CONNECTION HERE; AS IT IS USED BY
         * THE CALLING METHOD
         */

        public Boolean run(Connection con) {
            if (geometryAttributeName != null) {
                // job already done
                return true;
            }

            ResultSet rs = null;
            Statement stmt = null;
            boolean ret = false;

            try {
                stmt = con.createStatement();

                // gets the specific geometric column type
                StringBuffer query = new StringBuffer(
                        "select COLUMN_NAME from MDSYS.USER_SDO_GEOM_METADATA");
                query.append(" where TABLE_NAME='").append(tableName).append(
                        "'");
                rs = stmt.executeQuery(query.toString());

                if (rs.next()) {
                    // for the moment, only one geo column in a table
                    geometryAttributeName = rs.getString(1);
                }
                if (geometryAttributeName == null) {
                    errorMessage = "getColumnName: cannot get geometry_column for table: "
                            + tableName;
                    errorMessage += ".\n<br>Is the table is registered in metadata (call to addGeometryColumn)";
                } else {

                    query = new StringBuffer("SELECT a.").append(
                            geometryAttributeName).append(".GET_GTYPE() FROM ")
                            .append(tableName).append(" a where rownum=1");
                    rs = stmt.executeQuery(query.toString());
                    rs.next();
                    int dbtype = rs.getInt(1);

                    // set the type
                    if (dbtype == ORA_POINT) {
                        geomTypeCode = Geometry.POINT;
                    } else if (dbtype == ORA_MULTIPOINT) {
                        geomTypeCode = Geometry.MULTIPOINT;
                    } else if (dbtype == ORA_LINE) {
                        geomTypeCode = Geometry.LINESTRING;
                    } else if (dbtype == ORA_MULTILINE) {
                        geomTypeCode = Geometry.MULTILINESTRING;
                    } else if (dbtype == ORA_POLYGON) {
                        geomTypeCode = Geometry.POLYGON;
                    } else if (dbtype == ORA_MULTIPOLYGON) {
                        geomTypeCode = Geometry.MULTIPOLYGON;
                    } else {
                        geomTypeCode = Geometry.NULL;
                    }

                    // gets metadata for the table
                    DatabaseMetaData dbm = con.getMetaData();
                    // for oracle, username will be used as schema for
                    // geographic tables
                    rs = dbm.getColumns(null, getUserName().toUpperCase(),
                            tableName, "%");

                    dbVersion = dbm.getDriverMajorVersion();

                    if (metadata == null) {
                        metadata = new Vector<GeometryClassFieldBean>();
                    }
                    while (rs.next()) {
                        // skip geo column
                        if (rs.getString(4).equals(geometryAttributeName)) {
                            continue;
                        }
                        // nullable field:
                        String nullable = rs.getInt(11) == DatabaseMetaData.columnNullable ? "Nullable"
                                : "Not Null";
                        GeometryClassFieldBean as = new GeometryClassFieldBean(
                                rs.getString(4), rs.getString(6), rs.getInt(7),
                                nullable);
                        metadata.add(as);
                    }

                    // gets the number of geometries in this table
                    query = new StringBuffer("select count(*) from ");
                    query.append(tableName);
                    query.append(" where ").append(geometryAttributeName)
                            .append(" is not null");
                    rs = stmt.executeQuery(query.toString());
                    rs.next();
                    numGeometries = rs.getInt(1);

                    // this query gives the SRTEXT and SRID of the geo table:
                    query = new StringBuffer(
                            "select WKTEXT, MDSYS.CS_SRS.SRID from MDSYS.CS_SRS, USER_SDO_GEOM_METADATA ");
                    query
                            .append("where TABLE_NAME='")
                            .append(tableName)
                            .append(
                                    "' and  MDSYS.CS_SRS.SRID=USER_SDO_GEOM_METADATA.SRID");
                    rs = stmt.executeQuery(query.toString());
                    if (rs.next()) {
                        SRText = rs.getString(1);
                        SRID = rs.getInt(2);
                    }

                    // this query gives the geodata extent, based on the OpenGIS
                    // "envelope" method
                    if (numGeometries > 0) {
                        query = new StringBuffer(
                                "SELECT * FROM TABLE (CAST((SELECT DIMINFO FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='");
                        query.append(tableName);
                        query.append("') AS MDSYS.SDO_DIM_ARRAY)) LISTE");
                        rs = stmt.executeQuery(query.toString());
                        double xmin = 0.0;
                        double ymin = 0.0;
                        double xmax = 0.0;
                        double ymax = 0.0;
                        // X values
                        rs.next();
                        xmin = rs.getDouble("SDO_LB");
                        xmax = rs.getDouble("SDO_UB");

                        // Y values
                        rs.next();
                        ymin = rs.getDouble("SDO_LB");
                        ymax = rs.getDouble("SDO_UB");

                        extent = new Extent(xmin, ymin, xmax, ymax);
                    }

                    rs = dbm.getPrimaryKeys(null, null, tableName);
                    while (rs.next()
                            && (uniqueField == null || uniqueField
                                    .equalsIgnoreCase("null"))) {
                        /*
                         * String s1 = rs.getString(1); String s2 =
                         * rs.getString(2); String s3 = rs.getString(3); String
                         * s4 = rs.getString(4);
                         */
                        uniqueField = rs.getString(4);
                    }
                    rs.close();

                    ret = true;
                }
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
                } catch (SQLException sqle) {
                }
            }
            return ret;
        }

    }

    @Override
    public Vector<Vector<Object>> getSampleData(int from, int to) {
        ParameterizedCallable<Vector<Vector<Object>>, Connection> operation = new LoadOracleSampleData(
                from, to);
        return DatabaseDataAccessHelper.runOperation(getConnectionURI(),
                operation, logger);
    }

    public String getConnectionURI() {
        return "jdbc:oracle:thin:" + getUserName() + "/" + getUserPwd() + "@"
                + getHost() + ":" + this.dbPort + ":" + getOwner().getName();
    }

    @Override
    protected Layer createMSLayerInner(RGB color) {
        msLayer = new Layer();
        msLayer.setName(name);

        msLayer.setConnection(userName + "/" + userPwd + "@" + datasourceName);
        msLayer.setConnectionType(MsLayer.ORACLESPATIAL);
        // original data implementation from super class
        // msLayer.setData(columnName + " from " + schemaName + "." + name
        // + " USING UNIQUE " + uniqueField + " USING SRID=" + SRID);

        // from original LayerGeneralProperties. I brought it here because it
        // seems more complete
        String strData = this.geometryAttributeName + " from "
                + this.schemaName + "." + this.tableName;
        if (getUniqueField() != null || getSRText() != null) {
            strData += " USING";
            if (getUniqueField() != null) {
                strData += " UNIQUE " + getUniqueField();
            }
            if (getSRText() != null) {
                strData += " SRID=" + getSRID() + " FILTER";
            }
            if (dbVersion == 8) {
                strData += " VERSION 8i";
            } else if (dbVersion == 9) {
                strData += " VERSION 9i";
            } else if (dbVersion == 10) {
                strData += " VERSION 10g";
            }
        }
        msLayer.setData(strData);
        // a default display class for this geoobject
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
        return false;
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
        // COMPLETE implement so that features are created
        return null;
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

    private Option<OracleDataStore> getDatastore() {
        Factory<OracleDataStore, Map<String, Object>> factory = new GasOracleDatastoreFactory(
                logger);
        Map<String, Object> factoryParams = new HashMap<String, Object>();
        factoryParams.put("dbtype", "oracle");
        factoryParams.put("host", this.host);
        factoryParams.put("instance", this.datasourceName);
        factoryParams.put("port", this.dbPort);
        factoryParams.put("user", userName);
        factoryParams.put("passwd", this.userPwd);

        OracleDataStore dataStore = ObjectCache.getInstance().getCachedObject(new DataStoreCacheable(getConnectionURI()), factory, factoryParams);
        if (dataStore == null) {
            return Option.none();
        }
        return Option.some(dataStore);
    }

    private Option<FeatureSource<SimpleFeatureType,SimpleFeature>> getFeatureSource() {
        try {
            Option<OracleDataStore> ds = getDatastore();
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
        Option<FeatureSource<SimpleFeatureType,SimpleFeature>> option = getFeatureSource();
        if (option.isNone()) {
            return Option.none();
        }
        return Option.some(option.get().getSchema());
    }

    /** MISSING JAVADOC ! */
    public String getUserName() {
        return this.userName;
    }

    /** MISSING JAVADOC ! */
    public String getUserPwd() {
        return this.userPwd;
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

}
