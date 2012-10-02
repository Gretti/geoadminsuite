/*
 * @requires OpenLayers/Control.js
 */

/**
 * Class: GeoSIE.Control.FullScreen
 * Display map as full screen.
 * by: n.dhuygelaere@oieau.fr
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */

GeoSIE.Control.FullScreen = OpenLayers.Class(OpenLayers.Control, {
	
	/**
	 * Property: type
     * {OpenLayers.Control.TYPES}
	 */
	type: OpenLayers.Control.TYPE_TOGGLE, 
	
	/**
	 * APIProperty: minimizeTitle
	 * {String} This property is used for showing a tooltip over the  
     * Control when the control is activate.  
	 */
	minimizeTitle: null,
	
	/**
	 * APIProperty: scrollTop
	 * {Integer} Scroll zoom to move to the top (12 for GestEau).
	 */	
	scrollTop: 12,
	
	/**
	 * APIProperty: sizeFeatureList
	 * {Double} The pourcentage size for the widget FeatureList result (resize).
	 */	
	sizeFeatureList: 0.7,
	
    /**
     * Constructor: GeoSIE.Control.FullScreen
     * Constructor for a control to display map as full screen.
     * 
     * Parameters:
     * options - {Object} 
     * 
     * Returns: 
     * {<GeoSIE.Control.FullScreen>}
     */	
	initialize: function(options){
		OpenLayers.Util.extend(this, options);
		OpenLayers.Control.prototype.initialize.apply(this, [options]);
	},
	
    /**
	 * APIMethod: activate 
	 * Activate the control.
	 * 
	 * Returns: 
	 * {Boolean} Successfully activated the control. 
	 */    	
	activate: function(){
		var extent = this.map.getExtent();
		
		// Change the title control
		this.panel_div.title = this.minimizeTitle;
		
		// Display as full screen
		$("#container_geosie_brique").addClass('fullscreen');
		$(".fullscreen").height( $(document).height());
		//$(".fullscreen .container_geosie_brique_content").height( $(window).height()); //TODO : valeur 40 sinon deborde window).height()-40
		$(".fullscreen .container_geosie_brique_content").css('cssText', 'height: '+($(window).height()-40)+'px !important');
		
		//var mapHeight = ($(document).height())-120;
		//$(".fullscreen #map").css('cssText','height: '+mapHeight+'px !important');
		
		this.map.updateSize();
		this.map.zoomToExtent(extent, true);
		
		if(this.scrollTop){
			$('html, body').animate({scrollTop: this.scrollTop}, 'slow');
		}
		$("#accordion").accordion( "resize" );
		//$(".accordiontweak-content").height($("#accordion").height() - ($(".accordiontweak").length *14 + 2));
		
		//console.log($("#accordion").height());
		
		$(".accordiontweak-content").height($("#accordion").height() - ($(".accordiontweak").length *14 + 2));

		// Resize Resultat
		$(".researchInput").height($("#accordion").height()*this.sizeFeatureList);
		
		//FIX :  force ie 7 to refresh properties of the div
		var r = OpenLayers.Control.prototype.activate.apply(this, arguments);
		if ($.browser.msie  && parseInt($.browser.version) == 7) {
			$(".fullscreen").hide();
			$(".fullscreen").show();
		}
		
		return r;
	},
	
    /**
     * APIMethod: deactivate
     * Deactivate the control and all handlers.
     *
     * Returns:
     * {Boolean} Successfully deactivated the control.
     */	
	deactivate: function (){
		// Change the title control
		this.panel_div.title = this.title;
	
		$(".fullscreen").height('auto');
		$(".fullscreen .container_geosie_brique_content").height('auto');
		if($("#container_geosie_brique").is('.fullscreen')) {$("#container_geosie_brique").removeClass('fullscreen');}
		this.map.updateSize();
		$("#accordion").accordion( "resize" );
		$(".accordiontweak-content").height($("#accordion").height() - ($(".accordiontweak").length *14 + 2)); //42 est une valeur arbitraire, il faudrait normalement compte le nombre d'item "accordiontweak" (pour l'instant 3) et la hauteur (14) de leur titre pour definir cette valeur
		
		// Resize Resultat
		$(".researchInput").height($("#accordion").height()*this.sizeFeatureList);
		
		//FIX :  force ie 7 to refresh properties of the div
		if ($.browser.msie  && parseInt($.browser.version) == 7) {
			$("#navigationButtonBar").hide();
			$("#navigationButtonBar").show();
		}
		
		return OpenLayers.Control.prototype.deactivate.apply(this, arguments);
	},
	
	CLASS_NAME: 'GeoSIE.Control.FullScreen'
});