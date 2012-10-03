/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Handler/Box.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 */


/**
 * Class: GeoSIE.Control.WfsGetFeature
 * The WfsGetFeature control uses WFS queries to get features selected with a BBOX.
 * 
 * Inherits from:
 * - <OpenLayers.Control>
 */
GeoSIE.Control.WfsGetFeature = OpenLayers.Class( OpenLayers.Control, {

    /**
	 * Property: type
     * {OpenLayers.Control.TYPES}
	 */
    type: OpenLayers.Control.TYPE_TOOL, 
    
    /**
     * Property: queryableLayers
     * {Array(<OpenLayers.Layer>)} The list of layers to query.
     */
    queryableLayers: [],
    
    /**
     * APIProperty: layer
     * {<OpenLayers.Layer.Vector>} The vector layer used by this control to draw features.
     */
    layer: null,
    
    /**
	 * Property: wgs84 
	 * {OpenLayers.Projection} An EPSG:4326 projection instance.
	 */
    wgs84: null,  
    
    /**
	 * APIProperty: maxFeatures 
	 * {Integer} Maximum number of features to return
	 * from a query (WFS GetFeature). 
	 * See http://trac.osgeo.org/openlayers/wiki/FrequentlyAskedQuestions What is
	 * the maximum number of Coordinates / Features I can draw with a Vector
	 * layer? Default is 80.
	 */
    maxFeatures: 80, 

    /**
	 * APIProperty: tolerance 
	 * {Integer} The distance tolerance (in pixels) at which the getfeature may occur.
     */
    tolerance: null, 
    
    /**
	 * Constructor: GeoSIE.Control.WfsGetFeature
	 * 
	 * Parameters:
	 * options - {Object} An object containing all configuration properties for the control.
	 * 
     * Returns: 
     * {<GeoSIE.Control.WfsGetFeature>}  
	 */
    initialize: function(options){
        OpenLayers.Util.extend(this, options);
    	
        this.wgs84 = new OpenLayers.Projection("EPSG:4326");

        OpenLayers.Control.prototype.initialize.apply(this, [options]);
    },
    
    /**
     * Method: draw
     * Draw the control.
     */
    draw: function() {
        this.handler = new OpenLayers.Handler.Box( this,
        {
            done: this.selectBox
        }, {
            keyMask: this.keyMask
        } );
    },

    /**
	 * APIMethod: activate 
	 * Activate the control.
	 * 
	 * Returns: 
	 * {Boolean} Successfully activated the control. 
	 */    
    activate: function() {
        if (!this.layer){
            OpenLayers.Console.warn("Il faut definir une couche vecteur pour WfsGetfeature");
            return;
        }
        
        return OpenLayers.Control.prototype.activate.apply(this, arguments);
    },

    /**
     * Method: selectIds
     * Called when you need to reselect a featureIds list in the queryable layers.
     * Comment : the featureIds pattern "<layer name>.<id>"
     * 
     * Parameters:
     * featuresId - {Array(String)} The features id.
     */
    selectIds: function (featuresId) {
        // TODO : tester
        this.setQueryLayers();
        OpenLayers.Console.log(this.CLASS_NAME + "::selectIds - featuresId " + featuresId);
        // Effectue les GetFeature sur les couches interrogeables avec les featureIds spécifiés
        this.i_selectIds(featuresId, this.queryableLayers.slice(0), []);
    },

    /**
     * Method: i_selectIds 
     * Called to make GeFeature on the queryable layers with the specified featureIds.
     * The id (feature in DB) used for all the layers must be different.
     * Comment : the featureIds pattern "<layer name>.<id>"
     * 
     * Parameters:
     * featuresId - {Array(String)} The features id.
     * layerList - {Array(<OpenLayers.Layer.WMS>)} The layers to query.
     * featureList - {Array(<OpenLayers.Feature.Vector>)} The features to add.
     */
    i_selectIds: function (featuresId,layerList,featureList) {
        // Clause d'arrêt : s'il n'y a plus de couches à interroger, on dessine les features récupérés jusque là 
        if(!layerList || layerList.length <= 0) {
            this.layer.removeFeatures(this.layer.features);
            this.layer.addFeatures(featureList);
            return;
        }
       
        var layer = layerList.pop();
        
        // Déclinaison des featureIds selon le nom de la couche courante
        var selectedFeaturesString = "";
        var selectedFeaturesArray = new Array();
        // update geometry || new geometry TODO : verifier que fonctionne bien dans tous les cas
        for (var i=0; i<featuresId.length; i++) {
            //console.log("featuresId point: "+featuresId[i].split(".")[1]);
            var fid = featuresId[i].split(".")[1]; //|| featuresId[i]
            var layerFid = featuresId[i].split(".")[0];
            var layerParams = layer.params["LAYERS"];

            // request only the features that belong to this layer when refresh
            // TODO : probleme avec les generiques, je dois chercher sur toutes les layers
            // car je ne sais pas a quelle couche appartient ce nouveau points
            if(layerParams == layerFid || layerFid == "GENERIQUE" ){
                var selectedFeatures = layerParams + "." + fid;
                selectedFeaturesArray.push(selectedFeatures);
            }
        }
        
        if(selectedFeaturesArray.length>0){
            selectedFeaturesString = selectedFeaturesArray.join(",");
        }
    	
        var params = {
            SERVICE: "WFS",
            VERSION: "1.0.0",
            REQUEST: "GetFeature",
            TYPENAME: layer.params["LAYERS"],
            MAXFEATURES: this.maxFeatures,
            SRS: this.wgs84.getCode(),
            FEATUREID: selectedFeaturesString,
            NOCACHE: new Date().getTime()
        };
        
        OpenLayers.Request.GET({
            url: layer.url,
            params: params,
            scope: this,
            success: function(request) {
                var format = new OpenLayers.Format.GML({	            	
                    // to convert the geometry projection, WFS give 4326 we
                    // need to convert in the map projection
                    internalProjection: this.map.baseLayer.projection,
                    externalProjection: this.wgs84
                    });
                featureList = featureList.concat(format.read(request.responseXML));
                this.i_selectIds(featuresId, layerList, featureList);
            },
            failure: function() {
                OpenLayers.Console.error("failure");
            }
        });
    },

    /**
     * Method: getQueryLayers
     * Get all the queryable layer defined in the context.xml
     */    
    setQueryLayers: function(){
        // get all the queryable layer defined in the context.xml
        this.queryableLayers = [];
        if(this.map.layers && this.map.layers.length > 0) {
            for(var i = 0 ; i < this.map.layers.length ; i++) {
                var layer = this.map.layers[i];
                if(layer.options["queryable"] && layer.visibility == true) {
                    this.queryableLayers.push(layer);
                }
            }
        }
    },
    
    /**
     * Method: selectBox
     * Called when the user draw a box on the map.
     * 
     * Parameters:
     * position - {<OpenLayers.Bounds>} for a BBOX or {<OpenLayers.Pixel>} for a mouse click.
     */
    selectBox: function (position) {                
        var bounds, minXY, maxXY;
        if (position instanceof OpenLayers.Bounds) {
            // c'est une BBOX
            minXY = this.map.getLonLatFromPixel(
                new OpenLayers.Pixel(position.left, position.bottom));
            maxXY = this.map.getLonLatFromPixel(
                new OpenLayers.Pixel(position.right, position.top));
        } else {
            // c'est un pixel
            minXY = this.map.getLonLatFromPixel(
                new OpenLayers.Pixel(position.x-this.tolerance, position.y+this.tolerance));
            maxXY = this.map.getLonLatFromPixel(
                new OpenLayers.Pixel(position.x+this.tolerance, position.y-this.tolerance));
        }

        bounds = new OpenLayers.Bounds(minXY.lon, minXY.lat, maxXY.lon, maxXY.lat);
        
        this.setQueryLayers();

        // the layer is not find
        var layerList = this.queryableLayers.slice(0);
        
        if(layerList.length == 0){
            alert(GeoSIE.Messages.noQuerableLayerMessage); 
            return;
        } else {
            // Effectue les GetFeature sur les couches interrogeables avec la zone calculée
            this.i_selectBox(bounds, layerList, []);
        }               
    },

    /**
	 * Method: i_selectBox 
	 * Called to make WFS GetFeature request to the queryable
	 * layers with the specified bounds and add the the features to the map.
	 * 
	 * Parameters:
	 * bounds - {<OpenLayers.Bounds>} The maps bounds to query (BBOX params). 
	 * layerListName - {Array (String)} The layer names to query.
	 * featureList - {Array(<OpenLayers.Feature.Vector>)} The features to add.
	 */
    i_selectBox: function (bounds, layerList, featureList) {
        // Clause d'arrêt : s'il n'y a plus de couches à interroger, on dessine les features récupérés jusque là 
        if(!layerList || layerList.length <= 0) {
            this.layer.removeFeatures(this.layer.features);
            this.layer.addFeatures(featureList);
            return;
        }
		
        var layer = layerList.pop();
        
        // request in 4326
        var boundsConvert = bounds.clone().transform(layer.map.getProjectionObject(),this.wgs84);
        var srs = layer.projection && layer.projection.getCode() ||
        layer.map && layer.map.getProjectionObject().getCode();//this.wgs84.getCode(),
        
        var params = {
            SERVICE: "WFS",
            VERSION: "1.0.0",
            REQUEST: "GetFeature",
            TYPENAME: layer.params["LAYERS"],
            MAXFEATURES: this.maxFeatures,
            //SRS: this.wgs84.getCode(),//boundsConvert.toBBOX(),
            //BBOX: boundsConvert.toBBOX(),
            SRSNAME: srs,
            BBOX: bounds.toBBOX(),
            NOCACHE: new Date().getTime()
        };
                
        OpenLayers.Request.GET({
            url: layer.url,
            params: params,
            scope: this,
            success: 
            function(request) {
                var format = new OpenLayers.Format.GML({		            	
                    // to convert the geometry projection, WFS give 4326 we
                    // need to convert in the map projection 
                    // TODO : utiliser le parametre srsname dans la requete WFS pour obtenir le resultat dans la projection voulue
                    internalProjection: this.map.baseLayer.projection,
                    externalProjection: this.map.baseLayer.projection
                    //externalProjection: this.wgs84
                    });
                                
                var features = format.read(request.responseXML);
                OpenLayers.Console.log(this.CLASS_NAME + "::i_selectBox - nb features wfs getfeature : "+features.length);
                OpenLayers.Console.log(featureList);
                featureList = featureList.concat(features);
                this.i_selectBox(bounds, layerList, featureList);
            }
            ,
            failure: function() {
                OpenLayers.Console.error("failure");
            }
        });
    },
    
    CLASS_NAME: 'GeoSIE.Control.WfsGetFeature'
});