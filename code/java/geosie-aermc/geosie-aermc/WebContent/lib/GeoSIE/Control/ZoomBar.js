/*
 * @requires OpenLayers/Control/PanZoomBar.js
 */


/**
 * Class: GeoSIE.Control.ZoomBar
 * The ZoomBar is a visible control that represents
 *    a ladder (slider) letting user to switch to another zoom level
 *    
 * Inherits from:
 *  - <OpenLayers.Control.PanZoomBar>
 */
GeoSIE.Control.ZoomBar = OpenLayers.Class(OpenLayers.Control.PanZoomBar, {
    
    /**
     * Property: zoomBarDiv
     * {DOMElement} The DOM element where to put the zoom bar (ladder with + and -).
     */
    zoomBarDiv: null,

    /**
     * Constructor: GeoSIE.Control.ZoomBar
     * 
     * Returns:
     * {<GeoSIE.Control.ZoomBar>}
     */ 
    initialize: function() {
        OpenLayers.Control.PanZoomBar.prototype.initialize.apply(this, arguments);
    },
    
    /**
    * Method: draw 
    * Draw the control.
    * 
    * Parameters:
    * px - {<OpenLayers.Pixel>} 
    */
    draw: function(px){
        // initialize our internal div
        OpenLayers.Control.prototype.draw.apply(this, arguments);
        px = this.position.clone();

        // place the controls
        this.buttons = [];

        var sz = new OpenLayers.Size(18,18);
        this.div.innerHTML = "";

        this._addButton("zoomin", "zoom-plus-mini.png", new OpenLayers.Pixel(5, 5), sz);
        px = this._addZoomBar(new OpenLayers.Pixel(5, 25));
        this._addButton("zoomout", "zoom-minus-mini.png", px, new OpenLayers.Size(18,22));

        return this.div;
    }, 
    
    CLASS_NAME: "GeoSIE.Control.ZoomBar"
});