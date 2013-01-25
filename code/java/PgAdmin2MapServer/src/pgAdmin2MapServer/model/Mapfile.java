/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pgAdmin2MapServer.Config;
import pgAdmin2MapServer.Pg2MS;

/**
 * A simple object representing a MapFile: a name, an extent, a projection, a
 * list of layers Can write mapfile to file
 *
 * @author nicolas
 */
public class Mapfile {

    private static String name;
    private static Extent extent = new Extent(-180.0, -85.0, 180.0, 85.0);
    private static String projection = "init:epsg=4326";
    private static Map<String, Database> databases = new HashMap<String, Database>();
    public static String olBounds = "0";

    /**
     * Writes a mapfile with given params to system temp folder
     *
     * @return the path to the mapfile written
     */
    public static String write() throws Exception {
        // gets layers to display
        List<MSLayer> layers = null;

        try {
            Database d = Database.getDatabase();
            if (d != null) {
                databases.put(d.getName(), d);
                layers = d.getLayers();
            }

        } catch (Exception e) {
            Pg2MS.log("mapfile write: No database information. Stoping. Ex: " + e.toString());
            return "";
        }

        if (layers == null || layers.isEmpty()) {
            // ??
            Pg2MS.log("No spatial layers found in database: ddbb, schema: sscchh"
                    .replace("ddbb", Config.getInstance().database)
                    .replace("sscchh", Config.getInstance().schema));
            return "";
        }
        setMapExtentAndProjection(layers);
        // stores this extent as new OpenLayers.Bounds(1682667.23673968, 2182020.94070385, 1719513.08792259, 2242575.97358883)
        // to be injected in html OL page
        olBounds = "new OpenLayers.Bounds(" + extent.xMin + ", " + extent.xMax + ", " + extent.yMin + ", " + extent.yMax + ")";

        //String tmpDir = System.getProperty("java.io.tmpdir");
        String tmpDir = "/tmp";
        File mapfile = new File(Pg2MS.tmpDir, Pg2MS.mapfileName);

        FileWriter f = new FileWriter(mapfile);
        StringBuilder b = new StringBuilder("MAP\n");
        b.append("\tsize 500 500\n");
        b.append("\textent ").append(extent).append("\n");

        for (MSLayer layer : layers) {
            b.append(layer.toString());
        }

        b.append("\tWEB\n");
        b.append("\t    imagepath '").append(tmpDir).append("/'\n");
        b.append("\tEND #WEB\n");
        b.append("END #MAP\n");
        f.write(b.toString());

        f.close();
        return mapfile.getAbsolutePath();
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        name = name;
    }

    public static Extent getExtent() {
        return extent;
    }

    /**
     * Returns the map extent srs, in the form init:EPSG=<value> based on layers
     * extents
     */
    public static String getProjection() {
        return projection;
    }

    public static String getOlBounds() {
        return olBounds;
    }

    public static void setOlBounds(String olBounds) {
        Mapfile.olBounds = olBounds;
    }

    /**
     * Build the JSON object representing a mapConfig: { "mapExtent" : map
     * bounds, "mapProjection" : mapFile projection string, "databases" : [{
     * "dbName" : database name, "schemas" : [{ "schemaName" : schema name,
     * "layers" : [{ "name" : layer name, "url" : layer URL }, // other layers]
     * } // other schemas] }, // other databases] }
     * 
     * NEW: 
     *
     * @return
     */
    public static String getMapConfig() throws JSONException {
        StringBuilder s = new StringBuilder();

        JSONObject mapConf = new JSONObject();
        JSONArray dbs = new JSONArray();

        // Generates objects from database
        SortedSet<String> dbKeys = new TreeSet<String>(databases.keySet());
        for (String keyd : dbKeys) {
            Database d = databases.get(keyd);
            JSONObject dbJson = new JSONObject();
            dbJson.put("dbName", d.getName());
            JSONArray schemasJson = new JSONArray();

            SortedSet<String> schemasKeys = new TreeSet<String>(d.getSchemas().keySet());
            for (String keys : schemasKeys) {
                Schema sch = d.getSchemas().get(keys);
                JSONObject schJson = new JSONObject();
                schJson.put("schemaName", sch.getName());
                JSONArray layersJson = new JSONArray();

                SortedSet<String> layersKeys = new TreeSet<String>(sch.getLayers().keySet());
                for (String keyl : layersKeys) {
                    MSLayer lay = sch.getLayers().get(keyl);
                    JSONObject layJson = new JSONObject();
                    dbJson.put("name", lay.name);
                    dbJson.put("url", lay.url);

                    layersJson.put(layJson);
                }
                schJson.put("layers", layersJson);
                //dbs.put(d.getName(), dbJson);
            }
            dbJson.put("schemas", schemasJson);
        }

        mapConf.put("mapExtent", getExtent());
        mapConf.put("mapProjection", getProjection());
        mapConf.put("databases", dbs);

        return mapConf.toString(4);
    }

    public static String getMapConfigJson() throws JSONException {
        StringBuilder s = new StringBuilder();

        JSONObject mapConf = new JSONObject();
        JSONArray dbs = new JSONArray();
        
        JSONArray layers = getLayersAsJson();
        
        SortedSet<String> dbKeys = new TreeSet<String>(databases.keySet());
        for (String keyd : dbKeys) {
            dbs.put(databases.get(keyd).toJsonTreeModel());
        }

        mapConf.put("mapExtent", getExtent().msString());
        mapConf.put("mapProjection", projection.replace("init:", ""));
        mapConf.put("treeModel", dbs);
        mapConf.put("layers", layers);

        return mapConf.toString(4);
    }
    

    /**
     * sets the extent and SRS of the given layers:
     *
     * SRS: If all layers have the same SRS, it will be returned, else, a 4326
     * EPSG code is returned
     *
     * extent: if same SRS, expand extent, else force a wide WGS84 extent //
     * TODO: smarter handling
     *
     * @param layers
     */
    public static void setMapExtentAndProjection(List<MSLayer> layers) {
        String ret = "";

        boolean firstTime = true;
        String prevSrid = "";
        Extent globalExt = new Extent();

        if (layers != null) {
            for (MSLayer layer : layers) {
                if (firstTime) {
                    firstTime = false;
                    globalExt.expandToInclude(layer.getExtent());
                } else {
                    if (!prevSrid.equals(layer.srs)) {
                        // heterogeneous SRID for layers: defaulting Map to LatLong
                        Pg2MS.log("No unique SRID found for given layers. Default to LatLon Mapfile.");
                        return;
                    } else {
                        // expand global extent
                        globalExt.expandToInclude(layer.getExtent());
                    }
                }
                prevSrid = layer.srs;
            }
        }
        Mapfile.extent = globalExt;
        Mapfile.projection = "init:EPSG=" + prevSrid;
    }
    
    /**
     * Builds a JSONArray containig layers JSON objects
     * @return the array
     */
    public static JSONArray getLayersAsJson() throws JSONException {
        JSONArray arr = new JSONArray();
        
        for (Database d : databases.values()) {
            for (Schema s : d.getSchemas().values()) {
                for (MSLayer l : s.getLayers().values()) {
                    arr.put(l.toJson());
                }
            }
        }
        return arr;
    }
}
