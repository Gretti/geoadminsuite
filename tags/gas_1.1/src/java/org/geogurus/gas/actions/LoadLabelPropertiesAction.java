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
 * LoadLabelPropertiesAction.java
 *
 * Created on 27 fevrier 2007
 *
 */
package org.geogurus.gas.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Label;
/**
 *
 * @author GNG
 */

public class LoadLabelPropertiesAction extends Action {
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = null;
        HttpSession session = request.getSession();
        
        DataAccess gc = (DataAccess)session.getAttribute(ObjectKeys.CURRENT_GC);
        
        if(gc.getDefaultMsLayer().getMapClass().getFirstClass().getLabel() == null) {
            gc.getDefaultMsLayer().getMapClass().getFirstClass().setLabel(new Label());
        }
        
        if(session.getAttribute("fontList") == null) {
            // find the list of available fonts from font.list GAS file
            try {
                ArrayList fontList = new ArrayList();
                String fontPath = getServlet().getServletContext().getRealPath("");
                
                // check to see if path ends with a /
                if (!fontPath.endsWith(File.separator)) {
                    // tmp modiction, but value saved in other variables.
                    fontPath += File.separator;
                }
                fontPath += "msFiles";
                fontPath += File.separator;
                fontPath += "fonts";
                fontPath += File.separator;
                String fontFile = fontPath + "font.list";
                BufferedReader in = new BufferedReader(new FileReader(fontFile));
                String s = "";
                StringTokenizer tk = null;
                while ((s = in.readLine()) != null) {
                    tk = new StringTokenizer(s);
                    String currentFontName = tk.nextToken();
                    String currentFontFile = tk.nextToken();
                    if(new File(fontPath + currentFontFile).exists()) {
                        fontList.add(currentFontName);
                    }
                }
                in.close();
                session.setAttribute("fontList", fontList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        forward = mapping.findForward("labelProperties");
        
        return forward;
    }
}