/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.forms.LabelForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.Label;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;

/**
 *
 * @author gnguessan
 */
public class LabelPropertiesAction extends org.apache.struts.action.Action {

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

        DataAccess gc = (DataAccess) request.getSession().getAttribute(ObjectKeys.CURRENT_GC);
        
        Layer l = gc.getMSLayer(null, false);
        LabelForm labelForm = (LabelForm) form;
        
        //Treats layer parameters concerning labels
        if (labelForm.getLabelItem() != null && labelForm.getLabelItem().length() > 0) {
            // user choosed to labelise the class with a simple label
            l.setLabelItem(labelForm.getLabelItem());
        } else {
            // removes the labelitem if no field is choosen
            l.setLabelItem(null);
        }
        
        if (labelForm.getLabelAngleItem() != null && labelForm.getLabelAngleItem().length() > 0) {
            l.setLabelAngleItem(labelForm.getLabelAngleItem());
        } else {
            l.setLabelAngleItem(null);
        }

        if (labelForm.getLabelSizeItem() != null && labelForm.getLabelSizeItem().length() > 0) {
            l.setLabelSizeItem(labelForm.getLabelSizeItem());
        } else {
            l.setLabelSizeItem(null);
        }
        
        //Treats label itself
        Label lab = labelForm.getLabel();
        
        //Size is relative to type
        int typ = lab.getType();
        if (typ == Label.TRUETYPE) {
            lab.setSize(labelForm.getTruetypeSize());
        } else {
            lab.setSize(labelForm.getBitmapSize());
            lab.setFont(null);
        }
        
        //Deals with Colors
        if(labelForm.getLabelColor() != null && labelForm.getLabelColor().length() > 0) {
            lab.setColor(new RGB(labelForm.getLabelColor()));
        }
        if(labelForm.getLabelOutlineColor() != null && labelForm.getLabelOutlineColor().length() > 0) {
            lab.setOutlineColor(new RGB(labelForm.getLabelOutlineColor()));
        }
        if(labelForm.getLabelOutlineWidth()!=null) {
            lab.setOutlineWidth(labelForm.getLabelOutlineWidth());
        }
        if(labelForm.getLabelBackgroundColor() != null && labelForm.getLabelBackgroundColor().length() > 0) {
            lab.setBackgroundColor(new RGB(labelForm.getLabelBackgroundColor()));
        }
        lab.setWrap(labelForm.getLabelWrap());
        if(labelForm.getLabelMaxLength()!=null)
            lab.setMaxLength(labelForm.getLabelMaxLength());
        if(labelForm.getLabelAlign()!=null) 
            lab.setAlign(labelForm.getLabelAlign());
        
        
        
        //Applies label on each classes of the layer
        for (Iterator iter = l.getMapClass().getClasses();iter.hasNext();) {
            Class mc = (Class)iter.next();
            mc.setLabel(lab);
        }

        UserMapBean umb = (UserMapBean) request.getSession().getAttribute(ObjectKeys.USER_MAP_BEAN);
        umb.getUserLayerList().put(gc.getID(), gc);

        UserMapBeanManager manager = new UserMapBeanManager();
        manager.setUserMapBean(umb);
        manager.writeMapFile();
        return null;
    }
}
