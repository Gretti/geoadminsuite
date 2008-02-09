/*
 * GOSitePublisher.java
 *
 * Created on 16 octobre 2002, 20:27
 */
package org.geogurus.web;
import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.servlet.http.*;
import javax.servlet.*;
import org.geogurus.gas.utils.ObjectKeys;

import org.geogurus.tools.LogEngine;
import org.geogurus.tools.DataManager;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Legend;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.tools.util.ZipEngine;
import org.geogurus.Datasource;
import org.geogurus.Extent;
import org.geogurus.GeometryClass;
import org.geogurus.Geometry;
import org.geogurus.KaboumProperties;
import org.geogurus.gas.objects.UserMapBean;

/**
 * Servlet to deal with the site edition JSP page (publisher page).<br>
 * Allow to export as a geonline web site all necessary resources in a single zip file
 * @author  gng-nri
 * @version
 */
public class GOSitePublisher extends BaseServlet {
    protected final String editresult_jsp = "MP_edit_result.jsp";
    protected final String fs = System.getProperty("file.separator");
    
    /**
     * Receives publisher form submission and generates zip file containing all necessary
     * file for the web site.
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        debugParameters(request);
        //Gets the current session
        HttpSession session = request.getSession(true);
        
        // current mapfile
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        org.geogurus.mapserver.objects.Map currentMap = umb.getMapfile();
        // some convenient variables for paths
        // Application path
        String appPath = getServletConfig().getServletContext().getRealPath("");
        // path to mapfile
        String mapFileName = appPath + fs + "msFiles" + fs + "tmpMaps" + fs + "published" + session.getId() + ".map";
        //path to html page
        String htmlFileName = appPath + fs + "msFiles" + fs + "tmpMaps" + fs + "published" + session.getId() + ".html";
        
        // gets the data path from user input
        String dataPath = request.getParameter("data_path");
        if (dataPath.length() == 0) {
            dataPath = "data/";
        }
        if (dataPath.lastIndexOf("/") != dataPath.length()-1) {
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
        int numFiles =  33;
        // computes number of fonts
        File fontFolder = new File(appPath + "/msFiles/fonts/");
        File[] fontList = fontFolder.listFiles();
        numFiles += fontList.length -2; // font.list is already counted
        // computes number of html templates files (one per vector layer)
        Vector layers = currentMap.getLayers();
        
        for (Iterator iter = layers.iterator(); iter.hasNext(); ) {
            Layer l = (Layer)iter.next();
            
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
            files.add(appPath + "/images/" + images[z] + ".gif"); entries.add("images/" + images[z] + ".gif");
            files.add(appPath + "/images/" + images[z] + "_over.gif"); entries.add("images/" + images[z] + "_over.gif");
            files.add(appPath + "/images/" + images[z] + "_clicked.gif"); entries.add("images/" + images[z] + "_clicked.gif");
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
        // the geonline.css file
        files.add(appPath + "/geonline.css");
        entries.add("geonline.css");
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
            if ( fontList[j].isFile() && !fontList[j].getName().equals("font.list") ) {
                files.add(fontList[j].getAbsolutePath());
                entries.add("fonts/" + fontList[j].getName());
            }
        }
        // the html templates for info query
        Hashtable layerList = umb.getUserLayerList();
        for (Enumeration en = layerList.elements(); en.hasMoreElements();) {
            GeometryClass gc = (GeometryClass)en.nextElement();
            
            if (gc.getMSLayer(null, false).getType() != Layer.RASTER) {
                // this GC represents a layer whose attributes can be queried, and
                // for which a template html file was generated, based on its id.
                files.add(appPath + "/msFiles/tmpMaps/template_" + gc.getID() + ".html");
                entries.add("template_" + gc.getID() + ".html");
            }
        }
        // returns zip file
        //creating zip filename and outpustream
        response.setHeader("Content-Disposition","filename=\"geonline.zip\"");
        response.setContentType("application/x-zip-compressed");
        
        try{
            OutputStream out = response.getOutputStream();
            ByteArrayOutputStream zipout = new ByteArrayOutputStream();
            ZipEngine.zipToStream(zipout, files, entries);
            response.setContentLength(zipout.size());
            zipout.flush();
            zipout.writeTo(out);
            out.close();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        // removes temporary files
        return;
        
    }
    
    private void writeFiles(String fn, String mfn, String appPath, String dataPath, HttpServletRequest request, HttpSession session) {
        PrintWriter mainOut = null;
        BufferedReader mainIn = null;
        UserMapBean usermapbean = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        ResourceBundle messages = (ResourceBundle)session.getAttribute("messages");
        // some user defined variables
        String mapserverURL = request.getParameter("mapserver_url");
        // the full path to the web folder containing geonline application
        String serverPath = request.getParameter("server_path");
        
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
        
        try{
            SymbolSet symbolSet = new SymbolSet();
            BufferedReader br = new BufferedReader(new FileReader(new File(serverPath + "/data/symbols.sym")));
            symbolSet.load(br);
            m.setSymbolSet(symbolSet);
        }catch (Exception e){
            e.printStackTrace();
        }
        m.setShapePath(new File(dataPath));
        
        // replaces all layer path information to be relative to the datapath
        // from the GAS, all file layers (LOCAL) have a full path: it contains either a / or a \
        // looks for the last one, cut filename and adds it to the dataPath
        String lData = null;
        for (Iterator iter = m.getLayers().iterator(); iter.hasNext();) {
            Layer l = (Layer)iter.next();
            
            // update path to header and footer:
            l.setFooter(new File("../msfooter.html"));
            l.setHeader(new File("../msheader.html"));
            
            if (l.getConnectionType() == (byte)0 || l.getConnectionType() == Layer.LOCAL) {
                // a file layer
                lData = l.getData();
                int idx = lData.lastIndexOf("/");
                if (idx == -1) {
                    idx = lData.lastIndexOf("\\");
                }
                l.setData(dataPath + lData.substring(idx+1));
                // updates template path
                l.setTemplate("../" + l.getTemplate());
            }
        }
        
        //Modifies the parameters by setting the usermapbean properties to what has been specified in the parameters
        //Set extent
        String s = "";
        
        //set map units
        if(request.getParameter("combo_map_units").equalsIgnoreCase("meter")){
            m.setUnits(org.geogurus.mapserver.objects.Map.METERS);
        }else if(request.getParameter("combo_map_units").equalsIgnoreCase("kilometer")){
            m.setUnits(org.geogurus.mapserver.objects.Map.KILOMETERS);
        }else if(request.getParameter("combo_map_units").equalsIgnoreCase("dd")){
            m.setUnits(org.geogurus.mapserver.objects.Map.DD);
        }else if(request.getParameter("combo_map_units").equalsIgnoreCase("feet")){
            m.setUnits(org.geogurus.mapserver.objects.Map.FEET);
        }else if(request.getParameter("combo_map_units").equalsIgnoreCase("inches")){
            m.setUnits(org.geogurus.mapserver.objects.Map.INCHES);
        }else if(request.getParameter("combo_map_units").equalsIgnoreCase("miles")){
            m.setUnits(org.geogurus.mapserver.objects.Map.MILES);
        }
        //set map size
        int mapwidth = stoint(request.getParameter("map_width")) == 0 ? 400 : stoint(request.getParameter("map_width"));
        int mapheight = stoint(request.getParameter("map_height")) == 0 ? 400 : stoint(request.getParameter("map_height"));
        m.setSize(new Dimension(mapwidth, mapheight));
        
        //m.setScaleBar(null);
        m.setReferenceMap(null);
        
        //set legend status
        if(request.getParameter("legend") != null){
            if(m.getLegend() == null){
                m.setLegend(new Legend());
            }
            if(request.getParameter("combo_status_legend").equalsIgnoreCase("on")){
                m.getLegend().setStatus(org.geogurus.mapserver.objects.Legend.ON);
            }else if(request.getParameter("combo_position_scalebar").equalsIgnoreCase("off")){
                m.getLegend().setStatus(org.geogurus.mapserver.objects.Legend.OFF);
            }else if(request.getParameter("combo_position_scalebar").equalsIgnoreCase("embed")){
                m.getLegend().setStatus(org.geogurus.mapserver.objects.Legend.EMBED);
            }
        } else {
            m.setLegend(null);
        }
        
        //Writes the files for publication
        BufferedWriter out = null;
        try {
            // change "\" into "/" to avoid kaboum crashes when accessing Mapfile
            // the user mapfile will be written in the same directory, under the user-choosen name:
            // mapfile name is URL encoded, must decote it:
            mfn = mfn.replace('\\', '/');
            System.out.println("userMapFile = " + mfn);
            
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
        String tmpl = getServletConfig().getServletContext().getRealPath("") + fs + "msFiles" + fs + "templates" + fs + "default_tpl.html";
        
        try {
            // the HTML page:
            mainOut = new PrintWriter(new BufferedWriter(new FileWriter(fn)));
            mainIn = new BufferedReader(new FileReader(tmpl));
            
            while ((s = mainIn.readLine()) != null) {
                if (s.trim().indexOf("<HTML>") != -1 || s.trim().indexOf("<html>") != -1) {
                    // insert link to kaboum JS pages
                    mainOut.println("<HTML>");
                    mainOut.println("<SCRIPT language=JavaScript src='scripts/kaboum.js'></SCRIPT>");
                    // adds method to zoom to each theme
                    mainOut.println("<script>");
                    mainOut.println("var themesExtents = new Array();");
                    // loops through all themes to get their extents
                    for (Iterator iter = usermapbean.getUserLayerList().values().iterator(); iter.hasNext();) {
                        gc = (GeometryClass)iter.next();
                        mainOut.println("themesExtents['" + gc.getID() + "']='" + gc.getExtent().toKaboumString() + "';");
                    }
                    mainOut.println("function zoomToTheme(layerid) {");
                    mainOut.println("document.kaboum.kaboumCommand('EXTENT|MAP|' + themesExtents[layerid]);");
                    mainOut.println("}");
                    mainOut.println("</script>");
                }
                if (s.trim().indexOf("[AV2G") != -1) {
                    // a gas tag to replace
                    if (s.trim().equalsIgnoreCase("[AV2GPAGE]")) {
                        mainOut.println("geOnline map");
                    } else if (s.trim().equalsIgnoreCase("[AV2GTitle]")) {
                        mainOut.println("geOnline map");
                    } else if (s.trim().equalsIgnoreCase("[AV2GThm]")) {
                        // loops over all layers to create JS code
                        // writes all layers with their corresponding icons (zoom, status, etc...)
                        // try to display layers in the right order, based on the layerOrder vector
                        int i = 0;
                        // the kaboum image status, according to displayInKaboum boolean value
                        String imgStatus = "";
                        Vector order = usermapbean.getUserLayerOrder();
                        mainOut.println("<TABLE>");
                        
                        for (Iterator iter = order.iterator(); iter.hasNext();) {
                            i++;
                            currentLayer = (String)iter.next();
                            gc = (GeometryClass)layerList.get(currentLayer);
                            // prepares img status
                            imgStatus = gc.displayInKaboum ? "" : "_off";
                            String name = (String)gc.getName();
                            String tabname = (String)gc.getTableName();
                            // writes the corresponding theme
                            mainOut.print("<TR><TD class='tiny'><a href='#' onclick=\"zoomToTheme('");
                            mainOut.print(currentLayer + "')\"><IMG border='0' src='images/layer.gif' alt='");
                            mainOut.print(messages.getString("zoom_to_theme") + "'></a>");
                            mainOut.print("<nobr><script>kNewTheme('" + tabname + "','" + name + "',false, ");
                            // writes only info if gc type is not an image
                            if (gc.getDatasourceType() == GeometryClass.IMGCLASS || gc.getDatasourceType() == GeometryClass.TIFFCLASS) {
                                mainOut.print("false");
                            } else {
                                mainOut.print("true");
                            }
                            mainOut.print(", 'CHECKBOX','')</script>");
                            mainOut.println("</TD></TR>");
                            
                        }
                        mainOut.println("</TABLE>");
                        
                    } else if (s.trim().equalsIgnoreCase("[AV2GLeg]") && request.getParameter("legend") != null) {
                        mainOut.print("<a href='#' onclick='kGetLegend()'>");
                        mainOut.print(messages.getString("legend"));
                        mainOut.println("</a>");
                        
                        
                    } else if (s.trim().equalsIgnoreCase("[AV2GScale]") && request.getParameter("scalebar") != null) {
                        mainOut.print("<SCRIPT>kNewScale('");
                        mainOut.print(messages.getString("scale"));
                        mainOut.println(" 1:', 'tiny')</SCRIPT>");
                        
                    } else if (s.trim().equalsIgnoreCase("[AV2GTool]")) {
                        // all the user-chooser tools
                        mainOut.println("<script>");
                        if (request.getParameter("zoomin") != null) {
                            mainOut.print("kNewButton('images/zoomin', '");
                            mainOut.print(messages.getString("zoomin"));
                            mainOut.print("', 'modal', 'ZOOMIN', 28, 28);");
                            
                        }
                        if (request.getParameter("zoomout") != null) {
                            mainOut.print("kNewButton('images/zoomout', '");
                            mainOut.print(messages.getString("zoomout"));
                            mainOut.print("', 'modal', 'ZOOMOUT', 28, 28);");
                            
                        }
                        if (request.getParameter("pan") != null) {
                            mainOut.print("kNewButton('images/pan', '");
                            mainOut.print(messages.getString("pan"));
                            mainOut.print("', 'modal', 'PAN', 28, 28);");
                            
                        }
                        if (request.getParameter("back") != null) {
                            mainOut.print("kNewButton('images/back', '");
                            mainOut.print(messages.getString("back"));
                            mainOut.print("', 'action', 'BACK', 28, 28);");
                            
                        }
                        if (request.getParameter("forward") != null) {
                            mainOut.print("kNewButton('images/forward', '");
                            mainOut.print(messages.getString("forward"));
                            mainOut.print("', 'action', 'FORWARD', 28, 28);");
                            
                        }
                        if (request.getParameter("home") != null) {
                            mainOut.print("kNewButton('images/home', '");
                            mainOut.print(messages.getString("home"));
                            mainOut.print("', 'action', 'HOME', 28, 28);");
                            
                        }
                        if (request.getParameter("info") != null) {
                            mainOut.print("kNewButton('images/info', '");
                            mainOut.print(messages.getString("info"));
                            mainOut.print("', 'modal', 'QUERY', 28, 28);");
                            
                        }
                        if (request.getParameter("distance") != null) {
                            mainOut.print("kNewButton('images/distance', '");
                            mainOut.print(messages.getString("distance"));
                            mainOut.print("', 'modal', 'DISTANCE', 28, 28);");
                        }
                        if (request.getParameter("help") != null) {
                            
                            mainOut.print("kNewButton('images/help', '");
                            mainOut.print(messages.getString("help"));
                            mainOut.print("', 'action', 'JS_HELP', 28, 28);");
                            
                        }
                        /*
                        if (request.getParameter("print") != null) {
                            mainOut.print("kNewButton('images/print', '");
                            mainOut.print(messages.getString("print"));
                            mainOut.print("', 'action', 'JS_PRINT', 28, 28);");
                        }
                         */
                        mainOut.println("</script>");
                    } else if (s.trim().equalsIgnoreCase("[AV2GMAP]")) {
                        /*TODO utiliser kaboumBean
                        // writes the applet tag for the map
                        KaboumProperties kp = new KaboumProperties(usermapbean.getKaboumProperties());
                        // sets some properties for the KaboumProperties class.
                        
                        kp.setProperty("width", request.getParameter("map_width"));
                        kp.setProperty("height", request.getParameter("map_height"));
                        kp.setProperty("KABOUM_MAPSERVER_CGI_URL", mapserverURL);
                        kp.setProperty("MAPFILE_PATH", serverPath + "/data/geonline.map");
                        kp.setProperty("MAPFILE_EXTENT", m.getExtent().toKaboumString());
                        
                        mainOut.println(kp.getAppletHtmlRepresentation());
                        mainOut.println(kp.closeAppletTag());*/
                    }
                } else {
                    // a line to write so as.
                    mainOut.println(s);
                    
                }
            }
            mainOut.close();
            mainIn.close();
        } catch (Exception te) {
            te.printStackTrace();
        }
    }
    
    private double stod(String s) {
        double n = Double.MIN_VALUE;
        if(s == null){
            return n;
        }
        try {
            n = new Double(s.trim()).doubleValue();
        } catch(NumberFormatException e) {}
        return(n);
        
    }
    
    private int stoint(String s) {
        int n = Integer.MIN_VALUE;
        if(s == null){
            return n;
        }
        try {
            n = new Integer(s.trim()).intValue();
        } catch(NumberFormatException e) {}
        return(n);
        
    }
}
