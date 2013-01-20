/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.mapserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.util.PSQLException;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.server.ConnectionManager;

/**
 * Simple manager to build MS Layers from given connection arguments
 *
 * @author nicolas
 */
public class LayerManager {

    /**
     * Returns a List<Layer> from given array of arguments, in the form
     * host=localhost port=5432 dbname=nicolas user=nicolas pwd= schema=public
     * table=mytable
     * 
     * TODO: filter out layers based on given parameters:
     * if database only: all layers
     * if DB + schema: all layers in the schema
     * if DB + schema + table: only this table
     * @param args
     * @return
     */
    public static List<Layer> getLayers(String[] params) throws Exception {
        List<Layer> layers = new ArrayList<Layer>();

        if (params == null) {
            throw new NullPointerException("params arguments are null: cannot get PG connection");
        }

        Connection con = null;

        try {
            con = ConnectionManager.getConnection(params);
            //host=localhost port=5432 database=nicolas user=nicolas passwd= schema=public table=nicolas
            String schema = "";
            String tableName = "";
            String whereClause = "";
            if (params.length > 5) {
                // a database schema provided
                schema = params[5].split("=")[1];
                //TODO: smart prepared to manage double quotes in schema and table
                whereClause += " where f_table_schema = '" + schema + "'";
                if (params.length > 6) {
                    // a database schema provided
                    tableName = params[6].split("=")[1];
                    whereClause += " and f_table_name = '" + tableName + "'";
                }
            }

            String q = "select f_table_schema, f_table_name, f_geometry_column,"
                    + "coord_dimension, srid, type from geometry_columns" + whereClause;
            
            //String q = buildQueryFromParams(params);
            Pg2MS.log("query to list layers: " + q);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(q);
            while (rs.next()) {
                
            Layer l = new Layer(
                        rs.getString("f_table_schema"),
                        rs.getString("f_table_name"),
                        rs.getString("f_geometry_column"),
                        rs.getString("type"),
                        params,
                        "POSTGIS",
                        rs.getString("srid"));

                layers.add(l);
            }
            stmt.close();
            
            // gets layers extent
            q = "select st_estimated_extent(?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(q);
            for (Layer layer : layers) {
                pstmt.setString(1, layer.schema);
                pstmt.setString(2, layer.table);
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
            Pg2MS.log("LayerManager error: " + e.toString());
        }
        finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
        Pg2MS.log(layers.size() + " layer(s) loaded");
        return layers;
    }
    
    /**
     * returns the extent of the given layers, either by expanding extent if all layers
     * have the same SRS, or empty string otherwise.
     * TODO: code the function...
     * @param layers
     * @return 
     */
    public static String getMapExtent(List<Layer> layers) {
        String ret = "";
        
        if (layers != null) {
            for (Layer layer : layers) {
                ret = layer.extent;
            }
        }
        
        return ret;
    }
}
