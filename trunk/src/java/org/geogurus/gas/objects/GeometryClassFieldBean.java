/*
 * HostDescriptorBean.java
 *
 * Created on 27 december 2006, 10:46
 *
 */

package org.geogurus.gas.objects;

import java.io.Serializable;

/**
 *
 * @author Gretti
 */
public class GeometryClassFieldBean implements Serializable {
    
    public static String TYPE_STRING = "String";
    public static String TYPE_FLOAT = "Float";
    public static String TYPE_NUMERIC = "Numeric";
    public static String TYPE_INVALID = "Invalid Type";
    
    /** Field Name */
    protected String name = "";
    
    /** Field Type */
    protected String type = "";
    
    /** Field Length */
    protected int length = 0;
    
    /** Nullable */
    protected String nullable = "";
    
    /**
     * Creates a new instance of GeometryClassFieldBean
     */
    public GeometryClassFieldBean() {
        super();
    }
    
    public GeometryClassFieldBean(String name_, String type_, int length_, String nullable_) {
        super();
        this.name = name_;
        this.type = type_;
        this.length = length_;
        this.nullable = nullable_;
    }
    
    /** Getter Method : Name*/
    public String getName() {return name;}
    /** Getter Method : Type*/
    public String getType() {return type;}
    /** Getter Method : Length*/
    public int getLength() {return length;}
    /** Getter Method : Nullable*/
    public String getNullable() {return nullable;}
    
    public boolean isNumeric() {
        return (type.startsWith("int") || type.startsWith("float") || type.equalsIgnoreCase("decimal") || type.equalsIgnoreCase("numeric"));
    };
    
    /** Setter Method : Name*/
    public void setName(String name_) {name = name_;}
    /** Setter Method : Type*/
    public void setType(String type_) {type = type_;}
    /** Setter Method : Length*/
    public void setLength(int length_) {length = length_;}
    /** Setter Method : Nullable*/
    public void setNullable(String nullable_) {nullable = nullable_;}
    
    public String toString() {
        String str = "Field :\n";
        
        str += this.getName()  + "\t";
        str += this.getType()  + "\t";
        str += this.getLength()  + "\t";
        str += this.getNullable() + "\n";
        
        return str;
    }
}
