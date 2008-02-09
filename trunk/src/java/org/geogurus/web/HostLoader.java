/*
 * HostLoader.java
 *
 * Created on 31 juillet 2002, 20:42
 */
package org.geogurus.web;

import org.geogurus.Datasource;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.sql.ConPool;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet to create a hashtable of hosts and their datasources based on the host name
 * This hashtable is stored in session into the HOST_LIST key
 * Should improve the error handling mechanism: no more debugToClient, but redirect to error page
 * with error message in session.
 * @author  nri
 */
public class HostLoader extends BaseServlet {
    private final String mapgenerator_jsp = "MapGenerator.jsp";
    /**
     * Receives a list of hosts as http parameter for the server_list value:
     * host=<host_name>,<host_path>,<host_port>,<host_username>,<host_pwd>,<host_type>|<next_host>...
     *
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        
        String serverList = request.getParameter("server_list");
        Hashtable hostList = new Hashtable();
        StringTokenizer tok1 = new StringTokenizer(serverList, "|");
        StringTokenizer tok2 = null;
        Vector dss = null;
        String nme = null;
        String path = null;
        String port = null;
        String user = null;
        String pwd = null;
        String type = null;
        
        while (tok1.hasMoreTokens()) {
            tok2 = new StringTokenizer(tok1.nextToken(), ",");
            // compulsory values
            int num = tok2.countTokens();
            nme = tok2.nextToken();
            path = tok2.nextToken();
            port = tok2.nextToken();
            // optionnal values
            if (num == 4) {
                type = tok2.nextToken();
            } else if (num == 5) {
                user = tok2.nextToken();
                type = tok2.nextToken();
            } else  if (num == 6) {
                user = tok2.nextToken();
                pwd = tok2.nextToken();
                type = tok2.nextToken();
            } 
            
            dss = getDatasources(nme,path,port,user,pwd,type);
            
            if (dss != null) {
                if (hostList.get(nme) != null) {
                    // a list of datasources already exists for this host: add the new list to the existing one
                    ((Vector)hostList.get(nme)).addAll(dss);
                } else {
                    // cannot add a null object in hashtable skip it
                    hostList.put(nme, dss);
                }
            }
        }
        session.setAttribute(ObjectKeys.HOST_LIST, hostList);
        // dispatch now to the tree page
        dispatch(request, response, mapgenerator_jsp);
    }
    
    /**
     * Returns a vector of Datasources for the given parameters
     */
    protected Vector getDatasources(String name, String path, String port, String uname, String upwd, String type) {
        Vector res = new Vector();
        Datasource ds = null;
        
        if (type.equalsIgnoreCase("FOLDER")) {
            // a new file datasource is passed
            ds = new Datasource(path, null, Datasource.FOLDER, name, uname, upwd);
            
            if (ds.getDataInformation()) {
                // a valid datasource containing geo data: either file or geo tables
                res.add(ds);
            } else {
                // prints debug message
                LogEngine.log("getDataInformation failed on datasource: " + name + "\n\tmessage is: " + ds.errorMessage);
            }
        } else if (type.equals("PG")) {
            // load datasource information from postgres table.
            Connection con = null;
            String query = null;
            try {
                con = ConPool.getConnection(name, port, "template1", uname, upwd, "postgres");
                if (con == null) {
                    // not a valid host: skip it
                    LogEngine.log("Cannot get a connection (null) for: " + name + " " + port + " " + uname + " *****");
                    return null;
                }
                Statement stmt = con.createStatement();
                // this query gives all available databases for the given host, except template1
                query = "select datname from pg_database where datallowconn and datname <> 'template1' and datname <> 'postgres'";
                ResultSet rs = stmt.executeQuery(query.toString());
                
                while (rs.next()) {
                    ds = new Datasource(rs.getString(1), port, Datasource.PG, name, uname, upwd);
                    if (ds.getDataInformation()) {
                        // a valid datasource containing geo data: either file or geo tables
                        res.add(ds);
                    }
                }
                stmt.close();
            } catch (SQLException sqle) {
                String error = "HostLoader getDatasources: SQLException: " + sqle.getMessage();
                error += "<br>query was: <code>" + query.toString() + "</code>";
                LogEngine.log(error);
                sqle.printStackTrace();
            } finally {
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (Exception sqle2) {sqle2.printStackTrace();}
            }
        }
        if (type.equals("ORA") || type.equals("DB2")) {
            LogEngine.log("Retrieving from Oracle or DB2 not yet implemented");
        }
        return res;
    }
}
