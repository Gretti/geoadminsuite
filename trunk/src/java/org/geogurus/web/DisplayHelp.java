/*
 * HostLoader.java
 *
 * Created on 22 juin 2003, 16:05
 */
package org.geogurus.web;
import javax.servlet.http.*;
/**
 * @author  gng
 */
public class DisplayHelp extends BaseServlet {
    private String caller_jsp;
    /**
     * Receives an order to display or not the contextual help
     *
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String display = request.getParameter("helpme") == null ? "no" : "yes";
        caller_jsp = request.getParameter("caller");
        
        session.setAttribute("display_help", display);
        // dispatch now to the tree page
        dispatch(request, response, caller_jsp);
    }
    
}
