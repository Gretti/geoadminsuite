/*
 * GOLayerStatus.java
 *
 * Created on 23 septembre 2002, 14:58
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.GeometryClass;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;
/**
 * Servlet to deal with a geonline Layer status, directly inside the MC_result_map jsp page:
 * This servlet receives request from a javascript code and returns some javascript code to refresh
 * map status according to given request parameters: set mapserver visibility or kaboum object loading
 *
 * @author  nri
 */
public class GOLayerStatus extends BaseServlet {
    /**
     * Main method listening to client requests:
     * Paramters are:
     * target=<MAPSERRVER|KABOUM>&status=<on|off>, where:
     * MAPSERVER target to set mapserver layer status (either on or off)
     * KABOUM to force geo objects display by kaboum: on to load objects, off to remove them
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        PrintWriter out = null;
        debugParameters(request);
        String target = request.getParameter("target");
        String status = request.getParameter("status");
        String layerid = request.getParameter("layerid");
        // The html response
        String htmlResponse = "<html></html>";
        
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        
        if (target.equals("KABOUM")) {
            htmlResponse = getGeometries(umb, layerid, status);
        } else if (target.equals("MAPSERVER")) {
            htmlResponse = modifyLayerStatus(umb, layerid, status);
        }
        // the response
        try {
            out = response.getWriter();
            out.write(htmlResponse);
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    protected String getGeometries(UserMapBean umb, String layerid, String status) {
        // gets geometries from db
        Connection con = null;
        StringBuffer res = new StringBuffer("<html><head><script>");
        GeometryClass gc = (GeometryClass)umb.getUserLayerList().get(layerid);
        
        try {
            if (status.equalsIgnoreCase("on")) {
                // should get geometries, thus set displayInKaboum mode to true
                gc.displayInKaboum = true;
                con = ConPool.getConnection(gc.getHost(),
                gc.getDBPort(),
                gc.getDatasourceName(),
                gc.getUserName(),
                gc.getUserPwd(),
                "postgres");
                // forces a where clause by default: < 1000 objects to limit kaboum overload
                gc.whereClause = "limit 1000";
                if (!gc.getGeometriesFromDB(con, gc.whereClause)) {
                    LogEngine.log("cannot get geometries from DB for: " + gc.getName());
                    LogEngine.log("Error: " + gc.getErrorMessage());
                } else {
                    // prepares the object loading
                    res.append(gc.getKaboumObjectsString("parent.document.kaboum.kaboumCommand"));
                }
            } else {
                // gets objects removal string from the GeometryClass
                res.append(gc.getKaboumRemoveObjectsString("parent.document.kaboum.kaboumCommand"));
                gc.displayInKaboum = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception sqle2) {sqle2.printStackTrace();}
            res.append("\nparent.refreshExtent();</script></head></html>");
        }
        
        return res.toString();
    }
    
    /**
     * Changes the GeometryClass mapserver status according to status parameters
     */
    protected String modifyLayerStatus(UserMapBean umb, String layerid, String status) {
        Layer l = ((GeometryClass)umb.getUserLayerList().get(layerid)).getMSLayer(new RGB(0,0,0),false);
        
        if (status.equalsIgnoreCase("on")) {
            l.setStatus(Layer.ON);
        } else {
            l.setStatus(Layer.OFF);
        }
        // rebuild mapfile
        //umb.generateUserMapfile();
        return "<html><head><script>parent.refreshExtent();</script></head></html>";
    }
}
