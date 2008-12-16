/*
 * Copyright (C) 2007-2008  Camptocamp
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

package org.geogurus.gas.actions.cartoweb;

import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.cartoweb.CartowebLayer;
import org.geogurus.cartoweb.CartowebUtil;
import org.geogurus.gas.forms.cartoweb.IniConfigurationForm;
import org.geogurus.gas.utils.ObjectKeys;

/**
 *
 * @author nicolas
 */
public class SaveLayerTreeAction extends org.apache.struts.action.Action {
    private Logger logger = Logger.getLogger(SaveLayerTreeAction.class.getName());
    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    public ActionForward execute(ActionMapping mapping, ActionForm  form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        String message = "";
        String resJson = "[]";
        IniConfigurationForm cwIniConf = request.getSession().getAttribute(ObjectKeys.CW_INI_CONF_BEAN) == null ? 
            new IniConfigurationForm() :
            (IniConfigurationForm)request.getSession().getAttribute(ObjectKeys.CW_INI_CONF_BEAN);

        String layerJson = request.getParameter(ObjectKeys.CW_LAYER_TREE_JSON);
        CartowebUtil cwUtil = new CartowebUtil();
        logger.info("CW Layer Tree JSON representation from client: " + layerJson);
        CartowebLayer layer = cwUtil.buildLayerFromJson(layerJson);
        if (layer == null) {
            message = cwUtil.getMessage();
            
        } else {
            cwIniConf.getLayerConf().setRootLayer(layer);
            resJson = cwIniConf.getLayerConf().getRootLayer().getExtTreeJson();
        }
        request.getSession().setAttribute(ObjectKeys.CW_INI_CONF_BEAN, cwIniConf);

        // returns a json result to keep user posted about result
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(resJson);
        out.flush();
        out.close();
        return null;
    }
}