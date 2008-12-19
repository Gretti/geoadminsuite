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
 * DisplayHelpAction.java
 *
 * Created on 12 d�cembre 2006, 19:12
 *
 */

package org.geogurus.gas.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author GNG
 */

public class DisplayHelpAction extends Action 
{
    @Override
   public ActionForward execute(ActionMapping mapping, 
                                ActionForm form, 
                                HttpServletRequest request, 
                                HttpServletResponse response)
      throws IOException, ServletException 
   {

        ActionForward forward = null;
        
        String display = request.getParameter("helpme") == null ? "no" : "yes";
        String caller_jsp = request.getParameter("caller");
        
        request.getSession().setAttribute("display_help", display);
   
        forward = new ActionForward("/" + caller_jsp);
        
        return forward;
   }
}