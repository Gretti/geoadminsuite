/*
 * Attribute.java
 *
 * Created on 27 mars 2002, 13:47
 */
package org.geogurus.raster;
/**
 *
 * Copyright:    Copyright (c) 2001
 * Company: SCOT
 * @author  Bastien VIALADE
 */
public class Attribute {
    
    public String name;
    public String value;
    public String type;
    public String label;
    
    /** Creates a new instance of Attribute */
    public Attribute() {
        name=null;
        value=null;
        type=null;
        label=null;
    }
    
    public Attribute(String name, String value, String type, String label) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.label = label;
    }
    
}
