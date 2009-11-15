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
 * UserMapBeanManager.java
 *
 * Created on 3 janvier 2007, 23:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.geogurus.gas.managers;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.measure.unit.Unit;
import org.geogurus.data.DataAccess;
import org.geogurus.data.Datasource;
import org.geogurus.data.Extent;
import org.geogurus.data.Geometry;
import org.geogurus.data.files.MapFileDatasource;
import org.geogurus.data.webservices.WmsDataAccess;
import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.gas.objects.SymbologyListBean;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.gas.utils.Reprojector;
import org.geogurus.mapserver.objects.Label;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Legend;
import org.geogurus.mapserver.objects.MSExtent;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.OutputFormat;
import org.geogurus.mapserver.objects.Projection;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.ScaleBar;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.mapserver.objects.Web;
import org.geogurus.tools.DataManager;
import org.geogurus.gas.utils.ColorGenerator;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 * @author Administrateur
 */
public class UserMapBeanManager {

    // log Object
    transient Logger logger = null;
    public UserMapBean m_userMapBean;

    /** Creates a new instance of UserMapBeanManager */
    public UserMapBeanManager() {
        m_userMapBean = new UserMapBean();
        logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * build a valid mapfile for this user, saves it into user's tmp dir<br>
     * A new mapfile is built either from scratch (only with layers choosen in
     * the catalog) or by copying an existing map (one or several mapfiles are
     * choosen in the catalog.
     * <p>
     * If one or several mapfiles are choosen in the catalog, all mapfiles will
     * be merged by taking attributes of the first one, then by increasing
     * extent for each new map, and finally by adding all layers of other maps
     * in this map
     * @param colgen_ the ColorGenerator attached to the user session
     * @param services_ the | separated String list of chosen services (ie : google, yahoo or live)
     */
    public void buildFirstUserMapfile(ColorGenerator colgen_, String services_) throws Exception {

        if (m_userMapBean.getUserLayerList() == null) {
            Exception e = new Exception("User Map Bean is null");
            throw e;
        }

        DataAccess gc = null;
        Layer l = null;

        Extent mExt = new Extent();

        if (m_userMapBean.getMapfileDatasources().size() > 0) {
            // if a mapfile is choosen in the catalog, the userMapfile
            // generation is
            // a special case. will use choosen one.
            // TODO: currently, only the first choosen mapfile is taken into
            // account
            Datasource ds = m_userMapBean.getMapfileDatasources().get(0);
            Map map = ds.resource(Map.class).getOrElse(null);
            m_userMapBean.setMapfile(map);
            // checks fontset file
            File fontset = m_userMapBean.getMapfile().getFontSet();
            if (!fontset.exists()) {
                m_userMapBean.getMapfile().setFontSet(
                        new File(map.getSymbolSet().getSymbolSetFile().getParent() + File.separator + fontset.getName()));
            }
            Legend legend = m_userMapBean.getMapfile().getLegend();
            if (legend == null) {
                legend = new Legend();
                legend.setStatus(Legend.ON);
                legend.setKeySize(new Dimension(40, 22));
                legend.setOutlineColor(null);
                Label lab = new Label();
                lab.setType(Label.TRUETYPE);
                lab.setSize(9);
                lab.setFont("arial");
                lab.setColor(new RGB(0, 0, 0));
                legend.setLabel(lab);
            }
            legend.setTemplate(new File(m_userMapBean.getRootPath() + "msFiles" + File.separator + "templates" + File.separator + "legend.html"));
            m_userMapBean.getMapfile().setLegend(legend);
            // removes all unchecked layers (=GC) from the mapfile.
            Collection<DataAccess> col = ds.getDataList().values();
            for (Iterator<DataAccess> iter = col.iterator(); iter.hasNext();) {
                gc = iter.next();
                if (!m_userMapBean.getUserLayerList().contains(gc)) {
                    m_userMapBean.getMapfile().removeLayer(gc.getMSLayer());
                }
            }
            // now adds all other geometryClasses to the mapfile if they are not
            // in the mapfile datasource
            Enumeration<DataAccess> userGC = m_userMapBean.getUserLayerList().elements();
            while (userGC.hasMoreElements()) {
                gc = userGC.nextElement();
                if (!ds.getDataList().contains(gc)) {
                    l = buildFirstLayer(gc, colgen_);
                    m_userMapBean.getMapfile().addLayer(l);
                }
                m_userMapBean.setMapExtent(map.getExtent().toKaboumString());
            }
        } else {
            // creates a full mapfile from scratch
            m_userMapBean.setMapfile(new org.geogurus.mapserver.objects.Map());
            // creates a nice legend
            Legend legend = new Legend();
            legend.setStatus(Legend.ON);
            legend.setKeySize(new Dimension(40, 22));
            legend.setOutlineColor(null);
            Label lab = new Label();
            lab.setType(Label.TRUETYPE);
            lab.setSize(9);
            lab.setFont("arial");
            lab.setColor(new RGB(0, 0, 0));
            legend.setLabel(lab);
            legend.setTemplate(new File(m_userMapBean.getRootPath() + "msFiles" + File.separator + "templates" + File.separator + "legend.html"));
            m_userMapBean.getMapfile().setLegend(legend);

            // creates a little ScaleBar
            ScaleBar sc = new ScaleBar();
            sc.setBackgroundColor(new RGB(255, 255, 255));
            sc.setColor(new RGB(0, 0, 0));
            sc.setIntervals(3);
            sc.setOutlineColor(new RGB(0, 0, 0));
            sc.setSize(new Dimension(100, 3));
            sc.setUnits(ScaleBar.METERS);
            sc.setStatus(ScaleBar.OFF);
            sc.setStyle(0);
            Label scl = new Label();
            scl.setColor(new RGB(0, 0, 0));
            scl.setOutlineColor(new RGB(255, 255, 255));
            scl.setSize(Label.TINY);
            sc.setLabel(scl);
            m_userMapBean.getMapfile().setScaleBar(sc);

            if (DataManager.getProperty(DataManager.MAPSERVERSUPPORTS) != null &&
                    DataManager.getProperty(DataManager.MAPSERVERSUPPORTS).indexOf("AGG") > -1) {
                // Creates an AGG outputformat
                OutputFormat of = new OutputFormat();
                of.setName("png");
                of.setDriver("AGG/PNG");
                of.setImageMode(OutputFormat.RGB);
                of.setTransparent(true);
                m_userMapBean.getMapfile().setOutputFormat(of);
                m_userMapBean.getMapfile().setImageType(Map.PNG);
            }

            // performs common operations on created mapfile:

            // gets all GeometryClass' msLayers and their extents
            // cylcle through the list of layers to add them to the mapfile
            Hashtable hExtent = new Hashtable();
            Hashtable hproj = new Hashtable();
            String attr;
            boolean hasWMS = false;
            for (Enumeration en = m_userMapBean.getUserLayerList().elements(); en.hasMoreElements();) {
                gc = (DataAccess) en.nextElement();
                if (gc instanceof WmsDataAccess && !hasWMS) {
                    hasWMS = true;
                }
                // mapfile extent is increased by each layer's extent
                mExt.add(gc.getExtent());
                l = buildFirstLayer(gc, colgen_);
                l.setTemplate("template_" + gc.getID() + ".html");
                // FIXME :
                // retrieves projection attributes (default to 4326 if none
                // found)
                attr = "\"init=epsg:" + (gc.getSrid() <= 0 ? 4326 : gc.getSrid()) + "\"";
                ArrayList projAttr = new ArrayList(1);
                Projection proj = new Projection();
                projAttr.add(attr);
                proj.setAttributes(projAttr);
                l.setProjection(proj);
                if (hproj.containsKey(proj)) {
                    int nbdsProj = ((Integer) hproj.get(proj)).intValue();
                    hproj.put(proj, new Integer(nbdsProj++));
                } else {
                    hproj.put(proj, new Integer(1));
                }
                hExtent.put(proj, gc.getExtent());
                m_userMapBean.getMapfile().addLayer(l);
            }

            /*
             * Finds the most used projection and sets it for the map If WGS84
             * is used then it will be used as ref proj instead of most used one
             * as world data can't be reprojected to local projection.
             */
            Projection mostUsedProj = new Projection();
            if (services_.length() > 0) {
                attr = "\"init=epsg:900913\"";
                ArrayList projAttr = new ArrayList(1);
                projAttr.add(attr);
                mostUsedProj.setAttributes(projAttr);
            } else {
                int nbDs = -1;
                for (Enumeration e = hproj.keys(); e.hasMoreElements();) {
                    Projection curkey = (Projection) e.nextElement();
                    String curparam = (String) curkey.getAttributes().get(0);
                    String curepsg = curparam.substring(
                            curparam.lastIndexOf(":") + 1, curparam.lastIndexOf("\""));
                    Integer curval = (Integer) hproj.get(curkey);
                    //If 4326 is met then will be the used projection
                    if (curepsg.equalsIgnoreCase("4326")) {
                        mostUsedProj = curkey;
                        break;
                    } else if (curval.intValue() > nbDs) {
                        nbDs = curval.intValue();
                        mostUsedProj = curkey;
                    }
                }
            }

            m_userMapBean.getMapfile().setProjection(mostUsedProj);

            String refParam = (String) mostUsedProj.getAttributes().get(0);
            String refEpsg = refParam.substring(refParam.lastIndexOf(":") + 1, refParam.lastIndexOf("\""));
            CoordinateReferenceSystem crsDest = null;
            //Finds reference system. As 900913 code doesn't exist in hsql DB
            // for now. if 900913 is used, builds from proj4 wkt
            if (!refEpsg.equals("900913")) {
                crsDest = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null).createCoordinateReferenceSystem(refEpsg);
            } else {
                try {
                    crsDest = CRS.parseWKT("PROJCS[\"Google Mercator\",GEOGCS[\"WGS 84\",DATUM[\"World Geodetic System 1984\",SPHEROID[\"WGS 84\",6378137.0,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0.0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.017453292519943295],AXIS[\"Geodetic latitude\",NORTH],AXIS[\"Geodetic longitude\",EAST],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"semi_minor\",6378137.0],PARAMETER[\"latitude_of_origin\",0.0],PARAMETER[\"central_meridian\",0.0],PARAMETER[\"scale_factor\",1.0],PARAMETER[\"false_easting\",0.0],PARAMETER[\"false_northing\",0.0],UNIT[\"m\",1.0],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"900913\"]]]");
                } catch (FactoryException ex2) {
                    Exception e = new Exception(ex2.getMessage());
                    throw e;
                }
            }
            //Sets units according to detected most used projection
            Unit unit = crsDest.getCoordinateSystem().getAxis(0).getUnit();
            if (unit.toString().equalsIgnoreCase("°")) {
                m_userMapBean.getMapfile().setUnits(Map.DD);
            } else {
                m_userMapBean.getMapfile().setUnits(Map.METERS);
            }
            Web web = new Web();
            // If WMS layer found, must set image_path to have it work
            web.setImagePath(new File(m_userMapBean.getRootPath() + File.separator + "msFiles" + File.separator + "tmpMaps" + File.separator));
            web.setImageURL("msFiles/tmpMaps/");
            m_userMapBean.getMapfile().setWeb(web);

            // Calls for recalculation of map extent taking into account ref
            // projection
            Extent calcExtent = null;
            // String dbInfos = "";
            if (hproj.size() > 1 || refEpsg.equals("900913")) {
                calcExtent = Reprojector.returnBBox(mostUsedProj, hExtent);
            }

            // Sets extent to calculated extent if not null
            if (calcExtent != null) {
                mExt = calcExtent;
            }
            // verify the extent: if no layers with geographic object,
            // construct an extent that is supported by mapserver
            if (mExt.ll.x == Double.POSITIVE_INFINITY || mExt.ur.x == Double.NEGATIVE_INFINITY) {
                mExt = new Extent(0, 0, 1, 1);
            }

            //All choosen layers extent must be recalculated regarding to the projection used
            for (Enumeration en = m_userMapBean.getUserLayerList().elements(); en.hasMoreElements();) {
                gc = (DataAccess) en.nextElement();
                if (gc instanceof WmsDataAccess && !hasWMS) {
                    continue;
                }
                attr = "\"init=epsg:" + (gc.getSrid() <= 0 ? 4326 : gc.getSrid()) + "\"";
                ArrayList projAttr = new ArrayList(1);
                Projection proj = new Projection();
                projAttr.add(attr);
                proj.setAttributes(projAttr);
                Extent gcExtent = Reprojector.returnBBox(mostUsedProj, proj, gc.getExtent());
                gc.setRecalculatedExtent(gcExtent);
            }


            // construct the mapextent each time
            m_userMapBean.setMapExtent(mExt.toString());
            m_userMapBean.getMapfile().setExtent(new MSExtent(mExt.ll.x, mExt.ll.y, mExt.ur.x, mExt.ur.y));
            m_userMapBean.getMapfile().setSize(new Dimension(400, 400));
            //Maxsize, so that we may draw large maps (set to 30000, should be a user param)
            m_userMapBean.getMapfile().setMaxSize(30000);
            // sets fontset according to GAS default font list
            m_userMapBean.getMapfile().setFontSet(
                    new File(m_userMapBean.getRootPath() + File.separator + "msFiles" + File.separator + "fonts" + File.separator + "font.list"));

            // construct web tag

            //------------------------------------------------------------------
            // ----
            // symbol management: all symbols coming from an existing mapfile
            // will be
            // transfered to the userSymbolSet object.
            // Each time a new symbol is choosen with the web interface, the
            // userSymbolSet
            // object is updated
            // each time the mapfile is written, the symbol file corresponding
            // to this
            // symbolSet is also written
            // transfers all symbols from mapfile to the userSymbolSet
            if (m_userMapBean.getMapfile().getSymbolSet() != null) {
                if (m_userMapBean.getMapfile().getSymbolSet().getArrayListSymbol().size() > 0) {
                    m_userMapBean.getUserSymbolSet().getArrayListSymbol().addAll(
                            m_userMapBean.getMapfile().getSymbolSet().getArrayListSymbol());
                }
            }
            if (m_userMapBean.getMapfile().getSymbols() != null) {
                m_userMapBean.getUserSymbolSet().getArrayListSymbol().addAll(
                        m_userMapBean.getMapfile().getSymbols());
            }

            // empties the symbols of the map
            m_userMapBean.getMapfile().setSymbols(null);
            // sets the userSymbolSet
            m_userMapBean.getMapfile().setSymbolSet(
                    m_userMapBean.getUserSymbolSet());

            // FIXME : Create default legend and scalebar to avoid NullPointer
            // Should be instanciated while opening configuration jsp instead
            // construct an empty legend to avoid NullPointer
            if (m_userMapBean.getMapfile().getLegend() == null) {
                legend = new Legend();
                m_userMapBean.getMapfile().setLegend(legend);
            }
            // construct an empty scalebar to avoid NullPointer
            if (m_userMapBean.getMapfile().getScaleBar() == null) {
                sc = new ScaleBar();
                m_userMapBean.getMapfile().setScaleBar(sc);
            }
        } // end else: build a map from scratch
        // writes mapfile to the disk
        writeMapFile();
    }

    /**
     * Writes the current mapfile to the mapfilePath, on the disk
     */
    public void writeMapFile() {
        BufferedWriter out = null;
        try {
            // change "\" by "/" to avoid kaboum crashes when accessing Mapfile
            String mfp = URLDecoder.decode(m_userMapBean.getMapfilePath(),
                    "UTF-8");
            mfp = mfp.replace('\\', '/');
            m_userMapBean.setMapfilePath(mfp);
            out = new BufferedWriter(new FileWriter(mfp));
            if (!m_userMapBean.getMapfile().saveAsMapFile(out)) {
                logger.warning("map is not written !!");
            }
            out.flush();
            out.close();
            System.gc();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * update a valid mapfile for this user, saves it into user's tmp dir Allows
     * to set mapfile properties independently<br>
     * This method is called by all actions dealing with map composition: each
     * time a mapfile parameter is modified by user, his corresponding mapfile
     * is regenerated by this method.
     */
    public void generateUserMapfile(ColorGenerator colgen_) {
        if (m_userMapBean.getUserLayerList() == null) {
            return;
        }

        // gets all GeometryClass' msLayers and their extents
        DataAccess gc = null;

        // symbolset management
        m_userMapBean.getMapfile().setSymbolSet(
                m_userMapBean.getUserSymbolSet());

        // must clean all layers from mapfile to avoid accumulation
        if (m_userMapBean.getMapfile().getLayers() != null) {
            m_userMapBean.getMapfile().getLayers().removeAllElements();
        }

        // then re-add layers. Optimization to perform here ?
        for (int i = m_userMapBean.getUserLayerOrder().size() - 1; i >= 0; i--) {
            gc = (DataAccess) m_userMapBean.getUserLayerList().get(
                    (String) m_userMapBean.getUserLayerOrder().get(i));
            // Never uses white color as a default one
            RGB col = colgen_.getNextColor();
            if (col.getBlue() == 255 && col.getGreen() == 255 && col.getRed() == 255) {
                col = colgen_.getNextColor();
            }
            Layer l = gc.getMSLayer(col, false);
            // sets header, footer, template for all layers to allow attribute
            // query
            l.setHeader(new File("../../msheader.html"));
            l.setFooter(new File("../../msfooter.html"));
            // template name is an application generated-file for each layer.
            // this file is generated by this class
            l.setTemplate("template_" + gc.getID() + ".html");
            m_userMapBean.getMapfile().addLayer(l);
        }

        writeMapFile();
    }

    /**
     * Generate a template_<gcid>.html file for each GeometryClass layer
     * contained in the userLayerList hashtable.<br>
     * These files will be used by the application for query attributes.
     */
    public void generateTemplateFiles() {
        try {
            if (!m_userMapBean.getRootPath().endsWith(File.separator)) {
                m_userMapBean.setRootpath(m_userMapBean.getRootPath() + File.separator);
            }
            String templatePath = m_userMapBean.getRootPath() + "msFiles" + File.separator + "tmpMaps" + File.separator;
            BufferedWriter out = null;
            DataAccess gc = null;

            for (Enumeration en = m_userMapBean.getUserLayerList().elements(); en.hasMoreElements();) {
                // discard mapfiles
                gc = (DataAccess) en.nextElement();
                String tf = templatePath + "template_" + gc.getID() + ".html";

                out = new BufferedWriter(new FileWriter(tf));
                // writes header
                out.write("<th colspan='2'>");
                out.write(gc.getName());
                out.write("</th>");
                // loops through all attributes to get their names
                boolean td0 = true;
                for (Iterator iter = gc.getColumnInfo().iterator(); iter.hasNext();) {

                    String attName = ((GeometryClassFieldBean) iter.next()).getName();
                    out.write("<tr>");
                    out.write("<td class='");
                    out.write(td0 ? "td0tiny" : "td2tiny");
                    out.write("'><b>");
                    out.write(attName);
                    out.write("</b></td>");
                    out.write("<td class='");
                    out.write(td0 ? "td0tiny" : "td2tiny");
                    out.write("'><code>");
                    out.write("[" + attName + "]");
                    out.write("</code></td>");
                    out.write("</tr>");

                    td0 = !td0;
                }
                out.write("<tr class='tinynocolor'>");
                out.write("<td colspan='2' align='center'><a href='javascript:centerView([shpminx],[shpminy],[shpmaxx],[shpmaxy])'>");
                out.write("Visuel");
                out.write("</a></td></tr>");

                out.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Create the list of GeometryClasses (=layer) (userLayerList) from hostList
     * and userLayerChoice (= geometryClasses or mapfiles chosen in the catalog<br>
     * If a mapfile is present in a host's Datasource, all its layers will be
     * converted into geometryClasses.<br/> This mapfile will be stored in the
     * list of userMapfiles for later use: UserMapfile creation
     * 
     * Not at all optimized: should store GC directly in hostList, without a
     * datasource object ? Or an object allowing to retrieve GC based on their
     * ids
     */
    public void createUserLayerList(SymbologyListBean gasSymbolList_,
            Hashtable<String, Vector<Datasource>> hostList_) {
        Hashtable<String, DataAccess> userLayerList = new Hashtable<String, DataAccess>();
        Vector<String> userLayerOrder = new Vector<String>();
        boolean noPoint = true;

        Vector<Datasource> datasources = null;
        String host = null;
        String id = null;
        DataAccess gc = null;

        // creates the userSymbolSet here,
        // sets symbolset which name is the mapfile name with .sym extension
        String str_user_session = m_userMapBean.getMapfilePath().substring(0,
                (m_userMapBean.getMapfilePath().lastIndexOf(".")));
        File builtSymFile = null;
        try {
            builtSymFile = new File(URLDecoder.decode(str_user_session, "UTF-8") + ".sym");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

//        m_userMapBean.setUserSymbolSet(new SymbolSet(builtSymFile,
   //             new ArrayList()));
        m_userMapBean.setUserSymbolSet(new SymbolSet(builtSymFile,
                gasSymbolList_.getSymbols()));

        // get host and id from userLayerChoice:
        for (int i = 0; i < m_userMapBean.getUserLayerChoice().length; i++) {
            String[] userLayers = m_userMapBean.getUserLayerChoice()[i].split(",");
            // a user-choosen layer can be "host_name,ds_id" is, or
            // "host_name,gc_id"
            host = userLayers[0];
            id = userLayers[1];

            datasources = hostList_.get(host);
            for (Datasource ds : datasources) {
                if (ds.getId().equals(id) && ds instanceof MapFileDatasource) {
                    // a mapfile datasource selected
                    m_userMapBean.getMapfileDatasources().add(ds);
                    // this datasource is treated
                    break;
                }
                gc = ds.getDataList().get(id);

                if (gc != null) {
                    // load the Metadata for this GeometryClass: If user did not
                    // use quickview, metadata were
                    // not loaded for this GC
                    if (!gc.loadMetadata()) {
                        logger.warning("UserMapBean: createUserLayerList: could not get table Metadata for GC: " + gc.getName());
                    }
                    String objid = gc.getID();
                    userLayerList.put(objid, gc);
                    userLayerOrder.add(objid);

                    if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
                        // adds the gas default symbol point to the user
                        // hashtable
                        if (gasSymbolList_.getSymbolList() != null && noPoint) {
                            // transfers default GAS point symbol from generic
                            // hash to userSymbolSet
                            m_userMapBean.getUserSymbolSet().getArrayListSymbol().add(
                                    gasSymbolList_.getSymbol(ObjectKeys.DEFAULT_POINT_SYMBOL));
                            noPoint = false;
                        }
                    }
                }
            }
        }
        m_userMapBean.setUserLayerList(userLayerList);
        m_userMapBean.setUserLayerOrder(userLayerOrder);
        m_userMapBean.setMapserverURL(DataManager.getProperty("MAPSERVERURL"));
        m_userMapBean.setMapfishPrintURL(DataManager.getProperty("MAPFISHPRINTURL"));
    }

    public UserMapBean getUserMapBean() {
        return m_userMapBean;
    }

    public void setUserMapBean(UserMapBean userMapBean_) {
        m_userMapBean = userMapBean_;
    }

    /**
     * returns the MS layer built for the given GeometryClass, or null if input
     * geometryClass is null
     * 
     * 
     * @param gc
     *            the geometryClass to build layer on
     * @param colgen
     *            the color generator to use to build the layer
     * @return the Mapserver layer object representing this geometry Class
     */
    protected Layer buildFirstLayer(DataAccess gc, ColorGenerator colgen) {
        if (gc == null) {
            return null;
        }
        Layer l = null;
        // Never uses white color as a default one
        RGB col = colgen.getNextColor();
        if (col.getBlue() == 255 && col.getGreen() == 255 && col.getRed() == 255) {
            col = colgen.getNextColor();
        }
        l = gc.getMSLayer(col, false);
        // sets header, footer, template for all layers to allow attribute query
        l.setHeader(new File("../../msheader.html"));
        l.setFooter(new File("../../msfooter.html"));
        // template name is an application generated-file for each layer.
        l.setTemplate("template_" + gc.getID() + ".html");
        return l;
    }
}
