/*
 * MSReferenceMapProperties.java
 *
 * Created on 23 octobre 2002, 10:22
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
import org.geogurus.mapserver.objects.MSExtent;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.ReferenceMap;
/**
 * Servlet to deal with a ReferenceMap object, and its corresponding HTML configuration page:
 * MS_mapserver_reference_properties.jsp
 * @author  nri
 */
public class MSReferenceMapProperties extends BaseServlet {
    protected final String mc_msref_jsp = "MC_mapserver_reference_properties.jsp";
    
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
        dispatch(request, response, mc_msref_jsp + "?refreshmap=true");
    }
    /**
     * update the current Mapfile object with user-modified parameters.
     * The current mapfile is stored in the UserMapBean bean
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb) {
        org.geogurus.mapserver.objects.ReferenceMap ref = umb.getMapfile().getReferenceMap();
        //control is done by Javascript
        if (request.getParameter("reference_color").length() == 0) {
            ref.setColor(null);
        } else {
            ref.setColor(new RGB(request.getParameter("reference_color")));
        }
        if (request.getParameter("reference_outlinecolor").length() == 0) {
            ref.setOutlineColor(null);
        } else {
            ref.setOutlineColor(new RGB(request.getParameter("reference_outlinecolor")));
        }
        if (request.getParameter("reference_size_x").length() == 0 &&
        request.getParameter("reference_size_y").length() == 0 ) {
            ref.setSize(null);
        } else {
            ref.setSize(new Dimension(new Double(request.getParameter("reference_size_x")).intValue(),
            new Double(request.getParameter("reference_size_y")).intValue()));
        }
        if (request.getParameter("reference_status") != null) {
            ref.setStatus(ReferenceMap.ON);
        } else {
            ref.setStatus(ReferenceMap.OFF);
        }
        if (request.getParameter("reference_extent").length() == 0) {
            ref.setExtent(null);
        } else {
            //control is done by Javascript
            StringTokenizer tok = new StringTokenizer(request.getParameter("reference_extent"));
            ref.setExtent(new MSExtent(new Double(tok.nextToken()).doubleValue(),
                                       new Double(tok.nextToken()).doubleValue(),
                                       new Double(tok.nextToken()).doubleValue(),
                                       new Double(tok.nextToken()).doubleValue()));
        }
        if (request.getParameter("reference_image").length() == 0) {
            ref.setImage(null);
        } else {
            ref.setImage(new File(request.getParameter("reference_image")));
        }
        if (request.getParameter("reference_marker").length() == 0) {
            ref.setMarker(null);
        } else {
            ref.setMarker(request.getParameter("reference_marker"));
        }
        if (request.getParameter("reference_markersize").length() == 0) {
            ref.setMarkerSize(-1);
        } else {
            ref.setMarkerSize(new Integer(request.getParameter("reference_markersize")).intValue());
        }
        if (request.getParameter("reference_minboxsize").length() == 0) {
            ref.setMinBoxSize(-1);
        } else {
            ref.setMinBoxSize(new Integer(request.getParameter("reference_minboxsize")).intValue());
        }
        if (request.getParameter("reference_maxboxsize").length() == 0) {
            ref.setMaxBoxSize(-1);
        } else {
            ref.setMaxBoxSize(new Integer(request.getParameter("reference_maxboxsize")).intValue());
        }
        
        umb.getMapfile().setReferenceMap(ref);
        //umb.generateUserMapfile();
    }
}