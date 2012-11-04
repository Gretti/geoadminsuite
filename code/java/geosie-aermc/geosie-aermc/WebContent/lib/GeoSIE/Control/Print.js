/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Format/JSON.js
 * @include OpenLayers/Format/GeoJSON.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 */
 
/**
 * Class: GeoSIE.Control.Print
 * Class to build the configuration for
 * the print, communicate with the print module and display dialog widget.
 * 
 * This class will automatically pick the layers from the OL map and create the
 * configuration structure accordingly.
 * 
 * As we often want a sligtly different styling or minScale/maxScale, an
 * override functionallity is provided that allows to override the OL layers
 * configuration, this can be used as well if a layer's URL points to a
 * TileCache service to allow the print module to access the WMS service
 * directly.
 * 
 * An override structure may look like this: 
 * (start code)
 *    { 'layerName1': { visibility: false }, 
 *      'layerName2': { visibility: false, 300: { visibility: true } 
 *                    } 
 *    } 
 * (end)
 * 
 * In this example:
 * - the OL layer named "layerName1" is never printed. 
 * - the OL layer named "layerName2" is visible only when printed at 300DPI.
 * 
 * Based on MapFish PrintProtocol.js at <http://www.mapfish.org/doc/print/configuration.html> 
 */
GeoSIE.Control.Print = OpenLayers.Class(OpenLayers.Control, {
	
	/**
     * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this control.
     * 
     * Supported event types:
     * impression - Triggered when a TODO :
     */
    EVENT_TYPES: ["impression"],
    
    /**
     * Property: type
     * {OpenLayers.Control.TYPES}
     */	       
	type : OpenLayers.Control.TYPE_BUTTON,
	
    /**
     * APIProperty: vectorLayer
     * {<OpenLayers.Layer.Vector>} The vector layer used for displaying results.
     */ 
    vectorLayer: null,
    
    /**
	 * APIProperty: context
     * The WMC context as read by OpenLayers.
	 */
    context: null,
 
    /**
	 * Property: jsonFormatter
     * {<OpenLayers.Format.JSON>}
	 */
    jsonFormatter: null,
 
    /**
     * Property: dialogContent
     * {String} HTML content for the print dialog.
     */
    dialogContent: '<form id="printForm">'+
						'<fieldset>'+
							'<label for="title">Titre : </label>'+
							'<input type="text" name="title" id="title" value="'+GeoSIE.Config.PRE_TITLE_PRINT+'" style="width: 192px;"/><br /><br />'+ 
							//'<label for="result">Résultat : </label>'+
							//'<input type="checkbox" name="result" id="result" /><br />'+
							'<label for="format">Format : </label>'+
							'<select id="format">'+
							'</select><br /><br />'+ 
							'<label for="template">Template : </label>'+
							'<select id="template">'+
							'</select>'+ 					
						'</fieldset>'+
						'<div style="text-align: center;">'+
							'<input type="submit" style="cursor: pointer;" value="Imprimer" />'+
							'<input type="reset" style="cursor: pointer;" value="Annuler" />'+
						'</div>'+
					'</form>',
    
    /**
	* Constructor: GeoSIE.Control.Print
    * 
    * Returns: 
    * {<GeoSIE.Control.Print>}
	*/
    initialize : function(options) {	
	    this.EVENT_TYPES =
	    	GeoSIE.Control.Print.prototype.EVENT_TYPES.concat(
	        OpenLayers.Control.prototype.EVENT_TYPES
	    );    
        OpenLayers.Util.extend(this, options);
        
        OpenLayers.Control.prototype.initialize.apply(
                this, [options]
        );      

		this.jsonFormatter = new OpenLayers.Format.JSON();
		
        this.createPopup();
    },	

    /**
     * Method: trigger
     * Called by a control panel when the button is clicked.
     */     
    trigger : function (){
        this.showPopup();
    },    
    
    /**
	 * Method: createPopup
	 * Create the print popup.
	 */
    createPopup : function () {
		if($("#dialogPrint")) {
			  $("#dialogPrint").dialog({
					title: 'Impression PDF',
			  		bgiframe: true,
					modal: true,
					autoOpen: false,
					zIndex: 20000,
					minHeight : 90,
					//height: 100
					width:330
				});
			  
		   document.getElementById("dialogPrintContent").innerHTML = this.dialogContent;
		   
		   // Set the formats and templates
		   var formatsHtml = "";
		   for(var f=0; f<GeoSIE.Config.PRINT_FORMATS.length; f++){
			   var format = GeoSIE.Config.PRINT_FORMATS[f];
			   formatsHtml += '<option value="' + format.value + '">' + format.libelle + '</option>';
		   }
		   $('#format').html(formatsHtml);
		   
		   var templatesHtml = "";
		   for(var t=0; t<GeoSIE.Config.PRINT_TEMPLATES.length; t++){
			   var template = GeoSIE.Config.PRINT_TEMPLATES[t];
			   templatesHtml += '<option value="' + template.value + '">' + template.libelle + '</option>';
		   }
		   $('#template').html(templatesHtml);

		   // Bind the button
 	    	var that = this;
			
			$("#printForm").bind("submit", function(){
			    // Get the form values
				var title = $( "#title" ).val();
				//var result = $( "input[name=result]" ).is(':checked');
				var legend = true;
				var formatPrint = $("#format").val();
				var template = $("#template").val();
				
				var confBackground = that.getBackground(formatPrint,template,title);
				var layout = formatPrint+" "+template;
				
				that.impressionMapFish(title,null,legend,layout,confBackground);
				
				$("#dialogPrint").dialog("close");
				
				// prevent normal submit
				return false; 
			});
		   
		}	 
    },	
    
    /**
	 * Method: showPopup
	 * Show the print popup.
	 */
    showPopup : function (){
 		if($("#dialogPrint") && $("#dialogPrintContent")) {
			$("#title").val(GeoSIE.Config.PRE_TITLE_PRINT);
			$("#dialogPrint").dialog("open");
		}
    },

    /**
     * Method: getBackground
     * Get the config background pdf and svg depending the format, the orientation and
     * text length. Empirical method. 
     * TODO : To complete when you add a new format to print.
     * 
     * Parameters:
     * formatPrint - {String} A4 or A3
     * template - {String} portrait or paysage
     * title - {String} Title content
     * 
     * Return:
     * {Object} backgroundPdf {String} pdf filename for background
     *          backgroundSvg {String} svg filename for header over the background
     *          showHeaderShort {Boolean} display short header/map or not
     *          showLegendNextPage {Boolean} display the legend one map or not
     */
    getBackground : function(formatPrint, template, title){
    	var confBackground = {};
    	
    	// To load the header/template for long or short title
    	var textWidth = this.measureWidthText(title);
		OpenLayers.Console.log(this.CLASS_NAME + "::getTitleType() textWidth: "+textWidth);
		
		var backgroundPdf = "";
		var backgroundSvg = ""; // header
		var showHeaderShort = false;
		var showLegendNextPage = false;
		
    	if(formatPrint == "A4" && template == "paysage"){
    		if(textWidth <= 682){ //560
        		backgroundPdf = "A4_SIE_map_paysage_court.pdf";
        		backgroundSvg = "A4_SIE_map_paysage_court.svg";
        		showHeaderShort = true;
    		 } else {
         		backgroundPdf = "A4_SIE_map_paysage_long.pdf";
        		backgroundSvg = "A4_SIE_map_paysage_long.svg";
    		 }
    	} else if(formatPrint == "A3" && template == "paysage"){
	   		 if(textWidth <= 964){
	        	backgroundPdf = "A3_SIE_map_paysage_court.pdf";
	        	backgroundSvg = "A3_SIE_map_paysage_court.svg";
        		showHeaderShort = true;
			 } else {
	        	backgroundPdf = "A3_SIE_map_paysage_long.pdf";
	        	backgroundSvg = "A3_SIE_map_paysage_long.svg";
			 }
    	} else if(formatPrint == "A4" && template == "portrait"){
	   		 if(textWidth <= 658){ //560
	        	backgroundPdf = "A4_SIE_map_portrait_court.pdf";
	        	backgroundSvg = "A4_SIE_map_portrait_court.svg";
        		showHeaderShort = true;
			 } else {
        		backgroundPdf = "A4_SIE_map_portrait_long.pdf";
        		backgroundSvg = "A4_SIE_map_portrait_long.svg";
			 }
     		 showLegendNextPage = true;

    	} else if(formatPrint == "A3" && template == "portrait"){
	   		 if(textWidth <= 939){
		        backgroundPdf = "A3_SIE_map_portrait_court.pdf";
		        backgroundSvg = "A3_SIE_map_portrait_court.svg";
        		showHeaderShort = true;
			 } else {
	        	backgroundPdf = "A3_SIE_map_portrait_long.pdf";
	        	backgroundSvg = "A3_SIE_map_portrait_long.svg";
			 }    	
     		 showLegendNextPage = true;
    	}
    	
    	confBackground.backgroundPdf = backgroundPdf;
    	confBackground.backgroundSvg = backgroundSvg;
    	confBackground.showHeaderShort = showHeaderShort;
    	confBackground.showLegendNextPage = showLegendNextPage;
    	
    	return confBackground;
    },

    /**
     * Method: measureWidthText
     * Handy JavaScript to meature the size taken to render the supplied text;
     * you can supply additional style information too if you have it to hand.
     * TODO : a tester sur d'autre config pc
     * 
     * Parameters:
     * pText - {String} The text to measure.
     * 
     * Return:
     * {Integer} text width in pixel.
     */
    measureWidthText : function(pText) {
	     var lDiv = document.createElement('lDiv');
	
	     document.body.appendChild(lDiv);
	
	     lDiv.style.fontSize = "16px";
	     lDiv.style.position = "absolute";
	     lDiv.style.left = -1000;
	     lDiv.style.top = -1000;
	     lDiv.style.fontFamily = "Helvetica";
	
	     lDiv.innerHTML = pText;
	
	     var width = lDiv.clientWidth;
	
	     document.body.removeChild(lDiv);
	     lDiv = null;
	     
	     return width;
    },
    
    /**
     * Method: closePopup
     * Close the print popup.
     */
    closePopup : function () {
        if($("#dialogPrint")) {
            $("#dialogPrint").dialog("close");
        }
    },
    
    /**
	 * Method: impressionMapFish
	 * Print (pdf) the layer(s) and Resultat(s) with MapFish print servlet.
	 * 
	 * Parameters: 
	 * title - {String} The title of the map. 
	 * showRecords - {Boolean} Display the Resultat. 
	 * showLegends - {Boolean} Display the legend.
	 * layout - {String} The print document layout (A4 portrait, A3 portrait, A4 paysage, A3 paysage).
	 * confBackground - {Ojbect} Config for background.
	 */	
	impressionMapFish: function(title,showRecords,showLegends,layout,confBackground){
    	OpenLayers.Console.log(this.CLASS_NAME + "::impressionMapFish");
    	
		// id for the records
        /*var featuresToPrint = new Array();
        var select = GeoSIE.Brique.getSelection();
		
        for (var i=0; i<select.length; i++) {
        	var feature = select[i];
        	if (feature.fid) {
        		featuresToPrint.push(feature.fid); //.split(".")[1]
        	}
        }*/
        
		// Obstacles table data
		//var featuresToTablePrint = this.convertFiches(select);
		
		// Data au format MapFish see yaml file
		/*var dataTableTemp = {
			data:  new Array(),
			columns: ['obs_original_id', 'obs_nom','obs_nom_source','etat'] // ,'link'
		};*/
		
		// Get the copyrights (layer visible or not) and map Background
        var attributions = [];
//        var mapBackgrounds = [];
        if (this.map && this.map.layers) {
            for(var i=0, len=this.map.layers.length; i<len; i++) {
                var layer = this.map.layers[i];
                if (layer.attribution) {
                    // add attribution if duplicates shall be ignored generally
                    // or if the current attribution string is unique
                    if (OpenLayers.Util.indexOf(attributions, layer.attribution) === -1) {
                        attributions.push( layer.attribution );
                    }
                };
//                if (layer.mapBackground) {
//                    // add mapBackground if duplicates shall be ignored generally
//                    // or if the current mapBackground string is unique
//                    if (OpenLayers.Util.indexOf(mapBackgrounds, layer.name) === -1) {
//                    	mapBackgrounds.push( layer.name );
//                    }
//                };                
            } 
        }
		
        // builder the footer
        var sources = "Sources: "+ attributions.join(', ')+"\n";
        $.datepicker.setDefaults( $.datepicker.regional[ "fr" ] );
        var dateString = $.datepicker.formatDate('dd/mm/yy', new Date()); 
        var copyright = "© Onema, " + dateString.substring(6,10) + " - Date d'impression: "+dateString; 
        
		// legend
		var legendJSON = this.convertLegend();
        var printContext = {
        		layout: layout,
        		srs: this.map.projection,
        		units: this.map.units,
        		dpi: 96,
        		layers: [],
    			legends: legendJSON,
    			footer: sources+copyright,
//    			mapBackground: mapBackgrounds.join(', '),
    			urlAppli: this.getApplicationUrl(), 
    			outputFilename: GeoSIE.Config.OUTPUT_FILENAME,
    			// Data resume table
    			//data: dataTableTemp,
    			// Data for the records
        		//features: featuresToPrint,
    			
        		// Conditions
    			//showTable: showRecords,
    			//showRecords: showRecords,
    			showLegends: showLegends,
    			showHeaderShort: confBackground.showHeaderShort,
    			showHeaderLong: !confBackground.showHeaderShort,
    			showLegendNextPage: confBackground.showLegendNextPage,
    			showLegendMap:  !confBackground.showLegendNextPage,
    			
    			// Background
    			backgroundPdf: confBackground.backgroundPdf,
    			backgroundSvg: confBackground.backgroundSvg,
    			
        		pages: [
            		{
                		center: [this.map.getCenter().lon, this.map.getCenter().lat],
                		scale: GeoSIE.Brique.getNearestScale(),

    					showOverView:false,
                		mapTitle: title 
            		}
                    /*
                     * to display the table and record in the main page
					 * { data : dataTableTemp, showMap:false,
					 * showOverView:false, showLegends:false,
					 * showTable:showRecords, showRecords:showRecords, }
					 */        		
    		    ]
            };
            
            for(var i=0; i<this.map.layers.length; i++) {
            	var layer = this.map.layers[i];
    	    	// Don't add vector layer, base layer
            	if (layer.isVector || layer.isBaseLayer || !layer.params) {
    	    		continue;
    	    	}

            	var printLayer = {};
            	printLayer.type = layer.params["SERVICE"];
            	printLayer.layers = [layer.params["LAYERS"]];
    			printLayer.titles=[layer.name];
            	printLayer.baseURL = layer.url;
            	printLayer.format = layer.params["FORMAT"];
            	printLayer.opacity = layer.opacity;
            	printLayer.customParams = {order: i};
    			printLayer.displayInLayerSwitcher = layer.displayInLayerSwitcher;
    			printLayer.visibility = layer.visibility;
            	printContext.layers.push(printLayer);
            }
            
    		// Add the Vector Layer with the label for each feature to the spec
    		/*if(select.length>0 && showRecords){
    			var vectorLayerLabel = this.addLabelToVectorLayer(this.vectorLayer,featuresToPrint,select);
    	        var vectorLayerLabelJSON = this.convertVectorLayer(vectorLayerLabel);
    		    printContext.layers.push(vectorLayerLabelJSON);
    		}*/

            var specTxt = this.jsonFormatter.write(printContext);
            
            var printForm = document.createElement("form");
            printForm.method = "post";
            printForm.action = GeoSIE.Config.PRINT_URL;
            printForm.target = "_blank";
//            printForm.acceptcharset = "UTF-8";
//            printForm.enctype ="application/json";

            var printSpec = document.createElement("input");
            printSpec.name = "spec";
            printSpec.type = "hidden";
            printSpec.value = specTxt;

            // Yaml config from URL
            /*var printYaml = document.createElement("input");
            printYaml.name = "yamlURL";
            printYaml.type = "hidden";
            printYaml.value="http://localhost:8080/Geobs-WebStruts/config/print-layout-url.yaml";
            */
            // Yaml config from client TODO : a tester
            /*var printYaml = document.createElement("input");
            printYaml.name = "yaml";
            printYaml.type = "hidden";
            printYaml.value="dpis:....";
            */
            
    		printForm.appendChild(printSpec);
    		document.body.appendChild(printForm);

            printForm.submit();	
            
//            // methode POST avec appel servlet print Mapfish
//            var charset = "UTF-8";
//            /* +document.characterSet */
//            // POST {PRINT_URL}/create.json?url={PRINT_URL}%2Fcreate.json
//            var printURL = GeoSIE.Config.printURL;
//            
//            var params = OpenLayers.Util.applyDefaults({
//                url: printURL
//            }, this.params);
//            
//            // TODO : tester la methode GET 
//            OpenLayers.Request.POST({
//                url: printURL+'create.json',
//                data: specTxt,
//                params: params,
//                // comment the proxy the use the proxy
//                proxy: null,
//                headers: {
//            	    // 'serverUrl' : printUrl+"create.json", // for the java
//					// proxy
//                    'CONTENT-TYPE': "application/json; charset=" + charset
//                },
//                callback: function(request) {
//                	this.closePopup();
//                	//$("#content").empty();
//                	if (request.status >= 200 && request.status < 300) {
//                        var json = new OpenLayers.Format.JSON();
//                        var answer = json.read(request.responseText);
//                        if (answer && answer.getURL) {
//                        	window.location = answer.getURL;
//                        } else {
//                        	OpenLayers.Console.error("failure impressionMapFish: "+request.responseText);
//                        	alert("Erreur lors de l'impression : "+request.responseText);
//                        }
//                    } else {
//                    	OpenLayers.Console.error("failure to call print service: "+printURL);
//                    	alert("Erreur du service d'impression : "+request.responseText);
//                    }
//                },
//                scope: this
//            });
            
	},    
    
	/**
	 * Method: addLabelToVectorLayer
	 * Return a copy of the Vector Layer with the features selected and a style
	 * with a label for each features.
	 * 
	 * Parameters:
	 * olLayer - {<OpenLayers.Feature.Vector>} The Vector Layer.
	 * featuresToTablePrint - {Object} The array with the label.
	 * selectFeatures - {Object} The selected features.
	 * 
	 * Returns: 
	 * {<OpenLayers.Layer.Vector>} a Vector Layer with label for the
	 * features.
	 */
	addLabelToVectorLayer: function(olLayer,featuresToTablePrint,selectFeatures){
		var layerTemp = olLayer.clone();
		layerTemp.features = [];
		
		for (var i=0; i<featuresToTablePrint.length; i++) {
			// the label display on the map
			var label = featuresToTablePrint[i];//.obs_original_id; //FIXME
			var feature = selectFeatures[i].clone();
			
	        var oLstyle = feature.style || olLayer.style ||
	        olLayer.styleMap.createSymbolizer(feature, feature.renderIntent);
	        
	        // Add the label and style for the label to the style
	        oLstyle.label=label;
			/*
			 * Valid values for horizontal alignment: "l"=left, "c"=center,
			 * "r"=right. Valid values for vertical alignment: "t"=top,
			 * "m"=middle, "b"=bottom.
			 */
	        oLstyle.labelAlign='lb';
	        oLstyle.labelXOffset=8;
	        // oLstyle.labelYOffset=1;
	        oLstyle.fontColor='#1111CC';
	        // oLstyle.backGroundColor='#FF00FF';
	        /* Supported itext fonts: COURIER, HELVETICA, TIMES_ROMAN */
	        // oLstyle.fontFamily='HELVETICA'
	        // oLstyle.fontSize='12';
	        // oLstyle.fontWeight='bold';
	        
	        feature.style=oLstyle;

	        layerTemp.features.push(feature);
		}

		return layerTemp;
	},
	
//	/**
//	 * Method: convertFiches 
//	 * 
//	 * Return the features formated to the print table (attributes block) for Geobs
//	 * {
//	 * 	obs_original_id: {String}
//	 * 	obs_nom: {String} 
//	 * 	obs_nom_source: {String}
//	 * 	etat: {String} 
//	 * }
//	 * 
//	 * Parameters: 
//	 * features - {<OpenLayers.Feature.Vector>} The features.
//	 * 
//	 * Returns: 
//	 * {Object} The config with the features info display in Resultat
//	 * to print.
//	 */
//	convertFiches: function(features){
//       var featuresToPrint = new Array();
//	   var feature = [];
//	    // voir class FeatureList redraw pour la logique
//        for (var i = 0, l = features.length; i < l; i++) {
//            feature = features[i];
//			
//            // ms:OBSTACLE
//            if(feature.fid.indexOf("OBSTACLE.") >= 0) {
//            	// Etat
//	            var etat = "Non traité";
//	            if(feature.attributes.obs_id_source == '0') {
//	            	if(feature.attributes.obs_id_etat == '0') {
//	            		etat = "Validé";
//	            	}
//	            	if(feature.attributes.obs_id_etat == '1') {
//	            		etat = "Confirmé";
//	            	}
//	            	if(feature.attributes.obs_id_etat == '2') {
//	            		etat = "Non validé";
//	            	}
//	            }
//	            // Id d'affichage
//	            var id_affichage = "";
//	            if(feature.attributes.obs_id_source == '0') {
//	            	id_affichage = "ROE " + feature.attributes.obs_original_id;
//	            } else { 
//	            	id_affichage = feature.attributes.obs_original_id;
//	            }
//
//				featuresToPrint.push({
//							   obs_original_id:id_affichage,
//							   obs_nom :feature.attributes.obs_nom==null?"Non renseigné":feature.attributes.obs_nom,
//							   obs_nom_source : feature.attributes['obs_nom_source'],
//							   etat: etat
//				});	            
//	        }
//			
//	        // ms:OBSTACLE_MASQUE
//	        if (feature.fid.indexOf("OBSTACLE_MASQUE.") >= 0) {
//				featuresToPrint.push({
//							   obs_original_id:feature.id, 
//							   obs_nom :feature.attributes.obs_nom==null?"Non renseigné":feature.attributes.obs_nom,
//							   obs_nom_source : feature.attributes['obs_nom_source'],
//							   etat: 'Masqué'
//				});	   				
//	        }
//        }	
//		return featuresToPrint;
//	},    
    
    /**
	 * Method: convertVectorLayer 
	 * Builds the layer configuration from an {OpenLayers.Layer.Vector} layer.
	 * (See MapFish PrintProtocol.js)
	 * The structure expected from the print module is:
	 * (start code)
	 * { 
	 * 	type: 'Vector' 
	 * 	styles: {Object} 
	 * 	styleProperty: {String} 
	 * 	geoJson: {Object}
	 * 	opacity: {Float} 
	 * 	name: {String}
	 * } 
	 * (end)
	 * 
	 * Parameters: 
	 * olLayer - {OpenLayers.Layer.Vector} The OL layer.
	 * 
	 * Returns: 
	 * {Object} The config for this layer.
	 */
    convertVectorLayer: function(olLayer) {
        var olFeatures = olLayer.features;
        var features = [];
        var styles = {
        };
        var formatter = new OpenLayers.Format.GeoJSON();
        var nextId = 1;
        for (var i = 0; i < olFeatures.length; ++i) {
            var feature = olFeatures[i];
            var style = feature.style || olLayer.style ||
                        olLayer.styleMap.createSymbolizer(feature, feature.renderIntent);
            var styleName;
            if (style._printId) {
                // this style is already known
                styleName = style._printId;
            } else {
                // new style
                style._printId = styleName = nextId++;
                styles[styleName] = style;

                // Make the URLs absolute
                if (style.externalGraphic) {
                   style.externalGraphic = this.relativeToAbsoluteURL(style.externalGraphic);
				   // style.externalGraphic = style.externalGraphic;
                }
            }
            var featureGeoJson = formatter.extract.feature.call(formatter, feature);

            // OL just copy the reference to the properties. Since we don't want
            // to modify the original dictionary, we make a copy.
            featureGeoJson.properties = OpenLayers.Util.extend({
                _style: styleName
            }, featureGeoJson.properties);
            for (var cur in featureGeoJson.properties) {
                var curVal = featureGeoJson.properties[cur];
                if (curVal instanceof Object && !(curVal instanceof String)) {
                    // OL.Format.Json goes into an infinite recursion if we have
					// too
                    // complex objects. So we remove them.
                    delete featureGeoJson.properties[cur];
                }
            }

            features.push(featureGeoJson);
        }
        for (var key in styles) {
            delete styles[key]._printId;
        }

        var geoJson = {
            "type": "FeatureCollection",
            "features": features
        };
        return OpenLayers.Util.extend(this.convertLayer(olLayer), {
            type: 'Vector',
            styles: styles,
            styleProperty: '_style',
            geoJson: geoJson,
            name: olLayer.name,
            opacity:  (olLayer.opacity != null) ? olLayer.opacity : 1.0
        });
    },	

    /**
     * Method: convertLegend
     * 
     * Builds the legend configuration from this legend.
     * The structure expected from the print module is:
     * 
     * [{
     *   name: {String}
     *   classes: [{
     *      name: {String}
     *      icons: [{String} (the URL icon)]
     *   }]
     * },{...}]
     * 
     * Returns: 
     * {Object} The config for this legend.
     */
	convertLegend: function(){
	  var legendsSpec = new Array();
      var layers = this.map.layers;
      
      for (var l = layers.length - 1, i = l; i >= 0; i--) {
			var layer = layers[i];
			
			// Don't show the legend for layer if not visible ?
            //if (layer.visibility) { 
                var icons=[];
			    var icon;
				var title = layer.name;
				var context = this.getLayerContextByTitle(title);

				// If there is a fixed legend
				if (context && context.styles && context.styles[0] &&
					context.styles[0].legend && context.styles[0].legend.href /* wmc:StyleList/wmc:Style[@current='1']/wmc:LegendURL */) {
					icon = context.styles[0].legend.href;
				}
				// Otherwise, look for a legend service
				else {
					// Is there any custo legend
					if (false) { //???
					}
					// no legend
					else {
					   icon = "noLegend";
					}
				}
				
				icons.push(icon);
				
				// Don't show the legend for layer if there is no legend
				if(icon != "noLegend"){
					legendsSpec.push({
							name: title,
							//icons: icons
							classes: [{
							  name:"",
							  icons:icons
							}] // GeoExt.LegendPanel does not currently support classes
					});
				}	
            }
        //}
	
		return legendsSpec;
	},
	
    /**
     * Method: getLayerContextByTitle
     * Helper method that returns the layer context in the global contact
     *     searched by its name.
     * 
     * Parameters: 
     * title - {String} The layer title.
     */
    getLayerContextByTitle: function(title) {
        var layers = this.context.layersContext;
        for (var i = 0, l = layers.length; i < l; i++) {
            var layer = layers[i];
            if (layer.title == title) {
                return layer;
            }
        }
    },    
    
	/**
	 * APIFunction: relativeToAbsoluteURL
	 * (See MapFish Utils.js)
	 * 
	 * Parameters: 
	 * source - {String} The source URL.
	 * 
	 * Returns: 
	 * {String} An absolute URL, null is returned if the function
	 * couldn't do the conversion because there are occurrences of "/../" in the
	 * pathname that couldn't be deal with.
	 */
	relativeToAbsoluteURL : function(source, loc) {
		loc = loc || location;

		var h, p, re;

		if (/^\w+:/.test(source) || !source) {
			return source;
		}

		h = loc.protocol + "//" + loc.host;

		// if source starts with a slash, append source to
		// h and return that
		if (source.indexOf("/") == 0) {
			return h + source;
		}

		// get pathname up to the last slash, and remove
		// that slash
		p = loc.pathname.replace(/\/[^\/]*$/, '');

		// append source to p
		p = p + "/" + source;

		// replace every occurence of "/foo/../" by "/"
		re = /\/[^\/]+\/\.\.\//;
		while (p.match(re) !== null) { 
			p = p.replace(re, '/');
		}

		// if there are occurrences of "/../" we couldn't
		// deal with, returns null
		if (p.indexOf("/../") > -1) {
			return null;
		}

		// replace every occurence of "/./" by "/"
		re = /\/\.\//;
		while (p.match(re) !== null) {
			p = p.replace(re, '/');
		}

		return  h + p;
	},
	
    /**
	 * Method: convertLayer 
	 * Handles the common parameters of all supported layer types.
	 * (See MapFish PrintProtocol.js)
	 * 
	 * Parameters: 
	 * olLayer - {<OpenLayers.Layer>} The OL layer.
	 * 
	 * Returns: 
	 * {Object} The config for this layer.
	 */
    convertLayer: function(olLayer) {
        var url = olLayer.url;
        if (url instanceof Array) {
            url = url[0];
        }
        return {
            baseURL: this.relativeToAbsoluteURL(url),
            opacity: (olLayer.opacity != null) ? olLayer.opacity : 1.0,
            singleTile: olLayer.singleTile,
            customParams: {}
        };
    },	
	
	/**
	 * Method: getIconUrl 
	 * Builds the URL for a layer icon, based on a WMS GetLegendGraphic request.
	 * 
	 * Parameters: 
	 * wmsUrl - {String} The URL of a WMS server. 
	 * options - {Object} The options to set in the request: 
	 * *layer - the name of the layer for
	 * which the icon is requested (required) 
	 * *rule - the name of a class for
	 * this layer (this is set to the layer name if not specified) 
	 * *format - "image/png" by default ...
	 * 
	 * Returns: 
	 * {String} The URL at which the icon can be found.
	 */
	getIconUrl : function(wmsUrl, options) {
		if (!options.layer) {
			OpenLayers.Console.warn(
				'Missing required layer option in mapfish.Util.getIconUrl');
			return '';
		}
		
		if (!options.rule) {
			options.rule = options.layer;
		}
		
		if (wmsUrl.indexOf("?") < 0) {
			// add a ? to the end of the url if it doesn't
			// already contain one
			wmsUrl += "?";
		} else if (wmsUrl.lastIndexOf('&') != (wmsUrl.length - 1)) {
			// if there was already a ? , assure that the parameters
			// are ended with an &, except if the ? was at the last char
			if (wmsUrl.indexOf("?") != (wmsUrl.length - 1)) {
				wmsUrl += "&";
			}
		}
		
		var options = OpenLayers.Util.extend({
			layer: "",
			rule: "",
			service: "WMS",
			version: "1.1.1",
			request: "GetLegendGraphic",
			format: "image/png",
			width: 16,
			height: 16
		}, options);
		
		options = OpenLayers.Util.upperCaseObject(options);
		
		return wmsUrl + OpenLayers.Util.getParameterString(options);
	},
	
	/**
	 * Method: getApplicationUrl
	 * Builds the application URL.
	 * 	  
	 * Returns: 
	 * {String} The URL of the application.
	 * e.g : http://localhost:8080/GeoSIE-Brique/
	 */	
	getApplicationUrl : function() {
		var url=window.location.protocol+'//'+window.location.host;
		var pathArray = window.location.pathname.split( '/' );

		// Add the pathname(s) to URL
		if(pathArray.length>0){
			var newPathname = "";
			for (var i = 0; i< pathArray.length-1; i++ ) {
				  newPathname += pathArray[i]+"/";
			};
			url+=newPathname;
		}
		
		return url;
	},
	
    CLASS_NAME : 'GeoSIE.Control.Print'
});