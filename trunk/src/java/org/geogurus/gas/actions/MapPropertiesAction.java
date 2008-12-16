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
 * LayerPropertiesAction.java
 *
 * Created on 5 fevrier 2007, 22:58
 */
package org.geogurus.gas.actions;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.forms.MapForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.MSExtent;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.SymbolSet;

/**
 *
 * @author Administrateur
 * @version
 */
public class MapPropertiesAction extends Action {

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

        HttpSession session = request.getSession();
        MapForm mapForm = (MapForm) form;
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        //Boolean sizeChanged = new Boolean(false);

        if (mapForm.getExtent().length() > 0 && mapForm.getExtent().split(" ").length == 4) {
            StringTokenizer tok = new StringTokenizer(mapForm.getExtent());
            umb.getMapfile().setExtent(new MSExtent(new Double(tok.nextToken()).doubleValue(),
                    new Double(tok.nextToken()).doubleValue(),
                    new Double(tok.nextToken()).doubleValue(),
                    new Double(tok.nextToken()).doubleValue()));
        }

        umb.getMapfile().setImageType(mapForm.getImageType().byteValue());
        umb.getMapfile().setImageQuality(mapForm.getImageQuality().byteValue());
        umb.getMapfile().setImageColor(new RGB(mapForm.getImageColor()));
        umb.getMapfile().setInterlace(mapForm.getInterlace().byteValue());
        if (mapForm.getName().length() > 0) {
            umb.getMapfile().setName(mapForm.getName());
        }
        if (umb.getMapfile().getSize().getWidth() != mapForm.getWidth().doubleValue() ||
                umb.getMapfile().getSize().getHeight() != mapForm.getHeight().doubleValue()) {
            umb.getMapfile().setSize(new Dimension(mapForm.getWidth().intValue(), mapForm.getHeight().intValue()));
        //sizeChanged = new Boolean(true);
        }
        umb.getMapfile().setResolution(mapForm.getResolution().intValue());
        umb.getMapfile().setScale(mapForm.getScale().doubleValue());
        umb.getMapfile().setStatus(mapForm.getStatus().byteValue());

        if (new File(mapForm.getFontSet()).exists()) {
            umb.getMapfile().setFontSet(new File(mapForm.getFontSet()));
        }
        if (new File(mapForm.getShapePath()).exists()) {
            umb.getMapfile().setShapePath(new File(mapForm.getShapePath()));
        }

        umb.getMapfile().setTransparent(mapForm.getTransparent().byteValue());
        umb.getMapfile().setUnits(mapForm.getUnits().byteValue());

        SymbolSet symbolSet = new SymbolSet();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(mapForm.getCanonicalPath())));
            if (symbolSet.load(br)) {
                umb.getMapfile().setSymbolSet(symbolSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        UserMapBeanManager manager = new UserMapBeanManager();
        manager.setUserMapBean(umb);
        manager.writeMapFile();

        //request.setAttribute(ObjectKeys.REFRESH_KEY,ObjectKeys.REFRESH_KEY);
        //request.setAttribute(ObjectKeys.MAP_SIZE_CHANGED,sizeChanged);
        return null;

    }
}
