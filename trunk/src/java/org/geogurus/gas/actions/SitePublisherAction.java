/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.GeometryClass;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Legend;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.util.ZipEngine;

/**
 *
 * @author gnguessan
 */
public class SitePublisherAction extends org.apache.struts.action.Action {

    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        //Gets the current session
        HttpSession session = request.getSession(true);

        // current mapfile
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        org.geogurus.mapserver.objects.Map currentMap = umb.getMapfile();
        // some convenient variables for paths
        // Application path
        String appPath = getServlet().getServletContext().getRealPath("");
        // path to mapfile
        String mapFileName = appPath + File.separator + "msFiles" + File.separator + "tmpMaps" + File.separator + "published" + session.getId() + ".map";
        //path to html page
        String htmlFileName = appPath + File.separator + "msFiles" + File.separator + "tmpMaps" + File.separator + "published" + session.getId() + ".html";

        // gets the data path from user input
        String dataPath = "data/";
        if (dataPath.lastIndexOf("/") != dataPath.length() - 1) {
            // adds trailing slash.
            dataPath += "/";
        }

        // writes the HTML page for the site to publish, and the mapfile, with user-choosen parameters:
        writeFiles(htmlFileName, mapFileName, appPath, dataPath, request, session);

        // number of files to publish:
        // 9 images * 3 states + html page + html template pages (2 (header~footer) + 1 per data layer) +
        // kaboum.jar + mapfile + kaboum.js + symbol file + font list + all fonts
        //        int numFiles =  35;
        // locked for the moment: no js, no applet
        int numFiles = 33;
        // computes number of fonts
        File fontFolder = new File(appPath + "/msFiles/fonts/");
        File[] fontList = fontFolder.listFiles();
        numFiles += fontList.length - 2; // font.list is already counted
        // computes number of html templates files (one per vector layer)
        Vector layers = currentMap.getLayers();

        for (Iterator iter = layers.iterator(); iter.hasNext();) {
            Layer l = (Layer) iter.next();

            if (l.getType() != Layer.RASTER) {
                // this layer has attribute information
                numFiles++;
            }
        }

        //Prepares the list of all the files to zip for the publish:
        // an array of full path to the files, an array of relative path for zipentry:
        Vector files = new Vector();
        Vector entries = new Vector();
        // counter to current file entry
        int i = 0;
        // the images
        String[] images = {"zoomin", "zoomout", "pan", "info", "back", "forward", "home", "help", "distance"};

        for (int z = 0; z < images.length; z++) {
            files.add(appPath + "/images/" + images[z] + ".gif");
            entries.add("images/" + images[z] + ".gif");
            files.add(appPath + "/images/" + images[z] + "_over.gif");
            entries.add("images/" + images[z] + "_over.gif");
            files.add(appPath + "/images/" + images[z] + "_clicked.gif");
            entries.add("images/" + images[z] + "_clicked.gif");
        }
        // pix.gif, infolayer and layer.gif
        files.add(appPath + "/images/pix.gif");
        entries.add("images/pix.gif");
        files.add(appPath + "/images/infolayer.gif");
        entries.add("images/infolayer.gif");
        files.add(appPath + "/images/infolayer_clicked.gif");
        entries.add("images/infolayer_clicked.gif");
        files.add(appPath + "/images/layer.gif");
        entries.add("images/layer.gif");
        // the html file
        files.add(htmlFileName);
        entries.add("index.html");
        // the css file
        files.add(appPath + "/styles/layouts.css");
        entries.add("styles/layouts.css");
        // the mapfile: for the moment, put into the data folder, inside the web folder
        files.add(mapFileName);
        entries.add("data/geonline.map");
        // kaboum.jar
        //        files.add(appPath + "/class/kaboum.jar");
        //        entries.add("class/kaboum.jar");
        // kaboum.js
        //        files.add(appPath + "/scripts/kaboum.js");
        //        entries.add("scripts/kaboum.js");
        // symbol
        files.add(appPath + "/msFiles/templates/symbols.sym");
        entries.add("data/symbols.sym");
        // template header and footer
        files.add(appPath + "/msfooter.html");
        entries.add("msfooter.html");
        files.add(appPath + "/msheader.html");
        entries.add("msheader.html");

        // fontlist
        files.add(appPath + "/msFiles/fonts/font.list");
        entries.add("fonts/font.list");
        // the fonts
        for (int j = 0; j < fontList.length; j++) {
            if (fontList[j].isFile() && !fontList[j].getName().equals("font.list")) {
                files.add(fontList[j].getAbsolutePath());
                entries.add("fonts/" + fontList[j].getName());
            }
        }
        // the html templates for info query
        Hashtable layerList = umb.getUserLayerList();
        for (Enumeration en = layerList.elements(); en.hasMoreElements();) {
            GeometryClass gc = (GeometryClass) en.nextElement();

            if (gc.getMSLayer(null, false).getType() != Layer.RASTER) {
                // this GC represents a layer whose attributes can be queried, and
                // for which a template html file was generated, based on its id.
                files.add(appPath + "/msFiles/tmpMaps/template_" + gc.getID() + ".html");
                entries.add("template_" + gc.getID() + ".html");
            }
        }
        // returns zip file
        //creating zip filename and outpustream
        response.setHeader("Content-Disposition", "filename=\"geonline.zip\"");
        response.setContentType("application/x-zip-compressed");

        try {
            OutputStream out = response.getOutputStream();
            ByteArrayOutputStream zipout = new ByteArrayOutputStream();
            ZipEngine.zipToStream(zipout, files, entries);
            response.setContentLength(zipout.size());
            zipout.flush();
            zipout.writeTo(out);
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // removes temporary files
        return null;

    }

    private void writeFiles(String fn, String mfn, String appPath, String dataPath, HttpServletRequest request, HttpSession session) {
        PrintWriter mainOut = null;
        BufferedReader mainIn = null;
        UserMapBean usermapbean = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        ResourceBundle messages = (ResourceBundle) session.getAttribute("messages");
        // the full path to the web folder containing geonline application
        String serverPath = "/tmp";

        //Gets the usermapbean's map
        org.geogurus.mapserver.objects.Map mm = usermapbean.getMapfile();

        //Duplicates the map by creating a new map with the same properties (because Map is not cloneable)
        org.geogurus.mapserver.objects.Map m =
                new org.geogurus.mapserver.objects.Map(mm.getExtent(),
                mm.getFontSet(),
                mm.getImageColor(),
                mm.getImageQuality(),
                mm.getImageType(),
                mm.getLegend(),
                mm.getName(),
                mm.getProjection(),
                mm.getQueryMap(),
                mm.getReferenceMap(),
                mm.getScale(),
                mm.getScaleBar(),
                mm.getShapePath(),
                mm.getSize(),
                mm.getSymbolSet(),
                mm.getUnits(),
                mm.getWeb());

        // duplicates layers:
        Vector vec = new Vector(mm.getLayers());
        m.setLayers(vec);

        //updates fontset, data path and symbolset to reflect published values
        m.setFontSet(new File(serverPath + "/fonts/font.list"));

        /*        try {
        SymbolSet symbolSet = new SymbolSet();
        BufferedReader br = new BufferedReader(new FileReader(new File(serverPath + "/data/symbols.sym")));
        symbolSet.load(br);
        m.setSymbolSet(symbolSet);
        } catch (Exception e) {
        e.printStackTrace();
        }
         */
        m.setShapePath(new File(dataPath));

        // replaces all layer path information to be relative to the datapath
        // from the GAS, all file layers (LOCAL) have a full path: it contains either a / or a \
        // looks for the last one, cut filename and adds it to the dataPath
        String lData = null;
        for (Iterator iter = m.getLayers().iterator(); iter.hasNext();) {
            Layer l = (Layer) iter.next();

            // update path to header and footer:
            l.setFooter(new File("../msfooter.html"));
            l.setHeader(new File("../msheader.html"));

            if (l.getConnectionType() == (byte) 0 || l.getConnectionType() == Layer.LOCAL) {
                // a file layer
                lData = l.getData();
                int idx = lData.lastIndexOf("/");
                if (idx == -1) {
                    idx = lData.lastIndexOf("\\");
                }
                l.setData(dataPath + lData.substring(idx + 1));
                // updates template path
                l.setTemplate("../" + l.getTemplate());
            }
        }

        //Modifies the parameters by setting the usermapbean properties to what has been specified in the parameters
        //Set extent
        String s = "";

        //m.setScaleBar(null);
        m.setReferenceMap(null);

        //Writes the files for publication
        BufferedWriter out = null;
        try {
            // change "\" into "/" to avoid kaboum crashes when accessing Mapfile
            // the user mapfile will be written in the same directory, under the user-choosen name:
            // mapfile name is URL encoded, must decote it:
            mfn = mfn.replace('\\', '/');

            out = new BufferedWriter(new FileWriter(mfn));
            if (!m.saveAsMapFile(out)) {
                LogEngine.log("map has not been written !!!");
            }
            out.close();

            // then, userMap is encoded to produce a valid MS URL
            mfn = URLEncoder.encode(mfn, "UTF-8");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        GeometryClass gc = null;
        // the current layer identifier
        String currentLayer = null;
        Hashtable layerList = usermapbean.getUserLayerList();

        // the template name and path is known: fixed by GAS.
        String tmpl = getServlet().getServletContext().getRealPath("") +
                File.separator +
                "msFiles" +
                File.separator +
                "templates" +
                File.separator +
                "default_tpl.html";

        try {
            // the HTML page:
            mainOut = new PrintWriter(new BufferedWriter(new FileWriter(fn)));
            mainIn = new BufferedReader(new FileReader(tmpl));

            while ((s = mainIn.readLine()) != null) {
                if (s.trim().indexOf("[AV2G") != -1) {
                    // a gas tag to replace
                    if (s.trim().equalsIgnoreCase("[AV2GPAGE]")) {
                        mainOut.println("GAS - exported page");
                    } else if (s.trim().equalsIgnoreCase("[AV2GTitle]")) {
                        mainOut.println("GAS - exported page");
                    } else if (s.trim().equalsIgnoreCase("[AV2GPROJECTION]")) {
                        mainOut.println(usermapbean.getMapfile().getProjection().getAttributes());
                    } else if (s.trim().equalsIgnoreCase("[AV2GBOUNDS]")) {
                        mainOut.println(usermapbean.getMapExtent());
                    } else if (s.trim().equalsIgnoreCase("[AV2GMAPFILEPATH]")) {
                        if (request.getParameter("mapfile_path") != null) {
                            mainOut.println(request.getParameter("mapfile_path"));
                        } else {
                            mainOut.println("/path/to/my/mapfile/geonline.map");
                        }
                    } else if (s.trim().equalsIgnoreCase("[AV2GPATHTOMAPSERVER]")) {
                        if (request.getParameter("mapserver_url") != null) {
                            mainOut.println(request.getParameter("mapserver_url"));
                        } else {
                            mainOut.println("http://localhost/cgi-bin/mapserv?");
                        }
                    } else {
                        // a line to write so as.
                        mainOut.println(s);
                    }
                }
            }
            mainOut.flush();
            mainOut.close();
            mainIn.close();
        } catch (Exception te) {
            te.printStackTrace();
        }
    }
}
