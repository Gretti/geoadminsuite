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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.tools.string.ConversionUtilities;

/**
 *
 * @author gnguessan
 */
public class SubmitMapfileAction extends org.apache.struts.action.Action {

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
        //Get the mapfile content from textarea
        HttpSession session = request.getSession();
        String fullMapfile = request.getParameter("fullMapfile");

        //Builds a buffered reader
        BufferedReader br = new BufferedReader(new StringReader(fullMapfile));
        String line = br.readLine();
        // Looking for the first util line
        while ((line.trim().equals("")) || (line.trim().startsWith("#"))) {
            line = br.readLine();
        }
        // Gets array of words of the line
        String[] tokens = ConversionUtilities.tokenize(line.trim());
        // MapFile always starts with MAP keyword otherwise ERROR!
        if (tokens.length > 1) {
            return null;
        }
        if (tokens[0].equalsIgnoreCase("MAP")) {
            //Loads the map from the buffered reader into the usermapbean
            Map newMap = new Map();
            if (newMap.load(br)) {
                UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
                umb.setMapfile(newMap);
                //Updates UserMapBean re-reading mapfile
                //Extent
                umb.setMapExtent(newMap.getExtent().toString());
                //Units
                umb.setMapUnits(newMap.getUnits());
                //Layers (layer list)
                //FIXME : in case of new layer declared or order modification, should map changes
                for (Iterator it = newMap.getLayers().iterator(); it.hasNext();) {
                    Layer l = (Layer) it.next();
                    DataAccess gc = umb.getUserLayerByName(l.getName());
                    if (gc != null) {
                        gc.setMSLayer(l);
                    }
                }

                //Writes back user mapfile
                UserMapBeanManager manager = new UserMapBeanManager();
                manager.setUserMapBean(umb);
                manager.writeMapFile();
            }
        } else {
            //Errors in mapfile loading (should return error to client)
        }

        //XHR access -> no return necessary
        return null;

    }
}
