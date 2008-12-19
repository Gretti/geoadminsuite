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

package org.geogurus.gas.actions;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.Datasource;
import org.geogurus.gas.forms.cartoweb.IniConfigurationForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.SymbologyListBean;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.gas.utils.ColorGenerator;

/**
 * @author Gretti
 */

public class ComposeMapAction extends Action {
    
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        HttpSession session = request.getSession(false);
        
        StringTokenizer tok = new StringTokenizer(request.getParameter(ObjectKeys.SELECTED_IDS),"|");
        
        String[] layerChoice = new String[tok.countTokens()];
        int i = 0;
        while (tok.hasMoreTokens()) {
            layerChoice[i] = tok.nextToken();
            i++;
        }
        
        // the colorGenerator object, stored in session
        ColorGenerator colgen = (ColorGenerator)session.getAttribute(ObjectKeys.COLOR_GENERATOR);
        if (colgen == null) {
            colgen = new ColorGenerator();
            session.setAttribute(ObjectKeys.COLOR_GENERATOR, colgen);
        }
        
        Hashtable<String, Vector<Datasource>> hostList = (Hashtable<String, Vector<Datasource>>)session.getAttribute(ObjectKeys.HOST_LIST);
        SymbologyListBean gasSymbolList = (SymbologyListBean)getServlet().getServletConfig().getServletContext().getAttribute(ObjectKeys.GAS_SYMBOL_LIST);
        String rootPath = getServlet().getServletConfig().getServletContext().getRealPath("") + File.separator;
        UserMapBean userMapBean = new UserMapBean();
        
        try {
            userMapBean.setRootpath(rootPath);
            userMapBean.setUserLayerChoice(layerChoice);
            String mapfilePath = rootPath + "msFiles" + File.separator + "tmpMaps" + File.separator + "user_" + session.getId() + ".map";
            userMapBean.setMapfilePath(mapfilePath);
            
            UserMapBeanManager umbMgr = new UserMapBeanManager();
            umbMgr.setUserMapBean(userMapBean);
            umbMgr.createUserLayerList(gasSymbolList,hostList);
            umbMgr.generateTemplateFiles();
            umbMgr.buildFirstUserMapfile(1280,1024,colgen);
            // reset the cartoweb user list to force gas to regenerate a list from current layer list
            IniConfigurationForm cwIniConf = (IniConfigurationForm)request.getSession().getAttribute(ObjectKeys.CW_INI_CONF_BEAN);
            if (cwIniConf != null) {
                cwIniConf.getLayerConf().setRootLayer(null);
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        session.setAttribute(ObjectKeys.USER_MAP_BEAN,userMapBean);
        
        //cleans session
        session.removeAttribute(ObjectKeys.CURRENT_GC);
        session.removeAttribute(ObjectKeys.CLASSIF_MESSAGE);
        
        return null;
    }
}