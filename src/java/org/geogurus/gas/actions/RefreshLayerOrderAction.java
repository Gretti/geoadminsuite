/*
 * RefreshLayerOrderAction.java
 *
 * Created on 11 mars 2007, 21:29
 */

package org.geogurus.gas.actions;

import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.web.ColorGenerator;
/**
 *
 * @author postgres
 * @version
 */

public class RefreshLayerOrderAction extends Action {
    
    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    public ActionForward execute(ActionMapping mapping, ActionForm  form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HttpSession session = request.getSession();
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        Vector order = new Vector(umb.getUserLayerOrder().size());
        StringTokenizer tok = new StringTokenizer(request.getParameter("layerorder"), ",");
        
        while (tok.hasMoreElements()) {
            order.add(tok.nextToken());
        }
        // sets the new list of ids to the bean. Then redirect to JSP:
        umb.setUserLayerOrder(order);
        
        UserMapBeanManager manager = new UserMapBeanManager();
        manager.setUserMapBean(umb);
        manager.generateUserMapfile((ColorGenerator)session.getAttribute(ObjectKeys.COLOR_GENERATOR));
        
        return null;
        
    }
}