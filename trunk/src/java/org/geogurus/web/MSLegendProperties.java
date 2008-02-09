/*
 * MSLegendProperties.java
 *
 * Created on 21 ao�t 2002, 13:57
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
import java.awt.Dimension;
import org.geogurus.GeometryClass;
import org.geogurus.mapserver.objects.Legend;
import org.geogurus.mapserver.objects.RGB;
/**
 * Servlet to deal with a Legend object, and its corresponding HTML configuration page:
 * MS_mapserver_legend_properties.jsp
 * @author  nri
 */
public class MSLegendProperties extends BaseServlet {
    protected final String mc_msleg_jsp = "MC_mapserver_legend_properties.jsp";
    
    /**
     * Main method listening to client requests:
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        //debugParameters(request);
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        
        if (umb == null) {
            // session: expiration
            String error = "UserMapBean ou layerid manquant. La session a du expirer.";
            session.setAttribute(ObjectKeys.SERVLET_ERROR, error);
            dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
            return;
        }
        doUpdate(request, umb);
        // put in the request a parameter to refresh the mapfile in the main window
        request.setAttribute(ObjectKeys.REFRESH_KEY,ObjectKeys.REFRESH_KEY);
        dispatch(request, response, mc_msleg_jsp);
    }
    /**
     * update the current Mapfile object with user-modified parameters.
     * The current mapfile is stored in the UserMapBean bean
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb) {
        org.geogurus.mapserver.objects.Legend leg = umb.getMapfile().getLegend();
        
        //control is done by Javascript
        leg.setImageColor(new RGB(request.getParameter("legend_imagecolor")));
        leg.setOutlineColor(new RGB(request.getParameter("legend_outlinecolor")));
        
        leg.setPosition(Byte.parseByte(request.getParameter("position")));
        
        leg.setKeySize(new Dimension(new Double(request.getParameter("keySize.width")).intValue(),
                new Double(request.getParameter("keySize.height")).intValue()));
        leg.setKeySpacing(new Dimension(new Double(request.getParameter("keySpacing.width")).intValue(),
                new Double(request.getParameter("keySpacing.height")).intValue()));
        
        leg.setInterlace(Byte.parseByte(request.getParameter("interlace")));
        leg.setTransparence(Byte.parseByte(request.getParameter("transparence")));
        leg.setPostLabelCache(Boolean.parseBoolean(request.getParameter("postLabelCache")));
        
        leg.setStatus(Byte.parseByte(request.getParameter("status")));
        if (request.getParameter("template.path").length() > 0) {
            leg.setTemplate(new File(request.getParameter("template.path")));
        }
        umb.getMapfile().setLegend(leg);
        //umb.generateUserMapfile();
    }
}
