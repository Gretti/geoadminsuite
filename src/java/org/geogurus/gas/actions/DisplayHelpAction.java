/*
 * DisplayHelpAction.java
 *
 * Created on 12 d�cembre 2006, 19:12
 *
 */

package org.geogurus.gas.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author GNG
 */

public class DisplayHelpAction extends Action 
{
    @Override
   public ActionForward execute(ActionMapping mapping, 
                                ActionForm form, 
                                HttpServletRequest request, 
                                HttpServletResponse response)
      throws IOException, ServletException 
   {

        ActionForward forward = null;
        
        String display = request.getParameter("helpme") == null ? "no" : "yes";
        String caller_jsp = request.getParameter("caller");
        
        request.getSession().setAttribute("display_help", display);
   
        forward = new ActionForward("/" + caller_jsp);
        
        return forward;
   }
}