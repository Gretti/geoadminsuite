/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.forms.CheckConfigurationForm;
import org.geogurus.tools.DataManager;

/**
 *
 * @author nicolas
 */
public class CheckConfigurationAction extends org.apache.struts.action.Action {
    
    /* forward name="success" path="" */
    private final static String SUCCESS = "admin";
    
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
        
        //form validation is done in the CheckConfigurationForm ActionForm
        request.setAttribute("checkConfig", "ok");
        //sets current values to the DataManager.
        CheckConfigurationForm cc = (CheckConfigurationForm)form;
        DataManager.setProperty("MAPSERVERURL", cc.getMapserverURL());
        DataManager.setProperty("SHP2PGSQL", cc.getShp2pgsql());
        DataManager.setProperty("PGSQL2SHP", cc.getPgsql2shp());
        DataManager.setProperty("GAS_DB_REPROJ", cc.getGasDbReproj());
        return mapping.findForward(SUCCESS);
    }
}