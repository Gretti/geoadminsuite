/*
 * MapFileManager.java
 *
 * Created on June 5, 2002, 4:13 PM
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.Vector;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.KaboumApplet;
/**
 * Servlet dealing with kaboum application initialization:
 * Creates a new KaboumApplet obj and put it in session.
 * This object will be used by GIS page
 * @author  nri
 */
public class KaboumManager extends BaseServlet {
    /** jsp page corresponding to this servlet */
    private final String kaboum_map_jsp = "kaboum_map.jsp";
    /** this path should come from a properties file */
    private final String mspath = "http://pc-dev1/cgi-bin/mapserv.exe";
    
    /**
     * Request parameters are:
     * action=init to create a new kaboum object
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");
        
        if (action.equalsIgnoreCase("init")) {
            doInit(request, response, session);
            dispatch(request, response, kaboum_map_jsp);
            return;
        } else {
            debugToClient(response, "Invalid value for 'action' parameter. Expected 'init', 'load' or 'save'");
        }
    }
    
    protected void doInit(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        org.geogurus.mapserver.objects.Map m = (org.geogurus.mapserver.objects.Map)session.getAttribute(ObjectKeys.CURRENT_MAPFILE);
        
        if (m == null) {sessionHasExpired(request, response, "Map"); return; }
        
        KaboumApplet ka = new KaboumApplet();
        // sets compulsory parameters, some of them got from the Map object
        ka.addProperty(KaboumApplet.MAPFILE_EXTENT, m.getExtent().toKaboumString());
        ka.addProperty(KaboumApplet.MAPFILE_PATH, (m.getMapFile().getAbsolutePath()).replace(' ', '+').replace('\\', '/'));
        
        ka.addProperty(KaboumApplet.KABOUM_MAPSERVER_CGI_URL, mspath);
        
        // stores in session:
        session.setAttribute(ObjectKeys.KABOUM, ka);
    }
}
