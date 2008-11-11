/*
 * LayerPropertiesAction.java
 *
 * Created on 5 fevrier 2007, 22:58
 */
package org.geogurus.gas.actions;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.forms.LayerForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Layer;

/**
 *
 * @author Administrateur
 * @version
 */
public class LayerPropertiesAction extends Action {

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
        LayerForm layerForm = (LayerForm) form;
        Layer paramLayer = layerForm.getDefaultMsLayer();
        DataAccess gc = (DataAccess) session.getAttribute(ObjectKeys.CURRENT_GC);
        Layer l = gc.getDefaultMsLayer();

        l.setMinScale(paramLayer.getMinScale());
        l.setMaxScale(paramLayer.getMaxScale());
        if (paramLayer.getFilter() != null && !paramLayer.getFilter().equals("")) {
            l.setFilter(paramLayer.getFilter());
        } else {
            l.setFilter(null);
        }
        if (paramLayer.getFilterItem() != null && !paramLayer.getFilterItem().equals("")) {
            l.setFilterItem(paramLayer.getFilterItem());
        }
        l.setTransparency(paramLayer.getTransparency());
        
        //Deals with Raster
        //If a tileitem has been choosen, this mean that data must become tileindex
        if(paramLayer.getTileItem() != null && !paramLayer.getTileItem().equals("")) {
            l.setTileItem(paramLayer.getTileItem());
            l.setTileIndex(new File(l.getData()));
            l.setType(Layer.RASTER);
            l.setData(null);
        //If tileItem is not null for current GC and tileItem is null for paramLayer, this means
        //that layer must no more be displayed as a raster
        } else if(l.getTileItem() != null && paramLayer.getTileItem().equals("")) {
            l.setType(Layer.POLYGON);
            l.setData(l.getTileIndex().getAbsolutePath());
            l.setTileItem(null);
            l.setTileIndex(null);
        }
        
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        UserMapBeanManager manager = new UserMapBeanManager();
        umb.getUserLayerList().put(gc.getID(), gc);
        manager.setUserMapBean(umb);
        manager.writeMapFile();

        return null;

    }
}
