/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.mapserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Writes mapfile according to current params
 * @author nicolas
 */
public class MapfileWriter {
    
    public static String[] params = null;
    
    /**
     * Writes a mapfile with given params to system temp folder
     * @return the path to the mapfile written
     */
    public static String write() throws IOException {
        //String tmpDir = System.getProperty("java.io.tmpdir");
        String tmpDir = "/tmp";
        File mapfile = new File(tmpDir, "pgadmin_viewer.map");
        
        FileWriter f = new FileWriter(mapfile);
        StringBuilder b = new StringBuilder("MAP\n");
        b.append("\tsize 500 500\n");
        b.append("\textent 1682667.23673968 2182020.94070385 1719513.08792259 2242575.97358883\n");
        b.append("\tLAYER\n");
        b.append("\t    NAME testpg\n");
        b.append("\t    TYPE POLYGON\n");
        b.append("\t    STATUS ON\n");
        b.append("\t    opacity 50\n");
        b.append("\t    CONNECTIONTYPE POSTGIS\n");
        b.append("\t    CONNECTION 'dbname=").append(params[2]).append(" host=").append(params[0]).append(" user=nicolas'\n");
        b.append("\t    DATA 'geometrie from gn_2013'\n");
        b.append("\t    class\n");
        b.append("\t        color 255 0 0 \n");
        b.append("\t        outlinecolor 0 0 0 \n");
        b.append("\t    end\n");
        b.append("\tEND\n");
        b.append("\tLAYER\n");
        b.append("\t    NAME testline\n");
        b.append("\t    TYPE LINE\n");
        b.append("\t    STATUS ON\n");
        b.append("\t    opacity 50\n");
        b.append("\t    CONNECTIONTYPE POSTGIS\n");
        b.append("\t    CONNECTION 'dbname=").append(params[2]).append(" host=").append(params[0]).append(" user=nicolas'\n");
        b.append("\t    DATA 'geometrie from cst_lin11'\n");
        b.append("\t    class\n");
        b.append("\t        color 0 0 255 \n");
        b.append("\t        outlinecolor 0 0 255 \n");
        b.append("\t    end\n");
        b.append("\tEND\n");
        b.append("\tweb\n");
        b.append("\t    imagepath '").append(tmpDir).append("/'\n");
        b.append("\tend\n");
        b.append("END\n");
        f.write(b.toString());
        
        f.close();
        return mapfile.getAbsolutePath();
    }
    
}
