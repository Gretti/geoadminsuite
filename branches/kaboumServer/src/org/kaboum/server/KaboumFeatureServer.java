/*
 * KaboumFeatureServer.java
 *
 * Created on 18 aout 2005, 17:41
 */
package org.kaboum.server;

import com.vividsolutions.jts.geom.Envelope;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.server.utils.KaboumJTSFactory;
import org.kaboum.util.KaboumFeatureModes;
import org.kaboum.util.KaboumFeatureShuttle;

/**
 * The class controlling access to geometries, by converting KaboumGeometries into JTS geometries 
 * and calling the right GeometryAccess method according to the shuttle's mode
 * <p>
 * This class will maintain the list of geometries identifiers loaded into Kaboum,
 * to filter out geometries based on ID:
 * Geometries already loaded into Kaboum won't be loaded again.<br>
 * This mechanism must be improved in case of session expiration.<br>
 * This mechanism will take place ONLY IF following KaboumServer parameters are not set to false:
 * SERVER_VALIDATE_TO_DATASTORE
 * SERVER_DELETE_FROM_DATASTORE
 * </p>
 * @author Nicolas Ribot
 */
public class KaboumFeatureServer {

        /** the key under which the set of Kaboum geometries identifiers is stored, in the
         * servlet session
         */
        public static final String KABOUM_ID_LIST = "org.kaboum.server.KaboumFeatureServer.KaboumIDList";
        /** the error message */
        private String errorMessage;
        /** this class's logger */
        private transient final Logger logger = Logger.getLogger(KaboumFeatureServer.class);
        /**
         * the GeometryAccess instance that will get/set geometries in the underlying data storage
         * <p> Kaboum Server uses its own GeometryAccess object to retrieve geometries
         * and will build one according to the source type specified in the configuration file.
         * Third party applications provide this class with their own implementation of the
         * GeometryAccess interface by declaring the name of the class in the KaboumFeatureServlet configuration
         * file (web.xml)
         * </p>
         */
        private GeometryAccess geometryAccess;
        /**
         * The user HttpRequest containing session information used by this class to manage
         * the geometric objects identifiers.
         */
        private HttpServletRequest httpRequest;

        /**
         * The constructor taking a reference on the <CODE>GeometryAccess</CODE> to use to process the shuttle
         * and the the client request used to get session-specific information, like the
         * list of geometries IDs loaded into Kaboum.
         * @param geomAccess The reference to the class implementing the <CODE>GeometryAccess</CODE> interface that will be used
         * to access geometries
         * @param request The current HttpServletRequest as received by the <CODE>KaboumFeatureServlet</CODE> servlet
         * @see GeometryAccess
         */
        public KaboumFeatureServer(GeometryAccess geomAccess, HttpServletRequest request) {
                this.geometryAccess = geomAccess;
                httpRequest = request;
        }

        /**
         * The main entry point of this class, dealing with feature retrieval or saving.
         * According to the shuttle's mode, this method will call the appropriate
         * GeoemtryAccess method and either retrieve some geometries or save/remove
         * them in/from the underlying data store.
         * @param shuttle - The KaboumFeatureShuttle object to process
         * @return a KaboumFeatureShuttle object with the mode and geometries updated according
         * to the action.
         */
        public KaboumFeatureShuttle processGeometryAccess(KaboumFeatureShuttle shuttle) {
                long begin = System.currentTimeMillis();
                logger.info("Processing GeometryAccess...");
                if (shuttle == null) {
                        logger.error("Cannot process input shuttle: it is null");
                        KaboumFeatureShuttle s = new KaboumFeatureShuttle();
                        s.errorCode = KaboumFeatureModes.K_NULL_SHUTTLE;
                        return s;
                }
                // Convert shuttle's kaboumGeometries into JTS geometries:
                Hashtable jtsGeometries = KaboumJTSFactory.getJTSGeometries(shuttle.geometries);

                switch (shuttle.mode) {
                        case KaboumFeatureModes.K_ADD_FEATURES:
                                logger.debug("Adding features");
                                shuttle.geometries = KaboumJTSFactory.getKaboumGeometries(
                                        geometryAccess.addGeometries(jtsGeometries));

                                shuttle.errorCode = geometryAccess.getErrorCode();
                                break;
                        case KaboumFeatureModes.K_REMOVE_FEATURES:
                                logger.debug("Removing features");
                                shuttle.errorCode = geometryAccess.removeGeometries(jtsGeometries);

                                if (shuttle.mode == KaboumFeatureModes.K_REMOVE_FEATURES) {
                                        // can ligth the shuttle by removing the geometrie
                                        // otherwise, client still have passed shuttle to correct it eventually.
                                        // no more needed as we have to update kaboum geometries id list
                                        //shuttle.geometries = null;
                                }
                                break;
                        case KaboumFeatureModes.K_UPDATE_FEATURES:
                                logger.debug("Updating features");
                                shuttle.geometries = KaboumJTSFactory.getKaboumGeometries(geometryAccess.updateGeometries(jtsGeometries));
                                shuttle.errorCode = geometryAccess.getErrorCode();
                                break;
                        case KaboumFeatureModes.K_GET_FEATURES:
                                logger.debug("Getting features");
                                //Builds a JTS envelope from the given shuttle's map extent.
                                Envelope env = KaboumJTSFactory.getEnvelope(shuttle.parameters.getProperty("mapext"));
                                int scale = Integer.MIN_VALUE;

                                try {
                                        scale = Integer.parseInt(shuttle.parameters.getProperty("mapscale"));
                                } catch (NumberFormatException nfe) {
                                }

                                if (env == null) {
                                        errorMessage = "Cannot get valid envelope (null) for string: "
                                                + shuttle.parameters.getProperty("mapext");
                                        logger.error(errorMessage);
                                        shuttle.errorCode = KaboumFeatureModes.K_MISSING_MAP_EXTENT;
                                } /* else if (scale == Integer.MIN_VALUE) {
                                errorMessage = "missing or invalid scale : " +  shuttle.parameters.getProperty("mapscale");
                                logger.info(errorMessage);
                                shuttle.errorCode = KaboumFeatureModes.K_MISSING_SCALE;
                                }*/ else {
                                        //Hashtable hash = KaboumJTSFactory.getKaboumGeometries(geometryAccess.getGeometries(env, scale));
                                        Hashtable hash = KaboumJTSFactory.getKaboumGeometries(geometryAccess.getGeometries(env));
                                        shuttle.geometries = hash;
                                        shuttle.errorCode = geometryAccess.getErrorCode();

                                }
                                break;
                        default:
                                // shoud not happen has the called already checked the shuttle's mode
                                // before calling this method.
                                logger.error("Kaboum mode not understood: " + shuttle.mode);
                                shuttle.errorCode = KaboumFeatureModes.K_INVALID_OBJECT;
                                break;
                }
                // reset the shuttle parameters with Properties coming from the GeometryAccess.
                // this allows third-party application to modify properties on the fly according to the current
                // context (one properties set per map, for instance)
                shuttle.parameters = geometryAccess.getKaboumServerProperties();
                //return manageGeometriesID(shuttle);
                // no more id management on server, time to find an efficient mechanism.
                // dealing with sessions expirations and client refresh
                long end = System.currentTimeMillis();
                logger.info("GeometryAccess processed in: " + ((end-begin)/1000.0));
                return shuttle;
        }

        /**
         * Manage the list of geometries identifiers received from the datastore and sent to kaboum applet:
         * Removes from the shuttle geometries already loaded into kaboum, add in the list new geometries,
         * removes from the list geometries that are deleted from the client.
         * @param shuttle The <CODE>KaboumFeatureShuttle</CODE> object containing <CODE>Geometry</CODE> to filter
         * @return A <CODE>KaboumFeatureShuttle</CODE> with geometries updated according to the id-based filter rules
         */
        protected KaboumFeatureShuttle manageGeometriesID(KaboumFeatureShuttle shuttle) {
                if (!manageIDOnServer(shuttle)) {
                        // server parameters do not allow to manage ids in the server.
                        // doing so would generate inconsistent geometries state.
                        logger.info("Skipping id management because of server parameters");
                        return shuttle;
                }
                long t0 = System.currentTimeMillis();
                HttpSession session = httpRequest.getSession();
                Object o = session.getAttribute(KABOUM_ID_LIST);
                HashSet kaboumIDList = null;
                Hashtable geoms = shuttle.geometries;

                if (o == null) {
                        // no list in session: either first client request or session has expired
                        // build it from the shuttle
                        logger.info("no existing id list in session. Session is new ? " + session.isNew() + " sessionID: " + session.getId());
                        if (geoms != null) {
                                kaboumIDList = new HashSet();
                                Enumeration keys = geoms.keys();
                                while (keys.hasMoreElements()) {
                                        Vector vec = (Vector) geoms.get(keys.nextElement());
                                        if (vec != null) {
                                                for (Iterator iter = vec.iterator(); iter.hasNext();) {
                                                        kaboumIDList.add(((KaboumGeometry) iter.next()).id);
                                                }
                                        }
                                }
                        } else {
                                logger.error("null geometries hashtable while trying to build kaboum IDs list");
                        }
                } else {
                        // manages shuttle modes
                        kaboumIDList = (HashSet) o;
                        //String msg = "";
                        //String title = "";
                        if (geoms != null) {
                                Enumeration keys = geoms.keys();
                                while (keys.hasMoreElements()) {
                                        Vector vec = (Vector) geoms.get(keys.nextElement());
                                        if (vec != null) {
                                                for (Iterator iter = vec.iterator(); iter.hasNext();) {
                                                        KaboumGeometry geom = (KaboumGeometry) iter.next();
                                                        switch (shuttle.mode) {
                                                                case KaboumFeatureModes.K_GET_FEATURES:
                                                                        //title = "list of filtered ids: ";
                                                                        // removes all geometries whose id is in the list
                                                                        if (kaboumIDList.contains(geom.id)) {
                                                                                iter.remove();
                                                                                //msg += geom.id + " ";
                                                                        } else {
                                                                                // and adds those that are not in the list
                                                                                kaboumIDList.add(geom.id);
                                                                        }
                                                                        break;
                                                                case KaboumFeatureModes.K_ADD_FEATURES:
                                                                        //title = "list of added ids: ";
                                                                        // adds all server-generated ids to the list
                                                                        kaboumIDList.add(geom.id);
                                                                        //msg += geom.id + " ";
                                                                        break;
                                                                case KaboumFeatureModes.K_REMOVE_FEATURES:
                                                                        // removes all 'server-removed' ids from the list
                                                                        //title = "list of removed ids: ";
                                                                        // adds all server-generated ids to the list
                                                                        kaboumIDList.remove(geom.id);
                                                                        //msg += geom.id + " ";
                                                                        break;
                                                                default:
                                                                // no action for other modes, in particular:
                                                                // K_UPDATE_FEATURES must not be filtered as kaboum is waiting
                                                                // for the updated polygon
                                                        }
                                                } // end for each geometry in the current kaboum class name
                                        }
                                } // end while kaboum geometry class names
                                //logger.info(title + msg);
                        }
                }

                //finally, stores the updated id list in session
                httpRequest.getSession().setAttribute(KABOUM_ID_LIST, kaboumIDList);

                long t1 = System.currentTimeMillis();
                //logger.info("Time to process id filtering: " + (t1-t0));
                return shuttle;
        }

        /**
         * Returns the error message.
         * Error message will be set by this class' methods if something goes wrong
         * @return The error message
         */
        public String getErrorMessage() {
                return errorMessage;
        }

        /**
         * Returns true if shuttle's server parameters are set such that managing ids on the server
         * is not dangerous.
         *<p>
         * Namely, if SERVER_VALIDATION_TO_DATASTORE or SERVER_DELETION_FROM_DATASTORE is set to false,
         * this method will return false. (because in this case, client can handle geometries without any server
         * control, potentially leading to an inconsistent geometries state.
         *</p>
         *@param shuttle the KaboumFeatureShuttle object containing parameters to test
         *@return true if SERVER_VALIDATION_TO_DATASTORE and SERVER_DELETION_FROM_DATASTORE are set to true, or not set at all
         */
        private boolean manageIDOnServer(KaboumFeatureShuttle shuttle) {
                if (shuttle == null || shuttle.parameters == null) {
                        return false;
                }

                String val = shuttle.parameters.getProperty("SERVER_VALIDATE_TO_DATASTORE");
                String del = shuttle.parameters.getProperty("SERVER_DELETE_FROM_DATASTORE");

                return (val == null || "true".equalsIgnoreCase(val)) && (del == null || "true".equalsIgnoreCase(del));
        }
}
