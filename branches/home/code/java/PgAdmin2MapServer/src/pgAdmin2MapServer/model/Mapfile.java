/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * A simple object representing a MapFile: 
 *  a name, an extent, a projection, a list of layers
 * Can write mapfile to file
 * @author nicolas
 */
public class Mapfile {
    private String name;
    private String extent;
    private String projection;
    
    public static String olBounds = "0";
    
    /**
     * Writes a mapfile with given params to system temp folder
     * @return the path to the mapfile written
     */
    public static String write() throws Exception {
        // gets layers to display
        List<Layer> layers = LayerManager.getLayers(params);
        String mapExt = LayerManager.getMapExtent(layers);
        // stores this extent as new OpenLayers.Bounds(1682667.23673968, 2182020.94070385, 1719513.08792259, 2242575.97358883)
        // to be injected in html OL page
        String[] bounds = mapExt.split(" ");
        olBounds = "new OpenLayers.Bounds(" + bounds[0] + ", " + bounds[1] + ", " +bounds[2] + ", " + bounds[3] + ")";
        
        //String tmpDir = System.getProperty("java.io.tmpdir");
        String tmpDir = "/tmp";
        File mapfile = new File(tmpDir, "pgadmin_viewer.map");
        
        FileWriter f = new FileWriter(mapfile);
        StringBuilder b = new StringBuilder("MAP\n");
        b.append("\tsize 500 500\n");
        b.append("\textent ").append(mapExt).append("\n");
        
        for (Layer layer : layers) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public static String getOlBounds() {
        return olBounds;
    }

    public static void setOlBounds(String olBounds) {
        Mapfile.olBounds = olBounds;
    }
    
    
}
