/*
 * DataStoreProperties.java
 *
 * Created on 16 aout 2005, 17:07
 */

package org.kaboum.server.utils;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kaboum.util.KaboumFeatureModes;
import org.opengis.feature.simple.SimpleFeature;

/**
 * This bean maps Kaboum features properties related to a datastore (see GeoTools API):
 * name, attributes, tooltip expression, etc.
 *<p>
 * see Kaboum API for a complete description of features properties
 *</p>
 * @author Nicolas Ribot
 */
public class DataStoreProperties {
    /** the maximum number of features to retrieve on each client request */
    private int maxFeatures;
    /** The name of the DataStore if it is a database */
    private String databaseName;
    /** the name of the database schema */
    private String schemaName;
    /** the name of the datastore itself (file, database table name) */
    private String name;
    /** the name of the user used to connect to this datastore, if required by the datastore type */
    /** the name of the property storing geographic objects, if required by the datastore type */
    private String geoColumn;
    
    
    /**
     * the tooltip expression for features in this datastore.<br>
     * the syntax for this expression is to put attribute name into square brackets
     * they will be replaced by their values at runtime. Any other characters will be
     * kept unchanged to form a tooltip string. Ex:
     * roads name: [road_name], length: [road_length]
     * special keywords can be used to get feature id, feature surface (polygonal features)
     * and feature perimeter (polygonal and linear features).
     */
    private String toolTipExpression;
    
    /** Creates a new instance of DataStoreProperties */
    public DataStoreProperties() {
    }
    
    /**
     * Returns a Hashmap containing name of Datastore parameter as key and value as value.
     * This class parses a DATASTORE_PARAMS property set in the Kaboum Server Properties file
     * and builds a Hashmap from it
     *@param kaboumParams - the datastore parameters String as read in the properties file, under the <className>_DATASTORE_PARAMS key
     *@return a Hashmap containing name/value pairs, or null if input string is null or empty
     */
    public static HashMap getDataStoreParameters(String kaboumParams) {
        if (kaboumParams == null || kaboumParams.length() == 0) {
            return null;
        }
        StringTokenizer tok = new StringTokenizer(kaboumParams, ",");

        // test if there is only one parameter
        if (tok.countTokens() == 0) {
            String s = kaboumParams + ",";
            tok = new StringTokenizer(s, ",");
        }
        
        StringTokenizer tok2 = null;
        HashMap res = new HashMap(tok.countTokens());
        
        while (tok.hasMoreTokens()) {
            tok2 = new StringTokenizer(tok.nextToken(), "=");
            
            if (tok2.countTokens() != 2) {
                // invalid parameter format: not a <name>:<value> pair,
                //skip it
                continue;
            }
            // now, we know will can call nextToken twice
            res.put(tok2.nextToken(), tok2.nextToken());
        }
        return res;
    }
    
    
    /**
     * Gets the list of attributes that will be necessary to get from the datastore:
     * geometric one and those involved in the tooltip.<br>
     * This list is built from the geoColumn and all attributes present in the toolTipExpression
     * @return a String array containing attributes names.
     */
    public String[] getAttributesList() {
        // the geoColumn
        Vector vec = new Vector(1);
        
        if (toolTipExpression != null && toolTipExpression.length() > 0) {
            Pattern p = Pattern.compile("\\[[a-zA-Z_0-9| +]+\\]");
            Matcher m = p.matcher(toolTipExpression);
            while (m.find()) {
                String g = m.group();
                vec.add(g.substring(1, g.length() - 1) );
            }
        }
        vec.add(geoColumn);
        
        String[] res = new String[vec.size()];
        vec.copyInto(res);
        
        return res;
    }
    
    /**
     * Gets the list of attributes that will be necessary to get from the datastore:
     * geometric one and those involved in the tooltip.<br>
     * This list is built from the geoColumn and all attributes present in the toolTipExpression
     * @return a String array containing attributes names.
     * @param toolTipExpression The tooltip expression to parse to get mandatory attribute names.
     */
    public static String[] getAttributesList(String toolTipExpression) {
        if (toolTipExpression == null) return null;
        
        // the geoColumn
        Vector vec = new Vector(1);
        
        if (toolTipExpression != null && toolTipExpression.length() > 0) {
            Pattern p = Pattern.compile("\\[[a-zA-Z_0-9\\$| +]+\\]");
            Matcher m = p.matcher(toolTipExpression);
            while (m.find()) {
                String g = m.group();
                vec.add(g.substring(1, g.length() - 1) );
            }
        }
        //vec.add(geoColumn);
        
        String[] res = new String[vec.size()];
        vec.copyInto(res);
        
        return res;
    }
    
    /** returns a String representing the tooltip corresponding to the passed feature
     * by replacing each column name of the tooltipExpression by its actual value taken from
     * the feature itself
     *@param toolTip the ToolTipExpression string, containing placeholders for parameters values
     *@param f the feature containing actual values for attributes needed to build the tooltip
     *@param attList a string array of attribute placeholders used to make the replacement
     *@return the tooltipExpression string where column placeholders are replaced by their actual values.
     *returns an empty String if feature toolTipExpression is null
     */
    public static String getActualToolTip(String toolTip, SimpleFeature f, String[] attList) {
        if (f == null || toolTip == null || attList == null) {
            return "";
        }
        String res = toolTip;
        String replacement = null;
        String regex = null;
        
        for (int i = 0; i < attList.length; i++) {
            if (KaboumFeatureModes.K_TOOLTIP_FEATURE_ID_KEY.equals(attList[i])) {
                replacement = f.getID();
                //regex = "\\[" + KaboumFeatureModes.K_TOOLTIP_FEATURE_ID_KEY.replace("$", "\\$") + "\\]";
                // java 1.4 hack:
                regex = "\\[\\$\\$FID\\$\\$\\]";
            } else {
                replacement = "" + f.getAttribute(attList[i]);
                regex = "\\[" + attList[i] + "\\]";
            }
            // don't generate "null" but instead an empty string
            replacement = replacement.equalsIgnoreCase("null") ? "" : replacement;
            res = res.replaceAll(regex, replacement);
        }
        return res;
    }
}
