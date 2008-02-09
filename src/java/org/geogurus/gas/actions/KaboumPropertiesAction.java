/*
 * KaboumPropertiesAction.java
 *
 * Created on April 2, 2007, 8:00 PM
 */

package org.geogurus.gas.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.gas.forms.KaboumPropertiesForm;
import org.geogurus.gas.objects.KaboumBean;
import org.geogurus.gas.utils.ObjectKeys;

/**
 *
 * @author nicolas
 * @version
 */

public class KaboumPropertiesAction extends Action {
    
    /* forward name="success" path="" */
    private final static String SUCCESS = "kaboumProperties";
    
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
        KaboumBean kaboumBean = (KaboumBean)session.getAttribute(ObjectKeys.KABOUM_BEAN);
        
        KaboumPropertiesForm kaboumForm = (KaboumPropertiesForm)form;
        
        if (kaboumForm.getKaboumBean() == null) {
            kaboumForm.setKaboumBean(kaboumBean);
            
        } else {
            request.setAttribute(ObjectKeys.REFRESH_KEY,ObjectKeys.REFRESH_KEY);
            session.setAttribute(ObjectKeys.KABOUM_BEAN, kaboumForm.getKaboumBean());
        }
        return mapping.findForward(SUCCESS);
    }
}
