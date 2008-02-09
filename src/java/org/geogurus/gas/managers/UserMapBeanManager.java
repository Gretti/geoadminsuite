/*
 * UserMapBeanManager.java
 *
 * Created on 3 janvier 2007, 23:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.geogurus.gas.managers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import org.geogurus.Datasource;
import org.geogurus.Extent;
import org.geogurus.Geometry;
import org.geogurus.GeometryClass;
import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.mapserver.objects.Label;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Legend;
import org.geogurus.mapserver.objects.MSExtent;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.ScaleBar;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.tools.DataManager;
import org.geogurus.tools.LogEngine;
import org.geogurus.web.ColorGenerator;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.Reprojector;
import org.geogurus.mapserver.objects.Legend;
import org.geogurus.mapserver.objects.Projection;

/**
 *
 * @author Administrateur
 */
public class UserMapBeanManager {

    public UserMapBean m_userMapBean;

    /** Creates a new instance of UserMapBeanManager */
    public UserMapBeanManager() {
        m_userMapBean = new UserMapBean();
    }

    /**
     * build a valid mapfile for this user, saves it into user's tmp dir<br>
     * A new mapfile is built either from scratch (only with layers choosen in the catalog)
     * or by copying an existing map (one or several mapfiles are choosen in the catalog.<p>
     * If one or several mapfiles are choosen in the catalog, all mapfiles will be merged by taking
     * attributes of the first one, then by increasing extent for each new map, and finally by adding
     * all layers of other maps in this map
     */
    public void buildFirstUserMapfile(int screenWidth_, int screenHeight_, ColorGenerator colgen_) throws Exception {

        if (m_userMapBean.getUserLayerList() == null) {
            Exception e = new Exception("User Map Bean is null");
            throw e;
        }

        GeometryClass gc = null;
        Layer l = null;

        // sets the imgX and imgY values according to given screen dimension
        m_userMapBean.setImgX((int) (screenWidth_ / 2.10));
        m_userMapBean.setImgY((int) (screenHeight_ / 1.58));

        if (m_userMapBean.getMapfiles().size() > 0) {
            // if a mapfile is choosen in the catalog, the userMapfile generation is
            // a special case
            boolean firstTime = true;
            Extent mExt = new Extent();
            // cycle through all mapfiles choosen in the catalog:
            for (Iterator iter = m_userMapBean.getMapfiles().iterator(); iter.hasNext();) {
                org.geogurus.mapserver.objects.Map mapfile = (org.geogurus.mapserver.objects.Map) iter.next();
                if (firstTime) {
                    m_userMapBean.setMapfile(mapfile);
                    // extent is taken from the first mapfile
                    mExt = new Extent(mapfile.getExtent().getMinx(), mapfile.getExtent().getMiny(),
                            mapfile.getExtent().getMaxx(), mapfile.getExtent().getMaxy());
                    firstTime = false;
                } else {
                    mExt.add(
                            mapfile.getExtent().getMinx(),
                            mapfile.getExtent().getMiny(),
                            mapfile.getExtent().getMaxx(),
                            mapfile.getExtent().getMaxy());
                }
            }
            // removes all layers from initial mapfile, as they will be added from the
            // userLayerList hashtable
            m_userMapBean.getMapfile().getLayers().removeAllElements();
        } else {
            m_userMapBean.setMapfile(new org.geogurus.mapserver.objects.Map());
            // creates a nice legend
            Legend legend = new Legend();
            legend.setStatus(Legend.ON);
            legend.setKeySize(new Dimension(18, 12));
            Label lab = new Label();
            lab.setType(Label.TRUETYPE);
            lab.setSize(9);
            lab.setFont("arial");
            lab.setColor(new RGB(0, 0, 0));
            legend.setLabel(lab);
            m_userMapBean.getMapfile().setLegend(legend);

            // creates a little ScaleBar
            ScaleBar sc = new ScaleBar();
            sc.setBackgroundColor(new RGB(255, 255, 255));
            sc.setColor(new RGB(0, 0, 0));
            sc.setIntervals(3);
            sc.setOutlineColor(new RGB(0, 0, 0));
            sc.setSize(new Dimension(100, 3));
            sc.setUnits(ScaleBar.METERS);
            sc.setStatus(ScaleBar.EMBED);
            sc.setStyle(0);
            Label scl = new Label();
            scl.setColor(new RGB(0, 0, 0));
            scl.setOutlineColor(new RGB(255, 255, 255));
            scl.setSize(Label.TINY);
            sc.setLabel(scl);
            m_userMapBean.getMapfile().setScaleBar(sc);
        } // end else: build a map from scratch

        // now performs common operations on created mapfile:

        // gets all GeometryClass' msLayers and their extents
        // cylcle through the list of layers to add them to the mapfile
        Extent mExt = new Extent();
        Hashtable hExtent = new Hashtable();
        Hashtable hproj = new Hashtable();
        String attr;
        for (Enumeration en = m_userMapBean.getUserLayerList().elements(); en.hasMoreElements();) {
            gc = (GeometryClass) en.nextElement();
            if (gc.displayInMapserver) {
                // mapfile extent is increased by each layer's extent
                mExt.add(gc.getExtent());
                //Never uses white color as a default one
                RGB col = colgen_.getNextColor();
                if (col.getBlue() == 255 && col.getGreen() == 255 && col.getRed() == 255) {
                    col = colgen_.getNextColor();
                }
                l = gc.getMSLayer(col, false);
                // sets header, footer, template for all layers to allow attribute query
                l.setHeader(new File("../../msheader.html"));
                l.setFooter(new File("../../msfooter.html"));
                // template name is an application generated-file for each layer.
                l.setTemplate("template_" + gc.getID() + ".html");
                // FIXME :
                // retrieves projection attributes (default to 4326 in none found)
                attr = "\"init=epsg:" + (gc.getSRID() == 0 ? 4326 : gc.getSRID()) + "\"";
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
                hExtent.put(proj,gc.getExtent());
                m_userMapBean.getMapfile().addLayer(l);
            }
        }

        /*Finds the most used projection and sets it for the map
         * If WGS84 is used then it will be used asref proj 
         * instead of most used one as world data can't be reprojected to local
         * projection.
         */
        Projection mostUsedProj = new Projection();
        int nbDs = -1;
        for (Enumeration e = hproj.keys(); e.hasMoreElements();) {
            Projection curkey = (Projection) e.nextElement();
            String curparam = (String) curkey.getAttributes().get(0);
            String curepsg = curparam.substring(curparam.lastIndexOf(":") + 1, curparam.lastIndexOf("\""));
            Integer curval = (Integer) hproj.get(curkey);
            if (curepsg.equalsIgnoreCase("4326")) {
                mostUsedProj = curkey;
                break;
            } else if (curval.intValue() > nbDs) {
                mostUsedProj = curkey;
            }
        }
        
        m_userMapBean.getMapfile().setProjection(mostUsedProj);
        
        //Calls for recalculation of map extent taking into account ref projection
        Extent calcExtent = null;
        if(hExtent.size() > 1) calcExtent = Reprojector.returnBBox(mostUsedProj, hExtent);
        
        // verify the extent: if no layers with geographic object, construct an extent
        // that is supported by mapserver
        if (mExt.ll.x == Double.MAX_VALUE || mExt.ur.x == Double.MIN_VALUE) {
            mExt = new Extent(0, 0, 10, 10);
        }
        
        //Sets extent to calculated extent if not null
        if (calcExtent != null) mExt = calcExtent;
        
        // construct the mapextent each time
        m_userMapBean.setMapExtent(mExt.toString());
        m_userMapBean.getMapfile().setExtent(new MSExtent(mExt.ll.x, mExt.ll.y, mExt.ur.x, mExt.ur.y));
        m_userMapBean.getMapfile().setSize(new Dimension(m_userMapBean.getImgX(), m_userMapBean.getImgY()));
        // sets fontset according to GAS default font list
        m_userMapBean.getMapfile().setFontSet(new File(m_userMapBean.getRootPath() + File.separator + "msFiles" + File.separator + "fonts" + File.separator + "font.list"));

        //----------------------------------------------------------------------
        // symbol management: all symbols coming from an existing mapfile will be
        // transfered to the userSymbolSet object.
        // Each time a new symbol is choosen with the web interface, the userSymbolSet
        // object is updated
        // each time the mapfile is written, the symbol file corresponding to this
        // symbolSet is also written

        // transfers all symbols from mapfile to the userSymbolSet
        if (m_userMapBean.getMapfile().getSymbolSet() != null) {
            if (m_userMapBean.getMapfile().getSymbolSet().getArrayListSymbol().size() > 0) {
                m_userMapBean.getUserSymbolSet().getArrayListSymbol().addAll(m_userMapBean.getMapfile().getSymbolSet().getArrayListSymbol());
            }
        }
        if (m_userMapBean.getMapfile().getSymbols() != null) {
            m_userMapBean.getUserSymbolSet().getArrayListSymbol().addAll(m_userMapBean.getMapfile().getSymbols());
        }

        // empties the symbols of the map
        m_userMapBean.getMapfile().setSymbols(null);
        // sets the userSymbolSet
        m_userMapBean.getMapfile().setSymbolSet(m_userMapBean.getUserSymbolSet());
        // construct an empty legend to avoid NullPointer
        Legend legend = new Legend();
        m_userMapBean.getMapfile().setLegend(legend);

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
            String mfp = URLDecoder.decode(m_userMapBean.getMapfilePath(), "UTF-8");
            mfp = mfp.replace('\\', '/');
            m_userMapBean.setMapfilePath(mfp);
            out = new BufferedWriter(new FileWriter(mfp));
            if (!m_userMapBean.getMapfile().saveAsMapFile(out)) {
                LogEngine.log("map is not written !!");
            }
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Generate a template_<gcid>.html file for each GeometryClass layer contained
     * in the userLayerList hashtable.<br>
     * These files will be used by the application for query attributes.
     */
    public void generateTemplateFiles() {
        try {
            if (!m_userMapBean.getRootPath().endsWith(File.separator)) {
                m_userMapBean.setRootpath(m_userMapBean.getRootPath() + File.separator);
            }
            String templatePath = m_userMapBean.getRootPath() + "msFiles" + File.separator + "tmpMaps" + File.separator;
            BufferedWriter out = null;
            GeometryClass gc = null;

            for (Enumeration en = m_userMapBean.getUserLayerList().elements(); en.hasMoreElements();) {
                // discard mapfiles
                gc = (GeometryClass) en.nextElement();
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
     * Create the list of GeometryClasses (=layer) (userLayerList)
     * from hostList and userLayerChoice (= geometryClasses or mapfiles chosen in
     * the catalog<br>
     * If a mapfile is present in a host's Datasource, all its layers will be converted
     * into geometryClasses.<br>
     * This mapfile will be stored in the list of userMapfiles for later use:
     * UserMapfile creation
     *
     * Not at all optimized: should store GC directly in hostList, without a datasource object ?
     * Or an object allowing to retrieve GC based on their ids
     */
    public void createUserLayerList(Hashtable gasSymbolList_, Hashtable hostList_) {
        Hashtable userLayerList = new Hashtable();
        Vector userLayerOrder = new Vector();
        Vector mapfiles = new Vector();
        boolean noPoint = true;

        Vector vec = null;
        Vector layers = null;
        Datasource ds = null;
        Object obj = null;
        String host = null;
        String id = null;
        StringTokenizer tok = null;
        GeometryClass gc = null;

        // creates the userSymbolSet here,
        // sets symbolset which name is the mapfile name with .sym extension
        String str_user_session = m_userMapBean.getMapfilePath().substring(0, (m_userMapBean.getMapfilePath().lastIndexOf(".")));
        File builtSymFile = null;
        try {
            builtSymFile = new File(URLDecoder.decode(str_user_session, "UTF-8") + ".sym");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        m_userMapBean.setUserSymbolSet(new SymbolSet(builtSymFile, new ArrayList()));

        // get host and id from userLayerChoice:
        for (int i = 0; i < m_userMapBean.getUserLayerChoice().length; i++) {
            tok = new StringTokenizer(m_userMapBean.getUserLayerChoice()[i], ",");
            host = tok.nextToken();
            id = tok.nextToken();

            vec = (Vector) hostList_.get(host);
            for (Iterator iter = vec.iterator(); iter.hasNext();) {
                ds = (Datasource) iter.next();
                obj = ds.getDataList().get(id);

                if (obj != null) {
                    if (obj instanceof GeometryClass) {
                        // load the Metadata for this GeometryClass: If user did not use quickview, metadata were
                        // not loaded for this GC
                        if (!((GeometryClass) obj).getMetadata()) {
                            LogEngine.log("UserMapBean: createUserLayerList: could not get table Metadata for GC: " + ((GeometryClass) obj).getName());
                        }
                        String objid = ((GeometryClass) obj).getID();
                        userLayerList.put(objid, obj);
                        userLayerOrder.add(objid);

                        if (((GeometryClass) obj).getType() == Geometry.POINT || ((GeometryClass) obj).getType() == Geometry.MULTIPOINT) {
                            // adds the gas default symbol point to the user hashtable
                            if (gasSymbolList_ != null && noPoint) {
                                // transfers default GAS point symbol from generic hash to userSymbolSet
                                m_userMapBean.getUserSymbolSet().getArrayListSymbol().add(gasSymbolList_.get("gasdefsympoint"));
                                noPoint = false;
                            }
                        }
                    } else if (obj instanceof org.geogurus.mapserver.objects.Map) {
                        // stores this mapfile for later use
                        mapfiles.add(obj);
                        // extracts its layers and put them in the userLayerList
                        layers = ds.getGeometryClasses(id);
                        if (layers != null) {
                            for (Iterator iter2 = layers.iterator(); iter2.hasNext();) {
                                gc = (GeometryClass) iter2.next();
                                userLayerList.put(gc.getID(), gc);
                                userLayerOrder.add(gc.getID());
                            }
                        }
                    }
                    break;// no more GC or mapfile with this id can be found
                }
            }
        }
        m_userMapBean.setUserLayerList(userLayerList);
        m_userMapBean.setUserLayerOrder(userLayerOrder);

        // generates template files for each choosen layers, in the tmpMaps directory
        // can now build the map for this application
        // get the mapserver URL
        m_userMapBean.setMapserverURL(DataManager.getProperty("MAPSERVERURL"));
    }

    /** update a valid mapfile for this user, saves it into user's tmp dir
     * Allows to set mapfile properties independently<br>
     * This method is called by all actions dealing with map composition: each time
     * a mapfile parameter is modified by user, his corresponding mapfile is regenerated
     * by this method.
     */
    public void generateUserMapfile(ColorGenerator colgen_) {
        if (m_userMapBean.getUserLayerList() == null) {
            return;
        }

        // gets all GeometryClass' msLayers and their extents
        Extent e = new Extent();
        GeometryClass gc = null;

        // symbolset management
        m_userMapBean.getMapfile().setSymbolSet(m_userMapBean.getUserSymbolSet());

        // must clean all layers from mapfile to avoid accumulation
        if (m_userMapBean.getMapfile().getLayers() != null) {
            m_userMapBean.getMapfile().getLayers().removeAllElements();
        }

        // then re-add layers. Optimization to perform here ?
        for (int i = m_userMapBean.getUserLayerOrder().size() - 1; i >= 0; i--) {
            gc = (GeometryClass) m_userMapBean.getUserLayerList().get((String) m_userMapBean.getUserLayerOrder().get(i));
            if (gc.displayInMapserver) {
                e.add(gc.getExtent());
                //Never uses white color as a default one
                RGB col = colgen_.getNextColor();
                if (col.getBlue() == 255 && col.getGreen() == 255 && col.getRed() == 255) {
                    col = colgen_.getNextColor();
                }
                Layer l = gc.getMSLayer(col, false);
                // sets header, footer, template for all layers to allow attribute query
                l.setHeader(new File("../../msheader.html"));
                l.setFooter(new File("../../msfooter.html"));
                // template name is an application generated-file for each layer.
                // this file is generated by this class
                l.setTemplate("template_" + gc.getID() + ".html");
                m_userMapBean.getMapfile().addLayer(l);
            }
        }
        // verify the extent: if no layers with geographic object, construct an extent
        // that is supported by mapserver
        if (e.ll.x == Double.MAX_VALUE || e.ur.x == Double.MIN_VALUE) {
            e = new Extent(0, 0, 10, 10);
        }
        // construct the mapextent each time
        m_userMapBean.setMapExtent(e.toString());

        writeMapFile();
    }

    /**
     *  Method to return the list of objects for a geometryClass, but with decimal digits cut
     * by the specified amount, as specified in geonline.properties. (-1 means no digit cut)
     */
    public String getCutObject(String obj) {
        StringBuffer res = new StringBuffer();

        int num = -1;
        if (DataManager.getProperty("CUT_OBJECT_COORDINATES") != null) {
            try {
                num = new Integer(DataManager.getProperty("CUT_OBJECT_COORDINATES")).intValue();
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                num = -1;
            }
        }
        if (num == -1) {
            return obj;
        }
        // parses input string to remove decimal digits

        return res.toString();
    }

    public UserMapBean getUserMapBean() {
        return m_userMapBean;
    }

    public void setUserMapBean(UserMapBean userMapBean_) {
        m_userMapBean = userMapBean_;
    }
}
