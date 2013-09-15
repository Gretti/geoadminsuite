/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import pgAdmin2MapServer.Config;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.Pg2MS;

/**
 * A simple object representing a MapFile: a name, an extent, a projection, a
 * list of layers Can write mapfile to file
 *
 * @author nicolas
 */
public class Mapfile {

    
    public static final String INIT_PROJECTION = "init=epsg:4326";
    public  String name;
    public  String projection = INIT_PROJECTION;
    public  String olBounds = "0";
    // true if all layers have the same SRID, false otherwise.
    // This will be used to set the right extent to the OL layer: either projected or global (WGS84)
    public  boolean sameSRID = false;

    /**
     * Writes a mapfile with given params to system temp folder
     *
     * @return the path to the mapfile written
     */
    public  String write() throws Exception {
        // gets layers to display
        Collection<Layer> layers = Pg2MS.map.getLayers();

        if (layers == null || layers.isEmpty()) {
            // ??
            Pg2MS.log("No spatial layers found in database: ddbb, schema: sscchh"
                    .replace("ddbb", Config.getInstance().database)
                    .replace("sscchh", Config.getInstance().schema));
            return "";
        }
        //setMapExtentAndProjection(layers);
        // stores this extent as new OpenLayers.Bounds(1682667.23673968, 2182020.94070385, 1719513.08792259, 2242575.97358883)
        // t<o be injected in html OL page
        olBounds = "new OpenLayers.Bounds(" 
                + Pg2MS.map.extent.xMin + ", " 
                + Pg2MS.map.extent.xMax + ", " 
                + Pg2MS.map.extent.yMin + ", " 
                + Pg2MS.map.extent.yMax + ")";

        //String tmpDir = System.getProperty("java.io.tmpdir");
        File mapfile = new File(Pg2MS.tmpDir, Pg2MS.mapfileName);

        FileWriter f = new FileWriter(mapfile);
        StringBuilder b = new StringBuilder("MAP\n");
        b.append("\tname \"PgAdmin2MapServer\"\n");
        b.append("\tsize 500 500\n");
        b.append("\timagetype png\n");
        b.append("\ttransparent on\n");
        b.append("\textent ").append(Pg2MS.map.extent.msString()).append("\n");
        b.append("\n");
//        if (!"init=epsg:0".equals(projection) && !"init=epsg:-1".equals(projection)) {
//            b.append("\tPROJECTION").append("\n");
//            b.append("\t\t\"").append(projection).append("\"\n");;
//            b.append("\tEND#PROJECTION").append("\n");
//        }

        b.append("\n");
//        b.append("\tWEB\n");
//        b.append("\t\timagepath \"").append(Pg2MS.tmpDir).append(File.separator).append("\"\n");
//        b.append("\n");
//        b.append("\t\tMETADATA").append("\n");
//        b.append("\t\t\t\"wms_title\"\t\"PgAdmin2MapServer local WMS server\"").append("\n");
//        b.append("\t\t\t\"wms_onlineresource\"\t\"").append(Pg2MS.mapfileUrl).append("\"\n");
//        b.append("\t\t\t\"wms_enable_request\"\t\"*\"").append("\n");
//        b.append("\t\tEND #metadata").append("\n");
//        b.append("\tEND #WEB\n");

        b.append("\n##################LAYERS########################").append("\n");
        for (Layer layer : layers) {
            b.append(layer.toString());
        }
        b.append("\tSYMBOL").append("\n");
        b.append("\t\tNAME \"circle\"").append("\n");
        b.append("\t\tTYPE ELLIPSE").append("\n");
        b.append("\t\tPOINTS").append("\n");
        b.append("\t\t\t1 1").append("\n");
        b.append("\t\tEND # symbol").append("\n");
        b.append("\tEND").append("\n");
        b.append("END #MAP\n");
        f.write(b.toString());

        f.close();
        return mapfile.getAbsolutePath();
    }

    public  String getName() {
        return name;
    }

    public  void setName(String name) {
        name = name;
    }

    /**
     * Returns the map extent srs, in the form init=EPSG:<value> based on layers
     * extents
     */
    public  String getProjection() {
        return projection;
    }

    public  String getOlBounds() {
        return olBounds;
    }

    public  void setOlBounds(String olBounds) {
        olBounds = olBounds;
    }
}
