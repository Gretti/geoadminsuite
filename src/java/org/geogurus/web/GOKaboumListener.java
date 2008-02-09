/*
 * GOKaboumListener.java
 *
 * Created on 26 ao�t 2002, 15:40
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.GeometryClass;
import org.geogurus.Geometry;
import org.geogurus.topology.TopologyFactory;

/**
 * The servlet receiving requests from GAS kaboum client (SIG part).
 * This servlet is derived from SIGKaboumListener in geonline core package.
 * Should merge the 2 servlets to provide only one way to validate kaboum commands,
 * not matter session objects (SIGUserParameters or UserMapBean) are.
 * Supported commands are:
 * action=remove
 * action=validate
 * action=update
 *
 */
public class GOKaboumListener extends BaseServlet {
    protected final String mc_gokaboum_jsp = "MC_kaboum_validation_message.jsp";
    
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            debugToClient(response, "process: null session: cannot get session from request ?!");
            return;
        }
        // cleans session for any existing previous message or error:
        session.removeAttribute("SIG_MESSAGE");
        session.removeAttribute("SIG_ERROR");
        
        String action = request.getParameter("action");
        
        if (action == null) {
            // a client CANNOT call this servlet directly. The only supported interface is:
            // action=validate, action=remove, action=selection
            return;
        }
        
        if (action.equals("debug")) {
        } else if (action.equals("validate")) {
            doValidate(request, response);
        } else if (action.equals("remove")) {
            doRemove(request, response);
        } else if (action.equals("selection")) {
            doSelection(request, response);
        } else if (action.equals("topology")) {
            // topology operation is special: no dispatch, but direct response to server
            // (silent client-server communication
            doTopology(request, response);
        }
    }
    
    /**
     * Validates the given object. This is the minimum task to do:
     * request parameters are: action=validate&value=<kaboum object validation>
     * where <kaboum object validation> is (see kaboum API for more details):
     * OBJECT|CLASS_NAME|OBJID|AREA|WKT.
     */
    protected void doValidate(HttpServletRequest request,HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        
        if (umb == null) {
            // session invalidated
            session.setAttribute(ObjectKeys.SIG_ERROR, "doValidate: session expired. Reload the application");
            dispatch(request, response, mc_gokaboum_jsp);
        }
        String kaboumObj = request.getParameter("value");
        
        if (kaboumObj == null) {
            session.setAttribute(ObjectKeys.SIG_ERROR, "doValidate: invalid parameters: expected value=<kaboum object> for action=validate");
            dispatch(request, response, mc_gokaboum_jsp);
            return;
        }
        StringTokenizer tk = new StringTokenizer(kaboumObj, "|");
        // discard "OBJECT" keyword
        tk.nextToken();
        String classid = tk.nextToken();
        String geoid = tk.nextToken();
        // discard "area" value
        tk.nextToken();
        String wkt = tk.nextToken();
System.out.println("doValidate: receving WKT from kaboum: " + wkt);        
        
        GeometryClass curgeo = (GeometryClass)umb.getUserLayerList().get(classid);
        // construct a geometry from this object, with its id !
        Geometry geom = new Geometry(geoid, curgeo.getType(), wkt, curgeo.getTableName());
        
        // ask the corresponding geometry class to update/insert this object
        if (!curgeo.addGeometryToDB(geom)) {
            if (!geom.id.equals("NEW")) {
                //attempt to modify an existing object: must reload it into kaboum to redraw it:
                umb.setUpdatedGeometry(classid + "|" + geoid+ "|" + wkt);
            }
            session.setAttribute(ObjectKeys.SIG_ERROR, "doValidate: cannot add geometry to db: " + curgeo.getErrorMessage());
            dispatch(request, response, mc_gokaboum_jsp);
            return;
        }
        // make this kaboum geometry available to JSP pages, for ex to redraw it in kaboum
        umb.setUpdatedGeometry(classid + "|" + geoid+ "|" + wkt);
        session.setAttribute(ObjectKeys.SIG_MESSAGE, "Geometry from class: " + curgeo.getName()
        + " (id=" + geoid + ") correctly added to DB.");
        
        dispatch(request, response, mc_gokaboum_jsp);
    }
    
    /**
     * test for cropvision
     */ 
    protected void doTopology(HttpServletRequest request,HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        
        if (umb == null) {
            // session invalidated
            session.setAttribute(ObjectKeys.SIG_ERROR, "doTopology: session expired. Reload the application");
            dispatch(request, response, mc_gokaboum_jsp);
        }
        String kaboumObj = request.getParameter("value");
        
        if (kaboumObj == null) {
            session.setAttribute(ObjectKeys.SIG_ERROR, "doTopology: invalid parameters: expected value=<kaboum object> for action=topology");
            dispatch(request, response, mc_gokaboum_jsp);
            return;
        }
        StringTokenizer tk = new StringTokenizer(kaboumObj, "|");
        // discard "TOPOLOGY" keyword
        tk.nextToken();
        String operation = tk.nextToken();
        String classid = tk.nextToken();
        String geoid = tk.nextToken();
        // discard "area" value
        tk.nextToken();
        String wkt = tk.nextToken();
        
        // perform test union
        //wkt = test(wkt, operation);
        wkt = test(wkt, "");
        // rebuild a kaboum object (name, id, wkt)
        wkt = classid + "|" + geoid + "|" + wkt;
        // topology operation is special: no dispatch, but direct response to server
        // (silent client-server communication)
        try {
            PrintWriter out = response.getWriter();
            out.write("<html><script>parent.document.kaboum.standbyOff();\n");
            out.write("parent.document.kaboum.kaboumCommand(\"OBJECT|");
            out.write(wkt);
            out.write("\");\n");
            out.write("parent.document.kaboum.kaboumCommand(\"EDITION\");\n");
            out.write("</script></html");
            out.close();
        } catch (IOException ioe) {
        }
    }
    /**
     * Query the db to find all overlapping polygons
     * and performs intersection or union or...
     */
    private String test(String wkt, String operation) {
        try {
            TopologyFactory tf = new TopologyFactory();
            Connection con = ConPool.getConnection("pc-dev1", "5432", "cvscot", "postgres", "postgres", "POSTGRES");
            Statement stmt = con.createStatement();
            String query = "select geo_value from parcelle where box3d(geometryFromText('";
            query += wkt + "', -1)) && geo_value";
            ResultSet rs = stmt.executeQuery(query);
System.out.println("query is: " + query);
            // stores WKT
            Vector vec = new Vector();
            while (rs.next()) {
                vec.add(rs.getString(1));
            }
            
            if (vec.size() == 0) {
                System.out.println("no objects");
                return "";
            }
            
            String cur = (String)vec.get(0);
            
            if (vec.size() > 1) {
                // make union of all overlapping    
                for (int i = 1; i < vec.size(); i++) {
                    cur = tf.getWKT(tf.union(cur, (String)vec.get(i)));
                }
            }
            // stores the union for control
            query = "update control set geo_value=geometryFromText('";
            query += cur + "', -1) where id=1";
System.out.println("update query is: " + query);
            stmt.executeUpdate(query);
            
            
            String res = tf.getWKT(tf.subtract(wkt, cur));
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * Removes the given object.
     * action=remove&value=<kaboum_object>&hardremove=<true|false>
     * where <kabom_object> is a valid kaboum geometry
     * hardremove is true to delete the entire record from the db, false to set the geometry to null
     * in the DB.<br>
     * Deals with kaboumv2 and v3
     */
    protected void doRemove(HttpServletRequest request,HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        
        if (umb == null) {
            // session invalidated
            session.setAttribute(ObjectKeys.SIG_ERROR, "doRemove: session expired. Reload the application");
            dispatch(request, response, mc_gokaboum_jsp);
        }
        String kaboumObj = request.getParameter("value");
        String hr = request.getParameter("hardremove");
        boolean hardRemove = (hr == null || hr.equals("false")) ? false : true;
        
        if (kaboumObj == null) {
            session.setAttribute(ObjectKeys.SIG_ERROR, "doRemove: invalid parameters: expected value=<kaboum object> for action=remove");
            dispatch(request, response, mc_gokaboum_jsp);
            return;
        }
        // extracts kaboum representation for this object: deals with v2 and v3 commands
        StringTokenizer tk = new StringTokenizer(kaboumObj, "|");
        tk.nextToken();
        String classid = tk.nextToken();
        String geoid = tk.nextToken();
        
        GeometryClass curgeo = (GeometryClass)umb.getUserLayerList().get(classid);
        
        if (! curgeo.removeGeometryFromDB(geoid, hardRemove)) {
            session.setAttribute(ObjectKeys.SIG_ERROR, "doRemove: cannot add geometry to db: " + curgeo.getErrorMessage());
            dispatch(request, response, mc_gokaboum_jsp);
            return;
        }
        // now geometry is removed, update SIGUserParameters: there is no more a valid UpdatedObject:
        umb.setUpdatedGeometry(null);
        
        // put message in session in all cases, to allow clients to get it
        session.setAttribute(ObjectKeys.SIG_MESSAGE, "Geometry (id=" + geoid + ", from layer: " + curgeo.getName() + ") correctly removed.");
        
        dispatch(request, response, mc_gokaboum_jsp);
    }
    
    /**
     * Selection for the given object(s).
     * User can extend this servlet and override this method to provide custom treatment.
     * No treatment provided by default.
     */
    protected void doSelection(HttpServletRequest request,HttpServletResponse response) {
    }
}