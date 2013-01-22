/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.model;

/**
 * A simple object representing a pgAdmin spatial table with ONE geometric column.
 * If a table has several columns, several tables will be generated
 * 
 * @author nicolas
 */
public class Table {
    private String name;
    private String geom;
    
}
