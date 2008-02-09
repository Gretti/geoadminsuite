/*
 * GOMapfileLoader.java
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
import org.geogurus.tools.sql.ConPool;
import org.geogurus.GeometryClass;
import org.geogurus.Geometry;
// this package is tailored to handle file upload.
// There is a licence problem for commercial use
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
/**
 * This servlet deals with Mapfile upload. It creates a UserMapfileBean to store pertinent
 * information about the uploaded mapfile (correctness, etc.)
 */
public class GOMapfileLoader extends BaseServlet {
    protected final String mc_loaded_mapfile_jsp = "MC_loaded_mapfile.jsp";
    private final String fs = System.getProperty("file.separator");
    
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        response.setContentType("text/html");
        if (session == null) {
            debugToClient(response, "process: null session: cannot get session from request ?!");
            return;
        }
        
        // the path to the folder where user mapfiles are saved
        String tmpMaps = getServletContext().getRealPath("");
        tmpMaps += fs + "msFiles" + fs + "tmpMaps";
        // builds an o'reilly MultipartRequest object to handle file upload.
        // this class allows to control where the file will be saved
        MultipartRequest mpRequest = null;
        try {
            mpRequest = new MultipartRequest(request, tmpMaps, 1024*1024, "ISO-8859-1", new DefaultFileRenamePolicy());
        } catch (Throwable th) {
            // mapfile cannot be retrieved.
            debugToClient(response, "MultipartRequest failed: " + th.getMessage());
            return;
        }
        // gets the uploaded file: only one expected, so do not deal
        // with several files
        Enumeration en = mpRequest.getFileNames();
        String fn = null;
        while (en.hasMoreElements()) {
            fn = (String)en.nextElement();
        }
        // gets the uploaded file, give it a standart user mapfile name
        // (user_ + session id)
        File upMap = mpRequest.getFile(fn);
        // creates the UserMapfileBean
        UserMapfileBean umfb = new UserMapfileBean(upMap);
        umfb.checkMapfile();
        
        // stores this in session:
        session.setAttribute(ObjectKeys.USER_MAPFILE_BEAN, umfb);
        
        dispatch(request, response, mc_loaded_mapfile_jsp);
    }
}