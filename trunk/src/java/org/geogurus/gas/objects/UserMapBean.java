/*
 * UserMapBean.java
 *
 * Created on 6 ao�t 2002, 14:16
 */
package org.geogurus.gas.objects;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import org.geogurus.Extent;
import org.geogurus.GeometryClass;
import org.geogurus.mapserver.objects.SymbolSet;

/**
 * This bean represents a user map: a list of GeometryClass or geographic layers.
 * A Mapfile containing all the GeometryClasses will be built.
 * If some mapfiles are choosen in the catalog, all the layers of these mapfiles
 * will be added to the current user mapfile
 * Some kaboum applet parameters are also stored here.
 *
 * The MC_result_map.jsp page uses this bean to present a nice and useful map to the user.
 * Initialisation phase should construct the map, prepare kaboum applet code, etc...<p>
 *
 * This class is roughly equivalent to the com.scot.geonline.UserParameters, but simplified
 * for the geonline GAS context.<p>
 *
 * If several mapfiles are choosen in the catalog, this class will duplicate the first mapfile
 * and add to it all the layers of all other mapfiles.<br>
 * Extent of the duplicate mapfile will be increased with extents of all other mapfiles.<p>
 *
 * To work, this bean must receive the list of all GeometryClasses or Mapfiles constructed by the catalog
 * AND the list of user-choosen GeometryClasses or mapfiles, passed as a request parameter from previous page.
 * The simplest way is maybe to create this bean in the framset containing result_map and display_layer_properties,
 * Then result_map will be able to access it.
 * @author  nri
 */
public class UserMapBean implements Serializable {

    /**
     * The current mapfile for this user session.<br>
     * It could be the copy of a mapfile choosen in the catalog.
     */
    private org.geogurus.mapserver.objects.Map mapfile;
    /**
     * The list of mapfiles for this user session.<br>
     * this list is used when building the first map.
     */
    private Vector mapfiles;
    /** The list of all GeometryClass or Mapfiles choosen from the catalog. <br>
     * Indexed with the GeometryClass id. */
    private Hashtable userLayerList;
    /** The vector of GC identifiers used to maintain layer order in the application,
     * as Hashtable has no order */
    private Vector userLayerOrder;
    /** the list of GeometryClass or mapfiles identifiers selected by the user from the catalog */
    private String[] userLayerChoice;
    /** the path where the mapfile is put */
    private String mapfilePath;
    /** the mapserver URL, read from the geonline.properties file */
    private String mapserverURL;
    /** the general extent for all layers. */
    private String mapExtent;
    /** same as above, but as Extent object: must change that for a clean extent management */
    private Extent mExt;
    /** the URL of the working gif for kaboum applet, read from the geonline.properties file*/
    private String workingGif;
    /** the client screen width and height */
    private int screenWidth,  screenHeight;
    /** the kaboum width and height, derived from the mapfile dimensions */
    private int imgX;
    private int imgY;
    /** the current layer display name, if user changed it on the left frame
     * This display name is composed of:
     * <layerid>|layerDisplayName>
     *It is a request parameter give to the page by the MC_display_layer_properties
     */
    private String layerDisplayName;
    /** The root path of the application, set by the JSP page according to servlet context
     * Used to build a valid path to store mapfiles
     */
    private String rootPath;
    /** the updated kaboum geometry: <classname>|<id>|<wkt>
     * Allows JSP pages to pass this geometry back to kaboum
     */
    private String updatedGeometry;
    /** the mapfile target projection, as set by the user with the MC_geonline_gest_proj
     * If this variable is set to null, no projection tag will be written in the mapfile
     */
    private String targetProjection;
    /** The symbol set object for this user. <br>
     * All mapfile symbols will be put in this symbolset.
     * => user mapfiles will be linked to a symbol file.
     */
    private SymbolSet userSymbolSet = null;
    /** */
    private boolean noPoint = true;

    /** Creates a new instance of UserMapBean */
    public UserMapBean() {
        mExt = new Extent();
        mapfiles = new Vector();
    }

    ////////////////////////////////////////////////////////////////
    // GET methods
    ////////////////////////////////////////////////////////////////
    public String getMapfilePath() {
        // must encode mapfile path for kaboum:
        // kaboum should encode this path itself when building the mapserver URL
        try {
            return URLEncoder.encode(mapfilePath, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return mapfilePath;
        }
    }

    public Hashtable getUserLayerList() {
        return userLayerList;
    }

    public GeometryClass getUserLayer(String layerName_) {
        return (GeometryClass) userLayerList.get(layerName_);
    }

    public Vector getUserLayerOrder() {
        return userLayerOrder;
    }

    public String getWorkingGif() {
        return workingGif;
    }

    public String getMapserverURL() {
        return mapserverURL;
    }

    public String getMapExtent() {
        return mapExtent;
    }

    public org.geogurus.mapserver.objects.Map getMapfile() {
        return mapfile;
    }

    public Vector getMapfiles() {
        return mapfiles;
    }

    public String getUpdatedGeometry() {
        return updatedGeometry;
    }

    public int getImgY() {
        return imgY;
    }

    public int getImgX() {
        return imgX;
    }

    public String getRootPath() {
        return this.rootPath;
    }

    public String getTargetProjection() {
        return targetProjection;
    }

    public SymbolSet getUserSymbolSet() {
        return userSymbolSet;
    }

    public String[] getUserLayerChoice() {
        return this.userLayerChoice;
    }

    ////////////////////////////////////////////////////////////////
    // SET methods
    ////////////////////////////////////////////////////////////////
    public void setUserLayerList(Hashtable userLayerList_) {
        this.userLayerList = userLayerList_;
    }

    public void setUserLayerOrder(Vector vec) {
        userLayerOrder = vec;
    }

    public void setImgX(int x) {
        imgX = x;
    }

    public void setImgY(int y) {
        imgY = y;
    }

    public void setScreenWidth(int w) {
        screenWidth = w;
    }

    public void setScreenHeight(int h) {
        screenHeight = h;
    }

    public void setMapfile(org.geogurus.mapserver.objects.Map map) {
        mapfile = map;
    }

    public void setMapfilePath(String mapfilePath_) {
        mapfilePath = mapfilePath_;
    }

    public void setMapUnits(byte units) {
        mapfile.setUnits(units);
    }

    public void setMapserverURL(String msurl) {
        mapserverURL = msurl;
    }

    public void setUpdatedGeometry(String geom) {
        updatedGeometry = geom;
    }

    public void setTargetProjection(String targetProj) {
        targetProjection = targetProj;
    }

    public void setUserSymbolSet(SymbolSet ss) {
        userSymbolSet = ss;
    }

    public void setMapExtent(Vector vect) {
        double xmin;
        double ymin;
        double xmax;
        double ymax;
        try {
            xmin = Double.parseDouble((String) vect.get(0));
            ymin = Double.parseDouble((String) vect.get(1));
            xmax = Double.parseDouble((String) vect.get(2));
            ymax = Double.parseDouble((String) vect.get(3));
            mapfile.setExtent(new org.geogurus.mapserver.objects.MSExtent(xmin,ymin, xmax, ymax));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
    }

    public void setMapExtent(String extent_) {
        mapExtent = extent_;
    }

    public void setRootpath(String rp_) {
        rootPath = rp_;
    }

    /** Parses the given id|name string to set layer display name accordingly */
    public void setLayerDisplayName(String dn) {
        layerDisplayName = dn;
        StringTokenizer tok = new StringTokenizer(layerDisplayName, "|");
        ((GeometryClass) userLayerList.get(tok.nextToken())).setName(tok.nextToken());
    }

    /** to set the geometryClass user choice
     * recall the setList method if all parameters are available
     */
    public void setUserLayerChoice(String[] ulc) {
        userLayerChoice = ulc;
    }
}
