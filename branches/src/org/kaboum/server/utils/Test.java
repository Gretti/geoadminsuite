/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kaboum.server.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;

/**
 *
 * @author nicolas
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        for( Iterator i=DataStoreFinder.getAvailableDataStores(); i.hasNext(); ){
            DataStoreFactorySpi factory = (DataStoreFactorySpi) i.next();
            System.out.println(factory.getDisplayName() + " : " + factory.getDescription());
        } 
        
        Map params = new HashMap();
        params.put("dbtype", "postgis");        //must be postgis
        params.put("host", "localhost");        //the name or ip address of the machine running PostGIS
        params.put("port", new Integer(5432));  //the port that PostGIS is running on (generally 5432)
        params.put("database", "test");      //the name of the database to connect to.
        params.put("user", "postgres");         //the user to connect with
        params.put("passwd", "test");               //the password of the user.

        DataStore pgDatastore = DataStoreFinder.getDataStore(params);
        FeatureSource fsBC = pgDatastore.getFeatureSource("departements");

        System.out.println("dep count: " + fsBC.getCount(Query.ALL));        
    }

}
