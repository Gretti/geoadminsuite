/*
 * GOClassificationProperties.java
 *
 * Created on 14 ao�t 2002, 14:08
 */
/*
 * MSClassificationProperties.java
 *
 * Created on 9 ao�t 2002, 15:01
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.net.*;
import org.geogurus.gas.objects.ListClassesBean;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.DataManager;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.geogurus.GeometryClass;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.Geometry;
import org.geogurus.mapserver.objects.Label;
import org.geogurus.mapserver.objects.MapClass;
import org.geogurus.mapserver.objects.RGB;
import uk.ac.leeds.ccg.geotools.*;
/**
 * Servlet to deal with the geonline classification window 
 * It listens to MC_geonline_classification_properties.jsp page to create a list of classes
 * based on an attribute value = classitem
 *
 * @author  nri
 */
public class GOClassificationProperties extends BaseServlet {
    private final String mc_goclassif_jsp = "MC_geonline_classification_properties.jsp";
    private final String mc_golabel_jsp = "MC_geonline_label_properties.jsp";
    /**
     * Main method listening to client requests:
     * The JSP page can send to different commands:
     * "act=generate" to generate a new classification according to classification type
     * choosen by the user: a single class, a set of classes based on an attribute value,
     * a set of classes based on a range of values for numerical attribute.
     * each new single class is added to the list of class for the MSLayer object.
     * Each new set of classes replaced existing list of classes for the MSLAyer object.
     * 
     * Objects are stored in session:
     * The list of generated classes, under the "TMP_CLASSIFICATION" key;
     * The type of classification choosen by user, under the "CLASSIF_TYPE" key:
     * values are "RANGE", "UNIQUE", "SINGLE".
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
debugParameters(request);
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        String layerid = ((GeometryClass)session.getAttribute(ObjectKeys.CURRENT_GC)).getID();
        
        if (umb == null || layerid == null) {
            // session: expiration
            String error = "UserMapBean ou layerid manquant. La session a du expirer.";
            session.setAttribute(ObjectKeys.SERVLET_ERROR, error);
            dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
            return;
        }
        GeometryClass gc = (GeometryClass)umb.getUserLayerList().get(layerid);
        String classifType = request.getParameter("classificationtype");
        String classItem = request.getParameter("classitem");
        String action = request.getParameter("act");
        
        // first, stores user choices for this classification
        Label lab = doUpdate(request, gc);
        
        String message = "";
        if (action.equals("generate")) {
            if (classifType.equals("singleclass")) {
                message = classifySingleClass(request, session, gc);
            } else if (classifType.equals("uniquevalue")) {
                message = classifyUniqueValue(session, gc, classItem);
            } else if (classifType.equals("range")) {
                message = classifyRange(request, session, gc, classItem);
            } 
            // put a message in request for jsp pages, to know whats happened for this classif
            request.setAttribute(ObjectKeys.CLASSIF_MESSAGE, message);
            // with no refresh parameter
            dispatch(request, response, mc_goclassif_jsp);
            
        } else if (action.equals("save")) {
            validate(request, session, umb, layerid, lab);
            if (request.getParameter("advancedlabel") != null) {
                // redirect to advanced label config page
                dispatch(request, response, mc_golabel_jsp + "?refreshmap=true");
            } else {
                // redirect to classification config page
                dispatch(request, response, mc_goclassif_jsp + "?refreshmap=true");
            }
        }
    }
    
    /**
     * update the current GC' Layer with user-choices
     */
    protected Label doUpdate(HttpServletRequest request, GeometryClass gc) {
        Label lab = null;
        org.geogurus.mapserver.objects.Layer l = gc.getMSLayer(null,false);
        
        if (request.getParameter("advancedlabel") != null) {
            // user opens advanced label properties window. This window is only available
            // if a labelitem exists for the class(es), ie, a new label was already created
            
            if (l.getMapClass() != null && l.getMapClass().getNbClasses() > 0 && l.getMapClass().getClass(0) != null) {
                lab = (Label)((MapClass)l.getMapClass().getFirstClass()).getLabel();
            }
            if (lab == null) {
                lab = new Label();
                lab.setColor(new RGB(0,0,0));
            }
            
            l.setLabelAngleItem(request.getParameter("labelangleitem").length() == 0 ? null : request.getParameter("labelangleitem"));
            l.setLabelSizeItem(request.getParameter("labelsizeitem").length() == 0 ? null : request.getParameter("labelsizeitem"));
            // control of double values is done by JSP JS code
            l.setMinScale(
                request.getParameter("labelminscale").length() == 0 ? 
                    0.0 : 
                    new Double(request.getParameter("labelminscale")).doubleValue());
            l.setMaxScale(
                request.getParameter("labelmaxscale").length() == 0 ? 
                    0.0 : 
                    new Double(request.getParameter("labelmaxscale")).doubleValue());
            l.setSymbolScale(
                request.getParameter("symbolscale").length() == 0 ? 
                    0.0 : 
                    new Double(request.getParameter("symbolscale")).doubleValue());
                    
            //control is done by Javascript
            if (request.getParameter("label_angle").length() > 0) {
                String ang = request.getParameter("label_angle");
                if (ang.equalsIgnoreCase("auto")) {
                    lab.setAngle(Label.AUTO);
                } else {
                    lab.setAngle(new Double(ang).intValue());
                }
            } else {
                lab.setAngle(Label.UNDEF);
            }
            if (request.getParameter("label_font").length() > 0) {
                lab.setFont(request.getParameter("label_font"));
            }
            if (request.getParameter("label_color").length() > 0) {
                lab.setColor(new RGB(request.getParameter("label_color")));
            }
            // for background and outline color: empty value means null color
            if (request.getParameter("label_backgroundcolor").length() > 0) {
                lab.setBackgroundColor(new RGB(request.getParameter("label_backgroundcolor")));
            } else {
                lab.setBackgroundColor(null);
            }
            if (request.getParameter("label_outlinecolor").length() > 0) {
                lab.setOutlineColor(new RGB(request.getParameter("label_outlinecolor")));
            } else {
                lab.setOutlineColor(null);
            }
            String pos = request.getParameter("label_position");
            if (pos.equalsIgnoreCase("ul")) {
                lab.setPosition(Label.UL);
            } else if (pos.equalsIgnoreCase("uc")) {
                lab.setPosition(Label.UC);
            } else if (pos.equalsIgnoreCase("ur")) {
                lab.setPosition(Label.UR);
            } else if (pos.equalsIgnoreCase("cl")) {
                lab.setPosition(Label.CL);
            } else if (pos.equalsIgnoreCase("cc")) {
                lab.setPosition(Label.CC);
            } else if (pos.equalsIgnoreCase("cr")) {
                lab.setPosition(Label.CR);
            } else if (pos.equalsIgnoreCase("ll")) {
                lab.setPosition(Label.LL);
            } else if (pos.equalsIgnoreCase("lc")) {
                lab.setPosition(Label.LC);
            } else if (pos.equalsIgnoreCase("lr")) {
                lab.setPosition(Label.LR);
            } else if (pos.equalsIgnoreCase("auto")) {
                lab.setPosition(Label.AUTO);
            }
            if (request.getParameter("label_size").length() > 0) {
                String s = request.getParameter("label_size");
                if (s.equalsIgnoreCase("tiny")) {
                    lab.setSize(Label.TINY);
                } else if (s.equalsIgnoreCase("medium")) {
                    lab.setSize(Label.MEDIUM);
                } else if (s.equalsIgnoreCase("large")) {
                    lab.setSize(Label.LARGE);
                } else if (s.equalsIgnoreCase("small")) {
                    lab.setSize(Label.SMALL);
                } else if (s.equalsIgnoreCase("giant")) {
                    lab.setSize(Label.GIANT);
                } else {
                    lab.setSize(new Integer(s).intValue());
                }
            }
            String typ = request.getParameter("label_type");
            if (typ.equalsIgnoreCase("bitmap")) {
                lab.setType(Label.BITMAP);
            } else if (typ.equalsIgnoreCase("truetype")) {
                lab.setType(Label.TRUETYPE);
            }
        } else {
            // normal classification JSP page
            if (request.getParameter("labels") != null) {
                // user chooosed to labelise the classes, construct a simple label
                lab = new Label();
                lab.setColor(new RGB(0,0,0));
                
                if (request.getParameter("labelitem").length() > 0) {
                    l.setLabelItem(request.getParameter("labelitem"));
                }
            } else {
                // removes the labelitem: label checkbox is not choosen
                l.setLabelItem(null);
            }
            l.setClassItem(request.getParameter("classitem"));
        }
        return lab;
    }
    /**
     * perform a classification by looking for all distinct values for the given attribute
     */
    protected String classifyUniqueValue(HttpSession session, GeometryClass gc, String classitem) {
        String message = "";
        // the mapserver class limit
        int classLimit;
        String s = DataManager.getProperty("MAPSERVER_CLASS_LIMIT");
        classLimit = (s == null ? 44 : new Integer(s).intValue()); 
        
        if (gc.getDatasourceType() == GeometryClass.PGCLASS || gc.getDatasourceType() == GeometryClass.ORACLASS) {
            message = classifyUniqueValueFromDB(session, gc, classitem, classLimit);
            
        } else if (gc.getDatasourceType() == GeometryClass.ESRIFILECLASS) {
            message  = classifyUniqueValueFromFile(session, gc, classitem, classLimit);
        }
        // list storage in session was done in the called methods
        // removes any previous key that could exist in session
        session.setAttribute(ObjectKeys.CLASSIF_TYPE, new Short(ObjectKeys.UNIQUE));
        
        return message;
    }
    /**
     * Classify unique value by selecting distinct values from shape file:
     * Constructs shapefileReader here
     * @return The generated message, destinated to JSP page
     */
    protected String classifyUniqueValueFromFile(HttpSession session, GeometryClass gc, String classitem, int classLimit) {
        String message = "";
        ArrayList list = new ArrayList();
        //gets all unique values for the given column, generates classes
        // brute force: construct a new Hashtable indexed by the values =>
        try {
            String fileName = gc.getDatasourceName() + System.getProperty("file.separator") + gc.getTableName();
            int idx = fileName.lastIndexOf(".");
            URL url = new URL("file:///" + fileName.replace('\\', '/'));
            ShapefileReader sfr = new ShapefileReader(url);
            
            // gets column data
            GeoData data =  sfr.readData(classitem);
            Hashtable vals = new Hashtable(data.getSize());
            // keys are ints in data's hashtable
            for (Enumeration en = data.getIds(); en.hasMoreElements();) {
                Integer i = (Integer)en.nextElement();
                vals.put(data.getText(i.intValue()), i);
            }
            // now loops through created hash to create Layer classes.
            int count = 0;
            MapClass cl = null;
            String s = null;
            
            for (Enumeration en = vals.keys(); en.hasMoreElements();) {
                s = (String)en.nextElement();
                
                if (count++ < classLimit) {
                    cl = new MapClass();
                    cl.setColor(((ColorGenerator)session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor());
                    cl.setOutlineColor(new RGB(0,0,0));
                    
                    if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
                        cl.setSymbol("gasdefsympoint");
                        cl.setSize(3);
                    }
                    // trim expression and name: expression with leading or trailing spaces are not
                    // handled correctly by MapServer
                    cl.setName(s.trim());
                    cl.setExpression(s.trim());
                    list.add(cl);
                } else {
                    message = "classlimitation," + classLimit;
                    break;
                }
            }
            sfr = null;
            System.gc();
        } catch (Exception e) {
            // either a bad URL or another exception: should track it
            e.printStackTrace();
        }
        // stores the vector in the request
        session.setAttribute(ObjectKeys.TMP_CLASSIFICATION, list);
        return message;
    }
    
    /**
     * Classify unique value by selecting distinct values from the db datasource
     * @return The generated message, destinated to JSP page
     */
    protected String classifyUniqueValueFromDB(HttpSession session, GeometryClass gc, String classitem, int classLimit) {
        Connection con = null;
        String query = null;
        String message = "";
        try {
            if(gc.getDatasourceType() == GeometryClass.PGCLASS)
                con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), ConPool.DBTYPE_POSTGRES);
            else if (gc.getDatasourceType() == GeometryClass.ORACLASS)
                con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), ConPool.DBTYPE_ORACLE);
            if (con == null) {
                return "null connection";
            }
            Statement stmt = con.createStatement();
            // this query gives all classitem attribute distinct values where geographic field is not null, 
            // and construct new classes for each one
            // caution when db is not Postgis enabled
            query = "select distinct " + classitem + " from " + gc.getTableName() + " where " + gc.getColumnName();
            query += " is not null";
            ResultSet rs = stmt.executeQuery(query);
            MapClass cl = null;
            ArrayList list = new ArrayList();
            int count = 0;
            
            while (rs.next()) {
                if (count++ < classLimit) {
                    cl = new MapClass();
                    cl.setColor(((ColorGenerator)session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor());
                    cl.setOutlineColor(new RGB(0,0,0));
                    
                    if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
                        cl.setSymbol("gasdefsympoint");
                        cl.setSize(3);
                    }
                    //getString is null if attribute is null in the table
                    String s = rs.getString(1);
                    s = (s == null) ? null : s.trim();
                    // trim expression and name: expression with leading or trailing spaces are not
                    // handled correctly by MapServer
                    cl.setName(s);
                    cl.setExpression(s);
                    list.add(cl);
                } else {
                    message = "classlimitation," + classLimit;
                    break;
                }
            }
            stmt.close();
            // stores the generated List in the request
            session.setAttribute(ObjectKeys.TMP_CLASSIFICATION, list);
            return message;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception sqle2) {sqle2.printStackTrace();}
        }
        return message;
    }
    /**
     * creates a new class with a generated color
     */
    protected String classifySingleClass(HttpServletRequest request, HttpSession session, GeometryClass gc) {
        MapClass cl = new MapClass();
        // generated color:
        RGB color = ((ColorGenerator)session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor();
        cl.setColor(color);
        cl.setOutlineColor(new RGB(0,0,0));
        
        // symbol
        if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
            cl.setSymbol("gasdefsympoint");
            cl.setSize(3);
        }
        // set a name based on the number of classes existing for the currentlayer:
        if (gc.getNullMsLayer().getMapClass() == null) {
            cl.setName("class_0");
        } else {
             cl.setName("class_" + gc.getNullMsLayer().getMapClass().getNbClasses());
        }
        // set an empty expression:
        cl.setExpression("");
        // stores this class in session
        ArrayList list = new ArrayList(1);
        list.add(cl);
        // stores a key in request telling type is single class:
        session.setAttribute(ObjectKeys.CLASSIF_TYPE, new Short(ObjectKeys.SINGLE));
        session.setAttribute(ObjectKeys.TMP_CLASSIFICATION, list);
        return "";
    }
    
    /**
     * performs a classification by computing a range of values based on the number of classes choosen by user.
     * Looks at the GC type and call appropriate method to perform the classif
     */
    protected String classifyRange(HttpServletRequest request, HttpSession session, GeometryClass gc, String classitem) {
        // the message for JSP
        String message = "";
        String s = DataManager.getProperty("MAPSERVER_CLASS_LIMIT");
        // the mapserver class limit
        int classLimit = (s == null ? 44 : new Integer(s).intValue()); 
        session.setAttribute(ObjectKeys.CLASSIF_TYPE, new Short(ObjectKeys.RANGE));
        int numClasses = new Integer(request.getParameter("numclasses")).intValue();
        // the array of min and max value for the concerned attribute
        double[] minMax = null;
        
        // very the class limit:
        if (numClasses > classLimit) {
            numClasses = classLimit;
            message = "classlimitation," + classLimit;
        }
        
        // Look at the sql type for the classitem: only numeric types are allowed for ranges
        String[] colinfo = null;
        for (Iterator iter = gc.getColumnInfo().iterator(); iter.hasNext();) {
            colinfo = (String[])iter.next();
            if (colinfo[0].equals(classitem)) {
                // the good column name
                if (!colinfo[1].equalsIgnoreCase("int2") &&
                    !colinfo[1].equalsIgnoreCase("int4") &&
                    !colinfo[1].equalsIgnoreCase("int8") &&
                    !colinfo[1].equalsIgnoreCase("float4") &&
                    !colinfo[1].equalsIgnoreCase("float8") &&
                    !colinfo[1].equalsIgnoreCase("decimal") &&
                    !colinfo[1].equalsIgnoreCase("float") &&
                    !colinfo[1].equalsIgnoreCase("double") &&
                    !colinfo[1].equalsIgnoreCase("numeric")) {
                    // choosen column name is not elligible for range classif
                    LogEngine.log("GOClassificationProperties.classifyRange(): invalid data type col info is: " + colinfo[1]);                        
                    return "classrange," + colinfo[1];
                } else {
                    // bingo
                    break;
                }
            }
        }
        // according to gc type, call appropriate method
        if (gc.getDatasourceType() == GeometryClass.PGCLASS || gc.getDatasourceType() == GeometryClass.ORACLASS) {
            minMax = getMinMaxFromDB(gc, classitem);
            
        } else if (gc.getDatasourceType() == GeometryClass.ESRIFILECLASS) {
            minMax  = getMinMaxFromFile(gc, classitem);
        }
        double range = minMax[1]-minMax[0];
        double step = range / numClasses;
        double curmin = minMax[0];
        ArrayList list = new ArrayList();
        MapClass cl = null;
        StringBuffer exp = null;
        NumberFormat f = NumberFormat.getInstance(Locale.US);
         if (f instanceof DecimalFormat) {
            ((DecimalFormat) f).applyPattern("0.000");
        }
        
        String className = null;
        
        // build the classes and the range formulae for each one
        for (int i = 0; i < numClasses; i++) {
            cl = new MapClass();
            cl.setColor(((ColorGenerator)session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor());
            cl.setOutlineColor(new RGB(0,0,0));
            if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
                cl.setSymbol("gasdefsympoint");
                cl.setSize(3);
            }
            // build the expression (MapServer Logical Expression) and format numeric
            // values to avoid scientific notation: "1.2E3" must be written "1200"
            exp = new StringBuffer("([");
            exp.append(classitem).append("] >= ").append(f.format(curmin));
            className = ">=" + f.format(curmin) + " - ";
            
            if (i < numClasses -1) {
                // strict inequality to avoid overlap
                exp.append(" AND [").append(classitem).append("] < ");
                className += "<";
            } else {
                // non-strict inequality to allow last value to be represented
                exp.append(" AND [").append(classitem).append("] <= ");
                className += "<=";
            }
            curmin += step;
            exp.append(f.format(curmin)).append(")");
            className += f.format(curmin);
            
            cl.setExpression(exp.toString());
            // name is now the expression itself
            cl.setName(className);
            list.add(cl);
        }
        // stores the generated List in the request
        session.setAttribute(ObjectKeys.TMP_CLASSIFICATION, list);
        return message;
    }
    /**
     * looks for the min and max value of the given attribute in a gc of type ESRICLASS
     */
    protected double[] getMinMaxFromFile(GeometryClass gc, String classitem) {
        double[] res = new double[2];
        try {
            String fileName = gc.getDatasourceName() + System.getProperty("file.separator") + gc.getTableName();
            int idx = fileName.lastIndexOf(".");
            URL url = new URL("file:///" + fileName.replace('\\', '/'));
            ShapefileReader sfr = new ShapefileReader(url);
            
            // gets column data
            GeoData data =  sfr.readData(classitem);
            res[0] = data.getMin();
            res[1] = data.getMax();
            sfr = null;
            System.gc();
        } catch (Exception e) {
            // either a bad URL or another exception: should track it
            e.printStackTrace();
        }
        return res;
    }
    
    /**
     * looks for the min and max value of the given attribute in a gc of type DBCLASS
     */
    protected double[] getMinMaxFromDB(GeometryClass gc, String classitem) {
        Connection con = null;
        String query = null;
        double[] res = new double[2];
        
        // fetch min and max
        try {
            if(gc.getDatasourceType() == GeometryClass.PGCLASS)
                con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), ConPool.DBTYPE_POSTGRES);
            else if (gc.getDatasourceType() == GeometryClass.ORACLASS)
                con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), ConPool.DBTYPE_ORACLE);
            if (con == null) {
                return res;
            }
            Statement stmt = con.createStatement();
            // this query gives all classitem attribute distinct values where geographic field is not null, 
            // and construct new classes for each one
            // caution when db is not Postgis enabled
            query = "select min(" + classitem + "), max(" + classitem + ") from " + gc.getTableName() + " where " + gc.getColumnName();
            query += " is not null";
//LogEngine.log("Datasource.getDataInformation: query: " + query);
            ResultSet rs = stmt.executeQuery(query);
            double min=0, max=0;
            if (rs.next()) {
                res[0] = rs.getDouble(1);
                res[1] = rs.getDouble(2);
            }
            stmt.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception sqle2) {sqle2.printStackTrace();}
        }
        return res;
    }
    
    /**
     * sets all classes after user validation
     */
    protected void validate(HttpServletRequest request, HttpSession session, UserMapBean umb, String layerid, Label lab) {
        MapClass cl = new MapClass();
        GeometryClass gc = (GeometryClass)umb.getUserLayerList().get(layerid);
        // the list of classes to update according to user-choosen properties:
        // this list will be either a tmp list previously generated by this servlet (CLASSIF_TYPE = ObjectKeys.UNIQUE or
        // ObjectKeys.RANGE), or the original layer list of classes in case of null CLASSIF_TYPE
        ListClassesBean tmpClasses = null;
        Short classifType = (Short)session.getAttribute(ObjectKeys.CLASSIF_TYPE);
        
        if (classifType == null) {
            // cycle directly through Layer's classes
            tmpClasses = gc.getNullMsLayer().getMapClass();
        } else if (classifType.shortValue() == ObjectKeys.UNIQUE || 
                   classifType.shortValue() == ObjectKeys.SINGLE || 
                   classifType.shortValue() == ObjectKeys.RANGE) {
            // cycle through created tmp classes
            tmpClasses = (ListClassesBean)session.getAttribute(ObjectKeys.TMP_CLASSIFICATION);
        }
        // sets all properties for classes, according to user choice
        String s = null;
        for (Iterator iter = tmpClasses.getClasses(); iter.hasNext();) {
            cl = (MapClass)iter.next();
            
            if (request.getParameter("advancedlabel") == null) {
                // save request comes from Classification page, update class(es) properties AND label
                int id = cl.getID();
                // try to get all forms parameters guessed from MapClass id to set the class properties
                s = "c" + id + "_check";
                if (request.getParameter(s) == null) {
                    // current class was excluded from the list of classes. Removes it from this arrayList
                    iter.remove();
                    continue;
                }
                s = "c" + id + "_color";
                // color may be null (no fill for this layer)
                if (request.getParameter(s).length() > 0 && !request.getParameter(s).equalsIgnoreCase("null")) {
                    cl.setColor(new RGB(request.getParameter(s)));
                }
                s = "" + id + "_name";
                cl.setName(request.getParameter(s).length() > 0 ? request.getParameter(s) : null);
                s = "" + id + "_expression";
                cl.setExpression(request.getParameter(s).length() > 0 ? request.getParameter(s) : null);
            }
            // in all cases (save from classification or from label props,
            // sets label object according to user choice, this label was created in doUpdate
            // and is applied to all classes.
            cl.setLabel(lab);
        }
        if (classifType != null && classifType.shortValue() == ObjectKeys.SINGLE) {
            // transfers classes from tmp class list to layer class list: only one class in tmp list, normally
            gc.getNullMsLayer().setMapClass(tmpClasses);
        } else if (classifType != null && (classifType.shortValue() == ObjectKeys.UNIQUE || classifType.shortValue() == ObjectKeys.RANGE)) {
            // adds the new one
            gc.getNullMsLayer().setMapClass(tmpClasses);
        } // in case of null classifType, cycle was through original list, nothing to do
        
        // cleans the session by removing tmp list and classif type 
        request.removeAttribute("TMP_CLASSIFICATION");
        // removes the single class request attribute
        session.removeAttribute("CLASSIF_TYPE");
        // now everything is up to date, regenerate a mapfile to keep map accurate
        //umb.generateUserMapfile();
    }
}
