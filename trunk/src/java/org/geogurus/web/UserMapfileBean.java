/*
 * uploadedMapfileBean.java
 *
 * Created on 16 f�vrier 2003, 17:12
 */
package org.geogurus.web;
import org.geogurus.mapserver.objects.Map;
import java.util.Vector;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.File;
import java.io.BufferedReader;
import java.sql.*;
import org.geogurus.mapserver.MapFile;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.tools.sql.ConPool;
/**
 * This bean holds all necessary informations for the MC_loaded_mapfile.jsp page
 * to allow user to provide missing or incorrect information for the uploaded
 * mapfile
 * @author  nri
 */
public class UserMapfileBean {
    /**
     * The inner-class storing bad layer informations
     */
    public class BadLayer {
        public byte errorType;
        public int idx;
        
        public BadLayer(int idx, byte errorType) {
            this.idx = idx;
            this.errorType = errorType;
        }
        public String toString() {
        return "BadLayer index: " + idx + " - BadLayer errorType: " + errorType;
        }
    }
    
    /** */
    public static final byte BAD_CONNECTION = 0; 
    public static final byte BAD_DATA = 1; 
    /** boolean telling if the uploaded mapfile is correct:
     *  it can be handled by the userMapBean and the GAS composer part */
    private boolean valid;
    
    /** boolean telling if the uploaded mapfile symbolset parameter
     * points to a valid file for this server 
     */
    private boolean symbolsetValid;
    
    /** boolean telling if the uploaded mapfile fontset parameter
     * points to a valid file for this server 
     */
    private boolean fontsetValid;
    
    /** boolean telling if the uploaded mapfile shapepath parameter
     * points to a valid directory for this server 
     */
    private boolean shapepathValid;
    
    /**
     * The upload mapfile
     */
    private Map uploadedMapfile = null;
    
    /**
     * The vector of BadLayer object
     */
    private Vector badLayers = null;
    
    /** Creates a new instance of uploadedMapfileBean */
    public UserMapfileBean() {
        valid = false;
        symbolsetValid = true;
        fontsetValid = true;
        shapepathValid = true;
    }
    
    public UserMapfileBean(File um) {
        this();
        
        if (um == null) {
            uploadedMapfile = null;
        } else {
            MapFile mf = new MapFile(um.getAbsolutePath());
            uploadedMapfile = mf.load();
        }
    }
    
    /**
     * Peforms the check for the given mapfile
     */
    public void checkMapfile() {
        if (uploadedMapfile == null) {
            setValid(false);
            return;
        }
        // performs some checks for Map parameters:
        if (uploadedMapfile.getSymbolSet() == null) {
            //if (!uploadedMapfile.getSymbolSet().exists()) {
                setSymbolsetValid(false);
            //}
        }
        if (uploadedMapfile.getFontSet() != null) {
            if (!uploadedMapfile.getFontSet().exists()) {
                setFontsetValid(false);
            }
        }
        String shapepath = "";
        boolean shapepathIsValid = true;
        if (uploadedMapfile.getShapePath() != null) {
            shapepath = uploadedMapfile.getShapePath().getName();
            if (!uploadedMapfile.getShapePath().isDirectory()) {
                shapepathIsValid = false;
                setShapepathValid(false);
            }
        }
        // should check WEB object
        
        // checks each mapfile layer if shapepath is valid.
        // else, no need to check each layer separatly
        if (shapepathIsValid) {
            File f = null;
            
            if (uploadedMapfile.getLayers() != null) {
                for (int i = 0; i < uploadedMapfile.getLayers().size(); i++) {
                    Layer l = (Layer)uploadedMapfile.getLayers().get(i);
                    if (l.getConnectionType() == Layer.POSTGIS) {
                        // checks the connection for this layer:
                        // this server must be able to connect to the pointed database
                        if (!checkDatabaseConnection(l)) {
                            addBadLayer(i, UserMapfileBean.BAD_CONNECTION);
                        } 
                    } else if (l.getConnectionType() == Layer.LOCAL) {
                        // data must be a valid file
                        if (!checkLayerData(l, shapepath)) {
                            addBadLayer(i, UserMapfileBean.BAD_DATA);
                        }
                    }
                }
            }
        }
        // set valid to true, as validity cheks are done
        setValid(true);
        return;
    }
    
    /**
     * Checks if the data parameter of the given layer points to a
     * valid file (handle Map shapepath parameter)
     */
    private boolean checkLayerData(Layer l, String shapepath) {
        String lData = l.getData();
        
        if (lData.indexOf("/") == -1 && lData.indexOf("\\") == -1 && shapepath.length() > 0) {
            // no file separator in the Data parameter, reconstruc
            // a full data path with the given Map shapepath, if any
            lData = shapepath + "/" + lData;
        }
        // checks file extension, after separator
        if (lData.length() > 3 && lData.lastIndexOf(".") != lData.length() - 4) {
            // no file extention: this is a shapefile: rebuild it
            // extension case does not matter as mapserver itself cannot
            // handle shapefiles with Uppercase extensions
            lData += ".shp";
        }
        
        if (! new File(lData).exists()) {
            // denoted file does not exist
            return false;
        }
        // should check header and footer too.
        return true;
    }
    
    /**
     * Checks if this postgis layer contains a valid connection string
     * for this server. <br>
     *Parses the connection string to extract the database information
     *
     */
    private boolean checkDatabaseConnection(Layer l) {
        String conString = l.getConnection();
        String dbname = null;
        String host = null;
        String port = null;
        String user = null;
        String pwd = null;
        StringTokenizer tk = new StringTokenizer(conString);
        StringTokenizer tk2 = null;
        Connection con = null;
        
        while (tk.hasMoreElements()) {
            tk2 = new StringTokenizer(tk.nextToken(), "=");
            String key = tk2.nextToken();
            
            if (key.equalsIgnoreCase("dbname")) {
                dbname = tk2.nextToken();
            } else if (key.equalsIgnoreCase("host")) {
                host = tk2.nextToken();
            } else if (key.equalsIgnoreCase("port")) {
                port = tk2.nextToken();
            } else if (key.equalsIgnoreCase("user")) {
                user = tk2.nextToken();
            } else if (key.equalsIgnoreCase("password")) {
                pwd = tk2.nextToken();
            }
        }
        // attempt to connect with the given information
        try {
            con = ConPool.getConnection(host, port, dbname, user, pwd,  "postgres");
            if (con == null) {
                return false;
            }
            con.close();
            return true;
        } catch (Throwable th) {
            return false;
        } finally {
            try  {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Deals with bad mapfile fields correction: sets all user value to the mapfile,
     * then calls checkMapfile on it.
     * @param params a Map containing HttpServletRequest parameters (names and values)
     */
    public boolean checkMapfileCorrections(java.util.Map params) {
        String symbolSet = params.get("map_symbolset") == null ? null : (String)params.get("map_symbolset");
        System.out.println("obj: " + params.get("map_fontset").toString());
        String fontSet = params.get("map_fontset") == null ? null : (String)params.get("map_fontset");
        String shapepath = params.get("map_shapepath") == null ? null : (String)params.get("map_shapepath");
        
        if (symbolSet != null) {
            setSymbolsetValid(true);
        }
        if (fontSet != null) {
            uploadedMapfile.setFontSet(new File(fontSet));
            setFontsetValid(true);
        }
        if (shapepath != null) {
            uploadedMapfile.setShapePath(new File(shapepath));
            setShapepathValid(true);
        }
        
        // treats now all bad layers
        String param = null;
        for (Iterator iter = badLayers.iterator(); iter.hasNext(); ) {
            // buidls the request parameter name corresponding to bad layer parameters (data, connection)
            // if they are not null, update this layer with user-provided values
            BadLayer bl = (BadLayer)iter.next();
            Layer l = (Layer)uploadedMapfile.getLayers().get(bl.idx);
            param = "layer_connection" + bl.idx;
            if (params.get("param") != null) {
                //updates layer connection string
                l.setConnection((String)params.get(param));
            }
            param = "layer_data" + bl.idx;
            
            if (params.get("param") != null) {
                //updates layer connection string
                l.setData((String)params.get(param));
            }
        }
        
        // reset bad layers, as they just have been corrected.
        // If there is still bad layers or bad parameters, checkMapFile will find them
        badLayers.removeAllElements();
        badLayers = null;
        
        // recheck mapfile to see if all is ok, now.
        checkMapfile();
        
        return isValid();
    }
    
    /**
     * Adds information for a bad layer in the uploaded file
     * @param idx the layer index into the Map.Layers vector
     * @errorType the type of bad parameter for this layer
     */
    public void addBadLayer(int idx, byte errorType) {
        if (badLayers == null) {
            badLayers = new Vector();
        }
        badLayers.add(new BadLayer(idx, errorType));
    }
    
    // getXXX methods
    /**
     * Bean is valid if all its properties are valid
     */
    public boolean isValid() {
        return (valid && symbolsetValid && fontsetValid && shapepathValid && badLayers == null);
    }
    
    public boolean isSymbolsetValid() {return symbolsetValid;}
    public boolean isFontsetValid() {return fontsetValid;}
    public boolean isShapepathValid() {return shapepathValid;}
    public Map getUploadedMapfile() {return uploadedMapfile;}
    public Vector getBadLayers() {return badLayers;}
    
    // setXXX methods
    public void setValid(boolean valid) {this.valid = valid;}
    public void setSymbolsetValid(boolean symbolsetValid) {this.symbolsetValid = symbolsetValid;}
    public void setFontsetValid(boolean fontsetValid) {this.fontsetValid = fontsetValid;}
    public void setShapepathValid(boolean shapepathValid) {this.shapepathValid = shapepathValid;}
    public void setUploadedMapfile(Map upmf) {this.uploadedMapfile = upmf;}
    public void setBadLayers(Vector vec) {this.badLayers = vec;}
}
