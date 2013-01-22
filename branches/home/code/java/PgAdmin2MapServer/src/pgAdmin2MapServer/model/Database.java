/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

import java.util.Map;

/**
 * A simple object to represent a PgAdmin database: a name and a Map of schemas
 * TODO: inheritance
 * @author nicolas
 */
public class Database {
    private String name;
    private Map<String, Schema> schemas;
    
    public Database(String name, Map<String, Schema> schemas) {
        this.name = name;
        this.schemas = schemas;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(Map<String, Schema> schemas) {
        this.schemas = schemas;
    }
}
