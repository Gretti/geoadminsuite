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

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.utils.ObjectKeys;
import org.opengis.feature.simple.SimpleFeature;

import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.operations.SpatialQueryOperation;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.Projection;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;

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
            //Check projection of gc and mapfile => reproject given point and
            //recalculate tolerance if necessary
            Projection p = umb.getMapfile().getProjection();
            String param = (String) p.getAttributes().get(0);
            String mapEpsg = param.substring(param.lastIndexOf(":") + 1, param.lastIndexOf("\""));
            String gcEpsg = String.valueOf(gc.getSrid());
            if (!gcEpsg.equals(mapEpsg)) {
                //Reproject given point
                CoordinateReferenceSystem crsSrc = null;
                CoordinateReferenceSystem crsDest = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null).createCoordinateReferenceSystem(gcEpsg);
                if (!mapEpsg.equals("900913")) {
                    crsSrc = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null).createCoordinateReferenceSystem(mapEpsg);
                } else {
                    try {
                        crsSrc = CRS.parseWKT("PROJCS[\"Google Mercator\",GEOGCS[\"WGS 84\",DATUM[\"World Geodetic System 1984\",SPHEROID[\"WGS 84\",6378137.0,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0.0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.017453292519943295],AXIS[\"Geodetic latitude\",NORTH],AXIS[\"Geodetic longitude\",EAST],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"semi_minor\",6378137.0],PARAMETER[\"latitude_of_origin\",0.0],PARAMETER[\"central_meridian\",0.0],PARAMETER[\"scale_factor\",1.0],PARAMETER[\"false_easting\",0.0],PARAMETER[\"false_northing\",0.0],UNIT[\"m\",1.0],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"900913\"]],EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs\"]]");
                    } catch (FactoryException ex) {
                        Logger.getLogger(SearchFeatureAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                CoordinateOperationFactory coFactory = ReferencingFactoryFinder.getCoordinateOperationFactory(null);
                CoordinateOperation op = coFactory.createOperation(crsSrc, crsDest);
                MathTransform trans = op.getMathTransform();
                DirectPosition ll = new GeneralDirectPosition(Double.valueOf(lon).doubleValue(), Double.valueOf(lat).doubleValue());
                ll = trans.transform(ll, null);
                lon = String.valueOf(ll.getCoordinate()[0]);
                lat = String.valueOf(ll.getCoordinate()[1]);
            }
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
            SpatialQueryOperation op = new SpatialQueryOperation();
            Filter filter = CQL.toFilter("DWITHIN (the_geom, POINT(" + lon + " " + lat + ")," + tolerance + "," + toleranceUnit + ")");
            Query query = new DefaultQuery(gc.featureType().get().getTypeName(), filter);
            Vector<SimpleFeature> features = new Vector<SimpleFeature>();
            try {
                gc.run(op, features, query);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Gets only the first element from query
            SimpleFeature feature = features.get(0);
            //Finds feature of current layer
            if (feature != null) {
                NumberFormat f = NumberFormat.getInstance(Locale.US);
                if (f instanceof DecimalFormat) {
                    ((DecimalFormat) f).applyPattern("0.000");
                }
                json.append("{type:\"FeatureCollection\",");
                StringBuilder fields = new StringBuilder("fields:[");
                StringBuilder attributes = new StringBuilder("attributes:[");
                for (int o = 0; o < feature.getAttributeCount(); o++) {
                    if (feature.getFeatureType().getType(o) != null &&
                            !feature.getFeatureType().getType(o).getName().getLocalPart().equals(
                            feature.getDefaultGeometryProperty().getType().getName().getLocalPart())) {
                        String name = feature.getFeatureType().getType(o).getName().getLocalPart();
                        Object value = feature.getAttribute(name);
                        if (value instanceof Number && !(value instanceof Integer)) {
                            try {
                                double v = Double.parseDouble(String.valueOf(value));
                                value = String.valueOf(f.format(v));
                            } catch (NumberFormatException nfe) {
                                //Do nothing -> not a number
                            }

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
