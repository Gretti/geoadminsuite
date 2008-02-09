/*
 * MSLayerProperties.java
 *
 * Created on 8 ao�t 2002, 14:12
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
public class MSLayerProperties extends BaseServlet {
    protected final String mc_mslayer_jsp = "MC_mapserver_layer_properties.jsp";
    
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
        Layer l = ((GeometryClass)umb.getUserLayerList().get(layerid)).getMSLayer(new RGB(0,0,0),false);
        // l cannot be null as it is built by getMSLayer
        
        l.setConnection(request.getParameter("ms_connection"));
        
        String conType = request.getParameter("ms_connectiontype");
        
        if (conType.equals("local")) {
            l.setConnectionType(Layer.LOCAL);
        } else if (conType.equals("ogr")) {
            l.setConnectionType(Layer.OGR);
        } else if (conType.equals("oraclespatial")) {
            l.setConnectionType(Layer.ORACLESPATIAL);
        } else if (conType.equals("postgis")) {
            l.setConnectionType(Layer.POSTGIS);
        } else if (conType.equals("sde")) {
            l.setConnectionType(Layer.SDE);
        }
        
        
        l.setData(request.getParameter("ms_data"));
        
        if (request.getParameter("ms_group").length() > 0) 
            l.setGroup(request.getParameter("ms_group"));
        
        if (request.getParameter("ms_maxscale").length() > 0) {
            l.setMaxScale(new Double(request.getParameter("ms_maxscale")).doubleValue());
        } else {
            l.setMaxScale(0.0);
        }
        
        if (request.getParameter("ms_minscale").length() > 0) {
            l.setMinScale(new Double(request.getParameter("ms_minscale")).doubleValue());
        } else {
            l.setMinScale(0.0);
        }
        
        if (request.getParameter("ms_filter").length() > 0) 
            l.setFilter(request.getParameter("ms_filter"));
        
        if (request.getParameter("ms_filteritem").length() > 0) 
            l.setFilterItem(request.getParameter("ms_filteritem"));
        
        if (request.getParameter("ms_footer").length() > 0) 
            l.setFooter(new File(request.getParameter("ms_footer")));
        
        if (request.getParameter("ms_header").length() > 0) 
            l.setHeader(new File(request.getParameter("ms_header")));
        
        if (request.getParameter("ms_labelangleitem").length() > 0) 
            l.setLabelAngleItem(request.getParameter("ms_labelangleitem"));
        
        if (request.getParameter("ms_labelitem").length() > 0) 
            l.setLabelItem(request.getParameter("ms_labelitem"));
        
        if (request.getParameter("ms_labelsizeitem").length() > 0) 
            l.setLabelSizeItem(request.getParameter("ms_labelSizeitem"));
        
        l.setLabelCache(request.getParameter("ms_labelcache") != null ? Layer.ON : Layer.OFF);
        
        if (request.getParameter("ms_labelmaxscale").length() > 0) 
            l.setLabelMaxScale(new Double(request.getParameter("ms_labelmaxscale")).doubleValue());
        
        if (request.getParameter("ms_labelrequires").length() > 0) 
            l.setLabelRequires(request.getParameter("ms_labelrequires"));
        
        if (request.getParameter("ms_name").length() > 0) 
            l.setName(request.getParameter("ms_name"));
        
        if (request.getParameter("ms_offsite").length() > 0) 
            l.setOffSite(new Integer(request.getParameter("ms_offsite")).intValue());
        
        l.setPostLabelCache((request.getParameter("ms_postlabelcache") != null));
        
        String sizeUnits = request.getParameter("ms_sizeunits");
        
        if (sizeUnits.equals("pixels")) {
            l.setSizeUnits(Layer.PIXELS);
        } else if (sizeUnits.equals("inches")) {
            l.setSizeUnits(Layer.INCHES);
        } else if (sizeUnits.equals("meters")) {
            l.setSizeUnits(Layer.METERS);
        } else if (sizeUnits.equals("kilometers")) {
            l.setSizeUnits(Layer.KILOMETERS);
        } else if (sizeUnits.equals("miles")) {
            l.setSizeUnits(Layer.MILES);
        } else if (sizeUnits.equals("feet")) {
            l.setSizeUnits(Layer.FEET);
        }
        
        if (request.getParameter("ms_symbolscale").length() > 0) 
            l.setSymbolScale(new Double(request.getParameter("ms_symbolscale")).doubleValue());
        
        if (request.getParameter("ms_template").length() > 0) 
            l.setTemplate(request.getParameter("ms_template"));
        
        if (request.getParameter("ms_tileindex").length() > 0) 
            l.setTileIndex(new File(request.getParameter("ms_tileindex")));
        
        if (request.getParameter("ms_tileitem").length() > 0) 
            l.setTileItem(request.getParameter("ms_tileitem"));
        
        if (request.getParameter("ms_tolerance").length() > 0) 
            l.setTolerance(new Double(request.getParameter("ms_tolerance")).doubleValue());
        
        String status = request.getParameter("ms_status");
        
        if (status.equals("ON")) {
            l.setStatus(Layer.ON);
        } else if (status.equals("OFF")) {
            l.setStatus(Layer.OFF);
        } else {
            l.setStatus(Layer.DEFAULT);
        }
        l.setClassItem(request.getParameter("ms_classitem"));
        
        String tolUnits = request.getParameter("ms_toleranceunit");
        
        if (tolUnits.equals("pixels")) {
            l.setToleranceUnit(Layer.PIXELS);
        } else if (tolUnits.equals("inches")) {
            l.setToleranceUnit(Layer.INCHES);
        } else if (tolUnits.equals("meters")) {
            l.setToleranceUnit(Layer.METERS);
        } else if (tolUnits.equals("kilometers")) {
            l.setToleranceUnit(Layer.KILOMETERS);
        } else if (tolUnits.equals("miles")) {
            l.setToleranceUnit(Layer.MILES);
        } else if (tolUnits.equals("feet")) {
            l.setToleranceUnit(Layer.FEET);
        }
        
        l.setTransform(request.getParameter("ms_transform") != null);
        
        l.setTransparency(new Integer(request.getParameter("ms_transparency")).intValue());
        
        String type = request.getParameter("ms_type");
        
        if (type.equals("point")) {
            l.setType(Layer.POINT);
        } else if (type.equals("line")) {
            l.setType(Layer.LINE);
        } else if (type.equals("polyline")) {
            l.setType(Layer.POLYLINE);
        } else if (type.equals("polygon")) {
            l.setType(Layer.POLYGON);
        } else if (type.equals("annotation")) {
            l.setType(Layer.ANNOTATION);
        } else if (type.equals("raster")) {
            l.setType(Layer.RASTER);
        } else if (type.equals("queryonly")) {
            l.setType(Layer.QUERYONLY);
        }
        // now everything is up to date, regenerate a mapfile to keep map accurate
        //umb.generateUserMapfile();
        return;
    }
}
