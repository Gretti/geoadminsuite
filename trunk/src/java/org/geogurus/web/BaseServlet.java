/*
 * BaseServlet.java
 *
 * Created on April 26, 2002, 10:41 AM
 */
package org.geogurus.web;
import javax.servlet.http.*;
import javax.servlet.*;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.LogEngine;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Enumeration;
/**
 * Base servlet containing common features for all JSP servlets in this application.
 * All other servlets should extend this one.
 *
 * (Constants used by this class are defined in the ObjectKey class.)
 * Copyright:    Copyright (c) 2001
 * Company: SCOT
 * @author  Nicolas Ribot
 */
public class BaseServlet extends HttpServlet {
    /** The JSP page to deal with error messages in the application */
    public final static String JSP_ERROR_PAGE = "error.jsp";
    /** servlets accept both type of methods: get or post */
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        process(request, response);
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        process(request, response);
    }
    
    /**
     * Main method listening to client requests.
     * Child class must everwrite this method to define the contextual action.
     *
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
    }
    
    /**
     * perform the dispatch to the given url
     */
    protected void dispatch(HttpServletRequest request, HttpServletResponse response, String newURL) {
        try {
            request.getRequestDispatcher(newURL).forward(request, response);
        } catch (Exception se) {
            debugToClient(response, "IOException with request dispatcher for redirection to " + newURL + ":<br>" + se.getMessage());
            se.printStackTrace();
        }
    }
    /**
     * Logs the given message to the response's PrintWriter for hard client debugging
     */
    protected void debugToClient(HttpServletResponse response, String message) {
        try {
            PrintWriter out = response.getWriter();
            out.write(message);
            out.close();
            return;
        } catch (IOException ioe) {ioe.printStackTrace();	}
    }
    /**
     * Logs all request parameters
     */
    protected void debugParameters(HttpServletRequest request) {
        Enumeration en = request.getParameterNames();
        LogEngine.log("____Request parameters_____");
        while (en.hasMoreElements()) {
            String nam = (String)en.nextElement();
            String[] vals = request.getParameterValues(nam);
            
            for (int i = 0; i < vals.length; i++) {
                LogEngine.log("name: " + nam + " - value: " + vals[i]);
            }
        }
    }
    /**
     * Generates an error message corresponding to session expiration and dispatch to error page
     */
    protected void sessionHasExpired(HttpServletRequest request, HttpServletResponse response, String obj) {
        String error = "L'objet " + obj + " est manquant (null). La session a du expirer. Recharger l'application";
        request.getSession(true).setAttribute(ObjectKeys.SERVLET_ERROR, error);
        dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
        return;
    }
}
