/* 
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Handler/Point.js 
 * @include lib/GeoSIE/Control/SnappingSIE.js
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Geometry/Point.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 * @include OpenLayers/Projection.js
 */

/**
 * Class: GeoSIE.Control.Snapping
 * Acts as a snapping agent while editing vector features.
 * The feature added can be snapped or not. The feature snapped can be on a node or not.
 * Can return the attributs of the feature snapped if the property isSnapped is set to true.
 * 
 * To use this control, you must insert a ol:snappable tag for each layer you want to snap in context.xml:
 * (start code)
 *       <ol:snappable xmlns:ol="http://openlayers.org/context">true</ol:snappable>
 * (end)
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.Snapping = OpenLayers.Class(OpenLayers.Control, {
    
	/**
	 * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this control.
     * 
     * Supported control event types (in addition to those from <OpenLayers.Control>):
     * snapping - Triggered before a feature is added to the layer
	 */		
	EVENT_TYPES: ['snapping'],
	
    /**
     * Property: type
     * {OpenLayers.Control.TYPES}
     */	
    type: OpenLayers.Control.TYPE_TOOL,

    /**
	 * APIPropertyProperty: layer
	 * {<OpenLayers.Layer.Vector>} The vector layer used by
	 * this control to draw features.
	 */
    layer: null,
    
    /**
	 * Property: snappingControl
	 * {<GeoSIE.Control.SnappingSIE>} The custom snapping control.   OpenLayers.Control.Snapping
	 */ 
    snappingControl: null,

    /**
	 * Property: drawFeatureControl
	 * {<OpenLayers.Control.DrawFeature>} The drawFeature control.
	 */     
    drawFeatureControl: null,
    
    /**
	 * APIProperty: targets 
	 * {Array(<OpenLayers.Layer.Vector>)} The vector layers "snapped" (BD Topo for exemple).
	 */    
    targets: [],
    
    /**
	 * Property: queryableLayersName
	 * {Array (String)} The list of layersName to query.
	 */
    queryableLayersName: [],
    
    /**
	 * APIProperty: isSnapped 
	 * {Boolean} True the new point must be snapped on a target layer.
	 */    
    isSnapped: false,

    /**
	 * APIProperty: edge 
	 * {Boolean} True the snap only on node.
	 */    
    edge: false,
    
    /**
	 * Property: snapped 
	 * {Boolean} True if the point is snapped on a targetlayer.
	 */  
    snapped: false,

    /**
	 * Property: feature 
	 * {<OpenLayers.Feature.Vector>} The feature snapped.
	 */  
    feature: null,
    
    /**
	 * APIProperty: maxFeatures 
	 * {Integer} Maximum number of features to return
	 * from a query (WFS GetFeature). 
	 * See http://trac.osgeo.org/openlayers/wiki/FrequentlyAskedQuestions What is
	 * the maximum number of Coordinates / Features I can draw with a Vector
	 * layer? Default is 800.
	 */
    maxFeatures: 800,

    /**
	 * Property: wgs84 
	 * {OpenLayers.Projection} An EPSG:4326 projection instance.
	 */
    wgs84: null,    
    
    /**
	 * Constructor: GeoSIE.Control.Snapping
	 * 
	 * Parameters:
	 * options - {Object} An object containing all configuration properties for the control.
	 * 
     * Returns: 
     * {<GeoSIE.Control.Snapping>}  
	 */
    initialize: function(options){
	    this.EVENT_TYPES =
	    	GeoSIE.Control.Snapping.prototype.EVENT_TYPES.concat(
	        OpenLayers.Control.prototype.EVENT_TYPES
	    );
        OpenLayers.Util.extend(this, options);
		
        // options.handlerOptions={style:myStyle};
		this.drawFeatureControl = new OpenLayers.Control.DrawFeature(this.layer,OpenLayers.Handler.Point,options);
		this.drawFeatureControl.events.on({'featureadded': this.confirmSnap, scope: this});

		// this.map.addControl(this.drawFeatureControl); GeoSIE.Control.SnappingSIE OpenLayers.Control.Snapping
        this.snappingControl = new GeoSIE.Control.SnappingSIE({
            layer: this.layer,     
            targets: this.targets,
            greedy: false
        });

        // add event to the snapping control
        if(this.isSnapped){
	       	this.snappingControl.events.on({
					unsnap: this.unSnapEv,
					snap: this.snapEv,
					scope: this
				}); 	
        }    
        
        if(this.edge){
            // To snap the node only
            for (var i=0;i<this.snappingControl.targets.length;i++){
            	var target = this.snappingControl.targets[i];
            	target["edge"] = false;
            }
        }
        
        // TODO : utiliser le WFS 1.1.0 et le parametre srsName qui permet retroprojeter dans le systeme demande
        this.wgs84 = new OpenLayers.Projection("EPSG:4326");

        OpenLayers.Control.prototype.initialize.apply(this, [options]);
    },
    
    /**
	 * APIMethod: destroy 
	 * Take care of things that are not handled in superclass.
	 */
    destroy: function() {
        // this.layer = null;
 	   this.drawFeatureControl.events.un({'featureadded': this.confirmSnap, scope: this});            

        if(this.isSnapped){
       	 this.snappingControl.events.un({
				unsnap: this.unSnapEv,
				snap: this.snapEv,
				scope: this
			}); 	
       } 
    	
    	this.drawFeatureControl.destroy();
        this.snappingControl.destroy();
        OpenLayers.Control.prototype.destroy.apply(this, []);
    },    

    /**
	 * APIMethod: activate 
	 * Activate the control and the snappingControl.
	 * 
	 * Returns: 
	 * {Boolean} Successfully activated the control and feature.
	 * handler.
	 */
    activate: function() {
       if(!this.layer){
    	   return
       }
       var ret = OpenLayers.Control.prototype.activate.apply(this, arguments);

       /*if(ret) {

       } */  
       
	   // get the snappable layers
	   this.queryableLayersName = [];
       if(this.map.layers && this.map.layers.length > 0) {
        	for(var i = 0 ; i < this.map.layers.length ; i++) {
        		var layer = this.map.layers[i];
        		if(layer.snappable) {
        			this.queryableLayersName.push(layer.name);
        		}
        	}
       }
       if(this.queryableLayersName.length == 0){
    	   this.opaque(false);
    	   this.deactivate();
    	   alert(GeoSIE.Messages.noSnappableLayerMessage);
    	   return ret;
       }
       
       // TODO : pas bien ce this.active, car le checkactivable de carto,
		// decalage du active
       if(this.active){
           this.drawFeatureControl.activate();
           this.snappingControl.activate();
    	   this.opaque(true); 	
    	   
	       OpenLayers.Console.log(this.CLASS_NAME + "::activate - nb couche snappable : "+this.queryableLayersName.length);
           // add the features
           this.i_selectBox(this.map.getExtent(),this.queryableLayersName.slice(0),[]);
       }
       
       return ret;
    },    
    
    /**
	 * APIMethod: deactivate 
	 * Deactivate the control and all handlers.
	 * 
	 * Returns: 
	 * {Boolean} Successfully deactivated the control.
	 */
    deactivate: function() {
        var ret = OpenLayers.Control.prototype.deactivate.apply(this, arguments);
        if(ret) {
           this.drawFeatureControl.deactivate();
           this.snappingControl.deactivate();  
            
           // TODO : voir ce qu on fait, si on se deplace en utilisant 1 outil de navigation, pas pratique
           /*
			 * // Clear the vectorLayer this.layer.destroyFeatures();
			 */        	
        }	

        return ret;
    },    

    /**
	 * Method: setMap 
	 * Set the map property for the control and all handlers.
	 * 
	 * Parameters: 
	 * map - {<OpenLayers.Map>} The control's map.
	 */
    setMap: function(map) {
        this.snappingControl.setMap(map);
        this.drawFeatureControl.setMap(map);
        OpenLayers.Control.prototype.setMap.apply(this, arguments);
    },
    
    /**
	 * Method: i_selectBox 
	 * Call to make WFS GetFeature request to the query
	 * layers with the specified bounds and add the the features to the map when
	 * the snapping control is activated. (same method cf WfsGetFeature)
	 * 
	 * Parameters:
	 * bounds - {<OpenLayers.Bounds>} The maps bounds to query
	 * (BBOX params).
	 * layerListName - {Array (String)} The layer names to query.
	 * featureList - {Array(<OpenLayers.Feature.Vector>)} The features to add.
	 */
    i_selectBox: function (bounds,layerListName, featureList) {
    	// Clause d'arrêt : s'il n'y a plus de couches à interroger, on dessine
		// les features récupérés jusque là
		if(!layerListName || layerListName.length <= 0) {
			this.layer.removeFeatures(this.layer.features);
            this.layer.addFeatures(featureList,{silent:true});
            this.opaque(false);
			return;
		}
		
        var layerQuery = this.map.getLayersByName(layerListName.pop())[0];
        // the layer is not find
        if(!layerQuery){
            this.opaque(false);
            this.deactivate();
            // TODO : gerer si une couche dans la liste est quand meme presente
            alert(GeoSIE.Messages.noQuerableLayerMessage); 
        	return;
        }
		OpenLayers.Console.log(layerQuery.map.getProjectionObject());
        //var boundsConvert = bounds.clone().transform(layerQuery.map.getProjectionObject(),this.wgs84);
		
        var params = {
            SERVICE: "WFS",
            VERSION: "1.0.0",
            REQUEST: "GetFeature",
            TYPENAME: layerQuery.params["LAYERS"],
            MAXFEATURES: this.maxFeatures,
            SRS: layerQuery.projection && layerQuery.projection.getCode() ||
				 layerQuery.map && layerQuery.map.getProjectionObject().getCode(),
            BBOX: bounds.toBBOX(),
            NOCACHE: new Date().getTime()
        };
                
        OpenLayers.Request.GET({
            url: layerQuery.url,
            params: params,
            scope: this,
            success: 
				function(request) {
		            var format = new OpenLayers.Format.GML({
		            	// to convert the geometry projection, WFS give 4326 we
						// need to convert in the map projection
	                    /*internalProjection: this.map.baseLayer.projection,
	                    externalProjection: this.wgs84*/
					});
		            var features=format.read(request.responseXML);
		            OpenLayers.Console.log(this.CLASS_NAME + "::i_selectBox - nb features snapping : "+features.length);
		            featureList = featureList.concat(features);
		            this.i_selectBox(bounds,layerListName, featureList);
		        }
            ,
            failure: function() {
            	// TODO : gerer le cas ou on a 2 couches et y en a 1 qui plante
            	this.opaque(false);
                OpenLayers.Console.error("failure");
            }
        });
    },    
    
    /**
	 * Method: confirmSnap
	 * If the point must be snapped, delete the feature if
	 * not snapped. Fire the event "snapping" when the point is added to the
	 * layer.
	 * 
	 * Parameters: 
	 * event - {Object}
	 */
    confirmSnap: function (event){
    	var feature=event.feature; // .clone()
    	
    	// add the feature snapped attributs to the point
    	if(this.feature !=null){
    		feature.attributes = this.feature.attributes;
    	}	
    	
    	if(this.isSnapped){
        	if(!this.snapped){
        		this.layer.destroyFeatures([feature]);
        	} else {
        		this.events.triggerEvent("snapping",{feature : event});
        	}
    	} else {
    		this.events.triggerEvent("snapping",{feature : event});
    	}
    },
    
    /**
	 * Method: unSnapEv
	 * Set the snapped to false and the feature to null.
	 */
    unSnapEv: function (evt){
    	this.feature = null;
   	   	this.snapped = false;
    },

    /**
	 * Method: snapEv
	 * Set the snapped to true and the feature.
	 */    
    snapEv: function (evt){
    	this.feature = evt.feature;
    	this.snapped = true;
    },
    
    /**
	 * Method: opaque
	 * Grey out the web page.
	 * 
	 * Parameters:
	 * isOpaque - {Boolean} true to grey out the page.
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
    
    CLASS_NAME: 'GeoSIE.Control.Snapping'
});