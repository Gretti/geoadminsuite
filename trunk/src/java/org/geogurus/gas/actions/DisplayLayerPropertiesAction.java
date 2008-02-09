/*
 * DisplayHelpAction.java
 *
 * Created on 12 decembre 2006, 19:12
 *
 */

package org.geogurus.gas.actions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.GeometryClass;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;

/**
 *
 * @author GNG
 */

public class DisplayLayerPropertiesAction extends Action 
{
   public ActionForward execute(ActionMapping mapping, 
                                ActionForm form, 
                                HttpServletRequest request, 
                                HttpServletResponse response)
      throws IOException, ServletException 
   {

        ActionForward forward = null;
        HttpSession session = request.getSession();
        String layerId = request.getParameter("layerID");
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        GeometryClass gc = (GeometryClass)umb.getUserLayerList().get(layerId);
        session.setAttribute(ObjectKeys.CURRENT_GC, gc);
        //cleans session
        session.removeAttribute(ObjectKeys.CLASSIF_MESSAGE);
        session.removeAttribute(ObjectKeys.CLASSIF_TYPE);
        session.removeAttribute(ObjectKeys.TMP_CLASSIFICATION);
        
        forward = mapping.findForward("mapConfigurationLayerProperties");
        return null;
   }
}