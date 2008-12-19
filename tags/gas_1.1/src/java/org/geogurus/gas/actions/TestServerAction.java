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
 * TestServerAction.java
 *
 * Created on 10 janvier 2007, 23:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geogurus.gas.actions;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.managers.DatasourceManager;
import org.geogurus.gas.objects.HostDescriptorBean;

/**
 *
 * @author Administrateur
 */
public class TestServerAction extends Action {
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        String type = request.getParameter("type");
        String name = request.getParameter("name");
        String path = request.getParameter("path");
        String port = request.getParameter("port");
        String uname = request.getParameter("uname");
        String upwd = request.getParameter("upwd");
        String instance = request.getParameter("instance");
        // ext can return on when submitting a checkbox and GAS code can submit true...
        //Note: should unify the mechanism
        //boolean recurse = ("on".equalsIgnoreCase(request.getParameter("recurse")) || 
        //        "true".equalsIgnoreCase(request.getParameter("recurse")));
        String recurse = request.getParameter("recurse");
        
        String serverState = "";
        int nbGeoDs = 0;
        
        // now uses a Datasource Manager to list available datasources for an host
        // Note: should see how to avoid building 2 times datasources: if this action is called,
        // it could store a datasource under a session key for later use.
        // when clicking next on the configserver tab, one could reuse stored datasources
        //note: should use a Hostbean instead of parameters.
       
        //String hostString = "" + name + "," + path + "," + port + "," + uname + "," + upwd + "," + instance + "," + recurse + "," + type;
        HostDescriptorBean host = new HostDescriptorBean();
        host.setInstance(instance);
        host.setName(name);
        host.setPath(path);
        host.setPort(port);
        host.setRecurse(recurse);
        host.setType(type);
        host.setUname(uname);
        host.setUpwd(upwd);
        
        nbGeoDs = DatasourceManager.getInstance().getDatasourcesCountM(host);
        serverState = nbGeoDs >= 0 ? "ok" : "ko";
        String resp = "{'state':'" + serverState + "','nbds':" + Integer.toString(nbGeoDs) + "}";
        
        response.setContentType("application/x-json");
        Writer out = response.getWriter();
        out.write(resp);
        out.flush();
        out.close();

        return null;
    }
}