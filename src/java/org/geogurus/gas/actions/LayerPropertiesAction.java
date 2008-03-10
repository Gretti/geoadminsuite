/*
 * LayerPropertiesAction.java
 *
 * Created on 5 fevrier 2007, 22:58
 */

package org.geogurus.gas.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.GeometryClass;
import org.geogurus.gas.forms.LayerForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.web.ColorGenerator;
/**
 *
 * @author Administrateur
 * @version
 */

public class LayerPropertiesAction extends Action {
    
    /* forward name="success" path="" */
    private final static String SUCCESS = "layerProperties";
    
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
    public ActionForward execute(ActionMapping mapping, ActionForm  form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        HttpSession session = request.getSession();
        LayerForm layerForm = (LayerForm)form;
        Layer paramLayer = layerForm.getDefaultMsLayer();
        GeometryClass gc = (GeometryClass)session.getAttribute(ObjectKeys.CURRENT_GC);
        Layer l = gc.getDefaultMsLayer();
        
        l.setMinScale(paramLayer.getMinScale());
        l.setMaxScale(paramLayer.getMaxScale());
        if (paramLayer.getFilter() != null && !paramLayer.getFilter().equals("")) {
            l.setFilter(paramLayer.getFilter());
        } else {
            l.setFilter(null);
        }
        if (paramLayer.getFilterItem() != null && !paramLayer.getFilterItem().equals("")) l.setFilterItem(paramLayer.getFilterItem());
        l.setTransparency(paramLayer.getTransparency());
        
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        UserMapBeanManager manager = new UserMapBeanManager();
        umb.getUserLayerList().put(gc.getID(),gc);
        manager.setUserMapBean(umb);
        manager.generateUserMapfile((ColorGenerator)session.getAttribute(ObjectKeys.COLOR_GENERATOR));
        
        request.setAttribute(ObjectKeys.REFRESH_KEY,ObjectKeys.REFRESH_KEY);
        return mapping.findForward(SUCCESS);
        
    }
}
