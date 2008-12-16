/*
 * Copyright (C) 2007-2008  Camptocamp
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.cartoweb;

import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * A class representing layer's information needed in cartoweb configuration files:
 * layers.ini and query.ini. These files can define a set of properties for each layer.
 * Example:<br/>
 * in layers.ini:
 * layers.landsat.className = Layer
 *   layers.landsat.label = Images Landsat
 *   layers.landsat.msLayer = landsat
 * @author nicolas
 */
public class CartowebLayer extends CartowebObject {
    public static final String RENDERING_TREE = "tree";
    public static final String RENDERING_BLOCK = "block";
    public static final String RENDERING_RADIO = "radio";
    public static final String RENDERING_DROPDOWN = "dropdown";

    private String className;
    private String msLayer;
    private String label;
    private String icon;
    private String link;
    private String children;
    private String switchId;
    private Boolean aggregate;
    private String rendering;
    private Float mdMinScale;
    private Float mdMaxScale;
    private Vector<CartowebMetadata> metadataList;
    /** the layers truely belonging to this group: these layers id are the same
     * as the one contained in children list
     */
    private Vector<CartowebLayer> layers;
    
    public CartowebLayer(String id) {
        super(id);
        logger = Logger.getLogger(this.getClass().getName()); 
        rendering = CartowebLayer.RENDERING_TREE;
    }

    /**
     * returns the CartowebLayer object whose id matches the given id, by lookintg
     * at this id and all inner layers
     * @param id
     * @return the cartoweb layer with this id, or null if not found or key is null
     */
    public CartowebLayer getLayer(String id) {
        if (id == null) {
            return null;
        }
        if (this.id.equals(id)) {
            return this;
        }
        if (layers != null) {            for (CartowebLayer l : layers) {
                if (l.getLayer(id) != null) {
                    return l;
                }
            } 
        }
        return null;
    }
    
    /**
     * Gets this object's representation as key=value pairs.
     * and writes down all its children layers, recursively
     * @see Properties
     * @param id: the identifier to use when rendering this object. it will be used as:
     *        layers.id.[attribute] = [value]
     *        if id is null, the internal layer identifier (id) will be used
     * @return the string represntation of this object. 
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        if (getClassName() != null) {
            b.append("layers.").append(this.id).append(".className = ");
            b.append(getClassName().toString()).append(this.lineSep);
        }
        if (getMsLayer() != null) {
            b.append("layers.").append(this.id).append(".msLayer = ");
            b.append(getMsLayer()).append(this.lineSep);
        }
        if (getLabel() != null) {
            b.append("layers.").append(this.id).append(".label = ");
            b.append(label).append(this.lineSep);
        }
        if (getIcon() != null) {
            b.append("layers.").append(this.id).append(".icon = ");
            b.append(getIcon()).append(this.lineSep);
        }
        if (getLink() != null) {
            b.append("layers.").append(this.id).append(".link = ");
            b.append(getLink()).append(this.lineSep);
        }
        if (getChildren() != null) {
            b.append("layers.").append(this.id).append(".children = ");
            b.append(getChildren()).append(this.lineSep);
        }
        if (getSwitchId() != null) {
            b.append("layers.").append(this.id).append(".switchId = ");
            b.append(getSwitchId()).append(this.lineSep);
        }
        if (getAggregate() != null) {
            b.append("layers.").append(this.id).append(".aggregate = ");
            b.append(getAggregate()).append(this.lineSep);
        }
        if (getRendering() != null) {
            b.append("layers.").append(this.id).append(".rendering = ");
            b.append(getRendering()).append(this.lineSep);
        }
        if (getMdMinScale() != null) {
            b.append("layers.").append(this.id).append(".metadataMinScale = ");
            b.append(getMdMinScale()).append(this.lineSep);
        }
        if (getMdMaxScale() != null) {
            b.append("layers.").append(this.id).append(".metadataMaxScale = ");
            b.append(getMdMaxScale()).append(this.lineSep);
        }
        
        if (this.getMetadataList() != null) {
            for (CartowebMetadata md : this.getMetadataList()) {
                b.append("layers.").append(this.id).append(".metadata.");
                b.append(md.toString()).append(this.lineSep);
            }
        }
        b.append(lineSep);
        if (this.layers != null) {
            for (CartowebLayer l : layers) {
                b.append(l.toString());
            }
        }
    
        return b.toString();
    }
    
    /**
     * returns true if given layer is equal to this:
     * their id are equals
     * @param l the layer to test
     * @return true if equals
     */
    @Override
    public boolean equals(Object l) {
        if (l == null || !(l instanceof CartowebLayer) || this.id == null) {
            return false;
        }
        return this.id.equals(((CartowebLayer)l).getId());
    }
    
    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
    
    
    /**
     * Adds this layer to the list of layers. 
     * Callers of this function should ensure that this.children contains a list with the same
     * layers id as this.layers vector.
     * Use addNewLayer to add a layer that does not belong to the list of children.
     * Use reorderLayers to transfer newLayers objects attributes to this.layers object attributes
     * @param l the layer to add
     */
    public void addLayer(CartowebLayer l) {
        if (layers == null) {
            layers = new Vector<CartowebLayer>();
        }
        layers.add(l);
    }
    
    /**
     * add the given metadata to the Vector of metadata 
     * @param md the metadata to add
     */
    public void addMetadata(CartowebMetadata md) {
        if (metadataList == null) {
            metadataList = new Vector<CartowebMetadata>();
        }
        metadataList.add(md);
    }

    
    /**
     * Copies all not null attributes of the given layer to this object (if the same
     * attribute is null
     * by creating a new Object base on the layer's attribute
     * 
     * @param l the layer to synchronize with
     */
    public void synchronize(CartowebLayer l) {
        if (l == null) {
            return;
        }
        if (l.children != null && this.children == null) {
            this.children = new String(l.children);
        }

        if (l.className != null && this.className == null) {
            this.className = new String(l.className);
        }
        if (l.id != null && this.id == null) {
            this.id = new String(l.id);
        }
        if (l.msLayer != null && this.msLayer == null) {
            this.msLayer = new String(l.msLayer);
        }
        if (l.label != null && this.label == null) {
            this.label = new String(l.label);
        }
        if (l.icon != null && this.icon == null) {
            this.icon = new String(l.icon);
        }
        if (l.link != null && this.link == null) {
            this.link = new String(l.link);
        }
        if (l.switchId != null && this.switchId == null) {
            this.switchId = new String(l.switchId);
        }
        if (l.aggregate != null && this.aggregate == null) {
            this.aggregate = new Boolean(l.aggregate);
        }
        if (l.rendering != null && this.rendering == null) {
            this.rendering = new String(l.rendering);
        }
        if (l.mdMinScale != null && this.mdMinScale == null) {
            this.mdMinScale = new Float(l.mdMinScale);
        }
        if (l.mdMaxScale != null && this.mdMaxScale == null) {
            this.mdMaxScale = new Float(l.mdMaxScale);
        }
    }
    
    /**
     * adds each given layer to the root layer node, in the corresponding layer group
     * @param layersToCopy the layers to copy into each 
     * @return the number of remaining layers into layersToCopy
     */
    public int addLayers(Hashtable<String, CartowebLayer> layersToCopy) {
        if (layersToCopy == null || this.getChildren() == null ) {
            return -1;
        }
        StringTokenizer tok = new StringTokenizer(this.getChildren(), ",");
        while (tok.hasMoreTokens()) {
            String layerId = tok.nextToken().trim();
            CartowebLayer l = layersToCopy.get(layerId);
            if (l == null) {
                logger.warning("layers.ini: no layer definition found for: " + 
                        layerId + ". This layerId is defined in the children of layer: " + this.getId());
            } else {
                this.addLayer(l);
                // removes added layers from the list of layers.
                layersToCopy.remove(layerId);
                l.addLayers(layersToCopy);
            }
        }
        return layersToCopy.size();
    }
    
    /** writes this object into the given properties object, using
     * scales.<id>.key=value syntax
     * Not yet implemented (does nothing)
     * @param props the Properties to write into
     * @param id the identifier to write for the object
     */
    public void writeToProperties(Properties props) {
        if (props == null || id == null) {
            return;
        }
    }
    
    /** 
     * returns the ExtJS JSON structure to load this layer into a Ext layer tree.<br/>
     * root folder case is treated by looking at layer's name "root", as this name
     * is mandatory in layers.ini CW file
     * 
     * @return a string representing this layer as a Ext JS json tree data
     */
    public String getExtTreeJson() {
        StringBuilder b = new StringBuilder();
        if (isRootLayer()) {
            b.append("[");
        }
        b.append("{\"text\":").append("\"").append(this.id).append("\",");
        b.append("\"id\":").append("\"").append(this.id).append("\",");
        b.append("\"cwattributes\":").append(getAttributesJson(true)).append(",");
        if (layers == null || layers.size() == 0) {
            b.append("\"leaf\":").append("true").append(",");
            b.append("\"cls\":").append("\"file\"").append(",");
        } else {
            b.append("\"children\":").append("[");
            for (CartowebLayer layer : this.layers) {
                b.append(layer.getExtTreeJson()).append(",");
            }
            // removes trailing comma
            b.deleteCharAt(b.length() - 1);
            b.append("]");
        }
        if (b.charAt(b.length() - 1) == ',') {
            b.deleteCharAt(b.length() - 1);
        }
        b.append("}");
        
        if (isRootLayer()) {
            b.append("]");
        }
        return b.toString();
        
    }
    
    /**
     * Returns a JSON structure representing this objects attributes
     * @param excludeClassName true to exclude the className attribute from the json
     * representation
     * @return the json representation of this object's attributes, for instance:<br/>
     * 
     */
    public String getAttributesJson(boolean excludeClassName) {
        StringBuilder b = new StringBuilder();
        b.append("{");
        if (!excludeClassName) {
            b.append("\"className\":\"").append(className == null ? "" : className).append("\",");
        }
        b.append("\"msLayer\":\"").append(msLayer == null ? "" : msLayer).append("\",");
        b.append("\"label\":\"").append(label == null ? "" : label).append("\",");
        b.append("\"icon\":\"").append(icon == null ? "" : icon).append("\",");
        b.append("\"link\":\"").append(link == null ? "" : link).append("\",");
        b.append("\"children\":\"").append(children == null ? "" : children).append("\",");
        b.append("\"switchId\":\"").append(switchId == null ? "" : switchId).append("\",");
        if (aggregate == null || !aggregate.booleanValue() ) {
            b.append("\"aggregate\":\"false\",");
        } else {
            b.append("\"aggregate\":\"true\",");
        }
        b.append("\"rendering\":\"").append(rendering == null ? "" : rendering).append("\",");
        b.append("\"mdMinScale\":\"").append(mdMinScale == null ? "" : mdMinScale).append("\",");
        b.append("\"mdMaxScale\":\"").append(mdMaxScale == null ? "" : mdMaxScale).append("\",");
        
        if (b.length() > 2) {
            b.deleteCharAt(b.length() -1);
        }
        b.append("}");
        return b.toString();
    }
    
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        if (className != null && className.length() == 0) {
            this.className = null;
        }
        this.className = className;
    }

    public String getMsLayer() {
        return msLayer;
    }

    public void setMsLayer(String msLayer) {
        if (msLayer != null && msLayer.length() == 0) {
            this.msLayer = null;
        }
        this.msLayer = msLayer;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (label != null && label.length() == 0) {
            this.label = null;
        }
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        if (icon != null && icon.length() == 0) {
            this.icon = null;
        }
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        if (link != null && link.length() == 0) {
            this.link = null;
        }
        this.link = link;
    }

    public String getChildren() {
        return children;
    }

    public void setChildren(String children) {
        if (children != null && children.length() == 0) {
            this.children = null;
        }
        this.children = children;
    }

    public String getSwitchId() {
        return switchId;
    }

    public void setSwitchId(String switchId) {
        if (switchId != null && switchId.length() == 0) {
            this.switchId = null;
        }
        this.switchId = switchId;
    }

    public Boolean getAggregate() {
        return aggregate;
    }

    public void setAggregate(Boolean aggregate) {
        this.aggregate = aggregate;
    }

    public String getRendering() {
        return rendering;
    }

    public void setRendering(String rendering) {
        if (rendering != null && rendering.length() == 0) {
            this.rendering = CartowebLayer.RENDERING_TREE;
        }
        this.rendering = rendering;
    }

    public Float getMdMinScale() {
        return mdMinScale;
    }

    public void setMdMinScale(Float mdMinScale) {
        if (mdMinScale != null && mdMinScale == Float.NaN) {
            this.mdMinScale = null;
        }
        this.mdMinScale = mdMinScale;
    }

    public void setMdMinScale(String mdMinScale) {
        try {
            Float f = Float.valueOf(mdMinScale);
            this.mdMinScale = f;
        } catch (NumberFormatException nfe) {
            this.mdMinScale = null;
        }
    }

    public Float getMdMaxScale() {
        return mdMaxScale;
    }

    public void setMdMaxScale(Float mdMaxScale) {
        if (mdMaxScale != null && mdMaxScale == Float.NaN) {
            this.mdMaxScale = null;
        }
        this.mdMaxScale = mdMaxScale;
    }

    public void setMdMaxScale(String mdMaxScale) {
        try {
            Float f = Float.valueOf(mdMaxScale);
            this.mdMaxScale = f;
        } catch (NumberFormatException nfe) {
            this.mdMaxScale = null;
        }
    }

    public Vector<CartowebMetadata> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(Vector<CartowebMetadata> metadataList) {
        this.metadataList = metadataList;
    }
    
    /**
     * True if given layer is the root one (its id is "root")
     * @return true if layer is the root group
     */
    public boolean isRootLayer() {
        return ("root".equals(this.id));
    }

    /**
     * True if given layer is a group layer (its className is "LayerGroup")
     * @return true if layer is a group layer
     */
    public boolean isGroupLayer() {
        return ("LayerGroup".equals(this.className));
    }
}
