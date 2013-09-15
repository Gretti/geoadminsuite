/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pgAdmin2MapServer.Config;
import pgAdmin2MapServer.Pg2MS;

/**
 * A simple object representing the current list of layers C
 *
 * @author nicolas
 */
public class Map {
    private  static final String INIT_PROJECTION = "init=epsg:4326";

    public  static final Extent INIT_EXTENT = new Extent(-180.0, -85.0, 180.0, 85.0);
    public  String olBounds = "0";
    // true if all layers have the same SRID, false otherwise.
    // This will be used to set the right extent to the OL layer: either projected or global (WGS84)
    public boolean sameSRID = false;
    public java.util.Map<String, Layer> layers;
    public  Extent extent = INIT_EXTENT;
    public  String projection = INIT_PROJECTION;
    public  java.util.Map<String, Database> databases = null;;

    // TODO: handle multiple databases
    public Database database;
    public Mapfile mapfile;
    /**
     * Builds a new map, loading its layers from database configuration
     */
    public Map() {
        // inits the Mapfile
        loadLayers();
        mapfile = new Mapfile();
    }

    /**
     * Loads layers from database(s) config Private ?
     */
    public void loadLayers() {
        // gets layers to display
        layers = new HashMap<String, Layer>();

        try {
            database = Database.getDatabase();
            if (database != null) {
                databases = new HashMap<String, Database>();
                databases.put(database.getName(), database);
                layers = database.getLayers();
                this.extent = database.extent;
            }

        } catch (Exception e) {
            Pg2MS.log("loadLayers: No database information. Stoping. Ex: " + e.toString());
        }

        if (layers == null || layers.isEmpty()) {
            // ??
            Pg2MS.log("No spatial layers found in database: ddbb, schema: sscchh"
                    .replace("ddbb", Config.getInstance().database)
                    .replace("sscchh", Config.getInstance().schema));
        }
        setMapExtentAndProjection(layers);
    }

    /**
     * sets the extent and SRS of the given layers:
     *
     * SRS: If all layers have the same SRS, it will be returned, else, a 4326
     * EPSG code is returned. A config parameter will be stored telling if
     * layers have same SRID: it is used on the client
     *
     * extent: if same SRS, expand extent, else force a wide WGS84 extent //
     * TODO: smarter handling
     *
     * @param layers
     */
    public void setMapExtentAndProjection(java.util.Map<String, Layer> layers) {
        String ret = "";

        boolean firstTime = true;
        int prevSrid = 0;
        Extent globalExt = new Extent();

        if (layers != null) {
            for (Layer layer : layers.values()) {
                if (firstTime) {
                    firstTime = false;
                    globalExt.expandToInclude(layer.getExtent());
                } else {
                    if (prevSrid != layer.srs) {
                        // heterogeneous SRID for layers: defaulting Map to LatLong
                        Pg2MS.log("No unique SRID found for given layers. Default to LatLon projection.");
                        this.extent = INIT_EXTENT;
                        this.sameSRID = false;
                        return;
                    } else {
                        // expand global extent
                        globalExt.expandToInclude(layer.getExtent());
                    }
                }
                prevSrid = layer.srs;
            }
        }
        this.sameSRID = true;
        this.extent = globalExt;
        //Mapfile.projection = "init=epsg:" + prevSrid;
    }
    
    /**
     * Returns the map configuration in JSON.
     * layers is an array of OL3 vector source conf:
     * {
     *   source: new ol.source.Vector({
     *       parser: geoJsonParser,
     *       url: 'data/countries.geojson'
     *   }),
     *   style: styleConf
     * 
     * @param modelOnly
     * @return the map config object:
     * @throws JSONException 
     */
    public String getMapConfigJson(boolean modelOnly) throws JSONException {
        StringBuilder s = new StringBuilder();

        JSONObject mapConf = new JSONObject();
        JSONArray dbs = new JSONArray();

        SortedSet<String> dbKeys = new TreeSet<String>(databases.keySet());
        for (String keyd : dbKeys) {
            dbs.put(databases.get(keyd).toJsonTreeModel());
        }

        if (modelOnly) {
            return dbs.toString(4);
        } else {

            JSONArray layers = getLayersAsJson();

            mapConf.put("mapExtent", this.extent.toArray());
            mapConf.put("mapProjection", projection.replace("init=", ""));
            //mapConf.put("treeModel", dbs);
            mapConf.put("layers", layers);
            //mapConf.put("sameSRID", Map.sameSRID);

            return mapConf.toString(4);
        }
    }
    
    /**
     * Builds a JSONArray containig layers JSON objects
     *
     * @return the array
     */
    public JSONArray getLayersAsJson() throws JSONException {
        JSONArray arr = new JSONArray();

        for (Database d : databases.values()) {
            for (Schema s : d.getSchemas().values()) {
                for (Layer l : s.getLayers().values()) {
                    arr.put(l.toJson());
                }
            }
        }
        return arr;
    }
    
    /**
     * writes the given layer in the given outputstream in GeoJSON format.
     * 
     * @param dbName
     * @param qualName
     * @param maxFeature
     * @param out 
     */
    public void layerToGeoJson(String dbName, String qualName, int maxFeature, OutputStream out) {
        Database d = this.databases.get(dbName);
        // TODO: inheritance
        GeoJSONLayer l = (GeoJSONLayer)d.getLayer(qualName);
        l.toGeoJson(out);
    }
    
    /**
     * 
     * @return all the layers of all databases
     */
    public Collection<Layer> getLayers() {
        Collection<Layer> res = new ArrayList<Layer>();
        
        for (Database d : databases.values()) {
            for (Schema s : d.getSchemas().values()) {
                for (Layer l : s.getLayers().values()) {
                    res.add(l);
                }
            }
        }
        return res;
    }
    
}
