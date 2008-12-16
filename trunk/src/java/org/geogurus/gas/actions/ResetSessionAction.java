/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.actions;

import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

/**
 *
 * @author nicolas Ribot
 */
public class ResetSessionAction extends org.apache.struts.action.Action {
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
        
        // can this mechanism fail ?
        boolean success = true;
        HttpSession session = request.getSession();
        String sessionTime = new Date(session.getCreationTime()).toString();
        String message = "Session creation time  : " + sessionTime;
        session.invalidate();
        message += "- Session invalidated at : " + new Date().toString();

        //coming from Ext => no direct forward. but a json object
        String json = "{\"success\":";
        json += success;
        json += ", \"failure\":";
        json += !success;
        json += ", \"message\":\"";
        json += message;
        json += "\"}";
        response.setContentType("application/x-json");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
        return null;
    }
}
