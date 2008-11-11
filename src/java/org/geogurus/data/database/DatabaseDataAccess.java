package org.geogurus.data.database;

import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;

/**
 * Superclass for all Database DataAccess implementations
 * 
 * @author jesse
 */
// we may be able to get rid of this and put the code in the Helper class
// code sharing through inheritance is not a good pattern
public abstract class DatabaseDataAccess extends DataAccess {

    /** the DB major version for this datasource */
    protected int dbVersion;

    /**
     * Creates a new instance of type DatabaseDataAccess
     */
    public DatabaseDataAccess(String name, Datasource owner, DataAccessType type) {
        super(name, owner, type);
        // TODO Auto-generated constructor stub
    }

    private static final long serialVersionUID = 1L;

    // /** MISSING JAVADOC ! */
    // public String getIDColumn() {
    // return idColumn;
    // }
    //
    // /**
    // * The whereClause used to retrieve geo objects from DB, if any passed to
    // * getGeometriesFromDb
    // */
    // public String whereClause;
    //
    // /**
    // * Retrieve geometries corresponding to this class from the database.
    // *
    // * @param con
    // * a valid java.sql.connection for the target database. this
    // * connection IS NOT closed here
    // * @param whereClause
    // * the optionnal where clause to append to the select query to
    // * filter the geometry retrieval. Can be null or empty if no
    // * clause is wanted (should add a tableClause to allow joins. For
    // * the moment, it is possible to append table names in the where
    // * clause, like: table1 t1, table1 t2 where t1.id = t2.id...)
    // *
    // * @return false in case of error. errorMessage contains this error
    // */
    // public boolean getGeometriesFromDB(Connection con, String wc) {
    // if (con == null) {
    // errorMessage = "getGeometriesFromDB: null Connection Object";
    // return false;
    // }
    // if (columnName == null) {
    // if (!this.loadMetadata()) {
    // // error message is set by the called function
    // return false;
    // }
    // }
    // whereClause = wc == null ? "" : wc;
    // Statement stmt = null;
    // try {
    // stmt = con.createStatement();
    // StringBuffer query = new StringBuffer("select ");
    // query.append(idColumn).append(",astext(");
    // if (type == Geometry.MULTILINESTRING || type == Geometry.MULTIPOINT
    // || type == Geometry.MULTIPOLYGON) {
    // // takes only the first object to avoid kaboum crash => need to
    // // manage collections in kaboum
    // query.append("geometryn(");
    // }
    // query.append(columnName);
    // if (type == Geometry.MULTILINESTRING || type == Geometry.MULTIPOINT
    // || type == Geometry.MULTIPOLYGON) {
    // // takes only the first object to avoid kaboum crash => need to
    // // manage collections in kaboum
    // // NRI, 6 juin 2005: indices begin at 1 in the new postgis
    // // version...
    // query.append(",1)");
    // }
    // query.append("), box3d(").append(columnName).append(") from ")
    // .append(schemaName).append(".").append(tableName).append(
    // " ");
    // query.append(whereClause);
    // logger.fine("getGeometriesFromDB: query: " + query.toString());
    // ResultSet rs = stmt.executeQuery(query.toString());
    // // the geometry's extent
    // Extent e = null;
    // geometries = new Vector<Geometry>(numGeometries);
    // while (rs.next()) {
    // if (rs.getString(2) != null) {
    // e = new Extent(rs.getString(3));
    // // skip null geometries
    // // geometries are constructed with this id, not name
    // geometries.add(new Geometry(rs.getString(1), this.type, rs
    // .getString(2), this.id, e));
    // // expand this extent for the new geometry
    // extent.add(e);
    // }
    // }
    // return true;
    // } catch (SQLException sqle) {
    // errorMessage = "getGeometriesFromDB: SQLException: "
    // + sqle.getMessage();
    // sqle.printStackTrace();
    // return false;
    // } finally {
    // try {
    // stmt.close();
    // } catch (Exception e) {
    // }
    // }
    // }
    //
    // /** MISSING JAVADOC ! */
    // public String getWhereClause() {
    // return whereClause == null ? "" : whereClause;
    // }

}