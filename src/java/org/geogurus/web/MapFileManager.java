/*
 * MapFileManager.java
 *
 * Created on June 5, 2002, 4:13 PM
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.Vector;
import java.util.StringTokenizer;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import java.awt.Color;
import java.io.*;
import java.awt.Dimension;
import org.geogurus.mapserver.objects.MSExtent;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.RGB;
/**
 * Servlet dealing with application initialization:
 * Creates the first empty map file, put it in session and write it to disk
 * as a file, to be accessible by GIS page
 * @author  nri
 */
public class MapFileManager extends BaseServlet {
    /** jsp page corresponding to this servlet */
    private final String mapfile_manager_jsp = "mapfile_manager.jsp";
    /** map file initial properties */
    private final Dimension initMapSize = new Dimension(400, 400);
    private final RGB initMapColor = new RGB(255,255,255);
    private final MSExtent initMapExt = new MSExtent(0, 0, 10, 10);
    private final String initMapFileName = "../webapps/geonline/tmp.map";
    
    /**
     * Request parameters are:
     * action=init to create an empty mapfile and save it to disk
     * action=save to save a mapfile
     * action=load to load an existing mapfile
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");
        
        if (action.equalsIgnoreCase("init")) {
            doInit(request, response, session);
            dispatch(request, response, mapfile_manager_jsp);
            return;
        } else if (action.equalsIgnoreCase("load")) {
            debugToClient(response, "Not yet implemented");
        } else if (action.equalsIgnoreCase("save")) {
            debugToClient(response, "Not yet implemented");
        } else {
            debugToClient(response, "Invalid value for 'action' parameter. Expected 'init', 'load' or 'save'");
        }
    }
    
    protected void doInit(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        org.geogurus.mapserver.objects.Map m = (org.geogurus.mapserver.objects.Map)session.getAttribute(ObjectKeys.CURRENT_MAPFILE);
        
        if (m != null) {
            // consider an existing map already exists in session: do nothing
            return;
        }
        m = new org.geogurus.mapserver.objects.Map();
        // sets required parameters to produce a valid map:
        m.setSize(initMapSize);
        m.setImageColor(initMapColor);
        m.setExtent(initMapExt);
        m.setStatus(Map.ON);
        m.setUnits(Map.METERS);
        // writes this map to file
        try {
            File mf = new File(initMapFileName);
            BufferedWriter out = new BufferedWriter(new FileWriter(mf));
            m.saveAsMapFile(out);
            out.close();
            // stores this file for future use
            m.setMapFile(mf);
            
        } catch (IOException ioe) {
            debugToClient(response, "Problem during mapfile writting: " + ioe.getMessage());
            ioe.printStackTrace();
        }
        //stores the map in session:
        session.setAttribute(ObjectKeys.CURRENT_MAPFILE, m);
    }
}
