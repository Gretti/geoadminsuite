/*
 * MSClassProperties.java
 *
 * Created on 8 ao�t 2002, 19:48
 */
package org.geogurus.web;

import java.io.File;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.geogurus.data.DataAccess;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;

/**
 * Servlet to deal with a MS Class object, and its corresponding HTML configuration page:
 * MC_mapserver_class_properties.jsp
 * This servlet update the current GeometryClass' layer's class object (a layer choosed by user among all the available
 * layers
 * It listens to MC_mapserver_class_properties.jsp page to update the object.
 * The current Layer (GeometryClass object) to update is retrieved by looking the geometryClass
 * index, stored in the request under the "layerid" key
 *
 * @author  nri
 */
public class MSClassProperties extends BaseServlet {

    protected final String mc_mslayer_jsp = "MC_mapserver_class_properties.jsp";

    /**
     * Main method listening to client requests:
     * See this page for the complete list of parameters names
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        debugParameters(request);
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        String layerid = (String) session.getAttribute(ObjectKeys.CURRENT_GC);
        // the class id is passed by the JSP in the request.
        String classID = request.getParameter("classid");

        if (umb == null || layerid == null) {
            // session: expiration
            String error = "UserMapBean ou layerid manquant. La session a du expirer.";
            session.setAttribute(ObjectKeys.SERVLET_ERROR, error);
            dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
            return;
        }
        doUpdate(request, umb, layerid, classID);
        // put in the request a parameter to refresh the mapfile in the main window
        dispatch(request, response, mc_mslayer_jsp + "?refreshmap=true&classid=" + classID);
    }

    /**
     * update the current GeometryClass' MSLayer object with user-modified parameters.
     * The index of this GC is stored in the session under the "CURRENT_GEOMETRY_CLASS_INDEX" key
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb, String layerid, String classID) {
        Layer l = ((DataAccess) umb.getUserLayerList().get(layerid)).getMSLayer(new RGB(0, 0, 0), false);
        // there is alwways a valid class here, as JSP page has already created at least one.
        Class cl = null;
        for (Iterator iter = l.getMapClass().getClasses(); iter.hasNext();) {
            cl = (Class) iter.next();
            if (classID.equals("" + cl.getID())) {
                break;
            }
        }

        //control is done by Javascript
        if (request.getParameter("mapclass_backgroundcolor").length() > 0) {
            cl.setBackgroundColor(new RGB(request.getParameter("mapclass_backgroundcolor")));
        } else {
            cl.setBackgroundColor(null);
        }
        if (request.getParameter("mapclass_color").length() > 0) {
            cl.setColor(new RGB(request.getParameter("mapclass_color")));
        } else {
            cl.setColor(null);
        }
        if (request.getParameter("mapclass_outlinecolor").length() > 0) {
            cl.setOutlineColor(new RGB(request.getParameter("mapclass_outlinecolor")));
        } else {
            cl.setOutlineColor(null);
        }
        if (request.getParameter("mapclass_expression").length() > 0) {
            cl.setExpression(request.getParameter("mapclass_expression"));
        }
        //maxsize is always set: default value
        cl.setMaxSize(new Integer(request.getParameter("mapclass_maxsize")).intValue());
        //minsize is always set: default value
        cl.setMinSize(new Integer(request.getParameter("mapclass_minsize")).intValue());
        //size is always set: default value
        cl.setSize(new Integer(request.getParameter("mapclass_size")).intValue());

        if (request.getParameter("mapclass_name").length() > 0) {
            cl.setName(request.getParameter("mapclass_name"));
        } else {
            cl.setName(null);
        }
        if (request.getParameter("mapclass_symbol").length() > 0) {
            cl.setSymbol(request.getParameter("mapclass_symbol"));
        } else {
            cl.setSymbol(null);
        }
        if (request.getParameter("mapclass_template").length() > 0) {
            cl.setTemplate(new File(request.getParameter("mapclass_name")));
        } else {
            cl.setTemplate(null);
        }
        if (request.getParameter("mapclass_text").length() > 0) {
            cl.setText(request.getParameter("mapclass_text"));
        } else {
            cl.setText(null);
        }
        // overlay stuff
        if (request.getParameter("mapclass_overlaybackgroundcolor").length() > 0) {
            cl.setOverlayBackgroundColor(new RGB(request.getParameter("mapclass_overlaybackgroundcolor")));
        } else {
            cl.setOverlayBackgroundColor(null);
        }
        if (request.getParameter("mapclass_overlaycolor").length() > 0) {
            cl.setOverlayColor(new RGB(request.getParameter("mapclass_overlaycolor")));
        } else {
            cl.setOverlayColor(null);
        }
        if (request.getParameter("mapclass_overlayoutlinecolor").length() > 0) {
            cl.setOverlayOutlineColor(new RGB(request.getParameter("mapclass_overlayoutlinecolor")));
        } else {
            cl.setOverlayOutlineColor(null);
        }
        //overlaymaxsize is always set: default value
        if (request.getParameter("mapclass_overlaymaxsize").length() > 0) {
            cl.setOverlayMaxSize(new Integer(request.getParameter("mapclass_overlaymaxsize")).intValue());
        }
        //minsize is always set: default value
        if (request.getParameter("mapclass_overlayminsize").length() > 0) {
            cl.setOverlayMinSize(new Integer(request.getParameter("mapclass_overlayminsize")).intValue());
        }
        //size is always set: default value
        if (request.getParameter("mapclass_overlaysize").length() > 0) {
            cl.setOverlaySize(new Integer(request.getParameter("mapclass_overlaysize")).intValue());
        }

        // now everything is up to date, regenerate a mapfile to keep map accurate
        //umb.generateUserMapfile();
        return;
    }
}
