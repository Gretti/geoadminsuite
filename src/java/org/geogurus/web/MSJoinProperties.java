/*
 * MSJoinProperties.java
 *
 * Created on 13 janvier 2003, 16:02
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.util.StringTokenizer;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import java.awt.Color;
import java.io.File;
import java.awt.Dimension;
import org.geogurus.GeometryClass;
import org.geogurus.mapserver.objects.Join;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.MapClass;
import org.geogurus.mapserver.objects.RGB;
/**
 * Servlet to deal with a join object, and its corresponding HTML configuration page:
 * MS_mapserver_join_properties.jsp
 * @author  nri
 */
public class MSJoinProperties extends BaseServlet {
    protected final String mc_msjoin_jsp = "MC_mapserver_join_properties.jsp";
    
    /**
     * Main method listening to client requests:
     * See jsp page for input parameters
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        debugParameters(request);
        
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        // the internationalization resource:
        ResourceBundle messages = (ResourceBundle)session.getAttribute("messages");
        
        if (umb == null) {
            // session: expiration
            String error = messages.getString("missing_umb_or_layer");
            session.setAttribute(ObjectKeys.SERVLET_ERROR, error);
            dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
            return;
        }
        String classID = request.getParameter("classid");
        doUpdate(request, umb, classID);
        // put in the request a parameter to refresh the mapfile in the main window
        dispatch(request, response, mc_msjoin_jsp + "?refreshmap=true&classid=" + classID);
    }
    /**
     * update the current Mapfile object with user-modified parameters.
     * The current mapfile is stored in the UserMapBean bean
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb, String classID) {
        Join join = null;
        HttpSession session = request.getSession();
        
        // target MS object is a class
        // get the current GeometryClass' layer object whose class is to retrieve:
        String layerid = (String)session.getAttribute(ObjectKeys.CURRENT_GC);
        // the layer's object of the current GeometryClass: all the GeometryClass were asked to generate
        // their layer object, as they are displayed in the map
        GeometryClass gc = (GeometryClass)umb.getUserLayerList().get(layerid);
        Layer gcLayer = gc.getMSLayer(new RGB(0,0,0),false);
        // find the mapClass based on the given id:
        MapClass cl = null;
        for (Iterator iter = gcLayer.getMapClass().getClasses(); iter.hasNext();) {
            cl = (MapClass)iter.next(); 
            if (classID.equals("" + cl.getID())) break;
        }
        join = cl.getJoin();
        //control is done by Javascript
        join.setName(request.getParameter("join_name"));
        
        if (request.getParameter("join_from").length() == 0) {
            join.setFrom(null);
        } else {
            join.setFrom(request.getParameter("join_from"));
        }
        if (request.getParameter("join_to").length() == 0) {
            join.setTo(null);
        } else {
            join.setTo(request.getParameter("join_To"));
        }
        if (request.getParameter("join_table").length() == 0) {
            join.setTable(null);
        } else {
            join.setTable(new File(request.getParameter("join_table")));
        }
        if (request.getParameter("join_template").length() == 0) {
            join.setTemplate(null);
        } else {
            join.setTemplate(new File(request.getParameter("join_template")));
        }
        String type = request.getParameter("join_type");
        
        if (type.equals("multiple")) {
            join.setType(Join.MULTIPLE);
        } else if (type.equals("single")) {
            join.setType(Join.SINGLE);
        }
        
        cl.setJoin(join);
        //umb.generateUserMapfile();
    }
}