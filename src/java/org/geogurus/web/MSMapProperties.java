/*
 * MSMapProperties.java
 *
 * Created on 21 ao�t 2002, 11:38
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.Vector;
import java.util.StringTokenizer;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import java.awt.Color;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.Dimension;
import org.geogurus.GeometryClass;
import org.geogurus.mapserver.objects.MSExtent;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.SymbolSet;
/**
 * Servlet to deal with a Map object, and its corresponding HTML configuration page:
 * MC_mapserver_map_properties.jsp
 * @author  nri
 */
public class MSMapProperties extends BaseServlet {
    protected final String mc_msmap_jsp = "MC_mapserver_map_properties.jsp";
    
    /**
     * Main method listening to client requests:
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        debugParameters(request);
        
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        
        if (umb == null) {
            // session: expiration
            String error = "UserMapBean ou layerid manquant. La session a du expirer.";
            session.setAttribute(ObjectKeys.SERVLET_ERROR, error);
            dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
            return;
        }
        boolean  b = doUpdate(request, umb);
        // put in the request a parameter to refresh the mapfile in the main window
        dispatch(request, response, mc_msmap_jsp + "?refreshmap=true&sizechanged=" + b);
    }
    /**
     * update the current Mapfile object with user-modified parameters.
     * The current mapfile is stored in the UserMapBean bean
     */
    protected boolean doUpdate(HttpServletRequest request, UserMapBean umb) {
        org.geogurus.mapserver.objects.Map map = umb.getMapfile();
        boolean sizeChanged = false;
        
        //control is done by Javascript
        StringTokenizer tok = new StringTokenizer(request.getParameter("map_extent"));
        map.setExtent(new MSExtent(new Double(tok.nextToken()).doubleValue(),
        new Double(tok.nextToken()).doubleValue(),
        new Double(tok.nextToken()).doubleValue(),
        new Double(tok.nextToken()).doubleValue()));
        map.setImageColor(new RGB(request.getParameter("map_imagecolor")));
        
        String imgType = request.getParameter("map_imagetype");
        if (imgType.equals("gif")) {
            map.setImageType(Map.GIF);
        } else if (imgType.equals("jpeg")) {
            map.setImageType(Map.JPEG);
        }  else if (imgType.equals("png")) {
            map.setImageType(Map.PNG);
        } else if (imgType.equals("wbmp")) {
            map.setImageType(Map.WBMP);
        }
        map.setImageQuality(new Integer(request.getParameter("map_imagequality")).intValue());
        map.setInterlace(
        (request.getParameter("map_interlace").equals("map_interlace_on") ? Map.ON : Map.OFF));
        
        if (request.getParameter("map_name").length() > 0) {
            map.setName(request.getParameter("map_name"));
        }
        // control the mapsize to see if it changed. If so, a parameter is passsed to JSP page telling
        // map page should be reloaded.
        Dimension dim = new Dimension(new Integer(request.getParameter("map_width")).intValue(),
        new Integer(request.getParameter("map_height")).intValue());
        if (dim.width != map.getSize().width || dim.height != map.getSize().height) {
            sizeChanged = true;
            map.setSize(dim);
        }
        map.setResolution(new Integer(request.getParameter("map_resolution")).intValue());
        
        if (request.getParameter("map_scale").length() > 0) {
            map.setScale(new Double(request.getParameter("map_scale")).doubleValue());
        }
        
        map.setStatus(
        request.getParameter("map_status").equals("map_status_on") ? Map.ON : Map.OFF);
        
        if (request.getParameter("map_fontset").length() > 0) {
            map.setFontSet(new File(request.getParameter("map_fontset")));
        }
        if (request.getParameter("map_shapepath").length() > 0) {
            map.setShapePath(new File(request.getParameter("map_shapepath")));
        }
        if (request.getParameter("map_symbolset").length() > 0) {
            SymbolSet symbolSet = new SymbolSet();
            try{
                BufferedReader br = new BufferedReader(new FileReader(new File(request.getParameter("map_symbolset"))));
                symbolSet.load(br);
                map.setSymbolSet(symbolSet);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        map.setTransparent(
        (request.getParameter("map_transparent").equals("map_transparent_on") ? Map.ON : Map.OFF));
        
        String units = request.getParameter("map_units");
        if (units.equalsIgnoreCase("meters")) {
            map.setUnits(Map.METERS);
        } else if (units.equalsIgnoreCase("kilometers")) {
            map.setUnits(Map.KILOMETERS);
        }  else if (units.equalsIgnoreCase("miles")) {
            map.setUnits(Map.MILES);
        } else if (units.equalsIgnoreCase("feet")) {
            map.setUnits(Map.FEET);
        } else if (units.equalsIgnoreCase("inches")) {
            map.setUnits(Map.INCHES);
        } else if (units.equalsIgnoreCase("dd")) {
            map.setUnits(Map.DD);
        }
        // rebuild a new mapfile
        umb.setMapfile(map);
        //umb.generateUserMapfile();
        return sizeChanged;
    }
}
