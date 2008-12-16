/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.cartoweb;


/**
 * Represents a layers.ini layer metadata (name=value pair)
 * 
 * 
 * @author nicolas
 */
public class CartowebMetadata {

    private String name;
    private String value;

    public CartowebMetadata(String n, String v) {
        name = n;
        value = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public String toString() {
        return name + " = " + value;
    }
}
