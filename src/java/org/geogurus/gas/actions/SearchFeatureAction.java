/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.utils.DatasourceBuilder;
import org.geogurus.gas.utils.ObjectKeys;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.mapserver.objects.Map;

/**
 *
 * @author gnguessan
 */
public class SearchFeatureAction extends org.apache.struts.action.Action {

    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String lon = request.getParameter("lon");
        String lat = request.getParameter("lat");
        String tolerance = request.getParameter("tolerance");
        HttpSession session = request.getSession();

        DataAccess gc = (DataAccess) session.getAttribute(ObjectKeys.CURRENT_GC);
        //We also need units of current map
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        byte units = umb.getMapfile().getUnits();
        String toleranceUnit;
        switch (units) {
            case Map.INCHES:
            case Map.FEET:
                toleranceUnit = "feet";
                break;
            case Map.METERS:
                toleranceUnit = "meters";
                break;
            case Map.KILOMETERS:
                toleranceUnit = "kilometers";
                break;
            case Map.MILES:
                toleranceUnit = "statute miles";
                break;
            case Map.DD:
                toleranceUnit = "kilometers";
                /*in this case tolerance is given in decimal degrees so we must
                approximately convert tolerance in kilometers*/
                //tolerance = String.valueOf(Double.parseDouble(tolerance) * 110);
                break;
            default:
                toleranceUnit = "kilometers";
        }

        //Builds featuresource using geotools
        SimpleFeature feature = DatasourceBuilder.getFeature(gc, lon, lat, tolerance, toleranceUnit);

        //Finds feature of current layer
        NumberFormat f = NumberFormat.getInstance(Locale.US);
        if (f instanceof DecimalFormat) {
            ((DecimalFormat) f).applyPattern("0.000");
        }
        StringBuilder json = new StringBuilder("");
        if (feature != null) {
            json.append("{type:\"FeatureCollection\",");
            StringBuilder fields = new StringBuilder("fields:[");
            StringBuilder attributes = new StringBuilder("attributes:[");
            for (int o = 0; o < feature.getAttributeCount(); o++) {
                if (feature.getFeatureType().getType(o) != null &&
                        !feature.getFeatureType().getType(o).getName().getLocalPart().equals(
                        feature.getDefaultGeometryProperty().getType().getName().getLocalPart())) {
                    String name = feature.getFeatureType().getType(o).getName().getLocalPart();
                    String value = String.valueOf(feature.getAttribute(name));
                    try {
                        double v = Double.parseDouble(value);
                        value = String.valueOf(f.format(v));
                    } catch (NumberFormatException nfe) {
                        //Do nothing -> not a number
                    }
                    fields.append("\"" + name + "\"");
                    attributes.append("\"" + value + "\"");
                    if (o < feature.getAttributeCount() - 1) {
                        fields.append(",");
                        attributes.append(",");
                    }
                }
            }
            fields.append("],");
            attributes.append("]");
            json.append(fields.toString());
            json.append(attributes.toString());
            json.append("}");
        }

        response.setContentType("application/x-json");
        Writer out = response.getWriter();
        out.write(json.toString());
        out.flush();
        out.close();

        return null;

    }
}
