/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.util.SortedSet;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pgAdmin2MapServer.Config;

/**
 * Represents very simply a mapserver layer, able to write itself as a MapFile
 * LAYER object
 *
 * TODO: use Bastien framework to manage MapServer objects
 *
 * @author nicolas
 */
public class MSLayer {

    public String schema = "";
    public String name = "";
    public String url = ""; // the mapserver URL returning this layer
    public String geom = "";
    public String status = "";
    public String type = "";
    public String data = "";
    public String connection = "host=_host_  port=_port_  dbname=_dbname_  user=_user_  password=_pwd_";
    public String connectionType = "";
    public String srs = "";
    public String color = "";
    public String outlineColor = "";
    public String opacity = "100";
    public Extent extent = null;

    public MSLayer(String url, String schema, String table, String geom, String type,
            String connectionType, String srs) {
        this.url = url;
        this.schema = schema;
        this.name = table;
        this.geom = geom;
        this.status = "ON";
        this.setType(type);
        this.connectionType = connectionType;
        this.srs = srs;
        this.color = "255 0 0";
        this.outlineColor = "0 0 0";

        this.setConnection();
    }

    public Extent getExtent() {
        return extent;
    }

    /**
     * Returns the DATA property built from schema, name, geom: geom from
     * schema.name
     *
     * @return
     */
    public String getData() {
        return this.geom + " from " + this.schema + "." + this.name;
    }

    /**
     * Sets this layer connection based on PgAdmin PostgreSQL parameters
     * (program arguments)
     */
    public void setConnection() {
        Config c = Config.getInstance();
        this.connection = this.connection.replaceAll("_host_", c.host).replace("_port_", c.port)
                .replace("_dbname_", c.database).replace("_user_", c.user).replace("_pwd_", c.pwd);
    }

    /**
     * Sets the layer extent based on given box2d representation:
     * BOX(1111705.875 2037524.5,2241439.25 3100623)
     *
     * @param ext
     */
    public void setExtent(Extent ext) {
        this.extent = ext;
    }

    /**
     * Sets the Mapserver layer type according to given Postgis type
     *
     * @param pgType
     */
    public void setType(String pgType) {
        if ("POINT".equalsIgnoreCase(pgType) || "MULTIPOINT".equalsIgnoreCase(pgType)) {
            this.type = "POINT";
        } else if ("LINESTRING".equalsIgnoreCase(pgType) || "MULTILINESTRING".equalsIgnoreCase(pgType)
                || "GEOMETRY".equalsIgnoreCase(pgType) || "GEOMETRYCOLLECTION".equalsIgnoreCase(pgType)) {
            // also display geometries as lines
            this.type = "LINE";
        } else if ("POLYGON".equalsIgnoreCase(pgType) || "MULTIPOLYGON".equalsIgnoreCase(pgType)) {
            this.type = "POLYGON";
        } else if ("RASTER".equalsIgnoreCase(pgType)) {
            this.type = "RASTER";
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("\tLAYER\n");
        b.append("\t\tNAME \"").append(name).append("\"\n");
        b.append("\t\tTYPE ").append(type).append("\n");
        b.append("\t\tSTATUS ON\n");
        b.append("\t\tOPACITY ").append(opacity).append("\n");
        b.append("\t\tCONNECTIONTYPE ").append(connectionType).append("\n");
        b.append("\t\tCONNECTION \"").append(connection).append("\"\n");
        b.append("\t\tDATA \"").append(getData()).append("\"\n");
        b.append("\t\tCLASS\n");
        b.append("\t\t\tCOLOR ").append(color).append("\n");
        b.append("\t\t\tOUTLINECOLOR ").append(outlineColor).append("\n");
        
        if ("POINT".equals(type)) {
            // adds a symbol for point layer
            b.append("\t\t\tSYMBOL 'circle'\n");
            b.append("\t\t\tSIZE 10\n");
            b.append("\t\t\tCOLOR ").append("0 0 0").append("\n");
        }
        
        b.append("\t\tEND #CLASS\n");
        b.append("\n");
        if (this.srs.length() > 0 && !"0".equals(this.srs)) {
            b.append("\t\tPROJECTION\n");
            b.append("\t\t\t\"init=epsg:").append(this.srs).append("\"\n");
            b.append("\t\tEND #PROJECTION\n");
        }
        b.append("\n");
        b.append("\t\tMETADATA").append("\n");
        b.append("\t\t\t\"wms_title\"           \"").append(name).append("\"\n");
        b.append("\t\tEND #metadata\n");

        b.append("\tEND #LAYER\n");

        return b.toString();
    }

    public JSONObject toJsonTreeModel() throws JSONException {
        JSONObject res = new JSONObject();
        String img = "RASTER".equalsIgnoreCase(this.type) ? "img/photo.png" : "img/shape_group.png";
        res.put("text", this.name);
        res.put("layerName", this.name);
        res.put("icon", img);
        res.put("leaf", true);
        res.put("checked", true);

        
        return res;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("name", this.name);
        res.put("url", this.url);

        return res;
    }
}
