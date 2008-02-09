/*
 * ListProjection.java
 *
 * Created on 07 octobre 2002, 12:00
 */
package org.geogurus.web;
import javax.servlet.http.*;
import java.util.*;
import org.geogurus.Datasource;
import java.sql.*;
import java.io.*;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.tools.LogEngine;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.GeometryClass;
/**
 * Servlet to create hashtable of supported projection based on the epsg resource file
 * This hashtable is stored in session into the PROJECTION_LIST key
 * @author  gng
 */
public class ListProjection extends BaseServlet {
    private final String listprojection_jsp = "listProjection.jsp";
    /**
     * Parses a epsg file :
     * Describe the parser
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
 
        Vector majorProj = new Vector();
        String projFile = getServletConfig().getServletContext().getRealPath("");
        String curpath = (String)session.getAttribute("curpath");
        int idx = 0;
        try {
            // check to see if path ends with a /
            if (projFile.lastIndexOf(System.getProperty("file.separator")) != projFile.length() -1) {
                // tmp modiction, but value saved in other variables.
                projFile += System.getProperty("file.separator");
            }
            projFile += "msFiles";
            projFile += System.getProperty("file.separator");
            projFile += "projection";
            projFile += System.getProperty("file.separator");
            projFile += "epsg";
            BufferedReader in = new BufferedReader(new FileReader(projFile));
            String s = "";
            String paramLine = "";
            int i = 0;
            while ((s = in.readLine()) != null) {
                if((s.startsWith("#"))&&(!s.startsWith("# Unable"))&&(s != "null")){
                    if(s.substring(2).equalsIgnoreCase(request.getParameter("pastproj"))){
                        idx = i;
                    }
                    paramLine = in.readLine();
                    majorProj.add(s.substring(2) + "|" + paramLine.substring(paramLine.indexOf(">") + 1));
                    i++;
                }
            }
            in.close();
        } catch (Exception e) {
            log(e.getMessage());
        }
        
        session.setAttribute(ObjectKeys.PROJECTION_LIST, majorProj);
        request.setAttribute("idx", new Integer(idx));
        request.setAttribute("curpath", curpath);
        // dispatch now to the tree page
        dispatch(request, response, listprojection_jsp + "?layer=" + request.getParameter("layer") + "&type=" + request.getParameter("type") + "&caller=" + request.getParameter("caller"));            
    }
}
