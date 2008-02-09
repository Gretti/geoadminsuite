/*
 * DisplayHelpAction.java
 */

package org.geogurus.gas.actions;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.web.ColorGenerator;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.gas.objects.UserMapBean;

/**
 * @author Gretti
 */

public class ComposeMapAction extends Action {
    private Log log = LogFactory.getLog(ComposeMapAction.class);
    
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = null;
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
        
        Hashtable hostList = (Hashtable)session.getAttribute(ObjectKeys.HOST_LIST);
        Hashtable gasSymbolList = (Hashtable)getServlet().getServletConfig().getServletContext().getAttribute(ObjectKeys.GAS_SYMBOL_LIST);
        String rp = getServlet().getServletConfig().getServletContext().getRealPath("") + File.separator;
        UserMapBean userMapBean = new UserMapBean();
        
        try {
            userMapBean.setRootpath(rp);
            userMapBean.setUserLayerChoice(layerChoice);
            String mapfilePath = rp + "msFiles" + File.separator + "tmpMaps" + File.separator + "user_" + session.getId() + ".map";
            userMapBean.setMapfilePath(mapfilePath);
            
            UserMapBeanManager umbMgr = new UserMapBeanManager();
            umbMgr.setUserMapBean(userMapBean);
            umbMgr.createUserLayerList(gasSymbolList,hostList);
            umbMgr.generateTemplateFiles();
            umbMgr.buildFirstUserMapfile(Integer.valueOf(request.getParameter("screenWidth")).intValue(),Integer.valueOf(request.getParameter("screenHeight")).intValue(),colgen);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        session.setAttribute(ObjectKeys.USER_MAP_BEAN,userMapBean);
        
        //cleans session
        session.removeAttribute(ObjectKeys.CURRENT_GC);
        session.removeAttribute(ObjectKeys.CLASSIF_MESSAGE);
        session.removeAttribute(ObjectKeys.CLASSIF_TYPE);
        session.removeAttribute(ObjectKeys.TMP_CLASSIFICATION);
        
        return null;
    }
}