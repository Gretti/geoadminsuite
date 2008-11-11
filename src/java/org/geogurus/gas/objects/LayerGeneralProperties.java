/*
 * DataGeneralProperties.java
 *
 * Created on 1 ao�t 2002, 17:03
 */
package org.geogurus.gas.objects;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.Geometry;
import org.geogurus.data.database.OracleDataAccess;
import org.geogurus.data.database.PostgisDataAccess;
import org.geogurus.data.files.ShpDataAccess;
import org.geogurus.data.files.TiffDataAccess;
import org.geogurus.data.webservices.WmsDataAccess;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Points;
import org.geogurus.mapserver.objects.Projection;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.Symbol;
import org.geogurus.mapserver.objects.Web;
import org.geogurus.tools.DataManager;

/**
 * A bean to get information about a given geodata=GeometryClass or mapfile java
 * object (a table or a file) This bean is built when a
 * MG_layer_general_properties*.jsp page is called (for a pg table,
 * 
 * @author nri
 */
public class LayerGeneralProperties implements Serializable {

    transient Logger logger = Logger.getLogger(getClass().getName());
    public static int DEFAULT_SAMPLEDATA_SIZE = 50;
    /** the unique GeometryClass identifier */
    private String layerID;
    /** the unique GeometryClass identifier */
    private String strType;
    /** the SRTEXT projection field, or at least the firts part */
    private String projection;
    /** the vector of array of attributes for this geodata */
    private Vector attributes;
    /** The mapserver URL to display this geodata */
    private String imgUrl;
    /**
     * the size of the quickview for this geodata,
     */
    private int imgX, imgY;
    /** the geometry Class dealt by this bean */
    private DataAccess gc;
    /** the hostList session object */
    private Hashtable hostList;
    /** the hostlist key */
    private String host;
    /** default color scheme for quicklook map */
    private RGB ptCol;
    private RGB lnCol;
    private RGB pgCol;
    /**
     * The root path of the application, set by the JSP page according to
     * servlet context Used to build a valid path to store mapfiles
     */
    private String rootPath;
    /**
     * the session id, passed to this bean to generate user-specific
     * quickviewMap
     */
    private String sessionID;
    /** the type of data */
    private String dsType;

    /** Creates a new instance of DataGeneralProperties */
    public LayerGeneralProperties() {
        attributes = new Vector();
        projection = "";
        imgX = 150;
        imgY = 150;
        imgUrl = "images/noobjects.gif";
        // default colors for quickview map are set here, should put it in the
        // configuration file ?
        ptCol = new RGB(255, 0, 0);
        lnCol = new RGB(0, 0, 0);
        pgCol = new RGB(254, 245, 205);

        dsType = "not_supported";
    }

    /** method to retrieve the geometryClass from the session hostList object */
    public DataAccess getDataAccess() {
        if (gc == null && hostList != null && host != null) {
            Vector vec = (Vector) hostList.get(host);

            for (Iterator iter = vec.iterator(); iter.hasNext();) {
                Datasource ds = (Datasource) iter.next();
                if (ds.getDataList().get(layerID) != null) {
                    gc = (DataAccess) (ds.getDataList().get(layerID));
                    if (!gc.loadMetadata()) {
                        logger
                                .warning("Bean: getGeometryClass: could not get Metadata for GC: "
                                        + gc.getName());
                    }
                    break;
                }
            }
        }
        return gc;
    }

    // set methods
    public void setLayerID(String id) {
        this.layerID = id;
    }

    public void setImgX(int x) {
        this.imgX = x;
    }

    public void setImgY(int y) {
        this.imgY = y;
    }

    public void setHostList(Hashtable hl) {
        this.hostList = hl;
    }

    public void setHost(String h) {
        this.host = h;
    }

    public void setSessionID(String id) {
        sessionID = id;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public void setDsType(String dsType_) {
        dsType = dsType_;
    }

    public void setRootPath(String rp) {
        // check to see if path ends with a /
        if (rp.lastIndexOf(File.separator) != rp.length() - 1) {
            // tmp modiction, but value saved in other variables.
            rp += File.separator;
        }
        this.rootPath = rp;
        rootPath += "msFiles" + File.separator + "tmpMaps" + File.separator
                + "quickview_" + sessionID + ".map";
    }

    // get methods
    public int getImgX() {
        return this.imgX;
    }

    public int getImgY() {
        return this.imgY;
    }

    public String getLayerID() {
        return layerID;
    }

    public String getName() {
        return getDataAccess().getName();
    }

    public String getDbName() {
        return getDataAccess().getOwner().getName();
    }

    public String getHost() {
        return getDataAccess().getHost();
    }

    public Vector getFields() {
        return getDataAccess().getAttributeData() != null ? getDataAccess()
                .getAttributeData() : new Vector();
    }

    public Vector getAttributes() {
        int from = 0;
        int to = DEFAULT_SAMPLEDATA_SIZE;
        attributes = getColumnValues(from, to);
        return attributes != null ? attributes : new Vector();
    }

    public Vector getColumnValues(int from, int to) {
        attributes = null;
        attributes = getDataAccess().getSampleData(from, to);
        return attributes != null ? attributes : new Vector();
    }

    public int getNumRecords() {
        return getDataAccess().getNumGeometries();
    }

    public String getGeoType() {
        return getDataAccess().getOgisType();
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getEscapedRootPath() {
        return rootPath.replace('\\', '/');
    }

    public String getDsType() {
        dsType = getDataAccess().getDatasourceType().displayname();
        return dsType;
    }

    /**
     * Methods to build a valid MapServer URL to display the clicked geodata in
     * the quick view This will construct a valid map, write it to disk in the
     * application temp folder,
     */
    public String getImgURL() {
        gc = getDataAccess();

        if (gc.getNumGeometries() == 0 || 
                (gc.featureType().isNone() &&
                gc.getDatasourceType() != DataAccessType.WMS &&
                gc.getDatasourceType() != DataAccessType.IMG &&
                gc.getDatasourceType() != DataAccessType.TIFF &&
                gc.getDatasourceType() != DataAccessType.ECW)) {
            // no geometries for this table: no map and image
            return "images/noobjects.gif";
        }
        // the mapfile:
        org.geogurus.mapserver.objects.Map m = new org.geogurus.mapserver.objects.Map();
        // nri test: do not set extent, let MS do it
        m.setExtent(new org.geogurus.mapserver.objects.MSExtent(
                gc.getExtent().ll.x, gc.getExtent().ll.y, gc.getExtent().ur.x,
                gc.getExtent().ur.y));
        m.setSize(new Dimension(imgX, imgY));

        // builds new layer and class from scratch to avoid erasing any existing
        // DataAccess layer and classes
        // should construct it
        //Layer msLayer = new Layer();
        Layer msLayer = gc.getDefaultMsLayer();
        msLayer.setName(gc.getName());
        msLayer.setData(gc.getMSLayer().getData());

        if (gc instanceof WmsDataAccess) {
            Projection proj = new Projection();
            proj.addAttribute("\"init=epsg:" + gc.getSRID() + "\"");
            m.setProjection(proj);

            Web web = new Web();
            web.setImagePath(new File(rootPath).getParentFile());
            m.setWeb(web);
        }

        // a default display class for this geoobject
        Class c = new Class();

        msLayer.setStatus(Layer.ON);

        if (gc.getType() == Geometry.POINT
                || gc.getType() == Geometry.MULTIPOINT) {
            msLayer.setType(Layer.POINT);
            c.setColor(ptCol);
            c.setSymbol(ObjectKeys.DEFAULT_POINT_SYMBOL);
            c.setSize(3);

            // the point symbol
            Symbol s = new Symbol();
            s.setName(ObjectKeys.DEFAULT_POINT_SYMBOL);
            s.setType(Symbol.ELLIPSE);
            Points points = new Points();
            Point2D.Float p = new Point2D.Float();
            p.x = 1;
            p.y = 1;
            points.add(p);
            s.setPoints(points);
            m.addSymbol(s);

        } else if (gc.getType() == Geometry.POLYGON
                || gc.getType() == Geometry.MULTIPOLYGON) {
            msLayer.setType(Layer.POLYGON);
            c.setColor(pgCol);
            c.setOutlineColor(new RGB(0, 0, 0));
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
                logger.warning("map is not written !!");
            }
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // construct the mapserver URL: must encode special characters
        // (spaces for instance)
        try {
            // String imgURL = "/cgi-bin/mapserv.exe?mode=map&map=";
            imgUrl = DataManager.getProperty("MAPSERVERURL") + "?mode=map&map=";
            imgUrl += URLEncoder.encode(rootPath, "UTF-8") + "&layer="
                    + gc.getName();
            // must construct unique URL to avoid displaying the cached image
            imgUrl += "&ts=" + (new java.util.Date().getTime());
            return imgUrl;
        } catch (UnsupportedEncodingException uee) {
            return "images/noobjects.gif";
        }
    }

    public String getProjection() {
        projection = getDataAccess().getSRText();
        if (projection == null) {
            return "NULL";
        }
        // Tokenize the full projection string to get only the displayable part
        // :
        // after first square braket and then before first comma for SRText,
        // before
        // the | for proj4 text
        if (projection.indexOf("|") != -1) {
            // a proj4 projection
            return projection.substring(0, projection.indexOf("|"));
        } else {
            StringTokenizer s = new StringTokenizer(this.projection, "\"");
            if (s.countTokens() > 1) {
                s.nextToken();
                return s.nextToken();
            } else {
                return this.projection;
            }
        }
    }

    public String getStrType() {
        if (gc instanceof PostgisDataAccess) {
            strType = "PostGIS";
        } else if (gc instanceof OracleDataAccess) {
            strType = "Oracle Spatial";
        } else if (gc instanceof ShpDataAccess) {
            strType = "Fichier Shape";
        } else if (gc instanceof TiffDataAccess) {
            strType = "Fichier Tiff";
        }
        return this.strType;
    }
}
