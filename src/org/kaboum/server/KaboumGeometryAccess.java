/*
 *
 * KaboumGeometryAccess.java
 *
 * Created on 26 aout 2005, 16:23
 */
package org.kaboum.server;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.kaboum.server.utils.DataStoreManager;
import org.kaboum.server.utils.DataStoreProperties;
import org.kaboum.server.utils.KaboumJTSFactory;
import org.kaboum.util.KaboumFeatureModes;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

/**
 * Kaboum Server implementation of the GeometryAccess interface.
 * <p>
 * This class will use the underlying GeoTools API 2.6 to access physical data storage
 * </p>
 *
 *TODO update comments to precise Datastore stuff
 * @author Nicolas Ribot
 */
public class KaboumGeometryAccess extends AbstractGeometryAccess {

        /**
         * This class' logger (JDK logging mechanism)
         */
        private static Logger logger = Logger.getLogger(KaboumGeometryAccess.class);

        /**
         * Gets the error code, one of those defined in <CODE>org.kaboum.kaboumUtils.KaboumFeatureModes </CODE>class
         * @return The errorCode
         */
        @Override
        public short getErrorCode() {
                return errorCode;
        }

        /**
         * Creates a new instance of KaboumGeometryAccess by calling parent ctor and
         * setting up the Logger.
         * <p>
         * Currently, this userData object is the <CODE>HttpServletRequest</CODE> received by the
         * KaboumFeatureServlet servlet from the Kaboum Applet
         * </p>
         * @param kaboumProps The KaboumServer properties object (read from properties file)
         * @param userData The context user data. Currently, the <CODE>HttpServletRequest</CODE> object
         */
        public KaboumGeometryAccess(Properties kaboumProps, Object userData) {
                super(kaboumProps, userData);
        }

        /**
         * Gets all the geometries for layers configured in the Kaboum Server Properties file
         * laying in the given spatial extension.
         *
         * <p>
         * In case of error, the error code is set.
         * </p>
         * @return a Hashtable whose key is the name of the Kaboum geometry class and the
         * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
         * @param mapExtent The spatial client map extention to get feature from.
         */
        @Override
        public Hashtable getGeometries(Envelope mapExtent/*, int scale*/) {
                if (mapExtent == null) {
                        // should never happen, as caller have to test the validity of the map extent
                        logger.error("Cannot process getGeometries: null input map extent");
                        errorCode = KaboumFeatureModes.K_MISSING_MAP_EXTENT;
                        return null;
                }
                logger.info("Getting geometries for envelope: " + mapExtent.toString());
                // variables used in this class
                Hashtable geometries = new Hashtable();
                DataStore ds = null;
                String datastoreName = null;
                // the geometric column name, guessed from the DataStore
                String geoCol = null;
                // the tooltip Expression
                String toolTipExpression = null;
                // the list of attributes defined in the toolTip expression, that will be used
                // to build a valid tooltip for each feature
                String[] attributeList = null;
                String kaboumClass = null;
                FeatureReader reader = null;
                Vector vec = null;
                SimpleFeature f = null;
                //FilterFactory2 ff = null;
                Expression bb = null;
                Filter bboxFilter = null;
                // the maximum number of features to retrieve.
                // above this number, an error message is reached
                int maxFeatures = -1;

                // stuff for spatial filtering, based on client mapext
        /*
                try {
                //logger.info("filter set for envelope: " + mapExtent.toString());
                ff = CommonFactoryFinder.getFilterFactory(null)
                bb = ff.createBBoxExpression( mapExtent );
                bboxFilter = ff.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
                } catch (IllegalFilterException ife) {
                ife.printStackTrace();
                return null;
                }
                 * */
                Query query = null;
                boolean emptyClass = false;
                //retrieve the list of kaboum feature classes defined in the properties file, to be able
                // to get a valid datastore object for each of them.
                String classList = kaboumServerProperties.getProperty("DD_CLASS_LIST");
                StringTokenizer tok = new StringTokenizer(classList, ",");
                // check if there was only one entry in the DD_CLASS_LIST
                if (classList != null && classList.length() > 0 && tok.countTokens() == 0) {
                        classList += ",";
                }
                try {
                        while (tok.hasMoreTokens()) {
                                kaboumClass = tok.nextToken();
                                // From now (20/06/2008), skips classes that should be processed only, without
                                // loading their geometries from underlying datastore
                                String dsParams = kaboumServerProperties.getProperty(kaboumClass + "_DATASTORE_PARAMS");
                                if (dsParams.length() > 0) {
                                        /*
                                        // looks if layer's minscale and maxscale parameters are set, and if
                                        // data should be loaded, according to the current client scale
                                        int minscale = Integer.MIN_VALUE;
                                        int maxscale = Integer.MIN_VALUE;

                                        try {
                                        minscale = Integer.parseInt(kaboumServerProperties.getProperty(kaboumClass + "_MINSCALE"));
                                        } catch (NumberFormatException nfe) {}
                                        try {
                                        maxscale = Integer.parseInt(kaboumServerProperties.getProperty(kaboumClass + "_MAXSCALE"));
                                        } catch (NumberFormatException nfe) {}

                                        if (minscale != Integer.MIN_VALUE && minscale < scale) {
                                        logger.info("layer minscale < current scale, skipping features");
                                        continue;
                                        }
                                        if (maxscale != Integer.MIN_VALUE && maxscale < scale) {
                                        logger.info("layer maxscale < current scale, skipping features");
                                        continue;
                                        }

                                         */
                                        ds = getDataStore(dsParams);

                                        if (ds == null) {
                                                logger.error("Cannot get a valid datastore for class: " + kaboumClass);
                                                errorCode = KaboumFeatureModes.K_INVALID_DATASTORE_PROPERTIES;
                                                continue;
                                        }
                                        // gets some needed parameters from Kaboum Server properties file
                                        datastoreName = kaboumServerProperties.getProperty(kaboumClass + "_DATASTORE_NAME");
                                        maxFeatures = Integer.parseInt(kaboumServerProperties.getProperty(kaboumClass + "_MAX_FEATURES").trim());

                                        try {
                                                geoCol = ds.getSchema(datastoreName).getGeometryDescriptor().getLocalName();
                                        } catch (IOException ioe) {
                                                // two cases:
                                                // 1u) a datastore can be retrieved, but access to dataStore is denied:
                                                // probably a user right. No need to go on
                                                // orscript
                                                // 2u) DataStore properties were changed on the fly in the properties file
                                                // and these new properties are not correct
                                                if (ioe instanceof org.geotools.data.SchemaNotFoundException) {
                                                        logger.error("Cannot find a suitable schema for given datastore properties",
                                                                ioe);
                                                        errorCode = KaboumFeatureModes.K_INVALID_DATASTORE_PROPERTIES;
                                                } else {
                                                        logger.error("Cannot access datastore: ", ioe);
                                                        errorCode = KaboumFeatureModes.K_DATASTORE_ACCESS_DENIED;
                                                }
                                                logger.error("Invalid properties for kaboumClass: "
                                                        + kaboumClass
                                                        + " and datastoreName: "
                                                        + datastoreName
                                                        + ". Message: "
                                                        + ioe.getMessage());
                                                //ioe.printStackTrace();
                                                break;
                                        }
                                        // the tooltip Expression
                                        toolTipExpression = kaboumServerProperties.getProperty(kaboumClass + "_TOOLTIP_EXPRESSION", null);
                                        // the list of attributes defined in the toolTip expression, that will be used
                                        // to build a valid tooltip for each feature
                                        attributeList = DataStoreProperties.getAttributesList(toolTipExpression);

                                        //((GeometryFilter)bboxFilter).addRightGeometry(bb);
                                        //((GeometryFilter)bboxFilter).addLeftGeometry(ff.createAttributeExpression(ds.getSchema(datastoreName),geoCol));
                                        String bboxSt = "" + mapExtent.getMinX() + "," + mapExtent.getMinY() + ","
                                                + mapExtent.getMaxX() + "," + mapExtent.getMaxY();
                                        bboxFilter = CQL.toFilter("BBOX(" + geoCol + "," + bboxSt + ")");
                                        // this query ask to retrieve all features, with no filtering based on attributes names
                                        // the handle name is set to be same as the tablename, as this handle is not used in Kaboum Server
                                        query = new DefaultQuery(
                                                datastoreName,
                                                bboxFilter,
                                                Query.DEFAULT_MAX,
                                                null,
                                                datastoreName);

                                        reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);

                                        // checks geometric feature type
                                        //System.out.println("geom feature type: " + reader.getFeatureType().getDefaultGeometry().getType());

                                        vec = new Vector(10);
                                        int i = 0;
                                        int featureCount = 0;
                                        while (reader.hasNext()) {
                                                featureCount++;
                                                if (maxFeatures >= 0 && i++ > maxFeatures) {
                                                        // should stop loading as maximun features is reached
                                                        errorCode = KaboumFeatureModes.K_MAX_FEATURES_REACHED;
                                                        vec.removeAllElements();
                                                        logger.info("Maximum number of features reached "
                                                                + "(set to " + maxFeatures + " in kaboumServer.properties, emptying vector...");
                                                        break;
                                                }
                                                f = (SimpleFeature) reader.next();

                                                Geometry geom = (Geometry) f.getDefaultGeometry();

                                                if (geom == null) {
                                                        logger.info("null geom in featureReader. Feature is: " + f.getID());
                                                        continue;
                                                }

                                                UserData ud = new UserData(
                                                        f.getID(),
                                                        DataStoreProperties.getActualToolTip(toolTipExpression, f, attributeList));

                                                geom.setUserData(ud);
                                                vec.add(geom);
                                        }
                                        reader.close();
                                        logger.debug("Got " + featureCount + " features for class: " + kaboumClass + " in extent: "
                                                + "setsrid('BOX(" + mapExtent.getMinX() + " " + mapExtent.getMinY()
                                                + ", " + mapExtent.getMaxX() + " " + mapExtent.getMaxY() + ")'::box2d, 4326)");
                                } else {
                                        // a geometry CLASS without datasource params: add an empty vector into hash to allow this class
                                        // to be used anyway by business logic code or client-side code
                                        logger.info("Class: " + kaboumClass + " has no datasource parameters defined for it."
                                                + " \nThis layer is still available in kaboum if added to the DD_CLASS_LIST (kaboumServer.properties)");
                                        vec = new Vector();
                                }

                                geometries.put(kaboumClass, vec);
                        }
                } catch (Exception e) {
                        logger.error("Unknown error", e);
                        e.printStackTrace();
                        errorCode = KaboumFeatureModes.K_UNAVAILABLE_DATASTORE;
                        return null;
                } finally {
                        try {
                                //logger.info("closing the reader, asking ConnectionPool to close all...");
                                if (reader != null) {
                                        reader.close();
                                }
                                // Closes all DB connections even if it is not a JDBCDataStore,
                                //ConnectionPoolManager.getInstance().closeAll();
                        } catch (IOException ioe) {
                                ioe.printStackTrace();
                        }
                }

                // try to change KaboumServer properties on the fly, to test properties overloading mechanism
//        if (kaboumServerProperties != null) {
//            kaboumServerProperties.setProperty("depts_DD_FILL_COLOR", "pink");
//        }

                return geometries;
        }

        /**
         * adds the given collection of features to the underlying physical storage
         *
         * <p>
         * in case of error, implementors must set the error code in order for getErrorCode
         * to return a pertinent code for the caller
         * </p>
         *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the
         * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
         *@return a Hashtable containing added geomtries with their identifiers updated.
         */
        @Override
        public Hashtable addGeometries(Hashtable geometries) {
                if (geometries == null) {
                        logger.error("null geometries !");
                        return null;
                }
                logger.info("Number of geometries to add: " + geometries.size());
                long t0 = System.currentTimeMillis();

                Hashtable resGeoms = new Hashtable();
                DataStore ds = null;
                String tableName = null;
                SimpleFeature f = null;
                SimpleFeatureType featureType = null;
                FeatureCollection fc = null;
                // the collection of generated ids after an add operation
                List ids = null;
                Vector vec = null;
                // validate all geometries contained in the given hashtable
                Enumeration keys = geometries.keys();
                StringBuffer kaboumClassNames = new StringBuffer();

                try {
                        while (keys.hasMoreElements()) {
                                String cl = (String) keys.nextElement();
                                kaboumClassNames.append(cl).append(", ");
                                String dsParams = kaboumServerProperties.getProperty(cl + "_DATASTORE_PARAMS");
                                if (dsParams.length() == 0) {
                                        logger.info("Skipping this class, no datastore params defined for it");
                                        vec = new Vector();
                                } else {
                                        vec = (Vector) geometries.get(cl);
                                        tableName = kaboumServerProperties.getProperty(cl + "_DATASTORE_NAME");

                                        ds = getDataStore(dsParams);

                                        if (ds == null) {
                                                logger.error("cannot get a valid datastore");
                                                errorCode = KaboumFeatureModes.K_INVALID_DATASTORE_PROPERTIES;
                                                return null;
                                        }

                                        featureType = ds.getSchema(tableName);
                                        FeatureStore featureStore = (FeatureStore) (ds.getFeatureSource(tableName));
                                        // gets the array of GeoTools Features to validate
                                        //adds all the features and gets the generated ids back
                                        try {
                                                fc = KaboumJTSFactory.getFeatureCollection(vec, featureType);
                                                ids = featureStore.addFeatures(fc);
                                        } catch (IllegalAttributeException iae) {
                                                // probably an incoherent geometric type
                                                errorCode = KaboumFeatureModes.K_INCONSISTENT_GEOM_TYPE;
                                                logger.error("IllegalAttributeException: " + iae.getMessage()
                                                        + ". returning code KaboumFeatureModes.K_INCONSISTENT_GEOM_TYPE to client...");
                                                //iae.printStackTrace();
                                                break;
                                        } catch (Exception e) {
                                                // the datasource is no more available or read only: cannot add feature.
                                                // no need to continue: all add operation will fail
                                                if (e.getMessage() != null && e.getMessage().indexOf("denied") >= 0) {
                                                        logger.error("Read only datastore: " + e.getMessage(), e);
                                                        errorCode = KaboumFeatureModes.K_READ_ONLY_DATASTORE;
                                                        break;
                                                } else {
                                                        logger.error("Unavailable datastore: " + e.getMessage(), e);
                                                        errorCode = KaboumFeatureModes.K_UNAVAILABLE_DATASTORE;
                                                }
                                                //e.printStackTrace();
                                        }
                                        // Just set the new IDS into existing geometries vector, as geometry
                                        // shape is the same after the validation
                                        try {
                                                if (!KaboumJTSFactory.setObjectIDS(vec, ids, tableName)) {
                                                        logger.warn("Unable to set objects identifiers for table name: " + tableName);
                                                        errorCode = KaboumFeatureModes.K_NO_GENERATED_ID;
                                                }
                                        } catch (Exception e) {
                                                // invalid parameters or parameter size
                                                logger.error("Setting object identifiers throw exception", e);
                                                errorCode = KaboumFeatureModes.K_ADD_FEATURES_FAILED;
                                                e.printStackTrace();
                                        }
                                }
                                // stores added geometries in the table
                                resGeoms.put(cl, vec);

                        }
                } catch (Exception e) {
                        logger.error("Unavailable datastore.", e);
                        e.printStackTrace();
                        errorCode = KaboumFeatureModes.K_UNAVAILABLE_DATASTORE;
                        return null;
                } finally {
                        // Fixme: some cleanup to do ?
                }

                long t1 = System.currentTimeMillis();
                logger.info("Time to addGeometries: " + (t1 - t0) + " ms. For classes: " + kaboumClassNames.toString());

                return resGeoms;
        }

        /**
         * Removes the given collection of features from the underlying physical storage
         *
         * <p>
         * in case of error, implementors must set the error code in order for getErrorCode
         * to return a pertinent code for the caller
         * </p>
         *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the
         * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
         *@return a code from KaboumFeatureModes if an error occured, or K_REMOVE_FEATURE if all was ok
         */
        @Override
        public short removeGeometries(Hashtable geometries) {
                if (geometries == null) {
                        logger.error("null geometries !");
                        return KaboumFeatureModes.K_NULL_GEOMETRIES;
                }
                logger.info("will remove: " + geometries.size() + " geometries");
                long t0 = System.currentTimeMillis();

                DataStore ds = null;
                String tableName = null;
                // the geometric column name, guessed from the DataStore
                String cl = null;
                FeatureStore featureStore = null;
                Vector vec = null;
                Feature f = null;
                FeatureType featureType = null;
                FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
                Filter fidFilter = null;

                // Remove all geometries contained in the given hashtable
                Enumeration keys = geometries.keys();

                try {
                        while (keys.hasMoreElements()) {
                                cl = (String) keys.nextElement();
                                logger.info("treating class: " + cl);
                                String dsParams = kaboumServerProperties.getProperty(cl + "_DATASTORE_PARAMS");
                                if (dsParams.length() > 0) {
                                        vec = (Vector) geometries.get(cl);
                                        tableName = kaboumServerProperties.getProperty(cl + "_DATASTORE_NAME");

                                        ds = getDataStore(dsParams);

                                        if (ds == null) {
                                                logger.error("Cannot get a valid datastore");
                                                return KaboumFeatureModes.K_INVALID_DATASTORE_PROPERTIES;
                                        }

                                        // now gets the featureType
                                        featureType = ds.getSchema(tableName);

                                        // gets some needed (?) parameters from Kaboum Server properties file
                                        featureStore = (FeatureStore) (ds.getFeatureSource(tableName));

                                        for (Iterator iter = vec.iterator(); iter.hasNext();) {
                                                Geometry geom = (Geometry) iter.next();

                                                // tests if given JTS geometry is valid: it must contain a org.kaboum.server.UserData Object
                                                if (geom.getUserData() == null
                                                        || !(geom.getUserData() instanceof org.kaboum.server.UserData)
                                                        || ((org.kaboum.server.UserData) geom.getUserData()).id == null) {
                                                        logger.error("Invalid JTS geometry: no UserData field or missing id.");
                                                        // do not stop the process
                                                        errorCode = KaboumFeatureModes.K_INVALID_OBJECT;
                                                        continue;
                                                }
                                                logger.debug("Building a filter for geometry id: " + ((UserData) geom.getUserData()).id);

                                                // builds a filter based on the feature's FID
                                                //fidFilter = FilterFactory.createFilterFactory().createFidFilter(((UserData)geom.getUserData()).id);
                                                // replaced by new CQL Filter syntax
                                                fidFilter = ff.id(Collections.singleton(ff.featureId(((UserData) geom.getUserData()).id)));
                                                try {
                                                        featureStore.removeFeatures(fidFilter);
                                                } catch (Exception e) {
                                                        logger.error("Cannot remove geometry: "
                                                                + ((UserData) geom.getUserData()).id + ": "
                                                                + e.getMessage(), e);
                                                        errorCode = KaboumFeatureModes.K_REMOVE_FEATURES_FAILED;
                                                        break;
                                                }
                                        }
                                } else {
                                        logger.info("Skipping this class ("
                                                + cl + "), no datastore params defined for it");
                                }
                        }
                } catch (Exception e) {
                        logger.error("Removal of features failed.", e);
                        return KaboumFeatureModes.K_REMOVE_FEATURES_FAILED;
                } finally {
                        //fixme: some cleanup ?
                }

                long t1 = System.currentTimeMillis();
                logger.info("Time to processRemoveFeature: " + (t1 - t0) + " ms.");

                return errorCode;
        }

        /**
         * updates the given collection of features in the underlying physical storage
         *
         * <p>
         * in case of error, implementors must set the error code in order for getErrorCode
         * to return a pertinent code for the caller
         * </p>
         *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the
         * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
         *@return a Hashtable containing added geomtries' with their identifiers unchanged, in order
         * for the caller to reinsert given geometries.
         */
        @Override
        public Hashtable updateGeometries(Hashtable geometries) {
                if (geometries == null) {
                        logger.error("null geometries !");
                        return null;
                }
                logger.info("Updating: " + geometries.size() + " geometries");
                long t0 = System.currentTimeMillis();

                Hashtable resGeoms = new Hashtable();
                DataStore ds = null;
                String tableName = null;
                String cl = null;
                FeatureStore featureStore = null;
                Vector vec = null;
                Feature f = null;
                FeatureType featureType = null;
                Filter fidFilter = null;
                FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());

                // validate all geometries contained in the given hashtable
                Enumeration keys = geometries.keys();

                try {
                        while (keys.hasMoreElements()) {
                                cl = (String) keys.nextElement();
                                logger.debug("Treating class: " + cl);
                                String dsParams = kaboumServerProperties.getProperty(cl + "_DATASTORE_PARAMS");
                                if (dsParams.length() == 0) {
                                        logger.debug("Skipping this class, no datastore params defined for it");
                                        vec = new Vector();
                                } else {
                                        vec = (Vector) geometries.get(cl);
                                        tableName = kaboumServerProperties.getProperty(cl + "_DATASTORE_NAME");

                                        ds = getDataStore(dsParams);

                                        if (ds == null) {
                                                logger.error("Cannot get a valid datastore");
                                                errorCode = KaboumFeatureModes.K_INVALID_DATASTORE_PROPERTIES;
                                                return null;
                                        }
                                        // now gets the featureType
                                        featureType = ds.getSchema(tableName);

                                        // gets some needed (?) parameters from Kaboum Server properties file
                                        featureStore = (FeatureStore) (ds.getFeatureSource(tableName));

                                        // first process common operations for update and remove
                                        for (Iterator iter = vec.iterator(); iter.hasNext();) {
                                                Geometry geom = (Geometry) iter.next();
                                                logger.debug("Processing updateFeature for geom type: " + geom.getGeometryType());

                                                // tests if given JTS geometry is valid: it must contain a org.kaboum.server.UserData Object
                                                if (geom.getUserData() == null
                                                        || !(geom.getUserData() instanceof org.kaboum.server.UserData)
                                                        || ((org.kaboum.server.UserData) geom.getUserData()).id == null) {
                                                        logger.error("Invalid JTS geometry: no UserData field or missing id.");
                                                        // do not stop the process
                                                        errorCode = KaboumFeatureModes.K_INVALID_OBJECT;
                                                        continue;
                                                }

                                                // builds a filter based on the feature's FID
                                                logger.debug("Building a filter for geom id: " + ((UserData) geom.getUserData()).id);
                                                fidFilter = ff.id(Collections.singleton(ff.featureId(((UserData) geom.getUserData()).id)));
                                                try {
                                                        featureStore.modifyFeatures(
                                                                featureType.getGeometryDescriptor(),
                                                                geom,
                                                                fidFilter);
                                                } catch (DataSourceException dse) {
                                                        logger.error("Cannot update geometry: "
                                                                + ((UserData) geom.getUserData()).id + ": "
                                                                + dse.getMessage(), dse);
                                                        if (dse.getMessage().indexOf("violate") != -1 ||
                                                                dse.getMessage().indexOf("violation") != -1) {
                                                                errorCode = KaboumFeatureModes.K_INCONSISTENT_GEOM_TYPE;
                                                        } else {
                                                                errorCode = KaboumFeatureModes.K_DATASTORE_ACCESS_DENIED;
                                                        }
                                                        break;
                                                } catch (Exception e) {
                                                        logger.error("cannot update geometry: "
                                                                + ((UserData) geom.getUserData()).id + ": "
                                                                + e.getMessage(), e);
                                                        errorCode = KaboumFeatureModes.K_DATASTORE_ACCESS_DENIED;
                                                        break;
                                                }
                                        }
                                }
                                // stores updated geometries in the table
                                resGeoms.put(cl, vec);
                        }
                } catch (Exception e) {
                        logger.error("Updating geometries failed.", e);
                        e.printStackTrace();
                        // should catch illegalargument and manage error codes more precisely
                        errorCode = KaboumFeatureModes.K_UPDATE_FEATURES_FAILED;
                        return null;
                } finally {
                        // fixme: some cleanup
                }

                long t1 = System.currentTimeMillis();
                logger.info("time to updateGeometries: " + (t1 - t0));
                return resGeoms;
        }

        /**
         * Gets a reference to the current DataStore object used by kaboum Server
         * to retrieve Features.
         * <p>
         * DataStore will be built based on given datastore parameters,
         * (typically got from kaboumServer.properties) under the <className>__DATASTORE_PARAMS key.
         * </p>
         *
         *@param datastoreParams The datastore parameters string
         *@return the current DataStore object reference, or null if it cannot be retrieved
         *@throws IllegalArgumentException if one properties file DataStore-related value is not correct
         * (for instance, a port that cannot be converted into an int, or an invalid shapefile path)
         */
        public DataStore getDataStore(String datastoreParams) throws IllegalArgumentException {
                if (kaboumServerProperties == null) {
                        logger.error("Null kaboum properties...");
                        return null;
                }
                logger.info("Getting datastore for parameters: " + datastoreParams);
                // then uses properties file parameters and DataStoreFinder to get the DataStore
                DataStore ds = null;
                // the HashMap containing DataStore parameters
                HashMap dsParamsMap = null;

                // loads Datastore parameters from Kaboum Server properties file
                dsParamsMap = DataStoreProperties.getDataStoreParameters(datastoreParams);
                if (dsParamsMap == null) {
                        logger.error("Null datasource parameters. "
                                + "Check kaboumServer.properties file to declare "
                                + "a valid datasource for active GeometryClass");
                        return null;
                }
                // try to find an existing datastore
                ds = DataStoreManager.getInstance().getDataStore(datastoreParams);

                if (ds == null) {
                        try {
                                ds = DataStoreFinder.getDataStore(dsParamsMap);
                        } catch (Exception e) {
                                logger.error("Datastore cannot be retrieved: " + e.getMessage());
                                e.printStackTrace();
                                return null;
                        }
                        if (ds == null) {
                                logger.debug("null datastore, trying to find manually...");
                                ds = DataStoreManager.getInstance().getDataStoreManually(dsParamsMap);
                                if (ds == null) {
                                        logger.debug("Cannot manually find a datastore for given parameters.");
                                }
                        }
                        // stores this new datastore
                        if (ds != null) {
                                DataStoreManager.getInstance().setDataStore(datastoreParams, ds);
                        }
                }
                return ds;
        }
}
