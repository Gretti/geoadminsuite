/*
 * @include lib/GeoSIE/Control/Copyrights.js
 * @include lib/GeoSIE/Control/DragFeature.js
 * @include lib/GeoSIE/Control/EditLine.js
 * @include lib/GeoSIE/Control/EditPoint.js
 * @include lib/GeoSIE/Control/EditPolygon.js
 * @include lib/GeoSIE/Control/LoadingPanel.js
 * @include lib/GeoSIE/Control/NavigationHistory.js
 * @include lib/GeoSIE/Control/Print.js
 * @include lib/GeoSIE/Control/ScaleCombo.js
 * @include lib/GeoSIE/Control/Snapping.js
 * @include lib/GeoSIE/Control/WfsGetFeature.js
 * @include lib/GeoSIE/Control/WpsDownload.js
 * @include lib/GeoSIE/Control/WpsGetFeature.js
 * @include lib/GeoSIE/Control/WpsSelector.js
 * @include lib/GeoSIE/Control/ZoneSelector.js
 * @include lib/GeoSIE/Control/ZoomBar.js
 * @include lib/GeoSIE/Control/FullScreen.js
 * @include lib/GeoSIE/Control/Link.js
 * @include lib/GeoSIE/Control/Measure.js
 * @include lib/GeoSIE/Config.js
 * @include lib/GeoSIE/CrsSelector.js
 * @include lib/GeoSIE/FeatureList.js
 * @include lib/GeoSIE/Gazetteer.js
 * @include lib/GeoSIE/Goto.js
 * @include lib/GeoSIE/LayerListLegend.js
 * @include lib/GeoSIE/Messages.js
 * @include lib/GeoSIE/ZoneManager.js
 * @include lib/GeoSIE/WpsManager.js
 * @include OpenLayers/Map.js
 * @include OpenLayers/Layer.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 * @include OpenLayers/Format/WMC/v1_1_0.js
 * @include OpenLayers/Format/SLD/v1_0_0.js
 * @include OpenLayers/Format/JSON.js
 * @include OpenLayers/Events.js
 * @include OpenLayers/Projection.js
 * @include OpenLayers/Control/PanPanel.js
 * @include OpenLayers/Control/Navigation.js
 * @include OpenLayers/Control/ScaleLine.js
 * @include OpenLayers/Control/MousePosition.js
 * @include OpenLayers/Control/ZoomBox.js
 * @include OpenLayers/Control/DragPan.js
 * @include OpenLayers/Control/ZoomToMaxExtent.js
 * @include OpenLayers/Control/Panel.js
 * @include OpenLayers/Control/OverviewMap.js
 * @include OpenLayers/Control/SelectFeature.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Layer/Image.js
 * @include OpenLayers/Layer/WMS.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 */

/**
 * Header: GeoSIE.Brique
 * Facade d'accès aux opérations cartographiques
 * 
 * About: BRGM 2010
 */

/** 
 * Namespace: GeoSIE.Brique
 * Facade d'accès aux opérations cartographiques
 */

GeoSIE.Brique = {
		
    /**
     * Constant: EVENT_TYPES
     * {Array({String})} The event types supported by this object.
     * 
     * Supported event types:
     * creationpoint - Triggered after a point is added to the vector layer.
     * creationline - Triggered after a line is added to the vector layer.
     * creationpolygon - Triggered after a polygon is added to the vector layer.
     * consult - Triggered after a user click on a feature in the feature list widget.
     * suppression - Triggered after a user click on the "Suppression" button in the feature list widget.
     * deplacement - Triggered after a user move a feature.
     */
    EVENT_TYPES: ['creationpoint',
                  'creationline',
                  'creationpolygon',
                  'consult',
                  'suppression',
                  'deplacement'],
		
	/**
	 * Property: map
	 * {<OpenLayers.Map>} The map.
	 */	
    map: null,

    /**
     * Property: layerList
     * {<GeoSIE.LayerList>} The widget LayerList
     */
    layerList: null,      
    
    /**
     * Property: legend
     * {<GeoSIE.Legend>} The widget legend
     */    
    legend: null,
    
    /**
     * Property: layerListLegend
     * {<GeoSIE.LayerListLegend>} The LayerListLegend widget.
     */
    //layerListLegend: null,
    
    /**
     * Property: gazetteer
     * {<GeoSIE.Gazetteer>} The Gazetter widget.
     */
	gazetteer: null,
    
	/**
	 * Property: crsSelector 
     * {<GeoSIE.CrsSelector>} The CrsSelector widget.
	 */
	crsSelector: null,

    /**
     * Property: featureList
     * {<GeoSIE.FeatureList>} The FeatureList widget.
     */
    featureList: null,
	
	/**
	 * Property: printControl
	 * {<GeoSIE.Control.Print>} The control to print.
	 */			
	printControl: null,
	
	/**
	 * Property: editPointControl
	 * {<GeoSIE.Control.EditPoint>} The control to add a point.
	 */		
	editPointControl: null,

	/**
	 * Property: editLineControl
	 * {<GeoSIE.Control.EditLine>} The control to add a multi linestring.
	 */		
	editLineControl: null,
	
	/**
	 * Property: editPolygonControl
	 * {<GeoSIE.Control.EditPolygon>} The control to add a polygon.
	 */		
	editPolygonControl: null,
		
	/**
	 * Property: wfsGetFeatureControl
	 * {<GeoSIE.Control.WfsGetFeature>} The control to select features.
	 */			
	wfsGetFeatureControl: null,

	/**
	 * Property: snappingControl
	 * {<GeoSIE.Control.Snapping>} The snapping control.
	 */	
	snappingControl : null,

	/**
	 * Property: wpsSelector
	 * {<GeoSIE.Control.WpsSelector>} The widget drop down menu to select the process.
	 */	
	wpsSelector: null,

	/**
	 * Property: wpsGetFeatureControl
	 * {<GeoSIE.Control.WpsGetFeature>} The control to get and display the wps result. 
	 */		
	wpsGetFeatureControl: null,

	/**
	 * Property: wpsSelectProcess
	 * {<OpenLayers.Control.Button>} The control to display the wpsSelector.
	 */			
	wpsSelectProcess: null,
	
	/**
	 * Property: wpsDownloadControl
	 * {<GeoSIE.Control.WpsDownload>} The control to download the wps result displayed.
	 */				
	wpsDownloadControl: null,

	/**
	 * Property: measureControl
	 * {<GeoSIE.Control.Measure>} The measure control.
	 */		
	measureControl: null,
	
	/**
	 * Property: fullscreenControl
	 * {<GeoSIE.Control.FullScreen>} The control to display the view in full screen.
	 */				
	fullscreenControl: null,	

	/**
	 * Property: helpControl
	 * {<GeoSIE.Control.Link>} The control to display help button.
	 */			
	helpControl: null,

	/**
	 * Property: contactControl
	 * {<GeoSIE.Control.Link>} The control to display contact button.
	 */			
	contactControl: null,	
	
    /**
     * Property: copyrightsControl
     * {<GeoSIE.Control.Copyrights>} The control to display copyrights.
     */
    copyrightsControl: null,
    
    /**
     * Property: overviewControl
     * {<OpenLayers.Control.OverviewMap>} The control to the overview map.
     */
    overviewControl: null,	

    /**
     * Property: panelNav
     * {<OpenLayers.Control.Panel>} The Panel with the control to navigate.
     */
    panelNav: null,    
    
    /**
     * Property: panelEdit
     * {<OpenLayers.Control.Panel>} The Panel with the control to edit.
     */
    panelEdit: null,

    /**
     * Property: panelProcess
     * {<OpenLayers.Control.Panel>} The Panel with the control to process.
     */
    panelProcess: null,    
    
    /**
     * Property: panelOther
     * {<OpenLayers.Control.Panel>} The Panel with the other control.
     */
    panelOther: null,       
    
    /**
     * Property: layersContext
     * {Array({Object})} Array of layer contexts.
     */
    layersContext: [],

    /**
     * Property: format
     * {<OpenLayers.Format.WMC>} A WMC parser.
     */
    format: null,

    /**
     * APIProperty: events
     * {OpenLayers.Events} The object to use to register event.
     * listeners.
     */
    events: null,
    
    /**
     * Property: vectorTempLayer
     * {<OpenLayers.Layer.Vector>} The vector layer used for drawing temporary features.
     */
    vectorTempLayer: null,

    /**
     * Property: vectorLayer
     * {<OpenLayers.Layer.Vector>} The vector layer used for display features.
     */    
    vectorLayer: null,
    
    /**
     * Property: defaultControl
     * The default control, useful for example when an editing control.
     *     cannot be activated
     */
    defaultControl: null,
    
    /**
     * Property: selectControl
     * {<OpenLayers.Control.SelectFeature>} The SelectFeature control to be used to select (highlight) features
     * in the editing layer.
     */
    selectControl: null,
    
    /**
     * Property: dragFeatureControl
     * {<GeoSIE.Control.DragFeature>} The CustoDragFeature control.
     */
    dragFeatureControl: null,

    /**
     * Property: zoneselector
     * {<GeoSIE.Control.ZoneSelector>} Zone switching control.
     */
    zoneselector: null,    
    
    /**
     * Property: context
     * {Object} An object representing the Web Map Context as read by OpenLayers.
     */
    context: null,

    /**
     * Property: wgs84
     * {<OpenLayers.Projection>} Projection in 4326.
     */
    wgs84: null,
    
    /**
     * Method: init
     * Initializes the GeoSIE.Brique (ex Carto) object ie. creates the map, widgets,
     * loads the context then adds the controls to the map.
     * 
     * Parameters:
     * customSetup - {<GeoSIE.Config>} Custom Config.
     */
    init: function(customSetup) {
    	var setup = GeoSIE.Config;
    	if(customSetup) {
    		setup = customSetup;
    	}
    	
    	this.wgs84 = new OpenLayers.Projection('EPSG:4326');
    	
/*
    	try {this.map.destroy;}
    		   catch(e){
    			   OpenLayers.Console.log(e);
    	}; //ignore the error
*/    	
        this.map = new OpenLayers.Map('olmap', {
        	// get the default projection, if not set, the defaut projection is 4326
        	// and if a layer in the context has only one projection that is not 4326, but it has the projection
        	// of the zone, this layer is not added.
        	projection: GeoSIE.ZoneManager.getDefaultZone().projections[0][0],
            scales: setup.MAP_SCALES,
                 controls: [],
                 theme: null // or OpenLayers will try to load its
                             // default theme
        });
        
        // the base layer
        this.map.addLayers([
            new OpenLayers.Layer(
                "fake",
                {
                    displayInLayerSwitcher: false,
                    isBaseLayer: true
                }
            )
        ]);

        this.events = new OpenLayers.Events(this, null, this.EVENT_TYPES);
        
        this.loadContext(setup.WMC);
    },
    
    
    /**
     * Method: addVectorLayer
     * Add the vector layers to the map.
     */
    addVectorLayer: function(){
        this.vectorTempLayer = new OpenLayers.Layer.Vector("Temp", {
            displayInLayerSwitcher: false,
            rendererOptions: {
                // this forces the selectFeature to go on top
                // useful for overlapping features
                zIndexing: true 
            }
        });
        
        this.vectorLayer = new OpenLayers.Layer.Vector("Résultat", {
            displayInLayerSwitcher: false,
            rendererOptions: {
                // this forces the selectFeature to go on top
                // useful for overlapping features
                zIndexing: true 
            }
        });
        
        this.map.addLayers([this.vectorTempLayer,this.vectorLayer]); //,this.vectorTempLayer
    },
    
    /**
     * Method: reload
     * Reloads (or load (init)) the map when a new zone is selected.
     * 
     * Parameters:
     * zoneId - {String}
     */
    reload: function(zoneId) {
    	OpenLayers.Console.log("GeoSIE.Brique::reload");
        // remove the application layers
        for (var i=0; i<this.layersContext.length; i++) {
        	var layerContext = this.layersContext[i];
        	var layerOnMap = this.map.getLayersByName(layerContext.title)[0];
        	if(layerOnMap){
            	//this.map.removeLayer(layerOnMap, false); // bug map.getResolution map is null sometime
				layerOnMap.destroy();
        	}
        }

        // add the layers to the map
        this.addContextLayers(this.layersContext, zoneId);
        
        // raise the vector layer because after a reload they are not on 
        // the top of the layer list. Remove all the features.
        // Fire the map event changelayer => redraw the layerListLegend
        var nblayers = this.map.getNumLayers();
        this.map.setLayerIndex(this.vectorLayer,nblayers-1);
        this.map.setLayerIndex(this.vectorTempLayer,nblayers);
        this.vectorLayer.destroyFeatures();
        this.vectorTempLayer.destroyFeatures();
        
        // draw the layerlist/layerlistlegend
        this.layerListLegend.redraw();
        //this.layerList.redraw();
    },
    
    /**
     * Method: loadContext
     * Loads the WMC file.
     *
     * Parameters:
     * wmc - {String} WMC URL.
     */
    loadContext: function(wmc) {
        OpenLayers.Request.GET({
            url: wmc,
            success: this.onLoadContextComplete,
            failure: this.onLoadContextFailure,
            scope: this
        });
    },
    
    /**
     * Method: onLoadContextComplete
     * Called when the WMC request succeeds.
     * 
     * Parameters:
     * request - {XMLHttpRequest}
     */
    onLoadContextComplete: function(request) {
        var layerOptions = {
            transparent: true,
            numZoomLevels: GeoSIE.Config.MAP_SCALES.length,
            isBaseLayer: false,
            opacity: 1,
            displayInLayerSwitcher: true,
            singleTile: true,
            ratio: 1
        };
        var layerParams = {
            transparent: true
        };
        this.format = new OpenLayers.Format.WMC({
            layerOptions: layerOptions,
            layerParams: layerParams
        });

        // overrides layer readings for SRS        
        OpenLayers.Format.WMC.v1_1_0.prototype.read_wmc_SRS = function(layerContext, node) {
            if (!layerContext.srs)
                layerContext.srs=[];
            layerContext.srs.push(this.getChildValue(node));   
        },
        
        // overrides layer readings for attribution  
        OpenLayers.Format.WMC.v1_1_0.prototype.read_ol_attribution = function(layerContext, node) {
           layerContext.attribution = this.getChildValue(node);
        },
        
        // overrides layer readings for snappable
        OpenLayers.Format.WMC.v1_1_0.prototype.read_ol_snappable = function(layerContext, node) {
           layerContext.snappable = (this.getChildValue(node) == "true");
        },        
        
        // overrides layer readings for wpsable
        OpenLayers.Format.WMC.v1_1_0.prototype.read_ol_wpsable = function(layerContext, node) {
           layerContext.wpsable = (this.getChildValue(node) == "true");
        },  
        

        OpenLayers.Format.WMC.v1_1_0.prototype.read_ol_maxExtentLayer = function(layerContext, node) {
            if (!layerContext.maxExtentLayers)
                layerContext.maxExtentLayers=[];
            
            var bounds = new OpenLayers.Bounds(
                node.getAttribute("minx"), node.getAttribute("miny"),
                node.getAttribute("maxx"), node.getAttribute("maxy")
            );
            layerContext.maxExtentLayers.push(bounds);
        },
        
        // overrides layer readings for mapBackground
//        OpenLayers.Format.WMC.v1_1_0.prototype.read_ol_mapBackground = function(layerContext, node) {
//           layerContext.mapBackground = (this.getChildValue(node) == "true");
//        },  
        
        // we save the context as read by OpenLayers 
        // this is not API but really useful
        this.context = this.format.read(request.responseXML);
        var layers = this.context.layersContext;  
        
        // we save the layers in the context, the context.layersContext is update after
        for(var i=0;i<layers.length;i++){
            var layer = layers[i];
            this.layersContext.push(layer);
        }
        
        // the layers are added to the map later with method reload with setLayerIndex (fire the event changelayer)
        //this.addContextLayers(layers);
  
        this.addVectorLayer();
        this.loadSLD();  
        
        this.addControls();
        
        this.addWidgets();
        
        // register listeners for events triggered by other
        // application modules    
        this.registerListeners();
        
        this.firstLoadMapAndLocator(this.context.bounds);
    },
    
    
    /**
     * Method: addContextLayers
     * Add the layers in a WMC context to the map.
     * 
     * Parameters:
     * layers - {Object} The layers in a WMC context. 
     * zoneId - {String} The zone id.
     */
    addContextLayers : function(layers, zoneId){
        // we remove all unsupported layers for current SRS and zones
        var suitableLayers = [];
        var currentSRS = this.map.projection;
        OpenLayers.Console.log("GeoSIE.Brique::addContextLayers - currentSRS : "+currentSRS);

        var currentZone = GeoSIE.ZoneManager.getZoneById(zoneId);
        var currentZoneBounds = (currentZone.extent || currentZone.options.maxExtent);
        var currentZoneBoundsWGS84 = currentZoneBounds.clone().transform(
                new OpenLayers.Projection(this.map.projection),
                this.wgs84
        ); 
        
        // Filter SRS  
        for(var i=0;i<layers.length;i++){
        	var hasSRS = false;
            var layer = layers[i];
            if (layer.srs){
                for (var j=0; j<layer.srs.length; j++){
                    hasSRS = hasSRS || layer.srs[j].indexOf(currentSRS)==0;
                }
                
                // Filter extent
            	var hasZone = true;
                if (layer.maxExtentLayers){
                    for (var m=0; m<layer.maxExtentLayers.length; m++){
                    	var layerBounds = layer.maxExtentLayers[m];
                    	var hasZoneTemp = currentZoneBoundsWGS84.intersectsBounds(layerBounds);
                    	hasZone = hasZoneTemp;
                    	// if one extent include display the layer
                    	if(hasZoneTemp){
                    		break;
                    	}
                    }
                }
                
                if (hasSRS && hasZone)
                    suitableLayers.push(layer);
            }
        }
        
        this.context.layersContext = suitableLayers;
        
        // add the layers to the map
        this.format.mergeContextToMap(this.context, this.map);
        
		// add attribution and metadataURL to the layer on the map
		for(var i=0;i<layers.length;i++){
			var layer = layers[i];
			var layerOnMap=this.map.getLayersByName(layer.title)[0];

			if(layerOnMap){
				if(layer.attribution){
					layerOnMap.addOptions({attribution :layer.attribution});
				};
				
				if(layer.snappable){
					layerOnMap.addOptions({snappable :layer.snappable});
				};				
				
				if(layer.wpsable){
					layerOnMap.addOptions({wpsable :layer.wpsable});
				}
				
				if(layer.metadataURL){
				   layerOnMap.addOptions({metadataURL :layer.metadataURL});
				};
				
//				if(layer.mapBackground){
//				   layerOnMap.addOptions({mapBackground :layer.mapBackground});
//				};
			} 
		};
    },
    
    /**
     * Method: onLoadContextFailure
     * Called when the WMC request fails.
     * 
     * Parameters:
     * request - {XMLHttpRequest}
     */
    onLoadContextFailure: function(request) {
        OpenLayers.Console.error("Something went wrong with the WMC");
    },
    
    /**
     * Method: loadSLD
     * Loads the SLD file.
     */
    loadSLD: function() {
        OpenLayers.Request.GET({
            url: GeoSIE.Config.STYLE,
            success: this.onLoadSLDComplete,
            failure: this.onLoadSLDFailure,
            scope: this
        });
    },
    
    /**
     * Method: onLoadSLDComplete
     * Called when the SLD request succeeds.
     * 
     * Parameters:
     * request - {XMLHttpRequest}
     */
    onLoadSLDComplete: function(request) {
        var format = new OpenLayers.Format.SLD();
        var sld = format.read(request.responseXML || request.responseText);
        
        this.vectorLayer.styleMap.styles['default'] = this.getStyler(sld, "features", 'default');
        this.vectorLayer.styleMap.styles['select'] = this.getStyler(sld, "features", 'select');
        this.vectorLayer.styleMap.styles['temporary'] = this.getStyler(sld, "features", 'temporary');
        this.vectorLayer.redraw();
        
        this.vectorTempLayer.styleMap.styles['default'] = this.getStyler(sld, "features", 'defaultTemp');
        this.vectorTempLayer.styleMap.styles['select'] = this.getStyler(sld, "features", 'selectTemp');
        this.vectorTempLayer.styleMap.styles['temporary'] = this.getStyler(sld, "features", 'temporaryTemp');
        this.vectorTempLayer.redraw();
    },
    
    /**
     * Method: onLoadSLDFailure
     * Called when the SLD request fails.
     * 
     * Parameters:
     * request - {XMLHttpRequest}
     */
    onLoadSLDFailure: function(request) {
        OpenLayers.Console.error("Something went wrong with the SLD");
    },
    
    /**
     * Method: getStyler
     * Get the named style in the SLD.
     * 
     * Parameters:
     * sld - {Object} An object representing the SLD.
     * layerName - {String} The layer name.
     * stylerName - {String} The style name in the SLD (<sld:Name>default</sld:Name).
     * 
     * Returns: 
     * {<OpenLayers.Style>} a SLD style
     */
    getStyler: function(sld, layerName, stylerName) {
    	var styles = sld.namedLayers[layerName].userStyles;
        var style;
        for(var i=0; i<styles.length; ++i) {
            style = styles[i];
            if(style.name == stylerName) {
                break;
            }
        }
        return style;
    },

    /**
     * Method: addWidgets
     * Add all the widgets to the page.
     */
    addWidgets: function () {
        // layers list + legend
		this.layerListLegend = new GeoSIE.LayerListLegend({
			div: OpenLayers.Util.getElement("layerListLegend"),
			map: this.map,
			context: this.context,
			noLegendMessage: GeoSIE.Messages.noLegendMessage,
			labelLegend: 'Légende',
			titleLegend: 'Afficher les légendes de cette couche',
			bgcolor: '#ffffff'	
		});     
		
    	/*this.layerList = new GeoSIE.LayerList({
            div: OpenLayers.Util.getElement("layerList"),
            map: this.map
    	});
    	
	    this.legend = new GeoSIE.Legend({
	        div: OpenLayers.Util.getElement("legend"),
	        map: this.map,
	        context: this.context,
	        noLegendMessage: 'Aucune légende'
	    }); */   
    	
        // gazetteer
        this.gazetteer = new GeoSIE.Gazetteer({
        	div: OpenLayers.Util.getElement("gazetteer"),
        	map: this.map
        });

        GeoSIE.GoTo.init();
        
        // crsSelector (OpenLayers.MousePosition (cursorTrack) add-on)
        this.crsSelector = new GeoSIE.CrsSelector({
        	div: OpenLayers.Util.getElement("crsSelector"),
        	map: this.map       	
        });
        
        // feature list
        this.featureList = new GeoSIE.FeatureList({
        	id: "control_feature_list",
            div: OpenLayers.Util.getElement("featureList"),
            layer: this.vectorLayer
        });
    },
    
    /**
     * Method: addControls
     * Add all the OL controls to the map.
     */
    addControls: function() {
    	// Defaults control
        // zoneSelector control
        this.zoneselector = GeoSIE.ZoneManager.init(); //this.map
        this.map.addControl(this.zoneselector);

    	// loading control 
        // cf http://dev.openlayers.org/addins/loadingPanel/trunk/examples/loadingpanel.html
        this.map.addControl(new GeoSIE.Control.LoadingPanel({
            div: OpenLayers.Util.getElement("status_loading")
        }));        

        // copyright 
        this.copyrightsControl = new GeoSIE.Control.Copyrights({
        	div: OpenLayers.Util.getElement('sourceTrack')
        });
        this.map.addControl(this.copyrightsControl);        

        // FIXME : bug sous IE, les features sont "masqués" par le PanPanel,
        // un problème de z-index qui empeche de deplacer les features 
        var browserCode = OpenLayers.Util.getBrowserName();
        if(browserCode != "msie"){
            this.map.addControl(new OpenLayers.Control.PanPanel());
        }        
        // navigation 
        this.map.addControl(new OpenLayers.Control.Navigation(
        		{
                    // FIXME : probleme sur certaine configuration, 1 cran de souris = 4 niveau de zoom
        			// cf http://trac.osgeo.org/openlayers/ticket/2672
        			// http://osgeo-org.1560.n6.nabble.com/mousewheel-jumps-two-zoom-levels-td4340746.html
        			mouseWheelOptions: {
                        cumulative: false,
                        interval: 20
                    }
        		}
        ));
        
        // scaleLine 
        this.map.addControl(new OpenLayers.Control.ScaleLine({
                bottomOutUnits: ''
            })
        );
          
        // cursortrack 
        this.map.addControl(new OpenLayers.Control.MousePosition({
            div: OpenLayers.Util.getElement('cursorTrack'),
            prefix: "X&nbsp;:&nbsp;",
            separator: " Y&nbsp;:&nbsp;"
//            suffix: " SRS: "+this.map.projection,
//            numDigits: 2
        }));
        
        // scale
        this.map.addControl(new GeoSIE.Control.ScaleCombo({
            div: OpenLayers.Util.getElement("scale")
        }));

        // zoombar
        this.map.addControl(new GeoSIE.Control.ZoomBar({
            div: OpenLayers.Util.getElement('zoomBar'),
            zoomStopHeight: 6
        }));
        
        // Main controls bar
    	var controlsNav = [];
    	var controlsEdit = [];
    	var controlsProcess = [];
    	var controlsOther = [];

    	var widthControl = 37;//42
    	
    	// navigation bar
    	var leftEditionBar = 93; //width navigation libelle, -3
    	for(var i=0; i<GeoSIE.Config.NAVIGATION_BUTTONS.length; i++){
    		var button = GeoSIE.Config.NAVIGATION_BUTTONS[i];
    	    var control = null;
    	    
	        switch(button){
	            case 'zoomIn':
	                control = new OpenLayers.Control.ZoomBox({title: GeoSIE.Messages.zoomInToolTip});
	                break;
	            case 'zoomOut':
	                control = new OpenLayers.Control.ZoomBox({
	                	title: GeoSIE.Messages.zoomOutToolTip, 
	                	out: true, 
	                	displayClass: 'olControlZoomOut'}
	                );
	                break;	        
	            case 'nav':
	                // panel pour la navigation
	                control = new OpenLayers.Control.DragPan({title: GeoSIE.Messages.dragPanToolTip});
	                break;
	            case 'history':
	                // navigationHistory 
	                control = new GeoSIE.Control.NavigationHistory({
	                    zoneSelectorControl: this.zoneselector,
	            		previousOptions: {
	            			title: GeoSIE.Messages.navigationHistoryPreviousToolTip
	            		}, 
	            		nextOptions: {
	            			title: GeoSIE.Messages.navigationHistoryNextToolTip
	            		}
	                });
	                this.map.addControl(control);
	                break;
	            case 'maxExtent':
	                control = new OpenLayers.Control.ZoomToMaxExtent({title: GeoSIE.Messages.zoomToMaxExtentToolTip});
	                break;
	            case 'print':
	                this.printControl = new GeoSIE.Control.Print({
	        			title: GeoSIE.Messages.printToolTip,        	
	                    displayClass: 'GeoSIEControlPrint',
	                	vectorLayer: this.vectorLayer,
	                	context: this.context
	                });   	            	
	            	control = this.printControl;
	            	break;
	            default:
	                OpenLayers.Console.error("GeoSIE.Brique::addControls - control: "+button);
	                return;
	        }

	        if(button == 'history'){
	        	controlsNav.push(control.previous);
	        	controlsNav.push(control.next);
	           leftEditionBar += widthControl*2;
	        } else {
	        	controlsNav.push(control);
	           leftEditionBar += widthControl;
	        }    		
    	}

    	// edition bar
    	var widthEditionBar = 95;
    	
    	for(var i=0; i<GeoSIE.Config.EDITION_BUTTONS.length; i++){
    		var button = GeoSIE.Config.EDITION_BUTTONS[i];
    	    var control = null;
    	    
	        switch(button){
	            case 'point':
	                this.editPointControl = new GeoSIE.Control.EditPoint({
	        			title: GeoSIE.Messages.editPointToolTip,        	
	                    layer: this.vectorTempLayer,
	                    minScale: GeoSIE.Config.EDIT_MIN_SCALE,
	                    scaleMessage: GeoSIE.Messages.scaleMessage
	                });
	                control = this.editPointControl;
	                break;
	            case 'line':
	                this.editLineControl = new GeoSIE.Control.EditLine({
	        			title: GeoSIE.Messages.editLineToolTip,        	
	                    layer: this.vectorTempLayer,
	                    minScale: GeoSIE.Config.EDIT_MIN_SCALE,
	                    scaleMessage: GeoSIE.Messages.scaleMessage
	                });
	                control = this.editLineControl;
	                break;	        
	            case 'polygon':
	                this.editPolygonControl = new GeoSIE.Control.EditPolygon({
	        			title: GeoSIE.Messages.editPolygonToolTip,        	
	                    layer: this.vectorTempLayer,
	                    minScale: GeoSIE.Config.EDIT_MIN_SCALE,
	                    scaleMessage: GeoSIE.Messages.scaleMessage
	                });
	                control = this.editPolygonControl;
	                break;
	            case 'snapping':
	                this.snappingControl = new GeoSIE.Control.Snapping({
	                	//map: this.map,
	                	layer: this.vectorTempLayer,
	                	targets: [this.vectorTempLayer],
	                	edge: false,
	                	isSnapped: true,
	                	title: GeoSIE.Messages.snappingToolTip,        	
	                    minScale: GeoSIE.Config.SNAPPING_MIN_SCALE,
	                    scaleMessage: GeoSIE.Messages.scaleMessage
	                });  
	                control = this.snappingControl;
	                break;
	            case 'query':
	                this.wfsGetFeatureControl = new GeoSIE.Control.WfsGetFeature({
	        			title: GeoSIE.Messages.getFeatureToolTip,        	
	                    layer: this.vectorLayer,
	                    tolerance: 5,
	                    maxFeatures: 20,
	                    //minScale: GeoSIE.Config.QUERY_MIN_SCALE,
	                    scaleMessage: GeoSIE.Messages.scaleMessage
	                });
	                control = this.wfsGetFeatureControl;
	                break;
	            case 'drag':
	                this.dragFeatureControl = new GeoSIE.Control.DragFeature({
	                	featureLayer: this.vectorLayer,
	        			title: GeoSIE.Messages.dragFeatureToolTip,        	
	                    targetLayerName: "Résultat",
	                    //minScale: GeoSIE.Config.DRAG_MIN_SCALE,
	                    scaleMessage: GeoSIE.Messages.scaleMessage
	                });	   
	                control = this.dragFeatureControl;
	            	break;
	            default:
	                OpenLayers.Console.error("GeoSIE.Brique::addControls - control: "+button);
	                return;
	        }

	        controlsEdit.push(control);
	        widthEditionBar += widthControl;
    	}    	
    	var editionBar = GeoSIE.Util.displayWidgets(GeoSIE.Config.EDITION_BUTTONS, "edition", widthEditionBar, leftEditionBar);
    	
    	// process bar
    	var widthProcessBar = 95;
    	var hasWPS = false;
      	for(var i=0; i<GeoSIE.Config.PROCESS_BUTTONS.length; i++){
    		var button = GeoSIE.Config.PROCESS_BUTTONS[i];
    	    
	        switch(button){
	            case 'wps':
	                // WPS processes combo 
	                this.wpsSelector = GeoSIE.WpsManager.init(OpenLayers.Util.getElement("processes"));
	                this.map.addControl(this.wpsSelector);       
	                // select WPS process
	                this.wpsSelectProcess = new OpenLayers.Control.Button({
	        			title: GeoSIE.Messages.wpsSelectToolTip,        	
	                    displayClass: 'GeoSIEControlWpsSelect'
	                });	

	                // WPS control
	                this.wpsGetFeatureControl = new GeoSIE.Control.WpsGetFeature({
	        			title: GeoSIE.Messages.wpsGetFeatureToolTip,        	
	        			lineLayer: this.vectorTempLayer,
	                    maxFeatures: 1
	                });        

	                // download WPS
	                this.wpsDownloadControl = new GeoSIE.Control.WpsDownload({
	        			title: GeoSIE.Messages.wpsDownloadToolTip,        	
	                    displayClass: 'GeoSIEControlWpsDownload'
	                });
	                
	                controlsProcess.push(this.wpsSelectProcess);
	                controlsProcess.push(this.wpsGetFeatureControl);
	                controlsProcess.push(this.wpsDownloadControl);
	                
	                widthProcessBar += 3*widthControl;
	                hasWPS = true;
	                break;
	            case 'measure':
	                this.measureControl = new GeoSIE.Control.Measure(
		                OpenLayers.Handler.Path, {
		                    persist: true,
		                    handlerOptions: {
		                        layerOptions: {
		                        	layer: this.vectorLayer
		                            //renderers: renderer,
		                            //styleMap: styleMap
		                        }
		                    },
		                    title: GeoSIE.Messages.measureToolTip, 
		                    displayClass: 'GeoSIEControlMeasure'
		                    //isMeasurePartial: true
		                }
	                );
	                controlsProcess.push(this.measureControl);
	                widthProcessBar += widthControl;
	                break;	        
	            default:
	                OpenLayers.Console.error("GeoSIE.Brique::addControls - control: "+button);
	                return;
	        }
    	}    	
      	var leftProcessBar = leftEditionBar + (editionBar ? widthEditionBar : 0);
    	var processBar = GeoSIE.Util.displayWidgets(GeoSIE.Config.PROCESS_BUTTONS, "process", widthProcessBar, leftProcessBar);
    	
      	// other bar
    	for(var i=0; i<GeoSIE.Config.OTHER_BUTTONS.length; i++){
    		var button = GeoSIE.Config.OTHER_BUTTONS[i];
    	    var control = null;
    	    
	        switch(button){
	            case 'full':
	            	control = new GeoSIE.Control.FullScreen ({     	
	                	displayClass: 'GeoSIEControlFullScreen',
	                	sizeFeatureList: 0.7, // TODO : test si featureList dans dialog
	                	title: GeoSIE.Messages.fullScreenMaximizeToolTip,
	                	minimizeTitle: GeoSIE.Messages.fullScreenMinimizeToolTip
	                });	
	                break;
	            case 'help':
	                control = new GeoSIE.Control.Link({
	                	displayClass: 'GeoSIEControlHelp',
	                	title: GeoSIE.Messages.helpToolTip,
	                	url: GeoSIE.Config.URL_HELP
	                });
	                break;	        
	            case 'contact':
	                control = new GeoSIE.Control.Link({
	                	displayClass: 'GeoSIEControlContact',
	                	title: GeoSIE.Messages.contactToolTip,
	                	url: GeoSIE.Config.URL_CONTACT
	                });
	                break;
	            default:
	                OpenLayers.Console.error("GeoSIE.Brique::addControls - control: "+button);
	                return;
	        }

	        controlsOther.push(control);
	        //widthEditionBar += widthControl;
    	}    
    	//var otherBar = GeoSIE.Util.displayWidgets(GeoSIE.Config.OTHER_BUTTONS, "other", widthProcessBar, leftEditionBar + (editionBar ? widthEditionBar : 0));
      	
        // FeatureList
        this.selectControl = new OpenLayers.Control.SelectFeature(
                this.vectorLayer, 
                {
                    hover: true
                }
        );
        this.map.addControl(this.selectControl);
        this.selectControl.activate();
        
        // FIXME : bug avec Box sous IE quand unload avec debugs script actives, 
        // le dragHandler est null.
        // Se produit quand 1 des controls zoom est actif puis refresh page
		// cf http://trac.osgeo.org/openlayers/ticket/2578
        var browserCode = OpenLayers.Util.getBrowserName();
        if(browserCode == "msie"){
            OpenLayers.Handler.Box.prototype.deactivate = function () {
    	        if (OpenLayers.Handler.prototype.deactivate.apply(this, arguments)) {
    	            if(this.dragHandler != null) {
    	            	this.dragHandler.deactivate();
    	            }
    	            return true;
    	        } else {
    	            return false;
    	        }
            };
        }

        // TOTO : rendre configurable aussi
        this.defaultControl = controlsNav[0];
        
        this.panelNav = new OpenLayers.Control.Panel({
            div: OpenLayers.Util.getElement('navigationButtonBar_panel'),
            defaultControl: this.defaultControl,
            activateControl: this.multiPanelActivateControl
        });
        this.panelNav.addControls(controlsNav);
        this.map.addControl(this.panelNav);
        
        if(editionBar){
            this.panelEdit = new OpenLayers.Control.Panel({
                div: OpenLayers.Util.getElement('editionButtonBar_panel'),
                //defaultControl: this.defaultControl,
                activateControl: this.multiPanelActivateControl
            });
            this.panelEdit.addControls(controlsEdit);
            this.map.addControl(this.panelEdit);           	
        }
     
        if(processBar){
            this.panelProcess = new OpenLayers.Control.Panel({
                div: OpenLayers.Util.getElement('processButtonBar_panel'),
                //defaultControl: this.defaultControl,
                activateControl: this.multiPanelActivateControl
            });
            this.panelProcess.addControls(controlsProcess);
            this.map.addControl(this.panelProcess);   
            
            // Move the dropdown wps
            if(hasWPS){
            	OpenLayers.Util.getElement('processes').style.left = leftProcessBar + "px";
            }
        }
        
        if(GeoSIE.Config.OTHER_BUTTONS.length>0){
            this.panelOther = new OpenLayers.Control.Panel({
                div: OpenLayers.Util.getElement('otherButtonBar_panel'),
                //defaultControl: this.defaultControl,
                activateControl: this.multiPanelActivateControl
            });
            this.panelOther.addControls(controlsOther);
            this.map.addControl(this.panelOther);      
    		var el = OpenLayers.Util.getElement("otherButtonBar");
    		el.style.display = "block";
        }
        
        //this.map.addControl(new OpenLayers.Control.Permalink());//'permalink'

	},
	
	/**
	 * Method: multiPanelActivateControl
	 * Overload method for the activateControl of a OpenLayers.Control.Panel
	 * (cf InfoTerre).
	 *
	 * Parameters:
	 * control - {OpenLayers.Control} control to activate
	 */
	multiPanelActivateControl: function(control){
	    if (!this.active) {
	        return false;
	    }
	    if (control.type == OpenLayers.Control.TYPE_BUTTON) {
	        control.trigger();
	        this.redraw();
	        return;
	    }
	    if (control.type == OpenLayers.Control.TYPE_TOGGLE) {
	        if (control.active) {
	            control.deactivate();
	        } else {
	            control.activate();
	        }
	        this.redraw();
	        return;
	    }

	    var panelList = this.map.getControlsByClass("OpenLayers.Control.Panel");
	    for (var j=0, pLen=panelList.length; j<pLen; j++) {
	        var currPanel = panelList[j];
	        //if(currPanel.id != "toolsBar"){ // TODO: touver autre facon
	        for (var i=0, len=currPanel.controls.length; i<len; i++) {
	            if (currPanel.controls[i] != control) {
	                if (currPanel.controls[i].type != OpenLayers.Control.TYPE_TOGGLE) {
	                    currPanel.controls[i].deactivate();
	                }
	            }
	        }
	    //}
	    }
	    control.activate();		
	},
	
    /**
     * Method: registerListeners
     * Register listeners for events triggered by other
     * application modules.
     */
    registerListeners: function() {
		// Edition
    	if(this.editPointControl != null){
    		// point 
    		this.editPointControl.events.on({
                'activate': this.checkActivable,
                'creationpoint': function(feature) {
                    this.events.triggerEvent('creationpoint', feature);
                },
                scope: this
            });    		
    	}
    	
    	if(this.editLineControl != null){
            // multi-Line 
    		this.editLineControl.events.on({
                'activate': this.checkActivable,
                'creationline': function(feature) {
                    this.events.triggerEvent('creationline', feature);
                },
                scope: this
            });    		
    	}
		
    	if(this.editPolygonControl != null){
            // polygon 
    		this.editPolygonControl.events.on({
                'activate': this.checkActivable,
                'creationpolygon': function(feature) {
                    this.events.triggerEvent('creationpolygon', feature);
                },
                scope: this
            });    		
    	}

    	if(this.snappingControl!= null){
    		// snapping
            this.snappingControl.events.on({
                'activate': this.checkActivable,
                'snapping' : function(event){
                	this.events.triggerEvent('creationpoint', event.feature);
            	},
                scope: this
            });      		
    	}

    	if(this.wfsGetFeatureControl!= null){
            // get features
            this.wfsGetFeatureControl.events.on({
                'activate': this.checkActivable,
                scope: this
            });         		
    	}
 
    	if(this.dragFeatureControl != null){
            // drag feature
    		this.dragFeatureControl.events.on({
                'activate': this.checkActivable,
                'deplacement': function(feature) {
                    this.events.triggerEvent('deplacement', feature);
                },
                scope: this
            });        		
    	}
        
		this.vectorLayer.events.on({
            'featureselected': this.featureList.highlightRow,
            'featureunselected': this.featureList.dehighlightRow,
            scope: this.featureList
        });

        this.featureList.events.on({
            'overfeature': this.hilightFeature,
            'outfeature': this.dehilightFeature,
            scope: this
        });		
		
        this.map.events.on({
            "zoomend": function() {
                var editingControls = [this.editPointControl, this.wfsGetFeatureControl,
                                       this.snappingControl, this.dragFeatureControl,
                                       this.editLineControl, this.editPolygonControl]; 
                for (var i in editingControls) {
                    var control = editingControls[i];
                    if(control != null){
                        this.checkActivable(control, true);
                    }
                }
            },
            scope: this
         });  
        
		// Traitement
	    // Wps display drop menu
        if(this.wpsSelector != null){
    	    this.wpsSelectProcess.trigger = this.wpsSelector.showHide;
    	    this.wpsSelector.events.on({
    	    	'processchanged': this.wpsGetFeatureControl.setCurrentProcess,
                scope: this.wpsGetFeatureControl
    	    });        
    	    
	        // Wps get features
		    this.wpsGetFeatureControl.events.on({
		    	'getfeaturewps': this.wpsDownloadControl.onResultWps,
	            scope: this.wpsDownloadControl
	        });	    	
        }
   
	    // localisation
        this.map.events.on({
            moveend: this.onMoveend,
            scope: this
        });
        
        // zone manager
        GeoSIE.ZoneManager.events.on({
        	'zonechanged': this.onZonechanged,
            scope: this
        });
        
        // Gazetteer
        // goto
        GeoSIE.GoTo.events.on({
            zoomToExtent: this.onZoomToExtent,
            zoomToLonLat: this.onZoomToLonLat,
            scope: this
        });        
        
        // Map locator
        // listen the hide button 
        var arrowButton = document.getElementById('hide1');
        OpenLayers.Event.observe(arrowButton, "click", this.hideLocator);
	},	

	/**
	 * Method: checkActivable
	 * Checks if the control can be activated (depending on zoomLevel)
	 *     if not activable, the default control will be activated instead.
	 * 
	 * Parameters:
	 * object - {OpenLayers.Event|Object} event or object with an object property
	 *     containing the reference to the control for which we want to check
	 *     if it's activable
	 * silent - {Boolean} if true don't show any message to user
	 */
    checkActivable: function(object, silent) {
        var control = object.object || object;
        var minScale = control.minScale;
        var currentScale = Math.round(this.map.getScale());
        
        if (currentScale > minScale) { 
            if (!silent) {
                alert(GeoSIE.Messages.scaleMessage + minScale +").");
            }
            if (control.active) {
                // deactivate the editing control
                control.deactivate();
                this.defaultControl.activate();
            }
        }
    },

    /**
     * Method: forceRefreshLayers
     * Refresh to avoid caching of images. 
     * Usefull when you add a geomtry to show it in WMS.
     */
    forceRefreshLayers : function() {
        var layers = this.map.layers;
        for(var i = 0;i<layers.length;i++){
            var layer = layers[i];
            // pgiraud : TODO shouldn't we replace this by something like layer.redraw(true) ?
            if(layer.grid){
                for(var x = 0;x<layer.grid.length;x++){
                    var gx = layer.grid[x];
                    for(var y = 0;y<gx.length;y++){
                        var img = gx[y].imgDiv;
                        img.src = img.src.split("&uniqueidsie=")[0]+"&uniqueidsie="+new Date().getTime();
                    }
                }
            }
        }
    },

    /**
     * Method: refreshAfterCreation
     * 
     * Parameters: 
     * fid - {String} Openlayers fid or null if no features.
     */
    refreshAfterCreation : function(fid){
    	OpenLayers.Console.log("GeoSIE::refreshAfterCreation - Call Brique refreshAfterCreation");
        OpenLayers.Console.log(fid);    	
        var toRemove = [];
        for(var i=0; i < this.vectorLayer.features.length; i++){
            if (!this.vectorLayer.features[i].fid)
                toRemove.push(this.vectorLayer.features[i]);
        }
        OpenLayers.Console.log(toRemove);
        this.vectorLayer.removeFeatures(toRemove);
        // TODO : this has not been tested
        if (fid){
            //ajout à la sélection    
        	this.wfsGetFeatureControl.selectIds(fid);
        }
        this.forceRefreshLayers();
    },
       
    /**
     * Method: refreshSelection
     * Refresh the selection.
     * 
     * Parameters:
     * idsToAdd - {Array(String)} The ids to add. (ex: fid: couche.12, id => 12)
     * idsToDel - {Array(String)} The ids to del. (ex: fid: couche.14, id => 14)
     */
    refreshSelection : function(idsToAdd, idsToDel) {
    	var vectorLayer = this.vectorLayer;//getVectorLayer();
        if (vectorLayer) {
            var features = vectorLayer.features;

            var featuresId = [];

            for (var i=0; i<features.length; i++) {
                var featureId = features[i].fid;
                if (featureId) {
                    var toAdd = true;
                    for (var r=0;r<idsToDel.length;r++) {
                        if (featureId.split(".")[1] == idsToDel[r]) {
                            toAdd = false;
                            break;
                        }
                    }
                    if (toAdd) {
                        featuresId.push(featureId);
                    }
                }
            }

            for (var i=0;i<idsToAdd.length;i++) {
                featuresId.push("GENERIQUE." + idsToAdd[i]);
            }

            this.refreshFeatures(featuresId);
        }
    },
    
    /**
     * Method: refreshFeatures
     * Redraw the features selected.
     * 
     * Parameters:
     * featuresId - {Array(String)} The features id to refresh.
     */
    refreshFeatures : function(featuresId) {    	
    	//var wfsCtrl = this.map.getControlsBy('CLASS_NAME','GeoSIE.Control.WfsGetFeature')[0];
    	this.wfsGetFeatureControl.selectIds(featuresId);
    },    
    
    /**
     * Method: hilightFeature
     * 
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The selected feature. 
     */
    hilightFeature: function(feature) {
        this.selectControl.select(feature);
    },
    
    /**
     * Method: dehilightFeature
     * 
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The unselected feature. 
     */
    dehilightFeature: function(feature) {
        this.selectControl.unselect(feature);
    },

	/**
	 * Method: getNearestScale
	 * Return the closest scale calculate for the map.
	 * 
	 * Returns: 
	 * {Integer} The clost scale
	 */
	getNearestScale : function() {
		var scale = this.map.getScale();
		if (this.map.scales && this.map.scales.length > 0) {
			var currentScale = this.map.scales[0];
			for (var i=0; i<this.map.scales.length; i++) {
				var diffCurrent = Math.abs(scale - currentScale);
				var diffRang = Math.abs(scale - this.map.scales[i]);
				
				if (diffRang < diffCurrent) {
					currentScale = this.map.scales[i];
				}
			}
			return currentScale;
		}
		return scale;
	},

    /**
     * Method: getSelection
     * Returns the selected features.
     * 
     * Returns:
     * {Array(<OpenLayers.Feature.Vector>)} the features selected on the map
     */
    getSelection : function(){
        if (this.vectorLayer) {
            var selection = [];
            for(var i=0;i<this.vectorLayer.features.length;i++){
                var feature = this.vectorLayer.features[i];
                if (feature.attributes["sie_selected"]) //cf FeatureList methdo select
                    selection.push(feature);
            }
            return selection;
        } else return [];
    },

    /**
     * Method: firstLoadMapAndLocator
     * Force the right context to get loaded.
     * 
     * Parameters:
     * bounds - {<OpenLayers.Bounds>} the extent as read in the context
     */
    firstLoadMapAndLocator: function(bounds){
    	// TODO : plus complexe (plus de condition) sur infoterre
    	if(GeoSIE.Config.FORCED_AREA != null){
			this.zoneselector.selectZone(GeoSIE.Config.FORCED_AREA);
    	} else {
    		// zoom to the context extend (see all the world)
            this.map.zoomToExtent(bounds, true);
    	}
    },       
    
    /**
     * Method: onMoveend
     * Called each time the map is moved.
     * TODO : vraiment utile si integration complete de OL infoterre
     */
    onMoveend: function() {

    },    
 
    /**
     * Method: onZonechanged
     * Called when the GeoSIE.ZoneManager module triggers a
     * "zonechanged" event. 
     *
     * Parameters:
     * evt - {Object} Object with a two property:
     * * projection: the EPSG code of the projection
     * * displayProjections: the EPSG codes of the possible
     *   projections for the mouse position
     */
    onZonechanged: function(evt) {
    	OpenLayers.Console.log("GeoSIE.Brique::onZonechanged call");

        //var projection = evt.projection;
        var zoneId = evt.id;
        //var displayProjections = evt.displayProjections;
        var overviewInfo = evt.overviewInfo;

        // updates the layers
        this.reload(zoneId);
       
        OpenLayers.Console.log("GeoSIE.Brique::onZonechanged - reload done");

		// update the controls 
		this.panelNav.redraw();
		if(this.panelEdit != null){
			this.panelEdit.redraw();
		}
		if(this.panelProcess != null){
			this.panelProcess.redraw();
		}
		if(this.panelOther != null){
			this.panelOther.redraw();
		}
        // add overview map
        this.addOverviewMap(overviewInfo);
   },

    /**
     * Method: onZoomToExtent
     * Call when a recenter action is called (gazetter by coordinates). 
     * 
     * Parameters:
     * event - {OpenLayers.Event} The event containing the bounds in the selected projection.
     */
    onZoomToExtent: function(event) {
        var bounds = event.bounds;
        var epsgCode = event.epsgCode;
        var boundsCtrl = bounds.clone();
        
        if(epsgCode != "EPSG:4326"){
        	boundsCtrl.transform(new OpenLayers.Projection(epsgCode),
        			this.wgs84);
        }
        
        var zone = this.zoneselector.selectZoneForExtent(boundsCtrl);        
        if(!zone){
        	alert(GeoSIE.Messages.noZoneGotoMessage);
        	return;
        }
        
        if (this.zoneselector.zone != zone) {
            this.zoneselector.selectZone(zone, {zoomTo: false});
        } 
        
        bounds.transform(new OpenLayers.Projection(epsgCode),
        		this.map.getProjectionObject());
        
        this.map.zoomToExtent(bounds, epsgCode);
    },    
    
    /**
     * Method: onZoomToLonLat
     * Call when a recenter action is called (geonames).
     * 
     * Parameters:
     * event - {OpenLayers.Event} The event containing the lonlat in wgs84.
     */
    onZoomToLonLat: function(event) {
        var lonlat = event.lonlat; 
        var bounds = new OpenLayers.Bounds(lonlat.lon - 0.02, lonlat.lat - 0.02, lonlat.lon + 0.02, lonlat.lat + 0.02);
        var zone = this.zoneselector.selectZoneForExtent(bounds);
        if(!zone){
        	alert(GeoSIE.Messages.noZoneGotoMessage);
        	return;
        }
        if (this.zoneselector.zone != zone) {
            this.zoneselector.selectZone(zone, {zoomTo: false});
        }
		bounds.transform(this.wgs84, this.map.getProjectionObject());
		
		this.map.zoomToExtent(bounds);
    },  
    
    /**
     * Method: addOverviewMap
     * Add overview map control to the map.
     *
     * Parameters:
     * info - {Object} Overview map information, an object with three
     * properties:
     * * extent - {<OpenLayers.Bounds>} The bounds.
     * * url - {String} The URL to the overview image.
     * * size - {<OpenLayers.Size>} The size of the overview image.
     */
    addOverviewMap: function(info) {
    	if(this.overviewControl){
            this.overviewControl.destroy();
    	}
        
        var proj =  this.map.getProjectionObject();
        var units = proj.getUnits();
        var extent = info.extent.clone();

        var overViewOptions = {
                div: document.getElementById("locatorMap"),
                mapOptions: {
                    maxExtent: extent,
                    restrictedExtent: extent,
                    numZoomLevels: 1,
                    projection: proj,
                    units: units,
                    theme: null // or OpenLayers will try to load its
                                // default theme
                },
                layers: [
                    new OpenLayers.Layer.Image(
                        "Overview", GeoSIE.Config.ROOT_PATH + info.url, info.extent, info.size
                    )
                ],
                size: info.size
        };
        
        this.overviewControl = new OpenLayers.Control.OverviewMap(overViewOptions);
        this.map.addControl(this.overviewControl);
    },

    /**
     * Method: hideLocator
     * Hide or show the locator widget (overview map and zoombar).
     * 
     * Parameters:
     * evt - {Event}
     */    
    hideLocator: function (evt){
        // get the Locator (Zoom + overViewMap)
    	var img = document.getElementById('hide1');
        var zoom = document.getElementById('zoomBar');
        var overView = document.getElementById("locatorMap");

        // find if it is hide
        var hide = img.src.indexOf('off.png')>0;
       
        // if not hide
        if (hide) {
            // display it and change the bottom icon
        	zoom.style.display = '';
            overView.style.display = '';
            img.title='Fermer la mini carte',
            img.src= GeoSIE.Config.ROOT_PATH + 'images/locator_on.png';
        } else {
            img.src= GeoSIE.Config.ROOT_PATH + 'images/locator_off.png';
            img.title='Ouvrir la mini carte',
            zoom.style.display = 'none';
            overView.style.display = 'none';
        }
        if (evt != null) {
            OpenLayers.Event.stop(evt);
        }	
    },    
    
    /**
     * Method: consult
     * Triggers the event "consult" with the given feature.
     * 
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>}
     */
    consult: function(feature){
        console.log("consult called: ");
        this.events.triggerEvent("consult", feature);
    },

    /**
     * Method: suppression
     * Triggers the event "suppression".
     */
    suppression: function(){
        this.events.triggerEvent("suppression", this.getSelection());
    },

    /**
     * Method: impression
     * Triggers print control.
     */    
    impression: function(){
    	this.printControl.trigger();
    }
    
};// end of GeoSIE.Brique