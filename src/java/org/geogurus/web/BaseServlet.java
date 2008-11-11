/*
 * BaseServlet.java
 *
 * Created on April 26, 2002, 10:41 AM
 */
package org.geogurus.web;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geogurus.gas.utils.ObjectKeys;
/**
 * Base servlet containing common features for all JSP servlets in this application.
 * All other servlets should extend this one.
 *
 * (Constants used by this class are defined in the ObjectKey class.)
 * @author  Nicolas Ribot
 */
public class BaseServlet extends HttpServlet {
    protected transient Logger logger = Logger.getLogger(getClass().getName());
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
     * Child class must overwrite this method to define the contextual action.
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
        logger.fine("____Request parameters_____");
        while (en.hasMoreElements()) {
            String nam = (String)en.nextElement();
            String[] vals = request.getParameterValues(nam);
            
            for (int i = 0; i < vals.length; i++) {
                logger.fine("name: " + nam + " - value: " + vals[i]);
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
