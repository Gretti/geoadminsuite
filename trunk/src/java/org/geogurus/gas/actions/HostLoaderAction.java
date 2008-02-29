/*
 * DisplayHelpAction.java
 *
 * Created on 12 decembre 2006, 19:12
 *
 */

package org.geogurus.gas.actions;

import java.util.Enumeration;
import java.util.Iterator;
import org.geogurus.Datasource;
import org.geogurus.GeometryClass;
import org.geogurus.gas.utils.BeanPropertyUtil;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.sql.ConPool;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.objects.HostDescriptorBean;
import org.geogurus.gas.objects.ListHostDescriptorBean;


/**
 *
 * @author GNG
 */

public class HostLoaderAction extends Action {
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        HttpSession session = request.getSession();
        
        ActionForward forward = null;
        ActionErrors errors = null;
        /* V1
        ListHostDescriptorForm lhdf = (ListHostDescriptorForm) form;
        ListHostDescriptorBean listHost = lhdf.getListHost();
         */
        ListHostDescriptorBean listHostDescriptor = new ListHostDescriptorBean();
        
        
        Hashtable hostList = new Hashtable();
        Hashtable listHost = new Hashtable();
        Vector dss = null;
        HostDescriptorBean host = null;
        
        try {
            //Parses request parameters
            Enumeration en = request.getParameterNames();
            while (en.hasMoreElements()) {
                String nam = (String)en.nextElement();
                String[] vals = request.getParameterValues(nam);
                
                String curHostIndex = nam.substring(nam.indexOf("[") + 1, nam.indexOf("]"));
                String curParam = nam.substring(nam.indexOf(".") +1);
                if(listHost.keySet().contains(curHostIndex)){
                    host = (HostDescriptorBean)listHost.get(curHostIndex);
                } else {
                    host = new HostDescriptorBean();
                    listHost.put(curHostIndex,host);
                }
                BeanPropertyUtil.getSetter(curParam, host, vals);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //List is complete, must update session content to avoid losing list
        for(Iterator i = listHost.values().iterator(); i.hasNext();) {
            listHostDescriptor.addHost((HostDescriptorBean)i.next());
        }
        
        //for (int i=0; i < listHost.getNbHosts(); i++) {
        for (Iterator iteHost = listHost.keySet().iterator(); iteHost.hasNext();) {
            
            //host = listHost.getHost(i);
            host = (HostDescriptorBean)listHost.get(iteHost.next());
            
            dss = getDatasources(host);
            
            if (dss != null) {
                if (hostList.get(host.getName()) != null) {
                    // a list of datasources already exists for this host: add the new list to the existing one
                    ((Vector)hostList.get(host.getName())).addAll(dss);
                } else {
                    // cannot add a null object in hashtable skip it
                    hostList.put(host.getName(), dss);
                }
            }
        }
        
        session.setAttribute("listHostDescriptorBean",listHostDescriptor);
        session.setAttribute(ObjectKeys.HOST_LIST, hostList);
        //cleans session
        session.removeAttribute(ObjectKeys.CURRENT_GC);
        session.removeAttribute(ObjectKeys.CLASSIF_MESSAGE);
        session.removeAttribute(ObjectKeys.CLASSIF_TYPE);
        session.removeAttribute(ObjectKeys.TMP_CLASSIFICATION);
        
        //forward = mapping.findForward("mapgenerator");
        forward = mapping.findForward("mapCatalog");
        
        return forward;
    }
    
    /**
     * Returns a vector of Datasources for the given parameters
     */
    protected Vector getDatasources(HostDescriptorBean host) {
        Vector dss = null;
        if(!host.getName().equalsIgnoreCase("")) {
            dss = getDatasources(host.getName(),host.getPath(),host.getPort(),host.getUname(),host.getUpwd(),host.getType());
        }
        
        return dss;
    }
    
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
        } else if (type.equalsIgnoreCase("PG")) {
            // load datasource information from postgres table.
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            String query = null;
            try {
                con = ConPool.getConnection(name, port, "template1", uname, upwd, ConPool.DBTYPE_POSTGRES);
                if (con == null) {
                    // not a valid host: skip it
                    LogEngine.log("Cannot get a connection (null) for: " + name + " " + port + " " + uname + " *****");
                    return null;
                }
                stmt = con.createStatement();
                // this query gives all available databases for the given host, except template1
                query = "select datname from pg_database where datallowconn and datname <> 'template1' and datname <> 'postgres'";
                rs = stmt.executeQuery(query.toString());
                
                while (rs.next()) {
                    ds = new Datasource(rs.getString(1), port, Datasource.PG, name, uname, upwd);
                    if (ds.getDataInformation()) {
                        // a valid datasource containing geo data: either file or geo tables
                        res.add(ds);
                    }
                }
            } catch (SQLException sqle) {
                String error = "HostLoader getDatasources: SQLException: " + sqle.getMessage();
                error += "<br>query was: <code>" + query.toString() + "</code>";
                LogEngine.log(error);
                sqle.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (con != null) con.close();
                } catch (Exception sqle2) {sqle2.printStackTrace();}
            }
        } else if (type.equalsIgnoreCase("ORA")) {
            LogEngine.log("Retrieving from Oracle or DB2 not yet implemented");
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            String query = null;
            try {
                con = ConPool.getConnection(name, port, path, uname, upwd, ConPool.DBTYPE_ORACLE);
                if (con == null) {
                    // not a valid host: skip it
                    LogEngine.log("Cannot get a connection (null) for: " + name + " " + port + " " + uname + " *****");
                    return null;
                }
                
                stmt = con.createStatement();
                // this query gives all available databases for the given host, except template1
                query = "select TABLE_NAME from MDSYS.USER_SDO_GEOM_METADATA";
                
                rs = stmt.executeQuery(query.toString());
                ds = null;
                while (rs.next()) {
                    if(ds == null) {
                        ds = new Datasource(path, port, Datasource.ORA, name, uname, upwd);
                    }
                    GeometryClass gc = new GeometryClass(name, path, rs.getString(1), port, uname, upwd, GeometryClass.ORACLASS);
                    ds.getDataList().put(gc.getID(), gc);
                }
                
                if(ds != null) res.add(ds);
                
                stmt.close();
            } catch (SQLException sqle) {
                String error = "HostLoader getDatasources: SQLException: " + sqle.getMessage();
                error += "<br>query was: <code>" + query.toString() + "</code>";
                LogEngine.log(error);
                sqle.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (con != null) con.close();
                } catch (Exception sqle2) {sqle2.printStackTrace();}
            }
        }else if (type.equalsIgnoreCase("DB2")) {
            LogEngine.log("Retrieving from DB2 not yet implemented");
        }
        return res;
    }
    
}