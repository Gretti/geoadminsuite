
/**
 * Class: GeoSIE.Legend
 * The Legend object represents a widget for an {<OpenLayers.Map>} instance.
 * This widget displays legends associated with the layers shown in the layers list.
 * 
 * How to use this widget: 
 * 
 * - insert a div with id=legend into your layout (ie: viewer.html jQuery accordion)
 * - include lib/GeoSIE/Legend.js in viewer.html
 * - include style/legendStyle.css in viewer.html
 * - add a legend property with a null value in Brique.js
 * - add this block of code in the Brique.js in addWidgets() method:
 * (start code)
 *        // legend
 *        this.legend = new GeoSIE.Legend({
 *            div: OpenLayers.Util.getElement("legend"),
 *            map: this.map,
 *            context: this.context,
 *            noLegendMessage: 'Aucune légende'
 *        });    
 * (end)
 * - insert a LegendURL tag for each layer with a legend in context.xml:
 * (start code)
 *        <LegendURL format="image/png">
 *              <OnlineResource xlink:type="simple" xlink:href="http://mapsrefrec.brgm.fr/WMS/mapserv?map=/carto/RefCom/mapFiles/FXX_TopoIGN-NTF.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=TRONCON_COURS_EAU&amp;format=image/png"/>
 *        </LegendURL>
 * (end) 
 */

GeoSIE.Legend = OpenLayers.Class({
	
    /**
     * Property: mainTemplate
     */        
    mainTemplate: 
        '<div id="${id}"> This is the legend</div>',

    /**
     * Property: layerTemplate
     */         
    layerTemplate:
        '<div class="legendMapName" id="legendMapName_${id}" style="margin-bottom:10px;margin-top:10px;">' +
        '   <div class="showLayerName">${name}' +
        '   </div>' +
        '</div>' +
        '<div class="legendRow" id="legendRow_${id}">' +
        '</div>',

    /**
     * Property: legendImageTemplate
     */           
    legendImageTemplate:
        '<img src="${legendSrc}" border="0" />',

    /**
     * Property: labelNoLegend
     * {String}
     */    
    labelNoLegend: 'Aucune légende',        
        
	/**
	 * Property: map
	 * {<OpenLayers.Map>}
	 */
    map: null,

    /**
     * Property: context
     * The WMC context as read by OpenLayers.
     */
    context: null,
    
    /**
     * Constructor: GeoSIE.Legend
     * 
     * Parameters: 
     * options - {Object} Options for control.
     * 
     * Returns:
     * {<GeoSIE.Legend>}
     */
    initialize : function(options) {
        OpenLayers.Util.extend(this, options);
        
        if (this.id == null) {
            this.id = OpenLayers.Util.createUniqueID("Legend_");
        }

        this.map.events.on({
            'changelayer': this.redraw,
            scope: this
        });
        
        this.div = OpenLayers.Util.getElement(this.div);
        
        this.div.innerHTML = OpenLayers.String.format(this.mainTemplate, {
            id: this.id
        });
    },
    
    /**
     * Destructor: destroy
     */
    destroy: function(){
        this.map.events.un({
            'changelayer': this.redraw,
            scope: this
        });      
    },       
    
    /**
     * Method: redraw
     * Redraw the legend
     */
    redraw: function() {
        var elements = $('#' + this.id);
        var div = elements[0];
        elements.empty();
        
        var layers = this.map.layers;
        for (var l = layers.length - 1, i = l; i >= 0; i--) {
            var layer = layers[i];

            // don't show layer if not visible or not WMS
            if (!layer.visibility || !(layer instanceof OpenLayers.Layer.WMS)) {
                continue;
            }
            
            div.innerHTML += 
                OpenLayers.String.format(this.layerTemplate, layer);
            
            var context = this.getLayerContextByTitle(layer.name);
            
            var legendRow = OpenLayers.Util.getElement("legendRow_" + layer.id);
            // If there is a fixed legend
            if (context && context.styles && context.styles[0] &&
                context.styles[0].legend && context.styles[0].legend.href /* wmc:StyleList/wmc:Style[@current='1']/wmc:LegendURL */) {
                
                legendRow.innerHTML +=
                    OpenLayers.String.format(this.legendImageTemplate, {
                        legendSrc: context.styles[0].legend.href
                    });
                
            }
            // Otherwise, look for a legend service
            else {
                // Is there any custo legend
                if (false) {
                }
                // no legend
                else {
                    legendRow.innerHTML += this.labelNoLegend;
                }
            }
        }
    },
    
    /**
     * Method: getLayerContextByTitle
     * Helper method that returns the layer context in the global contact
     *     searched by its title
     *     
     * Parameters:
     * title - the layer title
     * 
     * Returns:
     * {Object} The layer with this title
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
    
	CLASS_NAME: 'GeoSIE.Legend'
});