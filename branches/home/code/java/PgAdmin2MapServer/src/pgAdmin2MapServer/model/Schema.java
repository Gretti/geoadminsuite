/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

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
    
}
