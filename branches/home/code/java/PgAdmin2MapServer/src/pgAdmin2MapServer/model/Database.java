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
import org.postgresql.util.PSQLException;
import pgAdmin2MapServer.Config;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.server.ConnectionManager;

/**
 * A simple object to represent a PgAdmin database: a name and a Map of schemas
 * TODO: inheritance
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
     * @param s 
     */
    public void addSchema(Schema s) {
        if (s != null) {
            this.schemas.put(s.getName(), s);
        }
    }
    
    /**
     * Creates and returns a Database object from current program argument.
     * Also creates database schemas and tables.
     * 
     * Loads the list of tables from database, according to given parameters:
     * if name name, build layer, schema, database. 
     * if schema name: list of geo layers.
     * if only database name: list of schemas, list of layers.
     * TODO: Manages extent and projection when adding objects in each other
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
            
            // database
            dbs = new Database(Config.getInstance().database);
            
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
                        rs.getString("srid"),
                        "POSTGIS");

                schema.getLayers().put(tname, l);
            }
            stmt.close();
            Pg2MS.log(dbs.getLayers().size() + " layer(s) loaded");
            
            // gets layers extent
            query = "select st_estimated_extent(?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(query);
            for (MSLayer layer : dbs.getLayers()) {
                pstmt.setString(1, layer.schema);
                pstmt.setString(2, layer.name);
                pstmt.setString(3, layer.geom);
                try {
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        Pg2MS.log("extent: " + rs.getString(1));
                        layer.setExtent(rs.getString(1));
                    }
                } catch (PSQLException pe) {
                    // null estimated extent may arise if table was not analysed
                    layer.setExtent("BOX(-180 -85, 180 85)");
                }
            }
            
            pstmt.close();
        } catch (Exception e) {
            Pg2MS.log("Error during DB info retrieval: " + e.toString());
        }
        finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
        return dbs;
    }
}
