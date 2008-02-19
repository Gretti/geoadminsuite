/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import java.io.BufferedReader;
import java.io.StringReader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.string.ConversionUtilities;
import org.geogurus.web.ColorGenerator;

/**
 *
 * @author gnguessan
 */
public class SubmitMapfileAction extends org.apache.struts.action.Action {

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
        //Get the mapfile content from textarea
        HttpSession session = request.getSession();
        String fullMapfile = request.getParameter("fullMapfile");
        //Builds a buffered reader
        BufferedReader br = new BufferedReader(new StringReader(fullMapfile));
        String line = br.readLine();
        // Looking for the first util line
        while ((line.trim().equals("")) || (line.trim().startsWith("#"))) {
            line = br.readLine();
        }
        // Gets array of words of the line
        String[] tokens = ConversionUtilities.tokenize(line.trim());
        // MapFile always starts with MAP keyword otherwise ERROR!
        if (tokens.length > 1) {
            return null;
        }
        if (tokens[0].equalsIgnoreCase("MAP")) {
            //Loads the map from the buffered reader into the usermapbean
            UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
            boolean load = umb.getMapfile().load(br);
            //Writes back user mapfile
            UserMapBeanManager manager = new UserMapBeanManager();
            manager.setUserMapBean(umb);
            manager.generateUserMapfile((ColorGenerator) session.getAttribute(ObjectKeys.COLOR_GENERATOR));
        }

        //XHR access -> no return necessary
        return null;

    }
}
