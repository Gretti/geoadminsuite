/*
 * @requires OpenLayers/Control/Attribution.js
 */

/**
 * Class: GeoSIE.Control.Copyrights
 * A subclass of OpenLayers.Control.Attribution displaying layers copyright.
 * 
 * TODO : eliminateDuplicates should be available by default in OL http://trac.osgeo.org/openlayers/ticket/2266
 * 
 * Inherits from:
 * - <OpenLayers.Control.Attribution>
 */
GeoSIE.Control.Copyrights = OpenLayers.Class(OpenLayers.Control.Attribution, {
    
    /**
     * APIProperty: prefix
     * {String}  
     */
    prefix: "Sources : ",

    /**
     * APIProperty: eliminateDuplicates
     * {Boolean} shall already present attribution strings be eliminated when other layers have the same attribution string?
     */
    eliminateDuplicates: true,

    /**
     * Constructor: GeoSIE.Control.Copyrights
     * 
     * Parameters:
     * options - {Object}
     * 
     * Returns:
     * {<GeoSIE.Control.Copyrights>}
     */
    initialize: function(options) {
    	OpenLayers.Control.Attribution.prototype.initialize.apply(this, arguments);
    },

    /**
     * Method: updateAttribution
     * Update attribution string.
     */
    updateAttribution: function() {
        var attributions = [];
        if (this.map && this.map.layers) {
            for(var i=0, len=this.map.layers.length; i<len; i++) {
                var layer = this.map.layers[i];
                if (layer.attribution && layer.getVisibility()) {
                    // add attribution if duplicates shall be ignored generally
                    // or if the current attribution string is unique
                    if (!this.eliminateDuplicates || OpenLayers.Util.indexOf(attributions, layer.attribution) === -1) {
                        attributions.push( layer.attribution );
                    }
                }
            } 
            this.div.innerHTML = this.prefix+attributions.join(this.separator);
        }
    },
                               
    CLASS_NAME: "GeoSIE.Control.Copyrights"
});