/*
 * @include lib/GeoSIE/Control/WpsSelector.js
 * 
 */

/**
 * Class: GeoSIE.WpsManager
 * Module for WPS processes switching.
 * 
 * TODO : pas fini en attente d'un service WPS complet  
 */
GeoSIE.WpsManager = {
	    
	/**
     * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this module.
     * 
     * Supported control event types:
     * processchanged - Relay the WpsSelector 'processchanged' event
     */
    EVENT_TYPES: ["processchanged"],	

    /**
     * Constant: PROCESSES
     * {Array(Object)} The WPS processes handled by this module.
     * 
     * A zone process with these properties:
     * * label - {String} Process name displayed in the menu.
     * * id - {String} Process id.
     * * zones - {Array(String)} Zones id list where the process is available.
     * * iconUrl - {String} Icon URL displayed in the menu.
     * * buttonUrl - {String} Icon URL displayed in the "Traitement" toolbar.
     * * srs - {String} SRS code used for this process.
     * * format - {String} Format result.
     * * url - {String} Process URL.
     * * urlDownload - {String} Process download URL.
     * * dialog - {Boolean} Display a dialog or not before calling the process.
     * * params - {Array(Object)} Parameters list in the dialog form for the request, a parameter:
     *    wms - {String} Feature attribute name (GetFeatureInfo result).
     *    id - {String} Query string name parameter.
     * * formatsDownload - {Array(String)} List of available download format.
     * * srsDownload - {Array(String)} List of available download SRS.
     * * option - {Function} Function, get the form values.
     * * dialogContent - {String} The HTML content in the dialog form. 
     * * setDialogContent - {Function} Set the dialogContent values (GetFeatureInfo result to the dialogContent).    
     * * paramsForm - {Array(Object)} Form field list to fill with WMS response, a form filed:
     *    wms - {String} Feature attribute name (GetFeatureInfo result).
     *    id - {String} Field name.
     *         
     */
    PROCESSES: [
    {
    	label:"Aucun",
    	id:"fake",
    	zones:["metropole","martinique","guadeloupe","guyane","mayotte","reunion","monde"],
    	iconUrl:"images/wps/menu/menu_vide.gif",
    	buttonUrl:"style/images/wps/wps_vide_off.gif",
    	dialog:false
    },{
    	label:"Amont",
    	id:"amont",
    	zones:["metropole","martinique"],
    	iconUrl:"images/wps/menu/menu_amont.gif",
    	buttonUrl:"style/images/wps/wps_amont_off.gif",
    	srs:"EPSG:27572",
    	format: "GML2",
    	url:"http://fmeserver.brgm-rec.fr/fmedatastreaming/demo_carthage/amont.fmw?",
    	urlDownload:"http://fmeserver.brgm-rec.fr/fmedatadownload/demo_carthage/amont.fmw?",
    	dialog:false,
    	params:[{wms:"gige_grou", id:"GIGE_GROU"},
    	        {wms:"gige_brni", id:"GIGE_BRNI"},
    	        {wms:"gige_brns", id:"GIGE_BRNS"},
    	        {wms:"gige_sthr", id:"GIGE_STHR"}],
    	formatsDownload: ["GEOJSON","GML2","MIF","SHP","TAB"],
    	srsDownload:["EPSG:27572","EPSG:2154","EPSG:4326"]
    },{	    	
    	label:"Amont (rayon)",
    	id:"amontr",
    	zones:["metropole","guadeloupe","martinique"],
    	iconUrl:"images/wps/menu/menu_amont_rayon.gif",
    	buttonUrl:"style/images/wps/wps_amont_rayon_off.gif",
    	srs:"EPSG:27572",
    	format: "GML2",
    	url:"http://fmeserver.brgm-rec.fr/fmedatastreaming/demo_carthage/amont_rayon.fmw?",
    	urlDownload:"http://fmeserver.brgm-rec.fr/fmedatadownload/demo_carthage/amont_rayon.fmw?",
    	dialog:true,
    	options: function() {
		    // Get the form values
			var rayon = $( "#rayon" ).val();
			var options =[{id:"RAYON", value: rayon}];
			return options;
		},
		dialogContent: 
			'<form id="wpsForm">'+
				'<fieldset>'+
					'<label for="rayon">Rayon (m) : </label>'+
					'<input type="text" name="rayon" id="rayon" style="width: 50px;"/>'+ 
				'</fieldset>'+
				'<div style="text-align: center;">'+
					'<input type="submit" style="cursor: pointer;" value="Executer" />'+
					'<input type="reset" style="cursor: pointer;" value="Annuler" />'+
			    '</div>'+	
			'</form>',
    	params:[{wms:"gige_grou", id:"GIGE_GROU"},
    	        {wms:"gige_brni", id:"GIGE_BRNI"},
    	        {wms:"gige_brns", id:"GIGE_BRNS"},
    	        {wms:"gige_long", id:"GIGE_LONG"}],
    	formatsDownload: ["GEOJSON","GML2","MIF","SHP","TAB"],
    	srsDownload:["EPSG:27572","EPSG:2154","EPSG:4326"]	    	        
    },{
    	label:"Amont toponyme",
    	id:"topo_amont",
    	zones:["metropole"],
    	iconUrl:"images/wps/menu/menu_amont_toponyme.gif",
    	buttonUrl:"style/images/wps/wps_amont_toponyme_off.gif",
    	srs:"EPSG:27572",
    	format: "GML2",
    	url:"http://fmeserver.brgm-rec.fr/fmedatastreaming/demo_carthage/amont_toponyme.fmw?",
    	urlDownload:"http://fmeserver.brgm-rec.fr/fmedatadownload/demo_carthage/amont_toponyme.fmw?",
    	dialog:true,
    	options: function() {
		    // Get the form values
			var topo = $( "#topo" ).val();
			var options =[{id:"TOPONYME1", value: topo}];
			return options;
		},
		dialogContent:"",    	
    	params:[{wms:"gige_grou", id:"GIGE_GROU"},
    	        {wms:"toponyme1", id:"TOPONYME1"},
    	        {wms:"gige_sthr", id:"GIGE_STHR"}],
    	formatsDownload: ["GEOJSON","GML2","MIF","SHP","TAB"],
    	srsDownload:["EPSG:27572","EPSG:2154","EPSG:4326"],	  	    	        
    	// Field to fill with WMS response        
    	paramsForm:[{wms:"toponyme1" , id:"topo"}],      
    	// <gige_long>281516.422352</gige_long>

    	setDialogContent: function(context){
			this.dialogContent = 
			'<form id="wpsForm">'+
				'<fieldset>'+
					'<label for="rayon">Toponyme : </label>'+
					'<input type="text" name="topo" readonly="readonly" id="topo" style="width: 192px" value="'+context.topo+'";"/>'+ 
				'</fieldset>'+
				'<div style="text-align: center;">'+
					'<input type="submit" style="cursor: pointer;" value="Executer" />'+
				'</div>'+					
			'</form>';
		}        
    },{
    	label:"Aval",
    	id:"aval",
    	zones:["metropole","guyane"],
    	iconUrl:"images/wps/menu/menu_aval.gif",
    	buttonUrl:"style/images/wps/wps_aval_off.gif",
    	srs:"EPSG:27572",
    	format: "GML2",
    	url:"http://fmeserver.brgm-rec.fr/fmedatastreaming/demo_carthage/aval.fmw?",
    	urlDownload:"http://fmeserver.brgm-rec.fr/fmedatadownload/demo_carthage/aval.fmw?",
    	dialog:false,
    	params:[{wms:"gige_grou", id:"GIGE_GROU"},
    	        {wms:"gige_brni" , id:"GIGE_BRNI"},
    	        {wms:"gige_brns" , id:"GIGE_BRNS"}],
    	formatsDownload: ["GEOJSON","GML2","MIF","SHP","TAB"],
    	srsDownload:["EPSG:27572","EPSG:2154","EPSG:4326"]	  	    	
    },{
    	label:"Toponyme",
    	id:"topo",
    	zones:["metropole"],
    	iconUrl:"images/wps/menu/menu_toponyme.gif",
    	buttonUrl:"style/images/wps/wps_toponyme_off.gif",
    	srs:"EPSG:27572",
    	format: "GML2",
    	url:"http://fmeserver.brgm-rec.fr/fmedatastreaming/demo_carthage/toponyme.fmw?",
    	urlDownload:"http://fmeserver.brgm-rec.fr/fmedatadownload/demo_carthage/toponyme.fmw?",
    	dialog:true,
    	options: function() {
		    // Get the form values
			var topo = $( "#topo" ).val();
			var options =[{id:"TOPONYME1", value: topo}];
			return options;
		},
		dialogContent: "",	    	
    	params:[{wms:"gige_grou", id:"GIGE_GROU"},
    	        {wms:"toponyme1" , id:"TOPONYME1"}],
    	formatsDownload: ["GEOJSON","GML2","MIF","SHP","TAB"],
    	srsDownload:["EPSG:27572","EPSG:2154","EPSG:4326"],	  	    	        
    	// Field to fill with WMS response        
    	paramsForm:[{wms:"toponyme1" , id:"topo"}],        
    	setDialogContent: function(context){
			this.dialogContent = 
			'<form id="wpsForm">'+
				'<fieldset>'+
					'<label for="rayon">Toponyme : </label>'+
					'<input type="text" name="topo" readonly="readonly" id="topo" style="width: 192px" value="'+context.topo+'";"/>'+ 
				'</fieldset>' +
				'<div style="text-align: center;">'+
					'<input type="submit" style="cursor: pointer;" value="Executer" />'+
				'</div>'+					
			'</form>';
		}        
    },{
    	label:"Distance à l'exutoire",
    	id:"getfeatureinfo",
    	zones:["metropole"],
    	iconUrl:"images/wps/menu/menu_distance.gif",
    	buttonUrl:"style/images/wps/distance_off.gif",
    	dialog:true,
    	options: function() {
		    // Get the form values
			var options =[{id:"getfeatureinfo", value: ""}];
			return options;
		},
		dialogContent: "",
    	params:[],
    	// Field to fill with WMS response        
    	paramsForm:[{wms:"gige_long", id:"lengthTr"}],        
    	setDialogContent: function(context){
			// convert to km with 5 precisions (513.26)
			this.dialogContent = 
			'<form id="wpsFormDistance">'+
				'<fieldset>'+
					'<label for="lengthTrLabel">Distance à l\'exutoire (km) : </label>'+
					'<input type="text" name="lengthTr" readonly="readonly" id="lengthTr" style="width: 130px" value="'+
					  OpenLayers.Util.toFloat(context.lengthTr/1000,5)+'";"/>'+ 
				'</fieldset>'+ 
				'<div style="text-align: center;">'+
					'<input type="submit" style="cursor: pointer;" value="Fermer" />'+
				'</div>'+					
			'</form>';
		}	    	
    }],
    
    /**
     * APIProperty: events
     * {OpenLayers.Events} The object to use to register event
     * listeners.
     */
    events: null,

    /**
     * Property: wpsSelector
     * {<GeoSIE.Control.WpsSelector>} The wpsSelector control instance.
     */
    wpsSelector: null,	    

    /**
     * Method: onProcessChanged
     * Called when switching from one process to another.
     * Trigger the event 'processchanged'.
     * Relay the event 'processchanged' from WpsSelector.
     * 
     * Parameters:
     * evt - {Object} An object with a process property.
     */
    onProcessChanged: function(evt) {
        // relay event
        //var zone = evt.zone;
    	var process = evt.process;
    	OpenLayers.Console.log(this.CLASS_NAME + "::onProcessChanged - process id "+process.id);
        
        this.events.triggerEvent("processchanged", process); //{enabled: false} 
    },
    
    /**
     * APIMethod: init
     * Initialize the module.
     * 
     * Parameters: 
     * div - {DOMElement}
     * 
     * Returns: 
     * {<GeoSIE.Control.WpsSelector>}
     */
    init: function(div) {
        this.events = new OpenLayers.Events(this, null, this.EVENT_TYPES);
        this.wpsSelector = new GeoSIE.Control.WpsSelector({
        	div: div,
        	processes: this.PROCESSES,
            eventListeners: {
        		'processchanged': this.onProcessChanged,
                scope: this
            }
        });
        
        return this.wpsSelector;
    }
    
};