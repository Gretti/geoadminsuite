/*
 * @include OpenLayers/Events.js
 * @include lib/GeoSIE/Util.js
 */

/**
 * Class: GeoSIE.FeatureList
 * The FeatureList object represents a widget that displays the list of features attributes of the given layer(s) 
 * (layer queryable in context.xml) selected with the button "Sélectionner un ensemble de point"
 * in the edition toolbar.
 *
 * How to use this widget: 
 * 
 * - include lib/GeoSIE/FeatureList.js in viewer.html
 * - set a layer has queryable in context.xml: 
 * (start code)
 * 		<Layer queryable="1" hidden="1">
 * (end)
 * - insert a div with id=featureList into your layout (ie: viewer.html jQuery accordion)
 * - add a featureList property with a null value in GeoSIE.Brique.js
 * - add this block of code in the Brique.js in addWidgets() method:
 * (start code)
 *       // feature list
 *       this.featureList = new GeoSIE.FeatureList({
 *       	 id: "control_feature_list",
 *           div: OpenLayers.Util.getElement("featureList"),
 *           layer: this.vectorLayer
 *       });
 * (end)
 * - add a template in GeoSIE.FeatureList for each queryable layer (see redraw method for
 * the implementation). A template display the feature attributs of a layer.
 */

GeoSIE.FeatureList = OpenLayers.Class({
    
	/**
     * Constant: EVENT_TYPES
     * {Array({String}) The event types supported by this widget.
     * 
     * Supported control event types:
     * overfeature - Triggered when the mouse is over a feature. Listeners receive a feature object.
     * outfeature - Triggered when the mouse is out a feature. Listeners receive a feature object.
     */
    EVENT_TYPES: ['overfeature', 'outfeature'],
    
    /**
     * Property: events
     * {OpenLayers.Events} The object to use to register event.
     * listeners.
     */
    events: null,
    
    /**
     * APIProperty: id
     * {String} The id.
     */    
    id: null,
    
    /**
     * Property: template
     * {String} The template with the buttons.
     */
    template: 
        '<div>' +
        '    <div id="${widgetId}_researchInput" class="researchInput" style="z-index:100;"></div>' +
        '</div>' +
        '<div class="researchButton">' +
        '    <div class="rowButton">' +
        '       <div id="${widgetId}_selectAll" class="button left">' +
        '            <center>' +
        '                <div class="innerContainer">' +
        '                    <img value="Tout sélectionner" src="${path}images/pictos_action_p_tout_selectionner.png"/>' + 
        '                    <span>Tout sélectionner</span>' +
        '                </div>' +
        '            </center>' +
        '        </div>' +
        '        <div id="${widgetId}_deselectAll" class="button right">' +
        '            <center>' +
        '                <div class="innerContainer">' +
        '                    <img value="Tout désélectionner" src="${path}images/pictos_action_p_tout_deselectionner.png"/>' +
        '                    <span>Tout désélectionner</span>' +
        '                </div>' +
        '            </center>' +
        '        </div>' +
        '    </div>' +
        '    <div  class="rowButton">' +
        '        <div id="delete" class="button left" onclick="GeoSIE.Brique.suppression()" >' +
        '            <center>' +
        '                <div class="innerContainer">' +
        '                    <img value="Supprimer" src="${path}images/pictos_action_p_supprimer.png"/>' +
        '                    <span>Supprimer</span>' +
        '                </div>' +
        '            </center>' +
        '        </div>' +
	    '        <div id="print" class="button right" onclick="GeoSIE.Brique.impression()" >' +
	    '              <center>' +
	    '                  <div class="innerContainer">' +
	    '                      <img value="Imprimer" src="${path}images/pictos_action_p_lier.png"/>' +
	    '                      <span>Imprimer</span>' +
	    '                  </div>' +
	    '              </center>' +
	    '       </div>' +
        '    </div>' +
        '</div>',

     /**
      * Property: templatePopup
      */   
     templatePopup:
         '<div>' +
         '    <div id="${widgetId}_researchInput" class="researchInpupPopup" style="z-index:100;"></div>' +
         '</div>',
         
    /**
     * Constructor: GeoSIE.FeatureList
     * 
     * Parameters: 
     * options - {Object} Options for this widget.
     * 
     * Returns: 
     * {<GeoSIE.FeatureList>}
     */
    initialize : function(options) {
        OpenLayers.Util.extend(this, options);

        this.layer.events.on({
            'featuresadded': this.redraw,
            //'featuresremoved': this.redraw,
            scope: this
        });
        
        // redraw on Zone Change
        GeoSIE.ZoneManager.events.on({
            'zonechanged': this.redraw,
            scope: this
        });

        this.events = new OpenLayers.Events(this, null, this.EVENT_TYPES);
        
        this.div = OpenLayers.Util.getElement(this.div);
        
        var template;
        if(GeoSIE.Config.RESULT_GF_DIALOG == true){
        	template = this.templatePopup;
            this.createPopup();        	
        } else {
        	template = this.template;
        }
        
        this.div.innerHTML = OpenLayers.String.format(template, {
            widgetId: this.id,
			path : GeoSIE.Config.ROOT_PATH
        });
        
        if(GeoSIE.Config.RESULT_GF_DIALOG == false){
            this.initEvents();         	
        }
    },

    /**
	 * Method: createPopup
	 * Create the featureList popup.
	 */
    createPopup: function () {
    	var id = this.div.id;
    	
		if($("#" + id)) {
		    $("#" + id).dialog({
				title: 'Résultat',
		  		bgiframe: true,
				modal: false,
				autoOpen: false,
				zIndex: 20000,
				minHeight : 40,// TODO : gerer hauteur
				//height: 100
				width:330
			});
		}	 
    },	    
    
    /**
     * Destructor: destroy
     */
    destroy: function(){
        this.layer.events.un({
            'featuresadded': this.redraw,
            //'featuresremoved': this.redraw,
            scope: this
        });	
        
        GeoSIE.ZoneManager.events.un({
            'zonechanged': this.redraw,
            scope: this
        });
    },     
    
    /**
     * Method: initEvents
     */
    initEvents: function() {
        var selectAllBtn = OpenLayers.Util.getElement(this.id + "_selectAll");
        OpenLayers.Event.observe(selectAllBtn, "click", OpenLayers.Function.bind(this.selectAll, this)); 
        
        var deselectAllBtn = OpenLayers.Util.getElement(this.id + "_deselectAll");
        OpenLayers.Event.observe(deselectAllBtn, "click", OpenLayers.Function.bind(this.deSelectAll, this)); 
    },
   
    /**
     * Method: redraw  
     * Redraw the featureList
     * 
     * Parameters:
     * evt - {Event}
     */
    redraw: function(evt) {
        if (evt == null) {
            return;
        }
        
        OpenLayers.Console.log(this.CLASS_NAME + "::redraw - featureList");
        
        var features = evt.features || [];
        
        $('#' + this.id + "_researchInput").empty();
        
        var feature, context, rows = [];
        for (var i = 0, l = features.length; i < l; i++) {
            feature = features[i];
            
            var layer = GeoSIE.Util.getFeatureTable(feature);
            
            // Set the template for this feature
            var configTemplate = this.getConfigAttribut(layer);
            if (configTemplate != null && feature.attributes){
                // Id d'affichage
            	var attributes = feature.attributes;
               
            	// Replace null value with "" else display null in "Résultat" 
            	for (key in attributes){
                	if(attributes[key]==null){
                		attributes[key] = ""; //TODO : mettre value non renseignée ?
                	}
                }
            	
                context = OpenLayers.Util.extend({
                    widgetId: this.id,
                    fid: feature.fid,
                    idw: feature.id,
    				path: GeoSIE.Config.ROOT_PATH
                }, attributes);
                
                var html = OpenLayers.String.format(configTemplate.template, context);
                rows.push(html);
            }
        }
        if(features.length == 0){
        	rows.push(GeoSIE.Messages.noResultWFS);
        }
        $('#' + this.id + "_researchInput").append(rows.join(''));

    	OpenLayers.Console.log(this.CLASS_NAME + "::redraw - nb features list event: "+features.length);
		// Add events
        for (var i = 0, l = features.length; i < l; i++) {
        	feature = features[i];
            var rowElement = document.getElementById(this.id + '_' + feature.id);
            console.log('looking for el: ' + this.id + '_' + feature.id)
            OpenLayers.Event.observe(rowElement, "mouseover", OpenLayers.Function.bind(this.onFeatureOver, this, feature));
            OpenLayers.Event.observe(rowElement, "mouseout", OpenLayers.Function.bind(this.onFeatureOut, this, feature));
            
        	// click on title
            var title = document.getElementById("title_" + this.id + '_' + feature.id);
            OpenLayers.Event.observe(title, "click", OpenLayers.Function.bind(function(feature, evt) {
            	GeoSIE.Brique.consult(feature);
            }, this, feature));
            
        	// click on the checkbox
            var checkbox = document.getElementById("chk_" + this.id + '_' + feature.id);
            OpenLayers.Event.observe(checkbox, "click", OpenLayers.Function.bind(function(feature, evt) {
            	var targ = evt.target || evt.srcElement;
                this.select(feature.id, targ.checked);
            }, this, feature));
        }
        
        if(GeoSIE.Config.RESULT_GF_DIALOG == true){
        	if(evt.features){
            	if($("#" + this.div.id)) {
            		$("#" + this.div.id).dialog("open");
            	}        		
        	}
        }
    },
    
    /**
     * Method: select
     *
     * Parameters:
     * id - {Integer} The id of the feature to (un)select.
     * select - {Boolean} Tells if the feature should be selected or unselected.
     */
    select: function(id, select) {
        if (this.layer) {
            var feature = this.layer.getFeatureById(id);
            feature.attributes["sie_selected"] = select; // TODO : sie_selected
        }
    },

    /**
     * Method: selectAll
     * Force all checkboxes selection.
     */
    selectAll: function(){
        if (this.layer) {
            var features= this.layer.features;
            for(var i=0;i<features.length;i++){
                var id = features[i].id;
                var input = document.getElementById("chk_" + this.id + "_" +id);
                if (input != null) {
                    input.checked = true;
                    this.select(id, true);
                }
            }
        }
    },

    /**
     * Method: deselectAll
     * Force all checkboxes de-selection.
     */
    deSelectAll: function(){
        if (this.layer) {
            var features= this.layer.features;
            for(var i=0;i<features.length;i++){
                var id = features[i].id;
                var input = document.getElementById("chk_" + this.id + "_" +id);
                if (input != null) {
                    input.checked = false;
                    this.select(id, false);
                }
            }
        }
    },
    
    /**
     * Method: onFeatureOver
     * Called when the mouse hovers a row in the list.
     * 
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The feature over.
     */
    onFeatureOver: function(feature) {
        this.events.triggerEvent('overfeature', feature);
        this.highlightRow({feature: feature});
    },

    /**
     * Method: onFeatureOut
     * Called when the mouse goes out of a row in the list.
     * 
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The feature out.
     */
    onFeatureOut: function(feature) {
        this.events.triggerEvent('outfeature', feature);
        this.dehighlightRow({feature: feature});
    },
    
    /**
     * Method: highlightRow
     * Widget function to highlight the list item and call the listener that
     * will highlight the feature in the map.
     * 
     * Parameters:
     * obj -
     */
    highlightRow: function(obj) {
        var element = OpenLayers.Util.getElement(this.id + '_' + obj.feature.id);
        if(element) {
        	element.className="listitem_active";
        }
    },
  
    /**
     * Method: dehighlightRow
     * Widget function to dehighlight the list item and call the listener that
     * will hdeighlight the feature in the map.
     * 
     * Parameters:
     * obj -
     */
    dehighlightRow: function(obj) {
        var element = OpenLayers.Util.getElement(this.id + '_' + obj.feature.id);
        if(element) {
	        element.className="listitem";
	    }
    },
    
    /**
     * Method: getConfigAttribut
     * Get the configuration to display WFS attributs.
     * 
     * Parameters:
     * fid - {String} fid table name.
     * 
     * Returns:
     * {Object} The config to display the feature attribut
     */
    getConfigAttribut: function (fid){
    	var configs = GeoSIE.Config.GF_ATTRIBUTS;
    	
    	for(var i=0; i<configs.length; i++){
    		var fids = configs[i].fids;
    		for(var ii=0; ii<fids.length; ii++){
        		if(fids[ii] == fid){
        			return configs[i];
        		}
    		}
    	}
    	
    	return null;
    },
    
    CLASS_NAME : 'GeoSIE.FeatureList'
});