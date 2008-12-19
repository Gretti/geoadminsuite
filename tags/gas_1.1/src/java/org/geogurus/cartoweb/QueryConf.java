/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.cartoweb;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Represents a query.ini cartoweb3 configuration file (client-side)
 * See cartoweb.org for documentation about this file.
 * @author nicolas Ribot
 */
public class QueryConf extends CartowebConf {

    // constants for defaultPolicy
    public static final byte POLICY_XOR = 0;
    public static final byte POLICY_UNION = 1;
    public static final byte POLICY_REPLACE = 2;
    public static final byte POLICY_INTERSECTION = 3;
    /** see cartoweb doc (cartoweb.org) */
    private Boolean persistentQueries;
    /** see cartoweb doc (cartoweb.org) */
    private Boolean displayExtendedSelection;
    /** see cartoweb doc (cartoweb.org) */
    private String queryLayers;
    /** see cartoweb doc (cartoweb.org) */
    private Boolean returnAttributesActive;
    /** see cartoweb doc (cartoweb.org) */
    private Byte defaultPolicy;
    /** see cartoweb doc (cartoweb.org) */
    private Boolean defaultMaskmode;
    /** see cartoweb doc (cartoweb.org) */
    private Boolean defaultHilight;
    /** see cartoweb doc (cartoweb.org) */
    private Boolean defaultAttributes;
    /** see cartoweb doc (cartoweb.org) */
    private Boolean defaultTable;
    /** see cartoweb doc (cartoweb.org) */
    private Integer weightQueryByPoint;
    /** see cartoweb doc (cartoweb.org) */
    private Integer weightQueryByBbox;
    /** see cartoweb doc (cartoweb.org) */
    private Integer weightQueryByPolygon;
    /** see cartoweb doc (cartoweb.org) */
    private Integer weightQueryByCircle;

    /**
     * Default constructor. call loadFromFile() to load object's properties
     */
    public QueryConf() {
        logger = Logger.getLogger(this.getClass().getName());
        displayExtendedSelection = Boolean.FALSE;
        returnAttributesActive  = Boolean.FALSE;
        defaultPolicy = QueryConf.POLICY_XOR;
        defaultMaskmode = Boolean.FALSE;
        defaultHilight = Boolean.TRUE;
        defaultAttributes = Boolean.TRUE;
        defaultTable = Boolean.TRUE;
        weightQueryByPoint = new Integer(40);
        weightQueryByBbox = new Integer(41);
        weightQueryByPolygon = new Integer(42);
        weightQueryByCircle = new Integer(43);
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
        Enumeration e = iniProps.propertyNames();
        while (e.hasMoreElements()) {
            String prop = (String) e.nextElement();

            // first
            if (prop.length() >= 1 && prop.trim().indexOf(";") == 0) {
                // a comment
                continue;
            }
            if ("persistentQueries".equalsIgnoreCase(prop)) {
                setPersistentQueries(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));

            } else if ("displayExtendedSelection".equalsIgnoreCase(prop)) {
                setDisplayExtendedSelection(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));

            } else if ("queryLayers".equalsIgnoreCase(prop)) {
                setQueryLayers(iniProps.getProperty(prop));

            } else if ("returnAttributesActive".equalsIgnoreCase(prop)) {
                setReturnAttributesActive(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));

            } else if ("defaultPolicy".equalsIgnoreCase(prop)) {
                String policy = iniProps.getProperty(prop);
                if ("POLICY_INTERSECTION".equalsIgnoreCase(policy)) {
                    setDefaultPolicy(new Byte(QueryConf.POLICY_INTERSECTION));
                } else if ("POLICY_XOR".equalsIgnoreCase(policy)) {
                    setDefaultPolicy(new Byte(QueryConf.POLICY_XOR));
                } else if ("POLICY_REPLACE".equalsIgnoreCase(policy)) {
                    setDefaultPolicy(new Byte(QueryConf.POLICY_REPLACE));
                } else if ("POLICY_UNION".equalsIgnoreCase(policy)) {
                    setDefaultPolicy(new Byte(QueryConf.POLICY_UNION));
                } else {
                    logger.warning("unknown defaultPolicy value: " + policy);
                }
            } else if ("defaultMaskmode".equalsIgnoreCase(prop)) {
                setDefaultMaskmode(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));

            } else if ("defaultHilight".equalsIgnoreCase(prop)) {
                setDefaultHilight(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));
            } else if ("defaultAttributes".equalsIgnoreCase(prop)) {
                setDefaultAttributes(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));

            } else if ("defaultTable".equalsIgnoreCase(prop)) {
                setDefaultTable(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));
            } else if ("weightQueryByPoint".equalsIgnoreCase(prop)) {
                setWeightQueryByPoint((Integer) Integer.parseInt(iniProps.getProperty(prop)));
            } else if ("weightQueryByBbox".equalsIgnoreCase(prop)) {
                setWeightQueryByBbox((Integer) Integer.parseInt(iniProps.getProperty(prop)));
            } else if ("weightQueryByPolygon".equalsIgnoreCase(prop)) {
                setWeightQueryByPolygon((Integer) Integer.parseInt(iniProps.getProperty(prop)));
            } else if ("weightQueryByCircle".equalsIgnoreCase(prop)) {
                setWeightQueryByCircle((Integer) Integer.parseInt(iniProps.getProperty(prop)));
            } else {
                logger.warning("unknown query.ini property: " + prop);
            }
        }
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
        // base class method sets the iniFile attribute with a valid file
        // or returns false if such a file cannot be loaded
        if (!super.loadFromFile(iniFilePath)) {
            return false;
        }
        return this.loadFromFile(iniFile);
    }
    
    /**
     * Returns the string representation of this object
     * @return
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (getPersistentQueries() != null) {
            b.append("persistentQueries = ").append(getPersistentQueries()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getDisplayExtendedSelection() != null) {
            b.append("displayExtendedSelection = ").append(getDisplayExtendedSelection().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getQueryLayers() != null) {
            b.append("queryLayers = ").append(getQueryLayers()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getReturnAttributesActive() != null) {
            b.append("returnAttributesActive = ").append(getReturnAttributesActive().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getDefaultPolicy() != null) {
            b.append("defaultPolicy = ").append(getDefaultPolicy().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getDefaultMaskmode() != null) {
            b.append("defaultMaskmode = ").append(getDefaultMaskmode().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getDefaultHilight() != null) {
            b.append("defaultHilight = ").append(getDefaultHilight().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getDefaultAttributes() != null) {
            b.append("defaultAttributes = ").append(getDefaultAttributes().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getDefaultTable() != null) {
            b.append("defaultTable = ").append(getDefaultTable().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getWeightQueryByPoint() != null) {
            b.append("weightQueryByPoint = ").append(getWeightQueryByPoint().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getWeightQueryByBbox() != null) {
            b.append("weightQueryByBbox = ").append(getWeightQueryByBbox().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getWeightQueryByPolygon() != null) {
            b.append("weightQueryByPolygon = ").append(getWeightQueryByPolygon().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (getWeightQueryByCircle() != null) {
            b.append("weightQueryByCircle = ").append(getWeightQueryByCircle().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        return b.toString();
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
        if (getPersistentQueries() != null) {
            iniProps.setProperty("persistentQueries", getPersistentQueries().toString());

        } 
        if (getDisplayExtendedSelection() != null) {
            iniProps.setProperty("displayExtendedSelection", getDisplayExtendedSelection().toString());
        }
        if (getQueryLayers() != null) {
            iniProps.setProperty("queryLayers",getQueryLayers());
        }
        if (getReturnAttributesActive() != null) {
            iniProps.setProperty("returnAttributesActive", getReturnAttributesActive().toString());
        }
        if (getDefaultPolicy() != null) {
            iniProps.setProperty("defaultPolicy", getDefaultPolicy().toString());
        }
        if (getDefaultMaskmode() != null) {
            iniProps.setProperty("defaultMaskmode", getDefaultMaskmode().toString());
        }
        if (getDefaultHilight() != null) {
            iniProps.setProperty("defaultHilight", getDefaultHilight().toString());
        }
        if (getDefaultAttributes() != null) {
            iniProps.setProperty("defaultAttributes", getDefaultAttributes().toString());
        }
        if (getDefaultTable() != null) {
            iniProps.setProperty("defaultTable", getDefaultTable().toString());
        }
        if (getWeightQueryByPoint() != null) {
            iniProps.setProperty("weightQueryByPoint", getWeightQueryByPoint().toString());
        }
        if (getWeightQueryByBbox() != null) {
            iniProps.setProperty("weightQueryByBbox", getWeightQueryByBbox().toString());
        }
        if (getWeightQueryByPolygon() != null) {
            iniProps.setProperty("weightQueryByPolygon", getWeightQueryByPolygon().toString());
        }
        if (getWeightQueryByCircle() != null) {
            iniProps.setProperty("weightQueryByCircle", getWeightQueryByCircle().toString());
        }
    }
     */

    public Boolean getPersistentQueries() {
        return persistentQueries;
    }

    public void setPersistentQueries(Boolean persistentQueries) {
        this.persistentQueries = persistentQueries;
    }

    public Boolean getDisplayExtendedSelection() {
        return displayExtendedSelection;
    }

    public void setDisplayExtendedSelection(Boolean displayExtendedSelection) {
        this.displayExtendedSelection = displayExtendedSelection;
    }

    public String getQueryLayers() {
        return queryLayers;
    }

    public void setQueryLayers(String queryLayers) {
        this.queryLayers = queryLayers;
    }

    public Boolean getReturnAttributesActive() {
        return returnAttributesActive;
    }

    public void setReturnAttributesActive(Boolean returnAttributesActive) {
        this.returnAttributesActive = returnAttributesActive;
    }

    public Byte getDefaultPolicy() {
        return defaultPolicy;
    }

    public void setDefaultPolicy(Byte defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
    }

    public Boolean getDefaultMaskmode() {
        return defaultMaskmode;
    }

    public void setDefaultMaskmode(Boolean defaultMaskmode) {
        this.defaultMaskmode = defaultMaskmode;
    }

    public Boolean getDefaultHilight() {
        return defaultHilight;
    }

    public void setDefaultHilight(Boolean defaultHilight) {
        this.defaultHilight = defaultHilight;
    }

    public Boolean getDefaultAttributes() {
        return defaultAttributes;
    }

    public void setDefaultAttributes(Boolean defaultAttributes) {
        this.defaultAttributes = defaultAttributes;
    }

    public Boolean getDefaultTable() {
        return defaultTable;
    }

    public void setDefaultTable(Boolean defaultTable) {
        this.defaultTable = defaultTable;
    }

    public Integer getWeightQueryByPoint() {
        return weightQueryByPoint;
    }

    public void setWeightQueryByPoint(Integer weightQueryByPoint) {
        this.weightQueryByPoint = weightQueryByPoint;
    }

    public Integer getWeightQueryByBbox() {
        return weightQueryByBbox;
    }

    public void setWeightQueryByBbox(Integer weightQueryByBbox) {
        this.weightQueryByBbox = weightQueryByBbox;
    }

    public Integer getWeightQueryByPolygon() {
        return weightQueryByPolygon;
    }

    public void setWeightQueryByPolygon(Integer weightQueryByPolygon) {
        this.weightQueryByPolygon = weightQueryByPolygon;
    }

    public Integer getWeightQueryByCircle() {
        return weightQueryByCircle;
    }

    public void setWeightQueryByCircle(Integer weightQueryByCircle) {
        this.weightQueryByCircle = weightQueryByCircle;
    }
}
