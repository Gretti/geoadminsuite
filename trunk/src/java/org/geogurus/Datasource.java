/*
 * Datasource.java
 *
 * Created on 31 juillet 2002, 17:41
 */

package org.geogurus;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.geogurus.tools.LogEngine;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.tools.DataManager;
import org.geogurus.raster.RasterRegistrar;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.tools.MapTools;

/**
 * A class mapping a datasource = place where to find geo data.
 * It can be a postgresql database, a folder, later ora or db2 databases
 * Retrieving list of data (geo tables, geo files or mapfiles) should maybe construct a geodata or geometry class
 * Directly To be more efficient??
 * @author  nri
 */
public class Datasource implements Serializable {
    // the datasource types
    /** a folder on an host containing geo data (should be accessible by server) */
    public static final short FOLDER = 1;
    public static final short PG = 2;
    public static final short ORA = 3;
    public static final short DB2 = 4;
    public static final short MAP = 5;
    
    protected String name = null;
    protected String dbPort = "5432";
    protected short type;
    protected String host = null;
    protected String userName = null;
    protected String userPwd = null;
    /** the name of data (table, file or mapfile) contained in this datasource */
    protected Hashtable dataList = null;
    /** the sorted list of GeometryClass, based on their names */
    protected GeometryClass[] sortedDataList;
    /** the exploration depth for folders: will look for geographic information up to this depth */
    protected int folderDepth;
    /** this error message, for callers */
    public String errorMessage;
    protected String fileSep = System.getProperty("file.separator");
    
    
    /** Creates a new instance of Datasource */
    public Datasource() {
        dataList = new Hashtable();
        folderDepth = 3;
        type = 2;
    }
    
    /** Creates a new instance of Datasource */
    public Datasource(String name, String dbPort, short type, String host, String userName, String userPwd) {
        this();
        this.name = name;
        this.dbPort = dbPort;
        this.type = type;
        this.host = host;
        this.userName = userName;
        this.userPwd = userPwd;
        
        //getDataInformation();
    }
    /**
     * Gets the name of geographic tables from the database pointed to by host,
     * or get geo files pointed to by
     * @return false when this datasource contains nothing (not a valid one)
     * DO NOT CLOSE THE CONNECTION HERE
     */
    public boolean getDataInformation() {
        boolean res = false;
        if (type == Datasource.FOLDER) {
            res =  getFileDataInformation();
        } else {
            res =  getDBDataInformation();
        }
        
        return res;
    }
    
    /**
     * Returns a sorted list of data (GeometryClass), usefull for presentation
     */
    public GeometryClass[] getSortedDataList() {
        if (dataList == null) {
            return null;
        }
        sortedDataList = (GeometryClass[])dataList.values().toArray(new GeometryClass[dataList.size()]);
        Arrays.sort(sortedDataList);
        return sortedDataList;
    }
    
    
    /**
     * Finds all geographic tables for this db, and construct their geometryClass equivalent.
     * All these geometryClasses are stored in the dataList hashtable, with the gc'id as a key.
     * The geometryClasses built here have the minimal set of information.
     */
    public boolean getDBDataInformation() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        boolean ret = false;
        
        if (type == Datasource.PG) {
            try {
                con = ConPool.getConnection(host, dbPort, name, userName, userPwd, ConPool.DBTYPE_POSTGRES);
                
                if (con == null) {
                    errorMessage = "Cannot get a connection (null) for: " + host + " " + dbPort + " " + userName + " " + userPwd;
                    LogEngine.log(errorMessage);
                } else {
                    stmt = con.createStatement();
                    // this query gives all geometric tables registered into the OpenGIS metadata tables:
                    // caution when db is not Postgis enabled
                    // The query selects distinct table_name to avoid specific behaviour for multiple geometry
                    // columns for the same data (not assumed by now)
                    // Selection reports only existing table in case of intempestive deletion of tables without cleaning geometry_columns
                    query = "select distinct g.f_table_name from geometry_columns g, pg_tables p where g.f_table_name = p.tablename";
                    
                    rs = stmt.executeQuery(query);
                    GeometryClass gc = null;
                    
                    while (rs.next()) {
                        gc = new GeometryClass(host, name, rs.getString(1), dbPort, userName, userPwd, GeometryClass.PGCLASS);
                        dataList.put(gc.getID(), gc);
                    }
                    rs.close();
                    stmt.close();
                }
                
                ret = true;
                
            } catch (SQLException sqle) {
                if (sqle.getMessage().indexOf("geometry_columns") != -1) {
                    // no geometry_columns table for this DB: skip this
                } else {
                    errorMessage = "GeometryClassExplorer.doList: SQLException: " + sqle.getMessage();
                    errorMessage += "<br>query was: <code>" + query.toString() + "</code>";
                    LogEngine.log(errorMessage);
                    sqle.printStackTrace();
                }
                
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (con != null) con.close();
                } catch (Exception sqle2) {sqle2.printStackTrace();}
            }
        }
        return ret;
    }
    
    /**
     * Finds all geographic files for the given path, and construct their geometryClass equivalent.
     * All these geometryClasses are stored in the dataList hashtable, with the gc'id as a key.
     * The geometryClasses built here have the minimal set of information.<br>
     * If a mapfile is found, it is stored as is in the dataList.<br>
     * Use the getGeometryClasses(id) method to get the list of GeometryClass for a given
     * mapfile.<p>
     *
     * Geographic files are those whose extension corresponds to the extension list arrays.
     */
    public boolean getFileDataInformation() {
        File filepath = new File(name);
        String[] list = filepath.list();
        String fullName = null;
        
        if (list == null) {
            // path is not a folder path
            errorMessage = "The given path (" + name+ ") is not valid";
            return false;
        }
        short res = -1;
        for (int i = 0; i < list.length; i++) {
            if ((res = getValidExtension(list[i])) != -1){
                if(res != GeometryClass.MAPCLASS){
                    // given file is a valid geographic source: creates a geometryClass
                    GeometryClass gc = new GeometryClass(host, name, list[i], null, null, null, res);
                    dataList.put(gc.getID(), gc);
                } else {
                    // given file is a valid mapfile source : creates a mapfile object and adds it to the list
                    if (name.lastIndexOf("\\") == name.length()-1 ||
                            name.lastIndexOf("/") == name.length() -1 ) {
                        fullName = name + list[i];
                    } else {
                        // look if the system file separator should be used, or another one
                        if (name.indexOf("\\") != -1) {
                            fullName = name + "\\" + list[i];
                        } else if (name.indexOf("/") != -1) {
                            fullName = name + "/" + list[i];
                        } else {
                            fullName = name + fileSep + list[i];
                        }
                    }
                    org.geogurus.mapserver.MapFile mf = new org.geogurus.mapserver.MapFile(fullName);
                    org.geogurus.mapserver.objects.Map map = mf.load();
                    
                    if (map != null) {
                        dataList.put(map.getID(), map);
                    } else {
                        LogEngine.log("Datasource.getFileDataInformation(): cannot load mapfile: " + fullName);
                    }
                }
            }
        }
        return true;
    }
    
    /** returns the datatype of the given filename if this file is valid:
     * a tif file is valid if it existst an associated tfw.
     * Geotiff files store their geolocalisation information. So only one file is valid
     * directly in the format.
     * shp file is valid if it existst an associated dbf and shx.
     * Caution: uppercase tfw extention seems to crash on MapServer
     * Note: extensions of associated files (dbf, tfw, shx, etc..) must be in the same
     * case a original file
     *@returns -1 if file is not valid, GeometryClass.geotype if it is valid
     */
    protected short getValidExtension(String fn) {
        // first gets extention:
        String ext = "";
        String begin = "";
        int pt = fn.lastIndexOf(".");
        try {
            ext = fn.substring(pt+1);
            begin = fn.substring(0, pt);
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
        if (ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")) {
            // verify if a tfw exists, or a wld, or
            if (RasterRegistrar.isGeoTIFF(name + fileSep + fn) ||
                    new File(name + fileSep + begin + ".tfw").exists() ||
                    new File(name + fileSep + begin + ".TFW").exists() ||
                    new File(name + fileSep + begin + ".wld").exists() ||
                    new File(name + fileSep + begin + ".WLD").exists()) {
                return GeometryClass.TIFFCLASS;
            }
        } else if (ext.equalsIgnoreCase("img")) {
            // verify if a tfw exists, or a wld
            if (new File(name + fileSep + begin + ".wld").exists() ||
                    new File(name + fileSep + begin + ".WLD").exists()) {
                return GeometryClass.IMGCLASS;
            } else {
                return -1;
            }
        } else if (ext.equalsIgnoreCase("shp")) {
            // verify if .dbf + .shx exists
            if ((new File(name + fileSep + begin + ".dbf").exists()  && new File(name + fileSep + begin + ".shx").exists()) ||
                    (new File(name + fileSep + begin + ".DBF").exists()) && new File(name + fileSep + begin + ".SHX").exists()) {
                return GeometryClass.ESRIFILECLASS;
            } else {
                return -1;
            }
        } else if(ext.equalsIgnoreCase("ecw")) {
            // verify if the associated ers file exists (temporary)
            if (new File(name + fileSep + begin + ".ers").exists()  ||
                    new File(name + fileSep + begin + ".ERS").exists()) {
                return GeometryClass.ECWCLASS;
            } else {
                return -1;
            }
        } else if(ext.equalsIgnoreCase("map")) {
            if (new File(name + fileSep + begin + ".map").exists()  ||
                    new File(name + fileSep + begin + ".MAP").exists()) {
                return GeometryClass.MAPCLASS;
            } else {
                return -1;
            }
        }
        return -1;
    }
    
    /**
     * Gets the list of all GeometryClass objects corresponding to each
     * Layer contained in the map whose ID is id.<br>
     * Paths and database parameters will be rebuilt from the mapfile values directly
     * @param id The identifier of the Map object (key of the dataList hashtable
     * @return the list of GeometryClass corresponding to each Layer of the mapfile,
     * or null if id is not a Mapfile identifier
     */
    public Vector getGeometryClasses(String id) {
        Object obj = dataList.get(id);
        if (obj == null) return null;
        
        org.geogurus.mapserver.objects.Map map = (org.geogurus.mapserver.objects.Map)obj;
        Vector layers = null;
        Vector res = new Vector();
        Layer l = null;
        GeometryClass gc = null;
        
        layers = map.getLayers();
        for (Iterator iter = layers.iterator(); iter.hasNext();) {
            l = (Layer)iter.next();
            // according to the layer's construct a GeometryClass accordingly
            switch (l.getConnectionType()) {
                case Layer.LOCAL :
                    // gets GC name (data field)
                    String mapFilePath = map.getMapFile().getParent();
                    String shapePath = map.getShapePath() != null ? map.getShapePath().getPath() : null;
                    String dataName = l.getData();
                    
                    File dataFile = MapTools.buildFileFromMapPath(mapFilePath, shapePath, dataName);
                    // must change the shapepath:  relative -> absolute
                    map.setShapePath((MapTools.buildFileFromMapPath(mapFilePath, null, shapePath)));
                    
                    String dataPath = dataFile.getParent();
                    dataName = dataFile.getName();
                    
                    // changes the layer to adapt file paths from initial mapfile to user mapfile
                    l.setData(dataFile.getPath());
                    
                    
                    // gets GC type (shape, tiff, ecw, etc.)
                    short t = 0;
                    if (dataName.lastIndexOf(".tif") == dataName.length() - 4 ||
                            dataName.lastIndexOf(".TIF") == dataName.length() - 4 ||
                            dataName.lastIndexOf(".tiff") == dataName.length() - 5 ||
                            dataName.lastIndexOf(".TIFF") == dataName.length() - 5) {
                        t = GeometryClass.TIFFCLASS;
                    } else if (dataName.lastIndexOf(".ecw") == dataName.length() - 4 ||
                            dataName.lastIndexOf(".ECW") == dataName.length() - 4) {
                        t = GeometryClass.ECWCLASS;
                    } else if (dataName.lastIndexOf(".img") == dataName.length() - 4 ||
                            dataName.lastIndexOf(".IMG") == dataName.length() - 4) {
                        t = GeometryClass.IMGCLASS;
                    } else {
                        t = GeometryClass.ESRIFILECLASS;
                        // adds the extension (.shp) if it is not present
                        if (dataName.lastIndexOf(".") != dataName.length() - 4) {
                            dataName += ".shp";
                        }
                    }
                    gc = new GeometryClass(host, dataPath, dataName, null, null, null, t);
                    break;
                case Layer.POSTGIS :
                    // gets the table name from the data field: looks like
                    // "geo_column from tablename"
                    dataName = l.getData();
                    StringTokenizer tk = new StringTokenizer(dataName);
                    if (tk.countTokens() >= 3) {
                        // discard the column name
                        tk.nextToken();
                        // check if "from keyword is present
                        if (tk.nextToken().equalsIgnoreCase("from")) {
                            dataName = tk.nextToken();
                        }
                    }
                    // gets the host, database, port, user, pwd from the connection string
                    tk = new StringTokenizer(l.getConnection());
                    StringTokenizer tk2 = null;
                    
                    while (tk.hasMoreElements()) {
                        tk2 = new StringTokenizer(tk.nextToken(), "=");
                        String key = tk2.nextToken();
                        
                        if (key.equalsIgnoreCase("dbname")) {
                            name = tk2.nextToken();
                        } else if (key.equalsIgnoreCase("host")) {
                            host = tk2.nextToken();
                        } else if (key.equalsIgnoreCase("port")) {
                            dbPort = tk2.nextToken();
                        } else if (key.equalsIgnoreCase("user")) {
                            userName = tk2.nextToken();
                        } else if (key.equalsIgnoreCase("password")) {
                            userPwd = tk2.nextToken();
                        }
                    }
                    gc = new GeometryClass(host, name, dataName, dbPort, userName, userPwd, GeometryClass.PGCLASS);
                    break;
                case Layer.ORACLESPATIAL :
                    // gets the table name from the data field: looks like
                    // "geo_column from tablename"
                    dataName = l.getData();
                    tk = new StringTokenizer(dataName);
                    if (tk.countTokens() >= 3) {
                        // discard the column name
                        tk.nextToken();
                        // check if "from keyword is present
                        if (tk.nextToken().equalsIgnoreCase("from")) {
                            dataName = tk.nextToken();
                        }
                    }
                    // gets the host, database, port, user, pwd from the connection string
                    tk = new StringTokenizer(l.getConnection());
                    tk2 = null;
                    
                    while (tk.hasMoreElements()) {
                        tk2 = new StringTokenizer(tk.nextToken(), "/");
                        tk2.nextToken();
                        userName = tk2.nextToken();
                        String dbpwd = tk2.nextToken();
                        tk2 = new StringTokenizer(dbpwd, "@");
                        userPwd = tk2.nextToken();
                        name = tk2.nextToken();
                        host = "";
                        dbPort = "1521";
                    }
                    gc = new GeometryClass(host, name, dataName, dbPort, userName, userPwd, GeometryClass.ORACLASS);
                    break;
                default:
                    LogEngine.log("Datasource.getGeometryCLasses: Mapserver layer type not yet supported (" + l.getType() + ")");
                    break;
            }
            // must set the name of the layer in the GC if it is not null
            if (l.getName() != null) {
                gc.setName(l.getName());
            }
            gc.setMSLayer(l);
            if (!gc.getMetadata()) {
                LogEngine.log("Datasource: getGeometryClasses: could not get table Metadata for GC: " + gc.getName());
            } else {
                LogEngine.log("Datasource: getGeometryClasses: Metadata got for GC: " + gc.getName());
            }
            res.add(gc);
        }
        return res;
    }
    
    // set methods
    public void setType(short type) {this.type = type; }
    public void setName(String name) {this.name = name; }
    public void setHost(String host) {this.host = host; }
    public void setDbPort(String dbPort) {this.dbPort = dbPort; }
    public void setUserName(String userName) {this.userName = userName; }
    public void setUserPwd(String userPwd) {this.userPwd = userPwd; }
    
    // get methods
    public short getType() {return this.type; }
    public String getName() {return this.name; }
    public String getHost() {return this.host; }
    public String getDbPort() {return this.dbPort; }
    public String getUserName() {return this.userName; }
    public String getUserPwd() {return this.userPwd; }
    public Hashtable getDataList() {return this.dataList; }
    
/*
    public static void main(String[] args) {
        Datasource ds = new Datasource("", null, Datasource.FOLDER, null, null, null);
        if (!ds.getDataInformation()) {
            System.out.println(ds.errorMessage);
        } else {
            System.out.println("finished...");
        }
    }
 */
}

