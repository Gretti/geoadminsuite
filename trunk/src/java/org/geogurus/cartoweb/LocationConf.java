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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.geogurus.data.Extent;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * Represents a location.ini cartoweb3 configuration file (client-side)
 * See cartoweb.org for documentation about this file.
 * @author nicolas Ribot
 */
public class LocationConf extends CartowebConf {
    /** collection of scales for this configuration file */
    private Hashtable<String, CartowebScale> scales;
    /** the array of CartowebScales as dislay String, (hyphen-separated list of attributes)
     * to work with view components like
     * web pages
     */
    private String[] scalesAsString;

    /** collection of shorcuts for this configuration file */
    private Hashtable<String, CartowebShortcut> shortcuts;

    /** the array of CartowebShortcuts as dislay Strings (hyphen-separated list of attributes), 
     * to work with view components like
     * web pages
     */
    private String[] shortcutsAsString;
    
   
    private Integer minScale; 
    private Integer maxScale; 
    private Boolean scaleModeDiscrete; 
    private Float zoomFactor; 
    private Boolean noBboxAdjusting; 
    private Integer recenterMargin; 
    private Float recenterDefaultScale; 
    private String refMarksSymbol; 
    private Integer refMarksSymbolSize; 
    private Integer refMarksSize; 
    private String refMarksColor; 
    private Integer refMarksTransparency; 
    private String refMarksOrigin; 
    private Boolean refLinesActive; 
    private Integer refLinesSize; 
    private Float refLinesFontSize;     

    public LocationConf() {
        logger = Logger.getLogger(this.getClass().getName()); 
        scaleModeDiscrete = Boolean.FALSE;
        noBboxAdjusting = Boolean.FALSE;
        refLinesActive = Boolean.FALSE;
        
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
        scales = new Hashtable<String, CartowebScale>();
        shortcuts = new Hashtable<String, CartowebShortcut>();
        Enumeration e = iniProps.propertyNames();
        while (e.hasMoreElements()) {
            String prop = (String) e.nextElement();

            // first
            if (prop.length() >= 1 && prop.trim().indexOf(";") == 0) {
                // a comment
                continue;
            }
            String[] keys = ConversionUtilities.explodeKey(prop);
            if (keys.length > 2) {
                if ("scales".equalsIgnoreCase(keys[0])) {
                    CartowebScale s = scales.containsKey(keys[1]) ? scales.get(keys[1]) : new CartowebScale(keys[1]);
                    if ("label".equalsIgnoreCase(keys[2])) {
                        s.setLabel(iniProps.getProperty(prop));
                    } else if ("value".equalsIgnoreCase(keys[2])) {
                        s.setValue(new Double(iniProps.getProperty(prop)));
                    } else if ("visible".equalsIgnoreCase(keys[2])) {
                        s.setVisible(new Boolean(iniProps.getProperty(prop)));
                    }
                    scales.put(s.getId(), s);
                } else if ("shortcuts".equalsIgnoreCase(keys[0])) {
                    CartowebShortcut sh = shortcuts.containsKey(keys[1]) ? shortcuts.get(keys[1]) : new CartowebShortcut(keys[1]);
                    if ("label".equalsIgnoreCase(keys[2])) {
                        sh.setLabel(iniProps.getProperty(prop));
                    } else if ("bbox".equalsIgnoreCase(keys[2])) {
                        sh.setBbox(Extent.getExtentFromCW3(iniProps.getProperty(prop)));
                    }
                    shortcuts.put(sh.getId(), sh);
                }
            } else if ("minScale".equalsIgnoreCase(prop)) {
                setMinScale(new Integer(iniProps.getProperty(prop)));
            } else if ("maxScale".equalsIgnoreCase(prop)) {
                setMaxScale(new Integer(iniProps.getProperty(prop)));
            } else if ("scaleModeDiscrete".equalsIgnoreCase(prop)) {
                setScaleModeDiscrete(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));
            } else if ("zoomFactor".equalsIgnoreCase(prop)) {
                setZoomFactor(new Float(iniProps.getProperty(prop)));
            } else if ("noBboxAdjusting".equalsIgnoreCase(prop)) {
                setNoBboxAdjusting(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));
            } else if ("recenterMargin".equalsIgnoreCase(prop)) {
                setRecenterMargin(new Integer(iniProps.getProperty(prop)));
            } else if ("recenterDefaultScale".equalsIgnoreCase(prop)) {
                setRecenterDefaultScale(new Float(iniProps.getProperty(prop)));
            } else if ("refMarksSymbol".equalsIgnoreCase(prop)) {
                setRefMarksSymbol(iniProps.getProperty(prop));
            } else if ("refMarksSymbolSize".equalsIgnoreCase(prop)) {
                setRefMarksSymbolSize(new Integer(iniProps.getProperty(prop)));
            } else if ("refMarksSize".equalsIgnoreCase(prop)) {
                setRefMarksSize(new Integer(iniProps.getProperty(prop)));
            } else if ("refMarksColor".equalsIgnoreCase(prop)) {
                setRefMarksColor(iniProps.getProperty(prop).replace(',', ' '));
            } else if ("refMarksTransparency".equalsIgnoreCase(prop)) {
                setRefMarksTransparency(new Integer(iniProps.getProperty(prop)));
            } else if ("refLinesActive".equalsIgnoreCase(prop)) {
                setRefLinesActive(new Boolean(iniProps.getProperty(prop).equalsIgnoreCase("true")));
            } else if ("refMarksOrigin".equalsIgnoreCase(prop)) {
                setRefMarksOrigin(iniProps.getProperty(prop));
            } else if ("refLinesSize".equalsIgnoreCase(prop)) {
                setRefLinesSize(new Integer(iniProps.getProperty(prop)));
            } else if ("refLinesFontSize".equalsIgnoreCase(prop)) {
                setRefLinesFontSize(new Float(iniProps.getProperty(prop)));
            } else {
                logger.warning("unknown location.ini property: " + prop);
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
        if (!super.loadFromFile(iniFilePath)) {
            return false;
        }
        return this.loadFromFile(iniFile);
    }

    /**
     * Returns a string representation of this object (key=value pairs, one empty line
     * between each
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (getMinScale() != null) {
            b.append("minScale = ").append(getMinScale().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getMaxScale() != null) {
            b.append("maxScale = ").append(getMaxScale().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getScaleModeDiscrete() != null) {
            b.append("scaleModeDiscrete = ").append(getScaleModeDiscrete().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getZoomFactor() != null) {
            b.append("zoomFactor = ").append(getZoomFactor().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getNoBboxAdjusting() != null) {
            b.append("noBboxAdjusting = ").append(getNoBboxAdjusting().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRecenterMargin() != null) {
            b.append("recenterMargin = ").append(getRecenterMargin().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRecenterDefaultScale() != null) {
            b.append("recenterDefaultScale = ").append(getRecenterDefaultScale().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefMarksSymbol() != null) {
            b.append("refMarksSymbol = ").append(getRefMarksSymbol().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefMarksSymbolSize() != null) {
            b.append("refMarksSymbolSize = ").append(getRefMarksSymbolSize().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefMarksSize() != null) {
            b.append("refMarksSize = ").append(getRefMarksSize().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefMarksColor() != null) {
            b.append("refMarksColor = ").append(getRefMarksColor()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefMarksTransparency() != null) {
            b.append("refMarksTransparency = ").append(getRefMarksTransparency().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefMarksOrigin() != null) {
            b.append("refMarksOrigin = ").append(getRefMarksOrigin().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefLinesActive() != null) {
            b.append("refLinesActive = ").append(getRefLinesActive().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefLinesSize() != null) {
            b.append("refLinesSize = ").append(getRefLinesSize().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getRefLinesFontSize() != null) {
            b.append("refLinesFontSize = ").append(getRefLinesFontSize().toString()).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        } 
        if (getScales() != null) {
            // gets sorted keys to iterate on, to have a nice ini file
            String[] sortedKeys = (String[])scales.keySet().toArray(new String[scales.size()]);
            Arrays.sort(sortedKeys);
            for (int i = 0; i < sortedKeys.length; i++) {
                b.append(scales.get(sortedKeys[i]).toString()).append(System.getProperty("line.separator"));
            }
        }
        if (getShortcuts() != null) {
            // gets sorted keys to iterate on, to have a nice ini file
            String[] sortedKeys = (String[])shortcuts.keySet().toArray(new String[shortcuts.size()]);
            Arrays.sort(sortedKeys);
            for (int i = 0; i < shortcuts.size(); i++) {
                b.append(shortcuts.get(sortedKeys[i]).toString()).append(System.getProperty("line.separator"));
            }
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
        if (getMinScale() != null) {
            iniProps.setProperty("minScale", getMinScale().toString());
        } 
        if (getMaxScale() != null) {
            iniProps.setProperty("maxScale", getMaxScale().toString());
        } 
        if (getScaleModeDiscrete() != null) {
            iniProps.setProperty("scaleModeDiscrete", getScaleModeDiscrete().toString());
        } 
        if (getZoomFactor() != null) {
            iniProps.setProperty("zoomFactor", getZoomFactor().toString());
        } 
        if (getNoBboxAdjusting() != null) {
            iniProps.setProperty("noBboxAdjusting", getNoBboxAdjusting().toString());
        } 
        if (getRecenterMargin() != null) {
            iniProps.setProperty("recenterMargin", getRecenterMargin().toString());
        } 
        if (getRecenterDefaultScale() != null) {
            iniProps.setProperty("recenterDefaultScale", getRecenterDefaultScale().toString());
        } 
        if (getRefMarksSymbol() != null) {
            iniProps.setProperty("refMarksSymbol", getRefMarksSymbol().toString());
        } 
        if (getRefMarksSymbolSize() != null) {
            iniProps.setProperty("refMarksSymbolSize", getRefMarksSymbolSize().toString());
        } 
        if (getRefMarksSize() != null) {
            iniProps.setProperty("refMarksSize", getRefMarksSize().toString());
        } 
        if (getRefMarksColor() != null) {
            iniProps.setProperty("refMarksColor", getRefMarksColor().toCWString());
        } 
        if (getRefMarksTransparency() != null) {
            iniProps.setProperty("refMarksTransparency", getRefMarksTransparency().toString());
        } 
        if (getRefMarksOrigin() != null) {
            iniProps.setProperty("refMarksOrigin", getRefMarksOrigin().toString());
        } 
        if (getRefLinesActive() != null) {
            iniProps.setProperty("refLinesActive", getRefLinesActive().toString());
        } 
        if (getRefLinesSize() != null) {
            iniProps.setProperty("refLinesSize", getRefLinesSize().toString());
        } 
        if (getRefLinesFontSize() != null) {
            iniProps.setProperty("refLinesFontSize", getRefLinesFontSize().toString());
        } 
        if (getScales() != null) {
            // gets sorted keys to iterate on, to have a nice ini file
            String[] sortedKeys = (String[])scales.keySet().toArray(new String[scales.size()]);
            Arrays.sort(sortedKeys);
            for (int i = 0; i < sortedKeys.length; i++) {
                scales.get(sortedKeys[i]).writeToProperties(iniProps, Integer.toString(i));
            }
        }
        if (getShortcuts() != null) {
            // gets sorted keys to iterate on, to have a nice ini file
            String[] sortedKeys = (String[])shortcuts.keySet().toArray(new String[shortcuts.size()]);
            Arrays.sort(sortedKeys);
            for (int i = 0; i < shortcuts.size(); i++) {
                shortcuts.get(sortedKeys[i]).writeToProperties(iniProps, Integer.toString(i));
            }
        }
    }
     */

    public Hashtable<String, CartowebScale> getScales() {
        return scales;
    }

    public void setScales(Hashtable<String, CartowebScale> scales) {
        this.scales = scales;
    }

    public Hashtable<String, CartowebShortcut> getShortcuts() {
        return shortcuts;
    }

    public void setShortcuts(Hashtable<String, CartowebShortcut> shortcuts) {
        this.shortcuts = shortcuts;
    }

    public Integer getMinScale() {
        return minScale;
    }

    public void setMinScale(Integer minScale) {
        this.minScale = minScale;
    }

    public Integer getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(Integer maxScale) {
        this.maxScale = maxScale;
    }

    public Boolean getScaleModeDiscrete() {
        return scaleModeDiscrete;
    }

    public void setScaleModeDiscrete(Boolean scaleModeDiscrete) {
        this.scaleModeDiscrete = scaleModeDiscrete;
    }

    public Float getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(Float zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public Boolean getNoBboxAdjusting() {
        return noBboxAdjusting;
    }

    public void setNoBboxAdjusting(Boolean noBboxAdjusting) {
        this.noBboxAdjusting = noBboxAdjusting;
    }

    public Integer getRecenterMargin() {
        return recenterMargin;
    }

    public void setRecenterMargin(Integer recenterMargin) {
        this.recenterMargin = recenterMargin;
    }

    public Float getRecenterDefaultScale() {
        return recenterDefaultScale;
    }

    public void setRecenterDefaultScale(Float recenterDefaultScale) {
        this.recenterDefaultScale = recenterDefaultScale;
    }

    public String getRefMarksSymbol() {
        return refMarksSymbol;
    }

    public void setRefMarksSymbol(String refMarksSymbol) {
        this.refMarksSymbol = refMarksSymbol;
    }

    public Integer getRefMarksSymbolSize() {
        return refMarksSymbolSize;
    }

    public void setRefMarksSymbolSize(Integer refMarksSymbolSize) {
        this.refMarksSymbolSize = refMarksSymbolSize;
    }

    public Integer getRefMarksSize() {
        return refMarksSize;
    }

    public void setRefMarksSize(Integer refMarksSize) {
        this.refMarksSize = refMarksSize;
    }

    public String getRefMarksColor() {
        return refMarksColor;
    }

    /**
     * Expects a space-separated R G B triplet in a string
     * @param refMarksColor
     */
    public void setRefMarksColor(String refMarksColor) {
        this.refMarksColor = refMarksColor;
    }

    public Integer getRefMarksTransparency() {
        return refMarksTransparency;
    }

    public void setRefMarksTransparency(Integer refMarksTransparency) {
        this.refMarksTransparency = refMarksTransparency;
    }

    public String getRefMarksOrigin() {
        return refMarksOrigin;
    }

    public void setRefMarksOrigin(String refMarksOrigin) {
        this.refMarksOrigin = refMarksOrigin;
    }

    public Boolean getRefLinesActive() {
        return refLinesActive;
    }

    public void setRefLinesActive(Boolean refLinesActive) {
        this.refLinesActive = refLinesActive;
    }

    public Integer getRefLinesSize() {
        return refLinesSize;
    }

    public void setRefLinesSize(Integer refLinesSize) {
        this.refLinesSize = refLinesSize;
    }

    public Float getRefLinesFontSize() {
        return refLinesFontSize;
    }

    public void setRefLinesFontSize(Float refLinesFontSize) {
        this.refLinesFontSize = refLinesFontSize;
    }
    
    /**
     * Calling this method will generate the scalesAsString array from the 
     * scales hash, each time a call is made (to assure both collections are synchronized
     * @return the array of CartowebScale display string
     */
    public String[] getScalesAsString() {
        int s = scales == null ? 0 : scales.size();
        scalesAsString = new String[s];
        int i = 0;
        for (CartowebScale scale : scales.values()) {
            scalesAsString[i++] = scale.getDisplayString();
        }
        return scalesAsString;
    }
    
    /**
     * Calling this method will generate the scalesAsString array from the 
     * scales hash, each time a call is made (to assure both collections are synchronized
     * @return the array of CartowebScale display string
     */
    public String[] getShortcutsAsString() {
        int s = shortcuts == null ? 0 : shortcuts.size();
        shortcutsAsString = new String[s];
        int i = 0;
        for (CartowebShortcut shortcut : shortcuts.values()) {
            shortcutsAsString[i++] = shortcut.getDisplayString();
        }
        return shortcutsAsString;
    }
    
    /** Calling this method will reinitialize the list of scales
     * with the given objects. if parameters is not null.
     * INvalid CartowebScale string representation will be discarded<br/>
     * Thus, passing a vector with invalid string will result to a empty list of CartowebScale
     * A valid string representation of a scale object is:<br/>
     * id - label - value - visible <br/>
     * characters separing attributes are then: ' - '
     * @param scalesAsString
     */ 
    public void setScalesAsString(String[] scalesAsString) {
        if (scalesAsString != null) {
            StringTokenizer tok = null;
            scales = new Hashtable<String, CartowebScale>(scalesAsString.length);
            String t = null;
            for (String s : scalesAsString) {
                tok = new StringTokenizer(s, " - ");
                if (tok.countTokens() != 4) {
                    logger.warning("invalid string representation for Scale object: " + s);
                } else {
                    CartowebScale scale = new CartowebScale(tok.nextToken());
                    t = tok.nextToken();
                    scale.setLabel("null".equalsIgnoreCase(t) || t.length() == 0 ? 
                        null : 
                        t);
                    t = tok.nextToken();
                    scale.setValue("null".equalsIgnoreCase(t) || t.length() == 0 ? 
                        null : 
                        Double.parseDouble(t.trim()));
                    t = tok.nextToken();
                    scale.setVisible("null".equalsIgnoreCase(t) || t.length() == 0 ? 
                        null : 
                        Boolean.parseBoolean(t.trim()));
                    scales.put(scale.getId(), scale);
                }
            }
            logger.info("scales object re-initialized with: " + scalesAsString.length + " elements");
        } 
        this.scalesAsString = scalesAsString;
    }
    /** Calling this method will reinitialize the list of shortcuts
     * with the given objects. if parameters is not null.
     * INvalid CartowebShortcut string representation will be discarded<br/>
     * Thus, passing a vector with invalid string will result to a empty list of CartowebShortcut
     * A valid string representation of a CartowebShortcut object is:<br/>
     * id - label - bbox <br/>
     * characters separing attributes are then: ' - '
     * @param scalesAsString
     */ 
    public void setShortcutsAsString(String[] shortcutsAsString) {
        if (shortcutsAsString != null) {
            StringTokenizer tok = null;
            shortcuts = new Hashtable<String, CartowebShortcut>(shortcutsAsString.length);
            String t = null;
            for (String s : shortcutsAsString) {
                tok = new StringTokenizer(s, " - ");
                if (tok.countTokens() != 3) {
                    logger.warning("invalid string representation for Scale object: ");
                } else {
                    CartowebShortcut shortcut = new CartowebShortcut(tok.nextToken());
                    t = tok.nextToken();
                    shortcut.setLabel("null".equalsIgnoreCase(t) || t.length() == 0 ? 
                        null : 
                        t);
                    t = tok.nextToken();
                    shortcut.setBbox("null".equalsIgnoreCase(t) || t.length() == 0 ? 
                        null : 
                        Extent.getExtentFromCW3(t.trim()));
                    shortcuts.put(shortcut.getId(), shortcut);
                }
            }
            logger.info("shortcuts object re-initialized with: " + shortcutsAsString.length + " elements");
        } 
        this.shortcutsAsString = shortcutsAsString;
    }
}
