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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.geogurus.data.DataAccess;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * Represents a layers.ini cartoweb3 configuration file (client-side)
 * See cartoweb.org for documentation about this file.
 * Caution: the layer object in the layers.ini file layers must be declared AFTER the
 * layer group containing it.
 * Therefore, if a layer is defined in the file before any layourGroup containing it,
 * the parser will add this layer to the root layerGroup.
 *  
 * @author nicolas Ribot
 */
public class LayerConf extends CartowebConf {
    private Boolean autoClassLegend;
    private CartowebLayer rootLayer;
    private Hashtable<String, CartowebLayer> newLayers;
    
    /**
     * Default ctor
     */
    public LayerConf() {
        logger = Logger.getLogger(this.getClass().getName());
    }
    
    /**
     * Returns a CartowebLayer whose id is "root" that contains all
     * current geometryClasses found in the given UserMapBean
     * @param umb the UserMapBean to get GC from
     * @return a root cartowebLayer, or null if no layers can be found
     */
    public static CartowebLayer getCurrentLayers(UserMapBean umb) {
        if (umb == null || umb.getUserLayerOrder() == null) {
            return null;
        }
        CartowebLayer layer = new CartowebLayer("root");
        for (String s : umb.getUserLayerOrder()) {
            DataAccess gc = umb.getUserLayerList().get(s);
            // avoid storing layers id with space or dot, as these characters will mess up layers.ini file
            CartowebLayer l = new CartowebLayer(gc.getName().replace(" ", "_").replace(".", "_"));
            l.setClassName("Layer");
            l.setMsLayer(gc.getName());
            l.setAggregate(Boolean.FALSE);
            l.setLabel(gc.getName());
            layer.addLayer(l);
        }
        return layer;
    }
    
    /**
     * load this object from the given cartoweb3 .ini configuration file
     * @param iniFile
     * @return true if the object is correctly loaded, false otherwise. 
     *         In this case, see the generated log (warning level)
     */
    @Override
    public boolean loadFromFile(InputStream iniStream) {
        if (!super.loadFromFile(iniStream)) {
            // error logged by superclass
            return false;
        }
        // build a fresh list of layers here; not in the ctor...
        newLayers = new Hashtable<String, CartowebLayer>();
        
        Enumeration e = iniProps.propertyNames();
        while (e.hasMoreElements()) {
            String prop = (String) e.nextElement();

            // first
            if (prop.length() >= 1 && prop.trim().indexOf(";") == 0) {
                // a comment
                continue;
            }
            String[] keys = ConversionUtilities.explodeKey(prop);
            if (keys.length > 1 && "layers".equalsIgnoreCase(keys[0])) {
                // a layer object to set
                CartowebLayer l = null;
                if (newLayers.get(keys[1]) != null) { 
                    l = newLayers.get(keys[1]);
                } else {
                    l = new CartowebLayer(keys[1]);
                    newLayers.put(keys[1], l);
                }
                if ("className".equalsIgnoreCase(keys[2])) {
                    l.setClassName(iniProps.getProperty(prop).trim());
                } else if ("msLayer".equalsIgnoreCase(keys[2])) {
                    l.setMsLayer(iniProps.getProperty(prop).trim());
                } else if ("label".equalsIgnoreCase(keys[2])) {
                    l.setLabel(iniProps.getProperty(prop).trim());
                } else if ("icon".equalsIgnoreCase(keys[2])) {
                    l.setIcon(iniProps.getProperty(prop).trim());
                } else if ("link".equalsIgnoreCase(keys[2])) {
                    l.setLink(iniProps.getProperty(prop).trim());
                } else if ("children".equalsIgnoreCase(keys[2])) {
                    // the list of layers belonging to this layer
                    l.setChildren(iniProps.getProperty(prop).trim());
                } else if ("switchId".equalsIgnoreCase(keys[2])) {
                    l.setSwitchId(iniProps.getProperty(prop).trim());
                } else if ("aggregate".equalsIgnoreCase(keys[2])) {
                    l.setAggregate(new Boolean(iniProps.getProperty(prop).trim().equalsIgnoreCase("true")));
                } else if ("rendering".equalsIgnoreCase(keys[2])) {
                    l.setRendering(iniProps.getProperty(prop).trim());
                } else if ("metadata.MinScale".equalsIgnoreCase(keys[2])) {
                    l.setMdMinScale(new Float(iniProps.getProperty(prop).trim()));
                } else if ("metadata.MaxScale".equalsIgnoreCase(keys[2])) {
                    l.setMdMaxScale(new Float(iniProps.getProperty(prop).trim()));
                } else if ("metadata".equalsIgnoreCase(keys[2])) {
                    l.addMetadata(
                            new CartowebMetadata(
                            keys[3],
                            iniProps.getProperty(prop)));
                    
                } else {
                    logger.warning("unknown layers.ini property for 'layers' object: " + keys[2]);
                }
            } else if ("autoClassLegend".equalsIgnoreCase(prop)) {
                autoClassLegend = new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true"));
            } else {
                logger.warning("unknown layers.ini property: " + prop);
            }
        }
        rootLayer = newLayers.get("root");
        if (rootLayer == null) {
            logger.warning("invalid layers.ini file: no root layer defined");
            return false;
        }
        //parsing done, reorder layers now:
        int res = rootLayer.addLayers(newLayers);
        if (res == -1) {
            logger.warning("Invalid layers.ini file: missing layers definition or root layer does not have children");
        }
        /*
        else if (res > 0) {
            logger.warning("layers.ini: " + res + " defined layer(s) do(es) not belong to any group: " + newLayers.keySet());
        }
         */
        logger.info("num layers added to root and its children: " + res);
        return true;
    }

    /**
     * load this object from the given cartoweb3 .ini configuration file path
     * @param iniFile
     * @return true if the object is correctly loaded, false otherwise. 
     *         In this case, see the generated log (warning level)
     */
    @Override
    public boolean loadFromFile(String iniFilePath) {
        if (!super.loadFromFile(iniFilePath)) {
            return false;
        }
        return this.loadFromFile(iniFile);
    }

    /**
     * Saves this object into the given file, as a .ini structure (key=value lines)
     * @param fileToSave
     * @return true if file was correctly saved, false otherwise.
     *         In this case, see the generated log (warning level)
    @Override
    public boolean saveAsFile(File fileToSave) {
        if (!super.saveAsFile(fileToSave)) {
            return false;
        }
        if (!fileToSave.isFile()) {
            logger.warning("given ini file path is not a valid file: " + this.iniFile);
            return false;
        }
        //synchronizeProperties();
        try {
            iniProps.store(new FileOutputStream(fileToSave), CartowebConf.GAS_HEADER);
        } catch (IOException ioe) {
            logger.warning("Exception during save: " + ioe.getMessage());
            return false;
        }
        return true;
    }
     */

    /**
     * Returns the string representation of this object as key=value pairs, where
     * key is the cartoweb3 property name for this configuration object.
     * @return
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (getAutoClassLegend() != null) {
            b.append("autoClassLegend = ").append(getAutoClassLegend().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        // writes layers
        if (rootLayer != null) {
            b.append(rootLayer.toString());
        }
        return b.toString();
    }

    public Boolean getAutoClassLegend() {
        return autoClassLegend;
    }

    public void setAutoClassLegend(Boolean autoClassLegend) {
        this.autoClassLegend = autoClassLegend;
    }

    public void setAutoClassLegendStruts(boolean autoClassLegend) {
        this.autoClassLegend = new Boolean(autoClassLegend);
    }

    public CartowebLayer getRootLayer() {
        return rootLayer;
    }

    public void setRootLayer(CartowebLayer rootLayer) {
        this.rootLayer = rootLayer;
    }
    
    public Hashtable<String, CartowebLayer> getNewLayers() {
        return newLayers;
    }

    /**
     * Synchronizes the properties attribute with current attributes values
    @Override
    public void synchronizeProperties() {
        if (iniProps == null) {
            return;
        }
        // rewrites all properties
        iniProps.clear();
    }
     */
    

}
