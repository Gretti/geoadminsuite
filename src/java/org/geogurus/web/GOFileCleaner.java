package org.geogurus.web;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Timer;
import java.util.Date;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.geogurus.gas.utils.ObjectKeys;

import org.geogurus.tools.util.FileDeletionTimerTask;
import org.geogurus.tools.LogEngine;
import org.geogurus.mapserver.tools.MapTools;

/**
 * <strong>SIGServlet</strong> initializes and finalizes the storage of User Mapfile
 * This mapfile is read from a file, corrected for paths, and stored in session under
 * the Constants.USER_MAP_KEY key
 *
 * @author Nicolas Ribot
 */

public final class GOFileCleaner extends HttpServlet {
    /**
     * The file separator
     */
    private final String FS = System.getProperty("file.separator");
    /**
     * The debugging detail level for this servlet.
     */
    private int debug = 0;
    /**
     * The timer for tmp maps deletion. Active only if servlet init parameter
     * "tempMapsDeletionDelay" is set to a value in ms (delay at which temp maps must be deleted)
     * 24 hours is 24 * 3600 * 1000 = 86400000 ms
     *
     */
    private Timer timer = null;
    /**
     * Gracefully shut down this servlet, releasing any resources
     * that were allocated at initialization.
     */
    public void destroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    /**
     * Initialize this servlet, including loading our initial mapfile from
     * reference file.  The following servlet initialization parameters
     * are processed, with default values in square brackets:
     * <ul>
     * <li><strong>debug</strong> - The debugging detail level for this
     *     servlet, which controls how much information is logged.  [0]
     * </ul>
     *
     * @exception ServletException if we cannot configure ourselves correctly
     */
    public void init() throws ServletException {
        
        Properties p = new Properties();
        
        try {
            p.load(getClass().getClassLoader().getResourceAsStream("org/geogurus/gas/resources/geonline.properties"));
        } catch (Exception e) {
            System.out.println("cannot load properties file");
            e.printStackTrace();
        }
        
        // initialize the LogEngine
        boolean debug = (p.getProperty("DEBUG") != null && p.getProperty("DEBUG").equalsIgnoreCase("TRUE")) ? true : false;
        LogEngine.setDebugMode(debug);

        // the temp mapfile deletion delay, based upon the initialisation parameter:
        // tmp maps will be deleted each <tempMapsDeletionDelay>
        String periodS = p.getProperty("tempMapsDeletionPeriod");
        // the temp mapfile deletion time range, based upon the initialisation parameter:
        // tmp maps will be deleted if older than <tempMapsDeletionTimeRange>
        String timeRange = p.getProperty("tempMapsDeletionTimeRange");
        
        if (periodS != null && timeRange != null) {
            try {
                // the time to wait before cleaning mapfiles: converts it in milliseconds
                long period = new Long(periodS).longValue() * 1000;
                long timerange = new Long(timeRange).longValue() * 1000;
                String path = getServletContext().getRealPath("").replace('\\','/') + "/msFiles/tmpMaps/*.*";
                FileDeletionTimerTask fd = new FileDeletionTimerTask(path, timerange, FileDeletionTimerTask.INTERVAL_BEFORE);
                // this timer is a daemon
                timer = new Timer(true);
                timer.schedule(fd, new Date(), period);
            } catch (NumberFormatException nfe) {nfe.printStackTrace();}
        }
        
        // also loads all symbols into a context Hashtable
        String symFile = p.getProperty("GAS_SYMBOL_FILE");
        Hashtable symHash = new Hashtable();
        
        if (symFile == null) {
            LogEngine.log("Missing GAS_SYMBOL_FILE parameter.");
        } else {
            // build full path to GAS symbol file
            if (symFile.charAt(0) != '/' && symFile.charAt(1) != ':') {
                //symFile is relative, build full path
                symFile = getServletContext().getRealPath("").replace('\\', '/') + "/" + symFile;
            }
            
            //loads the symbols
            ArrayList al = MapTools.getSymbolsFromSym(new File(symFile));
            if (al != null) {
                symHash = MapTools.makeHashtableFromArrayList(al);
            }
            al = null;
        }
        
        getServletContext().setAttribute(ObjectKeys.GAS_SYMBOL_LIST, symHash);

    }
}
