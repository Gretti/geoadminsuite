/*
 * @include OpenLayers/Events.js
 */

/**
 * Class: GeoSIE.LayerListLegend
 * The LayerListLegend object represents a widget for an {<OpenLayers.Map>} instance.
 * It allows switching between displayable loaded layers of different kinds (WMS, WFS) 
 * for which the user can change opacity, layer order, and expand informations like legends.
 * This widget displays the layers list and associated legends.
 * 
 * How to use this widget : 
 * 
 * - insert a div with id=layerListLegend into your layout (ie: viewer.html jQuery accordion)
 * - include LayerListLegend.js in viewer.html
 * - include LayerListLegendStyle.css in viewer.html
 * - add a layerListLegend property with a null value after the null map property in Brique.js
 * - add this block of code in the Brique.js in addWidgets() method:
 * (start code)
 * 		// layers list + legend
 *		this.layerListLegend = new GeoSIE.LayerListLegend({
 *			div: OpenLayers.Util.getElement("layerListLegend"),
 *			map: this.map,
 *			context: this.context,
 *			noLegendMessage: 'Aucune légende',
 *			labelLegend: 'Légende',
 *          titleLegend: 'Afficher les légendes de la couche',
 *          bgcolor: '#ffffff'  
 *		});   
 * (end)
 * - insert a LegendURL tag for each layer with a legend in context.xml:
 * (start code)
 *        <LegendURL format="image/png">
 *              <OnlineResource xlink:type="simple" xlink:href="http://mapsrefrec.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/FXX_TopoIGN-NTF.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=TRONCON_COURS_EAU&amp;format=image/png"/>
 *        </LegendURL>
 * (end) 
 * - insert a MetadataURL tag for each layer with a metadata resources in context.xml:
 * (start code)
 *	  <MetadataURL type="TC211">
 *			<Format>text/html</Format>
 *			<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://www.geocatalogue.fr/geocat/Detail.do?id=8233"/>
 *	  </MetadataURL>    
 * (end)  
 */

GeoSIE.LayerListLegend = OpenLayers.Class({
    
	/**
	 * APIProperty: map
	 * {<OpenLayers.Map>} instance on which the widget should be displayed
	 */
    map: null,
    
    /**
     * APIProperty: context
     */
    context: null,
    
    /**
     * Property: labelLegend
     */
    labelLegend: 'Légende',

    /**
     * Property: labelNoLegend
     */    
    labelNoLegend: 'Aucune légende',
    
    /**
     * Property: titleLegend
     */
    titleLegend: 'Afficher les légendes de cette couche',
        
    /**
     * Property: bgcolor
     */
    bgcolor: '#ffffff',
    
    /**
     * Property: mainTemplate
     */
    mainTemplate: 
        '<div id="${id}" class="layerListLegend_container">This is the layer list</div>',
        
    /**
     * Property: layerTemplate
     */    
    layerTemplate:
        '<div class="layer_bloc" id="layerListLegendLayer_${id}">' +
        '    <img class="up" src="${path}images/flecheup.png" title="Monter cette couche" style="margin: 10px 5px 5px;" /> ' +
        '    <img class="down" src="${path}images/flechedown.png" title="Descendre cette couche" style="margin: 5px 5px 10px;" /> ' +
        '    <img class="show" src="${path}images/visu_${hidden}.gif" title="${hiddenTitle}" /> ' +
        '    <div class="title">' +
        '        ${name}' +
        '    </div>' +
        '    <div class="opacity" id="layerListLegendOpacitySlider_${id}"></div>' +
        '    <div class="op_text" id="layerListLegendOpacityCursor_${id}_text">' +
        '        ${opacity}%' +
        '    </div>' +
        '    ${info}'+        
        '</div>' +
        '<div class="LayerLegendClass">${legend}</div>',

    /**
     * Property: legendTemplate
     */    
    legendTemplate: 
    	'<div class="bandeLegende">' + 
    	'    <center>' + 
    	'        <img src="${path}images/flechedown.png" style="display: inline;margin: 0 5px;" />&nbsp;' + 
    	        '<a style="cursor:pointer;" title="${titleLegend}" onclick="javascript:GeoSIE.Brique.layerListLegend.showHide(\'layerListLegendRow_${id}\')">${labelLegend}</a>&nbsp;' +
    	        '<img src="${path}images/flechedown.png" style="display: inline;margin: 0 5px;" />' + 
    	'    </center>' + 
    	'</div>' + 
		'<div class="layerListLegendRow" id="layerListLegendRow_${id}" style="display:none;text-align:center;background-color:${bgcolor}">' + 
    	'    <div class="legendMapName" id="legendMapName_${id}" style="display:none;margin-bottom:10px;margin-top:10px;">' + 
    	'        <div class="showLayerName">${name}</div>' +
		'    </div>' +
		    '${legendSrc}' + 
		'</div>',
         
	/**
	 * Property: legendImageTemplate
	 */	
    legendImageTemplate: 
    	'<img src="${legendSrc}" border="0" />',
    	
    /**
     * Constructor: GeoSIE.LayerListLegend
     * Constructor for a LayerListLegend.
     * 
     * Parameters: 
     * options - {Object} Options for control.
     * 
     * Returns: 
     * {<GeoSIE.LayerListLegend>}
     */
    initialize : function(options) {
        OpenLayers.Util.extend(this, options);
        
        if (this.id == null) {
            this.id = OpenLayers.Util.createUniqueID("LayerListLegend_");
        }

        /*this.map.events.on({
            'changelayer': this.redraw,
            scope: this
        });*/
 
        if ( this.div !== null ){
        	this.div = OpenLayers.Util.getElement(this.div);
        }
        
        this.div.innerHTML = OpenLayers.String.format(this.mainTemplate, {
            id: this.id
        });
    },
    
    /**
     * Destructor: destroy
     * Unregister any registration this object would have had on some event types.
     * To use before destruction of this object.
     */
    destroy: function(){
        /*this.map.events.un({
            'changelayer': this.redraw,
            scope: this
        });	*/
    },   
    
    /**
     * Method: redraw
     * Draws or redraws the LayerListLegend switcher widget accordingly from the context.
     * 
     * Parameters:
     * evt - {Event}
     */
    redraw: function(evt) {
        if (evt && evt.property == 'opacity' || (evt && evt.property == 'visibility')
        		|| (evt && evt.property == 'order')) {
            return;
        }
        
    	OpenLayers.Console.log(this.CLASS_NAME + "::redraw");
        
        $('#' + this.id).empty();
        
        var layers	= this.map.layers, rows = [];
        
        var infoHtml = '<img class="info" src="'+GeoSIE.Config.ROOT_PATH+'images/pictos_action_p_informer.png"'+
        'title="Lien vers la fiche de métadonnées de cette couche" onclick="window.open(\'';
                
        for (var l = layers.length - 1, i = l; i >= 0; i--) {
            var layer = layers[i];

            // don't show layer if not to be displayed here
            if ( !layer.displayInLayerSwitcher ) {
                continue;
            }
            
            var id		= layer.id.toString().replace(/\./g, "_");
            var context	= this.getLayerContextByTitle(layer.name);
            
            var legendSrc, urlSrc;
            var showHideLegendHtml = "";

            if ( context && 
            	 context.styles && 
            	 context.styles[0] &&
                 context.styles[0].legend && 
                 context.styles[0].legend.href /* wmc:StyleList/wmc:Style[@current='1']/wmc:LegendURL */ ) {
            	
            	legendSrc = OpenLayers.String.format( this.legendImageTemplate, {
                	legendSrc: context.styles[0].legend.href
            	});
            	
            	showHideLegendHtml = OpenLayers.String.format( this.legendTemplate, {
                	id: id,
                	name: layer.name,
                    labelLegend: this.labelLegend,
                    titleLegend: this.titleLegend,
                    legendSrc: legendSrc,
                    bgcolor: '#ffffff',
                    path: GeoSIE.Config.ROOT_PATH
                });    
            	
            }else{
            	if ( false ){
//            	if ( layer.params.LAYERS && layer.visibility ) {
//            		
//            		urlSrc = layer.url;
//            		if ( urlSrc.match(/\?/) !== null){
//            			urlSrc += "&";
//            		}else{
//            			urlSrc += "?";
//            		}
//            		
//            		urlSrc += "LAYER=" +
//            				  layer.params.LAYERS + 
//            				  "&FORMAT=" + 
//            				  layer.params.FORMAT + 
//            				  "&SERVICE=" + 
//            				  layer.params.SERVICE + 
//            				  "&VERSION=" + 
//            				  layer.params.VERSION + 
//            				  "&REQUEST=GetLegendGraphic&contenttype=image/gif";
//            		
//            		legendSrc = OpenLayers.String.format( this.legendImageTemplate, {
//            			legendSrc: urlSrc
//            		});           			
            		
            	}else{
            		legendSrc = '<span class="noLegend">' + this.labelNoLegend + '</span>';
            	}
            	
            }
            
            rows.push( 
                OpenLayers.String.format(this.layerTemplate,
                    OpenLayers.Util.applyDefaults(
                        {
                            hidden: (layer.visibility) ? 'on' : 'off',
         		            hiddenTitle: (layer.visibility) ? 'Masquer cette couche' : 'Afficher cette couche',		   
                            id: id,
                            opacity: layer.opacity * 100,
 		                    info: (layer.metadataURL && layer.metadataURL !== "") ? infoHtml+layer.metadataURL+'\');" >' : '',
                            legend: showHideLegendHtml,
                            path: GeoSIE.Config.ROOT_PATH 
                        },
                        layer
                    )
                )
            );
        }
        $('#' + this.id).append(rows.join(''));
        
        // add the events listeners
        for (var l = layers.length - 1, i = l; i >= 0; i--) {
            var layer = layers[i];
            
            // don't show layer if not to be displayed here
            if (!layer.displayInLayerSwitcher) {
                continue;
            }
            
            var id = layer.id.toString().replace(/\./g, "_");
            
            var downArrow = ($('#layerListLegendLayer_' + id + ' .down'))[0];
            if (downArrow) {
                OpenLayers.Event.observe(downArrow, "click", OpenLayers.Function.bind(function(layer, evt) {
                    // the bottom layer
                	if (layer.map.getLayerIndex(layer) == 1) {
                		OpenLayers.Console.log(this.CLASS_NAME + "::redraw - layer already on bottom, dont down");
                        return;
                    }
                	
                	this.moveLayer(layer, "down");
                }, this, layer));
            }
            
            var upArrow = ($('#layerListLegendLayer_' + id + ' .up'))[0];
            if (upArrow) {
                OpenLayers.Event.observe(upArrow, "click", OpenLayers.Function.bind(function(layer, evt) {
                   	// find the highest index for wms layer
                	var wmsLayers = layer.map.getLayersByClass("OpenLayers.Layer.WMS");
                	var maxIndex = 0;
                	for(var i=0;i<wmsLayers.length;i++){
                		var wmsLayer = wmsLayers[i];
                		var index = layer.map.getLayerIndex(wmsLayer);
                		if(index>maxIndex){
                			maxIndex = index;
                		}
                	}
                	var currentIndex=layer.map.getLayerIndex(layer);
                	// don't up the layer, already the highest
                	if(currentIndex >= maxIndex){
                		OpenLayers.Console.log(this.CLASS_NAME + "::redraw - layer already on top, dont up");
                		return;
                	}
                	
                	this.moveLayer(layer, "up");
                }, this, layer));
            }
            
            var showButton = ($('#layerListLegendLayer_' + id + ' .show'))[0];
            if (showButton) {
                OpenLayers.Event.observe(showButton, "click", OpenLayers.Function.bind(function(layer, evt) {
//                	if ( !layer.getVisibility()){
//                		GeoSIE.Brique.layerListLegend.redraw();
//                	}
                	var showButtonElement = OpenLayers.Event.element(evt);
                    layer.setVisibility(!layer.getVisibility());
                    // Toogle the img
                    var showButtonSrc = showButtonElement.src;
                    if(!layer.getVisibility()){
                    	showButtonSrc = showButtonElement.src.replace("visu_on.gif","visu_off.gif");
                    } else {
                    	showButtonSrc = showButtonElement.src.replace("visu_off.gif","visu_on.gif");
                    }
                	showButtonElement.src = showButtonSrc;                    
                }, this, layer));
            }
                       
            $('#layerListLegendOpacitySlider_' + id).slider({
                value: layer.opacity * 100,
                orientation: 'horizontal',
                slide: OpenLayers.Function.bind(function(layer, id, event, ui) {
                    layer.setOpacity(ui.value / 100);
                    OpenLayers.Util.getElement('layerListLegendOpacityCursor_'+ id + '_text')
                        .innerHTML = ui.value +'%';
                }, this, layer, id)
             });

        }
    },
    
    /**
     * Method: moveLayer
     * Move the layer up or down.
     * 
     * Parameters:
     * layer - {<OpenLayers.Layer.WMS>} The layer to move
     * direction - {String} up or down    
     */
    moveLayer: function(layer, direction){
        var id = layer.id.toString().replace(/\./g, "_");
        
  	  	var layerDiv = $('#layerListLegendLayer_' + id);
  	  	var legendDiv = layerDiv.next();
  	  	
  	  	var dir;
  	  	if(direction == "up"){
  	  		dir = 1;
  	  	    layerDiv.insertBefore(layerDiv.prev().prev());  
  	  	    legendDiv.insertBefore(legendDiv.prev().prev());   	  		
  	  	} else if (direction == "down") {
  	  		dir = -1;
  	  	    legendDiv.insertAfter(legendDiv.next().next());
  	  	    layerDiv.insertAfter(layerDiv.next().next());  
  	  	    //legendDiv.insertAfter(legendDiv.next());   	  		
  	  	}
    	layer.map.raiseLayer(layer, dir);
    },
    
    /**
     * Method: getLayerContextByTitle
     * Helper method that returns the layer context in the global context searched by its name
     * 
     * Parameters:
     * title - {String} The layer title
     * 
     * Returns:
     * {Object} The layer searched
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
	 * Method: showHide
	 * Helper method toggling a DOM element display style state (given its id)
	 * That method is embedded here to avoid bloating the global namespace with casual javascript
	 * 
	 * Parameters:
	 * id - {String} The element id
	 */
	showHide: function(id){
		var e = document.getElementById(id);
		if (e){
            if(e.style.display == 'block'){
                e.style.display = 'none';
            }else{
                e.style.display = 'block';
            }
		}
	},
	
	CLASS_NAME: 'GeoSIE.LayerListLegend'
});