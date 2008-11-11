package org.geogurus.web;

import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.geogurus.gas.objects.SymbologyListBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.tools.util.FileDeletionTimerTask;

/**
 * <strong>SIGServlet</strong> initializes and finalizes the storage of User Mapfile
 * This mapfile is read from a file, corrected for paths, and stored in session under
 * the Constants.USER_MAP_KEY key
 *
 * @author Nicolas Ribot
 */

public final class GOFileCleaner extends HttpServlet {
    transient Logger logger = Logger.getLogger(getClass().getName());
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
    @Override
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
    @Override
    public void init() throws ServletException {
        
        Properties p = new Properties();
        
        try {
            p.load(getClass().getClassLoader().getResourceAsStream("org/geogurus/gas/resources/geonline.properties"));
        } catch (Exception e) {
            logger.severe("cannot load geonline.properties file");
            e.printStackTrace();
        }

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
        
        // also loads all symbologies into the SymbologyListBean object and stores it in application context
        String symFile = p.getProperty("GAS_SYMBOL_FILE");
        SymbologyListBean symListBean = null;
        if (symFile == null) {
            logger.warning("Missing GAS_SYMBOL_FILE parameter.");
        } else {
            // build full path to GAS symbol file
            if (symFile.charAt(0) != '/' && symFile.charAt(1) != ':') {
                //symFile is relative, build full path
                symFile = getServletContext().getRealPath("").replace('\\', '/') + "/" + symFile;
            }
            
            p = new Properties();
            try {
                p.load(getClass().getClassLoader().getResourceAsStream("org/geogurus/gas/resources/symbology.properties"));
            } catch (Exception e) {
                logger.severe("cannot load symbology.properties file");
                e.printStackTrace();
            }
            //loads the symbols
            symListBean = new SymbologyListBean();
            symListBean.load(symFile, p);
        }
        getServletContext().setAttribute(ObjectKeys.GAS_SYMBOL_LIST, symListBean);
    }
}
