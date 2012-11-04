/*
 * @include lib/GeoSIE/Control/ZoneSelector.js
 * @include OpenLayers/Projection.js
 */


/**
 * Class: GeoSIE.ZoneManager
 * Module for geographic zone switching.
 */
GeoSIE.ZoneManager = {

    /**
     * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this module.
     * 
     * Supported control event types:
     * zonechanged - Relay the ZoneSelector 'zonechanged' event 
     */
    EVENT_TYPES: ["zonechanged"],

    /**
     * Constant: ZONES
     * {Array(Object)} The geographic zones handled by this module.
     * 
     * A zone object with these properties:
     * * name - {String} Zone name displayed in the menu.
     * * id - {String} Zone id.
     * * country - {String} Country code for geonames.
     * * projections - {Array(Array(String))} List of SRS code and displayed projection name for the map.
     * * gazetteerCrs - {Array(Object)} Gazetter parameters list of a gazetter object, the gazetter object has 3 properties:
     *    epsgCode - {String} SRS code.
     *    label - {String} SRS name displayed in the menu.
     *    units - {String} units displayed.
     * * cursorTrackCrs - {Array(Object)} cursorTrackCrs parameters list of a cursorTrackCrs object, the cursorTrackCrs object has 3 properties:
     *    epsgCode - {String} SRS code.
     *    label - {String} SRS name displayed in the menu.
     *    units - {String} Units displayed. 
     * * options - {Object} Maps options used in ZoneSelector.js:
     *    projection - {String} Map SRS code.
     *    units - {String} Map units.
     *    maxExtent - {<OpenLayers.Bounds>} Map max extent in map projection.
     * * overviewInfo - {Object} Overview options object:
     *    url - image URL {String} Image URL.
     *    extent - {<OpenLayers.Bounds>} Overview extent in map projection.
     *    size - {<OpenLayers.Size>} The size of the overview image (in pixel). 
     */
    ZONES: [{
        name: "Métropole",
        id: "metropole",
        country: "FR",
        projections: [
              ["EPSG:27582", "Lambert 2 étendu"],
              ["EPSG:2154", "Lambert 93"] // EPSG:2154
        ],
        gazetteerCrs:   [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  units: 'degrés' }, 
                          { epsgCode: 'EPSG:27582', label: 'Lambert 2 étendu', units: 'm' },
                          { epsgCode: 'EPSG:2154',  label: 'Lambert 93',       units: 'm' }],
        cursorTrackCrs: [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  numDigits: 5 },
                          { epsgCode: 'EPSG:27582', label: 'Lambert 2 étendu', numDigits: 2 },
                          { epsgCode: 'EPSG:2154',  label: 'Lambert 93',  numDigits: 2 }],
        options: {
            projection: "EPSG:2154",
            units: "m",
            maxExtent: new OpenLayers.Bounds(
                    85530.046558,6029664.01582,1270190.879397,7136592.704467
            )//-5.75537,41.113671,11.129072,51.060557
        },
        overviewInfo: {
            // wget -O overview_images/metropole.png 'http://mapsref.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/WLD_PetiteEchelle.map&LAYERS=MONDE&TRANSPARENT=true&VERSION=1.1.1&FORMAT=PNG&SERVICE=WMS&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A2154&BBOX=-423596.070122,6025082.273503,1779728.218275,7140524.043237&WIDTH=180&HEIGHT=90'
            url: "images/overview_images/metropole.png",
            extent: new OpenLayers.Bounds(
                    -423596.070122,6025082.273503,1779728.218275,7140524.043237
            ),
            size: new OpenLayers.Size(180, 90)
        }
    }, {
        name: "Martinique",
        id: "martinique",
        country: "MQ",
        projections: [
	          ["EPSG:2989", "RRAF 1991 / UTM zone 20N"],
	          ["IGNF:MART38UTM20", "Antilles 84"]
        ],
        gazetteerCrs:   [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  units: 'degrés' },
                          { epsgCode: 'EPSG:2989', label: 'RRAF 1991 / UTM zone 20N', units: 'm' },
                          { epsgCode: 'IGNF:MART38UTM20',  label: 'Antilles 84', units: 'm' }],  
        cursorTrackCrs: [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  numDigits: 5 },
                          { epsgCode: 'EPSG:2989', label: 'RRAF 1991 / UTM Zone 20N', numDigits: 2 },
                          { epsgCode: 'IGNF:MART38UTM20',  label: 'Antilles 84', numDigits: 2 }],                             
        extent: new OpenLayers.Bounds(687872.721221,1590303.282534,737270.318286,1647361.469508), // specific extent, different from the 32620 maxExtent 
      //-61.257636,14.374745,-60.794585,14.894211
        options: {
            projection: "EPSG:2989",
            units: "m",
            maxExtent: new OpenLayers.Bounds(
                    628067.0609, 1592302.0066, 735057.0338, 1826608.5053
            )
        },
        overviewInfo: {
            // wget -O overview_images/martinique.png 'http://mapsref.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/WLD_PetiteEchelle.map&LAYERS=MONDE&TRANSPARENT=true&VERSION=1.1.1&FORMAT=PNG&SERVICE=WMS&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A32620&BBOX=445000,1590000,925000,1830000&WIDTH=180&HEIGHT=90'
            url: "images/overview_images/martinique.png",
            extent: new OpenLayers.Bounds(
                445000,1590000,925000,1830000
            ),
            size: new OpenLayers.Size(180, 90)
        }
    }, {
        name: "Guadeloupe",
        id: "guadeloupe",
        country: "GP",
        projections: [
	          ["EPSG:2989", "RRAF 1991 / UTM zone 20N"],
	          ["IGNF:GUAD48UTM20", "Antilles 84"]
        ],
        gazetteerCrs:   [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  units: 'degrés' },
                          { epsgCode: 'EPSG:2989', label: 'RRAF 1991 / UTM zone 20N', units: 'm' },
                          { epsgCode: 'IGNF:GUAD48UTM20',  label: 'Antilles 84', units: 'm' }],  
        cursorTrackCrs: [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  numDigits: 5 },
                          { epsgCode: 'EPSG:2989', label: 'RRAF 1991 / UTM zone 20N', numDigits: 2 },
                          { epsgCode: 'IGNF:GUAD48UTM20',  label: 'Antilles 84', numDigits: 2 }],                             
        extent: new OpenLayers.Bounds(626356.863391,1748567.107863,714453.647446,1827550.431498), // specific extent, different from the 32620 maxExtent
        //-61.820166,15.806631,-60.990668,16.526533
        options: {
            projection: "EPSG:2989",
            units: "m",
            maxExtent: new OpenLayers.Bounds(
                628067.0609, 1592302.0066, 735057.0338, 1826608.5053
            )
        },
        overviewInfo: {
            // wget -O overview_images/guadeloupe.png 'http://mapsref.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/WLD_PetiteEchelle.map&LAYERS=MONDE&TRANSPARENT=true&VERSION=1.1.1&FORMAT=PNG&SERVICE=WMS&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A32620&BBOX=445000,1590000,925000,1830000&WIDTH=180&HEIGHT=90'
            url: "images/overview_images/guadeloupe.png",
            extent: new OpenLayers.Bounds(
                445000,1590000,925000,1830000
            ),
            size: new OpenLayers.Size(180, 90)
        }
    }, {
        name: "Guyane",
        id: "guyane",
        country: "GF",
        projections: [
	          ["EPSG:2972", "RGFG95 / UTM zone 22N"],
	          ["IGNF:RGFG95", "Guyane 95"]
        ],
        gazetteerCrs:   [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  units: 'degrés' }, 
                          { epsgCode: 'EPSG:2972', label: 'RGFG95 / UTM zone 22N', units: 'm' },
                          { epsgCode: 'IGNF:RGFG95',  label: 'Guyane 95',       units: 'm' }],  
        cursorTrackCrs: [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  numDigits: 5 },
                          { epsgCode: 'EPSG:2972', label: 'RGFG95 / UTM zone 22N', numDigits: 2 },
                          { epsgCode: 'IGNF:RGFG95',  label: 'Guyane 95',       numDigits: 2 }],                             
        options: {
            projection: "EPSG:2972",
            units: "m",
            maxExtent: new OpenLayers.Bounds(
               100000, 225000, 440000, 640000
            )//-54.610552,2.031591,-51.53951,5.789807
        },
        overviewInfo: {
            // wget -O overview_images/guyane.png 'http://mapsref.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/WLD_PetiteEchelle.map&LAYERS=MONDE&TRANSPARENT=true&VERSION=1.1.1&FORMAT=PNG&SERVICE=WMS&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A32622&BBOX=-145000,225000,685000,640000&WIDTH=180&HEIGHT=90'
            url: "images/overview_images/guyane.png",
            extent: new OpenLayers.Bounds(
                -145000,225000,685000,640000
            ),
            size: new OpenLayers.Size(180, 90)
        }
    }, {
        name: "Mayotte",
        id: "mayotte",
        country: "YT",
        projections: [
              ["EPSG:4471", "RGM04 / UTM zone 38S"],
              ["IGNF:RGM04", "Mayotte 2004"]
        ],
        gazetteerCrs:   [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  units: 'degrés' },
                          { epsgCode: 'EPSG:4471',  label: 'RGM04 / UTM zone 38S',  units: 'm' },
                          { epsgCode: 'IGNF:RGM04',  label: 'Mayotte 2004', units: 'm' }],   
        cursorTrackCrs: [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  numDigits: 5 },
                          { epsgCode: 'EPSG:4471', label: 'RGM04 / UTM zone 38S',  numDigits: 2 },
                          { epsgCode: 'IGNF:RGM04',  label: 'Mayotte 2004', numDigits: 2 }],                            
        options: {
            projection: "EPSG:4471",
            units: "m",
            maxExtent: new OpenLayers.Bounds(
                500000, 8560000, 545000, 8605000
            )//45,-13.025901,45.41501,-12.618652
        },
        overviewInfo: {
            // wget -O overview_images/mayotte.png 'http://mapsref.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/WLD_PetiteEchelle.map&LAYERS=MONDE&TRANSPARENT=true&VERSION=1.1.1&FORMAT=PNG&SERVICE=WMS&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A32738&BBOX=477500,8560000,567500,8605000&WIDTH=180&HEIGHT=90'
            url: "images/overview_images/mayotte.png",
            extent: new OpenLayers.Bounds(
                477500,8560000,567500,8605000
            ),
            size: new OpenLayers.Size(180, 90)
        }
    }, {
        name: "Réunion",
        id: "reunion",
        country: "RE",        
        projections: [
	          ["EPSG:2975", "RGR92 / UTM zone 40S"],
	          ["IGNF:RGR92", "Réunion 92"]
        ],
        gazetteerCrs:   [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  units: 'degrés' }, 
                          { epsgCode: 'EPSG:2975', label: 'RGR92 / UTM zone 40S', units: 'm' },
                          { epsgCode: 'IGNF:RGR92',  label: 'Réunion 92', units: 'm' }],     
        cursorTrackCrs: [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  numDigits: 5 },
                          { epsgCode: 'EPSG:2975', label: 'RGR92 / UTM zone 40S', numDigits: 2 },
                          { epsgCode: 'IGNF:RGR92',  label: 'Réunion 92', numDigits: 2 }],                          
        options: {
            projection: "EPSG:2975",
            units: "m",
            maxExtent: new OpenLayers.Bounds(
                310000, 7630000, 385000, 7695000
            )
        },//55.16685,-21.42869,55.894688,-20.835326
        overviewInfo: {
            // wget -O overview_images/reunion.png 'http://mapsref.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/WLD_PetiteEchelle.map&LAYERS=MONDE&TRANSPARENT=true&VERSION=1.1.1&FORMAT=PNG&SERVICE=WMS&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A32740&BBOX=282500,7630000,412500,7695000&WIDTH=180&HEIGHT=90'
            url: "images/overview_images/reunion.png",
            extent: new OpenLayers.Bounds(
                282500,7630000,412500,7695000
            ),
            size: new OpenLayers.Size(180, 90)
        }
      },{
            name: "Monde",
            id: "monde",
            country: "",
            projections: [
                ["EPSG:4326", "Longitude/Latitude"]
            ],
            gazetteerCrs:   [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  units: 'degrés' }], 
            cursorTrackCrs: [{ epsgCode: 'EPSG:4326',  label: 'Longitude/Latitude',  numDigits: 5 }],
            options: {
                projection: "EPSG:4326",
                units: "degrees",
                maxExtent: new OpenLayers.Bounds(
                    -180, -90, 180, 90
                )
            },
            overviewInfo: {
                // http://mapsref.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/WLD_PetiteEchelle.map&LAYERS=MONDE&TRANSPARENT=true&VERSION=1.1.1&FORMAT=jpg&SERVICE=WMS&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A4326&BBOX=-180,-90,180,90&WIDTH=180&HEIGHT=90
                url: "images/overview_images/monde.jpg",
                extent: new OpenLayers.Bounds(
                    -180, -90, 180, 90
                ),
                size: new OpenLayers.Size(180, 90)
            }
        }],

    /**
     * Constant: PROJECTIONS
     * {Array} The projections conversion table.
     * To be used to transform contexts.
     */
    PROJECTIONS: {
        'EPSG:27582': 'EPSG:2154',
        'EPSG:32620': 'EPSG:2989',
        'EPSG:32622': 'EPSG:2972',
        'EPSG:32738': 'EPSG:4471',
        'EPSG:32740': 'EPSG:2975',
        'EPSG:32758': 'EPSG:2984',
        'EPSG:32705': 'EPSG:3296',
        'EPSG:32706': 'EPSG:3297'
    },        
        
    /**
     * Property: zoneSelector
     * {<GeoSIE.Control.ZoneSelector>} The zone selector control instance.
     */
    zoneSelector: null,

    /**
     * APIProperty: events
     * {OpenLayers.Events} The object to use to register event
     * listeners.
     */
    events: null,
    
    /**
     * Method: onZonechanged
     * Called when switching from one zone to another.
     *
     * Parameters:
     * evt - {Object} An object with a zone property.
     */
    onZonechanged: function(evt) {
        // relay event
        var zone = evt.zone;
        OpenLayers.Console.log("GeoSIE.ZoneManager::onZonechanged");
        
        this.events.triggerEvent("zonechanged", {
        	id: zone.id,
            projection: zone.options.projection,
            cursorTrackCrs: zone.cursorTrackCrs,
            gazetteerCrs: zone.gazetteerCrs,
            overviewInfo: zone.overviewInfo
        });
    },


    /**
     * APIMethod: init
     * Initialize the module.
     */
    init: function() {
        this.events = new OpenLayers.Events(this, null, this.EVENT_TYPES);
        this.zoneSelector = new GeoSIE.Control.ZoneSelector({
            config: this.ZONES,
            eventListeners: {
                zonechanged: this.onZonechanged,
                scope: this
            }
        });
        return this.zoneSelector;
    },

    /**
     * APIMethod: changeZone
     * Switch to a new geographic zone, setting new parameters in the
     * map and layers as appropriate.
     *
     * Parameters:
     * name - {String} The name of the zone to switch to.
     */
    changeZone: function(name) {
//    	OpenLayers.Console.log("zoneManager changeZone");
        this.zoneSelector.selectZone(name);
    },

    /**
     * APIMethod: getZoneFromProj 
     * Return the zone corresponding to a projection code.
     *
     * Parameters:
     * projCode - {Number} The projection code.
     *
     * Returns:
     * {Object} The zone object.
     */
    getZoneFromProj: function(projCode) {
        var i, len, zone;
        for (i=0,len=this.ZONES.length; i<len; i++) {
            zone = this.ZONES[i];
            if (zone.options.projection === projCode) {
                break;
            }
        }
        return zone;
    },
    
    /**
     * APIMethod: getZonesCodes 
     * Return the countries codes for all the zones
	 * 
     * Returns:
     * {String} The countries codes.
     */    
    getZonesCodes: function(){
        var i, len, zone;
        var countries = "";
        
        for (i=0,len=this.ZONES.length; i<len; i++) {
            zone = this.ZONES[i];
            if (zone.country) {
            	countries+=zone.country+",";
            }
        }
        return countries;
    },

	/**
	 * APIMethod: getZoneById Return the zone for a zone id
	 * 
	 * Paremeters:
	 * id -  {String} The zone id.
	 * 
	 * Returns: {Object} The zone object.
	 */
	getZoneById: function(id) {
		for (i = 0, len = this.ZONES.length; i < len; i++) {
			var zone = this.ZONES[i];
			if (zone.id == id) {
				return zone;
			}
		}
		return null;
	},	    
    
    /**
     * APIMethod: getDefaultZone
     * Return the zone display at init (defined in GeoSIE.Config) 
     * 
     * Returns:
     * {Object} The zone object.
     */
    getDefaultZone: function(){
		// get the default zone
		var i, len, zone;
        for (i=0,len=this.ZONES.length; i<len; i++) {
            zone = this.ZONES[i];
            if (zone.id == GeoSIE.Config.FORCED_AREA) {
                break;
            }            	
        }
        return zone;
    },
    
    /**
     * APIMethod: convertContext
     * Converts a context to a compatible one. 
     * (ex. 27582 -> 2154)
     *
     * Parameters:
     * context: {Object}
     */
    convertContext: function(context) {
        if (this.PROJECTIONS[context.projection]) {
            context.bounds.transform(
                new OpenLayers.Projection(context.projection),
                new OpenLayers.Projection(this.PROJECTIONS[context.projection])
            ); 
            context.projection = this.PROJECTIONS[context.projection];
        }
    }    
    
};