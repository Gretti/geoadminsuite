/*
 * @requires OpenLayers/Control/Button.js
 */

/**
 * Class: GeoSIE.Control.Link
 * A link button to a page.
 * 
 * How to use this control: 
 * 
 * - include lib/GeoSIE/Control/Link.js in viewer.html
 * - add this control to the map in Brique.js (cf addControls method)
 * with displayClass 'GeoSIEControlContactItemInactive' for a contact page
 * or 'GeoSIEControlHelpItemInactive' for a help page: 
 * (start code)
 * 		var helpControl = new GeoSIE.Control.Link({
 *       	displayClass: 'GeoSIEControlHelp',
 *       	title: GeoSIE.Messages.helpToolTip,
 *       	url: GeoSIE.Config.URL_HELP
 *      });
 * (end)
 * 
 * Inherits from:
 * - <OpenLayers.Control.Button>
 */

GeoSIE.Control.Link = OpenLayers.Class(OpenLayers.Control.Button, {
	
	/**
	 * APIProperty: url 
	 * {String} The URL for the link.
	 */	
	url: "",
	
	/**
	 * APIProperty: target 
	 * {String} Target for the link. Default is _blank.
	 */		
	target: "_blank",
		
    /**
     * Constructor: GeoSIE.Control.Link
     * Constructor for a control to have a link.
     * 
     * Parameters:
     * options - {Object} 
     * 
     * Returns: 
     * {<GeoSIE.Control.Link>}
     */		
	initialize : function(options) {
		OpenLayers.Util.extend(this, options);

		OpenLayers.Control.Button.prototype.initialize.apply(this, [ options ]);
	},
	
    /**
     * Method: draw
     * Initialize control.
     * 
     * Returns: 
     * {DOMElement} A reference to the DIV DOMElement containing the control.
     */    
    /*draw: function() {
        OpenLayers.Control.Button.prototype.draw.apply(this, arguments);
        this.link = document.createElement("a");
        this.link.title = this.title;
        this.link.target = this.target;
        this.link.href = this.href;
        //var html = '<a href="http://www.eaufrance.fr" title="Eaufrance, le portail" target="_blank"> </a>';
        var html ='<a title="Aide" target="_blank" href="http://www.google.fr">'+
        '<img alt="Aide" src="/GeoSIE-Brique/style/images/aide_off.png">'+
        '</a>';
        if(this.panel_div){
            //this.panel_div.appendChild(this.link);
            this.panel_div.innerHTML = html;

        }
        
        return this.div;
    },*/

	/**
	 * Method: trigger
	 * Open the a window with the url
	 */
    trigger: function() {
    	window.open(this.url, this.target);
    },
    
	CLASS_NAME : 'GeoSIE.Control.Link'
});