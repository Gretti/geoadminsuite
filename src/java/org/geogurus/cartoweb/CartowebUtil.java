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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Utility class for cartoweb objects 
 * @author nicolas
 */
public class CartowebUtil {
    /** this class message, set during a Utility method call (like Layer building for instance) */
    private String message;
    
    public CartowebUtil(){
        
    }
    
    /**
     * Builds a CartowebLayer from given json representation, setting all children
     * layers found in the structure.
     * @see CartowebLayer.getExtTreeJson() for expected json structure
     * @param json the json structure describing the layers to build (typically coming
     * from an Ext TreePanel representing the layers
     * @return a CartowebLayer or null if structure is invalid. see message in this case.
     * 
     */
    public CartowebLayer buildLayerFromJson(String json) {
        if (json == null) {
            message = "buildLayerFromJson: null input json structure";
            return null;
        }
        try {
            JSONObject jsonObj = new JSONObject(json);
            if (jsonObj.getString("id") == null) {
                message = "missing required id attribute in first object";
                return null;
            }
            return buildLayerFromJson(jsonObj);
            
        } catch (JSONException jse) {
            jse.printStackTrace();
            message = jse.getMessage();
            return null;
        }
    }
    
    /**
     * Builds a CartowebLayer from given json representation, setting all children
     * layers found in the structure.
     * @see CartowebLayer.getExtTreeJson() for expected json structure
     * @param json the json structure describing the layers to build (typically coming
     * from an Ext TreePanel representing the layers, after a JSONObject was built with this string)
     * @return a CartowebLayer or null if structure is invalid. see message in this case.
     * 
     */
    public CartowebLayer buildLayerFromJson(JSONObject jsonLayer) {
        if (jsonLayer == null) {
            message = "buildLayerFromJson: null input json Object";
            return null;
        }
        CartowebLayer layer = null;
        try {
            String id = jsonLayer.optString("id");
            if (id.length() == 0) {
                message = "missing required id attribute in first object";
                return null;
            }
            
            if ("cw_layer_tree".equals(id)) {
                // given struct is a TreePanel containing the root layer, extract the root
                // this struct must contain one children representing the CW root layer (containing all others)
                JSONArray children = jsonLayer.optJSONArray("children");
                if (children == null || children.length() != 1 ||
                        children.getJSONObject(0) == null || ! children.getJSONObject(0).optString("id").equals("root")) {
                    message = "invalid json strucutre: cannot find a children with id=root";
                    return null;
                }
                jsonLayer = children.getJSONObject(0);
                id = "root";
            }
            layer = new CartowebLayer(id);
            JSONObject cwAttributes = jsonLayer.optJSONObject("cwattributes");
            if (cwAttributes == null) {
                message = "missing valid cwattributes in current object (id: " + id + ")";
                return null;
            }
            // sets layer's internal attributes
            layer.setClassName(cwAttributes.optString("className"));
            layer.setMsLayer(cwAttributes.optString("msLayer"));
            layer.setLabel(cwAttributes.optString("label"));
            layer.setIcon(cwAttributes.optString("icon"));
            layer.setLink(cwAttributes.optString("link"));
            layer.setChildren(cwAttributes.optString("children"));
            layer.setSwitchId(cwAttributes.optString("switchId"));
            layer.setAggregate(cwAttributes.optBoolean("aggregate"));
            layer.setRendering(cwAttributes.optString("rendering"));
            layer.setMdMinScale(cwAttributes.optString("mdMinScale"));
            layer.setMdMaxScale(cwAttributes.optString("mdMaxScale"));
            
            // gets children layers
            JSONArray children = jsonLayer.optJSONArray("children");
            if (children != null) {
                for (int i = 0; i < children.length(); i++) {
                    CartowebLayer l = buildLayerFromJson(children.getJSONObject(i));
                    if (l != null) {
                        layer.addLayer(l);
                    }
                }
            } else {
                //System.out.println("layer: " + id + " is a leaf layer");
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
            message = jse.getMessage();
            return null;
        }
        return layer;
    }

    public String getMessage() {
        return message;
    }

}
