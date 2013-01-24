/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple object representing a PgAdmin schema: name, databaseName, list of tables
 * @author nicolas
 */
public class Schema {
    /** schema name */
    private String name;
    /** database name */
    private String databaseName; // helper
    /** the list of tables */
    private Map<String, MSLayer> layers;

    public Schema(String name, String databaseName) {
        this.name = name;
        this.databaseName = databaseName;
        this.layers = new HashMap<String, MSLayer>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Map<String, MSLayer> getLayers() {
        return layers;
    }

    public void setLayers(Map<String, MSLayer> layers) {
        this.layers = layers;
    }
    /**
     * Adds the given schema to this database
     * @param s 
     */
    public void addTable(MSLayer l) {
        if (l != null) {
            this.layers.put(l.name, l);
        }
    }
    
}
