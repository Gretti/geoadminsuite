/*
 * GOLayerProperties.java
 *
 * Created on 14 ao�t 2002, 14:43
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.DataManager;
import java.awt.Color;
import org.geogurus.GeometryClass;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;
/**
 * Servlet to deal with a geonline Layer properties page:
 * MC_geonline_layer_properties.jsp
 * This servlet update the current GeometryClass' layer object (a layer choosed by user among all the available
 * layers
 * The current Layer (GeometryClass object) to update is retrieved by looking the geometryClass
 * index, stored in the request under the "layerid" key
 *
 * @author  nri
 */
public class GOLayerProperties extends BaseServlet {
    protected final String mc_golayer_jsp = "MC_geonline_layer_properties.jsp";
    
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
        dispatch(request, response, mc_golayer_jsp + "?refreshmap=true");
    }
    
    /**
     * update the current GeometryClass' MSLayer object with user-modified parameters.
     * The index of this GC is stored in the session under the "CURRENT_GEOMETRY_CLASS_INDEX" key
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb, String layerid) {
        Layer l = ((GeometryClass)umb.getUserLayerList().get(layerid)).getMSLayer(new RGB(0,0,0),false);
        // l cannot be null as it is built by getMSLayer
        
        l.setMinScale(
            request.getParameter("minscale").length() == 0 ? 
                0.0 : 
                new Double(request.getParameter("minscale")).doubleValue());
        l.setMaxScale(
            request.getParameter("maxscale").length() == 0 ? 
                0.0 : 
                new Double(request.getParameter("maxscale")).doubleValue());
        l.setFilter(request.getParameter("filter").length() == 0 ? null : request.getParameter("filter"));
        l.setFilterItem(request.getParameter("filteritem"));
        l.setTransparency(
            request.getParameter("transparency").length() == 0 ? 
                0 : 
                new Integer(request.getParameter("transparency")).intValue());
        // now everything is up to date, regenerate a mapfile to keep map accurate
        //umb.generateUserMapfile();
        return;
    }
}
