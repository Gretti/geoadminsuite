/*
 * @requires OpenLayers/Control/Measure.js
 * @include OpenLayers/Popup.js
 * @include OpenLayers/Popup/Anchored.js
 */

/**
 * Class: GeoSIE.Control.Measure
 * A subclass of OpenLayers.Control.Measure displaying measure in a popup.
 * 
 * How to use this control: 
 * - read the doc about the OpenLayers.Control.Measure
 * - include lib/GeoSIE/Control/Measure.js in viewer.html
 * - add this control to the map in Brique.js (cf addControls method): 
 * (start code)
 *        this.measureControl = new GeoSIE.Control.Measure(
 *                OpenLayers.Handler.Path, {
 *                    persist: true,
 *                    handlerOptions: {
 *                        layerOptions: {
 *                        	layer: this.vectorLayer
 *                            //renderers: renderer,
 *                            //styleMap: styleMap
 *                        }
 *                    },
 *                    displayClass: 'GeoSIEControlMeasure'
 *                    //isMeasurePartial: true
 *                }
 *        );
 * (end)
 * 
 * Inherits from:
 * - <OpenLayers.Control.Measure>
 */
GeoSIE.Control.Measure = OpenLayers.Class(OpenLayers.Control.Measure, {
	
    /**
     * Property: template
     * {String} The template for the popup content.
     */
    template: 
        '<div>' +
        '   Distance : ${measure} ${units}' +
        '</div>',	
	
    /**
     * APIProperty: precision
     * {Integer} The precision to display the distance
     * use with Number.toPrecision(). Default is 3.
     */    
    precision: 3,
        
    /**
     * APIProperty: isMeasurePartial
     * {Boolean} Display partial measure. Default is false.
     */
    isMeasurePartial: false,
    
    /**
     * APIProperty: border
     * {String} Css style for the popup border. Default is 1px solid #999999.
     */
    border: "1px solid #999999",
    
    /**
     * APIProperty: opacity
     * {Float} Popup opacity. Default is 0.6.
     */    
    opacity: 0.6,
    
	/**
	 * Property: popup
	 * {AutoSizeAnchored} Custom OpenLayers.Popup.Anchored.
	 */
	popup: null,
		
    /**
     * Constructor: GeoSIE.Control.Measure
     * 
     * Parameters:
     * options - {Object}
     * 
     * Returns:
     * {<GeoSIE.Control.Measure>}
     */
    initialize: function(options) {
    	OpenLayers.Control.Measure.prototype.initialize.apply(this, arguments);
    },

    /**
     * Method: draw
     */
    draw: function() {
    	OpenLayers.Console.log(this.CLASS_NAME + "::draw");
    	OpenLayers.Control.Measure.prototype.draw.apply(this, arguments);
	    
    	this.events.on({'measure': this.onMeasure, scope: this});
    	if(this.isMeasurePartial == true){
        	this.events.on({'measurepartial': this.onMeasurepartial, scope: this});
    	}

	    return this.div;
    },
    
    /**
	 * APIMethod: deactivate 
	 * Deactivate the control and all handlers.
	 * 
	 * Returns: 
	 * {Boolean} Successfully deactivated the control.
	 */
    deactivate: function() {
        var ret = OpenLayers.Control.Measure.prototype.deactivate.apply(this, arguments);
        if(ret) {
        	this.hidePopup();
        }	
        return ret;
    },       
    
    /**
     * Method: destroy
     */
    destroy: function() {
       this.events.unregister('measure', this, this.onMeasure);
   	   if(this.isMeasurePartial == true){
   	       this.events.unregister('measurepartial', this, this.onMeasurepartial);
   	   }
    },
    
    /**
     * Method: onMeasure
     * Relay the measure event to ShowPopup.
     * 
     * Parameters:
     * evt - {Object} Measure events
     */
    onMeasure: function(evt){
        this.showPopup(evt);
    },

    /**
     * Method: onMeasurepartial
     * Relay the measurepartial event to ShowPopup.
     * 
     * Parameters:
     * evt - {Object} Measure events
     */    
    onMeasurepartial: function(evt){
    	this.showPopup(evt);
    },
    
    /**
     * Method: showPopup
     * 
     * Parameters:
     * evt - {Object} Measure events
     */
    showPopup: function(evt){
    	var points = evt.geometry.components;
        var lastPoint = points[points.length-1];
        var lonlat = new OpenLayers.LonLat(lastPoint.x,lastPoint.y);
    	
        var context = {
        	measure: OpenLayers.Util.toFloat(evt.measure, this.precision),
        	units: evt.units
        };
        var htmlContent = OpenLayers.String.format(this.template, context);
        
	    // Destroy the old popup
	    if(this.popup != null){
	         this.popup.destroy();
	    }
	   
	    // New popup
	    // Override autosize
        AutoSizeAnchored = OpenLayers.Class(OpenLayers.Popup.Anchored, {
            'autoSize': true
        });
	    //OpenLayers.Popup.Anchored.prototype.autoSize = true;
        var id = lastPoint.id+'_popup';
	    this.popup = new AutoSizeAnchored(
	    	 id,
	    	 lonlat,
	         null,
	         htmlContent,
	         null,
	         true,
	         null
	    );
	    
	    // Add the popup 
	    this.popup.setBorder(this.border);
	    this.map.addPopup(this.popup); 		
	    OpenLayers.Util.getElement(id).style.opacity = this.opacity;
    },
    
    /**
     * Method: hidePopup
     * Hide the distance popup.
     */
    hidePopup: function(){
	   if(this.popup != null){
		   this.popup.hide();
	   }
    },

    CLASS_NAME: "GeoSIE.Control.Measure"
});