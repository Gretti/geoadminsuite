/*
 * KaboumLayerProperties.java
 *
 * Created on 8 ao�t 2002, 21:31
 */
package org.geogurus.web;

import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.sql.ConPool;
import java.awt.Color;
import java.sql.Connection;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.geogurus.GeometryClass;

/**
 * Servlet to deal with a MS Layer object, and its corresponding HTML configuration page:
 * MC_mapserver_layer_properties.jsp
 * This servlet update the current GeometryClass' layer object (a layer choosed by user among all the available
 * layers
 * It listens to MC_mapserver_layer_properties.jsp page to update the object.
 * The current Layer (GeometryClass object) to update is retrieved by looking the geometryClass
 * index, stored in the request under the "layerid" key
 *
 * @author  nri
 */
public class KaboumLayerProperties extends BaseServlet {
    protected final String mc_mslayer_jsp = "MC_kaboum_layer_properties.jsp";
    
    /**
     * Main method listening to client requests:
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
debugParameters(request);
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        String layerid = (String)session.getAttribute(ObjectKeys.CURRENT_GC);
        
        if (umb == null || layerid == null) {
            // session: expiration
            String error = "UserMapBean ou layerid manquant. La session a du expirer.";
            session.setAttribute(ObjectKeys.SERVLET_ERROR, error);
            dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
            return;
        }
        doUpdate(request, umb, layerid);
        // put in the request a parameter to refresh the mapfile in the main window
        dispatch(request, response, mc_mslayer_jsp + "?refreshmap=true");
    }
    
    /**
     * update the current GeometryClass' MSLayer object with user-modified parameters.
     * The index of this GC is stored in the session under the "CURRENT_GEOMETRY_CLASS_INDEX" key
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb, String layerid) {
        GeometryClass gc = (GeometryClass)umb.getUserLayerList().get(layerid);
        
        // some gc properties
        gc.displayInKaboum = (request.getParameter("k_displayinkaboum") != null);
        gc.whereClause = request.getParameter("k_whereclause");
        gc.isVisible = (request.getParameter("k_is_visible") != null);
        gc.isLocked = (request.getParameter("k_is_locked") != null);
        gc.isActive = (request.getParameter("k_is_active") != null);
        gc.isSurrounding = (request.getParameter("k_is_surrounding") != null);
        gc.isComputed = (request.getParameter("k_is_computed") != null);
        gc.isFilled = (request.getParameter("k_is_filled") != null);
        // some color stuff
        gc.setFillColor(getColor(request.getParameter("k_fillcolor")));
        gc.setHiliteColor(getColor(request.getParameter("k_hilitecolor")));
        gc.setModifiedColor(getColor(request.getParameter("k_modifiedcolor")));
        gc.setPointColor(getColor(request.getParameter("k_pointcolor")));
        gc.setPointHiliteColor(getColor(request.getParameter("k_pointhilitecolor")));
        gc.setPointForbiddenColor(getColor(request.getParameter("k_pointforbiddencolor")));
        
        String pointtype = request.getParameter("k_pointtype");
        if (pointtype.equals("K_TYPE_BOX")) {
            gc.setPointType(gc.K_TYPE_BOX);
        } else if (pointtype.equals("K_TYPE_CIRCLE")){
            gc.setPointType(gc.K_TYPE_CIRCLE);
        } else if (pointtype.equals("K_TYPE_IMAGE")){
            gc.setPointType(gc.K_TYPE_IMAGE);
        } else {
            gc.setPointType(gc.K_TYPE_POINT);
        }
        gc.setPointHeight(new Integer(request.getParameter("k_pointheight")).intValue());
        gc.setPointWidth(new Integer(request.getParameter("k_pointwidth")).intValue());
        gc.setPointImageURL(request.getParameter("k_pointimageurl"));
        
        // gets geometries from db
        if (gc.displayInKaboum) {
            Connection con = null;
            try {
                con = ConPool.getConnection(gc.getHost(), 
                                                       gc.getDBPort(), 
                                                       gc.getDatasourceName(), 
                                                       gc.getUserName(), 
                                                       gc.getUserPwd(),
                                                       "postgres");
                if (!gc.getGeometriesFromDB(con, gc.whereClause)) {
                    LogEngine.log("cannot get geometries from DB for: " + gc.getName());
                    LogEngine.log("Error: " + gc.getErrorMessage());
                }
            } catch (Exception e) {
            } finally {
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (Exception sqle2) {sqle2.printStackTrace();}
            }
        }
        
        // now everything is up to date, regenerate a mapfile to keep map accurate
        //umb.generateUserMapfile();
        return;
    }
    /**
     * Returns a new Color object based on the rgb values contained in the given string
     * Returns a red color if string does not contain 3 space separated int values
     *
     */ 
    protected Color getColor(String col) {
        StringTokenizer tk = new StringTokenizer(col);
        
        if (tk.countTokens() != 3) {
            return Color.red;
        }
        return new Color(new Integer(tk.nextToken()).intValue(),
                         new Integer(tk.nextToken()).intValue(),
                         new Integer(tk.nextToken()).intValue());
    }
}
