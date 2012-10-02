/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Format/JSON.js
 * @include OpenLayers/Format/GeoJSON.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 */

/**
 * Class: GeoSIE.Control.WpsDownload
 * The WpsDownload control represents the button and dialog box to download the WPS result
 * display on the map. 
 * TODO : pas fini en attente d'un service WPS complet
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.WpsDownload = OpenLayers.Class(OpenLayers.Control, {

	/**
	 * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this control.
     * 
     * Supported control event types (in addition to those from <OpenLayers.Control>):
     * downloadwps - Triggered when TODO : 
	 */
    EVENT_TYPES: ['downloadwps'],
    
    /**
     * Property: type
     * {OpenLayers.Control.TYPES}
     */	    
    type: OpenLayers.Control.TYPE_BUTTON,

    /**
     * Property: download
     * {Object} contain the configuration to download the WPS result with 5 properties:
     * * succes - {Boolean} True when the wps process return a result. Default is false. 
     * * urlDownload - {String} Process download URL.
     * * formatsDownload - {Array(String)} Available download format.
     * * srsDownload - {Array(String)} Available download SRS.
     * * params - {Object} Parameters of WFS download requests, 
     *     Example: {gige_grou: 0565,
     *     			 gige_brni: 5845
     *              ...}
     */     
    download : {},
    
    /**
     * Constructor: GeoSIE.Control.WpsDownload
     * 
     * Parameters:
     * options - {Object}
     *  
     * Returns: 
     * {<GeoSIE.Control.WpsDownload>}
     */
    initialize: function(options) {
	    this.EVENT_TYPES =
	    	GeoSIE.Control.WpsDownload.prototype.EVENT_TYPES.concat(
	        OpenLayers.Control.prototype.EVENT_TYPES
	    );
	    this.download.succes = false;
	    OpenLayers.Control.prototype.initialize.apply(this, [options]);
	    this.createPopup();
    },


    /**
     * Method: onResultWps
     * Set the download config.
     * 
     * Parameter:
     * evt - {Object} download config.
     */
    onResultWps : function(evt){
    	if(evt == null){
    		return
    	}
    	
    	this.download = evt;
     },
    
    /**
     * Method: trigger
     * Called by a control panel when the button is clicked.
     * Show the download popup.
     */     
    trigger : function (){
    	if(this.download.succes){
        	this.showPopup();
    	} else {
    		alert(GeoSIE.Messages.noResultWpsDownloadMessage);
    	}
    },

    /**
     * Method: openUrl
     * Build the url and open a window to the page
     * where to download the WPS result.
     * 
     * Parameters:
     * format - {String} the output format request (JSON, SHP...). 
     * srs - {String} the srs request for the output.
     */
    openUrl : function(format,srs){
    	this.download.params["SRS"] = srs;
    	this.download.params["FORMAT"] = format;
    	
    	var url =this.download.urlDownload+"&opt_servicemode=sync"; // TODO : a mettre en conf
    	// Build the url
    	for (property in this.download.params) {
    		 var value = this.download.params[property];
    		 url+="&"+property+"="+value;
    	}

    	window.open(url,'_blank');
    },
    
    /**
	 * Method: createPopup
	 * Create the download popup.
	 */
    createPopup : function () {
		if($("#dialogWpsDownload")) {
			  $("#dialogWpsDownload").dialog({
					title: 'Téléchargement WPS',
			  		bgiframe: true,
					modal: true,
					autoOpen: false,
					zIndex: 20000,
					minHeight: 70,
					// height: 100,
					width: 250
				});	
			}
    },	
    
    /**
     * Method: showPopup
	 * Show the download popup.
	 */
    showPopup : function (){
 		if($("#dialogWpsDownload") && $("#dialogWpsDownloadContent")) {
 	    	var wpsDownload = this; // TODO : pas propre

 	    	// Build the selects
 			var formats = "";
 			for(var i=0;i<this.download.formatsDownload.length;i++){
 				formats+='<option>'+this.download.formatsDownload[i]+'</option>';
 			}
 			var srs = "";
 			for(var i=0;i<this.download.srsDownload.length;i++){
 				srs+='<option>'+this.download.srsDownload[i]+'</option>';
 			}
 			
			document.getElementById("dialogWpsDownloadContent").innerHTML = 
			'<form id="wpsDownloadForm">'+
				'<fieldset>'+
				    '<label for="srsWps">SRS : </label>'+
				    '<select id="srsWps">'+
				      srs +
				    '</select>'+ 	
//						'<label for="result">Résultat : </label>'+
//						'<input type="checkbox" name="result" id="result" /><br />'+
				    '<label for="formatWps">Format : </label>'+
					'<select id="formatWps">'+
					  formats +
					'</select>'+ 						
				'</fieldset>' +
				'<div style="text-align: center;">'+
					'<input type="submit" value="Télécharger" style="cursor: pointer;"  />'+
					'<input type="reset" value="Annuler" style="cursor: pointer;" />'+
				'</div>'+			
			 '</form>';
				
			$("#wpsDownloadForm").bind("submit", function(){
			    // Get the form values
	  			var srs = $("#srsWps").val();
				var format = $ ("#formatWps").val();
				wpsDownload.openUrl(format,srs);
				
				$("#dialogWpsDownload").dialog("close");
				
				// prevent normal submit
				return false; 
			});
				
			$("#dialogWpsDownload").dialog("open");
		}
    },

    /**
     * Method: closePopup
     * Close the download popup.
     */
    closePopup : function () {
        if($("#dialogWpsDownload")) {
            $("#dialogWpsDownload").dialog("close");
        }
    },    
    
    CLASS_NAME : 'GeoSIE.Control.WpsDownload'
});
