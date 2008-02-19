/*
 * TestServerAction.java
 *
 * Created on 10 janvier 2007, 23:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geogurus.gas.actions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.raster.RasterRegistrar;
import org.geogurus.tools.sql.ConPool;

/**
 *
 * @author Administrateur
 */
public class TestServerAction extends Action {
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        String type = request.getParameter("type");
        String name = request.getParameter("name");
        String path = request.getParameter("path");
        String port = request.getParameter("port");
        String uname = request.getParameter("uname");
        String upwd = request.getParameter("upwd");
        
        String serverState = "";
        int nbGeoDs = 0;
        try {
            if(type.equalsIgnoreCase("folder")) {
                //Try to find folder
                File filepath = new File(path);
                String[] list = filepath.list();
                
                if (list == null || list.length == 0){
                    serverState = "ko";
                } else {
                    serverState = "ok";
                    for (int i = 0; i < list.length; i++) {
                        if (getValidExtension(path, list[i])){
                            nbGeoDs++;
                        }
                    }
                }
                
            } else if (type.equalsIgnoreCase("PG")) {
                Connection con = ConPool.getConnection(name,port,"template1",uname,upwd, ConPool.DBTYPE_POSTGRES);
                //try to connect to datasource
                serverState = con == null ? "ko" : "ok";
                if (con != null) {
                    Statement stmt = con.createStatement();
                    // this query gives all available databases for the given host, except template1
                    String query = "select datname from pg_database where datallowconn and datname <> 'template1' and datname <> 'postgres'";
                    ResultSet rs = stmt.executeQuery(query.toString());
                    while (rs.next()) nbGeoDs++;
                    rs.close();
                    stmt.close();
                    con.close();
                }
            } else if (type.equalsIgnoreCase("oracle")) {
                Connection con = ConPool.getConnection(name, port, path, uname, upwd, ConPool.DBTYPE_ORACLE);
                //try to connect to datasource
                serverState = con == null ? "ko" : "ok";
                if (con != null) {
                    Statement stmt = con.createStatement();
                    // this query gives all available datasources
                    String query = "select count(TABLE_NAME) from MDSYS.USER_SDO_GEOM_METADATA";
                    ResultSet rs = stmt.executeQuery(query.toString());
                    rs.next();
                    nbGeoDs = rs.getInt(1);
                    rs.close();
                    stmt.close();
                    con.close();
                }
            }
        } catch (Exception e) {
            serverState = "ko";
        }
        
        String resp = "{'state':'" + serverState + "','nbds':" + new Integer(nbGeoDs) + "}";
        
        response.setContentType("application/x-json");
        Writer out = response.getWriter();
        out.write(resp);
        out.flush();
        out.close();

        return null;
    }
    
    private boolean getValidExtension(String path, String fn) {
        // first gets extention:
        String ext = "";
        String begin = "";
        int pt = fn.lastIndexOf(".");
        boolean valid = false;
        try {
            ext = fn.substring(pt+1);
            begin = fn.substring(0, pt);
        } catch (IndexOutOfBoundsException e) {}
        if (ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")) {
            // verify if a tfw exists, or a wld, or
            if (RasterRegistrar.isGeoTIFF(path + File.separator + fn) ||
                    new File(path + File.separator + begin + ".tfw").exists() ||
                    new File(path + File.separator + begin + ".TFW").exists() ||
                    new File(path + File.separator + begin + ".wld").exists() ||
                    new File(path + File.separator + begin + ".WLD").exists()) {
                valid = true;
            }
        } else if (ext.equalsIgnoreCase("img")) {
            // verify if a tfw exists, or a wld
            if (new File(path + File.separator + begin + ".wld").exists() ||
                    new File(path + File.separator + begin + ".WLD").exists()) {
                valid = true;
            }
        } else if (ext.equalsIgnoreCase("shp")) {
            // verify if .dbf + .shx exists
            if ((new File(path + File.separator + begin + ".dbf").exists()  && new File(path + File.separator + begin + ".shx").exists()) ||
                    (new File(path + File.separator + begin + ".DBF").exists()) && new File(path + File.separator + begin + ".SHX").exists()) {
                valid = true;
            }
        } else if(ext.equalsIgnoreCase("ecw")) {
            // verify if the associated ers file exists (temporary)
            if (new File(path + File.separator + begin + ".ers").exists()  ||
                    new File(path + File.separator + begin + ".ERS").exists()) {
                valid = true;
            }
        }
        return valid;
    }
}