/*
 * KaboumServer.java
 *
 * Created on 5 aout 2005, 15:18
 */
package org.kaboum.server;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.kaboum.server.topology.JTSFactory;
import org.kaboum.util.KaboumFeatureModes;
import org.kaboum.util.KaboumFeatureShuttle;

/**
 * The server class dealing with Kaboum Applet client requests to display and manage features (= geometric objects)
 * in Kaboum.
 * <p>
 * This class receives client requests and according to the mode, will process the request and returns
 * a response (either a message, a list of geometric objects or both) to the client.
 * </p>
 * <p>
 * A KaboumFeatureShuttle object is used in its serialized form to exchange information between client and server.
 * </p>
 * @author Nicolas Ribot
 * @see <CODE>KaboumFeatureShuttle</CODE>
 */
public class KaboumFeatureServlet extends HttpServlet {

        public final static String GEOMETRY_ACCESS_CLASS = "GEOMETRY_ACCESS_CLASS";
        public final static String JTS_FACTORY = "JTS_FACTORY";
        /**
         * The name of this servlet's web.xml startup parameter configuring the
         * GeometryAccess instance to use in place of default KaboumServer mechanism
         */
        public static String KABOUM_GEOMETRY_ACCESS_CLASS = "KaboumGeometryAccessClass";
        /**
         * The name of this servlet's web.xml startup parameter configuring the
         * Kaboum server properties files containig vector objects parameters
         */
        public static String KABOUM_PROPERTIES_FILE = "KaboumPropertiesFile";
        /**
         * The qualified name of the GeometryAccess implementation that
         * should be used instead the Kaobum default one
         * <p>
         * Third party applications can implement their own mechanism to access
         * features.
         * </p>
         * <p>
         * The name of the implementation class must be configured in the web.xml.
         * </p>
         */
        private String geometryAccessClassName;
        /**
         * The path of the Kaboum properties file
         */
        private String kaboumPropertiesFile;
        private static Logger logger = Logger.getLogger(KaboumFeatureServlet.class);

        /**
         * Main method dealing with GET and POST requests.
         * Will process the commands based on MODE parameters and call factories/classes accordingly.
         * Supported modes are:
         * <ul>
         * <li></li>
         * <li></li>
         * </ul>
         * @param request The <CODE>HttpServletRequest</CODE> object
         * @param response The HttpServletResponse object
         */
        public void process(HttpServletRequest request, HttpServletResponse response) {
                ObjectInputStream oin = null;
                ObjectOutputStream oout = null;
                KaboumFeatureShuttle shuttleOut = null;
                GeometryAccess geomAccess = null;
                JTSFactory jtsFact = null;

                long begin = System.currentTimeMillis();
                long tprops = 0;
                long tfin = 0;
                try {
                        oin = new ObjectInputStream(request.getInputStream());
                        KaboumFeatureShuttle shuttleIn = (KaboumFeatureShuttle) oin.readObject();
                        // gets shuttle from
                        String userMD = shuttleIn == null ? "" : shuttleIn.userMetadata;

                        logger.info("---------KaboumFeatureServlet called from: " + request.getRemoteHost());
                        logger.debug("A client request with user metadata: " + userMD);

                        // stores the given user metadata into the request.
                        request.setAttribute(KaboumFeatureModes.K_USER_METADATA, userMD);

                        // gets the Kaboum parameters each time to allow user application to change
                        // parameters on the fly.
                        shuttleOut = loadKaboumProperties(shuttleIn);
                        HttpSession session = request.getSession();
                        jtsFact = (JTSFactory) session.getAttribute(KaboumFeatureServlet.JTS_FACTORY);
                        geomAccess = (GeometryAccess) session.getAttribute(KaboumFeatureServlet.GEOMETRY_ACCESS_CLASS);
                        if (jtsFact == null || geomAccess == null) {
                                // creates major manager objects and stores them in user session
                                //JTS
                                logger.debug("Session is new, creating JTS factory and GeometryAccess instance and storing them in session");
                                jtsFact = new JTSFactory();
                                //GeometryAccess Class
                                geomAccess = getGeometryClassInstance(shuttleOut.parameters, request);
                                session.setAttribute(KaboumFeatureServlet.GEOMETRY_ACCESS_CLASS, geomAccess);
                                session.setAttribute(KaboumFeatureServlet.JTS_FACTORY, jtsFact);
                        } else {
                                //System.out.println("session is old, using objects");
                                geomAccess.setUserData(request);
                                geomAccess.setKaboumServerProperties(shuttleOut.parameters);
                        }
                        if (shuttleOut == null) {
                                // major error on client side or during transport
                                logger.error("Null geometries shuttle. " +
                                        "Unable to build a valid shuttle from client shuttle and KaboumServer properties file");
                                shuttleOut = new KaboumFeatureShuttle();
                                shuttleOut.errorCode = KaboumFeatureModes.K_NULL_SHUTTLE;
                        } else if (shuttleOut.errorCode < 0) {
                                // route shuttle to the class able to handle its current mode
                                switch (shuttleOut.mode) {
                                        case KaboumFeatureModes.K_UNION:
                                        case KaboumFeatureModes.K_INTERSECTION:
                                        case KaboumFeatureModes.K_POLYGON_SPLITTING:
                                        case KaboumFeatureModes.K_POLYGON_SPLITTING_BY_LINE:
                                        case KaboumFeatureModes.K_POLYGON_ERASING:
                                        case KaboumFeatureModes.K_DIFFERENCE:
                                        case KaboumFeatureModes.K_SYM_DIFFERENCE:
                                        case KaboumFeatureModes.K_FUSION:
                                        case KaboumFeatureModes.K_HOLES_COMPLETION:
                                        case KaboumFeatureModes.K_POLYGON_COMPLETION:
                                        case KaboumFeatureModes.K_POLYGON_COMPLETION_FITTING:
                                        case KaboumFeatureModes.K_POLYGON_FITTING:
                                                // a spatial analysis is requested
                                                shuttleOut = jtsFact.processSpatialAnalysis(shuttleOut);
                                                break;
                                        case KaboumFeatureModes.K_ADD_FEATURES:
                                        case KaboumFeatureModes.K_REMOVE_FEATURES:
                                        case KaboumFeatureModes.K_UPDATE_FEATURES:
                                        case KaboumFeatureModes.K_GET_FEATURES:
                                                if (geomAccess == null) {
                                                        logger.error("cannot instantiate a GeometryAccess object with given name: " + geometryAccessClassName);
                                                        shuttleOut.errorCode = KaboumFeatureModes.K_INVALID_GEOMETRY_ACCESS_CLASS;
                                                } else {
                                                        KaboumFeatureServer server = new KaboumFeatureServer(geomAccess, request);
                                                        shuttleOut = server.processGeometryAccess(shuttleOut);
                                                }
                                                break;
                                        default:
                                                // unsupported mode ??!!
                                                logger.error("Unknown KaboumFeatureMode: " + shuttleOut.mode
                                                        + " (see class KaboumFeatureModes in the Kaboum project " +
                                                        "for a list of supported codes.");
                                                shuttleOut.errorCode = KaboumFeatureModes.K_UNSUPPORTED_MODE;
                                                break;
                                }
                        }

                        // sends response shuttle to servlet response
                        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
                        oout = new ObjectOutputStream(bos);
                        oout.writeObject(shuttleOut);
                        oout.flush();

                        // closes streams
                        bos.close();
                        //oout.close();
                        oin.close();

                } catch (Exception e) {
                        logger.error("Unable to process incoming request.", e);
                        e.printStackTrace();
                }
                long end = System.currentTimeMillis();
                logger.debug("KaboumFeatureServlet done in: "
                        + ((end -  begin) / 1000.0) + "s");

        }

        /**
         * Loads the Kaboum Server parameters by loading the properties files configured in this servlet if the shuttle does not
         * contain a props properties giving the path of the properties file to use instead of the default one
         * @param shuttle the client shuttle
         * @return the shuttle with its parameters field set to a Properties object containing Kaboum Parameters
         */
        public KaboumFeatureShuttle loadKaboumProperties(KaboumFeatureShuttle shuttle) {
                if (shuttle == null) {
                        logger.error("null shuttle");
                        return null;
                }
                long begin = System.currentTimeMillis();
                logger.debug("Loading kaboumProperties...");
                // gets required client parameters.
                String mapExt = shuttle.parameters.getProperty("mapext");
                String mapScale = shuttle.parameters.getProperty("mapscale");
                String clientProps = shuttle.parameters.getProperty("props");
                if (clientProps != null) {
                        kaboumPropertiesFile = clientProps;
                }
                if (kaboumPropertiesFile == null) {
                        logger.error("Null KaboumServer properties file. The shuttle must " +
                                "contain a props key whose value is the path to the properties file, " +
                                "relative to the KaboumServer application path");
                        shuttle.errorCode = KaboumFeatureModes.K_MISSING_FEATURE_FILE;
                        return shuttle;
                }
                // loads properties each time, to allow applications to modify properties on-the-fly
                Properties props = new Properties();

                try {
                        InputStream propsIn = this.getServletContext().getResourceAsStream(kaboumPropertiesFile);
                        if (propsIn == null) {
                                shuttle.errorCode = KaboumFeatureModes.K_INVALID_FEATURE_FILE;
                                logger.error("Invalid KaboumServer properties file: " + kaboumPropertiesFile);
                                return shuttle;
                        }
                        props.load(propsIn);
                } catch (Exception e) {
                        // the given featurefile is invalid
                        shuttle.errorCode = KaboumFeatureModes.K_INVALID_FEATURE_FILE;
                        logger.error("Invalid KaboumServer properties file: " + kaboumPropertiesFile + " message: " + e.getMessage());
                        return shuttle;
                }

                shuttle.parameters = props;

                // re-set received parameters, as they are not contained in the properties file
                // TODO: PropertiesManager should manage that, by keeping existing properties before parsing
                // new ones.
                if (mapExt != null) {
                        shuttle.parameters.setProperty("mapext", mapExt);
                }
                if (mapScale != null) {
                        shuttle.parameters.setProperty("mapscale", mapScale);
                }
                if (clientProps != null) {
                        shuttle.parameters.setProperty("props", clientProps);
                }

                long end = System.currentTimeMillis();
                logger.debug("kaboumProperties loaded in: "
                        + ((end - begin) / 1000) + "s.");
                return shuttle;
        }

        /**
         * Initialisation stuff should be done here.
         * <p>
         * Gets the servlet's initialisation parameters
         * </p>
         * @param config the <CODE>ServletConfig</CODE> object that contains configutation information for this servlet
         * @throws javax.servlet.ServletException if an exception occurs that interrupts the servlet's normal operation
         */
        public void init(ServletConfig config) throws ServletException {
                super.init(config);

                // gets the name of the GeometryAccess implementation class
                geometryAccessClassName = config.getInitParameter(KABOUM_GEOMETRY_ACCESS_CLASS);

                // gets the path of the properties file
                kaboumPropertiesFile = config.getInitParameter(KABOUM_PROPERTIES_FILE);
        }

        /**
         * Returns an instance of the class implementing <CODE>GeometryAccess</CODE> interface that will be used by KaboumServer to manage
         * geographic objects, or null if the configured name of the class does not allow to instantiate the class by
         * invocation.
         * @param props The <CODE>Properties</CODE> object containing KaboumServer properties
         * @param request The <CODE>HttpServletRequest</CODE> object
         * @return An instance of <CODE>GeometryAccess</CODE>
         */
        private GeometryAccess getGeometryClassInstance(Properties props, HttpServletRequest request) {
                logger.debug("Getting GeometryClass Instance...");
                long begin = System.currentTimeMillis();
                if (geometryAccessClassName == null) {
                        logger.error("NULL GeometryAccess Class Name !! cannot process shuttle");
                        return null;
                }
                Object object = null;
                Class[] parameterTypes = new Class[]{Properties.class, Object.class};
                Object[] arguments = new Object[]{props, request};
                Constructor propsCtor = null;

                try {
                        Class classDefinition = Class.forName(geometryAccessClassName, true, Thread.currentThread().getContextClassLoader());
                        propsCtor = classDefinition.getConstructor(parameterTypes);
                        object = propsCtor.newInstance(arguments);
                } catch (Exception e) {
                        logger.error("Cannot instantiate a GeometryAccess object with given name: "
                                + geometryAccessClassName
                                + " reason: "
                                + e.getMessage());
                        e.printStackTrace();

                }
                if (!(object instanceof GeometryAccess)) {
                        logger.error("Given GeoemtryClass instance is not a " +
                                "instance of GeometryAccess class. Is: "
                                + object.getClass().getName());
                        return null;
                }
                long end = System.currentTimeMillis();
                logger.debug("GeometryClass Instance got: "
                        + object.getClass().getName()
                        + " in: " + ((end-begin)/1000.0)
                        + "s.");
                return (GeometryAccess) object;
        }

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) {
                process(request, response);
        }

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
                process(request, response);
        }

        /**
         * See destroy method in HttpServlet
         */
        @Override
        public void destroy() {
                logger.info("Destroying KaboumFeatureServlet.");
                geometryAccessClassName = null;
                kaboumPropertiesFile = null;
        }
}

