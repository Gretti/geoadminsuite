/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessType;
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
        StringBuilder json = new StringBuilder("");

        if (gc.getDatasourceType() != DataAccessType.ECW &&
                gc.getDatasourceType() != DataAccessType.IMG &&
                gc.getDatasourceType() != DataAccessType.TIFF &&
                gc.getDatasourceType() != DataAccessType.WMS) {
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
            //TODO: check projection of gc => reproject given point and
            //recalculate tolerance if necessary
            SimpleFeature feature = DatasourceBuilder.getFeature(gc, lon, lat, tolerance, toleranceUnit);

            //Finds feature of current layer
            NumberFormat f = NumberFormat.getInstance(Locale.US);
            if (f instanceof DecimalFormat) {
                ((DecimalFormat) f).applyPattern("0.000");
            }
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
        }
        response.setContentType("application/x-json");
        Writer out = response.getWriter();
        out.write(json.toString());
        out.flush();
        out.close();

        return null;

    }
}
