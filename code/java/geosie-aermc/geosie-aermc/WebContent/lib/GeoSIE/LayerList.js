/*
 * @include OpenLayers/Events.js
 */

/**
 * Class: GeoSIE.LayerList
 * The LayerList object represents a widget for an {<OpenLayers.Map>} instance. 
 * It displays the layers list and allows the user to:
 * - change the opacity of a layer
 * - move up or move down a layer
 * - show or hide a layer
 * - open a window with the metadata resources about a layer
 * 
 * How to use this widget: 
 * 
 * - insert this code into your layout (ie: viewer.html after the div with id=accordion): 
 * 
 * <div id="layerList"></div>
 * 
 * - include lib/GeoSIE/LayerList.js in viewer.html
 * - add a layerList property with a null value in Brique.js
 * - add this block of code in the Brique.js in addWidgets() method:
 * 
 * (start code)
 *       this.layerList = new GeoSIE.LayerList({
 *           div: OpenLayers.Util.getElement("layerList"),
 *           map: this.map
 *       });
 * (end) 
 *  
 * - insert a MetadataURL tag for each layer with a metadata resources in context.xml:
 * (start code)
 *	  <MetadataURL type="TC211">
 *			<Format>text/html</Format>
 *			<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://www.geocatalogue.fr/geocat/Detail.do?id=8233"/>
 *	  </MetadataURL>    
 * (end)  
 *     
 */

GeoSIE.LayerList = OpenLayers.Class({
    
    /**
     * Property: mainTemplate
     */
    mainTemplate: 
        '<div id="${id}" class="legend_container"> This is the layer list</div>',
    
    /**
     * Property: layerTemplate
     */    
    layerTemplate:
        '<div class="layer_bloc" id="layer_${id}">' +
        '    <img class="up" src="${path}images/flecheup.png" title="Monter cette couche" style="margin: 10px 5px 5px;" /> ' +
        '    <img class="down" src="${path}images/flechedown.png" title="Descendre cette couche" style="margin: 5px 5px 10px;" /> ' +
        '    <img class="show" src="${path}images/visu_${hidden}.gif" title="${hiddenTitle}" /> ' +
        '    <div class="title">' +
        '        ${name}' +
        '    </div>' +
        '    <div class="opacity" id="opacitySlider_${id}"></div>' +
        '    <div class="op_text" id="opacityCursor_${id}_text">' +
        '        ${opacity}%' +
        '    </div>' +
        '     ${info}'+
        '</div>',
        
	/**
	 * APIProperty: map
     * {<OpenLayers.Map>} The map.
	 */
    map: null,
        
    /**
     * Constructor: GeoSIE.LayerList
     * 
     * Parameters: 
	 * options - {Object} Options for control.
     * 
     * Returns:
     * {<GeoSIE.LayerList>}
     */
    initialize : function(options) {
        OpenLayers.Util.extend(this, options);
        
        if (this.id == null) {
            this.id = OpenLayers.Util.createUniqueID("LayerList_");
        }

        /*this.map.events.on({
            'changelayer': this.redraw,
            scope: this
        });*/
 
        this.div = OpenLayers.Util.getElement(this.div);
        
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
        });*/  	
    },     
    
    /**
     * Method: redraw
     * Redraw the LayerList.
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
        
        var layers = this.map.layers, rows = [];
        
        var infoHtml = '<img class="info" src="'+GeoSIE.Config.ROOT_PATH+'images/pictos_action_p_informer.png"'+
           'title="Lien vers la fiche de métadonnées de cette couche" onclick="window.open(\'';

        for (var l = layers.length - 1, i = l; i >= 0; i--) {
            var layer = layers[i];

            // don't show layer if not to be displayed here
            if (!layer.displayInLayerSwitcher) {
                continue;
            }
            
            var id = layer.id.toString().replace(/\./g, "_");
			
		   rows.push( 
		       OpenLayers.String.format(this.layerTemplate,
		           OpenLayers.Util.applyDefaults(
		               {
		                   hidden: (layer.visibility) ? 'on' : 'off',
		                   hiddenTitle: (layer.visibility) ? 'Masquer cette couche' : 'Afficher cette couche',		   
		                   id: id,
		                   opacity: layer.opacity * 100,
		                   info: (layer.metadataURL && layer.metadataURL !== "") ? infoHtml+layer.metadataURL+'\');" >' : '',
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
            
            var downArrow = ($('#layer_' + id + ' .down'))[0];
            if (downArrow) {
                OpenLayers.Event.observe(downArrow, "click", OpenLayers.Function.bind(function(layer, evt) {
                    // the bottom layer
                	if (layer.map.getLayerIndex(layer) == 1) {
                		OpenLayers.Console.log(this.CLASS_NAME + "redraw:: - layer already on bottom, dont down");
                        return;
                    }
                	this.moveLayer(layer, "down");
                	
                }, this, layer));
            }
            
            var upArrow = ($('#layer_' + id + ' .up'))[0];
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

            var showButton = ($('#layer_' + id + ' .show'))[0];
            if (showButton) {
                OpenLayers.Event.observe(showButton, "click", OpenLayers.Function.bind(function(layer, evt) {
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
            
            var info = ($('#info_' + id ))[0];
            
            $('#opacitySlider_' + id).slider({
                value: layer.opacity * 100,
                orientation: 'horizontal',
                slide: OpenLayers.Function.bind(function(layer, id, event, ui) {
                    layer.setOpacity(ui.value / 100);
                    OpenLayers.Util.getElement('opacityCursor_'+ id + '_text')
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
        
  	  	var layerDiv = $('#layer_' + id);
  	  	
  	  	var dir;
  	  	if(direction == "up"){
  	  		dir = 1;
  	  	    layerDiv.insertBefore(layerDiv.prev().prev());  
  	  	} else if (direction == "down") {
  	  		dir = -1;
  	  	    layerDiv.insertAfter(layerDiv.next().next());  
  	  	    //legendDiv.insertAfter(legendDiv.next());   	  		
  	  	}
    	layer.map.raiseLayer(layer, dir);
    },    
    
	CLASS_NAME: 'GeoSIE.LayerList'
});