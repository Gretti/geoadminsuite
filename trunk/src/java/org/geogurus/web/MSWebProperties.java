/*
 * MSWebProperties.java
 *
 * Created on 13 janvier 2003, 14:17
 */
package org.geogurus.web;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;

/**
 * Servlet to deal with a Web object, and its corresponding HTML configuration page:
 * MS_mapserver_web_properties.jsp
 * @author  nri
 */
public class MSWebProperties extends BaseServlet {
    protected final String mc_msweb_jsp = "MC_mapserver_web_properties.jsp";
    
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
        doUpdate(request, umb);
        // put in the request a parameter to refresh the mapfile in the main window
        dispatch(request, response, mc_msweb_jsp + "?refreshmap=true");
    }
    
    /**
     * update the current Mapfile object with user-modified parameters.
     * The current mapfile is stored in the UserMapBean bean
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb) {
        org.geogurus.mapserver.objects.Web web = umb.getMapfile().getWeb();
        
        if (request.getParameter("web_empty").length() > 0) {
            //try {
                web.setEmpty(request.getParameter("web_empty"));
            //} catch (MalformedURLException mue) {
            //    web.setEmpty(null);
            //    mue.printStackTrace();
            //}
        } else {
            web.setEmpty(null);
        }
        if (request.getParameter("web_error").length() > 0) {
            try {
                web.setError(new URL(request.getParameter("web_error")));
            } catch (MalformedURLException mue) {
                web.setError(null);
                mue.printStackTrace();
            }
        } else {
            web.setError(null);
        }
        if (request.getParameter("web_footer").length() > 0) {
            web.setFooter(new File(request.getParameter("web_footer")));
        } else {
            web.setFooter(null);
        }
        if (request.getParameter("web_header").length() > 0) {
            web.setHeader(new File(request.getParameter("web_header")));
        } else {
            web.setHeader(null);
        }
        if (request.getParameter("web_imagepath").length() > 0) {
            web.setImagePath(new File(request.getParameter("web_imagepath")));
        } else {
            web.setImagePath(null);
        }
        if (request.getParameter("web_imageurl").length() > 0) {
            web.setImageURL(request.getParameter("web_imageurl"));
        } else {
            web.setImageURL(null);
        }
        if (request.getParameter("web_log").length() > 0) {
            web.setLog(new File(request.getParameter("web_log")));
        } else {
            web.setLog(null);
        }
        if (request.getParameter("web_maxscale").length() > 0 && 
            !request.getParameter("web_maxscale").equals("0") &&
            !request.getParameter("web_maxscale").equals("0.0")) {
            web.setMaxScale(new Double(request.getParameter("web_maxscale")).doubleValue());
        } else {
            web.setMaxScale(-1.0);
        }
        if (request.getParameter("web_minscale").length() > 0 && 
            !request.getParameter("web_minscale").equals("0") &&
            !request.getParameter("web_minscale").equals("0.0")) {
            web.setMinScale(new Double(request.getParameter("web_minscale")).doubleValue());
        } else {
            web.setMinScale(-1.0);
        }
        if (request.getParameter("web_maxtemplate").length() > 0) {
            web.setMaxTemplate(request.getParameter("web_maxtemplate"));
        } else {
            web.setMaxTemplate(null);
        }
        if (request.getParameter("web_mintemplate").length() > 0) {
            web.setMinTemplate(request.getParameter("web_mintemplate"));
        } else {
            web.setMinTemplate(null);
        }
        if (request.getParameter("web_template").length() > 0) {
            web.setTemplate(request.getParameter("web_template"));
        } else {
            web.setTemplate(null);
        }
        
        //metadata object to handle
       
        umb.getMapfile().setWeb(web);
        //umb.generateUserMapfile();
    }
}
