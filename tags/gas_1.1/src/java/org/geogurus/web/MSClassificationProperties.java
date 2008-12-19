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
 * MSClassificationProperties.java
 *
 * Created on 9 ao�t 2002, 15:01
 */
package org.geogurus.web;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.geogurus.data.DataAccess;
import org.geogurus.gas.objects.ListClassesBean;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Class;

/**
 * Servlet to deal with the classification window 
 * It listens to MC_mapserver_classification_properties.jsp page to create a list of classes
 * based on an attribute value = classitem
 *
 * @author  nri
 */
public class MSClassificationProperties extends BaseServlet {

    protected final String mc_msclassif_jsp = "MC_mapserver_classification_properties.jsp";
    private String message;

    /**
     * Main method listening to client requests from MC_mapserver_classification_properties.jsp
     * Receives a list of checkbox names based on classes identifiers.
     * Excludes from class list all classes not present in request parameters
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
//debugParameters(request);
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        String layerid = (String) session.getAttribute(ObjectKeys.CURRENT_GC);

        if (umb == null || layerid == null) {
            // session: expiration
            String error = "UserMapBean ou layerid manquant. La session a du expirer.";
            session.setAttribute(ObjectKeys.SERVLET_ERROR, error);
            dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
            return;
        }
        validate(request, session, umb, layerid);
        // put in the request a parameter to refresh the mapfile in the main window
        request.setAttribute(ObjectKeys.REFRESH_KEY, ObjectKeys.REFRESH_KEY);
        dispatch(request, response, mc_msclassif_jsp);
    }

    /**
     * sets all classes after user validation
     */
    protected void validate(HttpServletRequest request, HttpSession session, UserMapBean umb, String layerid) {
        Class cl = new Class();
        DataAccess gc = (DataAccess) umb.getUserLayerList().get(layerid);
        // the list of layer's classes to update according to user-choosen checkboxes:
        ListClassesBean tmpClasses = gc.getNullMsLayer().getMapClass();

        // removes all classes not choosen (checkbox not checked)
        String s = null;
        for (Iterator iter = tmpClasses.getClasses(); iter.hasNext();) {
            cl = (Class) iter.next();
            int id = cl.getID();
            s = "c" + id + "_check";
            if (request.getParameter(s) == null) {
                // current class was excluded from the list of classes. Removes it from this arrayList
                iter.remove();
                continue;
            }
        }
    }
}
