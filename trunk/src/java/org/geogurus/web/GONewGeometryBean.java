/*
 * GONewGeometryBean.java
 *
 * Created on 27 ao�t 2002, 15:05
 */
package org.geogurus.web;
import javax.servlet.http.HttpSession;
import java.sql.*;
import java.util.*;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.string.ConversionUtilities;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.GeometryClass;
import org.geogurus.Geometry;

/**
 * A bean to deal with adding a new kaboum geometry into a table:
 * Provides 2 modes: listing all records in a table where geometry is null
 * showing the result of the insert/update statement
 * @author  nri
 */
public class GONewGeometryBean {
    public static short LIST = 0;
    public static short RESULT = 1;
    /** the mode for this bean: either LIST: should provide a list of all records in the table
     * or RESULT: should provide a message to present the result to the user
     */
    private short mode;
    /** the session object to get GeometryClass From */
    private HttpSession session;
    /** the Kaboum object representation */
    private String kaboumObj;
    /** the current GeometryClass on which user works on */
    private GeometryClass gc;
    /** the result set of all values*/
    private ResultSet resultSet;
    /** the geometryClass columnInfo vector */
    private Vector columnInfo;
    /** the message after processing */
    private String message;
    /** the error if any */
    private String error;
    
    
    /** Creates a new instance of GONewGeometryBean */
    public GONewGeometryBean() {
        mode = LIST;
    }
    
    /** retrieves all information from DB based on current GeometryClass */
    private void init() {
        if (session == null || kaboumObj == null) {
            return;
        }
        error = null;
        message = null;
        String layerid = (String)session.getAttribute(ObjectKeys.CURRENT_GC);
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        gc =  (GeometryClass)umb.getUserLayerList().get(layerid);
        columnInfo = gc.getColumnInfo();
        
        Connection con = null;
        try {
            con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), "postgres");
            if (con == null) {
                error = "GONewGeometryBean: Cannot get a connection (null)";
                return ;
            }
            //first, select all values from table where geovalue is not null
            StringBuffer query = new StringBuffer("select ");
            for (Iterator iter = columnInfo.iterator(); iter.hasNext(); ) {
                String[] as = (String[])iter.next();
                query.append(as[0]);
                query.append(",");
            }
            query.append(gc.getIDColumn()).append(" from ").append(gc.getTableName());
            query.append(" where ").append(gc.getColumnName()).append(" is null");
            
            Statement stmt = con.createStatement();
            resultSet = stmt.executeQuery(query.toString());
            System.out.println("query is: " + query.toString());
            
        } catch (SQLException sqle) {
            error = "" + sqle.getMessage();
        } finally {
            try {con.close();} catch(Exception e) {}
        }
    }
    
    
    // setXX methods
    public void setMode(short m) {mode = m;}
    public void setSession(HttpSession s) {session = s; init();}
    public void setKaboumObj(String ko) {kaboumObj = ko;; init();}
    
    /**
     * This method either inserts or update GeometryClass' table.
     * In case of insert, oid parameter is "newgeo", else, oid is the row identifier
     * ie the geometry id, as GeometryClass geometries are identified by the row identifier
     * @param oid a String corresponding to the row identifier: "newgeo" indicates a new row => insert,
     *        else, indicates the oid row identifier => update
     *@return the new identifier for inserted/updated geometry, set by the GeometryClass
     */
    public String addGeometryToDB(String oid) {
        // builds the new geometry to insert
        StringTokenizer tk = new StringTokenizer(kaboumObj, "|");
        // discard "OBJECT" keyword
        tk.nextToken();
        String classid = tk.nextToken();
        String geoid = tk.nextToken();
        // This geoid is always NEW, as this servlet deals with object creation in kaboum
        // If user chose to update an existing row, force this id to be the row identifier, here the oid
        // for the table
        if (!oid.equals("newgeo")) {
            geoid = oid;
        }
        // discard "area" value
        tk.nextToken();
        String wkt = tk.nextToken();
        // in case of multipolygon GeometryClass, reconstruct a valid WKT multipolygon
        // from the given WKT polygon (Kaboum only builds polygons for the moment)
        if (gc.getType() == Geometry.MULTIPOLYGON) {
            wkt = "MULTIPOLYGON(" + wkt.substring(wkt.indexOf("(")) + ")";
        }
        // construct a geometry from this object:
        Geometry geom = new Geometry(geoid, wkt, gc.getTableName());
        
        //opens a connection for GeometryClass:
        Connection con = null;
        
        try {
            con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), "postgres");
            if (con == null) {
                error = "GONewGeometryBean: Cannot get a connection (null)";
                return null;
            }
            if (!gc.addGeometryToDB(con, geom)) {
//System.out.println("error produced when adding a new geometry: error message filled...");                
                error = gc.getErrorMessage();
                return null;
            }
        } catch (Exception e) {
            error = "" + e.getMessage();
        } finally {
            try {con.close();} catch(Exception e) {}
        }
        // message is now internationalized: Only table name is pertinent here
        message = gc.getTableName();
        return geom.id;
    }
    
    // getXX methods
    public short getMode() {return mode;}
    public ResultSet getResultSet() {return resultSet;}
    public String getMessage() {return message;}
    public String getError() {return error;}
    public String getKaboumObj() {return kaboumObj;}
    public GeometryClass getGeometryClass() {return gc;}
    public Vector getColumnInfo() {
        if (gc != null) {
            return gc.getColumnInfo();
        }
        return new Vector();
    }
}
