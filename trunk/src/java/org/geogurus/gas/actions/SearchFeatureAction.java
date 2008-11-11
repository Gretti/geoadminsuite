/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.objects.LayerGeneralProperties;
import org.geogurus.gas.utils.DatasourceBuilder;
import org.geogurus.gas.utils.GeoJSONBuilder;
import org.geogurus.gas.utils.ObjectKeys;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

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
        HttpSession session = request.getSession();

        LayerGeneralProperties lgp = (LayerGeneralProperties) session.getAttribute(ObjectKeys.LAYER_GENERAL_PROPERTIES);

        //Builds featuresource using geotools
        SimpleFeature feature = DatasourceBuilder.getFeature(lgp.getDataAccess(), lon, lat);
        
        //Checks for big features and replaces by centroïd in case
        if (((Geometry) feature.getDefaultGeometry()).getNumPoints() > 500) {
            feature.setDefaultGeometry(((Geometry) feature.getDefaultGeometry()).getCentroid());
        }

        //Finds feature of current layer

        response.setHeader("Content-Type", "text/plain; charset=utf-8");
        GeoJSONBuilder geojson = new GeoJSONBuilder(response.getWriter());
        geojson.object();
        geojson.key("type").value("Feature");
        //GeometryAttributeType geomAttr = feature.getFeatureType().getDefaultGeometry();
        List<Object> attributes = feature.getAttributes();
		for (int o = 0; o < attributes.size(); o++) {
            //if (!feature.getFeatureType().getAttributeType(o).getLocalName().equalsIgnoreCase(geomAttr.getLocalName())) {
            geojson.key(feature.getFeatureType().getType(o).getName().getLocalPart()).value(feature.getAttribute(o).toString());
        //}
        }
        geojson.key("geometry");
        geojson.writeGeom((Geometry) feature.getDefaultGeometry());
        geojson.endObject();

        response.flushBuffer();

        return null;

    }
}
