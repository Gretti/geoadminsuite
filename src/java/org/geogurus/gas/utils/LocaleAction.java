/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * LocaleAction.java
 *
 * Created on 5 juin 2005, 17h16
 */
package org.geogurus.gas.utils;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
/**
 * Action to allow user to change the locale.
 * Talen from struts-mailreade example
 * @author  Nicolas
 */
public class LocaleAction extends Action {
    /**
     * <p>Parameter for @link(java.util.Locale) language property.
     * ["language"]</p>
     */
    private static final String LANGUAGE = "language" ;
    
    /**
     * <p>Parameter for @link(java.util.Locale) country property.
     * ["country"]</p>
     */
    private static final String COUNTRY = "country";
    
    /**
     * <p>Parameter for response page URI. ["page"]</p>
     */
    private static final String PAGE = "page";
    
    /**
     * <p>Parameter for response forward name.
     * ["forward"]</p>
     */
    private static final String FORWARD = "forward";
    
    /**
     * <p>Logging message if LocaleAction is missing a target parameter.</p>
     */
    private static final String LOCALE_LOG = "LocaleAction: Missing page or forward parameter";
    
    /**
     * <p>
     * Change the user's Struts @link(java.util.Locale) based on request
     * parameters for "language", "country".
     * After setting the Locale, control is forwarded to an URI path
     * indicated by a "page" parameter, or a forward indicated by a
     * "forward" parameter, or to a forward given as the mappings
     * "parameter" property.
     * The response location must be specified one of these ways.
     * </p>
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @return An ActionForward indicate the resources that will render the response
     * @exception Exception if an input/output error or servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping,
    ActionForm form,
    HttpServletRequest request,
    HttpServletResponse response)
    throws Exception {
        
        Logger stdLog = Logger.getLogger(this.getClass().getName());
        
        String language = request.getParameter(LANGUAGE);
        String country = request.getParameter(COUNTRY);
        Locale locale = getLocale(request);
        
        if ((!isBlank(language)) && (!isBlank(country))) {
            locale = new Locale(language, country);
        }
        else if (!isBlank(language)) {
            locale = new Locale(language, "");
        }
        
        HttpSession session = request.getSession();
        session.setAttribute(Globals.LOCALE_KEY, locale);
        
        String target = request.getParameter(PAGE);
        if (!isBlank(target)) return new ActionForward(target);
        
        target = request.getParameter(FORWARD);
        if (isBlank(target)) {
            target = mapping.getParameter();
        }
        if (isBlank(target)) {
            stdLog.info(LOCALE_LOG);
            return null;
        }
        return mapping.findForward(target);
    }
    
    /**
     * <p>Return true if parameter is null or trims to empty.</p>
     * @param string The string to text; may be  null
     * @return true if parameter is null or empty
     */
    private boolean isBlank(String string) {
        return ((string==null) || (string.trim().length()==0));
    }
}
