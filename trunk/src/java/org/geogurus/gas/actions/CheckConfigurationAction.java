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

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.forms.CheckConfigurationForm;
import org.geogurus.tools.DataManager;

/**
 *
 * @author nicolas
 */
public class CheckConfigurationAction extends org.apache.struts.action.Action {
    
    /* forward name="success" path="" */
    private final static String SUCCESS = "admin";
    
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
        
        //form validation is done in the CheckConfigurationForm ActionForm
        request.setAttribute("checkConfig", "ok");
        //sets current values to the DataManager.
        CheckConfigurationForm cc = (CheckConfigurationForm)form;
        DataManager.setProperty("MAPSERVERURL", cc.getMapserverURL());
        DataManager.setProperty("SHP2PGSQL", cc.getShp2pgsql());
        DataManager.setProperty("PGSQL2SHP", cc.getPgsql2shp());
        DataManager.setProperty("GAS_DB_REPROJ", cc.getGasDbReproj());
        return mapping.findForward(SUCCESS);
    }
}