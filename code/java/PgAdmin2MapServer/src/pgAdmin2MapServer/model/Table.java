/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

/**
 * A simple object representing a pgAdmin spatial table with ONE geometric
 * column. If a table has several columns, several tables will be generated
 *
 * @author nicolas
 */
public class Table {

    private String schemaName;
    private String name;
    private String geom;
    private String projection;
    private String type;

    public Table(String schemaName, String name, String geom, String projection, String type) {
        this.schemaName = schemaName;
        this.name = name;
        this.geom = geom;
        this.projection = projection;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
