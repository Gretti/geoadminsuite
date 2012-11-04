/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Events.js
 */


/**
 * Class: GeoSIE.Control.ScaleCombo
 * A combo scales control.
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.ScaleCombo = OpenLayers.Class( OpenLayers.Control, {
    
    /**
     * Property: combo
     * {DOMElement} The DIV DOMElement containing the scale combo.
     */
    combo: null,
    
    /**
     * Constructor: GeoSIE.Control.ScaleCombo
     * 
     * Parameters:
     * options - {Object} Options for control.
     * 
     * Returns: 
     * {<GeoSIE.Control.ScaleCombo>} 
     */
    initialize: function(options) {
        OpenLayers.Control.prototype.initialize.apply(this, arguments);
    },

    /**
     * Method: destroy
     */
    destroy: function() {
        if (this.map) {
            this.map.events.unregister('zoomend', this, this.redraw);
        }
        OpenLayers.Control.prototype.destroy.apply(this, arguments);
    },

    /**
     * Method: draw
     * Initialize control.
     * 
     * Returns: 
     * {DOMElement} A reference to the DIV DOMElement containing the control.
     */    
    draw: function() {
        OpenLayers.Control.prototype.draw.apply(this, arguments);
        
        this.div.innerHTML = "Echelle : ";
        
        this.combo = document.createElement("select");
        
        for (var i in this.map.scales) {
            var option = document.createElement("option");
            var scale = this.map.scales[i];
            option.value = scale;
            option.appendChild(document.createTextNode(this.formatScale(scale)));
            this.combo.appendChild(option);
        }
        this.div.appendChild(this.combo);
        
        OpenLayers.Event.observe(this.combo, "change", OpenLayers.Function.bind(function(evt) {
            this.map.zoomToScale(OpenLayers.Event.element(evt).value, true);
        }, this));
        
        return this.div;
    },
   
    /**
     * Method: updateScale  
     * Update the scale value display in the combo.
     * 
     * Parameters:
     * evt - {Event}
     */
    updateScale: function(evt) {
        this.combo.value = Math.round(this.map.getScale());
    },
    
    /**
     * Method: formatScale
     * Formats the scale to be used as displayField in the combo
     *      to render something like 1:5'000.
     *
     *      In the future, we could imagine a mapfish.widgets.Format.scale method
     *      with args like for Ext.Util.Format.date.
     *
     * Parameters:
     * value - {Float} scale.
     *
     * Returns:
     * {String} The formated scale.
     */
    formatScale: function(value) {
        value = String(Math.round(value));
        var rgx = /(\d+)(\d{3})/;

        while (rgx.test(value)) {
            value = value.replace(rgx, '$1' + "'" + '$2');
        }

        return '1:' + value;
    },
    

    /** 
     * Method: setMap
     */
    setMap: function() {
        OpenLayers.Control.prototype.setMap.apply(this, arguments);
        this.map.events.register('zoomend', this, this.updateScale);
    },
    
    CLASS_NAME: 'GeoSIE.Control.ScaleCombo'
});
