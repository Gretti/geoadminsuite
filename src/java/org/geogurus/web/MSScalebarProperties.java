/*
 * MSScalebarProperties.java
 *
 * Created on 24 octobre 2002, 19:08
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.util.StringTokenizer;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import java.awt.Color;
import java.io.File;
import java.awt.Dimension;
import org.geogurus.GeometryClass;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.ScaleBar;
/**
 * Servlet to deal with a ScaleBar object, and its corresponding HTML configuration page:
 * MS_mapserver_scalebar_properties.jsp
 * @author  nri
 */
public class MSScalebarProperties extends BaseServlet {
    protected final String mc_msscale_jsp = "MC_mapserver_scalebar_properties.jsp";
    
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
        dispatch(request, response, mc_msscale_jsp + "?refreshmap=true");
    }
    /**
     * update the current Mapfile object with user-modified parameters.
     * The current mapfile is stored in the UserMapBean bean
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb) {
        
        ScaleBar scale = umb.getMapfile().getScaleBar();
        
        if (request.getParameter("color").length() == 0) {
            scale.setColor(null);
        } else {
            scale.setColor(new RGB(request.getParameter("color")));
        }
        
        if (request.getParameter("outlineColor").length() == 0) {
            scale.setOutlineColor(null);
        } else {
            scale.setOutlineColor(new RGB(request.getParameter("outlineColor")));
        }
        
        if (request.getParameter("imageColor").length() == 0) {
            scale.setImageColor(null);
        } else {
            scale.setImageColor(new RGB(request.getParameter("imageColor")));
        }
        
        if (request.getParameter("backgroundColor").length() == 0) {
            scale.setBackgroundColor(null);
        } else {
            scale.setBackgroundColor(new RGB(request.getParameter("backgroundColor")));
        }
        
        scale.setInterlace(request.getParameter("interlace") != null);
        
        if (request.getParameter("transparent") != null) {
            scale.setTransparent(ScaleBar.ON);
        } else {
            scale.setTransparent(ScaleBar.OFF);
        }
        
        if (request.getParameter("intervals").length() > 0) {
            scale.setIntervals(new Integer(request.getParameter("intervals")).intValue());
        } else {
            scale.setIntervals(4);
        }
        
        scale.setPosition(new Byte(request.getParameter("position")).byteValue());
        
        if (request.getParameter("size.width").length() == 0 &&
                request.getParameter("size.height").length() == 0 ) {
            scale.setSize(null);
        } else {
            scale.setSize(new Dimension(new Float(request.getParameter("x")).intValue(),
                    new Float(request.getParameter("y")).intValue()));
        }
        scale.setPostLabelCache(request.getParameter("postLabelCache") != null);
        
        scale.setStatus(new Byte(request.getParameter("status")).byteValue());
        
        if (request.getParameter("style").length() > 0) {
            scale.setStyle(new Integer(request.getParameter("style")).intValue());
        }
        
        scale.setUnits(new Byte(request.getParameter("units")).byteValue());
        
        umb.getMapfile().setScaleBar(scale);
        
        UserMapBeanManager manager = new UserMapBeanManager();
        manager.setUserMapBean(umb);
        manager.generateUserMapfile((ColorGenerator)request.getSession().getAttribute(ObjectKeys.COLOR_GENERATOR));
    }
}