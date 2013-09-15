/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
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
 * An object to represent a PgAdmin database: a name and a Map of schemas TODO:
 * inheritance
 *
 * @author nicolas
 */
public class Database {

    private String name;
    private Map<String, Schema> schemas;
    // TODO: ugly
    public static Extent extent;

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
    public Map<String, Layer> getLayers() {
        Map<String, Layer> layers = new HashMap<String, Layer>();

        for (Schema schema : schemas.values()) {
            layers.putAll(schema.getLayers());
        }
        return layers;
    }

    /**
     * Convenience method: returns the given layer (qualified name)
     *
     * @return
     */
    public Layer getLayer(String qualName) {
        Layer l = null;
        for (Schema schema : schemas.values()) {
            if (schema.getLayers().get(qualName) != null) {
                l = schema.getLayers().get(qualName);
                break;
            }
        }
        return l;
    }

    /*
     public void setSchemas(Map<String, Schema> schemas) {
     this.schemas = schemas;
     }
     * */
    /**
     * Adds the given schemaName to this database
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
     * creates database schemas and tables (layers)
     *
     * Loads the list of tables from database, according to given parameters: if
     * name name, build layer, schemaName, database. if schemaName name: list of geo
     * layers. if only database name: list of schemas, list of layers. TODO:
     * Manages extent and projection when adding objects in each other
     *
     * @return The database object represented by program arguments
     * @throws Exception
     */
    public static Database getDatabase() {
        Database database = null;
        Connection con = null;
        String query = "with geo as (\n" +
            "    select f_table_schema, f_table_name, f_geometry_column,coord_dimension, srid, type \n" +
            "    from geometry_columns \n" +
            "    WHERE_CLAUSE \n" +
            "), const as (\n" +
            "    SELECT kcu.column_name, tc.constraint_name, tc.constraint_type, tc.table_schema, tc.table_name\n" +
            "    FROM geo g, information_schema.key_column_usage kcu\n" +
            "        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name \n" +
            "    WHERE kcu.table_name = g.f_table_name and kcu.table_schema = g.f_table_schema\n" +
            ") select distinct on (g.f_table_name) g.f_table_name, g.f_table_schema, g.f_geometry_column, c.column_name, co.constraint_type, g.type, g.srid \n" +
            "from geo g, information_schema.columns c \n" +
            "    left join const co on (c.table_schema = co.table_schema and c.table_name = co.table_name and c.column_name = co.column_name) \n" +
            "where c.table_name = g.f_table_name and c.table_schema = f_table_schema \n" +
            "order by 1, 5";

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
            query = query.replace("WHERE_CLAUSE", whereClause);

            Pg2MS.log("query to list layers: " + query);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            database = new Database(Config.getInstance().database);

            // gets all registered layers:
            while (rs.next()) {
                String sname = rs.getString("f_table_schema");
                Schema schema = database.getSchemas().get(sname);
                if (schema == null) {
                    schema = new Schema(sname, database.getName());
                    database.getSchemas().put(sname, schema);
                }

                Layer l = null;
                String tname = rs.getString("f_table_name");

                // TODO: move logic elsewhere (layer ?)
                if (Pg2MS.USE_FORMAT_GEOJSON) {
                    l = new GeoJSONLayer(
                            database.name,
                            schema.getName(),
                            tname,
                            rs.getString("f_geometry_column"),
                            rs.getString("type"),
                            "POSTGIS",
                            rs.getInt("srid"));
                } else {
                    l = new MSLayer(
                            schema.getName(),
                            tname,
                            rs.getString("f_geometry_column"),
                            rs.getString("type"),
                            "POSTGIS",
                            rs.getInt("srid"),
                            rs.getString("column_name"),
                            rs.getString("constraint_type"));
                }

                schema.getLayers().put(l.qualName, l);
            }
            stmt.close();
            Pg2MS.log(database.getLayers().size() + " layer(s) loaded");

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
            
            Database.extent = new Extent();
            
            PreparedStatement pstmt = con.prepareStatement(query);
            for (Layer layer : database.getLayers().values()) {
                pstmt.setString(1, layer.schemaName);
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
                        Pg2MS.log("extent for " + layer.qualName + ": " + ext.msString() + " wgs extent: " + extWgs.msString());
                        Database.extent.expandToInclude(ext);
                    }
                } catch (PSQLException pe) {
                    pe.printStackTrace();
                    // null estimated extent may arise if table was not analysed
                    Pg2MS.log("No estimated extent for layer: " + layer.qualName + ". Consider ANALYZING this table");
                    layer.setExtent(pgAdmin2MapServer.model.Map.INIT_EXTENT);
                    layer.setWGSExtent(pgAdmin2MapServer.model.Map.INIT_EXTENT);
                }
            }
            Pg2MS.log("General map extent: " + Database.extent.msString());

            pstmt.close();
        } catch (Exception e) {
            Pg2MS.log("Error during DB info retrieval: " + e.toString());
        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
        return database;
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
