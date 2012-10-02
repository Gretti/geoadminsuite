/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Geometry/LineString.js
 * @include OpenLayers/Control/DragFeature.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 */


/**
 * Class: GeoSIE.Control.DragFeature
 * A control to drag feature.
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.DragFeature = OpenLayers.Class(OpenLayers.Control, {
    
	/**
	 * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this control.
     * 
     * Supported control event types (in addition to those from <OpenLayers.Control>):
     * deplacement - Triggered before a feature is added to the layer.
	 */	
    EVENT_TYPES: ['deplacement'],

    /**
     * Property: dragControl
     * {<OpenLayers.Control.DragFeature>} DragFeature control.
     */
    dragControl : null,

    /**
     * APIProperty: targetLayerName
     * {String} The layer name. 
     */    
    targetLayerName : null,

    /**
     * Property: featureLayer
     * {<OpenLayers.Layer>} The layer with the target layer name.
     */    
    featureLayer : null,

    /**
     * Property: deplacements
     * {Array(String)} The features id. 
     */    
    deplacements : [],

    /**
     * Property: destinations
     * {Array(String)} The features id. 
     */    
    destinations : [],

    /**
     * Property: lastFeature
     */    
    lastFeature : null,

    /**
     * Constructor: GeoSIE.Control.DragFeature
     * 
     * Parameters:
     * options - {Object} 
     * 
     * Returns:
     * {<GeoSIE.Control.DragFeature>}
     */
    initialize: function(options) {
        this.EVENT_TYPES =
        	GeoSIE.Control.DragFeature.prototype.EVENT_TYPES.concat(
            OpenLayers.Control.prototype.EVENT_TYPES
        );
        OpenLayers.Control.prototype.initialize.apply(this, [options]);
        this.dragControl = new OpenLayers.Control.DragFeature(this.featureLayer);
    },

    /**
     * APIMethod: destroy
     */
    destroy: function() {
        this.featureLayer = null;
        this.dragControl.destroy();
        OpenLayers.Control.prototype.destroy.apply(this, []);
    },

    /**
     * APIMethod: activate
     * Activate the control and the feature handler.
     *
     * Returns:
     * {Boolean} Successfully activated the control and feature handler.
     */
    activate: function() {
        this.featureLayer = this.map.getLayersByName(this.targetLayerName)[0];
        if (!this.featureLayer) {
            return false;
        }
        this.dragControl = new OpenLayers.Control.DragFeature(this.featureLayer);
        this.dragControl.onStart = this.onStart;
        this.dragControl.onComplete = this.onComplete;
        this.dragControl.setMap(this.map);
        this.dragControl.handlers.feature.layer = this.featureLayer;
        this.dragControl.wrapper = this;
        this.dragControl.activate();
        return OpenLayers.Control.prototype.activate.apply(this, arguments);
    },

    /**
     * APIMethod: deactivate
     * Deactivate the control and all handlers.
     *
     * Returns:
     * {Boolean} Successfully deactivated the control.
     */
    deactivate: function() {
        var deactivated = false;
        // the return from the controls is unimportant in this case
        if(OpenLayers.Control.prototype.deactivate.apply(this, arguments)) {
            this.dragControl.deactivate();                    
            deactivated = true;
        }
        return deactivated;
    },
   

    /**
     * Method: setMap
     * Set the map property for the control and all handlers.
     *
     * Parameters:
     * map - {<OpenLayers.Map>} The control's map.
     */
    setMap: function(map) {
        this.dragControl.setMap(map);
        OpenLayers.Control.prototype.setMap.apply(this, arguments);
    },

    /**
     * Method: onStart
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The feature that is about to be
     *     dragged.
     * pixel - {<OpenLayers.Pixel>} The pixel location of the mouse.
     */
    onStart : function(feature, pixel) {
        var geom = document.getElementById(feature.geometry.id);
        if (!geom.base)
            geom.base = feature.geometry.clone();
        this.wrapper.lastFeature = feature; 
    },

    /**
     * Method: onComplete
     * Draw a line between the point and the new point.
     * Trigger the event deplacement.
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The feature that was dragged.
     * pixel - {<OpenLayers.Pixel>} The pixel location of the mouse.
     */
    onComplete : function(feature, pixel) {
        if (!feature){
            feature = this.wrapper.lastFeature;
        }
        if (!feature.fid){
        	this.wrapper.events.triggerEvent('deplacement', feature);
        	return;
        }
        var geom = document.getElementById(feature.geometry.id);
        
        var point = geom.base;//new OpenLayers.Geometry.Point(geom.baseX,geom.baseY);

        if (!this.wrapper.deplacements[feature.fid])
            this.wrapper.deplacements[feature.fid]= point;
        this.wrapper.destinations[feature.fid]= this.layer.getFeatureById(feature.id).geometry;
        /*var lines = [];
        for(var id in this.wrapper.deplacements){
            var pointList = [];
            var p = this.wrapper.deplacements[id];
            var current = this.wrapper.destinations[id];
            pointList.push(p);
            pointList.push(current);
            var vect = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(pointList),null,null);
            vect.mbNoMouseEvent = true;
            lines.push(vect);
            var newGeom = new OpenLayers.Feature.Vector(current.clone(),null,null);
            newGeom.mbNoMouseEvent = true;
            lines.push(newGeom);
        }
        vectorLayer.addFeatures(lines);*/
        geom.base = null;
        this.wrapper.events.triggerEvent('deplacement', feature);
    },

    CLASS_NAME: "GeoSIE.Control.DragFeature"
});