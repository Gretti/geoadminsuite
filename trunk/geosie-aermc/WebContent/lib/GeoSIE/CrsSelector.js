
/**
 * Class: GeoSIE.CrsSelector
 * The CrsSelector object represents a widget for an {<OpenLayers.Map>} instance. 
 * It permits selecting a Coordinate Representation System (CRS) for a given world zone currently in display.
 * This widget displays the CrsSelector alongside the CursorTrack.
 * 
 * How to use this widget: 
 * 
 * - insert this code into your layout (ie: viewer.html after the div with id=cursorTrack): 
 * 
 * > <div  id="crsSelector" style="position: absolute; left: 42%;"></div>
 * 
 * - include lib/GeoSIE/CrsSelector.js in viewer.html
 * - add a crsSelector property with a null value after the null map property in Brique.js
 * - add this block of code in the Brique.js in addWidgets() method:
 * 
 * (start code)
 *      this.crsSelector = new GeoSIE.CrsSelector({
 *      	div: OpenLayers.Util.getElement("crsSelector"),
 *      	map: this.map       	
 *      });
 * (end)      
 */

GeoSIE.CrsSelector = OpenLayers.Class({
	
	/**
	 * APIProperty: map
	 * {<OpenLayers.Map} Map instance on which the CrsSelector will appear. 
	 */
    map: null,
    
    /**
     * Property: id
     */
    id: null,

    /**
     * Property: currentZone
     * {Object} The current zone
     */    
    currentZone: null,

    /**
     * Property: options
     */
    options: '',    
    
    /**
     * Property: containerTemplate
     */
    containerTemplate: 
        '<div id="${id}"> Chargement des SRS de la zone ...</div>',
        
    /**
     * Property: optionTemplate
     */    
    optionTemplate: 
    	'<option value="${crs_epsgCode}"${crs_selected}>${crs_label}</option>',
        	
    /**
     * Property: mainTemplate
     */	
    mainTemplate:
    	'<span style="font-size: xx-small;">SRS&nbsp;:&nbsp;</span>' + 
        '<select id="crsSelector_crs" style="float: none;" onchange="GeoSIE.Brique.crsSelector.setCursorTrackCRS(document.getElementById(\'crsSelector_crs\').value)">' + 
        '	${crs_options}' + 
        '</select>',
    	
    /**
     * Constructor: GeoSIE.CrsSelector
     * Constructor for a CrsSelector widget
     *  
     * Parameters: 
     * options - {Object} Options for control
     * 
     * Returns: 
     * {<GeoSIE.CrsSelector>}
     */
    initialize : function(options) {
        OpenLayers.Util.extend(this, options);
        
        if (this.id == null) {
            this.id = OpenLayers.Util.createUniqueID("CrsSelectorContainer_");
        }

        // Redraw on Zone Change
        GeoSIE.ZoneManager.events.on({
            'zonechanged': this.redraw,
            scope: this
        });
        
        this.div = OpenLayers.Util.getElement(this.div);

        this.div.innerHTML = OpenLayers.String.format(this.containerTemplate, {
            id: this.id
        });
    },
  
    /**
     * Destructor: destroy
     * To call upon destroying this instance (for cleaning registered listeners first)
     */
    destroy: function() {
    	GeoSIE.ZoneManager.events.un({
    		'zonechanged': this.redraw,
    		scope: this
    	});
    },
    
    /**
     * Method: redraw
     * Redraw the informations onscreen. This will call draw() at the end.
     */
    redraw: function(evt) {
    	this.currentZone = evt;
    	this.draw();
    },
    
    /**
     * Method: draw
     * Draw the informations onscreen
     */
    draw: function() {
		var elements = $('#' + this.id);
		var div = elements[0];
		elements.empty();
		this.refreshCrsOptions();
		div.innerHTML += OpenLayers.String.format(this.mainTemplate, {
			crs_options: this.options
		});
		return true;
    },
    
    /**
     * Method: refreshCrsOptions
     * Refresh the options of the CRS Selector depending of the possibilities within the selected world zone.
     */
    refreshCrsOptions: function(){
    	var i, selected, selectableCRS = '';
    	var cursorTrackCrsList = this.currentZone.cursorTrackCrs;
    	// TODO : j utilise le projection de la carte
    	// car si je change de zone this.getCursorTrackCRS peut retourner la projection  
    	// du mousePositionControl de l'ancienne zone (exemple, j'arrive sur france a l'init
    	// puis je choisis la guyane, this.getCursorTrackCRS me retourne du lambert2 etendu ),
    	var currentCRS = this.map.projection; //this.getCursorTrackCRS(); 
		
    	this.options = '';
    	// 1st pass: is the current cursortrack CRS a selectable ones ?
    	for(i=0;i<cursorTrackCrsList.length;i++){
    		if(currentCRS==cursorTrackCrsList[i].epsgCode){
    			selectableCRS=currentCRS;
    			break;
    		}
//    		selectableCRS = (selectableCRS==='' && currentCRS==cursorTrackCrsList[i].epsgCode) 
//    						? currentCRS 
//    						: '';
    	}

    	// 2nd pass: building the select options with preselected currentCRS or first one selected
    	for(i=0;i<cursorTrackCrsList.length;i++){
    		selected = ((i==0 && selectableCRS==='') || (currentCRS==cursorTrackCrsList[i].epsgCode)) 
    					? ' selected="selected" ' 
    					: '';
    		this.options += OpenLayers.String.format( this.optionTemplate, {
    			crs_epsgCode: cursorTrackCrsList[i].epsgCode,
    			crs_selected: selected,
    			crs_label: cursorTrackCrsList[i].label
    		});
    		// First CRS is to be selected, so will be set the MousePosition cursorTrack
    		if ( i==0 && selected !== '' ){
    			currentCRS = cursorTrackCrsList[i].epsgCode;
    		}
    	}    	

		this.setCursorTrackCRS( currentCRS );
    },
    
    /**
     * Method: getDigitsForEpsgCode
     * Retrieve the number of digits (after the dot) to use while displaying coordinates 
     * in the given units system.
     * @returns {Integer} number of digits
     */
    getDigitsForEpsgCode: function( epsgCode ){
    	var i, cursorTrackCrsList = this.currentZone.cursorTrackCrs;
    	
    	for(i=0;i<cursorTrackCrsList.length;i++){
    		if ( cursorTrackCrsList[i].epsgCode == epsgCode ){
    			return cursorTrackCrsList[i].numDigits;
    		}
    	}    
    	return 2; // default value if not set up
    },
    
    /**
     * Method: setCursorTrackCRS
     * Set the CRS to use for displaying coordinates, given by its epsgCode
     */
    setCursorTrackCRS: function( epsgCode ){
    	var numDigits = parseInt( this.getDigitsForEpsgCode( epsgCode ), 10);
    	var mousePositionControl = this.map.getControlsByClass('OpenLayers.Control.MousePosition');
    	if ( mousePositionControl.length >= 0 ){
    		mousePositionControl[0].displayProjection = new OpenLayers.Projection(epsgCode);
    		mousePositionControl[0].numDigits = numDigits;
    		mousePositionControl[0].redraw(null);
    	}
    },
    
//    /**
//     * Method: getCursorTrackCRS
//     */
//	getCursorTrackCRS: function(){
//    	var mousePositionControl = GeoSIE.Brique.map.getControlsByClass('OpenLayers.Control.MousePosition');
//    	if ( mousePositionControl.length >= 0 ){
//    		if ( mousePositionControl[0].displayProjection ){
//    			return mousePositionControl[0].displayProjection.projCode;
//    		}else{
//    			return GeoSIE.Brique.map.projection;
//    		}
//    	}
//    	return GeoSIE.Brique.map.Projection.projCode;
//	},
    
    CLASS_NAME: 'GeoSIE.CrsSelector'
});