package org.kaboum.geom;

/*
 *
 * Class KaboumGeoObjectProperties from the Kaboum project.
 * This class define a geometry class. A geometry class
 * id defined by a geometry type, a color...
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * Copyright (C) 2000-2003 Jerome Gasperi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

import java.io.Serializable;
import java.util.Hashtable;

/**
 *
 * This class define geoObject "computational" properties:
 *     - Do the objects from this class participate in topological computation
 *     - Do the other objects must lie into this object
 *     - Are the objects from this class visible
 *     - ...
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGeometryPropertiesDescriptor implements Serializable {
    
    /** Visibility */
    private boolean visibility;
    
    /** Topological status (True: object is used in topological computation) */
    private boolean topoComputation;
    
    /** True: with topo computation, other objects must be drawn inside this object */
    private boolean surrounding;
    
    /** True: objects from this geometry cannot be selected */
    private boolean locking;
    
    /** Name of this class */
    private String name;
    
    /** True: this layer supports multi objects on the server side (see kaboumServer.properties for details
     * 
     */
    private boolean multiSupported;
    
    /** True if this geoObjects layer participate to the vertex snapping mechanism: its vertices 
     * will be snapped to if they are close enough to the current edited vertex
     */
    private boolean vertexSnappable;
    
    /** True if this geoObjects layer participate to the edge snapping mechanism: its edges
     * will be snapped to if they are close enough to the current edited vertex
     */
    private boolean edgeSnappable;
    
    /** Reference to the contextual menu */
    private Hashtable contextualMenuHash = new Hashtable();
    
    
    /**
     *
     * Default constructor
     *
     */
    public KaboumGeometryPropertiesDescriptor(String name) {
        this(name, true, false, false, false, false, false);
    }
    
    
    /**
     *
     * Constructor
     *
     *  @param name Name of this class
     *  @param visibility Set the visibility (paint) of the geometry class
     *  @param topoComputation True: object is used in topological computation
     *  @param surrounding True: with topo computation, other objects must be drawn inside this object
     *  @param locking True: objects from this geometry cannot be selected
     *
     */
    public KaboumGeometryPropertiesDescriptor(
            String name, 
            boolean visibility, 
            boolean topoComputation, 
            boolean surrounding, 
            boolean locking, 
            boolean vertexSnappable,
            boolean edgeSnappable) {
        this.name = name;
        this.visibility = visibility;
        this.topoComputation = topoComputation;
        this.surrounding = surrounding;
        this.locking = locking;
        this.vertexSnappable = vertexSnappable;
        this.edgeSnappable = edgeSnappable;
    }
    
    
    /**
     *
     * Get name
     *
     */
    public String getName() {
        return this.name;
    }
    
    
    /**
     *
     * Get visibility
     *
     */
    public boolean isVisible() {
        return this.visibility;
    }
    
    
    /**
     *
     * Get Topological status
     *
     */
    public boolean isComputed() {
        return this.topoComputation;
    }
    
    
    /**
     *
     * Get surrounding status
     *
     */
    public boolean isSurrounding() {
        return this.surrounding;
    }
    
    
    /**
     *
     * Get locking status
     *
     */
    public boolean isLocked() {
        return this.locking;
    }
    
    /**
     *
     * Get vertex snappable status
     *
     */
    public boolean isVertexSnappable() {
        return this.vertexSnappable;
    }
    
    /**
     *
     * Get edge snappable status
     *
     */
    public boolean isEdgeSnappable() {
        return this.edgeSnappable;
    }
    
    
    //////////// SET METHODS /////////////////
    
    public void setLocking(boolean b) {
        this.locking = b;
    }
    
    public void setVertexSnappable(boolean b) {
        this.vertexSnappable = b;
    }
    
    public void setEdgeSnappable(boolean b) {
        this.edgeSnappable = b;
    }
    
    public void setSurrounding(boolean b) {
        this.surrounding = b;
    }
    
    public void setVisibility(boolean b) {
        this.visibility = b;
    }
    
    public void setComputation(boolean b) {
        this.topoComputation = b;
    }
    
    
    
    ///////////// Contextual menu stuff  ////////////////

    /**
     *
     * Get the menus hashtable
     *
     */
    public Hashtable getMenuHash() {
        return this.contextualMenuHash;
    }
    
    
    /**
     *
     * Add a new key/value menu to the
     * contextual menu hash
     *
     * @param key Menu key
     * @param value Menu value
     *
     */
    public void addItemToMenu(String key, String value) {
        this.contextualMenuHash.put(key, value);
    }
    
    
    /**
     *
     * Clear the contextual menu
     *
     */
    public void clearMenu() {
        this.contextualMenuHash.clear();
    }
    
    
    /**
     *
     * Returns a string representation of this object:
     * properties, visibility, topological status, surrounding status, lock status.
     *@return a String representing this object.
     */
    public String toString() {
        
        String str = "";
        
        str = "PROPERTIES : " + this.name + "\n";
        str += "  VISIBILITY : " + this.visibility + "\n";
        str += "  TOPOLOGICAL COMPUTATION : " + this.topoComputation + "\n";
        str += "  IS SURROUNDING : " + this.surrounding + "\n";
        str += "  IS LOCKED : " + this.locking + "\n";
        str += "  IS VERTEX SNAPPABLE : " + this.vertexSnappable + "\n";
        str += "  IS EDGE SNAPPABLE : " + this.edgeSnappable + "\n";
        str += "  MULTI SUPPORTED : " + this.isMultiSupported();
        
        return (str);
        
    }

    public boolean isMultiSupported() {
        return multiSupported;
    }

    public void setMultiSupported(boolean multiSupported) {
        this.multiSupported = multiSupported;
    }
}

