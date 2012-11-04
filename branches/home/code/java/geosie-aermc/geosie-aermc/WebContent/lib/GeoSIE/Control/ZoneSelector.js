/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Events.js
 * @include OpenLayers/Projection.js
 */

/**
 * Class: GeoSIE.Control.ZoneSelector
 * A control enabling switching from one geographic zone to another,
 * reconfiguring the map if the coordinates system changes.
 * See http://trac.osgeo.org/openlayers/browser/sandbox/elemoine/reproject/examples/reproject-map.html?rev=9817
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.ZoneSelector = OpenLayers.Class(OpenLayers.Control, {
    
	/**
     * Constant: EVENT_TYPES
     * {Array({String}) The event types supported by this control.
     */
    EVENT_TYPES: ["zonechanged"],

    /**
     * Property: autoActivate
     * {Boolean} Activate the control when it is added to the map,
     * defaults to true.
     */
    autoActivate: true,

    /**
     * APIProperty: config
     * {Array({Object})} An array of objects representing zones.
     */
    config: null,

    /**
     * Property: zone
     * {Object} The current zone.
     */
    zone: null,

    /**
     * Property: wgs84
     * {OpenLayers.Projection} An EPSG:4326 projection instance.
     */
    wgs84: null,

    /**
     * Constructor: GeoSIE.Control.ZoneSelector
     *
     * Parameters:
     * options - {Object} The options provided to the control, should
     *     include a "config" option referencing the zone config.
     *     
     * Returns:
     * (<GeoSIE.Control.ZoneSelector>)    
     */
    initialize: function(options) {
        this.EVENT_TYPES =
        	GeoSIE.Control.ZoneSelector.prototype.EVENT_TYPES.concat(
            OpenLayers.Control.prototype.EVENT_TYPES
        );
        this.wgs84 = new OpenLayers.Projection("EPSG:4326");
        this.div = OpenLayers.Util.getElement("zones");
        OpenLayers.Control.prototype.initialize.apply(this, arguments);
    },

    /**
     * Method: activate
     * Activate the control.
     */
    activate: function() {
        var ret = OpenLayers.Control.prototype.activate.call(this);
        if(ret) {
            this.map.events.on({
                movestart: this.onMoveStart,
                scope: this
            });
        }
        return ret;
    },

    /**
     * Method: deactivate
     * Deactivate the control.
     */
    deactivate: function() {
        var ret = OpenLayers.Control.prototype.deactivate.call(this);
        if(ret) {
            this.map.events.un({
                movestart: this.onMoveStart,
                scope: this
            });
        }
        return ret;
    },

    /**
     * Method: draw
     */    
    draw: function() {
    	// if you display the select zone
    	if(this.div){
            OpenLayers.Control.prototype.draw.apply(this, arguments);

            this.div.innerHTML = "Localisation : ";
            
            this.combo = document.createElement("select");
            
            for(i=0,len=this.config.length; i<len; i++) {
                var zone = this.config[i];
                var option = document.createElement("option");
                option.value = zone.id; 
                //option.text = zone.name;
                option.appendChild(document.createTextNode(zone.name));
                this.combo.appendChild(option);
            }
            this.div.appendChild(this.combo);
            
            OpenLayers.Event.observe(this.combo, "change", OpenLayers.Function.bind(function(evt) {
            	var id=OpenLayers.Event.element(evt).value;
            	this.selectZone(id,{select:false});
            }, this));
    	}        
        return this.div;
    },    
    
    /**
     * Method: onMoveStart
     * Called on each "movestart" event from the map.
     *
     * Parameters:
     * evt - {Object} An object with the following properties:
     * * lonlat {OpenLayers.LonLat} The lonlat the map is being
     *   centered to, null if the move action is a zoom
     * * zoom {Number} The zoom level, null if the move
     *   action is a pan
     * * zoomChanged {Boolean} True if zoom is about to change
     * * centerChanged {Boolean} True if center is about to change
     */
    onMoveStart: function(evt) {
        // don't do anything if zoom hasn't changed
        if(!evt.zoomChanged) {
            return;
        }

        var lonlat = evt.lonlat === null ?
            this.map.getCenter() : evt.lonlat.clone();

        var zone = this.selectZoneForLonLat(lonlat, evt.zoom);
        if(this.zone !== zone) {
            var src = this.map.getProjectionObject();
            var dst = new OpenLayers.Projection(zone.options.projection);
            this.selectZone(zone, {zoomTo: false});
            evt.lonlat = lonlat.transform(src, dst);
            evt.centerChanged = true;
        }
    },

    /**
     * Method: selectZoneForLonLat
     * 
     * Parameters:
     * lonlat - {OpenLayers.LonLat} The center the map is being
     *     moved to, expressed in the map's current projection.
     * zoom - {OpenLayers.LonLat} The zoom level the map is being
     *     moved to.
     *
     * Returns:
     * {Object} An object representing the selected zone.
     */
    selectZoneForLonLat: function(lonlat, zoom) {
        var resolution = this.map.getResolutionForZoom(zoom);
        var extent = this.map.calculateBounds(lonlat, resolution);
        extent.transform(this.map.getProjectionObject(), this.wgs84);
        return this.selectZoneForExtent(extent);
    },

    /**
     * Method: selectZoneForExtent
     * Return the best zone for a given extent.
     *
     * Parameters:
     * extent - {OpenLayers.Bounds} The extent the map is being
     *     moved to, expressed in EPSG:4326.
     *
     * Returns:
     * {Object} An object representing the selected zone.
     */
    selectZoneForExtent: function(extent) {
        var i, len, z, zone;
        for(i=0,len=this.config.length; i<len; i++) {
            z = this.config[i];
            if(this.compareToZoneExtent(z, extent)) {
                zone = z;
                break;
            }
        }

        return zone;
    },

    /**
     * Method: compareToZoneExtent
     * Returns true if the zone is appropriate for the extent, false
     * otherwise.
     *
     * Parameters:
     * zone - {Object} The zone the extent is compared to.
     * extent - {OpenLayers.Bounds} The extent, expressed in EPSG:4326.
     *
     * Returns:
     * {Boolean} true if the zone is appropriate for the extent, false
     * otherwise.
     */
    compareToZoneExtent: function(zone, extent) {
        if (!zone.maxExtentWGS84) {
            var src = new OpenLayers.Projection(zone.options.projection);
            zone.maxExtentWGS84 = zone.options.maxExtent
                .clone().transform(src, this.wgs84);
        }
        var z = zone.maxExtentWGS84;

        var intersection = extent.getIntersection(z);
        return (intersection && z.containsBounds(extent) ||
            (intersection && 
            (intersection.getWidth() * 2 > extent.getWidth() ||
             intersection.getHeight() * 2 > extent.getHeight())));
    },

    /**
     * APIMethod: selectZone
     * Selects a new zone.
     *
     * Parameters:
     * zone - {Object|String} The zone object or the zone name.
     * options - {Object} An optional object with these properties:
     * - zoomTo - {Boolean} whether the map should be centered to the
     *     zone's extent, defaults to true
     * - select - {Boolean} select the zone in the menu     
     *
     * Returns:
     * {Object} The zone.
     */
    selectZone: function(zone, options) {
        options = options || {};
        zone = typeof zone === "string" ?
            this.getZoneById(zone) : zone;

        if(this.active && zone !== null) {
        	OpenLayers.Console.log(this.CLASS_NAME + "::selectZone - init map selectiozone");

            var opts = zone.options;
            // get the source projection before resetting the map
            // (and changing its projection)
            var srcProjCode = this.zone ?
                this.zone.options.projection :
                this.map.projection; // do not use getProjection
                                     // here as we don't know if
                                     // the map has layers
            // reset map
            this.map.setOptions(opts);
            for(var i=0,len=this.map.layers.length; i<len; i++) {
                this.map.layers[i].addOptions(opts);
            }
            // reproject map.layerContainerOrigin, in case the next
            // call to moveTo does not change the zoom level and 
            // therefore centers the layer container
            if(this.map.layerContainerOrigin) {
                var srcProj = new OpenLayers.Projection(srcProjCode);
                var dstProj = new OpenLayers.Projection(
                    zone.options.projection
                );
                this.map.layerContainerOrigin.transform(srcProj, dstProj);
            }
            this.zone = zone;
            
            // Select the zone in the select zone (NavigationHistory, init)
            if(options.select !== false){
            	if(this.combo){
                    var optSelect = this.combo.options;
                    for (var j = 0; j < optSelect.length; ++j) {
                      var opt = optSelect[j];
                      if (opt.value == this.zone.id) {
                    	  opt.selected = true;
                        break;
                      }
                    }
            	}
            }
            
            this.events.triggerEvent("zonechanged", {zone: zone});
//            OpenLayers.Console.log("Zone selector triggerEvent zonechanged");
            if(options.zoomTo !== false) {
                this.map.zoomToExtent(zone.extent || zone.options.maxExtent);
            }
        }
        return zone;
    },

    /**
     * Method: getZoneByName
     * Get the zone object associated with a zone name.
     *
     * Parameters:
     * name - {String} The zone name.
     *
     * Returns:
     * {Object} The object representing the zone.
     */
    getZoneByName: function(name) {
        var i, len, zone;
        for(i=0,len=this.config.length; i<len; i++) {
            zone = this.config[i];
            if(zone.name === name) {
                break;
            }
        }
        return zone;
    },

    /**
     * Method: getZoneById
     * Get the zone object associated with a zone id.
     *
     * Parameters:
     * name - {String} The zone id.
     *
     * Returns:
     * {Object} The object representing the zone.
     */
    getZoneById: function(id) {
        var i, len, zone;
        for(i=0,len=this.config.length; i<len; i++) {
            zone = this.config[i];
            if(zone.id === id) {
                break;
            }
        }
        return zone;
    },    
    
    CLASS_NAME: "GeoSIE.Control.ZoneSelector"
});

OpenLayers.Bounds.prototype.getIntersection = function(bounds) {
    if (!this.intersectsBounds(bounds)) {
        return;
    }

    var left = Math.max(this.left, bounds.left);
    var right = Math.min(this.right, bounds.right);
    var bottom = Math.max(this.bottom, bounds.bottom);
    var top = Math.min(this.top, bounds.top);

    return new OpenLayers.Bounds(left, bottom, right, top);
};
