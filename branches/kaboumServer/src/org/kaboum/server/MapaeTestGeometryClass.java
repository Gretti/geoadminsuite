package org.kaboum.server;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.json.JSONObject;
import org.kaboum.server.MapaeTestGeometryClass;
import org.kaboum.util.KaboumFeatureModes;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

/**
 *Overloaded GeometryAccess used to test custom object management
 * @author nicolas
 */
public class MapaeTestGeometryClass extends KaboumGeometryAccess {

    private static Logger logger = Logger.getLogger(MapaeTestGeometryClass.class);

    public MapaeTestGeometryClass(Properties kaboumProps, Object userData) {
        super(kaboumProps, userData);
    }

    public Hashtable updateGeometries(Hashtable geometries) {
        Hashtable hash = super.updateGeometries(geometries);

        short errorCode = processGeometries(geometries);
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

        short errorCode = processGeometries(geometries);
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
        getXYGeom(hash);
        return hash;
    }

    /**
     * 
     * @param geometries
     * @return true if all is ok
     */
    public boolean getXYGeom(Hashtable geometries) {
        if (geometries == null) {
            logger.error("null geometries !");
            return false;
        }
        return true;
    }

    /**
     * processes the given collection of features.<br/>
     * Does nothing in this base implementation. 
     * Overload this class to provide a custom mechanism
     *@param geometries - a Hashtable whose key is the name of the Kaboum geometry class and the 
     * value is a vector of JTS Geometry with the userData field set to a KaboumUserData object.
     *@return a code from KaboumFeatureModes if an error occured, or K_REMOVE_FEATURE if all was ok
     */
    public short processGeometries(Hashtable geometries) {
        if (geometries == null) {
            logger.error("null geometries !");
            return KaboumFeatureModes.K_NULL_GEOMETRIES;
        }
        long t0 = System.currentTimeMillis();

        Vector vec = null;
        String cl = null;
        if (!geometries.containsKey("parc_ref_def")) {
            logger.warn("no class to process...");
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
                logger.warn("null userdata in request");
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

//                featureStore.modifyFeatures(featureType.getAttributeType("width"),
//                        Float.valueOf(jsonObj.getString("width")),
//                        fidFilter);
                logger.info("attributes for objects of layer: " + layerName + " treated.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorCode;
    }
}
