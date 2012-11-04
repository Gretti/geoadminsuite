/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Events.js
 */


/**
 * Class: GeoSIE.Control.WpsSelector
 * A control enabling switching from one WPS process to another.
 * A dropdown menu.
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.WpsSelector = OpenLayers.Class( OpenLayers.Control, {
    
	/**
     * Constant: EVENT_TYPES
     * {Array({String}) The event types supported by this control.
     * 
     * Supported event types:
     * processchanged - Triggered when a process is selected.
     */
    EVENT_TYPES: ["processchanged"],
    
    /**
     * Property: autoActivate
     * {Boolean} Activate the control when it is added to the map,
     * defaults to true.
     */
    autoActivate: true,	
    
    /**
     * Property: currentZone
     * {Object} The current zone.
     */    
    currentZone: null,	
    
    /**
     * APIProperty: processes
     * {Array({Object})} An array of objects representing processes.
     */
    processes: null,
    
    /**
     * Property: process
     * {Object} The current process.
     */
    process: null,    
	
    /**
     * Constructor: GeoSIE.Control.WpsSelector
     * 
     * Parameters:
     * options - {Object} Options for control.
     * 
     * Returns: 
     * {<GeoSIE.Control.WpsSelector>} 
     */
    initialize: function(options) {
        this.EVENT_TYPES =
        	GeoSIE.Control.WpsSelector.prototype.EVENT_TYPES.concat(
            OpenLayers.Control.prototype.EVENT_TYPES
        );
        
        OpenLayers.Control.prototype.initialize.apply(this, arguments);
    },	
	
    /**
     * Method: activate
     * Activates the control.
     * 
     * Returns:
     * {Boolean} The control was effectively activated.
     */
    activate: function () {
        var activated = OpenLayers.Control.prototype.activate.apply(
            this, arguments
        );
        
        // Redraw on Zone Change
        GeoSIE.ZoneManager.events.on({
            'zonechanged': this.redraw,
            scope: this
        });
        
        return activated;
    },

    /**
     * Method: deactivate
     * Deactivates the control.
     * 
     * Returns:
     * {Boolean} The control was effectively deactivated.
     */
    deactivate: function () {
    	//OpenLayers.Console.log("deactivate WpsSelector");
        // Redraw on Zone Change
    	GeoSIE.ZoneManager.events.un({
            'zonechanged': this.redraw,
            scope: this
        });
        
        return OpenLayers.Control.prototype.deactivate.apply(this, arguments);
    },    

    
    /**
     * Method: draw
     */    
    draw: function() {
        OpenLayers.Control.prototype.draw.apply(this, arguments);
          
        // Pas necessaire
        // Add jquery dropdown effect to the list
        /*$(".dropdownWps dt a").click(function() {
            $(".dropdownWps dd ul").toggle();
        });*/
        
        // FIXME : si ne clic pas sur un des liens est clic sur un bouton OL, le menu reste afficher
        // Hide the list when click outside the list
        $(document).bind('click', function(e) {
            var $clicked = $(e.target);
            if (! $clicked.parents().hasClass("dropdownWps")){
                $(".dropdownWps dd ul").hide();
            }
        });

        return this.div;
    },	

    /**
     * Method: redraw
     * Redraw the zones list.
     * 
     * Parameters:
     * evt - {Event} The current zone.
     */
    redraw: function(evt) {
    	this.currentZone = evt;
        // Build the menu with processes
        var dt = '<dd><ul>';
        //var dt = '<dt><a href="#"><span>Liste procédures</span></a></dt><dd><ul>';

		var li='';
		for(var i=0,len=this.processes.length; i<len; i++) {
	        var process = this.processes[i];
	        
	        for(var zone = 0;zone<process.zones.length;zone++){
	        	var id = process.zones[zone];
		        if(this.currentZone.id == id){
					li += '<li><a href="#" id="process_'+i+'"><img class="wpsIcon" src="'
					+ GeoSIE.Config.ROOT_PATH + process.iconUrl + '" alt="" />' + process.label + '</a></li>';
		        }
	        }
		}
        
        dt+=li+"</ul></dd>";
        OpenLayers.Util.getElement("wpsList").innerHTML=dt;    	
        
        // Listen the links
        for(var i=0;i<this.processes.length; i++) {
	   		 var link = ($('#process_' + i))[0]; //+' .span'
	   		 var process = this.processes[i];
		        for(var zone = 0;zone<process.zones.length;zone++){
		        	var id = process.zones[zone];
			        if(this.currentZone.id == id){
						 OpenLayers.Event.observe(link, "click", OpenLayers.Function.bind(function(process,evt) {
							  //var text = OpenLayers.Event.element(evt).innerHTML; 
							  this.selectProcess(process);
				            }, this,process));   			        }
		        }
        }        
    },
    
    /**
     * APIMethod: selectProcess
     * Selects a new process and set to the map the selected process.
     *
     * Parameters:
     * process - {Object} The process
     *
     * Returns:
     * {Object} The process.
     */    
    selectProcess: function(process) {
		// Switch the button image
		if($(".GeoSIEControlWpsSelectItemInactive")){
			$(".GeoSIEControlWpsSelectItemInactive").css("background-image", "url("+process.buttonUrl+")"); 
		}
		  
		// Hide the list
        $(".dropdownWps dd ul").hide();
    	
        // Activate or not the wps control
    	/*var control = this.map.getControlsByClass("Carto.Control.WpsGetFeature");
        OpenLayers.Console.log(control);
    	if(process.id === "fake"){
        	if(control.active){
        		control.deactivate;
        	}
        } else {
        	if(!control.active){
        		control.activate;
        	}
        }*/
        
    	this.events.triggerEvent("processchanged", {process: process});
    	return process;
    },
    
	/**
	 * Method: showHide
	 * Helper method toggling a DOM element display style state. 
	 */
	showHide: function(){
		$(".dropdownWps dd ul").toggle();
	},
    
    CLASS_NAME: 'GeoSIE.Control.WpsSelector'
});