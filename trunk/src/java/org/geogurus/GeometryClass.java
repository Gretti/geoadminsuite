package org.geogurus;

import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.raster.RasterRegistrar;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.sql.ConPool;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Vector;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.MapClass;
import org.geogurus.mapserver.objects.RGB;
import uk.ac.leeds.ccg.dbffile.Dbf;
import uk.ac.leeds.ccg.shapefile.Shapefile;

/**
 * Title:        geOnline Server classes
 * Description:  Set of Java classes to make the link between kaboum client and shape files / postgis DB containing geodata.
 * Copyright:    Copyright (c) 2002
 * Company:      Geogurus
 * @author Nicolas Ribot
 * @version 1.0
 */
/**
 * Represents a Kaboum class of geometric objects: a logical set of objets sharing
 * some properties ( representation, for instance).
 *
 * TODO: Change int static variables to byte.
 */
public class GeometryClass implements Serializable, Comparable {

    /** the constants for point type (see kaboum API for details */
    public static final int K_TYPE_POINT = 1;
    public static final int K_TYPE_BOX = 2;
    public static final int K_TYPE_CIRCLE = 3;
    public static final int K_TYPE_IMAGE = 4;
    /** CLASS indicates the type of datasource as byte this geometry comes from*/
    public static final byte PGCLASS = 11;
    public static final byte ESRIFILECLASS = 12;
    public static final byte TIFFCLASS = 13;
    public static final byte IMGCLASS = 14;
    public static final byte ECWCLASS = 14;
    public static final byte MAPCLASS = 15;
    public static final byte ORACLASS = 16;
    public static final Byte[] DATASOURCE_TYPES_ASBYTE = new Byte[]{
        new Byte(PGCLASS),
        new Byte(ESRIFILECLASS),
        new Byte(TIFFCLASS),
        new Byte(IMGCLASS),
        new Byte(ECWCLASS),
        new Byte(ORACLASS)
    };
    /** CLASS indicates the type of datasource as String this geometry comes from*/
    public static final String STRING_PGCLASS = "postgis";
    public static final String STRING_ESRIFILECLASS = "shapefile";
    public static final String STRING_TIFFCLASS = "tifffile";
    public static final String STRING_IMGCLASS = "imgfile";
    public static final String STRING_ECWCLASS = "ecwfile";
    public static final String STRING_MAPCLASS = "mapfile";
    public static final String STRING_ORACLASS = "oracle";
    //Bundle datasources keys for gas application
    public static final String[] DATASOURCE_TYPES_ASSTRING = new String[]{
        STRING_PGCLASS,
        STRING_ESRIFILECLASS,
        STRING_TIFFCLASS,
        STRING_IMGCLASS,
        STRING_ECWCLASS,
        STRING_ORACLASS
    };
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
    /**General variables from System*/
    public static final String ls = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");
    /** The default transparency level for the MS representation of this class */
    public static final int MS_LAYER_TRANSPARENCY = 50;
    /** The unique identifier for this geometryClass. Used to store gc in hashtables */
    protected String id;
    /** The generic name of this class (for instance used as display name in a web application */
    protected String name;
    /** The name of the DB table, or file representing this class */
    protected String tableName;
    /** The name of the DB column containing geographic objects of this class */
    protected String columnName;
    /** The name of the DB column containing the unique ID (Primary key) for this geometry class
     *  Must be set at the construction: a geometry must have a unique ID to be treated
     *  by the GIS
     */
    protected String idColumn;
    /** The name of the server = host hosting the geographic information represented
     * by this object
     */
    protected String host;
    /** The DB port */
    protected String dbPort;
    /** the username which can connect to this database*/
    protected String userName;
    protected String userPwd;
    /** The spatial reference text
     * the OpenGIS Spatial reference system string for this postgis table,
     * or an epsg string in case of shapefile. Format for this string is:
     * <name / subname> | <list of parameters>
     * This string is built from the EPSG proj4 reference file (provided with mapserver)
     */
    protected String SRText;
    /** The spatial reference identifier*/
    protected int SRID;
    /** The Vector of Geometry objects composing this class. Vector is used
     * to guarantee the order of geometries when retrieved from DB
     */
    protected Vector geometries;
    /**The number of geometries not null in the table. Can be different from geometries.size();
     * or might be available when geometries is null (no geometries got from db)
     */
    protected int numGeometries;
    /** the vector of index of selected geometries after client play */
    protected Vector selectedGeometries;
    /** the extent for all geometries */
    protected Extent extent;
    /** the geometric type of this GeometryClass*/
    protected int type;
    /** the datasource name
     * A database name if datasourceType is db, a folder path otherwise
     */
    protected String datasourceName;
    /** the datasource type, either a db table or a file */
    protected byte datasourceType;
    /** the DB major version for this datasource */
    protected int dbVersion;
    /** Unique field name */
    protected String uniqueField;
    /** the vector of fields attribute information */
    Vector columnInfo = null;
    /** the vector of array of attribute values information */
    Vector columnValues = null;
    /** The whereClause used to retrieve geo objects from DB, if any passed to getGeometriesFromDb */
    public String whereClause;
    /** True is this class should be visible when the application starts*/
    public boolean isVisible;
    /** True if this GeometryClass is to participate to the topology without being displayed:
     *  invisible java objects will be instantiated, and MapServer corresponding theme
     *  could be displayed too */
    public boolean isLocked;
    /** True is this GeometryClass is active in this application: */
    public boolean isActive;
    /** True is this GeometryClass is a surrounding theme: all geometries must be edited inside
     *  this theme (ex: exploit theme)*/
    public boolean isSurrounding;
    /** true is this GeometryClass' geo objects participate to topology control.
     *  If so, they cannot intersect or be intersected by another object */
    public boolean isComputed;
    /** true is this GeometryClass was set editable by the user. */
    public boolean isEdited;
    /** The Layer representing this GeometryClass */
    protected Layer msLayer;
    /** tells if this GeometryClass should be displayed by mapserver: a mapfile
     *  containing the definition for the msLayer object will be generated.
     *  NOTE: a properties object with definition for this GeometryClass MUST be accessible
     *  by the application
     */
    public boolean displayInMapserver;
    /** tells if this GeometryClass should be displayed by kaboum:
     *  NOTE: a valid Layer object must be created for this object
     */
    public boolean displayInKaboum;
    /** the error message generated by this class */
    protected String errorMessage;
    /** the Geometry identifier for the geometry to be created or modified in Kaboum:
     *  Allows geometric creation/modification for an existing object in DB
     */
    protected String editedGeometryID;
    /** the array of columns to add to the insert query */
    protected String[] insertColumns;
    /** the array of values corresponding to insertColumns to add to the insert query */
    protected String[] insertValues;
    /** the declared area of the edited geometry, used to perform area control */
    protected double declaredArea;
    /////////////////////////////////
    // Graphical representation of this class, for Kaboum.
    // see kaboum API for details
    /** is this geometryClass should be represented with a fill color */
    public boolean isFilled;
    /** The color of this GeometryClass */
    protected Color fillColor;
    /** The hilite color of this GeometryClass */
    protected Color hiliteColor;
    /** The modified color for this GeometryClass */
    protected Color modifiedColor;
    /** The kind of control points for this GeometryClass */
    protected int pointType;
    /** The color of a control point */
    protected Color pointColor;
    /** The color of an hilited control point */
    protected Color pointHiliteColor;
    /** The color of the drawing point when drawing is forbidden */
    protected Color pointForbiddenColor;
    /** The height of a control point, in pixel */
    protected int pointHeight;
    /** The width of a control point, in pixel */
    protected int pointWidth;
    /** The URL for the image to use for the point */
    protected String pointImageURL;

    /**
     * Constructs a new GeometryClass with the given name and tableName
     */
    public GeometryClass(String name, String tableName, String idColumn) {
        this.id = "" + System.identityHashCode(this);
        this.name = name;
        this.tableName = tableName;
        columnName = null;
        // default type set to polygon
        type = Geometry.POLYGON;
        isVisible = true;
        isLocked = false;
        isActive = true;
        isSurrounding = false;
        isComputed = false;
        isEdited = false;
        displayInMapserver = true;
        displayInKaboum = false;
        this.idColumn = idColumn;
        extent = new Extent();
        // an invalid declared area, to allow JS client to discard non existing declared area
        // for an object.
        this.declaredArea = -1.0;
        // kaboum default parameters:
        isFilled = true;
        fillColor = Color.red;
        hiliteColor = Color.blue;
        modifiedColor = Color.yellow;
        pointType = GeometryClass.K_TYPE_BOX;
        pointColor = Color.black;
        pointHiliteColor = Color.red;
        pointForbiddenColor = Color.green;
        pointHeight = 5;
        pointWidth = 5;
        pointImageURL = "";
        // default new object to Postgis class, as we want to support this type mainly.
        datasourceType = PGCLASS;
    }

    /**
     * Constructs a new GeometryClass with the given Db information
     * set the unique column id to be "oid": works only in postgres, where such a pk column is added
     * for each table
     */
    public GeometryClass(String host, String dbName, String tableName, String dbPort, String userName, String userPwd, short type) {
        this(tableName, tableName, "oid");
        this.host = host;
        this.datasourceName = dbName;
        this.dbPort = dbPort;
        this.userName = userName;
        this.userPwd = userPwd;
        datasourceType = (byte) type;
    }

    /**
     * Return the Kaboum keyword for a geometry type based on the DB values for geometry
     * types
     */
    public String getKaboumType() {
        switch (type) {
            case Geometry.POINT:
            case Geometry.MULTIPOINT:
                return "MS_LAYER_POINT";
            case Geometry.LINESTRING:
            case Geometry.MULTILINESTRING:
                return "MS_LAYER_POLYLINE";
            case Geometry.POLYGON:
            case Geometry.MULTIPOLYGON:
                return "MS_LAYER_POLYGON";
        }
        // the default value for majority of themes
        return "MS_LAYER_POLYLINE";
    }

    /**
     * Return the geometry type of this theme: for the moment, returns the type
     * of the first geometry in the geometries vector. Should perform test to ensure
     * that all geometries have the same type.
     * If the type has no geometries, get the type from the msLayer object
     */
    public int getGeoType() {
        type = msLayer.getType();
        if (type == 0 && type != Layer.NONE) {
            // type is not set by DB values, try to guess it from the
            //MapServerLayer object
            if (type == Layer.POINT) {
                return Geometry.POINT;
            }
            if ((type == Layer.POLYGON) ||
                    (type == Layer.POLYLINE_POLYGON)) {
                return Geometry.POLYGON;
            }
            if ((type == Layer.POLYLINE) ||
                    (type == Layer.LINE)) {
                return Geometry.LINESTRING;
            }
            return Geometry.NULL;
        }
        return type;
    }

    /**
     * Retrieve geometries corresponding to this class from the database.
     * @param con a valid java.sql.connection for the target database.
     *         this connection IS NOT closed here
     * @param whereClause the optionnal where clause to append to the select query
     * to filter the geometry retrieval. Can be null or empty if no clause is wanted
     * (should add a tableClause to allow joins. For the moment, it is possible to
     * append table names in the where clause, like:
     * table1 t1, table1 t2 where t1.id = t2.id...)
     *
     * @return false in case of error. errorMessage contains this error
     */
    public boolean getGeometriesFromDB(Connection con, String wc) {
        if (con == null) {
            errorMessage = "getGeometriesFromDB: null Connection Object";
            return false;
        }
        if (columnName == null) {
            if (!this.getTableMetadataPg(con) && !this.getTableMetadataOra(con)) {
                // error message is set by the called function
                return false;
            }
        }
        whereClause = wc == null ? "" : wc;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            StringBuffer query = new StringBuffer("select ");
            query.append(idColumn).append(",astext(");
            if (type == Geometry.MULTILINESTRING || type == Geometry.MULTIPOINT || type == Geometry.MULTIPOLYGON) {
                // takes only the first object to avoid kaboum crash => need to manage collections in kaboum
                query.append("geometryn(");
            }
            query.append(columnName);

            if (type == Geometry.MULTILINESTRING || type == Geometry.MULTIPOINT || type == Geometry.MULTIPOLYGON) {
                // takes only the first object to avoid kaboum crash => need to manage collections in kaboum
                //NRI, 6 juin 2005: indices begin at 1 in the new postgis version...
                query.append(",1)");
            }
            query.append("), box3d(").append(columnName).append(") from ").append(tableName).append(" ");
            query.append(whereClause);

            LogEngine.log("getGeometriesFromDB: query: " + query.toString());

            ResultSet rs = stmt.executeQuery(query.toString());

            // the geometry's extent
            Extent e = null;

            geometries = new Vector(numGeometries);

            while (rs.next()) {
                if (rs.getString(2) != null) {
                    e = new Extent(rs.getString(3));
                    // skip null geometries
                    // geometries are constructed with this id, not name
                    geometries.add(new Geometry(rs.getString(1),
                            this.type,
                            rs.getString(2),
                            this.id,
                            e));
                    // expand this extent for the new geometry
                    extent.add(e);
                }
            }
            return true;

        } catch (SQLException sqle) {
            errorMessage = "getGeometriesFromDB: SQLException: " + sqle.getMessage();
            sqle.printStackTrace();
            return false;
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * make an insert or update on the database for the given geometry.
     * Insert this geometry in the geometries vector to maintain coherence between
     * this class and the database.
     * Performs the DB connection based on this object DB information,
     * and call addGeoemtryToDB(con, geom)
     *
     * @param con a valid java.sql.connection for the target database
     * @param geom the Geometry to add
     * @return false in case of error. errorMessage contains this error
     */
    public boolean addGeometryToDB(Geometry geom) {
        Connection con = null;
        try {
            con = ConPool.getConnection(host, dbPort, datasourceName, userName, userPwd, "postgres");
            if (con == null) {
                LogEngine.log("GeometryClass.addGeometryToDB: Cannot get a connection (null) for: " + datasourceName + " " + dbPort + " " + userName + " " + userPwd);
                return false;
            }
            return addGeometryToDB(con, geom);

        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * makes an insert or update on the database for the given geometry.
     * Insert this geometry in the geometries vector to maintain coherence between
     * this class and the database.
     *
     * @param con a valid java.sql.connection for the target database
     * @param geom the Geometry to add
     * @return false in case of error. errorMessage contains this error
     */
    public boolean addGeometryToDB(Connection con, Geometry geom) {
        if (con == null) {
            errorMessage = "addGeometryToDB: null Connection Object";
            return false;
        }
        if (geom == null) {
            errorMessage = "addGeometryToDB: null geometry to add";
            return false;
        }
        if (columnName == null) {
            if (!this.getTableMetadataPg(con) && !this.getTableMetadataOra(con)) {
                // error message is set by the called function
                return false;
            }
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            StringBuffer query = new StringBuffer("");

            // Nico, 27 aout 2002: the firt query is to retrieve geometry's SRID:
            // find_srid() may return null if no entry is found in the spatial_ref_sys table
            // super heavy way to get SRID: getInt() on a null SRID returns 0, so must handle this case
            // as 0 is a potential valid SRID
            int srid = -1;
            query.append("select count(find_srid('");
            query.append(datasourceName).append("','").append(tableName).append("','");
            query.append(columnName).append("')), find_srid('");
            query.append(datasourceName).append("','").append(tableName).append("','");
            query.append(columnName).append("')");

            ResultSet rs = stmt.executeQuery(query.toString());
//System.out.println("query for srid: " + query.toString());
            if (rs.next()) {
                if (rs.getInt(1) == 1) {
                    srid = rs.getInt(2);
//System.out.println("srid found: "+rs.getInt(1));
                }
            }
            query = new StringBuffer("");

            if (geom.id.equals("NEW")) {
                // kaboum keyword for a newly created geometry: insert: see if some extra columns/values pairs
                // should be added to this query
                query.append("insert into ").append(tableName);
                query.append(" (").append(columnName);

                // adds extra columns names
                if (insertColumns != null) {
                    for (int i = 0; i < insertColumns.length; i++) {
                        query.append(",").append(insertColumns[i]);
                    }
                }

                query.append(") values(geometryFromText('");
                query.append(geom.rebuildWkt()).append("',").append(srid).append(")");

                // adds extra values
                if (insertValues != null) {
                    for (int i = 0; i < insertValues.length; i++) {
                        query.append(",").append(insertValues[i]);
                    }
                }

                query.append(")");
            } else {
                // existing geometry: update
                query.append("update ").append(this.tableName);
                query.append(" set ").append(columnName);
                query.append("=geometryFromText('").append(geom.rebuildWkt());
                query.append("',").append(srid).append(")");
                query.append(" where ").append(idColumn).append("=");
                query.append(geom.id);
            }
            LogEngine.log("addGeometryToDB: query: " + query.toString());
            stmt.executeUpdate(query.toString());

            // gets geom id in case of insert, and add the new geom to the
            // vector. Also get the geometries extent in this query to minimize
            // number of queries
            StringBuffer seqQuery = new StringBuffer("");
            if (geom.id.equals("NEW")) {
                // construct the sequence query to get geom id generated by the sequence
                // after the insert query.
                // if idColumn is oid, a Postgresql-specific column, special case to retrieve the value
                if (idColumn.equals("oid")) {
                    seqQuery.append(",max(oid) from " + tableName);
                } else {
                    // waaooo, the sql query to get the sequence name from a table name.
                    // should check if it works all the time, and handle the case where several sequences
                    // are put on the same table
                    seqQuery.append(",currval((select substring(substring(adsrc, strpos(adsrc, '''')+1), ");
                    seqQuery.append("0, strpos(substring(adsrc, strpos(adsrc, '''')+1), ''''))");
                    seqQuery.append(" from pg_attrdef, pg_class where relname='");
                    seqQuery.append(tableName);
                    seqQuery.append("' and relfilenode=adrelid and adnum=1)");
                    seqQuery.append(")");
                }
            }
            // the final query: first part is to get the geometry's extent, second part
            // is to get sequence current value, if relevant (insert mode)
            query = new StringBuffer("select box3d(geometryFromText('").append(geom.rebuildWkt());
            query.append("',0))");
            query.append(seqQuery.toString());
            //LogEngine.log("addGeoemtryToDB: query to get currval and extent: " + query.toString());
            rs = stmt.executeQuery(query.toString());
            //only one result guaranteed by the query
            rs.next();
            // construct geom's extent and add it to this extent
            Extent e = new Extent(rs.getString(1));
            extent.add(e);
            geom.setExtent(e);
            // gets the geom id in case of insert query
            if (geom.id.equals("NEW")) {
                geom.id = rs.getString(2);
            }

            geometries.add(geom);

            return true;

        } catch (SQLException sqle) {
            errorMessage = "AddGeometryToDB: SQLException: " + sqle.getMessage();
            return false;
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * removes the given geometry from DB.
     * Creates a connection to the database before calling the underlying function
     *
     * @param con a valid java.sql.connection for the target database
     * @param geom the Geometry to remove.
     * @param hardRemove true to indicate to remove the entire DB record, false to indicate
     * an update on the geometry field
     *
     * @return false in case of error. errorMessage contains this error
     */
    public boolean removeGeometryFromDB(String geomid, boolean hardRemove) {
        Connection con = null;
        try {
            con = ConPool.getConnection(host, dbPort, datasourceName, userName, userPwd, "postgres");
            if (con == null) {
                LogEngine.log("GeometryClass.removeGeometryToDB: Cannot get a connection (null) for: " + datasourceName + " " + dbPort + " " + userName + " " + userPwd);
                return false;
            }
            return removeGeometryFromDB(con, geomid, hardRemove);

        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * removes the given geometry from DB.
     * Remove the field or the entire row ?
     *
     * @param con a valid java.sql.connection for the target database
     * @param geom the Geometry to remove.
     * @param hardRemove true to indicate to remove the entire DB record, false to indicate
     * an update on the geometry field
     *
     * @return false in case of error. errorMessage contains this error
     */
    public boolean removeGeometryFromDB(Connection con, String geomid, boolean hardRemove) {
        if (con == null) {
            errorMessage = "removeGeometryFromDB: null Connection Object";
            return false;
        }
        if (columnName == null) {
            if (!this.getTableMetadataPg(con) && !this.getTableMetadataOra(con)) {
                // error message is set by the called function
                return false;
            }
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            StringBuffer query = new StringBuffer("");

            if (hardRemove) {
                query.append("delete from ");
                query.append(this.tableName);
                query.append(" where ").append(this.idColumn);
                query.append("=").append(geomid);
            } else {
                query.append("update ").append(tableName);
                query.append(" set ").append(columnName);
                query.append("=null where ").append(idColumn).append("=").append(geomid);
            }
            LogEngine.log("removeGeometryFromDB: query: " + query.toString());
            stmt.executeUpdate(query.toString());

            // removes the geometry from this class
            geometries.remove(getGeometry(geomid));

            return true;

        } catch (SQLException sqle) {
            errorMessage = "removeGeometryFromDB: SQLException: " + sqle.getMessage();
            return false;
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Gets the geometry whose id is given: if it is found in the hashtable
     * returns it, else connect to the db.
     * con can be null if geometry is present in the hashtable
     * Returns null if not found in the DB
     */
    public Geometry getGeometryFromDB(Connection con, String id) {
        Geometry g = getGeometry(id);
        if (g != null) {
            return g;
        }
        // the geometry is not present in the hashtable, fetch it from db
        if (con == null) {
            errorMessage = "getGeometryFromDB: null Connection Object, and geom with id" + id + " not cached";
            return null;
        }
        if (columnName == null) {
            if (!this.getTableMetadataPg(con) && !this.getTableMetadataOra(con)) {
                // error message is set by the called function
                return null;
            }
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            StringBuffer query = new StringBuffer("select astext(");
            query.append(columnName).append(") from ").append(tableName);
            query.append(" where ").append(idColumn).append("=").append(id);

            ResultSet rs = stmt.executeQuery(query.toString());
            if (!rs.next()) {
                //no geom in DB
                errorMessage = "getGeometryFromDB: cannot get geometry with id: " + id + "\n<b>";
                errorMessage += "query was: " + query.toString();
                return null;
            }
            g = new Geometry(id, type, rs.getString(1), name);
            geometries.add(g);
            return g;

        } catch (SQLException sqle) {
            errorMessage = "getGeometryFromDB: SQLException: " + sqle.getMessage();
            return null;
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Set the modifiable attribute of this geometries according to identifiers
     * contained in the given Vector
     * @param ids the vector of Geometry identifiers that are modifiable. These identifiers
     * correspond to the attribute part of the schema, as given by a client, not the primary key
     * identifiers in the Geometry table. AKA ENG_ID
     * geometry id mechanism not yet finished
     */
    public void setModifiableGeometries(Vector ids) {
        if (ids == null) {
            return;
        }
        for (Iterator iter = ids.iterator(); iter.hasNext();) {
            Geometry geom = getGeometry((String) iter.next());

            if (geom != null) {
                geom.modifiable = true;
            }
        }
    }

    /**
     * Set the isSelected attribute of this geometries according to identifiers
     * contained in the given Vector
     * @param ids the vector of Geometry identifiers that are modifiable. These identifiers
     * correspond to the attribute part of the schema, as given by a client, not the primary key
     * identifiers in the Geometry table. AKA ENG_ID
     * geometry id mechanism not yet finished
     */
    public void setSelectableGeometries(Vector ids) {
        if (ids == null) {
            return;
        }
        for (Iterator iter = ids.iterator(); iter.hasNext();) {
            Geometry geom = getGeometry((String) iter.next());

            if (geom != null) {
                geom.selected = true;
            }
        }
    }

    /**
     * MISSING JAVADOC !
     */
    public String readProjectionFile() {
        String dsName = getDatasourceName() + fs + getTableName();
        String projFile = dsName.substring(0, dsName.indexOf(".")) + ".glp";
        String projection = null;
        try {
            if (new File(projFile).exists()) {
                BufferedReader in = new BufferedReader(new FileReader(projFile));
                String s = "";
                int i = 0;
                while ((s = in.readLine()) != null) {
                    if (i == 0) {
                        projection = s;
                    } else {
                        projection += "|" + s;
                    }
                    i++;
                }
                in.close();
            }
        } catch (Exception e) {
            LogEngine.log(e.getMessage());
        }
        return projection;
    }

    /** public interface to get metadata on this object */
    public boolean getMetadata() {
        if (datasourceType == GeometryClass.PGCLASS || datasourceType == GeometryClass.ORACLASS) {
            return getTableMetadata();
        } else {
            return getFileMetadata();
        }
    }

    public Vector getSampleData(int limit) {
        int from = 0;
        int to = from + limit;
        return getSampleData(from, to);
    }

    public Vector getSampleData(int from, int to) {
        if (datasourceType == GeometryClass.PGCLASS || datasourceType == GeometryClass.ORACLASS) {
            return getTableSampledata(from, to);
        } else {
            return getFileSampledata(from, to);
        }
    }

    /**
     * creates a valid connection before calling getTableMetadata method
     */
    protected boolean getTableMetadata() {
        Connection con = null;
        boolean foundMetadata = false;
        try {
            con = ConPool.getConnection(host, dbPort, datasourceName, userName, userPwd, ConPool.DBTYPE_POSTGRES);
            if (con != null) {
                foundMetadata = getTableMetadata(con, ConPool.DBTYPE_POSTGRES);
            } else {
                con = ConPool.getConnection(host, dbPort, datasourceName, userName, userPwd, ConPool.DBTYPE_ORACLE);
                if (con != null) {
                    foundMetadata = getTableMetadata(con, ConPool.DBTYPE_ORACLE);
                } else {
                    LogEngine.log("getTableMetadata: Cannot get a connection (null) for: " + datasourceName + " " + dbPort + " " + userName + " " + userPwd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
        return foundMetadata;
    }

    /**
     * Gets some informations about the given file, like for tableMetadata:
     * columnName, columnInfo, type, numGeom, extent...
     */
    public boolean getFileMetadata() {
        if (columnName != null) {
            return true;
        }
        if (datasourceType == TIFFCLASS) {
            numGeometries = 1;
            columnName = "_tiff_file_";
            type = Geometry.RASTER;
            if (columnInfo == null) {
                // empty column information for rasters
                columnInfo = new Vector();
            }
            SRText = readProjectionFile();

            // computes tif extent based on its tfw file
            this.extent = RasterRegistrar.getTifExtent(datasourceName + fs + tableName);
            return true;
        } else if (datasourceType == ECWCLASS) {
            numGeometries = 1;
            columnName = "_ECW_file_";
            type = Geometry.RASTER;
            if (columnInfo == null) {
                // empty column information for rasters
                columnInfo = new Vector();
            }
            SRText = readProjectionFile();

            // computes ECW extent based on its tfw file
            this.extent = RasterRegistrar.getEcwExtent(datasourceName + fs + tableName);
            return true;
        } else if (datasourceType == IMGCLASS) {
            numGeometries = 1;
            columnName = "_img_file_";
            type = Geometry.RASTER;
            if (columnInfo == null) {
                // empty column information for rasters
                columnInfo = new Vector();
            }
            SRText = null;

            // computes tif extent based on its tfw file
            this.extent = RasterRegistrar.getTifExtent(datasourceName + fs + tableName);
            // should manage the SRtext based on the ERS header file
            return true;
        } else if (datasourceType == ESRIFILECLASS) {
            return getShapefileMetadata();
        }
        return false;
    }

    /**
     * Gets metadata information of a shapefile. Uses the SCOT-modified geotools.jar to
     * read file informations.
     */
    public boolean getShapefileMetadata() {
        // is filename in upper case ?
        boolean isLower = true;
        Dbf dbf = null;
        Shapefile sf = null;
        boolean ret = false;

        if (columnName != null) {
            ret = true;
        }
        try {
            String fileName = datasourceName + fs + tableName;
            int idx = fileName.lastIndexOf(".");
            if (idx != fileName.length() - 4) {
                // file have invalid extension: return
                LogEngine.log("GeometryClass.getShapefileMetadata: invalid shapefile: " + fileName);
                return false;
            } else {
                // deals with filename case by looking at the last filename letter
                // ascii code > 96 => lower case letter
                isLower = fileName.charAt(fileName.length() - 1) > 96;
                // removes extention from shapefile
                fileName = fileName.substring(0, idx);

                columnName = "shape";
                URL urlshp = new URL("file:///" + fileName.replace('\\', '/') + (isLower ? ".shp" : ".SHP"));
                URL urldbf = new URL("file:///" + fileName.replace('\\', '/') + (isLower ? ".dbf" : ".DBF"));

                // gets the SHP file
                sf = new Shapefile(urlshp);
                // gets the DBF file
                dbf = new Dbf(urldbf);

                // the geographic type:
                switch (sf.getShapeType()) {
                    case Shapefile.POLYGON:
                        type = Geometry.POLYGON;
                        break;
                    case Shapefile.ARC:
                    case Shapefile.ARC_M:
                        type = Geometry.LINESTRING;
                        break;
                    case Shapefile.POINT:
                        type = Geometry.POINT;
                        break;
                    case Shapefile.NULL:
                    case Shapefile.UNDEFINED:
                        type = Geometry.NULL;
                        break;
                }
                // the extent
                double[] ext = sf.getBounds();

                // checks if extent is not a single point
                if (ext[0] == ext[2] && ext[1] == ext[3]) {
                    // slighty increase the upper right coordinate to force a valid mapserver extent
                    ext[2] *= 1.01;
                    ext[3] *= 1.01;
                }

                extent = new Extent(ext[0], ext[1], ext[2], ext[3]);

                // the projection info
                SRText = readProjectionFile();

                // num geometries
                numGeometries = sf.getRecordCount();
                if (columnInfo == null) {
                    columnInfo = new Vector();
                }

                // attributes information, read from the DBF file, without reading all datas
                for (int i = 0; i < dbf.getNumFields(); i++) {

                    GeometryClassFieldBean as = new GeometryClassFieldBean();

                    // the field name:
                    as.setName(dbf.getFieldName(i).toString().trim());
                    // the field length
                    as.setLength(dbf.fielddef[i].fieldlen);

                    switch (dbf.getFieldType(i)) {
                        case 'C':
                        case 'c':
                        case 'D':
                        case 'L':
                        case 'M':
                        case 'G':
                            as.setType(GeometryClassFieldBean.TYPE_STRING);
                            break;
                        case 'N':
                        case 'n':
                            as.setType(GeometryClassFieldBean.TYPE_NUMERIC);
                            break;
                        case 'F':
                        case 'f':
                            as.setType(GeometryClassFieldBean.TYPE_FLOAT);
                            break;
                        default:
                            as.setType(GeometryClassFieldBean.TYPE_INVALID);
                    }
                    // DBF fields are nullable
                    as.setNullable("Nullable");
                    columnInfo.add(as);
                }
                ret = true;
            }
        } catch (Exception e) {
            // either a bad URL or another exception
            e.printStackTrace();
            return false;
        } finally {
            // try to release geotools objects to empty memory
            sf = null;
            dbf = null;
            System.gc();
        }
        return ret;
    }

    /**
     * Gets the DB name of the columns of the table representing this geometry Class.
     * DO NOT CLOSE THE CONNECTION HERE; AS IT IS USED BY THE CALLING METHOD
     */
    public boolean getTableMetadata(Connection con, String type) {
        boolean foundMetaData = false;
        if (type.equalsIgnoreCase(ConPool.DBTYPE_POSTGRES)) {
            foundMetaData = getTableMetadataPg(con);
        }
        if (type.equalsIgnoreCase(ConPool.DBTYPE_ORACLE)) {
            foundMetaData = getTableMetadataOra(con);
        }
        return foundMetaData;
    }

    /**
     * Gets the DB name of the columns of the table representing this geometry Class.
     * DO NOT CLOSE THE CONNECTION HERE; AS IT IS USED BY THE CALLING METHOD
     */
    public boolean getTableMetadataPg(Connection con) {

        Statement stmt = null;
        ResultSet rs = null;
        boolean ret = false;

        if (columnName != null) {
            //job already done
            ret = true;
        } else {
            try {
                stmt = con.createStatement();

                // gets the specific geometric column type
                StringBuffer query = new StringBuffer("select f_geometry_column,type from geometry_columns");
                query.append(" where lower(f_table_name)='").append(tableName.toLowerCase()).append("'");
                String dbtype = "";
                rs = stmt.executeQuery(query.toString());

                if (rs.next()) {
                    // for the moment, only one geo column in a table
                    columnName = rs.getString(1);
                    dbtype = rs.getString(2);
                }

                if (columnName == null) {
                    errorMessage = "getColumnName: cannot get geometry_column for table: " + tableName;
                    errorMessage += ".\n<br>Is the table is registered in metadata (call to addGeometryColumn)";
                } else {
                    // gets metadata for the table
                    DatabaseMetaData dbm = con.getMetaData();

                    dbVersion = dbm.getDatabaseMajorVersion();

                    rs = dbm.getIndexInfo(null, null, tableName, true, false);
                    if (rs.next()) {
                        uniqueField = rs.getString("COLUMN_NAME");
                    }

                    if (columnInfo == null) {
                        columnInfo = new Vector();
                    }

                    rs = dbm.getColumns(null, null, tableName, "%");

                    while (rs.next()) {
                        // skip geo column
                        if (rs.getString(4).equals(columnName)) {
                            continue;
                        }
                        // nullable field:
                        String nullable = rs.getInt(11) == DatabaseMetaData.columnNullable ? "Nullable" : "Not Null";
                        GeometryClassFieldBean as = new GeometryClassFieldBean(rs.getString(4), rs.getString(6), rs.getInt(7), nullable);
                        columnInfo.add(as);
                    }
                    // set the type
                    if (dbtype.equals("POINT")) {
                        type = Geometry.POINT;
                    } else if (dbtype.equals("LINESTRING")) {
                        type = Geometry.LINESTRING;
                    } else if (dbtype.equals("POLYGON")) {
                        type = Geometry.POLYGON;
                    } else if (dbtype.equals("MULTIPOLYGON")) {
                        type = Geometry.MULTIPOLYGON;
                    } else if (dbtype.equals("MULTILINESTRING")) {
                        type = Geometry.MULTILINESTRING;
                    } else if (dbtype.equals("MULTIPOINT")) {
                        type = Geometry.MULTIPOINT;
                    } else if (dbtype.equals("GEOMETRY")) {
                        type = Geometry.GEOMETRY;
                    } else {
                        // other OGIS types not yet supported
                        this.type = Geometry.NULL;
                    }
                    //gets the number of geometries in this table
                    query = new StringBuffer("select count(*) from ");
                    query.append(tableName);
                    query.append(" where ").append(columnName).append(" is not null");
                    rs = stmt.executeQuery(query.toString());
                    rs.next();
                    numGeometries = rs.getInt(1);

                    // this query gives the SRTEXT and SRID of the geo table:
                    query = new StringBuffer("select SRTEXT, spatial_ref_sys.SRID from spatial_ref_sys, geometry_columns ");
                    query.append("where f_table_name='").append(tableName).append("' and geometry_columns.srid=spatial_ref_sys.srid");
                    rs = stmt.executeQuery(query.toString());
                    if (rs.next()) {
                        SRText = rs.getString(1);
                        SRID = rs.getInt(2);
                    }

                    // this query gives the geodata extent, based on the OpenGIS "envelope" method
                    if (numGeometries > 0) {
                        query = new StringBuffer("select min(x(pointN(exteriorRing(envelope(");
                        query.append(columnName);
                        query.append(")), 1))),min(y(pointN(exteriorRing(envelope(");
                        query.append(columnName);
                        query.append(")), 1))),max(x(pointN(exteriorRing(envelope(");
                        query.append(columnName);
                        query.append(")), 3))),max(y(pointN(exteriorRing(envelope(");
                        query.append(columnName);
                        query.append(")), 3))) from ");
                        query.append(tableName);

                        rs = stmt.executeQuery(query.toString());
                        if (rs.next()) {
                            extent = new Extent(rs.getDouble(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4));
                        }
                    }
                }

                ret = true;

            } catch (SQLException sqle) {
                errorMessage = "getTableMetadata: SQLException: " + sqle.getMessage();
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

    /**
     * Gets the DB name of the columns of the table representing this geometry Class.
     * DO NOT CLOSE THE CONNECTION HERE; AS IT IS USED BY THE CALLING METHOD
     */
    public boolean getTableMetadataOra(Connection con) {
        if (columnName != null) {
            //job already done
            return true;
        }

        ResultSet rs = null;
        Statement stmt = null;
        boolean ret = false;

        try {
            stmt = con.createStatement();

            // gets the specific geometric column type
            StringBuffer query = new StringBuffer("select COLUMN_NAME from MDSYS.USER_SDO_GEOM_METADATA");
            query.append(" where TABLE_NAME='").append(tableName).append("'");
            rs = stmt.executeQuery(query.toString());

            if (rs.next()) {
                // for the moment, only one geo column in a table
                columnName = rs.getString(1);
            }
            if (columnName == null) {
                errorMessage = "getColumnName: cannot get geometry_column for table: " + tableName;
                errorMessage += ".\n<br>Is the table is registered in metadata (call to addGeometryColumn)";
            } else {

                query = new StringBuffer("SELECT a.").append(columnName).append(".GET_GTYPE() FROM ").append(tableName).append(" a where rownum=1");
                rs = stmt.executeQuery(query.toString());
                rs.next();
                int dbtype = rs.getInt(1);

                // set the type
                if (dbtype == ORA_POINT) {
                    type = Geometry.POINT;
                } else if (dbtype == ORA_MULTIPOINT) {
                    type = Geometry.MULTIPOINT;
                } else if (dbtype == ORA_LINE) {
                    type = Geometry.LINESTRING;
                } else if (dbtype == ORA_MULTILINE) {
                    type = Geometry.MULTILINESTRING;
                } else if (dbtype == ORA_POLYGON) {
                    type = Geometry.POLYGON;
                } else if (dbtype == ORA_MULTIPOLYGON) {
                    type = Geometry.MULTIPOLYGON;
                } else {
                    this.type = Geometry.NULL;
                }

                // gets metadata for the table
                DatabaseMetaData dbm = con.getMetaData();

                rs = dbm.getColumns(null, null, tableName, "%");

                dbVersion = dbm.getDriverMajorVersion();

                if (columnInfo == null) {
                    columnInfo = new Vector();
                }
                while (rs.next()) {
                    // skip geo column
                    if (rs.getString(4).equals(columnName)) {
                        continue;
                    }
                    // nullable field:
                    String nullable = rs.getInt(11) == DatabaseMetaData.columnNullable ? "Nullable" : "Not Null";
                    GeometryClassFieldBean as = new GeometryClassFieldBean(rs.getString(4), rs.getString(6), rs.getInt(7), nullable);
                    columnInfo.add(as);
                }

                //gets the number of geometries in this table
                query = new StringBuffer("select count(*) from ");
                query.append(tableName);
                query.append(" where ").append(columnName).append(" is not null");
                rs = stmt.executeQuery(query.toString());
                rs.next();
                numGeometries = rs.getInt(1);

                // this query gives the SRTEXT and SRID of the geo table:
                query = new StringBuffer("select WKTEXT, MDSYS.CS_SRS.SRID from MDSYS.CS_SRS, USER_SDO_GEOM_METADATA ");
                query.append("where TABLE_NAME='").append(tableName).append("' and  MDSYS.CS_SRS.SRID=USER_SDO_GEOM_METADATA.SRID");
                rs = stmt.executeQuery(query.toString());
                if (rs.next()) {
                    SRText = rs.getString(1);
                    SRID = rs.getInt(2);
                }

                // this query gives the geodata extent, based on the OpenGIS "envelope" method
                if (numGeometries > 0) {
                    query = new StringBuffer("SELECT * FROM TABLE (CAST((SELECT DIMINFO FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='");
                    query.append(tableName);
                    query.append("') AS MDSYS.SDO_DIM_ARRAY)) LISTE");
                    rs = stmt.executeQuery(query.toString());
                    double xmin = 0.0;
                    double ymin = 0.0;
                    double xmax = 0.0;
                    double ymax = 0.0;
                    //X values
                    rs.next();
                    xmin = rs.getDouble("SDO_LB");
                    xmax = rs.getDouble("SDO_UB");

                    //Y values
                    rs.next();
                    ymin = rs.getDouble("SDO_LB");
                    ymax = rs.getDouble("SDO_UB");

                    extent = new Extent(xmin, ymin, xmax, ymax);
                }

                rs = dbm.getPrimaryKeys(null, null, tableName);
                while (rs.next() && (uniqueField == null || uniqueField.equalsIgnoreCase("null"))) {
                    /*
                    String s1 = rs.getString(1);
                    String s2 = rs.getString(2);
                    String s3 = rs.getString(3);
                    String s4 = rs.getString(4);
                     */
                    uniqueField = rs.getString(4);
                }
                rs.close();

                ret = true;
            }
        } catch (SQLException sqle) {
            errorMessage = "getTableMetadata: SQLException: " + sqle.getMessage();
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

    /**
     * Return version of database (can be null if datasource is not a DBMS)
     */
    public int getDbVersion() {
        return dbVersion;
    }

    /**
     * Null param Layer constructor in case no info is given (easier for Struts)
     */
    public Layer getNullMsLayer() {
        return getMSLayer(null, false);
    }

    /**
     * Default param Layer constructor in case no info is given (easier for Struts)
     */
    public Layer getDefaultMsLayer() {
        return getMSLayer(new RGB(0, 0, 0), false);
    }

    /**
     * build the MapServerLayer representation of this GeometryClass
     *@param: color: the RGB object for MS object color
     *        force true to indicate that the MSLayer should be reconstructed from scratch with
     *              the given color. Useful to build quickview when a Layer already have some display properties
     */
    public Layer getMSLayer(RGB color, boolean force) {
        if (msLayer != null && !force) {
            return msLayer;
        }
        // should construct it
        msLayer = new Layer();
        msLayer.setName(name);
        // Data specific layer properties
        if (datasourceType == GeometryClass.PGCLASS) {
            msLayer.setConnection("dbname=" + datasourceName + " host=" + host + " port=" + dbPort + " user=" + userName + " password=" + userPwd);
            msLayer.setConnectionType(Layer.POSTGIS);
            msLayer.setData(columnName + " from " + name + " USING UNIQUE " + uniqueField + (SRID<=0 ? "" :" USING SRID=" + SRID));
        } else if (datasourceType == GeometryClass.ORACLASS) {
            //user/pwd@MYDB
            msLayer.setConnection(userName + "/" + userPwd + "@" + datasourceName);
            msLayer.setConnectionType(Layer.ORACLESPATIAL);
            msLayer.setData(columnName + " from " + name + " USING UNIQUE " + uniqueField + " USING SRID=" + SRID);
        } else {
            // other connectiontype, or nothing if file
            msLayer.setData(datasourceName + fs + tableName);
        }

        // a default display class for this geoobject
        MapClass c = new MapClass();
        // sets the name to the theme name, by default, without extension
        if (tableName.lastIndexOf(".") > -1) {
            c.setName(tableName.substring(0, tableName.lastIndexOf(".")));
        } else {
            c.setName(tableName);
        }

        msLayer.setStatus(Layer.ON);

        if (type == Geometry.POINT || type == Geometry.MULTIPOINT) {
            msLayer.setType(Layer.POINT);
            c.setColor(color);
            c.setSymbol("gasdefsympoint");
            c.setSize(5);
        } else if (type == Geometry.POLYGON || type == Geometry.MULTIPOLYGON) {
            msLayer.setType(Layer.POLYGON);
            c.setColor(color);
            c.setOutlineColor(new RGB(0, 0, 0));
        } else if (type == Geometry.RASTER) {
            msLayer.setType(Layer.RASTER);
        } else {
            // default value for all other types, including linestring
            msLayer.setType(Layer.LINE);
            c.setColor(color);
        }
        if (type != Geometry.RASTER) {
            // no classes for rasters
            msLayer.addClass(c);
        }
        // default all layers to be transparent (50%)
        msLayer.setTransparency(MS_LAYER_TRANSPARENCY);
        // the projection part for this layer, if any
        /*
        if (SRText != null) {
        // parses the GAS internal proj4 format to extract valid set of parameters for the mapfile
        // in case of DB source, should provide a mechanism to convert from OpenGIS SRText and Proj4 parameters,
        // if proj4text column is not present in the spatial_ref_sys table
        Projection prj = new Projection();
        String prjParams = SRText.substring(SRText.indexOf("|") + 1, SRText.lastIndexOf("<"));
        // example of proj4 params as stored in the spatial_ref_sys postgis table is:
        // +proj=utm +zone=56 +south +ellps=WGS84 +datum=WGS84 +units=m  no_defs
        prjParams = prjParams.replace('+', ' ');
        StringTokenizer tk = new StringTokenizer(prjParams);
        while (tk.hasMoreElements()) {
        prj.addAttribute("\"" + tk.nextToken() + "\"");
        }
        msLayer.setProjection(prj);
        }
         */
        return msLayer;
    }

    /**
     * Sets the identifier for the new geometry to be created.
     * Should extend the mecanism to allow several new objects to be defined
     *
     * @param id the unique identifier for the geometry to be created in Kaboum
     * @param declaredArea the declared area for the edited geometry, if it is relevant:
     * geometry is a polygon. The area control, if asked, will be done based on this value.
     */
    public void setEditedGeometryID(String id, double declaredArea) {
        editedGeometryID = id;
        this.declaredArea = declaredArea;
    }

    /**
     * Returns the Kaboum applet parameters corresponding to this GeometryClass
     * @param jsObject the javascript themeManager object's name on which the setAppletParameter
     * method will be applied.
     */
    public String getKaboumParameters(String jsObject) {
        StringBuffer list = new StringBuffer();

        if (isActive) {
            // the active class
            list.append("<param name=\"OBJECT_ACTIVE_CLASS\" value=\"");
            list.append(id).append("\">").append(ls);
            // same for kaboumv2 version: should remove this as soon as kaboum V3 will work
            list.append("<param name=\"ACTIVE_OBJECT_CLASS\" value=\"");
            list.append(id).append("\">").append(ls);
        }
        // the class type
        list.append("<param name=\"").append(id).append("_CLASS_TYPE\" value=\"");
        list.append(this.getKaboumType()).append("\">").append(ls);
        // classes properties
        list.append("<param name=\"").append(id).append("_PROPERTIES_IS_LOCKED\" value=\"");
        list.append(isLocked).append("\">").append(ls);
        list.append("<param name=\"").append(id).append("_PROPERTIES_IS_COMPUTED\" value=\"");
        list.append(isComputed).append("\">").append(ls);
        list.append("<param name=\"").append(id).append("_PROPERTIES_IS_SURROUNDING\" value=\"");
        list.append(isSurrounding).append("\">").append(ls);
        list.append("<param name=\"").append(id).append("_PROPERTIES_IS_VISIBLE\" value=\"");
        list.append(isVisible).append("\">").append(ls);

        // ALL the graphical properties:
        list.append("<param name=\"").append(id).append("_DD_IS_FILLED\" value=\"");
        list.append(isFilled).append("\">").append(ls);

        if (fillColor != null) {
            list.append("<param name=\"").append(id).append("_DD_COLOR\" value=\"");
            list.append(fillColor.getRed()).append(",").append(fillColor.getGreen());
            list.append(",").append(fillColor.getBlue()).append("\">").append(ls);
        }
        if (hiliteColor != null) {
            list.append("<param name=\"").append(id).append("_DD_HILITE_COLOR\" value=\"");
            list.append(hiliteColor.getRed()).append(",").append(hiliteColor.getGreen());
            list.append(",").append(hiliteColor.getBlue()).append("\">").append(ls);
        }
        if (modifiedColor != null) {
            list.append("<param name=\"").append(id).append("_DD_MODIFIED_COLOR\" value=\"");
            list.append(modifiedColor.getRed()).append(",").append(modifiedColor.getGreen());
            list.append(",").append(modifiedColor.getBlue()).append("\">").append(ls);
        }
        if (pointColor != null) {
            list.append("<param name=\"").append(id).append("_DD_POINT_COLOR\" value=\"");
            list.append(pointColor.getRed()).append(",").append(pointColor.getGreen());
            list.append(",").append(pointColor.getBlue()).append("\">").append(ls);
        }
        if (pointHiliteColor != null) {
            list.append("<param name=\"").append(id).append("_DD_POINT_HILITE_COLOR\" value=\"");
            list.append(pointHiliteColor.getRed()).append(",").append(pointHiliteColor.getGreen());
            list.append(",").append(pointHiliteColor.getBlue()).append("\">").append(ls);
        }
        if (pointForbiddenColor != null) {
            list.append("<param name=\"").append(id).append("_DD_POINT_FORBIDDEN_COLOR_COLOR\" value=\"");
            list.append(pointForbiddenColor.getRed()).append(",").append(pointForbiddenColor.getGreen());
            list.append(",").append(pointForbiddenColor.getBlue()).append("\">").append(ls);
        }
        if (pointHeight != 0) {
            list.append("<param name=\"").append(id).append("_DD_POINT_HEIGHT\" value=\"");
            list.append(pointHeight).append("\">").append(ls);
        }
        if (pointWidth != 0) {
            list.append("<param name=\"").append(id).append("_DD_POINT_WIDTH\" value=\"");
            list.append(pointWidth).append("\">").append(ls);
        }
        if (pointImageURL != null) {
            list.append("<param name=\"").append(id).append("_DD_POINT_IMAGE\" value=\"");
            list.append(pointImageURL).append("\">").append(ls);
        }
        if (pointType != 0) {
            String t = "K_TYPE_BOX";
            switch (this.pointType) {
                case K_TYPE_BOX:
                    t = "K_TYPE_BOX";
                    break;
                case K_TYPE_CIRCLE:
                    t = "K_TYPE_CIRCLE";
                    break;
                case K_TYPE_IMAGE:
                    t = "K_TYPE_IMAGE";
                    break;
                case K_TYPE_POINT:
                    t = "K_TYPE_POINT";
                    break;
            }
            list.append("<param name=\"").append(id).append("_DD_POINT_TYPE\" value=\"");
            list.append(t).append("\">").append(ls);
        }

        return list.toString();
    }

    /**
     * returns a string containing the kaboum command to load all this object's geometries
     *@param jsFunction: the javascript function reference used to build the full command. Ex:
     *      if passing "document.kaboum.kaboumCommand" as jsFunction, the resulting string would be:
     *      "document.kaboum.kaboumCommand("<kaboum command>");
     */
    public String getKaboumObjectsString(String jsFunction) {
        if (!displayInKaboum || geometries == null) {
            // not elligible for kaboum, or getGeometriesFromDB not called
            return "";
        }
        StringBuffer res = new StringBuffer();

        for (Iterator iter = geometries.iterator(); iter.hasNext();) {
            res.append(jsFunction).append("(\"");
            res.append(((Geometry) iter.next()).toKaboumString());
            res.append("\");\n");
        }

        return res.toString();
    }

    /**
     * returns a string containing the kaboum command to remove all this object's geometries from applet
     *@param jsFunction: the javascript function reference used to build the full command. Ex:
     *      if passing "document.kaboum.kaboumCommand" as jsFunction, the resulting string would be:
     *      "document.kaboum.kaboumCommand("<kaboum command>");
     */
    public String getKaboumRemoveObjectsString(String jsFunction) {
        if (!displayInKaboum || geometries == null) {
            // not elligible for kaboum, or getGeometriesFromDB not called
            return "";
        }
        StringBuffer res = new StringBuffer();

        for (Iterator iter = geometries.iterator(); iter.hasNext();) {
            res.append(jsFunction).append("(\"");
            res.append(((Geometry) iter.next()).toKaboumRemoveString());
            res.append("\");\n");
        }

        return res.toString();
    }

    /**
     * Gives information to this GeometryClass used when inserting a new geometry into
     * the DB: the insert SQL command could need some extra values
     * (foreign keys on other tables for instance)
     * @param columns a String array containing the name of DB columns to add to the insert command
     * @param values a String array containing the values for each column to add to the insert command.
     * @return true if columns and values have the same size, false otherwise.
     *
     * Note: indices in "columns" must match indices in "values".
     * Ex: The zonage DB table contains a FK on the parcelle table.
     * This DB was mapped to a zonage GeometryClass.
     * When inserting a new zonage, one wants to maintain the relation between zonage and parcelle,
     * based on the parc_id foreign key on the table zonage.
     * columns would contain one entry: "parc_id"
     * values would contain one entry: 3
     * the generated insert query for a newly created zonage
     * insert into zonage (geo_column, parc_id) values (<geo_value>, 3)
     *
     * Be sure to enclose textual values into single quotes (postgres std).<br>
     * It is up to the caller to make sure single quotes are correctly escaped if a value
     * is textual and contains a single quote.
     * use com.scot.tools.string.ConversionUtilities.escapeSingleQuotes(String s) to do that.
     */
    public boolean setDbInsertInforamtion(String[] columns, String[] values) {
        if (columns.length != values.length) {
            errorMessage = "Arrays length don't match";
            return false;
        }
        insertColumns = columns;
        insertValues = values;
        return true;
    }

    /**
     * Returns the Kaboum actionPerformed methods for each geometry in this geometryClass
     * @param jsObject; the name of the js Toolbar object on which the actionPerformed method will
     * be called:
     * ex: tb.actionPerformed("OBJECT|CLASS|id|coords")
     */
    public String toKaboumString(String jsObject) {
        StringBuffer buf = new StringBuffer();

        for (Iterator iter = geometries.iterator(); iter.hasNext();) {
            buf.append(jsObject).append(".actionPerformed(\"");
            buf.append(((Geometry) iter.next()).toKaboumString());
            buf.append("\">").append(ls);
        }
        return buf.toString();
    }

    /** true if one graphic parameter is set: SIGUserParameters can generate kaboum list of parameters */
    public boolean isDDSet() {
        if (fillColor != null ||
                hiliteColor != null ||
                modifiedColor != null ||
                pointColor != null ||
                pointHiliteColor != null ||
                pointForbiddenColor != null ||
                pointImageURL != null ||
                pointHeight != 0 ||
                pointWidth != 0 ||
                pointType != 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns the geometry whose id equals the given one.
     * @param id the geometry's id to get
     * @return the geometry whose id is equals to the given one, null otherwise.
     */
    public Geometry getGeometry(String gid) {
        for (Iterator iter = geometries.iterator(); iter.hasNext();) {
            Geometry g = (Geometry) iter.next();
            if (g.id.equals(gid)) {
                return g;
            }
        }
        return null;
    }

    /**
     * gets this geo type as an OpenGIS String type,
     * Ogis types are Geometry class constants
     */
    public String getOgisType() {
        switch (type) {
            case Geometry.POINT:
                return "POINT";
            case Geometry.GEOMETRY:
                return "GEOMETRY";
            case Geometry.LINESTRING:
                return "LINESTRING";
            case Geometry.POLYGON:
                return "POLYGON";
            case Geometry.RASTER:
                return "RASTER";
            case Geometry.MULTIPOLYGON:
                return "MULTIPOLYGON";
            case Geometry.MULTIPOINT:
                return "MULTIPOINT";
            case Geometry.MULTILINESTRING:
                return "MULTILINESTRING";
            case Geometry.GEOMETRYCOLLECTION:
                return "GEOMETRYCOLLECTION";
        }
        return "Type invalide";
    }

    /**
     * Sets this geo type according to the given OpenGIS String type,
     * Ogis types are Geometry class constants
     */
    public void setOgisType(String ogisType) {
        if (ogisType.equalsIgnoreCase("geometry")) {
            this.type = Geometry.GEOMETRY;
        } else if (ogisType.equalsIgnoreCase("geometrycollection")) {
            this.type = Geometry.GEOMETRYCOLLECTION;
        } else if (ogisType.equalsIgnoreCase("point")) {
            this.type = Geometry.POINT;
        } else if (ogisType.equalsIgnoreCase("multipoint")) {
            this.type = Geometry.MULTIPOINT;
        } else if (ogisType.equalsIgnoreCase("polygon")) {
            this.type = Geometry.POLYGON;
        } else if (ogisType.equalsIgnoreCase("multipolygon")) {
            this.type = Geometry.MULTIPOLYGON;
        } else if (ogisType.equalsIgnoreCase("linestring")) {
            this.type = Geometry.LINESTRING;
        } else if (ogisType.equalsIgnoreCase("multilinestring")) {
            this.type = Geometry.MULTILINESTRING;
        }
    }

    /**MISSING JAVADOC !*/
    public String getDatasourceTypeAsString() {
        if (datasourceType == PGCLASS) {
            return STRING_PGCLASS;
        } else if (datasourceType == ORACLASS) {
            return STRING_ORACLASS;
        } else if (datasourceType == ESRIFILECLASS) {
            return STRING_ESRIFILECLASS;
        } else if (datasourceType == TIFFCLASS) {
            return STRING_TIFFCLASS;
        } else if (datasourceType == ECWCLASS) {
            return STRING_ECWCLASS;
        } else if (datasourceType == IMGCLASS) {
            return STRING_IMGCLASS;
        }
        return "_";
    }

    /**
     * Returns true if this GeometryClass is eligible to be edited by Kaboum:
     * For the moment, only PGCLASS GC can be editable.
     * the isEdited field tells if an editable GC (in absolute) was specifically set
     * editable by the user (defautl is false)
     */
    public boolean isEditable() {
        return (this.datasourceType == PGCLASS);
    }

////////////////////////////////////////////////////////////////////////////////
// Graphic representation methods
////////////////////////////////////////////////////////////////////////////////
    /**MISSING JAVADOC !*/
    public void setFilled(boolean isfilled) {
        isFilled = isfilled;
    }

    /**MISSING JAVADOC !*/
    public void setFillColor(Color col) {
        fillColor = col;
    }

    /**MISSING JAVADOC !*/
    public void setHiliteColor(Color col) {
        hiliteColor = col;
    }

    /**MISSING JAVADOC !*/
    public void setModifiedColor(Color col) {
        modifiedColor = col;
    }

    /**MISSING JAVADOC !*/
    public void setPointType(int pt) {
        pointType = pt;
    }

    /**MISSING JAVADOC !*/
    public void setPointHeight(int x) {
        pointHeight = x;
    }

    /**MISSING JAVADOC !*/
    public void setPointWidth(int x) {
        pointWidth = x;
    }

    /**MISSING JAVADOC !*/
    public void setPointColor(Color col) {
        pointColor = col;
    }

    /**MISSING JAVADOC !*/
    public void setPointHiliteColor(Color col) {
        pointHiliteColor = col;
    }

    /**MISSING JAVADOC !*/
    public void setPointForbiddenColor(Color col) {
        pointForbiddenColor = col;
    }

    /**MISSING JAVADOC !*/
    public void setPointImageURL(String s) {
        pointImageURL = s;
    }

    /**MISSING JAVADOC !*/
    public void setName(String name) {
        this.name = name;
    }

    /**MISSING JAVADOC !*/
    public void setTableName(String tablename) {
        this.tableName = tablename;
    }

    /**MISSING JAVADOC !*/
    public void setColumnName(String colname) {
        this.columnName = colname;
    }

    /**MISSING JAVADOC !*/
    public void setDatasourceName(String dsname) {
        this.datasourceName = dsname;
    }

    /**MISSING JAVADOC !*/
    public void setType(int type) {
        this.type = type;
    }

    /**MISSING JAVADOC !*/
    public void setDatasourceType(byte dstype) {
        this.datasourceType = dstype;
    }

    /**MISSING JAVADOC !*/
    public void setMSLayer(Layer l) {
        this.msLayer = l;
    }

    /** allows to force a declaredArea for the edited geometry .
     * Must change that to set the declared area at the geometry level, not geometryClass level.
     */
    public void setDeclaredArea(double da) {
        this.declaredArea = da;
    }

    /**
     * Sets a new projection string for this GC, also updates the MSLayer object to reflect the change.
     */
    public void setSRText(String srtext) {
        this.SRText = srtext;
        if (msLayer == null) {
            getMSLayer(new RGB(255, 0, 0), true);
        } else {
            getMSLayer(((MapClass) (msLayer.getMapClass().getFirstClass())).getColor(), true);
        }
    }

//--------------------------------------//
//----------- GETTER METHODS -----------//
//--------------------------------------//
    /**MISSING JAVADOC !*/
    public String getName() {
        return this.name;
    }

    /**MISSING JAVADOC !*/
    public String getTableName() {
        return this.tableName;
    }

    /**MISSING JAVADOC !*/
    public String getColumnName() {
        return this.columnName;
    }

    /**MISSING JAVADOC !*/
    public String getDBPort() {
        return this.dbPort;
    }

    /**MISSING JAVADOC !*/
    public String getUserName() {
        return this.userName;
    }

    /**MISSING JAVADOC !*/
    public String getUserPwd() {
        return this.userPwd;
    }

    /**MISSING JAVADOC !*/
    public String getHost() {
        return this.host;
    }

    /**MISSING JAVADOC !*/
    public String getDatasourceName() {
        return this.datasourceName;
    }

    /**MISSING JAVADOC !*/
    public String getSRText() {
        return this.SRText;
    }

    /**MISSING JAVADOC !*/
    public int getSRID() {
        return this.SRID;
    }

    /**MISSING JAVADOC !*/
    public Vector getSelectedGeometries() {
        return this.selectedGeometries;
    }

    /**MISSING JAVADOC !*/
    public int getType() {
        return this.type;
    }

    /**MISSING JAVADOC !*/
    public byte getDatasourceType() {
        return this.datasourceType;
    }

    /**MISSING JAVADOC !*/
    public Vector getGeometries() {
        return this.geometries;
    }

    /**MISSING JAVADOC !*/
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**MISSING JAVADOC !*/
    public String getEditedGeometryID() {
        return this.editedGeometryID;
    }

    /**MISSING JAVADOC !*/
    public Extent getExtent() {
        return this.extent;
    }

    /**MISSING JAVADOC !*/
    public double getDeclaredArea() {
        return this.declaredArea;
    }

    /**MISSING JAVADOC !*/
    public String getID() {
        return this.id;
    }

    /**MISSING JAVADOC !*/
    public String getIDColumn() {
        return idColumn;
    }

// kaboum display parameters
    /**MISSING JAVADOC !*/
    public boolean isFilled() {
        return isFilled;
    }

    /**MISSING JAVADOC !*/
    public String getFillColor() {
        return fillColor.getRed() + "," + fillColor.getGreen() + "," + fillColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getHiliteColor() {
        return hiliteColor.getRed() + "," + hiliteColor.getGreen() + "," + hiliteColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getModifiedColor() {
        return modifiedColor.getRed() + "," + modifiedColor.getGreen() + "," + modifiedColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getCCFillColor() {
        return fillColor.getRed() + " " + fillColor.getGreen() + " " + fillColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getCCHiliteColor() {
        return hiliteColor.getRed() + " " + hiliteColor.getGreen() + " " + hiliteColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getCCModifiedColor() {
        return modifiedColor.getRed() + " " + modifiedColor.getGreen() + " " + modifiedColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public int getPointHeight() {
        return pointHeight;
    }

    /**MISSING JAVADOC !*/
    public int getPointWidth() {
        return pointWidth;
    }

    /**MISSING JAVADOC !*/
    public String getPointColor() {
        return pointColor.getRed() + "," + pointColor.getGreen() + "," + pointColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getPointHiliteColor() {
        return pointHiliteColor.getRed() + "," + pointHiliteColor.getGreen() + "," + pointHiliteColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getPointForbiddenColor() {
        return pointForbiddenColor.getRed() + "," + pointForbiddenColor.getGreen() + "," + pointForbiddenColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getCCPointColor() {
        return pointColor.getRed() + " " + pointColor.getGreen() + " " + pointColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getCCPointHiliteColor() {
        return pointHiliteColor.getRed() + " " + pointHiliteColor.getGreen() + " " + pointHiliteColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getCCPointForbiddenColor() {
        return pointForbiddenColor.getRed() + " " + pointForbiddenColor.getGreen() + " " + pointForbiddenColor.getBlue();
    }

    /**MISSING JAVADOC !*/
    public String getPointImageURL() {
        return pointImageURL;
    }

    /**MISSING JAVADOC !*/
    public int getPointType() {
        return pointType;
    }

    /**MISSING JAVADOC !*/
    public Vector getColumnInfo() {
        return columnInfo;
    }

    /**MISSING JAVADOC !*/
    public Vector getColumnValues() {
        return columnValues;
    }

    /**MISSING JAVADOC !*/
    public Vector getColumnNamesInfo() {
        Vector columnNames = new Vector();
        for (Iterator iteInfo = columnInfo.iterator(); iteInfo.hasNext();) {
            columnNames.add(((GeometryClassFieldBean) iteInfo.next()).getName());
        }
        return columnNames;
    }

    /**MISSING JAVADOC !*/
    public int getNumGeometries() {
        return numGeometries;
    }

    /**MISSING JAVADOC !*/
    public String getWhereClause() {
        return whereClause == null ? "" : whereClause;
    }

    /**MISSING JAVADOC !*/
    public Boolean getVisible() {
        return new Boolean(isVisible);
    }

    /**MISSING JAVADOC !*/
    public Boolean getActive() {
        return new Boolean(isActive);
    }

    /**MISSING JAVADOC !*/
    public Boolean getFilled() {
        return new Boolean(isFilled);
    }

    /**MISSING JAVADOC !*/
    public Boolean getComputed() {
        return new Boolean(isComputed);
    }

    /**MISSING JAVADOC !*/
    public Boolean getSurrounding() {
        return new Boolean(isSurrounding);
    }

    /**MISSING JAVADOC !*/
    public Boolean getLocked() {
        return new Boolean(isLocked);
    }

    /**MISSING JAVADOC !*/
    public String getPointTypeAsKaboumString() {
        switch (pointType) {
            case K_TYPE_BOX:
                return "K_TYPE_BOX";
            case K_TYPE_POINT:
                return "K_TYPE_POINT";
            case K_TYPE_CIRCLE:
                return "K_TYPE_CIRCLE";
            case K_TYPE_IMAGE:
                return "K_TYPE_IMAGE";
            default:
                return "";
        }
    }

    /**MISSING JAVADOC !*/
    public Boolean getDisplayInKaboum() {
        return new Boolean(this.displayInKaboum);
    }

    /**MISSING JAVADOC !*/
    public Boolean getDisplayInMapserver() {
        return new Boolean(this.displayInMapserver);
    }

    /**
     * Return unique field name (can be null if datasource is not a DBMS)
     */
    public String getUniqueField() {
        return uniqueField.equalsIgnoreCase("null") ? null : uniqueField;
    }

    /**
     *Implements the Comparable::compareTo() method.
     *the comparison is based on tableName
     *
     */
    public int compareTo(Object o) {
        if (!(o instanceof GeometryClass) || this.tableName == null) {
            return -1;
        }
        return this.tableName.compareTo(((GeometryClass) o).tableName);
    }

    public Vector getFileSampledata(int from, int to) {
        if (datasourceType == ESRIFILECLASS) {
            return getShapefileSampledata(from, to);
        }
        return null;
    }

    /**
     * Gets sample data from shapefile attributes.
     */
    public Vector getShapefileSampledata(int from, int to) {
        // is filename in upper case ?
        boolean isLower = true;
        try {
            String fileName = datasourceName + fs + tableName;
            int idx = fileName.lastIndexOf(".");
            if (idx != fileName.length() - 4) {
                // file have invalid extension: return
                LogEngine.log("GeometryClass.getShapefileMetadata: invalid shapefile: " + fileName);
                return null;
            }
            // deals with filename case by looking at the last filename letter
            // ascii code > 96 => lower case letter
            isLower = fileName.charAt(fileName.length() - 1) > 96;
            // removes extention from shapefile
            fileName = fileName.substring(0, idx);

            columnName = "shape";
            URL url = null;

            columnValues = new Vector();

            // attributes information, read from the DBF file, without reading all datas
            // builds the url for DBF file
            url = new URL("file:///" + fileName.replace('\\', '/') + (isLower ? ".dbf" : "DBF"));
            Dbf dbf = new Dbf(url);

            Vector currentRecord = null;
            int limit = (to < numGeometries) ? (to + 1) : numGeometries;
            for (int j = from; j < limit; j++) {
                currentRecord = dbf.ParseDbfRecord(j);
                if (columnValues == null) {
                    columnValues = new Vector();
                }
                columnValues.add(currentRecord);
            }
            // try to release geotools object to empty memory
            dbf = null;
            System.gc();
            return columnValues;
        } catch (Exception e) {
            // either a bad URL or another exception
            e.printStackTrace();
            return null;
        }
    }

    /**
     * creates a valid connection before calling getTableSampledata method
     */
    protected Vector getTableSampledata(int from, int to) {
        Connection con = null;
        Vector foundMetadata = null;
        try {
            con = ConPool.getConnection(host, dbPort, datasourceName, userName, userPwd, ConPool.DBTYPE_POSTGRES);
            if (con != null) {
                foundMetadata = getTableSampledata(con, ConPool.DBTYPE_POSTGRES, from, to);
            } else {
                con = ConPool.getConnection(host, dbPort, datasourceName, userName, userPwd, ConPool.DBTYPE_ORACLE);
                if (con != null) {
                    foundMetadata = getTableSampledata(con, ConPool.DBTYPE_ORACLE, from, to);
                } else {
                    LogEngine.log("getTableMetadata: Cannot get a connection (null) for: " + datasourceName + " " + dbPort + " " + userName + " " + userPwd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
        return foundMetadata;
    }

    /**
     * Gets the DB name of the columns of the table representing this geometry Class.
     * DO NOT CLOSE THE CONNECTION HERE; AS IT IS USED BY THE CALLING METHOD
     */
    public Vector getTableSampledata(Connection con, String type, int from, int to) {
        Vector foundMetaData = null;
        if (type.equalsIgnoreCase(ConPool.DBTYPE_POSTGRES)) {
            foundMetaData = getTableSampledataPg(con, from, to);
        }
        if (type.equalsIgnoreCase(ConPool.DBTYPE_ORACLE)) {
            foundMetaData = getTableSampledataOra(con, from, to);
        }
        return foundMetaData;
    }

    /**
     * Gets PG DB current table first values.
     * DO NOT CLOSE THE CONNECTION HERE; AS IT IS USED BY THE CALLING METHOD
     */
    public Vector getTableSampledataPg(Connection con, int from, int to) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();

            //gets the first n records
            StringBuffer query = new StringBuffer("select ");
            //uses columnInfo definition to get values
            for (int i = 0; i < columnInfo.size(); i++) {
                if (i == 0) {
                    query.append(((GeometryClassFieldBean) columnInfo.get(i)).getName());
                } else {
                    query.append(", ").append(((GeometryClassFieldBean) columnInfo.get(i)).getName());
                }
            }
            //finishes query
            query.append(" from ").append(tableName).append(" limit ").append((to - from)).append(" offset ").append(from);
            System.out.println("QUERY DATA :" + query.toString());

            //executes query
            rs = stmt.executeQuery(query.toString());
            //parses result and fills columnValues
            Vector currentRecord = null;
            columnValues = new Vector();
            while (rs.next()) {
                currentRecord = new Vector();
                for (int n = 1; n <= columnInfo.size(); n++) {
                    currentRecord.add(rs.getString(n));
                }
                if (columnValues == null) {
                    columnValues = new Vector();
                }
                columnValues.add(currentRecord);
            }
            rs.close();
            stmt.close();
            return columnValues;

        } catch (SQLException sqle) {
            errorMessage = "getTableMetadata: SQLException: " + sqle.getMessage();
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

    /**
     * Gets ORA DB current table first values.
     * DO NOT CLOSE THE CONNECTION HERE; AS IT IS USED BY THE CALLING METHOD
     */
    public Vector getTableSampledataOra(Connection con, int from, int to) {

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();

            //gets the first n records
            StringBuffer query = new StringBuffer("select ");
            //uses columnInfo definition to get values
            for (int i = 0; i < columnInfo.size(); i++) {
                if (i == 0) {
                    query.append((String) columnInfo.get(i));
                } else {
                    query.append(", ").append((String) columnInfo.get(i));
                }
            }
            //finishes query
            query.append(" from ").append(tableName.toLowerCase());
            //executes query
            rs = stmt.executeQuery(query.toString());
            //parses result and fills columnValues
            Vector currentRecord = null;
            columnValues = new Vector();
            int counter = 0;
            while (rs.next()) {
                if (counter >= from && counter <= to) {
                    currentRecord = new Vector();
                    for (int n = 1; n <= columnInfo.size(); n++) {
                        currentRecord.add(rs.getString(n));
                    }
                    if (columnValues == null) {
                        columnValues = new Vector();
                    }
                    columnValues.add(currentRecord);
                }
                counter++;
            }
            rs.close();
            stmt.close();
            return columnValues;

        } catch (SQLException sqle) {
            errorMessage = "getTableMetadata: SQLException: " + sqle.getMessage();
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
