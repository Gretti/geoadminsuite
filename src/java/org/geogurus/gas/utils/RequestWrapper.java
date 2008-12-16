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
 * RequestWrapper.java
 *
 * Created on 17 octobre 2007, 12:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geogurus.gas.utils;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.geogurus.gas.objects.HostDescriptorBean;

/**
 *
 * @author gnguessan
 */
public class RequestWrapper {
    
    /**
     * Wraps request parameters to Host List
     **/
    public static Hashtable resquestToHostList(HttpServletRequest request) {
        Hashtable hostDesc = new Hashtable();
        HostDescriptorBean host = null;
        String prefHost = "";
        for (int i=0; i< request.getParameterMap().size(); i++) {
            prefHost = "host[" + i + "].";
            
            if(! String.valueOf(request.getParameter(prefHost + "name")).equalsIgnoreCase("null")) {
                if(!hostDesc.containsKey(request.getParameter(prefHost + "name"))) {
                    host = new HostDescriptorBean();
                    host.setName(request.getParameter(prefHost + "name"));
                    host.setPath(request.getParameter(prefHost + "path"));
                    host.setPort(request.getParameter(prefHost + "port"));
                    host.setType(request.getParameter(prefHost + "type"));
                    host.setUname(request.getParameter(prefHost + "uname"));
                    host.setUpwd(request.getParameter(prefHost + "upwd"));
                    hostDesc.put(request.getParameter(prefHost + "name"),host);
                }
            } else {
                break;
            }
        }
        return hostDesc;
    }
}
