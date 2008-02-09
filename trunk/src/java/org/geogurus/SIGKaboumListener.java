package org.geogurus;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import org.geogurus.tools.LogEngine;
import org.geogurus.tools.DataManager;
import org.geogurus.tools.sql.ConPool;

/**
 * Title:        geOnline Server classes
 * Description:  Set of Java classes to make the link between kaboum client and shape files / postgis DB containing geodata.
 * Copyright:    Copyright (c) 2002
 * Company:      SCOT
 * @author Niclas Ribot
 * @version 1.0
 */

/**
 * The servlet receiving requests from client (SIG part).
 * See SIGUserParameters description for valid bean info for this class
 * Request parameters are:
 * ACTION=
 *
 */
public class SIGKaboumListener extends HttpServlet {
	/** The key name for the SIGUserParameters object stored in the session */
	public static final String USER_SESSION_KEY = "com.scot.geonline.SIGUserParameters";
	/** The constant for the system-independant new line separator*/
	protected final String NEWLINE = System.getProperty("line.separator");
    /** application parameters keys used to store application data: refers to properties file
     *  load by this servlet
     *  ALLOWGEOMODIFICATION allows/disable the modification of geographic objects.
     *  DEBUGMODE       the debug mode: true => extensive system.err, false: no log
     *  DBHOST          the name of the host running the database
     *  DBPORT          the port on which the db is running
     *  DBNAME          the name of the database
     *  DBUSER          the user to make connection
     *  DBPWD           the user pwd to make connection
     *  WEBPATH         the absolute URL to find JSP, images, etc... Pages's url are relative to this path
     *  MAPWIDTH        the width of the applet containing the map. This width MUST correspond to the mapfile width
     *  MAPHEIGHT       the height of the applet containing the map. This height MUST correspond to the mapfile height
     *  MAPSERVERURL    the absolute URL to the mapserver CGI program
     *  MAPFILEPATH     the absolute path to the mapfile controling this application
     *  DATAPATH        the absolute path to the directory containing the data for this application (raster, vectors)
     *  BUSYIMAGEURL    the absolute URL to the busy image displayed by applet
     *  SERVLETPATH     the absolute URL to the servlet listening to the client requests
     *
     */

    /** this class version, used for debug */
    protected final String version = "1.0";

    /** the JSP page to display messages (error + info): this servlet puts error messages in session,
     *  under the "SIGApplicationError" key.
     *  and the information messages under the "SIGApplicationMessage" key.
     *  The JSP page can then gets the message with:
     *  String msg = session.getAttribute("SIGApplicationError");
     *  This page is loaded from Properties file
     */
     protected String SIGMessagePage;

//******************************************************************************
//**************** METHODS *****************************************************
//******************************************************************************

public void start() {}
public void stop() {}

public void init(ServletConfig config) throws ServletException {
    super.init(config);
    //load all application properties from properties file
    // the full path to the properties file is a servlet parameter
    // all properties will be stored in the ServletContext
    try {
        String pfile = config.getInitParameter("PROPERTIES_FILE");
        Properties props = new Properties();
        props.load(new FileInputStream(pfile));
        // sets the debug mode to the LogEngine level
        LogEngine.setDebugMode(props.getProperty("DEBUGMODE").equalsIgnoreCase("true"));
        // sets properties into the ServletContext to make them available in the JSP pages
        config.getServletContext().setAttribute("CROPVISION_PROPERTIES", props);
        // stores one propertie at this servlet level
        SIGMessagePage = props.getProperty("SIGMESSAGEPAGE");
        // also makes the properties available for everyone by giving it to
        // DataManager class.
        DataManager.setProperties(props);
LogEngine.log("init completed");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public void doGet(HttpServletRequest request, HttpServletResponse response) {
    process(request, response);
}
public void doPost(HttpServletRequest request, HttpServletResponse response) {
    process(request, response);
}

public void process(HttpServletRequest request, HttpServletResponse response) {
	HttpSession session = request.getSession(false);
	if (session == null) {
        debugToClient(response, "process: null session: cannot get session from request ?!");
		return;
	}
	// the user-specific informations shuttle
	SIGUserParameters userParams = (SIGUserParameters)session.getAttribute(SIGKaboumListener.USER_SESSION_KEY);

	if (isRequestFromDispatcher(session, userParams)) {
		// another servlet calls us to process some action:
        initSIG(request, response, userParams);
    } else {
        // direct call to the servlet: client SIG request, mainly to validate a
        // particular Kaboum action
        String action = request.getParameter("action");

        if (action == null) {
            // a client CANNOT call this servlet directly. The only supported interface is:
            // action=validate, action=remove, action=selection
            session.setAttribute("SIGApplicationError", "SIGKaboumListener::process(): no 'action' parameter in servlet's request. Quit");
LogEngine.log("process: action is null, redirecting to: " + SIGMessagePage);
            dispatch(request, response, SIGMessagePage);
        }

        if (action.equals("debug")) {
        } else if (action.equals("validate")) {
            doValidate(request, response);
        } else if (action.equals("remove")) {
            doRemove(request, response);
        } else if (action.equals("selection")) {
            doSelection(request, response);
        } else {
            //debugToClient(response, "invalid 'action' command: " +action +"<p>Supported actions are: debug, view, update, login");
            session.setAttribute("SIGApplicationError", "process: invalid 'action' command: " +action +"<p>Supported actions are: debug, view, update, login");
            dispatch(request, response, SIGMessagePage);
            return;
        }
    }
}

/**
 * The SIG initialisation method: a request comes from another servlet with a
 * SIGUserParameters bean: see its properties and initialize a SIG page accordingly
 */
protected void initSIG(HttpServletRequest request,
                       HttpServletResponse response,
                       SIGUserParameters userParams) {


    String sigURL = userParams.getSIGURL();
LogEngine.log("initSIG: redirecting to : " + sigURL);
    //mark the SIGUserParameters mode as Processed to avoid recall of this method
    ((SIGUserParameters)request.getSession(false).getAttribute(SIGKaboumListener.USER_SESSION_KEY)).setMode(SIGUserParameters.PROCESSED);
    dispatch(request, response, sigURL);
}

/**
 * Validates the given object. This is the minimum task to do:
 * request parameters are: action=validate&value=<kaboum object validation>
 * where <kaboum object validation> is (see kaboum API for more details):
 * OBJECT|CLASS_NAME|OBJID|AREA|WKT
 *
 * User can extend this servlet and override this method to provide custom treatment
 */
protected void doValidate(HttpServletRequest request,HttpServletResponse response) {
    HttpSession session = request.getSession(true);
    SIGUserParameters up = (SIGUserParameters)session.getAttribute(SIGKaboumListener.USER_SESSION_KEY);

    if (up == null) {
        // session invalidated
        session.setAttribute("SIGApplicationError", "doValidate: session expired. Reload the application");
        dispatch(request, response, SIGMessagePage);
    }
    String kaboumObj = request.getParameter("value");

    if (kaboumObj == null) {
        session.setAttribute("SIGApplicationError", "doValidate: invalid parameters: expected value=<kaboum object> for action=validate");
        dispatch(request, response, SIGMessagePage);
        return;
    }
    StringTokenizer tk = new StringTokenizer(kaboumObj, "|");
    // discard "OBJECT" keyword
    tk.nextToken();
    String classname = tk.nextToken();
    String id = tk.nextToken();
    // discard "area" value
    tk.nextToken();
    String wkt = tk.nextToken();

    // construct a geometry from this object:
    Geometry geom = new Geometry(id, wkt, classname);

    // ask the corresponding geometry class to update/insert this object
    Connection con = ConPool.getConnection();

    if (! up.getGeometryClass(classname).addGeometryToDB(con, geom)) {
        session.setAttribute("SIGApplicationError", "doValidate: cannot add geometry to db: " + up.getGeometryClass(classname).getErrorMessage());
        dispatch(request, response, SIGMessagePage);
        return;
    }
    // make this geom available to JSP pages, for ex to redraw it in kaboum
    up.setUpdatedGeometry(geom);
    // put bean back to session:
    session.setAttribute(SIGKaboumListener.USER_SESSION_KEY, up);
    session.setAttribute("SIGApplicationMessage", "Geometry from class: " + classname
    + " (id=" + id + ") correctly added to DB.");
    
    if (up.getReturnURL() != null) {
        dispatch(request, response, up.getReturnURL());
    } else {
        dispatch(request, response, SIGMessagePage);
    }
}

/**
 * Removes the given object. This is the minimum task to do.
 * User can extend this servlet and override this method to provide custom treatment
 * URL parameters for this method are:
 * action=remove&value=<kaboum_object>&hardremove=<true|false>
 * where <kabom_object> is a valid kaboum geometry
 *hardremove is true to delete the entire record from the db, false to set the geometry to null
 * in the DB.
 */
protected void doRemove(HttpServletRequest request,HttpServletResponse response) {
    HttpSession session = request.getSession(true);
    SIGUserParameters up = (SIGUserParameters)session.getAttribute(SIGKaboumListener.USER_SESSION_KEY);

    if (up == null) {
        // session invalidated
        session.setAttribute("SIGApplicationError", "doRemove: session expired. Reload the application");
        dispatch(request, response, SIGMessagePage);
    }
    String kaboumObj = request.getParameter("value");
    String hr = request.getParameter("hardremove");
    boolean hardRemove = (hr == null || hr.equals("false")) ? false : true;

    if (kaboumObj == null) {
        session.setAttribute("SIGApplicationError", "doRemove: invalid parameters: expected value=<kaboum object> for action=remove");
        dispatch(request, response, SIGMessagePage);
        return;
    }
    StringTokenizer tk = new StringTokenizer(kaboumObj, "|");
    // discard "OBJECT" keyword
    tk.nextToken();
    String classname = tk.nextToken();
    String id = tk.nextToken();

    // ask the corresponding geometry class to remove this object
    Connection con = ConPool.getConnection();

    if (! up.getGeometryClass(classname).removeGeometryFromDB(con, id, hardRemove)) {
        session.setAttribute("SIGApplicationError", "doRemove: cannot add geometry to db: " + up.getGeometryClass(classname).getErrorMessage());
        dispatch(request, response, SIGMessagePage);
        return;
    }
    // now geometry is removed, update SIGUserParameters: there is no more a valid UpdatedObject:
    up.setUpdatedGeometry(null);

    // put message in session in all cases, to allow clients to get it
    session.setAttribute("SIGApplicationMessage", "Geometry (id=" + id + ") correctement effacee.");
    
    if (up.getReturnURL() != null) {
        dispatch(request, response, up.getReturnURL());
    } else {
        dispatch(request, response, SIGMessagePage);
    }
}

/**
 * Selection for the given object(s).
 * User can extend this servlet and override this method to provide custom treatment.
 * No treatment provided by default.
 */
protected void doSelection(HttpServletRequest request,HttpServletResponse response) {
}
/**
* Tests the session to guess if request comes from a client or from another servlet<br>
* @param session: the HttpSession, valid in this context
* @param userParams: the SIGUserParameters beam associated with this session, must
* not be null for the request to come from a RequestDispatcher
* @return true if the <code>HttpRequest</code> producung the session comes from another servlet
*
*/
protected boolean isRequestFromDispatcher(HttpSession session, SIGUserParameters userParams) {
    if (session == null) {
        return false;
    }

    if (userParams == null) {
        return false;
    }
    return userParams.getMode() != SIGUserParameters.ERROR &&
           userParams.getMode() != SIGUserParameters.PROCESSED;
    }

/**
 * perform the dispatch to the given url
 */
protected void dispatch(HttpServletRequest request, HttpServletResponse response, String newURL) {
    if (newURL == null) {
        String err = (String)request.getSession(true).getAttribute("SIGApplicationError");

        if (err != null) {
            // an application error exists in the session: no JSP page to display it:
            // send it directly to the client browser:
            debugToClient(response, "SIGApplicationError: <b><code>" + err + "</code></b>");
        }
        debugToClient(response, "dispatch: null URL ");
    }
    try {
        request.getRequestDispatcher(newURL).forward(request, response);
    } catch (Exception se) {
        debugToClient(response, version + " IOException with request dispatcher for redirection to " + newURL + ":<br>");
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

}