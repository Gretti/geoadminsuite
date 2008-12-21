/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.actions;

import java.net.URL;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.tools.DataManager;

/**
 * The default initialisation action for the gas, gets the current URL to generate
 * MapServer URL
 * @author nicolas
 */
public class InitAction extends org.apache.struts.action.Action {
    
    /* forward name="success" path="" */
    private final static String INDEX = "index";
    
    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String file = "/cgi-bin/" + (System.getProperty("os.name").toLowerCase().contains("windows") ? "mapserv.exe" : "mapserv");
        URL reconstructedURL = new URL(request.getScheme(),
            request.getServerName(),
            // use default port
            file);

        // deals with mapserver URL
        if (DataManager.getProperty(DataManager.MAPSERVERURL) == null) {
            DataManager.setProperty(DataManager.MAPSERVERURL, reconstructedURL.toString());
        }

        // try to get MS version from MS error page
        Hashtable<String, String> msInfo = DataManager.getMSVersion();
        if (msInfo != null) {
            DataManager.setProperty(DataManager.MAPSERVERVERSION, msInfo.get("MSVERSION"));
            DataManager.setProperty(DataManager.MAPSERVERINPUTS, msInfo.get("INPUTS"));
            DataManager.setProperty(DataManager.MAPSERVEROUTPUTS, msInfo.get("OUTPUTS"));
            DataManager.setProperty(DataManager.MAPSERVERSUPPORTS, msInfo.get("SUPPORTS"));
        }

        //launch datasources list page
        return mapping.findForward(INDEX);
    }
}
