/*
 * @include lib/GeoSIE/Messages.js
 * @include lib/GeoSIE/ZoneManager.js
 * @include OpenLayers/Events.js
 * @include OpenLayers/BaseTypes/Bounds.js
 * @include OpenLayers/Projection.js
 */

/**
 * Namespace: GeoSIE.GoTo 
 */

GeoSIE.GoTo = {
		
    /**
     * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this module.
	 *
     * Supported event types:
     * zoomToExtent - Triggered when click "Recentrer la carte".
     * zoomToLonLat - Triggered when click on a city in the list.
     */
    EVENT_TYPES: [ 'zoomToExtent', 'zoomToLonLat' ],

    /**
     * APIProperty: events
     * {OpenLayers.Events} The object to use to register event.
     * listeners.
     */
    events: null,

    /**
     * Property: gMessages
     * {Object} Titles in French for geonames icons.
     */
    gMessages: null,    
    
    /**
     * APIMethod: init
     * Initialize the module.
     */
    init: function() {
    	this.gMessages = new Object(); 
    	this.gMessages['js.message.geonamesP'] = 'D\u00E9mographie (villes, villages...)'; 
    	this.gMessages['js.message.geonamesH'] = 'Hydrographie (cours d\'eau, lacs...)'; 
    	this.gMessages['js.message.geonamesU'] = 'Milieu sous-marin'; 
    	this.gMessages['js.message.geonamesR'] = 'Voies de communication (routes, voies ferr\u00E9es...)'; 
    	this.gMessages['js.message.geonamesA'] = 'Entit\u00E9s administratives (pays, \u00E9tats, r\u00E9gions...)'; 
    	this.gMessages['js.message.geonamesT'] = 'Relief (montagnes, collines...)';     	
    	
        this.events = new OpenLayers.Events(this, null, this.EVENT_TYPES);
    },    
    
	/**
	 * Method: point
	 * Zoom on the specified area by the coordinate and projection.
	 * Trigger the event zoomToExtent.
	 * 
	 * Parameters: 
     * x - {Float} x (longitude) coordinate.
     * y - {Float} y (latitude) coordinate.
     * epsgCode - {String} coordinate epsg code.
     * unit - {String} unit coordinate (degrés or m).
	 */		
    point: function(x, y, epsgCode, unit) {
        if (x && y) {
        	// Replace , by .
        	x = x.replace(",", ".");
        	y = y.replace(",", ".");

        	OpenLayers.Console.log("GeoSIE.Goto::point - unit : "+unit+" x: "+x+" y: "+y+" epsgCode: "+epsgCode);

            var buffer = 0.1; 
            if (unit === "m") {
                buffer = 10000;
            }
            //config.objects.mainMap.map.setCenter(new OpenLayers.LonLat(x, y));
            var bounds = new OpenLayers.Bounds(parseFloat(x) - buffer, parseFloat(y) + buffer, parseFloat(x) + buffer, parseFloat(y) - buffer);
            this.events.triggerEvent('zoomToExtent', {
            	bounds: bounds,
            	epsgCode: epsgCode
            });
        } else {
            alert(GeoSIE.Messages.errorGotoCoordinatesMessage);
        }
    },

    /**
     * Method: pointLatLon 
     * Recenter on coordinates.
     * Trigger the event zoomToLonLat.
     * 
     * Parameters:
     * lon - {float} The longitude to recenter on.
     * lat {float} The latitude to recenter on.
     */
    pointLatLon: function(lat, lon) {
        if (lat && lon) {
            var lonlat = new OpenLayers.LonLat(lon, lat);
            var options = { lonlat: lonlat };

            this.events.triggerEvent('zoomToLonLat', options);
        } else {
            alert(GeoSIE.Messages.errorGotoCoordinatesMessage);
        }
    },

    /**
     * Method: search
     * Search for places with geonames with the search term (str), country code, 
     * lang (results in French) and featureClass to filter results (skip hostel).
     * Maximum results: 6. 
     * 
     * Parameters: 
     * str - {String} the city name to find.
     */       
    search: function(str) {
    	// List iso code country : http://www.geonames.de/countries.html
    	// Params : http://www.geonames.org/export/web-services.html
    	//  - country = string : country code, ISO-3166 (optional)
    	// sur infoterre &country=PF,YT,NC,GP,MQ,GF,RE,FR'; 
    	var countries = GeoSIE.ZoneManager.getZonesCodes();
    	var url = 'http://ws.geonames.org/searchJSON?q=' +  encodeURIComponent(str)  + '&maxRows='+
    	GeoSIE.Config.MAX_RESULT_GEONAMES + '&callback=getLocation&country='+countries+'&lang=fr&featureClass=P&featureClass=A';
        
        this.callGeonames(url); 
    },

    /**
     * Method: callGeonames
     * Calls the geonames webservice.
     * 
     * Parameters: 
     * url - {String} the URL to call geonames service.
     */
    callGeonames : function(url) {
    	  $.getScript(url);
    },
    
    location: function(data) {
        if (data == null) {
            // there was a problem parsing search results
        	OpenLayers.Console.log("GeoSIE.Goto::location - Erreur pour parser resultat geonames");
            return;
        }

        // if geonames is to busy and not return data.geonames
        // {"status":{"message":"the free servers are currently overloaded with requests."
        if(!data.geonames){
        	OpenLayers.Console.log(data.status);
        	alert(GeoSIE.Messages.geonamesTooBusyMessage);
        	return;
        }
        
        if(data.geonames.length == 0){
        	document.getElementById('gotoResultDiv').innerHTML = GeoSIE.Messages.geonamesNoResultMessage;
        	//alert(GeoSIE.Messages.geonamesNoResultMessage);
        } else {
            var html = '<table cellspacing=5>';
            var geonames = data.geonames;
            for (var i=0; i<geonames.length; i++) {
                var name = geonames[i];
                // we create a simple html list with the geonames objects
                // the link will call the center() javascript method with lat/lng as parameter
                var adminArea = "";
                if (name.adminName1 != "") {
                    adminArea = ' (' + name.adminName1 + ')';
                }
                html = html + '<tr><td align="right"><img src="'+GeoSIE.Config.ROOT_PATH+'images/geonames/icone_' 
                + name.fcl.toLowerCase() 
                + '.gif" title="'+ this.gMessages["js.message.geonames" + name.fcl] + '"></td>'
                +'<td align="left"><a class="infoterreLink" href="javascript:GeoSIE.GoTo.pointLatLon(' + name.lat +',' + name.lng + ');">';

                html += name.name + adminArea + '</a></td></tr>';
            }
            html += '</table>';
            document.getElementById('gotoResultDiv').innerHTML = html;
        }
    }
};

/**
 * Namespace: window
 */

/**
 * Method: getLocation
 * 
 * Parameters:
 * jData -
 */
function getLocation(jData) {
	GeoSIE.GoTo.location(jData);
}