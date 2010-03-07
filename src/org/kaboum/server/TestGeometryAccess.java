/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kaboum.server;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.text.cql2.CQL;
import org.json.JSONObject;
import org.kaboum.util.KaboumFeatureModes;
import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

/**
 *Overloaded GeometryAccess used to test custom object management
 * @author nicolas
 */
public class TestGeometryAccess extends KaboumGeometryAccess {

    private Logger logger;

    public TestGeometryAccess(Properties kaboumProps, Object userData) {
        super(kaboumProps, userData);
        logger = Logger.getLogger(this.getClass().getName());
    }

    public Hashtable updateGeometries(Hashtable geometries) {
        Hashtable hash = super.updateGeometries(geometries);

        //short errorCode = processGeometries(geometries);
        return hash;
    }

    public Hashtable addGeometries(Hashtable geometries) {
        //Vector vec = (Vector)geometries.elements().nextElement();
        //Geometry geom = (Geometry)vec.get(0);
        //System.out.println("userdata before adding: " + ((UserData)geom.getUserData()).kaboumUserData);
        Hashtable hash = super.addGeometries(geometries);
        //vec = (Vector)hash.elements().nextElement();
        //geom = (Geometry)vec.get(0);
        //System.out.println("userdata after adding: " + ((UserData)geom.getUserData()).kaboumUserData);

        //short errorCode = processGeometries(geometries);
        return hash;
    }

    public Hashtable getGeometries(Envelope mapExtent) {
        //Vector vec = (Vector)geometries.elements().nextElement();
        //Geometry geom = (Geometry)vec.get(0);
        //System.out.println("userdata before adding: " + ((UserData)geom.getUserData()).kaboumUserData);
        Hashtable hash = super.getGeometries(mapExtent);
        //vec = (Vector)hash.elements().nextElement();
        //geom = (Geometry)vec.get(0);
        //System.out.println("userdata after adding: " + ((UserData)geom.getUserData()).kaboumUserData);
        //getXYGeom(hash);
        return hash;
    }

    /**
     * 
     * @param geometries
     * @return true
    public boolean getXYGeom(Hashtable geometries) {
        if (geometries == null) {
            logger.severe("null geometries !");
            return false;
        }
        long t0 = System.currentTimeMillis();

        HttpServletRequest request = (HttpServletRequest) this.getUserData();
        // hashtable whose index is the clicked point as a WKT string. Do not retrieve object if
        // it was already got.
        //Hashtable<String, Vector<Geometry>> virtualFeatures = 
          //      (Hashtable<String, Vector<Geometry>>)request.getSession().getAttribute("MAPAE_VIRTUAL_FEATURES");
        Hashtable virtualFeatures = 
                (Hashtable)request.getSession().getAttribute("MAPAE_VIRTUAL_FEATURES");
        if (virtualFeatures == null) {
            logger.info("no virtual features hash found in session, building one");
            virtualFeatures = new Hashtable();
        }
        
        JSONObject jsonObj = null;
        String cl = null;
        DataStore ds = null;
        String layerName = null;
        String sourceTableName = null;
        String sourceDSParams = null;
        Vector vec = null;
        try {
            if (request == null || request.getParameter("KABOUM_USER_METADATA") == null) {
                logger.warning("null userdata in request");
            } else {
                String userMD = request.getParameter("KABOUM_USER_METADATA");
                if (userMD == null || userMD.length() == 0 || userMD.equals("dummy")) {
                    logger.info("no valid user data found");
                    return true;
                }
                logger.info("user md: " + userMD);
                jsonObj = new JSONObject(userMD);
                String action = jsonObj.getString("action");
                if ("buffer".equalsIgnoreCase(action)) {
                    return false;
                }
                layerName = jsonObj.getString("targetClass");
                
                if ("delete".equalsIgnoreCase(action)) {
                    // removes all virtual features
                    virtualFeatures = new Hashtable();
                } else if ("get".equalsIgnoreCase(action)) {
                    String point = jsonObj.getString("xy");
                    sourceTableName = jsonObj.getString("sourceClass");
                    sourceDSParams = jsonObj.getString("sourceDatasourceParams");

                    if (virtualFeatures.containsKey(point)) {
                        logger.info("Feature(s) already got for point: " + point);
                    } else {
                        // process our object
                        if (!geometries.containsKey(layerName)) {
                            logger.warning("no class to process in geometries hash...");
                            return false;
                        }

                        ds = getDataStore(sourceDSParams);
                        logger.info("getting datastore for params: " + sourceDSParams + " ( " + sourceTableName + ")");

                        FeatureReader reader = null;
                        Feature f = null;
                        Query query = null;
                        String geoCol = ds.getSchema(sourceTableName).getDefaultGeometry().getLocalName();
                        //Filter result = CQL.toFilter("DISTANCE(" + geoCol + ", " + point + ") = 0");
                        Filter result = CQL.toFilter("CONTAINS(" + geoCol + ", " + point + ")");
                        query = new DefaultQuery(
                                sourceTableName,
                                result,
                                Query.DEFAULT_MAX,
                                null,
                                sourceTableName);
                        reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);

                        vec = new Vector(10);
                        int i = 0;

                        while (reader.hasNext()) {
                            f = (Feature) reader.next();
                            Geometry geom = f.getDefaultGeometry();
                            if (geom == null) {
                                logger.info("null geom in featureReader. Feature is: " + f.getID());
                                continue;
                            }
                            UserData ud = new UserData(f.getID(), "");

                            geom.setUserData(ud);
                            vec.add(geom);
                        }
                        reader.close();
                        // stores features for this query
                        logger.info("adding a vector of geoms (layer: " + sourceTableName + ") for point: " + point + " (num feat: " + vec.size() + ")");
                        virtualFeatures.put(point, vec);
                        logger.info("num vf vectors: " + virtualFeatures.size());
                    }
                }
                // now data are got, restore hash in session and build vector of geometries
                request.getSession().setAttribute("MAPAE_VIRTUAL_FEATURES", virtualFeatures);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        // extract a vector of all features to pass to the client
        geometries.put(layerName, null);
        return true;
    }
     */

    /**
     * processes the given collection of features.<br/>
     * Does nothing in this base implementation. 
     * Overload this class to provide a custom mechanism
     *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the 
     * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
     *@return a code from KaboumFeatureModes if an error occured, or K_REMOVE_FEATURE if all was ok
    public short processGeometries(Hashtable geometries) {
        if (geometries == null) {
            logger.severe("null geometries !");
            return KaboumFeatureModes.K_NULL_GEOMETRIES;
        }
        long t0 = System.currentTimeMillis();

        Vector vec = null;
        String cl = null;
        if (!geometries.containsKey("parc_ref_def")) {
            logger.warning("no class to process...");
            return 0;
        }
        DataStore ds = null;
        String layerName = "parc_ref_def";
        String layerParams = "dbtype=postgis,host=localhost,port=5432,database=test,user=postgres,passwd=postgres";
        String tableName = null;
        JSONObject jsonObj = null;
        try {
            HttpServletRequest request = (HttpServletRequest) this.getUserData();
            if (request == null || request.getParameter("KABOUM_USER_METADATA") == null) {
                logger.warning("null userdata in request");
            } else {
                String userMD = request.getParameter("KABOUM_USER_METADATA");
                jsonObj = new JSONObject(userMD);
                if (jsonObj.getString("action") != null) {
                }
                // process our layer
                vec = (Vector) geometries.get(layerName);
                ds = getDataStore(layerParams);
                tableName = kaboumServerProperties.getProperty(layerName + "_DATASTORE_NAME");

                Geometry geom = (Geometry) vec.get(0);
                FeatureType featureType = ds.getSchema(tableName);
                FeatureStore featureStore = (FeatureStore) (ds.getFeatureSource(tableName));
                // updates all features with the same userData field
                FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
                Filter fidFilter = ff.id(Collections.singleton(ff.featureId(((UserData) geom.getUserData()).id)));

                featureStore.modifyFeatures(featureType.getAttributeType("width"),
                        Float.valueOf(jsonObj.getString("width")),
                        fidFilter);
                logger.info("attributes for objects of layer: " + layerName + " treated.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorCode;
    }
     */
}
