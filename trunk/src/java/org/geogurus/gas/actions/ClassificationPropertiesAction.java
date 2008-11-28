/*
 * LayerPropertiesAction.java
 *
 * Created on 5 f�vrier 2007, 22:58
 */
package org.geogurus.gas.actions;

import org.geogurus.data.operations.MinMaxAttributeOperation;
import org.geogurus.data.operations.UniqueValueFeatureClassification;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.forms.LayerForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.gas.objects.ListClassesBean;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.LegendGenerator;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.tools.DataManager;
import org.geogurus.gas.utils.ColorGenerator;
import org.geotools.data.Query;

/**
 * Updates the current user mapfile according to given parameters,
 * 
 * @author GNG
 * @version
 */
public class ClassificationPropertiesAction extends Action {

    private Logger logger = Logger.getLogger(ClassificationPropertiesAction.class.getName());

    /* forward name="success" path="" */
    private final String CLASSIF_FORWARD = "classificationProperties";

    /**
     * This is the action called from the Struts framework.
     * 
     * @param mapping
     *            The ActionMapping used to select this instance.
     * @param form
     *            The optional ActionForm bean for this request.
     * @param request
     *            The HTTP Request we are processing.
     * @param response
     *            The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        HttpSession session = request.getSession();
        LayerForm layerForm = (LayerForm) form;
        Layer paramLayer = layerForm.getDefaultMsLayer();
        DataAccess gc = (DataAccess) session.getAttribute(ObjectKeys.CURRENT_GC);
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        LegendGenerator lg = null;

        // removes any classif message
        if (gc.getDefaultMsLayer().getMapClass() != null) {
            gc.getDefaultMsLayer().getMapClass().setMessage(null);
        }
        // first, stores user choices for this classification
        if (paramLayer.getClassItem() != null && paramLayer.getClassItem().length() > 0) {
            gc.getDefaultMsLayer().setClassItem(paramLayer.getClassItem());
        } else {
            gc.getDefaultMsLayer().setClassItem(null);
        }

        if (layerForm.getClassificationType().equals("singleclass")) {
            classifySingleClass(request, session, gc);
        } else if (layerForm.getClassificationType().equals("uniquevalue")) {
            classifyUniqueValue(session, gc, paramLayer.getClassItem());
        } else if (layerForm.getClassificationType().equals("range")) {
            classifyRange(request, session, gc, paramLayer.getClassItem());
        }
        // Updates and Rewrites user map
        umb.getUserLayerList().put(gc.getID(), gc);

        UserMapBeanManager manager = new UserMapBeanManager();
        manager.setUserMapBean(umb);
        manager.writeMapFile();
        // generates MapServer legend for current geometryClass Layer and
        // mapfile
        try {
            String rootPath = getServlet().getServletConfig().getServletContext().getRealPath("") + File.separator;

            lg = new LegendGenerator();
            boolean legendOk = lg.setLayerLegend(umb.getMapserverURL(),
                    umb.getMapfilePath(),
                    rootPath + "msFiles" + File.separator + "tmpMaps" + File.separator,
                    "msFiles/tmpMaps/",
                    rootPath.replace('\\', '/') + "msFiles/templates/legend.html",
                    gc.getMSLayer(),
                    rootPath + "images/empty.gif");
            if (!legendOk) {
                logger.warning(lg.getErrorResponse());
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        // Prepare forward and keys for map refreshing
        session.setAttribute(ObjectKeys.CURRENT_GC, gc);
        request.setAttribute(ObjectKeys.CLASSIF_MESSAGE, gc.getDefaultMsLayer().getMapClass().getMessage());
        request.setAttribute(ObjectKeys.LEGEND_MESSAGE, lg.getErrorResponse());
        request.setAttribute(ObjectKeys.REFRESH_KEY, ObjectKeys.REFRESH_KEY);
        return mapping.findForward(CLASSIF_FORWARD);
    }

    /**
     * perform a classification by looking for all distinct values for the given
     * attribute
     */
    protected void classifyUniqueValue(HttpSession session, DataAccess gc,
            String classitem) {
        // the mapserver class limit
        int classLimit;
        String s = DataManager.getProperty("MAPSERVER_CLASS_LIMIT");
        classLimit = (s == null ? 256 : new Integer(s).intValue());
        ListClassesBean list = gc.getDefaultMsLayer().getMapClass();
        String symName = gc.getDefaultMsLayer().getMapClass().getFirstClass().getSymbol();
        int symSize = gc.getDefaultMsLayer().getMapClass().getFirstClass().getSize();
        list.clear();
        ColorGenerator colorGenerator = (ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR);

        // gets all unique values for the given column, generates classes
        // brute force: construct a new Hashtable indexed by the values =>
        try {
            UniqueValueFeatureClassification op = new UniqueValueFeatureClassification(
                    classitem, classLimit, colorGenerator, list, symName,
                    symSize);
            Set<Integer> hashcodes = new HashSet<Integer>();
            gc.run(op, hashcodes, Query.ALL);
        } catch (Exception e) {
            // either a bad URL or another exception: should track it
            e.printStackTrace();
        }
    }

    /**
     * creates a new class with a generated color
     */
    protected void classifySingleClass(HttpServletRequest request,
            HttpSession session, DataAccess gc) {
        Class cl = new Class();
        // generated color:
        RGB color = ((ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor();
        cl.setColor(color);
        cl.setOutlineColor(new RGB(0, 0, 0));

        // Uses first class of defaultMsLayer of gc to assign to all other
        // classes
        String symName = gc.getDefaultMsLayer().getMapClass().getFirstClass().getSymbol();
        int symSize = gc.getDefaultMsLayer().getMapClass().getFirstClass().getSize();
        cl.setSymbol(symName);
        cl.setSize(symSize);

        // set a name based on the number of classes existing for the
        // currentlayer:
        if (gc.getMSLayer(null, false).getMapClass() == null) {
            cl.setName("class_0");
        } else {
            cl.setName("class_" + gc.getNullMsLayer().getMapClass().getNbClasses());
        }
        // stores this class in session
        ListClassesBean list = gc.getDefaultMsLayer().getMapClass();
        list.clear();
        list.addClass(cl);
    }

    /**
     * performs a classification by computing a range of values based on the
     * number of classes choosen by user. Looks at the GC type and call
     * appropriate method to perform the classif
     */
    protected void classifyRange(HttpServletRequest request,
            HttpSession session, DataAccess gc, String classitem) {
        // the message for JSP
        ListClassesBean listClassesBean = gc.getDefaultMsLayer().getMapClass();
        if (listClassesBean.getFirstClass() == null) {
            return;
        }
        String symName = listClassesBean.getFirstClass().getSymbol();
        int symSize = listClassesBean.getFirstClass().getSize();
        listClassesBean.clear();
        String s = DataManager.getProperty("MAPSERVER_CLASS_LIMIT");
        // the mapserver class limit
        int classLimit = (s == null ? 256 : new Integer(s).intValue());
        int numClasses = new Integer(request.getParameter("numClasses")).intValue();
        // the array of min and max value for the concerned attribute
        double[] minMax = null;

        // very the class limit:
        if (numClasses > classLimit) {
            numClasses = classLimit;
            listClassesBean.setMessage("classlimitation," + classLimit);
        }

        // Look at the sql type for the classitem: only numeric types are
        // allowed for ranges
        String colName;
        GeometryClassFieldBean gcfb;
        for (Iterator<GeometryClassFieldBean> iter = gc.getAttributeData().iterator(); iter.hasNext();) {
            gcfb = iter.next();
            colName = gcfb.getName();
            if (colName.equals(classitem)) {
                if (gcfb.isNumeric()) {
                    // Field found
                    break;
                } else {
                    // invalid field: must handle this.
                    listClassesBean.setMessage("classrange," + colName);
                }
            }
        }

        MinMaxAttributeOperation<Double> op = new MinMaxAttributeOperation<Double>(
                classitem, Double.MIN_VALUE, Double.MAX_VALUE);
        //Query query = new DefaultQuery("Feature", Filter.INCLUDE,
        //        new String[] { classitem });
        Query query = Query.ALL;
        try {
            gc.run(op, (Object) null, query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double range = op.max() - op.min();
        double step = range / numClasses;
        double curmin = op.min();
        Class cl = null;
        StringBuffer exp = null;
        NumberFormat f = NumberFormat.getInstance(Locale.US);
        if (f instanceof DecimalFormat) {
            ((DecimalFormat) f).applyPattern("0.000");
        }

        String className = null;

        // build the classes and the range formulae for each one
        for (int i = 0; i < numClasses; i++) {
            cl = new Class();
            cl.setColor(((ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR)).getNextColor());
            cl.setOutlineColor(new RGB(0, 0, 0));
            // Uses first class of defaultMsLayer of gc to assign to all other
            // classes
            cl.setSymbol(symName);
            cl.setSize(symSize);
            // build the expression (MapServer Logical Expression) and format
            // numeric
            // values to avoid scientific notation: "1.2E3" must be written
            // "1200"
            exp = new StringBuffer("([");
            exp.append(classitem).append("] >= ").append(f.format(curmin));
            if (i < numClasses - 1) {
                // strict inequality to avoid overlap
                exp.append(" AND [").append(classitem).append("] < ");
            } else {
                // non-strict inequality to allow last value to be represented
                exp.append(" AND [").append(classitem).append("] <= ");
            }
            curmin += step;
            exp.append(f.format(curmin)).append(")");
            className = "range_" + i;

            cl.setExpression(exp.toString());
            // name is now the expression itself
            cl.setName(className);
            listClassesBean.addClass(cl);
        }
    }
}
