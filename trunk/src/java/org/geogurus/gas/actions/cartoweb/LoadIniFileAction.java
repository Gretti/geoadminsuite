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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.geogurus.gas.forms.cartoweb.IniConfigurationForm;
import org.geogurus.gas.forms.cartoweb.IniFileForm;
import org.geogurus.gas.utils.ObjectKeys;

/**
 * Load a CW configuration ini file.
 * @author nicolas
 */
public class LoadIniFileAction extends org.apache.struts.action.Action {
    
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
    public ActionForward execute(ActionMapping mapping, ActionForm  form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        IniConfigurationForm cwIniConf = request.getSession().getAttribute(ObjectKeys.CW_INI_CONF_BEAN) == null ? 
            new IniConfigurationForm() :
            (IniConfigurationForm)request.getSession().getAttribute(ObjectKeys.CW_INI_CONF_BEAN);
        
        String iniFileType = ((IniFileForm)form).getFileType();
        FormFile iniFile = ((IniFileForm)form).getIniFile();
        String message = this.getResources(request).getMessage("cartoweb.upload.ok");
        //loads properties from the given file, according to the INI file type
        if ("LOCATION_INI".equals(iniFileType)) {
            if (!cwIniConf.getLocationConf().loadFromFile(iniFile.getInputStream())) {
                message = "Cannot load given ini file: " + iniFile.getFileName() + " . see server logs for details";
            }
            
        } else if ("QUERY_INI".equals(iniFileType)) {
            if (!cwIniConf.getQueryConf().loadFromFile(iniFile.getInputStream())) {
                message = "Cannot load given ini file: " + iniFile.getFileName() + " . see server logs for details";
            }
        } else if ("IMAGES_INI".equals(iniFileType)) {
            if (!cwIniConf.getImagesConf().loadFromFile(iniFile.getInputStream())) {
                message = "Cannot load given ini file: " + iniFile.getFileName() + " . see server logs for details";
            }
        } else if ("LAYERS_INI".equals(iniFileType)) {
            if (!cwIniConf.getLayerConf().loadFromFile(iniFile.getInputStream())) {
                message = "Cannot load given ini file: " + iniFile.getFileName() + " . see server logs for details";
            }
        }
        iniFile.getInputStream().close();
        request.getSession().setAttribute(ObjectKeys.CW_INI_CONF_BEAN, cwIniConf);
        // sets the message at
        response.setContentType("text/html");
        String json = "{\"success\":";
        //json += error.length() > 0 ? "false" : "true";
        json += "true";
        json += ", \"failure\":";
        json += "false";
        json += ", \"message\":\"";
        json += message;
        json += "\"}";
        //response.setContentType("application/x-json");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
        return null;
        
    }
}