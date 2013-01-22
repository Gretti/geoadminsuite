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
    private Map<String, Table> tables;

    public Schema(String name, String databaseName) {
        this.name = name;
        this.databaseName = databaseName;
        this.tables = new HashMap<String, Table>();
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

    public Map<String, Table> getTables() {
        return tables;
    }

    public void setTables(Map<String, Table> tables) {
        this.tables = tables;
    }
    /**
     * Adds the given schema to this database
     * @param s 
     */
    public void addTable(Table t) {
        if (t != null) {
            this.tables.put(t.getName(), t);
        }
    }
    
}
