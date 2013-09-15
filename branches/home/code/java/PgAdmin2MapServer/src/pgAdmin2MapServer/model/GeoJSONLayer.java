/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONException;
import org.json.JSONObject;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.server.ConnectionManager;

/**
 * Represent a Postgis table in GeoJSON format TODO: stream JSON directly from
 * database to client
 *
 * @author nicolas
 */
public class GeoJSONLayer extends Layer {
    public static String JSON_QUERY_TEMPLATE = "SELECT row_to_json(t) FROM (\n" +
"  SELECT 'FeatureCollection' AS type,\n" +
"         array_to_json(array_agg(row_to_json(m))) AS features\n" +
"  FROM (\n" +
"    SELECT 'Feature' AS type,\n" +
"           ST_AsGeoJSON(GEO_COL)::json AS geometry,\n" +
"           (\n" +
"             SELECT row_to_json(b)::json\n" +
"             FROM (\n" +
"               SELECT  COL_LIST\n" +
"               FROM TABLE_NAME\n" +
"               WHERE JOIN_COND\n" +
"             ) b\n" +
"           ) AS properties\n" +
"    FROM TABLE_NAME g\n" +
"  ) m\n" +
") t;";
    
    public static final String COL_MD_TEMPLATE = "with const as (\n" +
"    SELECT kcu.column_name, tc.constraint_name, tc.constraint_type, tc.table_schema, tc.table_name\n" +
"    FROM information_schema.key_column_usage kcu\n" +
"    JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name \n" +
"    WHERE kcu.table_name = ? and kcu.table_schema = ?\n" +
") select c.column_name, co.constraint_type \n" +
"from information_schema.columns c left join const co on (c.table_schema = co.table_schema and c.table_name = co.table_name and c.column_name = co.column_name)\n" +
"where c.table_name = ? and c.table_schema = ?";
    
    
    public GeoJSONLayer(String dbName, String schema, String table, String geom, String type, String connectionType, int srs) {
        this.databaseName = dbName;
        this.schemaName = schema;
        this.name = table;
        this.qualName = this.schemaName + "." + this.name;
        this.geom = geom;
        this.srs = srs;
        this.url = "/geoJSON?dbName=" + this.databaseName + "&layerName=" + this.qualName + "&maxFeatures=" + this.maxFeatures;
    }

    /**
     * Returns a OpenLayers 3 JSON object suitable to call a vector layer this
     * way: new ol.source.Vector(obj). { parser: new ol.parser.GeoJSON(), url:
     * 'data/countries.geojson' }
     */
    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("name", this.qualName);
        res.put("databaseName", this.databaseName);
        // TODO: constant for JS objects names
        res.put("url", this.url);
        res.put("extent", this.extent.msString());
        res.put("srid", this.srs);

        return res;
    }

    /**
     * Returns the query suitable to retrieve the given table in GeoJSON format.
     * Needs Postgresql 9.2
     * An attempt is made to find a primary/unique key on the table for the query to work
     * if none found, generates an identifier for the table
     */
    public String getGeoJsonQuery() {
        String res = "";
        Connection con = null;
        String colList = "";
        String joinCond = null;
        
        try {
            con = ConnectionManager.getConnection();
            String query = GeoJSONLayer.COL_MD_TEMPLATE;
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, this.name);
            stmt.setString(2, this.schemaName);
            stmt.setString(3, this.name);
            stmt.setString(4, this.schemaName);
            Pg2MS.log("getting table metadata with: " + stmt.toString());

            // prepare the list of non geo columns for the table
            // and finds the first pk/unik constraint
            String idCol = "";
            ResultSet rs = stmt.executeQuery();
            String sep = "";
            
            while (rs.next()) {
                if (! this.geom.equals(rs.getString(1))) {
                    colList += sep + rs.getString(1);
                    sep = ", ";
                }
                if (idCol.isEmpty() && "PRIMARY KEY".equals(rs.getString(2))) {
                    idCol = rs.getString(1);
                } else if (idCol.isEmpty() && "UNIQUE".equals(rs.getString(2))) {
                    idCol = rs.getString(1);
                }
            }
            
            String tableWithId = "";
            String tabName = this.name;
            if (idCol.isEmpty()) {
                // table has no pk/unique constraint: generates one by rewriting the table
                // including a row_number() window function
                tableWithId = "with tmp as (\n" +
                    "    select row_number() over () as gen_id__, " + colList + ", " + this.geom +
                    "    from " + this.qualName + "\n" +
                    ") ";
                tabName = "tmp";
                idCol = "gen_id__";
                colList = idCol + ", " + colList;
            }
            joinCond = idCol + "=g." + idCol; 
            
            // Build the GeoJSON query, taking into account table id
            res = tableWithId + GeoJSONLayer.JSON_QUERY_TEMPLATE.replace("COL_LIST", colList)
                .replace("JOIN_COND", joinCond)
                .replace ("TABLE_NAME", tabName)
                .replace("GEO_COL", this.geom);

            stmt.close();
        } catch (Exception e) {
            Pg2MS.log("Error retrieving layer: " + this.qualName + " as GeoJSON: " + e.toString());
        } finally {
            try {
                con.close();
            } catch (Exception e) {
                Pg2MS.log("Cannot close connection: " + e.toString());
            }
        }
        return res;
    }
    
    /**
     * Returns the GeoJSON representation of this layer, writing directly in the provided
     * output stream
     * @param out 
     */
    public void toGeoJson(OutputStream out) {
        Connection con = null;

        try {
            // TODO: a pool of connections per database ?
            con = ConnectionManager.getConnection();
            // builds SQL Query
            String query = this.getGeoJsonQuery();
            Pg2MS.log("GeoJSON query: " + query);

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                out.write(rs.getBytes(1));
            }
            stmt.close();
        } catch (Exception e) {
            Pg2MS.log("Error retrieving layer: " + this.qualName + " as GeoJSON: " + e.toString());
        } finally {
            try {
                con.close();
            } catch (Exception e) {
                Pg2MS.log("Cannot close connection: " + e.toString());
            }
        }
    }
}
