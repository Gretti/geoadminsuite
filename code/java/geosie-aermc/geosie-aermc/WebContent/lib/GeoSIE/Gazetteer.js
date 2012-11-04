
/**
 * Class: GeoSIE.Gazetteer	
 * The Gazetteer object represents a widget for an {<OpenLayers.Map>} instance. 
 * It allows the user to:
 * - choose a coordinate representation system (CRS) to express a spatial position
 * - enter the coordinates following the selected CRS
 * - zoom to the designated area by centering the map on the coordinates
 * - look for a GeoName, then zoom to the position
 * This widget displays a gazetteer form.
 * 																									
 * How to use this widget: 																		
 * 																									
 * - insert a div with id=gazetteer into your layout (ie: viewer.html jQuery accordion)				
 * - include lib/GeoSIE/Gazetteer.js in viewer.html														
 * - include style/gazetteerStyle.css in viewer.html												
 * - add a gazetteer property with a null value after the null map property in Brique.js		
 * - add this block of code in the Brique.js in addWidgets() method:			
 * (start code)					
 * 		// gazetteer																				
 *		this.gazetteer = new GeoSIE.Gazetteer({															
 *			div: OpenLayers.Util.getElement("gazetteer"),											
 *			map: this.map																			
 *		});   																						
 * (end)
 */

GeoSIE.Gazetteer = OpenLayers.Class({
	
	/**
	 * APIProperty: map
	 * {<OpenLayers.Map>} map instance used for displaying this Gazetteer
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
     * Property: containerTemplate
     */
    containerTemplate: 
        '<div id="${id}"> Refreshing the panel ...</div>',
        
    /**
     * Property: optionTemplate
     */    
    optionTemplate: 
    	'<option value="${crs_epsgCode}">${crs_label}</option>',
    
    /**
     * Property: mainTemplate
     */	
    mainTemplate:
        '<br/><br/>' + 
        '<div class="title"> Par coordonnées :</div><br/>' + 
        '<div style="overflow: hidden;white-space:nowrap;">' + 
        '    <div>' + 
        '        <span style="margin-left: 21px;">SRS : </span>' + 
        '        <select id="gazetteer_crs" style="float:none;width:150px;" onchange="GeoSIE.Brique.gazetteer.switchToCrs(document.getElementById(\'gazetteer_crs\').value)">' + 
        '	     ${crs_options}' +
        '        </select>' + 
        '    </div>' + 
        '    <div style="margin-top:2px;">Coord X : <input style="width:90px;" type="text" id="gazetteer_coordx"/>&nbsp;<span id="gazetteer_units_x">${crs_units}</span></div>' + 
        '    <div style="margin-top:2px;">Coord Y : <input style="width:90px;" type="text" id="gazetteer_coordy"/>&nbsp;<span id="gazetteer_units_y">${crs_units}</span></div><br/>' +
        '</div>' + 
        '<center>' + 
        '    <span class="divSubmit"' +  
        '             onclick="GeoSIE.GoTo.point(document.getElementById(\'gazetteer_coordx\').value, document.getElementById(\'gazetteer_coordy\').value,document.getElementById(\'gazetteer_crs\').value,document.getElementById(\'gazetteer_units_x\').innerHTML);" >' + //, \'EPSG:27572\');" >' + 
        '       Recentrer la carte' + 
        '    </span>' + 
        '</center>' + 
        '<br/>' + 
        '<hr/>' + 
        '<br/>' + 
        '<div class="title"> Par nom de commune :</div><br/>' + 
        '<div style="text-align: center;"><input type="text" id="gazetteer_goToText"/></div>' + 
        '<br/>' + 
        '<center>' + 
        '    <span class="divSubmit"' + 
        '         onclick="GeoSIE.GoTo.search(document.getElementById(\'gazetteer_goToText\').value);" >' + 
        '       Aller à' + 
        '    </span>' + 
        '</center>' + 
        '<br/>' + 
        '<div id="gotoResultDiv"></div>',
    	
    /**
     * Constructor: GeoSIE.Gazetteer
     * Constructor for a Gazetteer
     * 
     * Parameters: 
     * options - {Object} Options for control.
     * 
     * Returns: 
     * {<GeoSIE.Gazetteer>}
     */
    initialize : function(options) {
        OpenLayers.Util.extend(this, options);
        
        if (this.id == null) {
            this.id = OpenLayers.Util.createUniqueID("GazetteerContainer_");
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
     */
    destroy: function(){
    	GeoSIE.ZoneManager.events.un({
            'zonechanged': this.redraw,
            scope: this
        });    	
    },
    
    /**
     * Method: redraw
     */
    redraw: function(evt){
    	this.currentZone = evt;
    	this.drawOnce();
    },
    
    /**
     * Method: drawOnce
     */
    drawOnce: function() {
    	var i;
        var elements = $('#' + this.id);
        var div = elements[0];
        var units = '';
        var options = '';
        elements.empty();
    	var gazetteerCrsList = this.currentZone.gazetteerCrs;
    	for(i=0;i<gazetteerCrsList.length;i++){
    		options += OpenLayers.String.format( this.optionTemplate, {
    			crs_epsgCode: gazetteerCrsList[i].epsgCode,
    			crs_label: gazetteerCrsList[i].label
    		});
    		if ( units === '' ){
    			units = gazetteerCrsList[i].units;
    		}
    	}
        div.innerHTML += OpenLayers.String.format(this.mainTemplate, { 
        	crs_options: options, 
        	crs_units: units
        });
        
        // Listen key enter for input text
        $('#gazetteer_goToText').bind('keypress', function(e) {
            if(e.keyCode==13){
            	GeoSIE.GoTo.search($('#gazetteer_goToText').val());
            }
        });
        
        // Auto complete
        // cf http://1300grams.com/2009/08/17/jquery-autocomplete-with-json-jsonp-support-and-overriding-the-default-search-parameter-q/
        var countries = GeoSIE.ZoneManager.getZonesCodes();

        $("#gazetteer_goToText").autocomplete("http://ws.geonames.org/searchJSON", {
        	   dataType: 'jsonp',
        	   parse: function(data) {
	        	     var rows = new Array();
	        	     if(data){
		        	     data = data.geonames;
		        	     for(var i=0; i<data.length; i++){
		        	       rows[i] = { data:data[i], value:data[i].name, result:data[i].name };
		        	     }
		        	     return rows;	        	    	 
	        	     }
        	     },
        	     formatItem: function(row, i, n) {
        	       return row.name + (row.adminName1 ? ", " + row.adminName1 : "");
        	     },
        	     extraParams: {
        	       // geonames doesn't support q and limit, which are the autocomplete plugin defaults, so let's blank them out.
        	       q: '',
        	       limit: '',
        	       featureClass: 'A',
        	       country: countries,
        	       style: 'full',
        	       lang: "fr",
        	       maxRows: GeoSIE.Config.MAX_RESULT_GEONAMES+20,
        	       name_startsWith: function () { return $("#gazetteer_goToText").val();}
        	     },
        	     max: 50
        });        
        
    },
    
    /**
     * Method: switchToCrs
     */
    switchToCrs: function( epsgCode ){
    	var i;
    	var gazetteerCrsList = this.currentZone.gazetteerCrs;
    	for(i=0;i<gazetteerCrsList.length;i++){
    		if ( gazetteerCrsList[i].epsgCode == epsgCode )
    		{
    			OpenLayers.Util.getElement('gazetteer_units_x').innerHTML = gazetteerCrsList[i].units;
    			OpenLayers.Util.getElement('gazetteer_units_y').innerHTML = gazetteerCrsList[i].units;
    			break;
    		}
    	}		
    },
    
    CLASS_NAME: 'GeoSIE.Gazetteer'
});
