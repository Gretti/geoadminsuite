
import com.vividsolutions.jts.geom.Geometry;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.spatialite.SpatiaLiteDataStoreFactory;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.kaboum.server.utils.DataStoreManager;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nicolas
 */
public class KaboumServerTest {

        private void testFactory() throws Exception {
                Hints hints = GeoTools.getDefaultHints();
                FactoryRegistry registry = new FactoryCreator(new Class[]{FilterFactory.class,});
                Iterator i = registry.getServiceProviders(FilterFactory.class, null, hints);
                while (i.hasNext()) {
                        FilterFactory factory = (FilterFactory) i.next();
                        System.out.println("found factory: " + factory.toString());
                }
        }

        private DataStore getDatastore(Map<String, Serializable> params) throws Exception {
                System.out.println("entering testDatastore");
                for (Iterator<DataStoreFactorySpi> iter = DataStoreFinder.getAvailableDataStores();
                        iter.hasNext();) {
                        DataStoreFactorySpi ds = iter.next();
                        System.out.println("ds factory found: "
                                + ds.getDisplayName() + " desc: "
                                + ds.getDescription());
                }
                DataStore ds = null;
                try {
                        ds = DataStoreFinder.getDataStore(params);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                if (ds == null) {
                        System.out.println("null datastore, trying to find manually...");
                        ds = DataStoreManager.getInstance().getDataStoreManually(params);
                        if (ds == null) {
                                System.out.println("cannot manually find a datastore for given parameters.");
                        }
                }
                System.out.println("leaving testDatastore");
                return ds;
        }

        private void testSpatial() throws Exception {
                // TODO code application logic here
                System.out.println("Welcome to GeoTools:" + GeoTools.getVersion());

                String javaLibPath = "/usr/local/lib";
                System.setProperty("java.library.path", javaLibPath);
                System.out.println("lib path: " + System.getProperty("java.library.path"));

                Map<String, Serializable> connect = new HashMap<String, Serializable>();
                connect.put("type", "javax.sql.DataSource");
                //connect.put("driver", "SQLite.JDBCDriver");
                //connect.put("url", "jdbc:sqlite:/Users/nicolas/code/SQL/SQLite_databases/spatial.sqlite");
                connect.put("database", "/Users/nicolas/code/SQL/SQLite_databases/spatial.sqlite");

                System.out.println("user.dir" + System.getProperty("user.dir"));

                DataStore spatialDs = getDatastore(connect);

                // with finder
                //DataStore spatialDs = DataStoreFinder.getDataStore(connect);
                String[] typeNames = spatialDs.getTypeNames();


                for (String s : typeNames) {
                        System.out.println("tables: " + s);


                }
                String typeName = spatialDs.getTypeNames()[0];
                FeatureSource<SimpleFeatureType, SimpleFeature> geomFs = spatialDs.getFeatureSource("dep_france_dom");
                FeatureCollection<SimpleFeatureType, SimpleFeature> featCol = geomFs.getFeatures();



                long start = System.currentTimeMillis();


                int i = 0;


                for (Iterator<SimpleFeature> iter = featCol.iterator(); iter.hasNext(); i++) {
                        SimpleFeature feature = iter.next();
                        GeometryAttribute geomAttribute = feature.getDefaultGeometryProperty();
                        //System.out.println("attrib: " + geomAttribute.getDescriptor().getType().getName());
                        //System.out.println("Feature: " + feature.getID() + " " + feature.getFeatureType().getTypeName());
                        Geometry geometry = (Geometry) feature.getDefaultGeometry();


                        if (geometry == null) {
                                System.out.println("null geom");


                        } else {
                                Geometry centroid = geometry.getCentroid();


                        }
                }
                String time = "" + (System.currentTimeMillis() - start) / 1000.0 + "s.";
                System.out.println(i + " features and their centroids read in: " + time);
                System.exit(0);



        }

        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) throws Exception {
                // TODO code application logic here
                KaboumServerTest t = new KaboumServerTest();
                t.testSpatial();

        }
}
