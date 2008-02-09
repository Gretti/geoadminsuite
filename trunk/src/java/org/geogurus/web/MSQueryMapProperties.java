/*
 * MSQueryMapProperties.java
 *
 * Created on 13 janvier 2003, 15:25
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
import java.awt.Dimension;
import org.geogurus.GeometryClass;
import org.geogurus.mapserver.objects.QueryMap;
import org.geogurus.mapserver.objects.RGB;
/**
 * Servlet to deal with a QueryMap object, and its corresponding HTML configuration page:
 * MS_mapserver_QueryMap_properties.jsp
 * @author  nri
 */
public class MSQueryMapProperties extends BaseServlet {
    protected final String mc_msquerymap_jsp = "MC_mapserver_querymap_properties.jsp";
    
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
        doUpdate(request, umb);
        // put in the request a parameter to refresh the mapfile in the main window
        dispatch(request, response, mc_msquerymap_jsp + "?refreshmap=true");
    }
    /**
     * update the current Mapfile object with user-modified parameters.
     * The current mapfile is stored in the UserMapBean bean
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb) {
        QueryMap qm = umb.getMapfile().getQueryMap();
        //control is done by Javascript
        if (request.getParameter("querymap_color").length() == 0) {
            qm.setColor(null);
        } else {
            qm.setColor(new RGB(request.getParameter("querymap_color")));
        }
        if (request.getParameter("querymap_size_x").length() == 0 &&
            request.getParameter("querymap_size_y").length() == 0 ) {
            qm.setSize(null);
        } else {
            qm.setSize(new Dimension(new Double(request.getParameter("querymap_size_x")).intValue(),
                                        new Double(request.getParameter("querymap_size_y")).intValue()));
        }
        
        if (request.getParameter("querymap_status") != null) {
            qm.setStatus(QueryMap.ON);
        } else  {
            qm.setStatus(QueryMap.OFF);
        }
        
        String style = request.getParameter("querymap_style");
        if (style.equals("normal")) {
            qm.setStyle(QueryMap.NORMAL);
        } else if (style.equals("hilite")) {
            qm.setStyle(QueryMap.HILITE);
        } else  if (style.equals("selected")) {
            qm.setStyle(QueryMap.SELECTED);
        }
        
        umb.getMapfile().setQueryMap(qm);
        //umb.generateUserMapfile();
    }
}