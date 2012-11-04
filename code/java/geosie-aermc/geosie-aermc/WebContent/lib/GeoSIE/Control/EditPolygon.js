/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Handler/Polygon.js 
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Geometry/Point.js
 * @include OpenLayers/Geometry/Polygon.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 */


/** 
 * Class: GeoSIE.Control.EditPolygon
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.EditPolygon = OpenLayers.Class( OpenLayers.Control, {

	/**
	 * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this control.
	 */
    EVENT_TYPES: ['creationpolygon'],
    
    /**
     * Property: type
     */
    type: OpenLayers.Control.TYPE_TOOL,
    
    /**
     * APIProperty: layer
     * {<OpenLayers.Layer.Vector>} The vector layer used by this control to draw features
     */
    layer: null,
    
    /**
     * Property: handler
     * {<OpenLayers.Control.DrawFeature>} The OpenLayers.Handler to use for drawing a Polygon. 
     */
    handler: null,
    
    /**
     * Constructor: GeoSIE.Control.EditPolygon
     * Constructor for a polygon marker editing tool 
     * 
     * Parameters:
     * options - {Object} 
     * 
     * Returns: 
     * {<GeoSIE.Control.EditPolygon>}
     */
    initialize: function(options) {
        this.EVENT_TYPES =
        	GeoSIE.Control.EditPolygon.prototype.EVENT_TYPES.concat(
            OpenLayers.Control.prototype.EVENT_TYPES
        );
        OpenLayers.Control.prototype.initialize.apply(this, [options]);
    },

    /**
     * Method: draw
     * Draws a Polygon Feature on the vector layer.
     * Actually, it will initiate an handler to do the job of listening to the clicks and drawings on the map. 
     * When the polygon is validated (double click), the handler finalizes the shape and raise the featureadded event.  
     * See the onFeatureadded method.
     */      
    draw: function() {
    	if (this.layer !== null){
    		this.handler = new OpenLayers.Control.DrawFeature(
    				this.layer,
    				OpenLayers.Handler.Polygon,
    				{	
    					multi: false
    				}
    		); 
    		this.handler.events.on({'featureadded': this.onFeatureadded, scope: this});
    		this.handler.setMap(this.map);
    		//GeoSIE.Brique.map.addControl(this.handler);    		
    	}
    },

    /**
     * Callback: onFeatureadded
     * Is executed when the drawing of a polygon is finished. 
     * The feature passed as a parameter to this function is added to the vector layer. 
     * A creationpolygon event is also immediatly retriggered with the feature as a parameter.   
     * 
     * Parameters: 
     * feature - {OpenLayers.Feature}
     */        
    onFeatureadded: function(feature){
    	OpenLayers.Console.dir(feature);
		//this.layer.addFeatures([feature], {silent: true});
    	this.events.triggerEvent('creationpolygon', feature);
    },
    
    CLASS_NAME: 'GeoSIE.Control.EditPolygon'
});