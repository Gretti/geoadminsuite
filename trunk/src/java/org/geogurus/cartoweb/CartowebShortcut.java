/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.cartoweb;

import java.util.Properties;
import java.util.logging.Logger;

import org.geogurus.data.Extent;

/**
 * Represents a Cartoweb location.ini shortcut set of properties:<br/>
 * shortcuts.2.label = Midi-Pyrenees
 * shortcuts.2.bbox = "330000, 1730000,700000,2005000"<br/>
 * This object is used by the LocationConf object to manage a location.ini cartoweb
 * configuration file.
 * @author nicolas
 */
public class CartowebShortcut extends CartowebObject {
    /** the shortcut's label */
    private String label;
    /** the shortcut spatial extension */
    private Extent bbox;
    
    public CartowebShortcut(String id) {
        super(id);
        logger = Logger.getLogger(this.getClass().getName()); 
   }
     /**
     * Gets this object's representation as key=value pairs.
     * Each object's attribute will be rendered on a single line, with its name
     * as a key and its value as a value.
     * @see Properties
     * @param id: the identifier to use when rendering this object. it will be used as:
     *        shortcuts.id.[attribute] = [value]
     * @return the string represntation of this object, or null if id is null
     */
    public String toString() {
        if (id == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (label != null) {
            builder.append("shortcuts.").append(id).append(".label=");
            builder.append(getLabel()).append(System.getProperty("line.separator"));
        }
        if (bbox != null) {
            builder.append("shortcuts.").append(id).append(".bbox=\"");
            builder.append(getBbox().toCartowebString()).append("\"").append(System.getProperty("line.separator"));
        }
        
        return builder.toString();
    }
    
    /** writes this object into the given properties object, using
     * shortcuts.<id>.key=value syntax
     * See derived classes
     * @param props the Properties to write into
     */
    public void writeToProperties(Properties props) {
        if (props == null || id == null) {
            return;
        }
        if (getLabel() != null) {
            props.setProperty("shortcuts."+id+".label",getLabel());
        }
        if (getBbox() != null) {
            props.setProperty("shortcuts."+id+".bbox", getBbox().toCartowebString());
        }
        
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Extent getBbox() {
        return bbox;
    }

    public void setBbox(Extent bbox) {
        this.bbox = bbox;
    }
    
    /**
     * Returns the display string representing this shortcut:
     * id - label - value - visible
     * @return
     */
    @Override
    public String getDisplayString() {
        displayString = id + " - ";
        displayString += getLabel() == null ? "null" : getLabel();
        displayString += " - ";
        displayString += getBbox() == null ? "null" : getBbox().toCartowebString();
        return displayString;
    }
}
