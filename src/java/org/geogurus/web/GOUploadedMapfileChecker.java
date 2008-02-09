/*
* GOUploadedMapfileChecker
 *
 * Created on 16 f�vrier 2003, 15:30
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.sql.*;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.GeometryClass;
import org.geogurus.Geometry;
// this package is tailored to handle file upload.
// There is a licence problem for commercial use
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import org.geogurus.mapserver.MapFile;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.Layer;
/**
 *
 */
public class GOUploadedMapfileChecker extends BaseServlet {
    protected final String mg_loadedmap_jsp = "MC_loaded_mapfile.jsp";
    protected final String map_composer_jsp = "MapComposer.jsp";
    
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        //response.setContentType("text/html");
        
        debugParameters(request);
        
        if (session == null) {
            debugToClient(response, "process: null session: cannot get session from request ?!");
            return;
        }
        // the action parameter telling what to do:
        // upload file if action parameter = upload, or check given paramters
        String action = request.getParameter("action");
        
        UserMapfileBean umfb = (UserMapfileBean)session.getAttribute(ObjectKeys.USER_MAPFILE_BEAN);
        if (umfb.checkMapfileCorrections(request.getParameterMap()) || request.getParameter("valid") != null) {
            // must prepare all objects for the MapComposer Page
            //prepareUserMapBean(request, session);
            dispatch(request, response, map_composer_jsp);
        } else {
            dispatch(request, response, mg_loadedmap_jsp);
        }
    }
    
}