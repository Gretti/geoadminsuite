/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        return (type.startsWith("Int") || type.startsWith("int") || 
                type.startsWith("Float") || type.startsWith("float") || 
                type.equalsIgnoreCase("Decimal") || 
                type.equalsIgnoreCase("Numeric") || 
                type.equalsIgnoreCase("Double") || 
                type.equalsIgnoreCase("Long") || 
                type.equalsIgnoreCase("Serial"));
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
