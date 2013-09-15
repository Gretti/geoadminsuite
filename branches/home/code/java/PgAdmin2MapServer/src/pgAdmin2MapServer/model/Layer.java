/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import org.json.JSONException;
import org.json.JSONObject;
import pgAdmin2MapServer.Config;

/**
 * Base class representing a displayed layer
 *
 * @author nicolas
 */
public class Layer {
    
    public String databaseName = "";
    public String schemaName = "";
    public String name = "";
    /** schema.name */
    public String qualName = "";
    public String url = ""; // the mapserver URL returning this layer
    public String geom = "";
    public int srs = 0;
    public String type = "";
    public Extent extent = null;
    public Extent WGSExtent = null;
    // get all features
    public long maxFeatures = -1;
    
    public Layer() {
    }

    public Layer(String dbName, String url, String schema, String table, String geom, String type,
            String connectionType, int srs) {
        this.databaseName = dbName;
        this.url = url;
        this.schemaName = schema;
        this.name = table;
        this.qualName = this.schemaName + "." + this.name;
        this.geom = geom;
        this.srs = srs;
        this.type = type;
    }

    public Extent getExtent() {
        return extent;
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
     * Sets the layer WGS84 extent based on given box2d representation: BOX(1
     * 1,2 2);
     *
     * @param ext
     */
    public void setWGSExtent(Extent wgsext) {
        this.WGSExtent = wgsext;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("\tLAYER\n");
        return b.toString();
    }

    public JSONObject toJsonTreeModel() throws JSONException {
        JSONObject res = new JSONObject();
        String img = "img/shape_group.png";
        if (this.type.contains("RASTER")) {
            img = "img/photo.png";
        } else if (this.type.contains("POINT")) {
            img = "img/star.png";
        } else if (this.type.contains("POLYGON")) {
            img = "img/shape_group.png";
        } else if (this.type.contains("LINE")) {
            img = "img/chart_line.png";
        }
        res.put("text", this.name);
        res.put("layerName", this.name);
        res.put("icon", img);
        res.put("leaf", true);
        res.put("checked", true);


        return res;
    }

    /**
     * JSON info to build a new Layer. TODO: factorize with json tree model
     */
    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("name", this.name);
        res.put("url", this.url);
        res.put("extent", this.extent.msString());
        res.put("WGSExtent", this.WGSExtent.msString());

        return res;
    }
}
