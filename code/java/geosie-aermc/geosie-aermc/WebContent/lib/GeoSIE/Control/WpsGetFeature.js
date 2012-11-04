/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Handler/Click.js
 * @include OpenLayers/Format/GeoJSON.js
 * @include OpenLayers/Format/GML.js
 * @include OpenLayers/Format/WMSGetFeatureInfo.js
 * @include OpenLayers/Format/WMSCapabilities.js
 * @include OpenLayers/Format/WMSCapabilities/v1_3_0.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 */



/**
 * Class: GeoSIE.Control.WpsGetFeature
 * The WpsGetFeature control uses a WMS query to get information about a point on the map,
 * the information in GML are used (can be completed with a form value) to make a WPS request, 
 * the result in GML or GEOJSON is displayed on the map.
 * 
 * Fires a 'getfeaturewps' event when the WPS request is successful, the event is a download 
 * object with the configuration to download the WPS result with the WpsDownload control.
 * 
 * To use this control, you must insert a ol:wpsable tag for the layer you request in context.xml:
 * (start code)
 *         <ol:wpsable xmlns:ol="http://openlayers.org/context">true</ol:wpsable>
 * (end)
 * TODO : pas fini en attente d'un service WPS complet
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.WpsGetFeature = OpenLayers.Class(OpenLayers.Control, {
    
	/**
     * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this control.
     * 
     * Supported event types:
     * getfeaturewps - Triggered when a WPS request is done. 
     */
    EVENT_TYPES: ["getfeaturewps"],
    
    /**
     * Property: type
     * {OpenLayers.Control.TYPES}
     */	       
	type : OpenLayers.Control.TYPE_TOOL,

    /**
     * APIProperty: defaultHandlerOptions
     * {Object} Additional options for the handlers used by this control, e.g.
     * (start code)
     * {
     *     'single' : true,
     *     'double' : false,
     *     'pixelTolerance' : 0
     * }
     * (end)
     */
	defaultHandlerOptions : {
		'single' : true,
		'double' : false,
		'pixelTolerance' : 0,
		'stopSingle' : false,
		'stopDouble' : false
	},
	
    /**
     * Property: currentProcess
     * {Object} The current process.
     */    
    currentProcess: null,
    
    /**
     * Property: queryableLayersName
     * {String} The layer name of the layer to query.
     */
    queryableLayerName: null, //si je passe la queryableLayer, la couche n est pas la bonne	
    
    /**
     * APIProperty: lineLayer
     * {OpenLayers.Layer.Vector} The vector layer used by this control to draw features line.
     */    
    lineLayer: null,    
	
    /**
     * Property: selectFeature
     * {OpenLayers.Feature.Vector} The feature selected by the mouse click.
     */       
	selectFeature: null,
	
    /**
     * APIProperty: maxFeatures
     * {Integer} Maximum number of features to return from a WMS query. This
     * sets the feature_count parameter on WMS GetFeatureInfo requests.
     * Default is 1.
     */
	maxFeatures: 1,
	
    /**
     * Property: format
     * {<OpenLayers.Format>} A format for parsing WPS responses (GML/GML2/GEOSJON(soon)).
     */    	
	format: null,
	
    /**
     * Property: download
     * {Object} contain the configuration to download the WPS result with 5 properties:
     * * succes - {Boolean} True when the wps process return a result. Default is false 
     * * urlDownload - {String} Process download URL.
     * * formatsDownload - {Array} Available download format.
     * * srsDownload - {Array} Available download SRS.
     * * params - {Object} Parameters of WFS download requests, 
     *     Example: {gige_grou: 0565,
     *     			 gige_brni: 5845
     *              ...}
     */    	
	download : null,
	
    /**
     * Constructor: GeoSIE.Control.WpsGetFeature
     *
     * Parameters:
     * options - {Object} The options provided to the control.
     * 
     * Returns: 
     * {<GeoSIE.Control.WpsGetFeature>}
     */
	initialize : function(options) {
	        this.EVENT_TYPES =
	        	GeoSIE.Control.WpsGetFeature.prototype.EVENT_TYPES.concat(
	            OpenLayers.Control.prototype.EVENT_TYPES
	        );
		
            this.handlerOptions = OpenLayers.Util.extend(
                {}, this.defaultHandlerOptions
            );

            this.handler = new OpenLayers.Handler.Click(
                this, {
                    'click': this.onClick,
                    'dblclick': this.onDblclick 
                }, this.handlerOptions
            );
           
            OpenLayers.Control.prototype.initialize.apply(
                    this, [options]
            );    
            
            this.createPopupGetFeatureInfo();
            this.createPopup();
	},
	

	/**
	 * Method: onClick
	 * Execute a GetFeatureInfo request on mouse click.
	 * 
	 * Parameters:
	 * evt - {Event} 
	 */
    onClick: function(evt) {
		// init download
		this.download = {};
		
		if(this.currentProcess == null || this.currentProcess.id == "fake"){
			this.lineLayer.destroyFeatures();
            this.trigger(false);
			alert(GeoSIE.Messages.selectWpsProcessMessage);
			return;
		}
		
		// find the wpsable layer
        if(this.map.layers && this.map.layers.length > 0) {
        	for(var i = 0 ; i < this.map.layers.length ; i++) {
        		var layer = this.map.layers[i];
                // TODO : comment gerer plusieurs couche wpsable ?
        		if(layer.wpsable) {
        			this.queryableLayerName=layer.name;
        		}
        	}
        }
		var layer = this.map.getLayersByName(this.queryableLayerName)[0];
        // the layer is not find
        if(!layer){
            this.opaque(false);
            this.trigger(false);
            this.deactivate();
            alert(GeoSIE.Messages.noQuerableLayerMessage); //TODO : gerer si une couche dans la liste est quand meme presente
        	return;
        }

		this.requestLayer(layer,evt);
    },	
    
    /**
     * Method: activate
     * Activate the control.
     * 
     * Returns:
     * {Boolean} Successfully activate the control.
     */    
    activate: function() {
         return OpenLayers.Control.prototype.activate.apply(this, arguments);
    },    

    /**
     * APIMethod: deactivate
     * Deactivate the control and all handlers.
     *
     * Returns:
     * {Boolean} Successfully deactivated the control.
     */
    deactivate: function() {
        return OpenLayers.Control.prototype.deactivate.apply(this, arguments);
    },    
    
    /**
     * Method: requestLayer
     * Call a getFeatureInfo on 1 layer of a layers group corresponding to 
     * the nearest current map scale (in meters).
     * 
     * Parameters:
     * layerRequest - {<OpenLayers.Layer>} The layer.
     * evt -  {Event} Mouse click event.
     */
    requestLayer: function (layerRequest,evt){
    	// GetCapabilities WMS 1.3.0 to get layer scales
    	OpenLayers.Request.GET({
    	       url : layerRequest.url+"&request=GetCapabilities&service=WMS&version=1.3.0",
    	       scope: this,
    	       success: function(response) {
		        	var format = new OpenLayers.Format.WMSCapabilities({version : "1.3.0"});
		        	var capabilities = format.read(response.responseXML || response.responseText);
		        	
		        	var layers = capabilities.capability.layers;
	        		var currentScale = Math.round(this.map.getScale());
	        		
	        		var maxLayer, groupLayers, currentLayer;
	        		var delta = 500; // cf BRGM Mael, round scale margin TODO: une couche peut ne pas etre visible mais donner un resultat avec getfeatureinfo
	        		
	        		// find the layer groups
	        		for (var i=0, len=layers.length; i<len; i++) {
	        			var layer = layers[i];
	        			if(layer.name == layerRequest.params.LAYERS && layer.nestedLayers.length>0){
	        				groupLayers = layer;
	        				break;
	        			}
	        		}	
	        		
	        		if(groupLayers){
		        		// find the layer for the current scale
			        	for (var i=0, len=groupLayers.nestedLayers.length; i<len; i++) {
			        		var layer = groupLayers.nestedLayers[i];
			        		
			        		// layer at height scale (your city)
			        		if (layer.minScale && !layer.maxScale) {
			        			maxLayer = layer;
			        		}
			        		
			        		if(layer.minScale && layer.maxScale){
		        				// Spec WMS 1.3.0 p 29: the minimum scale is inclusive and the maximum scale is exclusive
			        			if(currentScale >= layer.maxScale +delta && currentScale < layer.minScale +delta){ //+500
			        				currentLayer=layer;
			        			}
			        		}
			        	}
			        	
			        	// if layer not in scale range
			        	if(currentLayer == null){
				        	if(currentScale<maxLayer.minScale + delta){
				        		currentLayer=maxLayer;
				        	} else {
				        		// layer not visible (low scale)
				                OpenLayers.Console.error("failure");
				                this.trigger(false);
				                this.opaque(false);
				                alert(GeoSIE.Messages.noResultGetFeatureWpsMessage);			        		
				        		return
				        	}		        		
			        	}
		        		
	    				// minScale in OL means maxScale in WMS
	    				OpenLayers.Console.log(this.CLASS_NAME + "::requestLayer - Scales - max: "+currentLayer.minScale+" min: "+currentLayer.maxScale+" current: "+currentScale+" layer name: "+currentLayer.name);
	        		} else {
	        			currentLayer.name = layerRequest.params.LAYERS;
	        			OpenLayers.Console.log(this.CLASS_NAME + "::requestLayer - layers group not found");
	        		}

		        	// call GetFeatureInfo
		    		var srs = layerRequest.projection && layerRequest.projection.getCode() ||
		    		layerRequest.map && layerRequest.map.getProjectionObject().getCode();
		    		
		    		var params = {
		    	            VERSION: "1.1.1",
		    	            REQUEST: "GetFeatureInfo",
		    	            EXCEPTIONS: "application/vnd.ogc.se_xml", //  XML
		    	            BBOX: this.map.getExtent().toBBOX(),
		    	            X: evt.xy.x,
		    	            Y: evt.xy.y,
		    	            INFO_FORMAT: "application/vnd.ogc.gml",//'text/plain',  text/xml
		    	            QUERY_LAYERS: currentLayer.name,
		    	            FEATURE_COUNT: this.maxFeatures,
		    	            SRS: srs,
		    	            Layers: currentLayer.name, //
		    	            Styles: '',
		    	            WIDTH: this.map.size.w,
		    	            HEIGHT: this.map.size.h,
		    	            format: "image/jpeg"
		    	    };

		    	    this.opaque(true);
		    	    OpenLayers.loadURL(layerRequest.url, params, this, this.callProcess, this.onError);	  	        		
    	       },
    	       
        	   failure: function() {
                   OpenLayers.Console.error("failure");
                   this.trigger(false);
                   this.opaque(false);
                   alert(GeoSIE.Messages.errorWMSGetCapabilitiesMessage); 
               }
    	
    	});    	
    },
    
    /**
     * Method: callProcess
     * Read the WMS GetFeatureInfo response and
     * call the WPS process or display a dialog to enter the parameters.
     * 
     * Parameters:
     * response - {XMLHttpRequest}
     */    
    callProcess: function(response){
        var format = new OpenLayers.Format.WMSGetFeatureInfo();
    	
        var feature = format.read(response.responseText || response.responseXML ); //responseXML
        
        // not click on a troncon
        if (feature.length == 0){
            this.trigger(false);
        	this.opaque(false);
        	alert(GeoSIE.Messages.noResultGetFeatureWpsMessage);
        	return;
        }

        if (feature.length>2){
            this.opaque(false);
        	alert(GeoSIE.Messages.tooMuchFeaturesWpsMessage);
        } else {
			this.selectFeature = feature[0];
			
			// process has a dialog
			if(this.currentProcess.dialog){
				// fill the form
				if(this.currentProcess.setDialogContent && this.currentProcess.paramsForm){
			    	var paramsForm = this.currentProcess.paramsForm;
			    	var context = {};
			    	for (var i=0; i<paramsForm.length; i++){
			    		var paramForm = paramsForm[i];
			    		var valWms = this.selectFeature.attributes[paramForm.wms];
			    		// user params request for the form
			    		if(valWms){
			    			context[paramForm.id]=valWms;
			    		} else { //TODO : gerer d'une autre facon peut etre
			    			alert(GeoSIE.Messages.noToponymeWpsMessage);
			    			this.trigger(false);
			    			this.opaque(false);
			    			return;
			    			//this.map.process.setDialogContent(paramForm.id,GeoSIE.Messages.noToponymeWpsMessage);
			    		}
			    	}
	    			this.currentProcess.setDialogContent(context);
				}
	            this.opaque(false);
	            if(this.currentProcess.id == "getfeatureinfo"){
	            	this.showPopupGetFeatureInfo();
	            } else {
	            	this.showPopup();
	            }
			} else {
        	    // Process without a form
				this.callWPS();
			}
         }
    },
    
    /**
     * Method: onError
     * Called when an error occurs with the AJAX request
     *
     * Parameters:
     * response - {AJAX response}
     */
    onError: function(response){
    	OpenLayers.Console.error("failure to call GetFeatureInfo");
        this.trigger(false);
        this.opaque(false);
        alert(GeoSIE.Messages.errorWMSGetFeatureInfoMessage);
    },
    
    /**
     * Method: callWPS
     * Build the request and call the WPS process. 
     * 
     * Parameters:
     * options - {Array} values from the dialog.
     */      
    callWPS: function (options){ 
    	var params = {};
   	
    	// set the getfeatureinfo values to the params for the WPS request
    	var paramsProcess = this.currentProcess.params;
    	for (var i=0; i<paramsProcess.length; i++){
    		var param = paramsProcess[i];
    		var valWms = this.selectFeature.attributes[param.wms];
    		if(valWms){
        		params[param.id] = valWms;
    		}
    	}
    	
    	// values from the dialog
    	if(options){
    		for (var i=0; i<options.length; i++){
    			var option=options[i];
    			params[option.id] = option.value;
    		}
    	}
   	
    	// Warning : the parameter must be in capital letter (cf BRGM Mael with FME)
    	// Default params for display WPS result
    	params["SRS"] = this.map.projection;
    	params["FORMAT"] = this.currentProcess.format;
    	
    	OpenLayers.Console.log(params);
    	
    	var header = null;
    	if(this.currentProcess.format === "GML2" || this.currentProcess.format === "GML"){
            this.format = new OpenLayers.Format.GML();
            header = "application/xml; charset=utf-8";
    	} else {
            this.format = new OpenLayers.Format.JSON(); // TODO: GEOJSON or JSON ?
            header = "application/json; charset=utf-8";
    	}

        //OpenLayers.loadURL(fmeURL, params, this, displayResponse, displayResponse);
        // TODO : tester POST
        OpenLayers.Request.GET({
            url: this.currentProcess.url,
            params: params,
            headers: {
           	 "Content-Type": header
            },
            scope: this,
            success: function(request) {
            	// set the download 
            	this.download.params = params;
                this.download.formatsDownload = this.currentProcess.formatsDownload;
                this.download.urlDownload = this.currentProcess.urlDownload;
                this.download.srsDownload = this.currentProcess.srsDownload;
            	this.addResult(request);
		    },
            failure: function() {
                OpenLayers.Console.error("failure");
                this.trigger(false);
                this.opaque(false);
                alert(GeoSIE.Messages.errorServerWpsMessage);
            }
        });
    },


    /**
     * Method: addResult
     * Read the WPS response and add features to the lineLayer
     * (the last features are destroyed).
     * 
     * Parameters:
     * response - {XMLHttpRequest}
     */
    addResult: function(response){
    	// TODO : probleme avec le debut du JSON il a  { "resultat" :{"type":"FeatureCollection"
    	// il ne faut pas de { "resultat" : ... }
    	// cf exemple OL en ajoutant resultat ou pas : http://openlayers.org/dev/examples/vector-formats.html
    	
    	// check if service not failed TODO : mieux gerer l'erreur
    	if(response.responseText.indexOf("FAILED") >= 0 ){
            this.trigger(false);
        	this.opaque(false);
    		alert("Error: Data Streaming Service FAILED");
        	return;
    	}
    	
        var featureList = this.format.read(response.responseText);
        if(featureList == null){
            this.trigger(false);
        	this.opaque(false);
    		alert(response.responseText);
        	return;
        }
        
        // FIXME : le GeoJSON de FME est mal formee, 1 attribut de trop
        /*var format2 = new OpenLayers.Format.GeoJSON();
        var response2 = featureListTemp.resultat;
        var featureList = format2.read(response2);*/

		// delete the features
        if (featureList && featureList.length>0){
        	OpenLayers.Console.log(this.CLASS_NAME + "::addResult - nbr de features a afficher : "+featureList.length);
        		
            this.lineLayer.destroyFeatures(); // can crash
        	//this.lineLayer.removeAllFeatures();
            // Pas besoin
            /*for(var i=0;i<featureList.length;i++){
            	var line = featureList[i];
                line.style = this.lineLayer.styleMap.createSymbolizer(line, 'temporary');
            }*/
            this.lineLayer.addFeatures(featureList);
            this.trigger(true);
            this.opaque(false);
        } else {
            OpenLayers.Console.error("failure");
            this.trigger(false);
            this.opaque(false);
            alert(GeoSIE.Messages.noResultWpsMessage);
        }
        
    },     
    
    /**
     * Method: trigger
     * Called when a WPS request is done, the getfeaturewps event is triggered.
     * 
     * Parameter:
     * succes - {Boolean} True if the WPS request get values.
     */
    trigger : function (succes){
    	this.download.succes = succes;
        this.events.triggerEvent('getfeaturewps', this.download);
    },

    /**
     * Method: setCurrentProcess
     * Set the currentProcess value.
     * 
     * Parameters:
     * evt - {Event} The process.
     */
    setCurrentProcess : function(evt) {
    	this.currentProcess = evt.process;
    },
    
    /**
	 * Method: createPopupGetFeatureInfo
	 * Create the GetFeatureInfo popup.
	 */    
    createPopupGetFeatureInfo : function () {
		var wps = this;
    	if($("#dialogGetFeatureInfo")) {
			  $("#dialogGetFeatureInfo").dialog({
					title: 'Service WPS',
			  		bgiframe: true,
					modal: true,
					autoOpen: false,
					zIndex: 20000,
					minHeight : 90,
					// height: 100,
					width: 330
				});	
		}
    },
    
    /**
	 * Method: createPopup
	 * Create the wps popup.
	 */
    createPopup : function () {
    	var wps = this;// TODO : pas propre
		if($("#dialogWps")) {
			  $("#dialogWps").dialog({
					title: 'Service WPS',
			  		bgiframe: true,
					modal: true,
					autoOpen: false,
					zIndex: 20000,
					minHeight : 80,
					// height: 100,
					width: 330
				});	
		}
    },	    

    /**
     * Method: showPopup
	 * Show the wps popup.
	 */
    showPopup : function (){
 		if($("#dialogWps") && $("#dialogWpsContent")) {
 			var wps = this;
 			
 			document.getElementById("dialogWpsContent").innerHTML = this.currentProcess.dialogContent;

 			$("#wpsForm").bind("submit", function(){
 	  			var options = wps.currentProcess.options();
			    // Get the form values
	  			wps.callWPS(options);
	  			wps.opaque(true);
				
				$("#dialogWps").dialog("close");
				
				// prevent normal submit
				return false; 
			});
 			
			$("#dialogWps").dialog("open");
		}
    },

    /**
     * Method: showPopupGetFeatureInfo
	 * Show the getfeatureinfo popup.
	 */    
    showPopupGetFeatureInfo: function (){
 		if($("#dialogGetFeatureInfo") && $("#dialogGetFeatureInfoContent")) {
 			document.getElementById("dialogGetFeatureInfoContent").innerHTML = this.currentProcess.dialogContent;    
 			
 			$("#wpsFormDistance").bind("submit", function(){
				$("#dialogGetFeatureInfo").dialog("close");
				// prevent normal submit
				return false; 
			}); 			
 			
			$("#dialogGetFeatureInfo").dialog("open");
		}
    },
    
    /**
     * Method: closePopup
     * Close the wps popup.
     */
    closePopup : function () {
        if($("#dialogWps")) {
            $("#dialogWps").dialog("close");
        }
    },    

    /**
     * Method: closePopupGetFeatureInfo
     * Close the getfeatureinfo popup.
     */
    closePopupGetFeatureInfo : function () {
        if($("#dialogGetFeatureInfo")) {
            $("#dialogGetFeatureInfo").dialog("close");
        }
    },      
    
    /**
     * Method: opaque
     * Grey out the web page.
     * 
     * Parameters:
     * isOpaque - {Boolean} True to grey out the page.
     */
    opaque : function(isOpaque){
    	var e = document.getElementById('opaque');
    	if (e){
    		if(isOpaque){
        		e.style.display='block';
    		} else {
        		e.style.display='none';
    		}
    	}
    },
    
	CLASS_NAME : 'GeoSIE.Control.WpsGetFeature'
});