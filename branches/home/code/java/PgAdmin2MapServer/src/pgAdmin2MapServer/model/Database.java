/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;
import pgAdmin2MapServer.Config;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.server.ConnectionManager;

/**
 * A simple object to represent a PgAdmin database: a name and a Map of schemas
 * TODO: inheritance
 *
 * @author nicolas
 */
public class Database {

    private String name;
    private Map<String, Schema> schemas;

    public Database(String name) {
        this.name = name;
        this.schemas = new HashMap<String, Schema>();
    }

    public String getName() {
        return name;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Schema> getSchemas() {
        return schemas;
    }

    /**
     * Returns the list of layers for all schemas (flatened schemas and layers)
     *
     * @return
     */
    public List<MSLayer> getLayers() {
        List<MSLayer> layers = new ArrayList<MSLayer>();

        for (Schema schema : schemas.values()) {
            layers.addAll(schema.getLayers().values());
        }
        return layers;
    }

    /*
     public void setSchemas(Map<String, Schema> schemas) {
     this.schemas = schemas;
     }
     * */
    /**
     * Adds the given schema to this database
     *
     * @param s
     */
    public void addSchema(Schema s) {
        if (s != null) {
            this.schemas.put(s.getName(), s);
        }
    }

    /**
     * Creates and returns a Database object from current program argument. Also
     * creates database schemas and tables.
     *
     * Loads the list of tables from database, according to given parameters: if
     * name name, build layer, schema, database. if schema name: list of geo
     * layers. if only database name: list of schemas, list of layers. TODO:
     * Manages extent and projection when adding objects in each other
     *
     * @return The database object represented by program arguments
     * @throws Exception
     */
    public static Database getDatabase() {
        Database dbs = null;
        Connection con = null;

        try {
            con = ConnectionManager.getConnection();
            // builds SQL Query
            String whereClause = "";

            // TODO: prepared statement to avoid bad strings
            if (Config.getInstance().schema.length() > 0) {
                whereClause += " where f_table_schema='" + Config.getInstance().schema + "'";

                if (Config.getInstance().table.length() > 0) {
                    whereClause += " and f_table_name='" + Config.getInstance().table + "'";
                }
            }
            String query = "select f_table_schema, f_table_name, f_geometry_column,"
                    + "coord_dimension, srid, type from geometry_columns" + whereClause;

            Pg2MS.log("query to list layers: " + query);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            dbs = new Database(Config.getInstance().database);
            
            // gets all registered layers:
            // TODO: also compute general extent and SRID here
            while (rs.next()) {
                String sname = rs.getString("f_table_schema");
                Schema schema = dbs.getSchemas().get(sname);
                if (schema == null) {
                    schema = new Schema(sname, dbs.getName());
                    dbs.getSchemas().put(sname, schema);
                }

                String tname = rs.getString("f_table_name");
                MSLayer l = new MSLayer(
                        Pg2MS.mapfileUrl + "&layer=" + tname,
                        schema.getName(),
                        tname,
                        rs.getString("f_geometry_column"),
                        rs.getString("type"),
                        "POSTGIS",
                        rs.getInt("srid"));

                schema.getLayers().put(tname, l);
            }
            stmt.close();
            Pg2MS.log(dbs.getLayers().size() + " layer(s) loaded");

            // gets layers estimated extent in native and WGS84 projection, to 
            query = "with est_ext as ( ";
            query += "    select st_estimated_extent(?, ?, ?) as e, ? as srid ";
            query += "), bboxes as ( ";
            query += "	select e, ";
            query += "		case when srid < 1 then 'BOX(-180 -85, 180 85)'::box2d ";
            query += "		else st_transform(st_setSRID(e, srid), 4326)::box2d end as we  ";
            query += "	from est_ext ";
            query += ") select st_xmin(e), st_ymin(e), st_xmax(e), st_ymax(e), st_xmin(we), st_ymin(we), st_xmax(we), st_ymax(we) ";
            query += " from bboxes";
            //query = "select st_xmin(e), st_ymin(e), st_xmax(e), st_ymax(e) from (select st_estimated_extent(?,?,?) as e) as t;";
            PreparedStatement pstmt = con.prepareStatement(query);
            for (MSLayer layer : dbs.getLayers()) {
                pstmt.setString(1, layer.schema);
                pstmt.setString(2, layer.name);
                pstmt.setString(3, layer.geom);
                pstmt.setInt(4, layer.srs);
                try {
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        Extent ext = new Extent(
                                rs.getDouble(1),
                                rs.getDouble(2),
                                rs.getDouble(3),
                                rs.getDouble(4));
                        Extent extWgs = new Extent(
                                rs.getDouble(5),
                                rs.getDouble(6),
                                rs.getDouble(7),
                                rs.getDouble(8));
                        layer.setExtent(ext);
                        layer.setWGSExtent(extWgs);
                        Pg2MS.log("extent: " + ext.msString() + " wgs extent: " + extWgs.msString());
                    }
                } catch (PSQLException pe) {
                    pe.printStackTrace();
                    // null estimated extent may arise if table was not analysed
                    Pg2MS.log("No estimated extent for layer: " + layer.getQualName() + ". Consider ANALYZING this table");
                    layer.setExtent(Mapfile.INIT_EXTENT);
                    layer.setWGSExtent(Mapfile.INIT_EXTENT);
                }
            }

            pstmt.close();
        } catch (Exception e) {
            Pg2MS.log("Error during DB info retrieval: " + e.toString());
        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
        return dbs;
    }
    
    public JSONObject toJsonTreeModel() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("text", this.name);
        res.put("leaf", false);
        res.put("icon", "img/database.png");
        res.put("checked", true);
        res.put("expanded", true);
        
        JSONArray schs = new JSONArray();
        SortedSet<String> schemasKeys = new TreeSet<String>(getSchemas().keySet());
        for (String keys : schemasKeys) {
            schs.put(getSchemas().get(keys).toJsonTreeModel());
        }
        res.put("children", schs);
        return res;
    }
}
