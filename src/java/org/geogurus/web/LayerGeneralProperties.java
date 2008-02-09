/*
 * DataGeneralProperties.java
 *
 * Created on 1 ao�t 2002, 17:03
 */

package org.geogurus.web;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.net.*;
import java.awt.Point;
import java.awt.Dimension;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.DataManager;
import org.geogurus.Datasource;
import org.geogurus.GeometryClass;
import org.geogurus.Geometry;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.MapClass;
import org.geogurus.mapserver.objects.Points;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.Symbol;
/**
 * A bean to get information about a given geodata=GeometryClass or mapfile java object (a table or a file)
 * This bean is built when a MG_layer_general_properties*.jsp page is called (for a pg table,
 * @author  nri
 */
public class LayerGeneralProperties {
    
    public static int DEFAULT_SAMPLEDATA_SIZE = 50;
    
    /** the unique GeometryClass identifier */
    private String layerID;
    
    /** the unique GeometryClass identifier */
    private int numRecords;
    
    /** the unique GeometryClass identifier */
    private String strType;
    
    /** the SRTEXT projection field, or at least the firts part */
    private String projection;
    
    /** the type of geographic object: point, polyline, polygon, etc... */
    private String geoType;
    
    /** the vector of columns/fields for this geodata */
    private Vector fields;
    
    /** the vector of array of attributes for this geodata */
    private Vector attributes;
    
    /** The mapserver URL to display this geodata */
    private String imgUrl;
    
    /** the size of the quickview for this geodata,
     */
    private int imgX, imgY;
    
    /** are metadata for the geometryClass retrieved ? */
    private boolean metadataRetrieved;
    
    /** the geometry Class dealt by this bean */
    private GeometryClass gc;
    
    /** the hostList session object */
    private Hashtable hostList;
    
    /** the hostlist key */
    private String host;
    
    /** default color scheme for quicklook map */
    private RGB ptCol;
    private RGB lnCol;
    private RGB pgCol;
    
    /** The root path of the application, set by the JSP page according to servlet context
     * Used to build a valid path to store mapfiles*/
    private String rootPath;
    
    /** the session id, passed to this bean to generate user-specific quickviewMap */
    private String sessionID;
    
    /** the type of data */
    private String dsType;
    
    /** Creates a new instance of DataGeneralProperties */
    public LayerGeneralProperties() {
        numRecords = -1;
        geoType = null;
        fields = new Vector();
        attributes = new Vector();
        projection = "";
        imgX = 150;
        imgY = 150;
        imgUrl = "images/noobjects.gif";
        metadataRetrieved = false;
        // default colors for quickview map are set here, should put it in the configuration file ?
        ptCol = new RGB(255,0,0);
        lnCol = new RGB(0,0,0);
        pgCol = new RGB(254,245,205);
        
        dsType = "not_supported";
    }
    
    /** method to retrieve the geometryClass from the session hostList object */
    public GeometryClass getGeometryClass() {
        if (gc == null && hostList != null && host != null) {
            Vector vec = (Vector)hostList.get(host);
            
            for (Iterator iter = vec.iterator(); iter.hasNext();) {
                Datasource ds = (Datasource)iter.next();
                if (ds.getDataList().get(layerID) != null) {
                    gc = (GeometryClass)(ds.getDataList().get(layerID));
                    if (!gc.getMetadata()) {
                        LogEngine.log("Bean: getGeometryClass: could not get Metadata for GC: " + gc.getName());
                    }
                    return gc;
                }
            }
        }
        return gc;
    }
    
    // set methods
    public void setLayerID(String id) {this.layerID = id;}
    public void setImgX(int x) {this.imgX = x;}
    public void setImgY(int y) {this.imgY = y;}
    public void setHostList(Hashtable hl) {this.hostList = hl;}
    public void setHost(String h) {this.host = h;}
    public void setSessionID(String id) {sessionID = id;}
    public void setProjection(String projection) {this.projection = projection;}
    public void setDsType(String dsType_) {dsType = dsType_;}
    public void setRootPath(String rp) {
        // check to see if path ends with a /
        if (rp.lastIndexOf(File.separator) != rp.length() -1) {
            // tmp modiction, but value saved in other variables.
            rp += File.separator;
        }
        this.rootPath = rp;
        rootPath += "msFiles" + File.separator + "tmpMaps" + File.separator + "quickview_" + sessionID + ".map";
    }
    
    // get methods
    public int getImgX() {return this.imgX;}
    public int getImgY() {return this.imgY;}
    public String getLayerID() {return layerID;}
    public String getName() {return getGeometryClass().getTableName();}
    public String getDbName() {return getGeometryClass().getDatasourceName();}
    public String getHost() {return getGeometryClass().getHost(); }
    public String getDbPort() {return getGeometryClass().getDBPort(); }
    public String getUserName() {return getGeometryClass().getUserName(); }
    public String getUserPwd() {return getGeometryClass().getUserPwd(); }
    public Vector getFields() {return getGeometryClass().getColumnInfo() != null ? getGeometryClass().getColumnInfo() : new Vector();}
    public Vector getAttributes() {
        int from = 0;
        int to = DEFAULT_SAMPLEDATA_SIZE;
        attributes = getColumnValues(from, to);
        return attributes != null ? attributes : new Vector();
    }
    public Vector getColumnValues(int from, int to) {
        attributes = null;
        attributes = getGeometryClass().getSampleData(from, to);
        return attributes != null ? attributes : new Vector();
    }
    public int getNumRecords() {return getGeometryClass().getNumGeometries();}
    public String getGeoType() {return getGeometryClass().getOgisType();}
    public String getRootPath() {return rootPath;}
    public String getDsType() {
        switch (getGeometryClass().getDatasourceType()) {
            case (GeometryClass.ESRIFILECLASS):
                dsType = "shapefile";
                break;
            case (GeometryClass.TIFFCLASS):
                dsType = "tifffile";
                break;
            case (GeometryClass.IMGCLASS):
                dsType = "imgfile";
                break;
            case (GeometryClass.PGCLASS):
                dsType = "dbfile";
                break;
            case (GeometryClass.ORACLASS):
                dsType = "ORA";
                break;
            default:
                dsType = "not_supported";
        }
        return dsType;
    }
    
    /**
     * Methods to build a valid MapServer URL to display the clicked geodata in the quick view
     * This will construct a valid map, write it to disk in the application temp folder,
     */
    public String getImgURL() {
        gc = getGeometryClass();
        
        if (gc.getNumGeometries() == 0 || gc.getColumnName() == null) {
            //no geometries for this table: no map and image
            return "images/noobjects.gif";
        }
        // the mapfile:
        org.geogurus.mapserver.objects.Map m = new org.geogurus.mapserver.objects.Map();
        //nri test: do not set extent, let MS do it
        m.setExtent(new org.geogurus.mapserver.objects.MSExtent(
                gc.getExtent().ll.x,
                gc.getExtent().ll.y,
                gc.getExtent().ur.x,
                gc.getExtent().ur.y));
        m.setSize(new Dimension(imgX, imgY));
        
        // builds new layer and class from scratch to avoid erasing any existing GC layer and classes
        // should construct it
        Layer msLayer = new Layer();
        msLayer.setName(gc.getName());
        // Data specific layer properties
        if (gc.getDatasourceType() == GeometryClass.PGCLASS) {
            msLayer.setConnection("dbname=" + gc.getDatasourceName() +
                    " host=" + gc.getHost() +
                    " port=" + gc.getDBPort() +
                    " user=" + gc.getUserName() +
                    " password=" + gc.getUserPwd());
            msLayer.setConnectionType(Layer.POSTGIS);
            String strData = gc.getColumnName() + " from " + gc.getName();
            if(gc.getUniqueField() != null || gc.getSRText() != null) {
                if(gc.getUniqueField() != null) strData += " USING UNIQUE " + gc.getUniqueField();
                if(gc.getSRText() != null) strData += " USING SRID=" + gc.getSRID();
            }
            msLayer.setData(strData);
        } else if (gc.getDatasourceType() == GeometryClass.ORACLASS) {
            msLayer.setConnection(gc.getUserName() + "/" + gc.getUserPwd() + "@" + gc.getDatasourceName());
            msLayer.setConnectionType(Layer.ORACLESPATIAL);
            String strData = gc.getColumnName() + " from " + gc.getName();
            if(gc.getUniqueField() != null || gc.getSRText() != null) {
                strData += " USING";
                if(gc.getUniqueField() != null) strData += " UNIQUE " + gc.getUniqueField();
                if(gc.getSRText() != null) {
                    strData += " SRID=" + gc.getSRID() + " FILTER";
                }
                if(gc.getDbVersion() == 8) strData += " VERSION 8i";
                else if(gc.getDbVersion() == 9) strData += " VERSION 9i";
                else if(gc.getDbVersion() == 10) strData += " VERSION 10g";
            }
            msLayer.setData(strData);
        } else {
            // other connectiontype, or nothing if file
            msLayer.setData(gc.getDatasourceName() + File.separator + gc.getTableName());
        }
        
        // a default display class for this geoobject
        MapClass c = new MapClass();
        
        msLayer.setStatus(Layer.ON);
        
        if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
            msLayer.setType(Layer.POINT);
            c.setColor(ptCol);
            c.setSymbol("gasdefsympoint");
            c.setSize(3);
            
            // the point symbol
            Symbol s = new Symbol();
            s.setName("gasdefsympoint");
            s.setType(Symbol.ELLIPSE);
            Points p = new Points();
            p.add(new Point(1, 1));
            s.setPoints(p);
            m.addSymbol(s);
            
        } else if (gc.getType() == Geometry.POLYGON || gc.getType() == Geometry.MULTIPOLYGON) {
            msLayer.setType(Layer.POLYGON);
            c.setColor(pgCol);
            c.setOutlineColor(new RGB(0,0,0));
        } else if (gc.getType() == Geometry.RASTER) {
            msLayer.setType(Layer.RASTER);
        } else {
            // default value for all other types, including linestring
            msLayer.setType(Layer.LINE);
            c.setColor(lnCol);
        }
        if (gc.getType() != Geometry.RASTER) {
            // no classes for rasters
            msLayer.addClass(c);
        }
        m.addLayer(msLayer);
        
        BufferedWriter out = null;
        
        try {
            out = new BufferedWriter(new FileWriter(rootPath));
            
            if (!m.saveAsMapFile(out)) {
                LogEngine.log("map is not written !!");
            }
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // construct the mapserver URL: must encode special characters
        // (spaces for instance)
        try {
            //String imgURL = "/cgi-bin/mapserv.exe?mode=map&map=";
            imgUrl= DataManager.getProperty("MAPSERVERURL") + "?mode=map&map=";
            imgUrl += URLEncoder.encode(rootPath, "UTF-8") + "&layer=" + gc.getTableName();
            //must construct unique URL to avoid displaying the cached image
            imgUrl += "&"+ (new java.util.Date().getTime());
            return imgUrl;
        } catch (UnsupportedEncodingException uee) {
            return "images/noobjects.gif";
        }
    }
    
    public String getProjection() {
        projection = getGeometryClass().getSRText();
        if (projection == null) {
            return "NULL";
        }
        //Tokenize the full projection string to get only the displayable part :
        //after first square braket and then before first comma for SRText, before
        // the | for proj4 text
        if (projection.indexOf("|") != -1) {
            // a proj4 projection
            return projection.substring(0, projection.indexOf("|"));
        } else {
            StringTokenizer s = new StringTokenizer(this.projection,"\"");
            if(s.countTokens()>1){
                s.nextToken();
                return s.nextToken();
            } else {
                return this.projection;
            }
        }
    }
    
    public String getStrType() {
        if(getGeometryClass().getDatasourceType() == GeometryClass.PGCLASS){
            strType = "PostGIS";
        } else if(getGeometryClass().getDatasourceType() == GeometryClass.ORACLASS){
            strType = "Oracle Spatial";
        } else if(getGeometryClass().getDatasourceType() == GeometryClass.ESRIFILECLASS){
            strType = "Fichier Shape";
        } else if(getGeometryClass().getDatasourceType() == GeometryClass.TIFFCLASS){
            strType = "Fichier Tiff";
        }
        return this.strType;
    }
}
