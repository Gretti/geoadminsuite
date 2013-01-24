/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

/**
 * Represents very simply a mapserver layer, able to write itself as a MapFile LAYER
 * object
 * 
 * TODO: use Bastien framework to manage MapServer objects
 * @author nicolas
 */
public class MSLayer {
    public String schema = "";
    public String name = "";
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
    public String extent = "";
    
    public MSLayer(String schema, String table, String geom, String type,
            String connectionType, String srs) {
        this.schema = schema;
        this.name = table;
        this.geom = geom;
        this.status = "ON";
        this.setType(type);
        this.connectionType = connectionType;
        this.srs = srs;
        this.color = "255 0 0";
        this.outlineColor = "0 0 0";
    }
    
    /**
     * Returns the DATA property built from schema, name, geom:
     * geom from schema.name
     * @return 
     */
    public String getData() {
        return this.geom + " from " + this.schema + "." + this.name;
    }
    
    /** 
     * Sets this layer connection based on PgAdmin PostgreSQL parameters (program arguments)
     * @param params the program arguments (argv)
     */
    public void setConnection(String[] params) {
        if (params != null && params.length > 4) {
            String host = params[0].split("=").length > 1 ? params[0].split("=")[1] : "" ;
            String port = params[1].split("=").length > 1 ? params[1].split("=")[1] : "" ;
            String dbname = params[2].split("=").length > 1 ? params[2].split("=")[1] : "" ;
            String user = params[3].split("=").length > 1 ? params[3].split("=")[1] : "" ;
            String pwd = params[4].split("=").length > 1 ? params[4].split("=")[1] : "" ;
            
            this.connection = this.connection.replaceAll("_host_", host).replace("_port_", port)
                    .replace("_dbname_", dbname).replace("_user_", user).replace("_pwd_", pwd);
        }
    }
    /**
     * Sets the layer extent based on given box2d representation:
     * BOX(1111705.875 2037524.5,2241439.25 3100623)
     * @param ext 
     */
    public void setExtent(String ext) {
        if (ext != null && ext.startsWith("BOX(")) {
            this.extent = ext.substring(4, ext.length()-1).replace(",", " ");
        }
    }
    
    /**
     * Sets the Mapserver layer type according to given Postgis type
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
        b.append("\t\tNAME ").append(name).append("\n");
        b.append("\t\tTYPE ").append(type).append("\n");
        b.append("\t\tSTATUS DEFAULT\n");
        b.append("\t\tOPACITY ").append(opacity).append("\n");
        b.append("\t\tCONNECTIONTYPE ").append(connectionType).append("\n");
        b.append("\t\tCONNECTION '").append(connection).append("'\n");
        b.append("\t\tDATA '").append(getData()).append("'\n");
        b.append("\t\tCLASS\n");
        b.append("\t\t\tCOLOR ").append(color).append("\n");
        b.append("\t\t\tOUTLINECOLOR ").append(outlineColor).append("\n");
        b.append("\t\tEND #CLASS\n");
        if (this.srs.length() > 0 && !"0".equals(this.srs)) {
            b.append("\t\tPROJECTION\n");
            b.append("\t\t\t'init=epsg:").append(this.srs).append("'\n");
            b.append("\t\tEND #PROJECTION\n");
        }
        b.append("\tEND #LAYER\n");
        
        return b.toString();
    }
}
