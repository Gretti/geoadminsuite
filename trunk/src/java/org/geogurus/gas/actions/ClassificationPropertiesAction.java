/*
 * LayerPropertiesAction.java
 *
 * Created on 5 f�vrier 2007, 22:58
 */
package org.geogurus.gas.actions;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.Geometry;
import org.geogurus.GeometryClass;
import org.geogurus.gas.forms.LayerForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.gas.objects.ListClassesBean;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Label;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.MapClass;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.tools.DataManager;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.web.ColorGenerator;
import uk.ac.leeds.ccg.geotools.GeoData;
import uk.ac.leeds.ccg.geotools.ShapefileReader;

/**
 * Updates the current user mapfile according to given parameters, 
 * @author GNG
 * @version
 */
public class ClassificationPropertiesAction extends Action {

    /* forward name="success" path="" */
    private final String CLASSIF_FORWARD = "classificationProperties";
    private final String LABEL_FORWARD = "labelProperties";

    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        HttpSession session = request.getSession();
        LayerForm layerForm = (LayerForm) form;
        Layer paramLayer = layerForm.getDefaultMsLayer();
        GeometryClass gc = (GeometryClass) session.getAttribute(ObjectKeys.CURRENT_GC);
        String success = "";
        String message = "";
        // first, stores user choices for this classification
        Label lab = doUpdate(request, gc, paramLayer, layerForm);
        if(paramLayer.getClassItem() != null && paramLayer.getClassItem().length() > 0) {
            gc.getDefaultMsLayer().setClassItem(paramLayer.getClassItem());
        } else {
            gc.getDefaultMsLayer().setClassItem(null);
        }

        if (layerForm.getAct().equals("generate")) {
            if (layerForm.getClassificationType().equals("singleclass")) {
                message = classifySingleClass(request, session, gc);
            } else if (layerForm.getClassificationType().equals("uniquevalue")) {
                message = classifyUniqueValue(session, gc, paramLayer.getClassItem());
            } else if (layerForm.getClassificationType().equals("range")) {
                message = classifyRange(request, session, gc, paramLayer.getClassItem());
            }
            // put a message in request for jsp pages, to know whats happened for this classif
            request.setAttribute(ObjectKeys.CLASSIF_MESSAGE, message);
            // with no refresh parameter
            success = CLASSIF_FORWARD;

        } else if (layerForm.getAct().equals("save")) {
            gc = validate(request, session, gc, lab);
            //Updates and Rewrites user map
            UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
            umb.getUserLayerList().put(gc.getID(), gc);

            UserMapBeanManager manager = new UserMapBeanManager();
            manager.setUserMapBean(umb);
            manager.generateUserMapfile((ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR));
            //Prepare forward and keys for map refreshing
            success = CLASSIF_FORWARD;
            request.setAttribute(ObjectKeys.REFRESH_KEY, ObjectKeys.REFRESH_KEY);
        }

        return mapping.findForward(success);

    }

    /**
     * update the current GC' Layer with user-choices
     */
    protected Label doUpdate(HttpServletRequest request, GeometryClass gc, Layer paramLayer, LayerForm form_) {
        Label lab = null;
        Layer l = gc.getMSLayer(null, false);

        if (request.getParameter("advancedlabel") != null) {
            // user opens advanced label properties window. This window is only available
            // if a labelitem exists for the class(es), ie, a new label was already created

            if (l.getMapClass() != null && l.getMapClass().getNbClasses() > 0 && l.getMapClass().getClass(0) != null) {
                lab = (Label) ((MapClass) l.getMapClass().getClass(0)).getLabel();
            }
            if (lab == null) {
                lab = new Label();
                lab.setColor(new RGB(0, 0, 0));
            }

            l.setLabelAngleItem(paramLayer.getLabelAngleItem());
            l.setLabelSizeItem(paramLayer.getLabelSizeItem());
            // control of double values is done by JSP JS code
            l.setMinScale(paramLayer.getLabelMinScale());
            l.setMaxScale(paramLayer.getLabelMaxScale());
            l.setSymbolScale(paramLayer.getSymbolScale());

            //control is done by Javascript
            if (paramLayer.getLabelAngleItem().length() > 0) {
                lab.setAngle(new Double(paramLayer.getLabelAngleItem()).intValue());
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
            if (form_.getLabels() != null && request.getParameter("defaultMsLayer.labelItem").length() > 0) {
                // user chooosed to labelise the classes, construct a simple label
                lab = new Label();
                lab.setColor(new RGB(0, 0, 0));

                l.setLabelItem(paramLayer.getLabelItem());
            } else {
                // removes the labelitem: label checkbox is not choosen
                l.setLabelItem(null);
            }
            if (paramLayer.getClassItem() != null && request.getParameter("defaultMsLayer.classItem").length() > 0) {
                l.setClassItem(paramLayer.getClassItem());
            } else {
                // removes the labelitem: label checkbox is not choosen
                l.setLabelItem(null);
            }
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
            message = classifyUniqueValueFromFile(session, gc, classitem, classLimit);
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
        ListClassesBean list = new ListClassesBean();
        //gets all unique values for the given column, generates classes
        // brute force: construct a new Hashtable indexed by the values =>
        try {
            String fileName = gc.getDatasourceName();
            if (!gc.getDatasourceName().endsWith("\\") && !gc.getDatasourceName().endsWith("/")) {
                fileName += File.separator;
            }
            fileName += gc.getTableName();
            fileName = fileName.replace('\\', '/');
            URL url = new URL("file:///" + fileName);
            ShapefileReader sfr = new ShapefileReader(url);

            // gets column data
            GeoData data = sfr.readData(classitem);
            Hashtable vals = new Hashtable(data.getSize());
            // keys are ints in data's hashtable
            for (Enumeration en = data.getIds(); en.hasMoreElements();) {
                Integer i = (Integer) en.nextElement();
                vals.put(data.getText(i.intValue()), i);
            }
            // now loops through created hash to create Layer classes.
            int count = 0;
            MapClass cl = null;
            String s = null;

            for (Enumeration en = vals.keys(); en.hasMoreElements();) {
                s = (String) en.nextElement();

                if (count++ < classLimit) {
                    cl = new MapClass();
                    cl.setColor(((ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor());
                    cl.setOutlineColor(new RGB(0, 0, 0));

                    if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
                        cl.setSymbol("gasdefsympoint");
                        cl.setSize(3);
                    }
                    // trim expression and name: expression with leading or trailing spaces are not
                    // handled correctly by MapServer
                    cl.setName(s.trim());
                    cl.setExpression(s.trim());
                    list.addClass(cl);
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
            if (gc.getDatasourceType() == GeometryClass.PGCLASS) {
                con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), ConPool.DBTYPE_POSTGRES);
            } else if (gc.getDatasourceType() == GeometryClass.ORACLASS) {
                con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), ConPool.DBTYPE_ORACLE);
            }
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
            ListClassesBean list = new ListClassesBean();
            int count = 0;

            while (rs.next()) {
                if (count++ < classLimit) {
                    cl = new MapClass();
                    cl.setColor(((ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor());
                    cl.setOutlineColor(new RGB(0, 0, 0));

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
                    list.addClass(cl);
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
            } catch (Exception sqle2) {
                sqle2.printStackTrace();
            }
        }
        return message;
    }

    /**
     * creates a new class with a generated color
     */
    protected String classifySingleClass(HttpServletRequest request, HttpSession session, GeometryClass gc) {
        MapClass cl = new MapClass();
        // generated color:
        RGB color = ((ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor();
        cl.setColor(color);
        cl.setOutlineColor(new RGB(0, 0, 0));

        // symbol
        if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
            cl.setSymbol("gasdefsympoint");
            cl.setSize(3);
        }
        // set a name based on the number of classes existing for the currentlayer:
        if (gc.getMSLayer(null, false).getMapClass() == null) {
            cl.setName("class_0");
        } else {
            cl.setName("class_" + gc.getNullMsLayer().getMapClass().getNbClasses());
        }
        // set an empty expression:
        cl.setExpression("");
        // stores this class in session
        ListClassesBean list = new ListClassesBean();
        list.addClass(cl);
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
        int numClasses = new Integer(request.getParameter("numClasses")).intValue();
        // the array of min and max value for the concerned attribute
        double[] minMax = null;

        // very the class limit:
        if (numClasses > classLimit) {
            numClasses = classLimit;
            message = "classlimitation," + classLimit;
        }

        // Look at the sql type for the classitem: only numeric types are allowed for ranges
        String colName;
        boolean numeric;
        GeometryClassFieldBean gcfb;
        for (Iterator iter = gc.getColumnInfo().iterator(); iter.hasNext();) {
            gcfb = (GeometryClassFieldBean) iter.next();
            colName = gcfb.getName();
            if (colName.equals(classitem)) {
                numeric = gcfb.isNumeric();
                if (numeric) {
                    //Field found
                    break;
                } else {
                    return "classrange," + colName;
                }
            }
        }
        // according to gc type, call appropriate method
        if (gc.getDatasourceType() == GeometryClass.PGCLASS || gc.getDatasourceType() == GeometryClass.ORACLASS) {
            minMax = getMinMaxFromDB(gc, classitem);

        } else if (gc.getDatasourceType() == GeometryClass.ESRIFILECLASS) {
            minMax = getMinMaxFromFile(gc, classitem);
        }
        double range = minMax[1] - minMax[0];
        double step = range / numClasses;
        double curmin = minMax[0];
        ListClassesBean list = new ListClassesBean();
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
            cl.setColor(((ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor());
            cl.setOutlineColor(new RGB(0, 0, 0));
            if (gc.getType() == Geometry.POINT || gc.getType() == Geometry.MULTIPOINT) {
                cl.setSymbol("gasdefsympoint");
                cl.setSize(3);
            }
            // build the expression (MapServer Logical Expression) and format numeric
            // values to avoid scientific notation: "1.2E3" must be written "1200"
            exp = new StringBuffer("([");
            exp.append(classitem).append("] >= ").append(f.format(curmin));
            className = ">=" + f.format(curmin) + " - ";

            if (i < numClasses - 1) {
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
            list.addClass(cl);
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
            String fileName = gc.getDatasourceName();
            if (!gc.getDatasourceName().endsWith("\\") && !gc.getDatasourceName().endsWith("/")) {
                fileName += File.separator;
            }
            fileName += gc.getTableName();
            fileName = fileName.replace('\\', '/');
            URL url = new URL("file:///" + fileName);
            ShapefileReader sfr = new ShapefileReader(url);

            // gets column data
            GeoData data = sfr.readData(classitem);
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
     * looks for the min and max value of the given attribute in a gc of type PGCLASS or ORACLASS
     */
    protected double[] getMinMaxFromDB(GeometryClass gc, String classitem) {
        Connection con = null;
        String query = null;
        double[] res = new double[2];

        // fetch min and max
        try {
            if (gc.getDatasourceType() == GeometryClass.PGCLASS) {
                con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), ConPool.DBTYPE_POSTGRES);
            } else if (gc.getDatasourceType() == GeometryClass.ORACLASS) {
                con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), ConPool.DBTYPE_ORACLE);
            }
            if (con == null) {
                return res;
            }
            Statement stmt = con.createStatement();
            // this query gives all classitem attribute distinct values where geographic field is not null,
            // and construct new classes for each one
            // caution when db is not Postgis enabled
            query = "select min(" + classitem + "), max(" + classitem + ") from " + gc.getTableName() + " where " + gc.getColumnName();
            query += " is not null";
            ResultSet rs = stmt.executeQuery(query);
            double min = 0, max = 0;
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
            } catch (Exception sqle2) {
                sqle2.printStackTrace();
            }
        }
        return res;
    }

    /**
     * sets all classes after user validation
     */
    protected GeometryClass validate(HttpServletRequest request, HttpSession session, GeometryClass gc, Label lab) {
        MapClass cl = new MapClass();
        // the list of classes to update according to user-choosen properties:
        // this list will be either a tmp list previously generated by this servlet (CLASSIF_TYPE = ObjectKeys.UNIQUE or
        // ObjectKeys.RANGE), or the original layer list of classes in case of null CLASSIF_TYPE
        ListClassesBean tmpClasses = null;
        Short classifType = (Short) session.getAttribute(ObjectKeys.CLASSIF_TYPE);

        if (classifType == null) {
            // cycle directly through Layer's classes
            tmpClasses = gc.getNullMsLayer().getMapClass();
        } else if (classifType.shortValue() == ObjectKeys.UNIQUE ||
                classifType.shortValue() == ObjectKeys.SINGLE ||
                classifType.shortValue() == ObjectKeys.RANGE) {
            // cycle through created tmp classes
            tmpClasses = (ListClassesBean) session.getAttribute(ObjectKeys.TMP_CLASSIFICATION);
        }
        // sets all properties for classes, according to user choice
        String s = null;
        for (Iterator iter = tmpClasses.getClasses(); iter.hasNext();) {
            cl = (MapClass) iter.next();

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
        if (classifType != null) {
            // removes all existing classes from layer, and adds all tmp ones
            gc.getNullMsLayer().setMapClass(tmpClasses);
        }


        // cleans the session by removing tmp list and classif type
        session.removeAttribute("TMP_CLASSIFICATION");
        // removes the single class request attribute
        session.removeAttribute("CLASSIF_TYPE");
        //Returns updated GeometryClass so that it can be stored in session and usermapbean
        return gc;
    }
}
