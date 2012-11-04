/*
 * @requires OpenLayers/Control/NavigationHistory.js
 * @include lib/GeoSIE/Control/ZoneSelector.js
 */

/**
 * Class: GeoSIE.Control.NavigationHistory
 * A subclass of OpenLayers.Control.NavigationHistory enabling
 * selecting a zone when restoring a state.
 * 
 * Inherits from:
 * - <OpenLayers.Control.NavigationHistory>
 */
GeoSIE.Control.NavigationHistory = OpenLayers.Class(OpenLayers.Control.NavigationHistory, {
	
    /**
     * Property: zoneSelectorControl
     * {<GeoSIE.Control.ZoneSelector>} A reference to the zone selector
     * control.
     */
    zoneSelectorControl: null,
    
    /**
     * Method: getState
     * Get the current state.
     *
     * Returns:
     * {Object} An object representing the current state.
     */
    getState: function() {
        var superclass = OpenLayers.Control.NavigationHistory.prototype;
        return OpenLayers.Util.extend(
            superclass.getState.apply(this, arguments),
            {zone: this.zoneSelectorControl.zone}
        );
    },

    /**
     * Method: restore
     * Restore a state.
     *
     * Parameters:
     * state - {Object} An object representing the state to restore.
     */
    restore: function(state) {
        if(this.zoneSelectorControl.zone !== state.zone) {
            this.zoneSelectorControl.selectZone(state.zone, {zoomTo: false});
        }
        var superclass = OpenLayers.Control.NavigationHistory.prototype;
        superclass.restore.apply(this, arguments);
    },
    
    CLASS_NAME: 'GeoSIE.Control.NavigationHistory'
});
