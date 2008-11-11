/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.forms.LegendForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Legend;
import org.geogurus.mapserver.objects.RGB;

/**
 *
 * @author gnguessan
 */
public class LegendPropertiesAction extends org.apache.struts.action.Action {

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
        LegendForm legendForm = (LegendForm) form;
        Legend legend = legendForm.getLegend();
        legend.setImageColor(new RGB(legendForm.getImgColor()));

        legend.setOutlineColor(new RGB(legendForm.getOutlnColor()));

        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        umb.getMapfile().setLegend(legend);

        UserMapBeanManager manager = new UserMapBeanManager();
        manager.setUserMapBean(umb);
        manager.writeMapFile();

        return null;

    }
}
