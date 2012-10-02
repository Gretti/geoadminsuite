/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Handler/Point.js 
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Geometry/Point.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 */


/**
 * Class: GeoSIE.Control.EditPoint
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.EditPoint = OpenLayers.Class( OpenLayers.Control, {

	/**
	 * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this control.
	 */
    EVENT_TYPES: ['creationpoint'],
    
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
     * {<OpenLayers.Control.DrawFeature>} The OpenLayers.Handler to use for drawing a Point.  
     */
    handler: null,
    
    /**
     * Constructor: GeoSIE.Control.EditPoint
     * Constructor for a point marker editing tool
     * 
     * Parameters:
     * options - {Object} 
     * 
     * Returns: 
     * {<OpenLayers.Control.EditPoint>}
     */
    initialize: function(options) {
        this.EVENT_TYPES =
        	GeoSIE.Control.EditPoint.prototype.EVENT_TYPES.concat(
            OpenLayers.Control.prototype.EVENT_TYPES
        );
        OpenLayers.Control.prototype.initialize.apply(this, [options]);
    },

    /**
     * Method: draw
     * Draws a Point Feature on the vector layer.
     * Actually, it will initiate an handler to do the job of listening to the clicks and drawings on the map. 
     * When the path is point (double click), the handler raise the featureadded event. 
     * See the onFeatureadded method.
     */    
    draw: function() {
    	if (this.layer !== null){
    		this.handler = new OpenLayers.Control.DrawFeature(
    				this.layer,
    				OpenLayers.Handler.Point,
    				{	
    					multi: false
    					// to custom the point style drawing 
    					/*handlerOptions: {
    						style: "default", // this forces default render intent
    						layerOptions: {
    					 		styleMap: this.layer.styleMap //.styles['default']
    						}
    					}*/
    				}
    		); 
    		this.handler.events.on({'featureadded': this.onFeatureadded, scope: this});
    		this.handler.setMap(this.map);
    		//GeoSIE.Brique.map.addControl(this.handler);
    	}
    },
    
    /**
     * Method: onFeatureadded
     * Is executed when the drawing of a point is finished. 
     * The feature passed as a parameter to this function is added to the vector layer. 
     * A creationpoint event is also immediatly retriggered with the feature as a parameter.   
     * 
     * Parameters: 
     * feature - {OpenLayers.Feature}
     */    
    onFeatureadded: function(feature){
    	OpenLayers.Console.dir(feature);
    	//this.layer.addFeatures([feature], {silent: true});
    	this.events.triggerEvent('creationpoint', feature);
    },
    
    CLASS_NAME: 'GeoSIE.Control.EditPoint'
    
});