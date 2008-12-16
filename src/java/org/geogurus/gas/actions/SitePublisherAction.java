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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.MsLayer;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.tools.util.ZipEngine;

/**
 * 
 * @author gnguessan
 */
public class SitePublisherAction extends org.apache.struts.action.Action {
    transient Logger logger = Logger.getLogger(getClass().getName());

    private static HashMap<String, String> listOlTools() {
        HashMap<String, String> oltools = new HashMap();
        oltools.put("OL_EditingToolbar",
                "new OpenLayers.Control.EditingToolbar()");
        oltools.put("OL_LayerSwitcher",
                "new OpenLayers.Control.LayerSwitcher()");
        oltools.put("OL_MousePosition",
                "new OpenLayers.Control.MousePosition()");
        oltools.put("OL_MouseToolbar", "new OpenLayers.Control.MouseToolbar()");
        oltools.put("OL_NavToolbar", "new OpenLayers.Control.NavToolbar()");
        oltools.put("OL_Navigation", "new OpenLayers.Control.Navigation()");
        oltools.put("OL_OverviewMap", "new OpenLayers.Control.OverviewMap()");
        oltools.put("OL_PanZoomBar", "new OpenLayers.Control.PanZoomBar()");
        oltools.put("OL_Permalink", "new OpenLayers.Control.Permalink()");
        oltools.put("OL_Scale", "new OpenLayers.Control.Scale()");
        return oltools;
    }

    /**
     * This is the action called from the Struts framework.
     * 
     * @param mapping
     *            The ActionMapping used to select this instance.
     * @param form
     *            The optional ActionForm bean for this request.
     * @param request
     *            The HTTP Request we are processing.
     * @param response
     *            The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // Gets the current session
        HttpSession session = request.getSession(true);

        // current mapfile
        UserMapBean umb = (UserMapBean) session
                .getAttribute(ObjectKeys.USER_MAP_BEAN);
        org.geogurus.mapserver.objects.Map currentMap = umb.getMapfile();
        // some convenient variables for paths
        // Application path
        String appPath = getServlet().getServletContext().getRealPath("");
        // path to mapfile
        String mapFileName = appPath + File.separator + "msFiles"
                + File.separator + "tmpMaps" + File.separator + "published"
                + session.getId() + ".map";
        // path to html page
        String htmlFileName = appPath + File.separator + "msFiles"
                + File.separator + "tmpMaps" + File.separator + "published"
                + session.getId() + ".html";

        // gets the data path from user input
        String dataPath = "data/";
        if (dataPath.lastIndexOf("/") != dataPath.length() - 1) {
            // adds trailing slash.
            dataPath += "/";
        }

        // writes the HTML page for the site to publish, and the mapfile, with
        // user-choosen parameters:
        writeFiles(htmlFileName, mapFileName, appPath, dataPath, request,
                session);

        // number of files to publish:
        // 9 images * 3 states + html page + html template pages (2
        // (header~footer) + 1 per data layer) +
        // kaboum.jar + mapfile + kaboum.js + symbol file + font list + all
        // fonts
        // int numFiles = 35;
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

        // Prepares the list of all the files to zip for the publish:
        // an array of full path to the files, an array of relative path for
        // zipentry:
        Vector files = new Vector();
        Vector entries = new Vector();
        // counter to current file entry
        // the images & css file
        String[] images = { "zoomin.png", "zoomout.png", "mouse.png",
                "printer.png", "world.png", "mapfish.css", "block-bg.gif",
                "block-top.gif", "block-bottom.gif" };

        for (int z = 0; z < images.length; z++) {
            files.add(appPath + "/styles/" + images[z]);
            entries.add("styles/" + images[z]);
        }
        files.add(appPath + "/images/layers.png");
        entries.add("images/layers.png");
        // the html file
        files.add(htmlFileName);
        entries.add("index.html");
        // the mapfile: for the moment, put into the data folder, inside the web
        // folder
        files.add(mapFileName);
        entries.add("data/geonline.map");

        // Mapfish-OL-Ext
        File folder = new File(appPath + "/scripts/refexportfiles");
        File fileList[] = listFilesAsArray(folder, null, true);
        String refPath = "";

        try {
            for (int fz = 0; fz < fileList.length; fz++) {
                if (fileList[fz].isFile()) {
                    refPath = fileList[fz].getAbsolutePath().substring(
                            folder.getAbsolutePath()
                                    .lastIndexOf(File.separator)
                                    + "refexportfiles".length() + 1);
                    files.add(fileList[fz].getAbsolutePath());
                    entries.add("scripts" + refPath);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // kaboum.jar
        // files.add(appPath + "/class/kaboum.jar");
        // entries.add("class/kaboum.jar");
        // kaboum.js
        // files.add(appPath + "/scripts/kaboum.js");
        // entries.add("scripts/kaboum.js");
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
            if (fontList[j].isFile()
                    && !fontList[j].getName().equals("font.list")) {
                files.add(fontList[j].getAbsolutePath());
                entries.add("fonts/" + fontList[j].getName());
            }
        }

        // the html templates for info query
        Hashtable layerList = umb.getUserLayerList();
        for (Enumeration en = layerList.elements(); en.hasMoreElements();) {
            DataAccess gc = (DataAccess) en.nextElement();

            if (gc.getMSLayer(null, false).getType() != Layer.RASTER) {
                // this GC represents a layer whose attributes can be queried,
                // and
                // for which a template html file was generated, based on its
                // id.
                files.add(appPath + "/msFiles/tmpMaps/template_" + gc.getID()
                        + ".html");
                entries.add("template_" + gc.getID() + ".html");
            }
        }
        // returns zip file
        // creating zip filename and outpustream
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

    private void writeFiles(String fn, String mfn, String appPath,
            String dataPath, HttpServletRequest request, HttpSession session) {
        PrintWriter mainOut = null;
        BufferedReader mainIn = null;
        UserMapBean usermapbean = (UserMapBean) session
                .getAttribute(ObjectKeys.USER_MAP_BEAN);
        ResourceBundle messages = (ResourceBundle) session
                .getAttribute("messages");

        // Gets the usermapbean's map
        org.geogurus.mapserver.objects.Map mm = usermapbean.getMapfile();

        // Duplicates the map by creating a new map with the same properties
        // (because Map is not cloneable)
        org.geogurus.mapserver.objects.Map m = new org.geogurus.mapserver.objects.Map(
                mm.getAngle(), mm.getConfig(), mm.getDataPattern(), mm
                        .getDebug(), mm.getExtent(), mm.getFontSet(), mm
                        .getImageColor(), mm.getImageQuality(), mm
                        .getImageType(), mm.getLegend(), mm.getMaxSize(), mm
                        .getName(), mm.getProjection(), mm.getQueryMap(), mm
                        .getReferenceMap(), mm.getScale(), mm.getScaleBar(), mm
                        .getShapePath(), mm.getSize(), mm.getSymbolSet(), mm
                        .getTemplatePattern(), mm.getUnits(), mm.getWeb(), mm
                        .getInclude(), mm.getOutputFormat());

        // duplicates layers:
        Vector vec = new Vector(mm.getLayers());
        m.setLayers(vec);

        // updates fontset, data path and symbolset to reflect published values
        m.setFontSet(new File("../fonts/font.list"));

        try {
            SymbolSet symbolSet = new SymbolSet(new File("symbols.sym"));
            m.setSymbolSet(symbolSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (request.getParameter("keepAbsoluteDataPath") == null) {

            m.setShapePath(new File(dataPath));

            // replaces all layer path information to be relative to the datapath
            // from the GAS, all file layers (LOCAL) have a full path: it contains
            // either a / or a \
            // looks for the last one, cut filename and adds it to the dataPath
            String lData = null;
            for (Iterator iter = m.getLayers().iterator(); iter.hasNext();) {
                Layer l = (Layer) iter.next();

                // update path to header and footer:
                l.setFooter(new File("../msfooter.html"));
                l.setHeader(new File("../msheader.html"));

                if (l.getConnectionType() == null
                        || l.getConnectionType() == MsLayer.LOCAL) {
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
        }
        // Modifies the parameters by setting the usermapbean properties to what
        // has been specified in the parameters
        // Set extent
        String s = "";

        // m.setScaleBar(null);
        m.setReferenceMap(null);

        // Writes the files for publication
        BufferedWriter out = null;
        try {
            // change "\" into "/" to avoid kaboum crashes when accessing
            // Mapfile
            // the user mapfile will be written in the same directory, under the
            // user-choosen name:
            // mapfile name is URL encoded, must decote it:
            mfn = mfn.replace('\\', '/');

            out = new BufferedWriter(new FileWriter(mfn));
            if (!m.saveAsMapFile(out)) {
                logger.warning("map has not been written !!!");
            }
            out.close();

            // then, userMap is encoded to produce a valid MS URL
            mfn = URLEncoder.encode(mfn, "UTF-8");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        DataAccess gc = null;

        // the template name and path is known: fixed by GAS.
        String tmpl = getServlet().getServletContext().getRealPath("")
                + File.separator + "msFiles" + File.separator + "templates"
                + File.separator + "default_tpl.html";

        try {
            // the HTML page:
            mainOut = new PrintWriter(new BufferedWriter(new FileWriter(fn)));
            mainIn = new BufferedReader(new FileReader(tmpl));

            while ((s = mainIn.readLine()) != null) {
                if (s.trim().indexOf("[AV2G") != -1) {
                    // a gas tag to replace
                    if (s.trim().equalsIgnoreCase("[AV2GPAGE]")) {
                        mainOut
                                .println("<title>Mapfish generated page</title>");
                    } else if (s.trim().equalsIgnoreCase("[AV2GTITLE]")) {
                        mainOut
                                .println("<h1 style='align:center'>Resulting Page</h1>");
                    } else if (s.trim().equalsIgnoreCase("[AV2GPROJECTION]")) {
                        String proj = (String) usermapbean.getMapfile()
                                .getProjection().getAttributes().get(0);
                        proj = proj.substring(proj.lastIndexOf("=") + 1, proj
                                .length() - 1);
                        mainOut.println("var proj = '" + proj + "';\n");
                    } else if (s.trim().equalsIgnoreCase("[AV2GBOUNDS]")) {
                        mainOut.println("var bounds = new OpenLayers.Bounds("
                                + usermapbean.getMapExtent() + ");\n");
                    } else if (s.trim().equalsIgnoreCase("[AV2GMAPFILEPATH]")) {
                        if (request.getParameter("mapfile_path") != null) {
                            mainOut.println("var mapfile_path='"
                                    + request.getParameter("mapfile_path")
                                    + "';\n");
                        } else {
                            mainOut
                                    .println("var mapfile_path='/path/to/my/mapfile/geonline.map';\n");
                        }
                    } else if (s.trim().equalsIgnoreCase(
                            "[AV2GPATHTOMAPSERVER]")) {
                        if (request.getParameter("mapserver_url") != null) {
                            String msurl = request
                                    .getParameter("mapserver_url");
                            mainOut
                                    .println("var path_to_mapserver='" + msurl
                                            + (msurl.endsWith("?") ? "" : "?")
                                            + "';\n");
                        } else {
                            mainOut
                                    .println("var path_to_mapserver='http://localhost/cgi-bin/mapserv?';\n");
                        }
                    } else if (s.trim().equalsIgnoreCase("[AV2GOLTOOLS]")) {
                        if (request.getParameter("selected_components") != null) {
                            StringTokenizer tok = new StringTokenizer(request
                                    .getParameter("selected_components"), "|");
                            String jsTools = "";
                            while (tok.hasMoreTokens()) {
                                String olctrl = tok.nextToken();
                                if (olctrl.startsWith("OL_")) {
                                    jsTools += "map.addControl("
                                            + listOlTools().get(olctrl)
                                            + ",null);\n";
                                }
                            }
                            mainOut.println(jsTools);
                        }
                    } else if (s.trim().equalsIgnoreCase("[AV2GLAYERS]")) {
                        StringBuilder strLayers = new StringBuilder();
                        for (Iterator ite = usermapbean.getUserLayerOrder()
                                .iterator(); ite.hasNext();) {
                            gc = usermapbean.getUserLayer((String) ite.next());
                            strLayers.append("myLayers.push('" + gc.getName()
                                    + "');\n");
                        }
                        mainOut.println(strLayers.toString());

                    } else if (s.trim().equalsIgnoreCase("[AV2GMFTREE]")) {
                        StringBuilder strTree = new StringBuilder();
                        if (request.getParameter("selected_components") != null) {
                            StringTokenizer tok = new StringTokenizer(request
                                    .getParameter("selected_components"), "|");
                            while (tok.hasMoreTokens()) {
                                String mfctrl = tok.nextToken();
                                if (mfctrl.equalsIgnoreCase("MF_LayerTree")) {
                                    strTree.append("var pchildren = [];\n");
                                    for (Iterator ite = usermapbean
                                            .getUserLayerOrder().iterator(); ite
                                            .hasNext();) {
                                        gc = usermapbean
                                                .getUserLayer((String) ite
                                                        .next());
                                        strTree.append("pchildren.push({\n");
                                        strTree.append("\tid: '" + gc.getID()
                                                + "',\n");
                                        strTree.append("\ttext: '"
                                                + gc.getName() + "',\n");
                                        strTree.append("\tleaf: true,\n");
                                        strTree.append("\tlayerName: 'myGroup:"
                                                + gc.getName() + "',\n");
                                        strTree.append("\tchecked: true,\n");
                                        strTree
                                                .append("\ticon: 'images/layers.png'\n");
                                        strTree.append("});\n");
                                    }
                                    strTree.append("var pmodel = [{\n");
                                    strTree.append("\ttext: 'Layer tree',\n");
                                    strTree.append("\texpanded: true,\n");
                                    strTree.append("\tchecked: true,\n");
                                    strTree.append("\tchildren: pchildren\n");
                                    strTree.append("}];\n");
                                    strTree
                                            .append("var tree = new mapfish.widgets.LayerTree({\n");
                                    strTree.append("\tid:'tree',\n");
                                    strTree.append("\tmap: map,\n");
                                    strTree.append("\tmodel: pmodel,\n");
                                    strTree.append("\tenableDD: true,\n");
                                    strTree.append("\twidth: '100%',\n");
                                    strTree.append("\theight: '100%',\n");
                                    strTree.append("\tborder: false,\n");
                                    strTree.append("\tautoScroll: true\n");
                                    strTree.append("});\n");
                                    strTree.append("tree.render('mftree');\n");
                                    mainOut.println(strTree.toString());
                                    break;
                                }
                            }
                        }

                    } else if (s.trim().equalsIgnoreCase("[AV2GMFTOOLBAR]")) {
                        StringBuilder strToolbar = new StringBuilder();
                        if (request.getParameter("selected_components") != null) {
                            StringTokenizer tok = new StringTokenizer(request
                                    .getParameter("selected_components"), "|");
                            while (tok.hasMoreTokens()) {
                                String mfctrl = tok.nextToken();
                                if (mfctrl.equalsIgnoreCase("MF_NavToolbar")) {
                                    strToolbar
                                            .append("var toolbar = new mapfish.widgets.toolbar.Toolbar({map: map, configurable: false});\n");
                                    strToolbar
                                            .append("toolbar.render('toolbar');\n");
                                    strToolbar
                                            .append("toolbar.addControl(new OpenLayers.Control.ZoomBox(), {iconCls: 'bzoomin', toggleGroup: 'map'});\n");
                                    strToolbar
                                            .append("toolbar.addControl(new OpenLayers.Control.DragPan({isDefault: true}), {iconCls: 'bdrag', toggleGroup: 'map'});\n");
                                    strToolbar
                                            .append("toolbar.addControl(new OpenLayers.Control.ZoomToMaxExtent(), {iconCls: 'bzoomtomax', toggleGroup: 'map'});\n");
                                    strToolbar
                                            .append("toolbar.add(new Ext.Toolbar.Spacer());\n");
                                    strToolbar
                                            .append("toolbar.add(new Ext.Toolbar.Separator());\n");
                                    strToolbar
                                            .append("toolbar.add(new Ext.Toolbar.Spacer());\n");
                                    strToolbar
                                            .append("var vectorLayer = map.getLayersByName('Draw')[0];\n");
                                    strToolbar
                                            .append("toolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Point), {iconCls: 'bdrawpoint', toggleGroup: 'map'});\n");
                                    strToolbar
                                            .append("toolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Path), {iconCls: 'bdrawline', toggleGroup: 'map'});\n");
                                    strToolbar
                                            .append("toolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Polygon), {iconCls: 'bdrawpolygon', toggleGroup: 'map'});\n");
                                    strToolbar.append("toolbar.activate();\n");
                                    mainOut.println(strToolbar.toString());
                                    break;
                                }
                            }
                        }
                    }

                } else {
                    // a line to write so as.
                    mainOut.println(s);
                }
            }
            mainOut.flush();
            mainOut.close();
            mainIn.close();
        } catch (Exception te) {
            te.printStackTrace();
        }
    }

    private static File[] listFilesAsArray(File directory,
            FilenameFilter filter, boolean recurse) {

        Collection<File> files = listFiles(directory, filter, recurse);

        File[] arr = new File[files.size()];
        return files.toArray(arr);
    }

    private static Collection<File> listFiles(File directory,
            FilenameFilter filter, boolean recurse) {

        // List of files / directories
        Vector<File> files = new Vector<File>();

        // Get files / directories in the directory
        File[] entries = directory.listFiles();

        // Go over entries
        for (File entry : entries) {

            // If there is no filter or the filter accepts the
            // file / directory, add it to the list
            if (filter == null || filter.accept(directory, entry.getName())) {
                files.add(entry);
            }

            // If the file is a directory and the recurse flag
            // is set, recurse into the directory
            if (recurse && entry.isDirectory()) {
                files.addAll(listFiles(entry, filter, recurse));
            }
        }

        // Return collection of files
        return files;
    }
}
