package org.geogurus.cartoweb;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Represents a Cartoweb images.ini "mapSize" set of properties:<br/>
 * mapSizes.0.label = 400x300
 * mapSizes.0.width = 400
 * mapSizes.0.height = 300
<br/>
 * This object is used by the ImagssConf object to manage a location.ini cartoweb
 * configuration file.
 * @author nicolas
 */
public class CartowebMapSize extends CartowebObject {
    /** the shortcut's label */
    private String label;
    /** the image's width */
    private Integer width;
    /** the image's height */
    private Integer height;
    
    public CartowebMapSize(String id) {
        super(id);
        logger = Logger.getLogger(this.getClass().getName()); 
    }

    
     /**
     * Gets this object's representation as key=value pairs.
     * Each object's attribute will be rendered on a single line, with its name
     * as a key and its value as a value.
     * @see Properties
     * @param id: the identifier to use when rendering this object. it will be used as:
     *        mapSizes.id.[attribute] = [value]
     * @return the string represntation of this object, or null if id is null
     */
    public String toString() {
        if (id == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (getLabel() != null) {
            builder.append("mapSizes.").append(id).append(".label=");
            builder.append(getLabel()).append(System.getProperty("line.separator"));
        }
        if (getWidth() != null) {
            builder.append("mapSizes.").append(id).append(".width=");
            builder.append(getWidth().toString()).append(System.getProperty("line.separator"));
        }
        if (getHeight() != null) {
            builder.append("mapSizes.").append(id).append(".height=");
            builder.append(getHeight().toString()).append(System.getProperty("line.separator"));
        }
        
        return builder.toString();
    }
    
    /** writes this object into the given properties object, using
     * mapSizes.<id>.key=value syntax
     * See derived classes
     * @param props the Properties to write into
     * @param id the identifier to write for the object
     */
    public void writeToProperties(Properties props) {
        if (props == null || id == null) {
            return;
        }
        props.setProperty("mapSizes."+id+".label",getLabel());
        props.setProperty("mapSizes."+id+".width", getWidth().toString());
        props.setProperty("mapSizes."+id+".height", getHeight().toString());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

        /**
     * Returns the display string representing this MapSize:
     * id - label - value - visible
     * @return
     */
    @Override
    public String getDisplayString() {
        displayString = id + " - ";
        displayString += getLabel() == null ? "null" : getLabel();
        displayString += " - ";
        displayString += getWidth() == null ? "null" : getWidth().toString();
        displayString += " - ";
        displayString += getHeight() == null ? "null" : getHeight().toString();
        return displayString;
    }

}
