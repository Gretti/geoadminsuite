/**
 * 
 */
package org.geogurus.data.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.Datasource;
import org.geogurus.data.DatasourceType;
import org.geogurus.data.Option;
import org.geogurus.tools.sql.ConPool2;

/**
 * Datasource for Accessing a Postgis database.
 * 
 * @author jesse
 */
public class PostgisDatasource extends Datasource {
    private static final long serialVersionUID = 1L;
    protected String dbPort = "5432";
    protected String userName = null;
    protected String userPwd = null;

    /**
     * Creates a new instance of type PostgisDatasource
     * 
     */
    public PostgisDatasource() {
    }

    public PostgisDatasource(String name, String dbPort, String host,
            String userName, String userPwd) {
        super(name, host, DatasourceType.VECTOR);
        this.dbPort = dbPort;
        this.userName = userName;
        this.userPwd = userPwd;
    }

    /**
     * Finds all geographic tables for this db, and construct their
     * geometryClass equivalent. All these geometryClasses are stored in the
     * Datasource dataList hashtable, with the gc'id as a key. The
     * geometryClasses built here have the minimal set of information.
     */
    public boolean load() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        boolean ret = false;
        PostgisDataAccess gc = null;

        try {
            ConPool2 conPool = ConPool2.getInstance();
            con = conPool.getConnection(conPool.getConnectionURI(this.host,
                    this.dbPort, this.name, this.userName, this.userPwd,
                    ConPool2.DBTYPE_POSTGRES));

            if (con == null) {
                logger.warning("cannot get connection for host: " + this.host
                        + "," + this.name + "," + this.dbPort + ","
                        + this.userName);
                return false;
            }
            stmt = con.createStatement();
            // this query gives all geometric tables registered into the
            // OpenGIS metadata tables:
            // caution when db is not Postgis enabled
            // The query selects distinct table_name to avoid specific
            // behaviour for multiple geometry
            // columns for the same data (not assumed by now)
            // Selection reports only existing table in case of intempestive
            // deletion of tables without cleaning geometry_columns
            // query =
            // "select distinct g.f_table_name from geometry_columns g, pg_tables p where g.f_table_name = p.tablename"
            // ;

            // new query to get all tables aving a geometric column, not
            // necessirally registered into geometry_columns.
            // this allows to list views also
            query = "SELECT DISTINCT table_name, table_schema "
                    + "FROM information_schema.columns "
                    + "WHERE udt_name = 'geometry' "
                    + "ORDER BY table_schema, table_name";
            rs = stmt.executeQuery(query);
            logger.fine("query to list geo tables (host: " + this.host
                    + " name: " + this.name + ") : " + query);

            final int tableNameColumnIndex = 1;
            final int schemaColumnIndex = 2;
            PostgisAccessFactory factory = new PostgisAccessFactory();
            while (rs.next()) {
                ConnectionParams bean = new ConnectionParams(this);
                bean.host = host;
                bean.dbname = name;
                bean.port = dbPort;
                bean.username = userName;
                bean.password = userPwd;
                bean.schema = rs.getString(schemaColumnIndex);
                bean.table = rs.getString(tableNameColumnIndex);
                gc = factory.createOne(bean);
                // needs to get metadata now, as this information is needed
                // to see if the layer is valid (18/03/2008)
                gc.setUniqueField(null);
                if (gc.getUniqueField() == null) {
                    // layers without unique keys cannot be displayed by
                    // mapserver skip it
                    logger
                            .warning("Layer: "
                                    + gc.getName()
                                    + " does not have a unique key. MapServer cannot display it."
                                    + " Add a unique key to this layer to enable it in the GAS");
                } else {
                    getDataList().put(gc.getID(), gc);
                }
            }
            rs.close();
            stmt.close();
            ret = true;

        } catch (SQLException sqle) {
            if (sqle.getMessage().indexOf("geometry_columns") != -1) {
                // no geometry_columns table for this DB: skip this
            } else {
                String errorMessage = "GeometryClassExplorer.doList: SQLException: "
                        + sqle.getMessage();
                errorMessage += "<br>query was: <code>" + query.toString()
                        + "</code>";
                logger.warning(errorMessage);
            }

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception sqle2) {
                sqle2.printStackTrace();
            }
        }

        return ret;
    }

    @Override
    public <T> Option<T> resource(Class<T> resourceType) {
        return null;
    }

}
