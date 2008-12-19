/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * UserMapBean.java
 *
 * Created on 6 ao�t 2002, 14:16
 */
package org.geogurus.gas.objects;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.geogurus.data.Datasource;
import org.geogurus.data.DataAccess;
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
     * The list of datasources (MAP type) for this user session.<br>
     * this list is used when building the first map to use one of these map instead
     * of a generated one.
     */
    private Vector<Datasource> mapfileDatasources;
    /** The list of all GeometryClass or Mapfiles choosen from the catalog. <br>
     * Indexed with the GeometryClass id. */
    private Hashtable<String, DataAccess> userLayerList;
    /** The vector of GC identifiers (String) used to maintain layer order in the application,
     * as Hashtable has no order */
    private Vector<String> userLayerOrder;
    /** the list of GeometryClass or mapfiles identifiers selected by the user from the catalog */
    private String[] userLayerChoice;
    /** the path where the mapfile is put */
    private String mapfilePath;
    /** the mapserver URL, read from the geonline.properties file */
    private String mapserverURL;
    /** the mapfish print service URL, read from the geonline.properties file */
    private String mapfishPrintURL;
    /** the general extent for all layers. */
    private String mapExtent;
    /** the URL of the working gif for kaboum applet, read from the geonline.properties file*/
    private String workingGif;
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

    /** Creates a new instance of UserMapBean */
    public UserMapBean() {
        mapfileDatasources = new Vector<Datasource>();
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

    public Hashtable<String, DataAccess> getUserLayerList() {
        return userLayerList;
    }

    public DataAccess getUserLayerByName(String layerName_) {
        Enumeration<String> enu = userLayerList.keys();
        DataAccess gc = null;
        while (enu.hasMoreElements()) {
            Object key = enu.nextElement();
            gc = userLayerList.get(key);
            if(gc.getName().equalsIgnoreCase(layerName_)) {
                break;
            }
        }
        return gc;
    }
    
    public DataAccess getUserLayer(String id_) {
        return userLayerList.get(id_);
    }

    public Vector<String> getUserLayerOrder() {
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

    public Vector<Datasource> getMapfileDatasources() {
        return mapfileDatasources;
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

    public String getMapfishPrintURL() {
        return mapfishPrintURL;
    }

    ////////////////////////////////////////////////////////////////
    // SET methods
    ////////////////////////////////////////////////////////////////
    public void setUserLayerList(Hashtable<String, DataAccess> userLayerList_) {
        this.userLayerList = userLayerList_;
    }

    public void setUserLayerOrder(Vector<String> vec) {
        userLayerOrder = vec;
    }

    public void setImgX(int x) {
        imgX = x;
    }

    public void setImgY(int y) {
        imgY = y;
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

    public void setMapfishPrintURL(String mapfishPrintURL) {
        this.mapfishPrintURL = mapfishPrintURL;
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
        userLayerList.get(tok.nextToken()).setName(tok.nextToken());
    }

    /** to set the geometryClass user choice
     * recall the setList method if all parameters are available
     */
    public void setUserLayerChoice(String[] ulc) {
        userLayerChoice = ulc;
    }
}
